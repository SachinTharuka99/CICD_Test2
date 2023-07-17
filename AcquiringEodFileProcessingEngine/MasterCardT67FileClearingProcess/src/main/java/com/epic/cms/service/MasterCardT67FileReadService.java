/**
 * Author : rasintha_j
 * Date : 7/10/2023
 * Time : 1:24 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.dao.MasterCardT67FileReadDao;
import com.epic.cms.model.bean.EODInputFileDetailBean;
import com.epic.cms.model.bean.IP0040T1Bean;
import com.epic.cms.model.bean.IP0075T1Bean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

@Service
public class MasterCardT67FileReadService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    MasterCardT67FileReadDao masterCardT67FileReadDao;

    String print = null;

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void FileAvailabilityCheck(File inputFile, int fileCount) {
        try {
            try {
                String md5Checksum = "";
                EODInputFileDetailBean fileBean = new EODInputFileDetailBean();
                if (inputFile.isFile() && inputFile.canRead()) { // check the file is readable or not
                    // Calculate file checksum
                    try (InputStream is = Files.newInputStream(Paths.get(inputFile.getAbsolutePath()))) {
                        md5Checksum = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
                    }

                    // check the calculated file checksum already exist in the table (file already exists or not?)
                    boolean inputFileExists = masterCardT67FileReadDao.isInputFileExists(inputFile.getName());
                    if (!inputFileExists) { //file is not already uploaded

                        //set file details to file bean (fileid, filename,filetype,checksum)
                        fileBean.setFileId(generateFileId(fileCount));
                        fileBean.setFileName(inputFile.getName());
                        fileBean.setCheckSum(md5Checksum);
                        //insert record to EODINPUTFILES table INIT record
                        masterCardT67FileReadDao.insertRecordToEODMASTERT67FILE(fileBean);

                        print = "T67/T68 file queued for processing: ".concat(inputFile.getName());
                        logInfo.info(print);
                    }
                }
            } catch (Exception ex) {
                logError.error("failed identifing T67/68 file from location: ", ex);
            }
        } catch (Exception ex) {
            logError.error("identifing T67/68  rollback failed ", ex);
            logError.error("failed identifing T67/68 file from location: ", ex);
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void IP0040T1UnpackThread(ArrayList<String> ip0040T1RecordList, int ip0040T1KeyStart, int ip0040T1KeyLength, int totalIP0040T1RecordsProcessedByThread, String fileId) {
        int count = -1;

        try {
            for (String ip0040t1Record : ip0040T1RecordList) {
                count++;
                try {
                    masterCardT67FileReadDao.updateOrInsertMasterIP0040T1Data(unpackIP0040T1(ip0040t1Record, ip0040T1KeyStart, ip0040T1KeyLength), fileId);

                } catch (Exception ex) {
                    logError.error("Error writing record", ex);
                }
            }
            totalIP0040T1RecordsProcessedByThread += ip0040T1RecordList.size();
            print = "Account Range Processed: ".concat(Integer.toString(totalIP0040T1RecordsProcessedByThread));
            logInfo.info(print);
        } catch (Exception ex) {
            logError.error("IP0040T1UnpackThread  rollback failed ", ex);
            logError.error("IP0040T1UnpackThread failed for Record: ".concat(ip0040T1RecordList.get(count)), ex);
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void IP0075T1UnpackThread(ArrayList<String> ip0075T1RecordList, int ip0075T1KeyStart, int ip0075T1KeyLength, int totalIP0075T1RecordsProcessedByThread, String fileId) {
        int count = -1;

        try {
            for (String ip0075t1Record : ip0075T1RecordList) {
                count++;
                try {
                    masterCardT67FileReadDao.updateOrInsertMasterIP0075T1Data(unpackIP0075T1(ip0075t1Record, ip0075T1KeyStart, ip0075T1KeyLength), fileId);

                } catch (Exception ex) {
                    logError.error("Error writing record", ex);
                }
            }
            totalIP0075T1RecordsProcessedByThread += ip0075T1RecordList.size();
            print = "MCC Processed: ".concat(Integer.toString(totalIP0075T1RecordsProcessedByThread));
            logInfo.info(print);
        } catch (Exception ex) {
            logError.error("IP0075T1UnpackThread  rollback failed ", ex);
            logError.error("IP0075T1UnpackThread failed for Record: ".concat(ip0075T1RecordList.get(count)), ex);
        }
    }

    private String generateFileId(int fileSequenceNo) {
        String fileId = "";
        try {
            fileId = "T".concat(Integer.toString(Configurations.EOD_ID).substring(0, 6)).concat(ISOUtil.zeropad(fileSequenceNo + "", 3));
        } catch (ISOException ex) {
            logError.error("T67/68 File Read Process", ex);
        }
        return fileId;
    }

    private IP0040T1Bean unpackIP0040T1(String ip0040t1Record, int keyStart, int keyLength) {
        try {
            IP0040T1Bean ip0040t1 = new IP0040T1Bean();
            ip0040t1.setKey(ip0040t1Record.substring(keyStart - 1, keyLength - 8).substring(8));
            ip0040t1.setEffectiveTimeStamp(ip0040t1Record.substring(0, 7));
            ip0040t1.setActiveInactiveCode(ip0040t1Record.substring(7, 8));
            ip0040t1.setTableID(ip0040t1Record.substring(8, 11));
            ip0040t1.setLowAccountRange(ip0040t1Record.substring(11, 30));
            ip0040t1.setGCMSProductID(ip0040t1Record.substring(30, 33));
            ip0040t1.setHighAccountRange(ip0040t1Record.substring(33, 52));
            ip0040t1.setCardProgramIdentifier(ip0040t1Record.substring(52, 55));
            ip0040t1.setICPIPriorityCode(ip0040t1Record.substring(55, 57));
            ip0040t1.setMemberId(ip0040t1Record.substring(57, 68));
            ip0040t1.setProductTypeId(ip0040t1Record.substring(68, 69));
            ip0040t1.setEndpoint(ip0040t1Record.substring(69, 76));
            ip0040t1.setCountryCodeAlpha(ip0040t1Record.substring(76, 79));
            ip0040t1.setCountryCodeNumeric(ip0040t1Record.substring(79, 82));
            ip0040t1.setRegion(ip0040t1Record.substring(82, 83));
            ip0040t1.setProductClass(ip0040t1Record.substring(83, 86));
            ip0040t1.setTxnRoutingIndicator(ip0040t1Record.substring(86, 87));
            ip0040t1.setLicensedProductId(ip0040t1Record.substring(90, 93));
            ip0040t1.setMappingServiceIndicator(ip0040t1Record.substring(93, 94));
            ip0040t1.setBillingCurrencyDefault(ip0040t1Record.substring(101, 104));
            ip0040t1.setBillingExponentDefault(ip0040t1Record.substring(104, 105));
            ip0040t1.setBillingPrimaryCurrency(ip0040t1Record.substring(105, 133));
            ip0040t1.setContaclessEnableInd(ip0040t1Record.substring(151, 152));
            ip0040t1.setCurrencyIndicator(ip0040t1Record.substring(169, 170));

            return ip0040t1;
        } catch (Exception ex) {
            throw ex;
        }
    }

    private IP0075T1Bean unpackIP0075T1(String ip0075t1Record, int keyStart, int keyLength) {
        try {
            IP0075T1Bean ip0075t1 = new IP0075T1Bean();
            ip0075t1.setKey(ip0075t1Record.substring(keyStart - 1, keyLength - 8).substring(8));
            ip0075t1.setEffectiveTimeStamp(ip0075t1Record.substring(0, 7));
            ip0075t1.setActiveInactiveCode(ip0075t1Record.substring(7, 8));
            ip0075t1.setTableID(ip0075t1Record.substring(8, 11));
            ip0075t1.setMCC(ip0075t1Record.substring(11, 16));
            ip0075t1.setCAB(ip0075t1Record.substring(16, 20));
            ip0075t1.setCABProgramLifecycleIndicator(ip0075t1Record.substring(20, 21));
            ip0075t1.setCABType(ip0075t1Record.substring(21, 22));
            ip0075t1.setCABLifeCycleIndicator(ip0075t1Record.substring(22, 23));

            return ip0075t1;
        } catch (Exception ex) {
            throw ex;
        }
    }
}
