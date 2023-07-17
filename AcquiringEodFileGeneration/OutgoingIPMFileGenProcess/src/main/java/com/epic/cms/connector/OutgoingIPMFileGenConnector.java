package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.model.bean.EodOuputFileBean;
import com.epic.cms.model.bean.MasterOutgoingFieldIdentityBean;
import com.epic.cms.model.bean.TransactionDataBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.OutgoingIPMFileGenRepo;
import com.epic.cms.service.OutgoingIPMFileGenService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.apache.commons.io.FileUtils;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.SimpleLogListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class OutgoingIPMFileGenConnector extends ProcessBuilder {

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    OutgoingIPMFileGenRepo outgoingIPMFileGenRepo;

    @Autowired
    OutgoingIPMFileGenService outgoingIPMFileGenService;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    //user variables
    int pendingTxnCount = 0;
    int failedTxnCount = 0;
    int irdNotDecidedTxnCount = 0;
    String outgoinIPMFilePath = "";
    String outgoinIPMFileName;
    List<MasterOutgoingFieldIdentityBean> pendingTxnList = null;

    // Create Packager based on XML that contain DE type
    GenericPackager packager;
    @Override
    public void concreteProcess() throws Exception {
        try {
            initializeGenericPackage();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_OUTGOING_IPM_FILE_GEN);
            Configurations.RUNNING_PROCESS_ID=Configurations.PROCESS_ID_OUTGOING_IPM_FILE_GEN;
            if (processBean != null) {
                CommonMethods.eodDashboardProgressParametersReset();

                //get transction, merchant data from db as a transaction bean list
                List<TransactionDataBean> outgoingTransactionData = outgoingIPMFileGenRepo.getOutgoingIPMTransactionData();
                pendingTxnCount = outgoingTransactionData.size();

                //Load data from RECVISATXNFIELD table
                //Configurations.VISA_TXN_FIELD_TABLE = acqBackendDbConn.getVisaTxnFields();
                Configurations.OUTGOING_MASTER_REJECT_REASON_TABLE = outgoingIPMFileGenRepo.getMasterOutgoingRejectReasonTable();
                Configurations.CURRENCY_EXPONENT_TABLE = outgoingIPMFileGenRepo.getCurrencyExponentTable();

                outgoingTransactionData
                        .forEach(bean -> outgoingIPMFileGenService.processOutgoingTransactionData(bean));

                irdNotDecidedTxnCount = outgoingIPMFileGenService.recalculateIRDValue();

                //------ Generate outgoing IPM file ---
                File outputDirectory = new File(Configurations.OUTGOING_IPM_FILE_PATH);

                outputDirectory.mkdirs(); // create parent directories if not exists

                outgoinIPMFileName = "MASTER_IPM_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()) + ".IPM";
                outgoinIPMFilePath = Configurations.OUTGOING_IPM_FILE_PATH + outgoinIPMFileName;

                // get TXNID list to generate file
                pendingTxnList = outgoingIPMFileGenRepo.getPendingIPMTxnList();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS=pendingTxnList.size();
                Configurations.GENERATED_IPM_TRANSACTION_COUNT = pendingTxnList.size();
                Configurations.GENERATED_IPM_AMOUNT_CHECKSUM = 0;

                //write header record
                writeHeaderRecordToIpmFile(outgoinIPMFilePath);

                //write transaction records
                int de71Count = 2;
                for (MasterOutgoingFieldIdentityBean txnBean : pendingTxnList) { //recurse txn list
                    try {
                        writeTransactionRecordToIPMFile(outgoinIPMFilePath, txnBean, de71Count);
                        de71Count++;
                        outgoingIPMFileGenRepo.updateMasterOutgoingFieldIdentityFileStatus(txnBean.getTxnId(), outgoinIPMFileName); // set file status to 1 in OUTGOINGFIELDIDENTITY Table
                        Configurations.PROCESS_SUCCESS_COUNT++;
                    } catch (Exception ex) {
                        failedTxnCount++;
                        Configurations.PROCESS_FAILD_COUNT++;
                        logError.error("Errors occurred while processing transaction: " + txnBean.getTxnId(), ex);
                    }
                }
                pendingTxnCount = de71Count - 2; //for summery
                writeFooterRecordToIpmFile(outgoinIPMFilePath);

                if (Configurations.OUTGOING_IPM_FILE_LAYOUT == 2) { //1014 layout
                    // change file to 1014 layout ***************************************
                    /*
                        data file is blocked into lengths of 1012, and an additional 2 x‘40’ characters are appended at each block.
                       Finally, the total file length is made a multiple of 1014 with the final incomplete record being filled with the x‘40’
                       character
                     */

                    //read no RDW generated file
                    byte[] noRDWByteArray = FileUtils.readFileToByteArray(new File(outgoinIPMFilePath));

                    //delete content of the same file before rewrite with 1014 layout
                    PrintWriter writer = new PrintWriter(outgoinIPMFilePath);
                    writer.print("");
                    writer.close();

                    FileOutputStream outputFile = new FileOutputStream(outgoinIPMFilePath, true); //enable append true

                    byte[] block = new byte[1012];
                    int count = 0;
                    for (byte b : noRDWByteArray) {
                        block[count] = b;
                        count++;
                        if (count == 1012) {
                            outputFile.write(block); //write 1012 length block
                            outputFile.write("@@".getBytes(StandardCharsets.UTF_8));//append two x'40' characters
                            count = 0;
                            block = new byte[1012];
                        }

                    }
                    if (count != 0) { //for remaining last part of the file
                        StringBuffer buffer = new StringBuffer();
                        outputFile.write(Arrays.copyOfRange(block, 0, count + 4)); // write remainig record+adding 4 zeros(next record length is zero) to end to indicate no records further
                        count += 4;
                        for (int i = count; i < 1014; i++) { //fill remaining 1014 block with x'40 characters
                            buffer.append("@");
                        }
                        outputFile.write(buffer.toString().getBytes(Charset.forName("UTF-8")));
                    }
                    outputFile.close();
                }

                //insert EODMASTEROUTGOINGFILESUMMERY table
                outgoingIPMFileGenRepo.insertIPMFileSummery(outgoinIPMFileName, Configurations.GENERATED_IPM_TRANSACTION_COUNT);
                summery.put("Started Date", Configurations.EOD_DATE.toString());

                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS=pendingTxnCount;
                Configurations.PROCESS_SUCCESS_COUNT=(pendingTxnCount-failedTxnCount);
                Configurations.PROCESS_FAILD_COUNT=failedTxnCount;
                summery.put("IPM File Path ", outgoinIPMFilePath);
                summery.put("No of Success Txn in File ", Integer.toString(pendingTxnCount));
                summery.put("No of Failed Txn ", Integer.toString(failedTxnCount));
                summery.put("No of IRD Undecided Txns ", Integer.toString(irdNotDecidedTxnCount));
                //logInfo.info(logManager.logSummery(summery));

                //update next EOD schedule
                //CommonMethods.updateNextSheduleTime(Configurations.PROCESS_ID_OUTGOING_IPM_FILE_GEN, con, processBean);

                EodOuputFileBean eodOuputFileBean=new EodOuputFileBean();
                eodOuputFileBean.setFileName(outgoinIPMFileName);
                eodOuputFileBean.setNoOfRecords(pendingTxnCount);

                outgoingIPMFileGenRepo.insertOutputFiles(eodOuputFileBean,"OUTMASTER");
            }
            //if all goes well commit the db change
        }catch (Exception e){
            logError.error("Errors occurred", e);
            Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
        }
    }

    private void initializeGenericPackage() throws Exception {
        String rootPath = "";
        if (Configurations.SERVER_RUN_PLATFORM.equals("LINUX")) {
            rootPath = "/app/config";
        } else {
            rootPath = "classpath:config";
        }
        if (Configurations.OUTGOING_IPM_FILE_ENCODING_FORMAT == 1) { //ASCII
            packager = new GenericPackager(ResourceUtils.getFile(rootPath + File.separator + "iso8583MasterCardASCIIoutgoing.xml").getAbsolutePath());
        } else {//EBCDIC
            packager = new GenericPackager(ResourceUtils.getFile(rootPath + File.separator + "iso8583MasterCardEBCIDICoutgoing.xml").getAbsolutePath());
        }
    }

    public void writeFooterRecordToIpmFile(String filePath)  throws IOException, ISOException {
        File f = new File(filePath);
        //f.createNewFile(); // if file not exists, create a new file

        try {
            // Create ISO Message
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);

            isoMsg.setMTI("1644");
            isoMsg.set(24, "695");

            // create PDS values for field48 list
            Map<String, String> pdsMap = new LinkedHashMap<>();

            //create pds0105
            pdsMap.put("105", Configurations.GENERATED_IPM_FILE_ID);

            //create pds0301
            String pds301Value = ISOUtil.zeropad(Long.toString(Configurations.GENERATED_IPM_AMOUNT_CHECKSUM), 16);
            pdsMap.put("301", pds301Value);

            //create pds0306
            String pds306Value = ISOUtil.zeropad(Integer.toString(Configurations.GENERATED_IPM_TRANSACTION_COUNT + 2), 8);
            pdsMap.put("306", pds306Value);

            if (pdsMap != null && !pdsMap.isEmpty()) {
                String field48ValueStr = createField48(pdsMap);
                isoMsg.set(48, field48ValueStr);
            }

            isoMsg.set(71, Integer.toString(Configurations.GENERATED_IPM_TRANSACTION_COUNT + 2)); //message number (header count+record count+1)

            // Get and print the output result
            byte[] data = isoMsg.pack();

            try (FileOutputStream outputStream = new FileOutputStream(filePath, true)) {
                //write length part of tranasction record
                for (int j = Integer.toHexString(data.length).length(); j <= 4; j++) {
                    outputStream.write(0); //initial 00 values in legth field
                }
                // when data.length write to outputstream as bytes only two hex value will be written.to avoid it split
                //length to hex values seperately and write individually. eg : 00 00 01 1C
                Stack<String> stk = new Stack<>();
                int previousX = Integer.toHexString(data.length).length();
                for (int x = Integer.toHexString(data.length).length() - 2; x > 0; x -= 2) {
                    stk.push(Integer.toHexString(data.length).substring(x, previousX));
                    previousX = x;
                }
                stk.push(Integer.toHexString(data.length).substring(0, previousX));
                for (int z = 0; z <= stk.size(); z++) {
                    outputStream.write(Integer.parseInt(stk.pop(), 16));
                }

                //write transaction record
                outputStream.write(data);
            }

        } catch (IOException e) {
            throw e;
        }
    }

    public void writeTransactionRecordToIPMFile(String filePath, MasterOutgoingFieldIdentityBean txnBean, int de71Count)  throws IOException, ISOException {

        org.jpos.util.Logger l = new org.jpos.util.Logger();
        l.addListener(new SimpleLogListener(System.out));
        packager.setLogger(l, "");
        try {
            // Create ISO Message
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);

            String dataElementValue = txnBean.getDE0();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.setMTI(dataElementValue);
            }
            // dataElementValue=txnBean.getDE1();if(dataElementValue!=null && !dataElementValue.isEmpty()){isoMsg.set(1,dataElementValue);}

            dataElementValue = txnBean.getDE2();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(2, dataElementValue);
            }
            dataElementValue = txnBean.getDE3();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(3, dataElementValue);
            }
            dataElementValue = txnBean.getDE4();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(4, dataElementValue);
                Configurations.GENERATED_IPM_AMOUNT_CHECKSUM = Configurations.GENERATED_IPM_AMOUNT_CHECKSUM + Long.parseLong(dataElementValue);
            }
            dataElementValue = txnBean.getDE5();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(5, dataElementValue);
            }
            dataElementValue = txnBean.getDE6();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(6, dataElementValue);
            }
            dataElementValue = txnBean.getDE7();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(7, dataElementValue);
            }
            dataElementValue = txnBean.getDE8();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(8, dataElementValue);
            }
            dataElementValue = txnBean.getDE9();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(9, dataElementValue);
            }
            dataElementValue = txnBean.getDE10();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(10, dataElementValue);
            }
            dataElementValue = txnBean.getDE11();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(11, dataElementValue);
            }
            dataElementValue = txnBean.getDE12();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(12, dataElementValue);
            }
            dataElementValue = txnBean.getDE13();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(13, dataElementValue);
            }
            dataElementValue = txnBean.getDE14();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(14, dataElementValue);
            }
            dataElementValue = txnBean.getDE15();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(15, dataElementValue);
            }
            dataElementValue = txnBean.getDE16();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(16, dataElementValue);
            }
            dataElementValue = txnBean.getDE17();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(17, dataElementValue);
            }
            dataElementValue = txnBean.getDE18();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(18, dataElementValue);
            }
            dataElementValue = txnBean.getDE19();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(19, dataElementValue);
            }
            dataElementValue = txnBean.getDE20();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(20, dataElementValue);
            }
            dataElementValue = txnBean.getDE21();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(21, dataElementValue);
            }
            dataElementValue = txnBean.getDE22();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(22, dataElementValue);
            }
            dataElementValue = txnBean.getDE23();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(23, dataElementValue);
            }
            dataElementValue = txnBean.getDE24();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(24, dataElementValue);
            }
            dataElementValue = txnBean.getDE25();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(25, dataElementValue);
            }
            dataElementValue = txnBean.getDE26();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(26, dataElementValue);
            }
            dataElementValue = txnBean.getDE27();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(27, dataElementValue);
            }
            dataElementValue = txnBean.getDE28();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(28, dataElementValue);
            }
            dataElementValue = txnBean.getDE29();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(29, dataElementValue);
            }
            dataElementValue = txnBean.getDE30();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(30, dataElementValue);
            }
            dataElementValue = txnBean.getDE31();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(31, dataElementValue);
            }
            dataElementValue = txnBean.getDE32();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(32, dataElementValue);
            }
            dataElementValue = txnBean.getDE33();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(33, dataElementValue);
            }
            dataElementValue = txnBean.getDE34();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(34, dataElementValue);
            }
            dataElementValue = txnBean.getDE35();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(35, dataElementValue);
            }
            dataElementValue = txnBean.getDE36();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(36, dataElementValue);
            }
            dataElementValue = txnBean.getDE37();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(37, dataElementValue);
            }
            dataElementValue = txnBean.getDE38();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(38, dataElementValue);
            }
            dataElementValue = txnBean.getDE39();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(39, dataElementValue);
            }
            dataElementValue = txnBean.getDE40();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(40, dataElementValue);
            }
            dataElementValue = txnBean.getDE41();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(41, dataElementValue);
            }
            dataElementValue = txnBean.getDE42();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(42, dataElementValue);
            }
            dataElementValue = txnBean.getDE43();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(43, dataElementValue);
            }
            dataElementValue = txnBean.getDE44();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(44, dataElementValue);
            }
            dataElementValue = txnBean.getDE45();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(45, dataElementValue);
            }
            dataElementValue = txnBean.getDE46();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(46, dataElementValue);
            }
            dataElementValue = txnBean.getDE47();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(47, dataElementValue);
            }

            // setting PDS values for field 48
            Map<String, String> pdsMap = txnBean.getPdsMap(); // map containg pds id as the key and the value without calculated length
            if (pdsMap != null && !pdsMap.isEmpty()) {
                String field48ValueStr = createField48(pdsMap);
                isoMsg.set(48, field48ValueStr);
            }

            dataElementValue = txnBean.getDE49();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(49, dataElementValue);
            }
            dataElementValue = txnBean.getDE50();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(50, dataElementValue);
            }
            dataElementValue = txnBean.getDE51();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(51, dataElementValue);
            }
            dataElementValue = txnBean.getDE52();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(52, dataElementValue);
            }
            dataElementValue = txnBean.getDE53();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(53, dataElementValue);
            }
            dataElementValue = txnBean.getDE54();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(54, dataElementValue);
            }
            dataElementValue = txnBean.getDE55();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(55, dataElementValue);
            }
            dataElementValue = txnBean.getDE56();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(56, dataElementValue);
            }
            dataElementValue = txnBean.getDE57();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(57, dataElementValue);
            }
            dataElementValue = txnBean.getDE58();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(58, dataElementValue);
            }
            dataElementValue = txnBean.getDE59();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(59, dataElementValue);
            }
            dataElementValue = txnBean.getDE60();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(60, dataElementValue);
            }
            dataElementValue = txnBean.getDE61();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(61, dataElementValue);
            }
            dataElementValue = txnBean.getDE62();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(62, dataElementValue);
            }
            dataElementValue = txnBean.getDE63();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(63, dataElementValue);
            }
            dataElementValue = txnBean.getDE64();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(64, dataElementValue);
            }
            dataElementValue = txnBean.getDE65();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(65, dataElementValue);
            }
            dataElementValue = txnBean.getDE66();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(66, dataElementValue);
            }
            dataElementValue = txnBean.getDE67();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(67, dataElementValue);
            }
            dataElementValue = txnBean.getDE68();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(68, dataElementValue);
            }
            dataElementValue = txnBean.getDE69();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(69, dataElementValue);
            }
            dataElementValue = txnBean.getDE70();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(70, dataElementValue);
            }
            // message number de71 will be recalculated.value in database will not used
            isoMsg.set(71, Integer.toString(de71Count));

            dataElementValue = txnBean.getDE72();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(72, dataElementValue);
            }
            dataElementValue = txnBean.getDE73();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(73, dataElementValue);
            }
            dataElementValue = txnBean.getDE74();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(74, dataElementValue);
            }
            dataElementValue = txnBean.getDE75();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(75, dataElementValue);
            }
            dataElementValue = txnBean.getDE76();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(76, dataElementValue);
            }
            dataElementValue = txnBean.getDE77();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(77, dataElementValue);
            }
            dataElementValue = txnBean.getDE78();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(78, dataElementValue);
            }
            dataElementValue = txnBean.getDE79();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(79, dataElementValue);
            }
            dataElementValue = txnBean.getDE80();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(80, dataElementValue);
            }
            dataElementValue = txnBean.getDE81();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(81, dataElementValue);
            }
            dataElementValue = txnBean.getDE82();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(82, dataElementValue);
            }
            dataElementValue = txnBean.getDE83();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(83, dataElementValue);
            }
            dataElementValue = txnBean.getDE84();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(84, dataElementValue);
            }
            dataElementValue = txnBean.getDE85();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(85, dataElementValue);
            }
            dataElementValue = txnBean.getDE86();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(86, dataElementValue);
            }
            dataElementValue = txnBean.getDE87();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(87, dataElementValue);
            }
            dataElementValue = txnBean.getDE88();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(88, dataElementValue);
            }
            dataElementValue = txnBean.getDE89();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(89, dataElementValue);
            }
            dataElementValue = txnBean.getDE90();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(90, dataElementValue);
            }
            dataElementValue = txnBean.getDE91();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(91, dataElementValue);
            }
            dataElementValue = txnBean.getDE92();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(92, dataElementValue);
            }
            dataElementValue = txnBean.getDE93();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(93, dataElementValue);
            }
            dataElementValue = txnBean.getDE94();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(94, dataElementValue);
            }
            dataElementValue = txnBean.getDE95();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(95, dataElementValue);
            }
            dataElementValue = txnBean.getDE96();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(96, dataElementValue);
            }
            dataElementValue = txnBean.getDE97();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(97, dataElementValue);
            }
            dataElementValue = txnBean.getDE98();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(98, dataElementValue);
            }
            dataElementValue = txnBean.getDE99();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(99, dataElementValue);
            }
            dataElementValue = txnBean.getDE100();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(100, dataElementValue);
            }
            dataElementValue = txnBean.getDE101();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(101, dataElementValue);
            }
            dataElementValue = txnBean.getDE102();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(102, dataElementValue);
            }
            dataElementValue = txnBean.getDE103();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(103, dataElementValue);
            }
            dataElementValue = txnBean.getDE104();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(104, dataElementValue);
            }
            dataElementValue = txnBean.getDE105();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(105, dataElementValue);
            }
            dataElementValue = txnBean.getDE106();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(106, dataElementValue);
            }
            dataElementValue = txnBean.getDE107();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(107, dataElementValue);
            }
            dataElementValue = txnBean.getDE108();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(108, dataElementValue);
            }
            dataElementValue = txnBean.getDE109();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(109, dataElementValue);
            }
            dataElementValue = txnBean.getDE110();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(110, dataElementValue);
            }
            dataElementValue = txnBean.getDE111();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(111, dataElementValue);
            }
            dataElementValue = txnBean.getDE112();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(112, dataElementValue);
            }
            dataElementValue = txnBean.getDE113();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(113, dataElementValue);
            }
            dataElementValue = txnBean.getDE114();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(114, dataElementValue);
            }
            dataElementValue = txnBean.getDE115();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(115, dataElementValue);
            }
            dataElementValue = txnBean.getDE116();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(116, dataElementValue);
            }
            dataElementValue = txnBean.getDE117();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(117, dataElementValue);
            }
            dataElementValue = txnBean.getDE118();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(118, dataElementValue);
            }
            dataElementValue = txnBean.getDE119();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(119, dataElementValue);
            }
            dataElementValue = txnBean.getDE120();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(120, dataElementValue);
            }
            dataElementValue = txnBean.getDE121();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(121, dataElementValue);
            }
            dataElementValue = txnBean.getDE122();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(122, dataElementValue);
            }
            dataElementValue = txnBean.getDE123();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(123, dataElementValue);
            }
            dataElementValue = txnBean.getDE124();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(124, dataElementValue);
            }
            dataElementValue = txnBean.getDE125();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(125, dataElementValue);
            }
            dataElementValue = txnBean.getDE126();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(126, dataElementValue);
            }
            dataElementValue = txnBean.getDE127();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(127, dataElementValue);
            }
            dataElementValue = txnBean.getDE128();
            if (dataElementValue != null && !dataElementValue.isEmpty()) {
                isoMsg.set(128, dataElementValue);
            }

            // Get and print the output result
            byte[] data = isoMsg.pack();
            //byte[] data = ISOUtil.asciiToEbcdic(isoMsg.pack());

            /*isoMsg.dump(System.out, "");
            isoMsg.unpack(data);
            System.out.println(ISOUtil.hexdump(data));
            isoMsg.dump(System.out, "");*/
            try (FileOutputStream outputStream = new FileOutputStream(filePath, true)) {
                //write length part of tranasction record
                for (int j = Integer.toHexString(data.length).length(); j <= 4; j++) {
                    outputStream.write(0); //initial 00 values in legth field
                }
                // when data.length write to outputstream as bytes only two hex value will be written.to avoid it split
                //length to hex values seperately and write individually. eg : 00 00 01 1C
                Stack<String> stk = new Stack<>();
                int previousX = Integer.toHexString(data.length).length();
                for (int x = Integer.toHexString(data.length).length() - 2; x > 0; x -= 2) {
                    stk.push(Integer.toHexString(data.length).substring(x, previousX));
                    previousX = x;
                }
                stk.push(Integer.toHexString(data.length).substring(0, previousX));
                for (int z = 0; z <= stk.size(); z++) {
                    outputStream.write(Integer.parseInt(stk.pop(), 16));
                }

                //write transaction record
                outputStream.write(data);
            }

        } catch (IOException e) {
            throw e;
        }
    }

    public void writeHeaderRecordToIpmFile(String filePath) throws IOException, ISOException {
        File f = new File(filePath);
        f.createNewFile(); // if file not exists, create a new file

        try {
            // Create ISO Message
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);

            isoMsg.setMTI("1644");
            isoMsg.set(24, "697");

            // create PDS values for field48 list
            Map<String, String> pdsMap = new LinkedHashMap<>();

            //create pds0105
            SimpleDateFormat format = new SimpleDateFormat("yyMMdd");
            String eodDateStr = format.format(Configurations.EOD_DATE);
            Random r = new Random(System.currentTimeMillis());
            String randomSequenceNumber = Integer.toString(10000 + r.nextInt(20000));
            String pds105Value = "002".concat(eodDateStr).concat(ISOUtil.zeropad(Configurations.TRANSACTION_ORIGINATOR_INSTITUTION_ID_CODE, 11)).concat(randomSequenceNumber);
            pdsMap.put("105", pds105Value);
            Configurations.GENERATED_IPM_FILE_ID = pds105Value;

            //create pds0122
            String pds122Value = Configurations.PROCESSING_MODE;
            pdsMap.put("122", pds122Value);

            if (pdsMap != null && !pdsMap.isEmpty()) {
                String field48ValueStr = createField48(pdsMap);
                isoMsg.set(48, field48ValueStr);
            }
            isoMsg.set(71, "1"); //1 for header record,all other transactions and fotter will be sequentially increment the value

            // Get and print the output result
            byte[] data = isoMsg.pack();
            // byte[] data = ISOUtil.asciiToEbcdic(isoMsg.pack());

            try (FileOutputStream outputStream = new FileOutputStream(filePath, true)) {
                //write length part of tranasction record
                for (int j = Integer.toHexString(data.length).length(); j <= 4; j++) {
                    outputStream.write(0); //initial 00 values in legth field
                }
                // when data.length write to outputstream as bytes only two hex value will be written.to avoid it split
                //length to hex values seperately and write individually. eg : 00 00 01 1C
                Stack<String> stk = new Stack<>();
                int previousX = Integer.toHexString(data.length).length();
                for (int x = Integer.toHexString(data.length).length() - 2; x > 0; x -= 2) {
                    stk.push(Integer.toHexString(data.length).substring(x, previousX));
                    previousX = x;
                }
                stk.push(Integer.toHexString(data.length).substring(0, previousX));
                for (int z = 0; z <= stk.size(); z++) {
                    outputStream.write(Integer.parseInt(stk.pop(), 16));
                }

                //write transaction record
                outputStream.write(data);
            }

        } catch (IOException e) {
            throw e;
        }
    }

    private String createField48(Map<String, String> pdsMap) throws ISOException{
        /**
         * **************************
         * Field 48 Format as follows
         * [LLL][TAG|LENGTH|DATA....][TAG|LENGTH|DATA...]....... [LLL] is total
         * length of whole tag string. will automatically calculated by jpos
         * library
         */

        String field48ValueStr = "";
        for (Map.Entry<String, String> entry : pdsMap.entrySet()) {
            String key = ISOUtil.zeropad(entry.getKey(), 4); // 4 digit PDS number eg: 0023
            String value = entry.getValue(); // PDS value without calculated length to begining
            String valueLength = ISOUtil.zeropad(Integer.toString(value.length()), 3); // // PDS value length
            field48ValueStr = field48ValueStr.concat(key).concat(valueLength).concat(value); //PDS Number + Value Length + Value
        }
        return field48ValueStr;
    }

    @Override
    public void addSummaries() {

    }
}
