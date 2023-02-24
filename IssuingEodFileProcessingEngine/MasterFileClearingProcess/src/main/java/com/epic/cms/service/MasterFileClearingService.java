/**
 * Author :
 * Date : 2/3/2023
 * Time : 11:46 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.MasterFileClearingDao;
import com.epic.cms.Exception.RejectException;
import com.epic.cms.model.bean.FileBean;
import com.epic.cms.model.bean.MasterFieldsDataBean;
import com.epic.cms.model.bean.MasterRejectBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.*;
import org.jpos.iso.ISOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.UUID;

import static com.epic.cms.util.LogManager.*;

@Service
public class MasterFileClearingService {
    @Autowired
    public LogManager logManager;
    @Autowired
    public MasterFileClearingDao masterFileClearingDao;
    @Autowired
    public StatusVarList status;
    @Autowired
    public CommonRepo commonRepo;
    @Autowired
    public MasterExtractElementService masterExtractElementService;

    /**
     * read file
     *
     * @param fileBean
     */
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void processFile(FileBean fileBean) {
        MasterFieldsDataBean masterFieldsBean = new MasterFieldsDataBean();
        MasterRejectBean rejectBean = new MasterRejectBean();
        String fileId = null;
        InputStream is = null;
        LinkedHashMap<String, Object> details = new LinkedHashMap<>();
        String print = null;
        int presentmentTxnCount = 0;

        File file;
        try {
            // get file from uploaded location
            file = new File(Configurations.PATH_MASTER_FILE + File.separator + fileBean.getFileName());
            fileId = fileBean.getFileId();

            // Make sure file exist
            if (file.isFile() && file.exists() && file.canRead()) {
                Configurations.PROCESS_MASTER_FILE = true;

                print = "Master file identified..."
                        + "\nFile Name : " + file.getName()
                        + "\nFile ID   : " + fileId;

                infoLoggerEFPE.info(logManager.processHeaderStyle(print));

                String masterFileId = fileId;
                String fileName = fileBean.getFileName();

                /**
                 * ------------------------------Input File reading-----------------------------------------------------
                 */
                //update the master file processing start time.
                masterFileClearingDao.updateFileStartTime(fileId);

                is = new FileInputStream(file);

                if (!file.exists()) {
                    infoLoggerEFPE.info(logManager.processHeaderStyle("Error : File does not exist"));
                } else if (!file.isFile()) {
                    infoLoggerEFPE.info(logManager.processHeaderStyle("Error : File is not a file type"));
                } else if (!file.canRead()) {
                    infoLoggerEFPE.info(logManager.processHeaderStyle("Error : File is unreadable"));
                } else {
                    long length = file.length();
                    infoLoggerEFPE.info(logManager.processHeaderStyle("File size : " + length + " ( bytes )"));
                    if (length <= 0) {
                        infoLoggerEFPE.info(logManager.processHeaderStyle("Error : Empty file"));
                    } else if (length > Integer.MAX_VALUE) {
                        infoLoggerEFPE.info(logManager.processHeaderStyle("Error : File size too large"));
                    } else {
                        //update file status to FREAD
                        masterFileClearingDao.updateFileStatus(fileId, DatabaseStatus.STATUS_FILE_READ);
                        print = "Master File [ " + fileName + " ] accepted for processing.";
                        infoLoggerEFPE.info(logManager.processHeaderStyle(print));

                        byte[] bytes = new byte[(int) length];

                        int offset = 0;
                        int numRead = 0;
                        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                            offset += numRead;
                        }

                        if (offset < bytes.length) {
                            details.put("Error : Could not completely read the file", "");
                            errorLoggerEFPE.error("Master File Reading Process: Could not completely read the file");
                            return;
                        }

                        //get the hexa string
                        String hexString = ISOUtil.hexString(bytes);
                        /*All file consists of records. Each record is prefixed with a 4 byte binary length. There are no carriage returns or line
                            feeds in the file. Before sending, the contents is blocked into lengths of 1012, and an additional 2 x‘40’ characters are
                            appended at each block. Finally, the total file length is made a multiple of 1014 with the final incomplete record being
                            filled with the x‘40’ character

                            in EBCIDIC file it will fill from x'00' not x'40'
                             */
                        //remove x'00' or x'40' block separator characters
                        StringBuilder sb = new StringBuilder(hexString);
                        for (int j = 2024; j < sb.length(); j += 2024) {
                            sb = sb.delete(j, (j + 4));
                        }

                        hexString = sb.toString();
                        print = "Initialized the Master Card Extractor : " + MasterExtractElementService.initialize();
                        infoLoggerEFPE.info(print);
                        int recordLengthInt = 0;
                        int count = 0;

                        while (true) { //recurse the transactions
                            try {
                                String recordLengthStr = "";
                                if (hexString.length() >= 8) {
                                    recordLengthStr = hexString.substring(0, 8); //first 8 characters represents the particular record length
                                } else {
                                    infoLoggerEFPE.info("File reading finished");
                                    break;
                                }
                                recordLengthInt = Integer.parseInt(recordLengthStr, 16); // convert record length to a integer value
                                if (recordLengthInt == 0) {
                                    infoLoggerEFPE.info("File reading finished");
                                    break;
                                }
                                String transactionDataRecord = hexString.substring(8, ((recordLengthInt * 2) + 8)); //get the record data without the length part

                                hexString = hexString.substring(((recordLengthInt * 2) + 8)); //assign remaining file part as new hexa string

                                // set values to txn bean
                                masterFieldsBean = new MasterFieldsDataBean();
                                masterFieldsBean.setFileid(masterFileId);
                                masterFieldsBean.setLineNumber(String.valueOf(count));
                                masterFieldsBean.setTxnId(setMasterTxnId(count));
                                masterFieldsBean.setStatus("ACCEPT");

                                // set values to reject details bean
                                rejectBean = new MasterRejectBean();
                                rejectBean.setFileId(masterFileId);
                                rejectBean.setLineNumber(String.valueOf(count));
                                rejectBean.setLineContent(transactionDataRecord);

                                // extract the record elements and insert to recmasterfieldidentity & recmastertransaction
                                masterExtractElementService.doUnpack(transactionDataRecord, masterFieldsBean);
                                Thread.sleep(10);

                                // insert record data to tables (RecMasterInputRowData, RecMasterFileSplit, RecMasterTransaction)
                                if (masterFieldsBean.getMti() != null && masterFieldsBean.getMti().equals(Configurations.FIRST_PRESENTMENT_MTI)) { //1240
                                    masterFileClearingDao.insertFileDetailsIntoEODMasterInputRowData(masterFileId, String.valueOf(count), transactionDataRecord);
                                    masterFileClearingDao.insertFileDetailsIntoEODMasterFieldIdentity(masterFieldsBean);

                                    //check for valid card in card table
                                    if (commonRepo.checkForValidCard(masterFieldsBean.getPan())) {
                                        masterFileClearingDao.insertFileDetailsIntoEODMasterTransaction(masterFieldsBean);
                                    } else {
                                        try {
                                            // Insert transaction into EodExceptionalTransaction table
                                            masterFileClearingDao.insertExceptionalTransactionData(masterFileId, masterFieldsBean.getTxnId(), "", masterFieldsBean.getPan(), masterFieldsBean.getApprovalCode(),
                                                    masterFieldsBean.getAcceptoerId(), masterFieldsBean.getTxnAmount(),
                                                    masterFieldsBean.getTxnCurrencyCode(), masterFieldsBean.getTxnDate(), masterFieldsBean.getTxnTime(), "", Configurations.EOD_USER, new java.sql.Date(System.currentTimeMillis()), masterFieldsBean.getBillingAmount(),
                                                    masterFieldsBean.getBillingCurrencyCode(), "", masterFieldsBean.getAcceptorName(), masterFieldsBean.getMerchantCity(), masterFieldsBean.getMerchantCountryCode(), masterFieldsBean.getAcceptorBusinessCode(), "", "", "", masterFieldsBean.getAcceptorTerminalId(), "", "MASTER");
                                        } catch (Exception e) {
                                            errorLoggerEFPE.error("Unable to insert exceptional master transaction record: ", e);
                                        }
                                    }
                                    presentmentTxnCount++;
                                }
                                count++;
                            } catch (RejectException ex) {
                                errorLoggerEFPE.error("Master File Transaction Rejected: ", ex);
                                print = "Master file transaction rejected - ";
                                infoLoggerEFPE.info(print);
                                findRejectedFields(ex.getMessage(), rejectBean);
                                try {
                                    masterFileClearingDao.insertRejectedMasterDetails(rejectBean, Configurations.EOD_USER);
                                } catch (Exception e) {
                                    throw e;
                                }
                                break;
                            }
                        }

                        masterFileClearingDao.updateFileRecordCount(masterFileId, String.valueOf(count));
                        masterFileClearingDao.updateFileStatus(masterFileId, DatabaseStatus.STATUS_FILE_COMP);
                        masterFileClearingDao.updateFileTxnCount(masterFileId, String.valueOf(masterFileClearingDao.loadMasterTransactionCount(masterFileId)));

                        print = "Master File processing completed... \n Total Presentments: " + presentmentTxnCount;
                        infoLoggerEFPE.info(print);

                        Configurations.PROCESS_MASTER_FILE = false;
                    }
                }

            } else {
                masterFileClearingDao.updateFileStatus(fileId, DatabaseStatus.STATUS_FILE_ERROR);

                print = "Master file not found..."
                        + "\nFile Name : " + file.getName()
                        + "\nFile ID   : " + fileId;

                infoLoggerEFPE.info(print);
            }
        } catch (Exception ex) { //exception occured in considering ipm file
            errorLoggerEFPE.error("Error Occured in Master transaction: ", ex);
            print = "Master File rejected...";
            infoLoggerEFPE.info(print);
            try {
                masterFileClearingDao.updateFileStatus(fileId, DatabaseStatus.STATUS_FILE_REJECT);
            } catch (Exception e) {
                errorLoggerEFPE.error("Master File Reading Process: ", e);
            }
        } finally {
            try {
                //infoLoggerEFPE.info(logManager.processDetailsStyles(details));
                System.out.flush();
                if (is != null) {
                    is.close();
                }
                is = null;
                file = null;

                //nullify masterFields Bean and PAN
                CommonMethods.clearStringBuffer(masterFieldsBean.getPan());
                masterFieldsBean = null;
            } catch (Exception ex) {
                errorLoggerEFPE.error("Master File Reading Process: ", ex);
            }
        }
    }

    public synchronized String setMasterTxnId(int lineNumber) throws Exception {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    /**
     * @param errorMsg
     * @throws Exception
     */
    public synchronized void findRejectedFields(String errorMsg, MasterRejectBean rejectBean) throws Exception {

        String fieldId = "";

        char[] charArray = errorMsg.toCharArray();
        for (int x = 0; x < charArray.length; x++) {
            try {
                fieldId += String.valueOf(Integer.parseInt(String
                        .valueOf(charArray[x])));
            } catch (NumberFormatException e) {
            }
        }
        rejectBean.setFieldId(fieldId);
    }
}
