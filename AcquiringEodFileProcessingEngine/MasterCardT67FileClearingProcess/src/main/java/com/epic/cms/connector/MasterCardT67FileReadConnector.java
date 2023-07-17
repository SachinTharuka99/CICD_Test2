/**
 * Author : rasintha_j
 * Date : 7/10/2023
 * Time : 1:22 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileProcessingProcessBuilder;
import com.epic.cms.dao.MasterCardT67FileReadDao;
import com.epic.cms.model.bean.FileDetailsBean;
import com.epic.cms.model.bean.FilePathBean;
import com.epic.cms.model.bean.IP0000T1Bean;
import com.epic.cms.service.MasterCardT67FileReadService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DatabaseStatus;
import com.epic.cms.util.LogManager;
import org.apache.commons.io.FileUtils;
import org.jpos.iso.ISOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Service
public class MasterCardT67FileReadConnector extends FileProcessingProcessBuilder {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    public static int totalIP0040T1RecordsProcessedByThread = 0; //issuer account range
    public static int totalIP0075T1RecordsProcessedByThread = 0; //MCC
    String print = null;
    int ip0040t1TotalRecordCount;
    int ip0075t1TotalRecordCount;
    boolean isReplacementFile;
    @Autowired
    MasterCardT67FileReadService masterCardT67FileReadService;
    @Autowired
    LogManager logManager;
    @Autowired
    @Qualifier("ThreadPool_ATMFileValidator")
    ThreadPoolTaskExecutor taskExecutor;
    @Autowired
    MasterCardT67FileReadDao masterCardT67FileReadDao;
    private File file;

    @Override
    public void concreteProcess(String fileId) throws Exception {
        Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_MASTER_CARD_T67_FILE_READ;
        CommonMethods.eodDashboardProgressParametersReset();

        try {
            FilePathBean filePathBean = masterCardT67FileReadDao.loadFilePaths();

            Configurations.PATH_MASTER_FILE_WINDOWS = filePathBean.getPath_master_file_windows();
            Configurations.PATH_MASTER_FILE_LINUX = filePathBean.getPath_master_file_linux();
            Configurations.PATH_BACKUP_WINDOWS = filePathBean.getPath_backup_windows();
            Configurations.PATH_BACKUP_LINUX = filePathBean.getPath_backup_linux();

            if ("LINUX".equals(Configurations.SERVER_RUN_PLATFORM)) {
                Configurations.PATH_MASTER_FILE = Configurations.PATH_MASTER_FILE_LINUX;
                Configurations.PATH_BACKUP = Configurations.PATH_BACKUP_LINUX;
            }
            if ("WINDOWS".equals(Configurations.SERVER_RUN_PLATFORM)) {
                Configurations.PATH_MASTER_FILE = Configurations.PATH_MASTER_FILE_WINDOWS;
                Configurations.PATH_BACKUP = Configurations.PATH_BACKUP_WINDOWS;
            }

            //check file upload location for direct copy files other than web upload.
            //detect files from the location
            File folder = new File(Configurations.PATH_MASTER_FILE);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles != null) {
                int totalFileCount = listOfFiles.length;
                int fileCount = 0;

                for (int i = 0; i < totalFileCount; i++) { //recurse file by file
                    try {
                        fileCount++;
                        masterCardT67FileReadService.FileAvailabilityCheck(listOfFiles[i], fileCount);

                    } catch (Exception ex) {
                        logError.error("T67/T68 file identification from location failed: ", ex);
                    }
                }

                //wait till all the threads are completed
                while (!(taskExecutor.getActiveCount() == 0)) {
                    Thread.sleep(1000);
                }
            }

            // Check availability of master files
            if (masterCardT67FileReadDao.isFilesAvailable(DatabaseStatus.STATUS_FILE_INIT)) { //INIT but not FINIT
                ArrayList<FileDetailsBean> filDetails = new ArrayList<>();
                // Get file names
                filDetails = masterCardT67FileReadDao.getFileDetails(DatabaseStatus.STATUS_FILE_INIT); //INIT but not FINIT

                for (int i = 0; i < filDetails.size(); i++) { // loop file by file
                    try {
                        // wait 10 second to start reading new file
                        //Thread.sleep(5000);

                        HashMap<String, IP0000T1Bean> headerKeyMap = new LinkedHashMap<>();
                        ip0040t1TotalRecordCount = 0;
                        ip0075t1TotalRecordCount = 0;
                        totalIP0040T1RecordsProcessedByThread = 0;
                        totalIP0075T1RecordsProcessedByThread = 0;
                        isReplacementFile = false;

                        // Get each file
                        file = new File(Configurations.PATH_MASTER_FILE.concat(File.separator).concat(filDetails.get(i).getFileName()));
                        fileId = filDetails.get(i).getFileId();

                        // Make sure each file exist
                        if (file.isFile() && file.exists() && file.canRead()) {
                            Configurations.PROCESS_MASTER_FILE = true;

                            print = "Master T067/T068 file identified..."
                                    .concat("\nFile name : ").concat(file.getName())
                                    .concat("\nFile id   : ").concat(fileId);

                            logInfo.info(logManager.processStartEndStyle(print));

                            String masterFileId = fileId;
                            String fileName = filDetails.get(i).getFileName();

                            //------------------------------Input File reading-----------------------------------------------------//
                            // update the master file processing start time.
                            try {
                                masterCardT67FileReadDao.updateFileStartTime(masterFileId);
                            } catch (Exception ex) {
                                print = "Connection not valid. requesting new connection from database";
                                logInfo.info(print);
                                //if connection not available due to long time for file processsing in T68, take a new connection
                                masterCardT67FileReadDao.updateFileStartTime(masterFileId);
                            }

                            if (!file.exists()) {
                                logInfo.info(logManager.logHeader("Error : File does not exist"));
                                break;

                            }
                            if (!file.isFile()) {
                                logInfo.info(logManager.logHeader("Error : File is not a file type"));
                                break;

                            }
                            if (!file.canRead()) {
                                logInfo.info(logManager.logHeader("Error : File is unreadable"));
                                break;

                            }
                            long length = file.length();

                            logInfo.info(logManager.logHeader("File size : ".concat(Long.toString(length)).concat(" ( bytes )")));

                            if (length <= 0) {
                                logInfo.info(logManager.logHeader("Error : Empty file"));
                                break;
                            }

                            if (length > Integer.MAX_VALUE) {
                                logInfo.info(logManager.logHeader("Error : File size too large"));
                                break;
                            }

                            masterCardT67FileReadDao.updateFileStatus(masterFileId, DatabaseStatus.STATUS_FILE_READ);

                            print = "Master T67/T68 File [ ".concat(fileName).concat(" ] accepted for processing.");
                            logInfo.info(print);

                            /**
                             * *************************** start reading file
                             * *********************************************************************
                             */
                            byte[] bytes = FileUtils.readFileToByteArray(file); //read full file into byte array (file will be in EBCDIC Format)

                            if (bytes[1012] == (byte) 64 && bytes[1013] == (byte) 64) { //file in 1014 layout. need to remove 2 X '40' records
                                /*All file consists of records. Each record is prefixed with a 4 byte binary length. There are no carriage returns or line
                                feeds in the file. Before sending, the contents is blocked into lengths of 1012, and an additional 2 x‘40’ characters are
                                appended at each block. Finally, the total file length is made a multiple of 1014 with the final incomplete record being
                                filled with the x‘40’ character
                                in EBCIDIC file it will fill from x'00' not x'40'
                                 */

                                int noOf1014Blocks = bytes.length / 1014; //get number of 1014 blocks
                                byte[] byteArrWith1014Removed = new byte[bytes.length - (2 * noOf1014Blocks)]; //create new array to store remaining after removing two x40 data in each 1014 block
                                for (int x = 0; x < noOf1014Blocks; x++) { //recurse each block and remove all x40 characters
                                    System.arraycopy(bytes, x * 1014, byteArrWith1014Removed, x * 1012, 1012);
                                }

                                bytes = byteArrWith1014Removed; //copy final x40 removed array again to bytes array
                            }

                            int fullFileLength = bytes.length;
                            byte[] remainingRecords = Arrays.copyOf(bytes, fullFileLength);

                            String recordLengthStr = "";

                            int recordLength = 0;
                            byte[] record = null;
                            byte[] recordAscii = null;

                            String recordPart1 = "";
                            int count = 0;
                            String consideringRecordTableID = "";
                            String IP0040T1TableKey = "041";
                            int IP0040T1KeyStart = 0, IP0040T1KeyLength = 0;
                            String IP0075T1TableKey = "071";
                            int IP0075T1KeyStart = 0, IP0075T1KeyLength = 0;
                            int startIndex = 2; // for 00 00 00 1B files need to start from 2. for 00 1F 00 00 files need to start from  0
                            String recordAsciiStr = "";

                            ArrayList<String> ip0040T1RecordList = new ArrayList<>();
                            int ip0040T1RecordListCount = 0;
                            ArrayList<String> ip0075T1RecordList = new ArrayList<>();
                            int ip0075T1RecordListCount = 0;

                            boolean isFileContainIP0040T1Records = false;
                            boolean isFileContainIP0075T1Records = false;

                            //Detect file structure(00 00 00 1B , 00 1F 00 00) and encoding format (EBCDIC, ASCII)
                            boolean is1FFormat = "1f".equalsIgnoreCase(String.format("%02x", remainingRecords[1])); // file start length format 00 00 00 1B (not 00 1F 00 00)
                            //file start length format  00 1F 00 00

                            boolean isEBCDICEncodedFile = false;
                            String ebcdicCheckStr = new String(Arrays.copyOfRange(remainingRecords, 4, 5), java.nio.charset.Charset.forName("ibm500"));
                            //if character convert successfully then file in EBCDIC encoded
                            //ASCII format
                            isEBCDICEncodedFile = ebcdicCheckStr.equalsIgnoreCase("U") || ebcdicCheckStr.equalsIgnoreCase("R");

                            while (true) {

                                if (is1FFormat) { //file start length format  00 1F 00 00
                                    /* record length will be four parts (eg1: 1 7 0 0  = 00 07 00 00, eg1: 0 31 0 0  = 00 1F 00 00)
                                first two sections contain actual length, other two used for split single record to two parts
                                eg:  0 5 1 0 ..first part... 0 31 2 0...second part...
                                   third digit used as record part number(1 or 2)
                                     */
                                    /**
                                     * *********** read record by length
                                     * ************************************************
                                     */
                                    if (count == 0) {
                                        startIndex = 0;
                                    }
                                    try {
                                        recordLengthStr = ISOUtil.zeropad(String.format("%02x", remainingRecords[startIndex]), 2).concat(ISOUtil.zeropad(String.format("%02x", remainingRecords[startIndex + 1]), 2)); // get record length (eg: 1 7 0 0 ) as two digit (01 07 00 00)
                                    } catch (ArrayIndexOutOfBoundsException ex) {
                                        //end of file
                                        break;
                                    }
                                    recordLength = Integer.parseInt(recordLengthStr, 16); // convert hex length to decimal length ( 0107 => 263 )

                                    record = Arrays.copyOfRange(remainingRecords, startIndex + 4, startIndex + recordLength); // copy record to byte array excluding the length part

                                    if (isEBCDICEncodedFile) {
                                        recordAscii = new String(record, java.nio.charset.Charset.forName("ibm500"))
                                                .getBytes(StandardCharsets.US_ASCII); // convert EBCDIC format byte array to record to ASCII format byte array

                                        recordAsciiStr = new String(recordAscii, StandardCharsets.US_ASCII); // convert ASCII byte array to a ASCII String
                                    } else {
                                        recordAsciiStr = new String(record, StandardCharsets.US_ASCII); // convert ASCII byte array to a ASCII String
                                    }

                                    if ("01".equals(String.format("%02x", remainingRecords[startIndex + 2]))) { // if third number in length(00 05 01 00) is 01 , that means single record splitted to two parts, this is first part
                                        recordPart1 = recordAsciiStr; //this is first part
                                        //remainingRecords = Arrays.copyOfRange(remainingRecords, recordLength, fullFileLength);
                                        startIndex = startIndex + recordLength;
                                        continue; //need to read second part before processing the first part of record
                                    } else if ("02".equals(String.format("%02x", remainingRecords[startIndex + 2]))) { // if third fourth section(00 31 02 00)  in length is 02, that means second part of the splitted record
                                        recordAsciiStr = recordPart1.concat(recordAsciiStr); // concat first part and second part to make a whole record
                                        recordPart1 = "";
                                    }
                                    //remainingRecords = Arrays.copyOfRange(remainingRecords, recordLength, fullFileLength); // always assign 'to be read records' to this variable
                                    startIndex = startIndex + recordLength;
                                    /**
                                     * *********** process record
                                     * *************************************************
                                     */
                                    if (count == 0) {
                                        if (!(recordAsciiStr.contains("UPDATE FILE") || recordAsciiStr.contains("REPLACEMENT FILE"))) { // first line of the file need to contain text "UPDATE FILE" OR "REPLACEMENT FILE"
                                            break; // if not reject the file //TODO: throw exception
                                        }
                                        if (recordAsciiStr.contains("REPLACEMENT FILE")) { //if replacement file found remove all entries in current ip0040t1data,ip0040t75data
                                            isReplacementFile = true;
                                            masterCardT67FileReadDao.truncateEodMasterIP0040T1Data();
                                            masterCardT67FileReadDao.truncateEodMasterIP0075T1Data();
                                        }
                                    }
                                    if (recordAsciiStr.contains("IP0000T1") && !recordAsciiStr.contains("TRAILER")) { // read header records contain keys to other tables
                                        IP0000T1Bean unpackedIP0000T1Bean = unpackKeysInIP0000T1(recordAsciiStr);
                                        print = unpackedIP0000T1Bean.toString();
                                        logInfo.info(print);
                                        headerKeyMap.put(unpackedIP0000T1Bean.getKey(), unpackedIP0000T1Bean);

                                    } else if (recordAsciiStr.contains("IP0000T1") && recordAsciiStr.contains("TRAILER")) { // header record finished

                                        if (headerKeyMap.containsKey("IP0040T1")) {
                                            isFileContainIP0040T1Records = true;
                                            IP0040T1TableKey = headerKeyMap.get("IP0040T1").getSubTableIndicator(); //eg: 041 - update file or 037-replace file
                                            IP0040T1KeyStart = Integer.parseInt(headerKeyMap.get("IP0040T1").getSubTableKeyStart()); // key start for IP0040T1 records
                                            IP0040T1KeyLength = Integer.parseInt(headerKeyMap.get("IP0040T1").getSubTableKeyLength()); // key length for  IP0040T1 records
                                        }
                                        if (headerKeyMap.containsKey("IP0075T1")) {
                                            isFileContainIP0075T1Records = true;
                                            IP0075T1TableKey = headerKeyMap.get("IP0075T1").getSubTableIndicator(); //eg: 041 - update file or 037-replace file
                                            IP0075T1KeyStart = Integer.parseInt(headerKeyMap.get("IP0075T1").getSubTableKeyStart()); // key start for IP0075T1 records
                                            IP0075T1KeyLength = Integer.parseInt(headerKeyMap.get("IP0075T1").getSubTableKeyLength()); // key length for  IP0075T1 records
                                        }
                                    }
                                    if (recordAsciiStr.contains("TRAILER")) {
                                        print = recordAsciiStr;
                                        logInfo.info(print);
                                    }

                                    try {
                                        consideringRecordTableID = recordAsciiStr.substring(8, 11);
                                    } catch (
                                            Exception e) {//sometime array index outofbound will be thorow, but no action needed for that records
                                        consideringRecordTableID = "";
                                    }
                                    /*
                                    Note: Table IP0040T1: Issuer Account Range is in compressed format in file, effective timestamp(YYYYMMDDHH -> YYDDDHH)
                                    and table id(IP0040T1 -> 041) will be in compressed format
                                     */
                                    if (isFileContainIP0040T1Records && Configurations.READ_T67_IP0040T1_TABLE == 1) {
                                        if (consideringRecordTableID.equals(IP0040T1TableKey)) { //process IP0040T1 (041) - Issuer Account Range records
                                            ip0040T1RecordList.add(recordAsciiStr); // add raw ip0040t1 record to a array list until its size=100
                                            ip0040T1RecordListCount++;
                                            if (ip0040T1RecordListCount == 100) { // if array size is 1000 then initiate singele thread to process 100 records

                                                masterCardT67FileReadService.IP0040T1UnpackThread(ip0040T1RecordList, IP0040T1KeyStart, IP0040T1KeyLength, totalIP0040T1RecordsProcessedByThread, fileId);

                                                //wait till all the threads are completed
                                                while (!(taskExecutor.getActiveCount() == 0)) {
                                                    Thread.sleep(1000);
                                                }

                                                ip0040T1RecordList = new ArrayList<>(); //reset array list and count
                                                ip0040T1RecordListCount = 0;
                                            }
                                            ip0040t1TotalRecordCount++;
                                        }
                                    }
                                    if (isFileContainIP0075T1Records && Configurations.READ_T67_IP0075T1_TABLE == 1) {
                                        if (consideringRecordTableID.equals(IP0075T1TableKey)) { //process IP0075T1  - Card Acceptor Business Codes (MCCs)
                                            ip0075T1RecordList.add(recordAsciiStr); // add raw ip0075t1 record to a array list until its size=100
                                            ip0075T1RecordListCount++;
                                            if (ip0075T1RecordListCount == 100) { // if array size is 1000 then initiate singele thread to process 100 records

                                                masterCardT67FileReadService.IP0075T1UnpackThread(ip0075T1RecordList, IP0075T1KeyStart, IP0075T1KeyLength, totalIP0075T1RecordsProcessedByThread, fileId); //initialize threads

                                                //wait till all the threads are completed
                                                while (!(taskExecutor.getActiveCount() == 0)) {
                                                    Thread.sleep(1000);
                                                }
                                                ip0075T1RecordList = new ArrayList<>(); //reset array list and count
                                                ip0075T1RecordListCount = 0;
                                            }
                                            ip0075t1TotalRecordCount++;
                                        }
                                    }
                                    count++;
                                } else { //file start length format 00 00 00 1B
                                    /* record length will be four parts (eg1: 1 7 0 0  = 00 07 00 00, eg1: 0 31 0 0  = 00 1F 00 00)
                                first two sections contain actual length, other two used for split single record to two parts
                                eg:  0 5 1 0 ..first part... 0 31 2 0...second part...
                                   third digit used as record part number(1 or 2)
                                     */
                                    /**
                                     * *********** read record by length
                                     * ************************************************
                                     */

                                    try {
                                        recordLengthStr = ISOUtil.zeropad(String.format("%02x", remainingRecords[startIndex]), 2).concat(ISOUtil.zeropad(String.format("%02x", remainingRecords[startIndex + 1]), 2)); // get record length (eg: 0 0 0 27 ) as two digit (00 00 00 27)
                                    } catch (ArrayIndexOutOfBoundsException ex) {
                                        //end of file
                                        break;
                                    }
                                    recordLength = Integer.parseInt(recordLengthStr, 16); // convert hex length to decimal length ( 0107 => 263 )

                                    record = Arrays.copyOfRange(remainingRecords, startIndex + 2, startIndex + 2 + recordLength); // copy record to byte array excluding the length part

                                    if (isEBCDICEncodedFile) {
                                        recordAscii = new String(record, java.nio.charset.Charset.forName("ibm500"))
                                                .getBytes(StandardCharsets.US_ASCII); // convert EBCDIC format byte array to record to ASCII format byte array

                                        recordAsciiStr = new String(recordAscii, StandardCharsets.US_ASCII); // convert ASCII byte array to a ASCII String
                                    } else {
                                        recordAsciiStr = new String(record, StandardCharsets.US_ASCII); // convert ASCII byte array to a ASCII String
                                    }

                                    //remainingRecords = Arrays.copyOfRange(remainingRecords, recordLength, fullFileLength); // always assign 'to be read records' to this variable
                                    startIndex = startIndex + 4 + recordLength;
                                    /**
                                     * *********** process record
                                     * *************************************************
                                     */
                                    if (count == 0) {
                                        if (!(recordAsciiStr.contains("UPDATE FILE") || recordAsciiStr.contains("REPLACEMENT FILE"))) { // first line of the file need to contain text "UPDATE FILE" OR "REPLACEMENT FILE"
                                            break; // if not reject the file //TODO: throw exception
                                        }
                                        if (recordAsciiStr.contains("REPLACEMENT FILE")) { //if replacement file found remove all entries in current ip0040t1data,ip0040t75data
                                            isReplacementFile = true;
                                            masterCardT67FileReadDao.truncateEodMasterIP0040T1Data();
                                            masterCardT67FileReadDao.truncateEodMasterIP0075T1Data();
                                        }
                                    }
                                    if (recordAsciiStr.contains("IP0000T1") && !recordAsciiStr.contains("TRAILER")) { // read header records contain keys to other tables
                                        IP0000T1Bean unpackedIP0000T1Bean = unpackKeysInIP0000T1(recordAsciiStr);
                                        print = unpackedIP0000T1Bean.toString();
                                        logInfo.info(print);
                                        headerKeyMap.put(unpackedIP0000T1Bean.getKey(), unpackedIP0000T1Bean);

                                    } else if (recordAsciiStr.contains("IP0000T1") && recordAsciiStr.contains("TRAILER")) { // header record finished

                                        if (headerKeyMap.containsKey("IP0040T1")) {
                                            isFileContainIP0040T1Records = true;
                                            IP0040T1TableKey = headerKeyMap.get("IP0040T1").getSubTableIndicator(); //eg: 041 - update file or 037-replace file
                                            IP0040T1KeyStart = Integer.parseInt(headerKeyMap.get("IP0040T1").getSubTableKeyStart()); // key start for IP0040T1 records
                                            IP0040T1KeyLength = Integer.parseInt(headerKeyMap.get("IP0040T1").getSubTableKeyLength()); // key length for  IP0040T1 records
                                        }
                                        if (headerKeyMap.containsKey("IP0075T1")) {
                                            isFileContainIP0075T1Records = true;
                                            IP0075T1TableKey = headerKeyMap.get("IP0075T1").getSubTableIndicator(); //eg: 041 - update file or 037-replace file
                                            IP0075T1KeyStart = Integer.parseInt(headerKeyMap.get("IP0075T1").getSubTableKeyStart()); // key start for IP0075T1 records
                                            IP0075T1KeyLength = Integer.parseInt(headerKeyMap.get("IP0075T1").getSubTableKeyLength()); // key length for  IP0075T1 records
                                        }
                                    }
                                    if (recordAsciiStr.contains("TRAILER")) {
                                        print = recordAsciiStr;
                                        logInfo.info(print);
                                    }

                                    try {
                                        consideringRecordTableID = recordAsciiStr.substring(8, 11);
                                    } catch (
                                            Exception e) {//sometime array index outofbound will be thorow, but no action needed for that records
                                        consideringRecordTableID = "";
                                    }
                                    /*
                                    Note: Table IP0040T1: Issuer Account Range is in compressed format in file, effective timestamp(YYYYMMDDHH -> YYDDDHH)
                                    and table id(IP0040T1 -> 041) will be in compressed format
                                     */
                                    if (isFileContainIP0040T1Records && Configurations.READ_T67_IP0040T1_TABLE == 1) {
                                        if (consideringRecordTableID.equals(IP0040T1TableKey)) { //process IP0040T1 (041) - Issuer Account Range records
                                            ip0040T1RecordList.add(recordAsciiStr); // add raw ip0040t1 record to a array list until its size=100
                                            ip0040T1RecordListCount++;
                                            if (ip0040T1RecordListCount == 100) { // if array size is 1000 then initiate singele thread to process 100 records

                                                masterCardT67FileReadService.IP0040T1UnpackThread(ip0040T1RecordList, IP0040T1KeyStart, IP0040T1KeyLength, totalIP0040T1RecordsProcessedByThread, fileId); //initialize threads

                                                //wait till all the threads are completed
                                                while (!(taskExecutor.getActiveCount() == 0)) {
                                                    Thread.sleep(1000);
                                                }
                                                ip0040T1RecordList = new ArrayList<>(); //reset array list and count
                                                ip0040T1RecordListCount = 0;
                                            }
                                            ip0040t1TotalRecordCount++;
                                        }
                                    }
                                    if (isFileContainIP0075T1Records && Configurations.READ_T67_IP0075T1_TABLE == 1) {
                                        if (consideringRecordTableID.equals(IP0075T1TableKey)) { //process IP0075T1  - Card Acceptor Business Codes (MCCs)
                                            ip0075T1RecordList.add(recordAsciiStr); // add raw ip0075t1 record to a array list until its size=100
                                            ip0075T1RecordListCount++;
                                            if (ip0075T1RecordListCount == 100) { // if array size is 1000 then initiate singele thread to process 100 records

                                                masterCardT67FileReadService.IP0075T1UnpackThread(ip0075T1RecordList, IP0075T1KeyStart, IP0075T1KeyLength, totalIP0075T1RecordsProcessedByThread, fileId); //initialize threads

                                                //wait till all the threads are completed
                                                while (!(taskExecutor.getActiveCount() == 0)) {
                                                    Thread.sleep(1000);
                                                }
                                                ip0075T1RecordList = new ArrayList<>(); //reset array list and count
                                                ip0075T1RecordListCount = 0;
                                            }
                                            ip0075t1TotalRecordCount++;
                                        }
                                    }
                                    count++;
                                }
                            }

                            if (ip0040T1RecordList.size() != 0) {

                                masterCardT67FileReadService.IP0040T1UnpackThread(ip0040T1RecordList, IP0040T1KeyStart, IP0040T1KeyLength, totalIP0040T1RecordsProcessedByThread, fileId); //initialize threads

                                //wait till all the threads are completed
                                while (!(taskExecutor.getActiveCount() == 0)) {
                                    Thread.sleep(1000);
                                }
                            }
                            if (ip0075T1RecordList.size() != 0) {

                                masterCardT67FileReadService.IP0075T1UnpackThread(ip0075T1RecordList, IP0075T1KeyStart, IP0075T1KeyLength, totalIP0075T1RecordsProcessedByThread, fileId); //initialize threads

                                //wait till all the threads are completed
                                while (!(taskExecutor.getActiveCount() == 0)) {
                                    Thread.sleep(1000);
                                }
                            }

                            try {
                                masterCardT67FileReadDao.updateFileStatistics(masterFileId, DatabaseStatus.STATUS_FILE_COMP, String.valueOf(ip0040t1TotalRecordCount));
                            } catch (Exception ex) {
                                print = "Connection not valid. requesting new connection from database";
                                logInfo.info(print);
                                //if connection not available due to long time for file processsing in T68, take a new connection
                                masterCardT67FileReadDao.updateFileStatistics(masterFileId, DatabaseStatus.STATUS_FILE_COMP, String.valueOf(ip0040t1TotalRecordCount));
                            }
                            print = "Master Card T67/T68 file processing completed... \n Total Bin Updates: ".concat(Integer.toString(ip0040t1TotalRecordCount).concat(" Total MCC Updates: ".concat(Integer.toString(ip0075t1TotalRecordCount))));
                            logInfo.info(print);
                            Configurations.PROCESS_MASTER_FILE = false;

                            //move file to backup directory
                            print = "Moving file to: ".concat(Configurations.PATH_BACKUP.concat(File.separator).concat(filDetails.get(i).getFileName()));
                            logInfo.info(print);

                            Files.createDirectories(Paths.get(Configurations.PATH_BACKUP));
                            Files.move(Paths.get(file.getAbsolutePath()),
                                    Paths.get(Configurations.PATH_BACKUP.concat(File.separator).concat(filDetails.get(i).getFileName())),
                                    StandardCopyOption.REPLACE_EXISTING);

                        } else {
                            try {
                                masterCardT67FileReadDao.updateFileStatus(fileId, DatabaseStatus.STATUS_FILE_ERROR);
                            } catch (Exception ex) {
                                print = "Connection not valid. requesting new connection from database";
                                logInfo.info(print);
                                //if connection not available due to long time for file processsing in T68, take a new connection
                                masterCardT67FileReadDao.updateFileStatus(fileId, DatabaseStatus.STATUS_FILE_ERROR);
                            }

                            print = "Master file not found..."
                                    .concat("\nFile name : ").concat(file.getName())
                                    .concat("\nFile id   : ").concat(fileId);

                            logInfo.info(print);
                        }

                    } catch (Exception ex) { //exception occured in considering T67 file
                        logError.error("Error Occured in T67/T68 file read : ", ex);

                        try {
                            masterCardT67FileReadDao.updateFileStatus(fileId, DatabaseStatus.STATUS_FILE_ERROR);
                        } catch (Exception e) {
                            print = "Connection not valid. requesting new connection from database";
                            logInfo.info(print);
                            //if connection not available due to long time for file processsing in T68, take a new connection
                            masterCardT67FileReadDao.updateFileStatus(fileId, DatabaseStatus.STATUS_FILE_REJECT);
                        }
                        print = "Master T67/T68 File rejected...";
                        logInfo.info(print);
                    }
                } // end  file looping
            }
        } catch (Exception e) {
            logError.error("Error Occured in Master T67 File Reading Process: ", e);
        } finally {
            try {
                Configurations.PROCESS_MASTER_FILE = false;
                System.out.flush();
                file = null;
            } catch (Exception e) {
                logError.error("Master T67 Reading Process: ", e);
            }
            logInfo.info(logManager.logSummery(details));
            logInfo.info(logManager.logStartEnd("Master Card T67 File Reading process completed"));
            logInfo.info(logManager.logStartEnd("Master Card T67 File Reading process completed"));
        }
    }

    private IP0000T1Bean unpackKeysInIP0000T1(String ip0000t1Record) {
        try {
            IP0000T1Bean ip0000t1 = new IP0000T1Bean();
            ip0000t1.setTableId(ip0000t1Record.substring(11, 19));
            ip0000t1.setSubTableId(ip0000t1Record.substring(19, 27));
            ip0000t1.setSubTableName(ip0000t1Record.substring(28, 55));
            ip0000t1.setSubTableKeyLength(ip0000t1Record.substring(55, 60));
            ip0000t1.setSubTableKeyStart(ip0000t1Record.substring(60, 64));
            ip0000t1.setSubTableIndicator(ip0000t1Record.substring(243, 246));
            ip0000t1.setKey(ip0000t1.getSubTableId());

            return ip0000t1;

        } catch (Exception ex) {
            throw ex;
        }
    }

    @Override
    public void addSummaries() {

    }
}
