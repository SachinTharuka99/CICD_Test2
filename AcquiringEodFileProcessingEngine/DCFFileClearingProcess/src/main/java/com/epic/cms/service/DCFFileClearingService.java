/**
 * Author : rasintha_j
 * Date : 7/13/2023
 * Time : 10:00 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.PaymentFileDataBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import com.epic.cms.dao.DCFFileClearingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.epic.cms.validation.PaymentValidations.isNumeric;
import static com.epic.cms.validation.PaymentValidations.isValidPaymentFileDate;

@Service
public class DCFFileClearingService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    public LogManager logManager;
    @Autowired
    public StatusVarList status;
    @Autowired
    private DCFFileClearingDao dcfFileReadDao;
    private String recordContent;
    private int noofrecords;
    private FileInputStream fileInputStream = null;
    private BufferedReader bufferReader = null;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void fileProcess(PaymentFileDataBean fileBean, String isFileNameValid, String filepath, AtomicInteger failedFileNameCount, AtomicInteger failedFileCount) throws Exception {
        LinkedHashMap details = new LinkedHashMap();
        try {
            if (isFileNameValid.isEmpty()) {

                File file = new File(filepath + File.separator + fileBean.getFilename());
                fileInputStream = new FileInputStream(file);
                bufferReader = new BufferedReader(new InputStreamReader(fileInputStream));

                dcfFileReadDao.updateEODDCFFILE(fileBean.getFileid());

                noofrecords = 0;
                int batchNo = 0;
                while ((recordContent = bufferReader.readLine()) != null) {
                    if (recordContent.length() >= 4 && Character.isDigit(recordContent.charAt(0))) {
                        noofrecords++;
                        String FieldType = recordContent.substring(0, 4);
                        if (FieldType.equals("6200") || FieldType.equals("6220") || FieldType.equals("6240")) {
                            batchNo++;
                        }
                        dcfFileReadDao.insertToRECDCFINPUTROWDATA(fileBean.getFileid(), noofrecords, recordContent, batchNo, FieldType);
                    }
                }

                dcfFileReadDao.dcfFileSplitter();//split record content into fields by procedure
                dcfFileReadDao.updateEODDCFFILE(noofrecords, Configurations.COMPLETE_STATUS, fileBean.getFileid());

                details.put("File Id ", fileBean.getFileid());
                details.put("File Name ", fileBean.getFilename());
                details.put("Number records ", noofrecords);

            } else {
                failedFileNameCount.addAndGet(1);
                logError.error("DCF file read process failed for file " + fileBean.getFileid() + " , " + isFileNameValid);
                dcfFileReadDao.updateEODDCFFILE(noofrecords, Configurations.FAIL_STATUS, fileBean.getFileid());
            }
        } catch (IOException e) {
            logError.error("DCF file read process failed for file " + fileBean.getFileid(), e);
            dcfFileReadDao.updateEODDCFFILE(noofrecords, Configurations.FAIL_STATUS, fileBean.getFileid());
            failedFileCount.addAndGet(1);
        } catch (Exception e) {
            logError.error("DCF file read process failed for file " + fileBean.getFileid(), e);
            dcfFileReadDao.updateEODDCFFILE(noofrecords, Configurations.FAIL_STATUS, fileBean.getFileid());
            failedFileCount.addAndGet(1);
        } finally {
            if (bufferReader != null) {
                bufferReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        }
    }

    public String fileNameValidation(ArrayList<String> nameFields, String filename) throws Exception {
        String isValid = "";
        String[] fields = filename.split("[_.]");

        if (!fields[0].trim().equals(nameFields.get(0))) {
            isValid = "Invalid File Prefix";
        } else if (!isValidPaymentFileDate(fields[1].trim())) {
            isValid = "Invalid File Date Prefix";
        } else if (!isNumeric(fields[2].trim())) {
            isValid = "Invalid File Sequence";
        } else if (!fields[3].trim().equals(nameFields.get(1))) {
            isValid = "Invalid File Postfix";
        } else if (!fields[4].trim().equals(nameFields.get(2))) {
            isValid = "Invalid File Extension";
        }

        return isValid;
    }
}
