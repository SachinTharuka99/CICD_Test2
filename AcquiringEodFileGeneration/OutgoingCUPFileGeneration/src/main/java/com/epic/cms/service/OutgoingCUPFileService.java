/**
 * Author : yasiru_l
 * Date : 6/30/2023
 * Time : 9:43 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.Exception.RejectException;
import com.epic.cms.dao.OutgoingCUPFileDao;
import com.epic.cms.model.bean.OutgoingCUPFileTransactionBean;
import com.epic.cms.util.Configurations;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class OutgoingCUPFileService {

    public static OutgoingCUPFileTransactionBean originalTxnBean; //for reversal transactions
    int[] blockBitMap = new int[16];
    StringBuffer blockBitmapBuffer = null;
    @Autowired
    OutgoingCUPFileDao outgoingCUPFileDao;
    String outgoingCUPFilePath = "";
    int selectedSuccessCount = 0;
    int selectedFaildCount = 0;
    int fileWriteSuccessCount = 0;
    int fileWriteFailedCount = 0;

    Set<String> pendingTxnIdList = null;
    StringBuffer txnValueBuffer = null;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void generateOutgoingCUPFile(ArrayList<OutgoingCUPFileTransactionBean> outgoingCUPFileDataList) throws Exception {

        try{
            if (Configurations.OUTGOING_CUP_FILE_TXN_BLOCK_FIELD_TABLE != null) {

                for(OutgoingCUPFileTransactionBean bean : outgoingCUPFileDataList){

                    try {

                        this.addToStatementOutgoingFieldIdentityTable(bean);
                        outgoingCUPFileDao.updateEodMerchantTransactionFileStatus(bean.getTransactionId());

                        selectedSuccessCount++;
                        Configurations.PROCESS_SUCCESS_COUNT++;

                    }catch (Exception e){

                        if (e.getCause() != null && e.getCause() instanceof RejectException) {
                            //if exception occured for reject from validation
                            RejectException rejEx = (RejectException) e.getCause();
                            //add reject transaction to OUTGOINGREJECT Table
                            outgoingCUPFileDao.insertRejectOutgoingCUPTransaction(Integer.toString(Configurations.EOD_ID), bean.getTransactionId(), rejEx.getMessage());

                        } else { //not a reject exception
                            logError.error("Exception occured while processing the transaction : " + bean.getTransactionId(), e);
                        }

                    }
                }

                // Generate outgoig UPI Statement file..
                File fileCreateDirectory = new File(Configurations.OUTGOING_CUP_FILE_PATH);

                // directory will create if doesn't exits
                fileCreateDirectory.mkdirs();

                // getnerate outgoing file name...
                String outgoingUPIStatementFileName = Configurations.OUTGOING_CUP_FILE_NAME_PREFIX
                        + new SimpleDateFormat("yyMMdd").format(Configurations.EOD_DATE)
                        + Configurations.OUTGOING_CUP_FILE_NAME_SUFIX;

                outgoingCUPFilePath = Configurations.OUTGOING_CUP_FILE_PATH + outgoingUPIStatementFileName;
                File outgoingUpiStatementFile = new File(outgoingCUPFilePath);
                outgoingUpiStatementFile.createNewFile(); // if file not exists, create a new file

                // genetate and write the Header for the file
                String fileHeader = this.generateHeader();
                BufferedWriter headerWriter = new BufferedWriter(new FileWriter(outgoingCUPFilePath, true));
                headerWriter.write(fileHeader);
                headerWriter.newLine();
                headerWriter.flush();
                headerWriter.close();

                // get pending transactino id list
                pendingTxnIdList = outgoingCUPFileDao.getPendingOutgoingUPIStatementTxnIDList();

                for (String pendigTxn:pendingTxnIdList) {
                    txnValueBuffer = outgoingCUPFileDao.getUPIStatementFileTxnFieldValues(pendigTxn);

                    try {
                        BufferedWriter out = new BufferedWriter(new FileWriter(outgoingCUPFilePath, true));
                        out.write(txnValueBuffer.toString());
                        out.newLine();
                        out.flush();
                        out.close();
                        fileWriteSuccessCount++;
                        Configurations.PROCESS_SUCCESS_COUNT = fileWriteSuccessCount;
                        // UPDATE TRANSACTION STATUS AS PICKED AND FILE NAME..
                        outgoingCUPFileDao.updateOutgoingUpiStatementFieldIdentityFileStatus(pendigTxn, outgoingUPIStatementFileName);
                    } catch (Exception e) {
                        fileWriteFailedCount++;
                        Configurations.PROCESS_FAILD_COUNT = fileWriteFailedCount;
                        logError.error("Errors occurred while writing the transaction : " + pendigTxn);
                    }
                }

                // generate and write tailer for the file
                String fileTailer = this.generateTailer();
                BufferedWriter tailerWriter = new BufferedWriter(new FileWriter(outgoingCUPFilePath, true));
                tailerWriter.write(fileTailer);
                tailerWriter.newLine();
                tailerWriter.flush();
                tailerWriter.close();

                // insert file record into DOWNLOADFILE table.. for download purpose from web
                outgoingCUPFileDao.insertOutgoingStatementFilePathToDownloadFile(outgoingUPIStatementFileName);

                logInfo.info("Successfully created Outgoing CUP File " + fileHeader);
            }
            selectedSuccessCount++;
            Configurations.PROCESS_SUCCESS_COUNT++;

        }catch (Exception e){

            selectedFaildCount++;
            Configurations.PROCESS_FAILD_COUNT++;
            logError.error("Block Fields Are Empty ! Can not be generate the CUP outgoing file .... " + e);
            throw e;
        }
    }

    public String generateHeader() throws ISOException {
        StringBuilder headerBuffer = new StringBuilder("");
        headerBuffer.append(this.getTxnCodeHeader());
        headerBuffer.append(this.getBlockbitmapHeader());
        headerBuffer.append(ISOUtil.strpad(this.getIIN(),11));
        headerBuffer.append(this.getBatchDate());
        headerBuffer.append(this.getGSCSReservedDate());
        headerBuffer.append(this.getVersionTag());
        headerBuffer.append(this.getVersionNumber());

        return headerBuffer.toString();
    }

    public String generateTailer() throws ISOException {
        StringBuilder tailerBuffer = new StringBuilder("");
        tailerBuffer.append(this.getTxnCodeTailer());
        tailerBuffer.append(this.getBlockbitmapTailer());
        tailerBuffer.append(this.getTransactionRecodeCountTailer());
        tailerBuffer.append(this.getMAK());
        tailerBuffer.append(this.getMAC());

        return tailerBuffer.toString();
    }

    public String getTxnCodeTailer() {
        return Configurations.CUP_FILE_TAILER_TXN_CODE;
    }

    public String getTransactionRecodeCountTailer() throws ISOException {
        String fileRecodeCount = ISOUtil.padleft(String.valueOf(fileWriteSuccessCount + 2), 10, '0');
        return fileRecodeCount;
    }

    public String getMAK() throws ISOException {
        return ISOUtil.padleft("", 16, ' ');
    }

    public String getMAC() throws ISOException {
        return ISOUtil.padleft("", 16, ' ');
    }

    public String getBlockbitmapTailer() {
        // tailer has only one block (block 0), therefor bitmap should be 1000 0000 0000 0000
        long bitmapBinaryTailer = Long.parseLong("1000000000000000");
        String bitMapHexStrTailer = this.decimalToHex(bitmapBinaryTailer);
        return bitMapHexStrTailer;
    }


    public String getGSCSReservedDate() throws ISOException {
        return ISOUtil.padleft("", 8, ' ');
    }

    public String getTxnCodeHeader() {
        return Configurations.CUP_FILE_HEADER_TXN_CODE;
    }

    public String getIIN() {
        return Configurations.INSTITUTION_IDENTIFICATION_NUMBER;
    }

    public String getVersionTag() {
        return Configurations.OUTGOING_CUP_FILE_HEADER_VERSION_TAG;
    }

    public String getVersionNumber() {
        return Configurations.OUTGOING_CUP_FILE_HEADER_VERSION_NUMBER;
    }

    public String getBatchDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        return simpleDateFormat.format(Configurations.EOD_DATE);
    }

    public String getBlockbitmapHeader() {
        // header has only one block (block 0), therefor bitmap should be 1000 0000 0000 0000
        long bitmapBinary = Long.parseLong("1000000000000000");
        String bitMapHexStr = this.decimalToHex(bitmapBinary);
        return bitMapHexStr;
    }

    public void addToStatementOutgoingFieldIdentityTable(OutgoingCUPFileTransactionBean txnBean) throws Exception {

        // Decide block bitmap
        if (txnBean.getTransactionType().equals(Configurations.TXN_TYPE_SALE)||txnBean.getTransactionType().equals(Configurations.TXN_TYPE_PRE_COMPLETION)||txnBean.getTransactionType().equals(Configurations.TXN_TYPE_CUP_QR_PAYMENT)) { // settlement transaction with trnsaction code 100
            txnBean.setTransactionCode("100");
            blockBitMap[0] = 1;

            if (txnBean.getPosEntryMode() != null && !txnBean.getPosEntryMode().isEmpty()) {
                if (txnBean.getPosEntryMode().startsWith("05") || txnBean.getPosEntryMode().startsWith("07")) {
                    blockBitMap[1] = 1;
                }
            }
            blockBitMap[2] = 1;

        } else if (txnBean.getTransactionType().equals(Configurations.TXN_TYPE_REFUND) || txnBean.getTransactionType().equals(Configurations.TXN_TYPE_REVERSAL)|| txnBean.getTransactionType().equals(Configurations.TXN_TYPE_CUP_QR_REFUND)) { // offline refund/reversal transaction with transaction code 101
            txnBean.setTransactionCode("101");
            blockBitMap[0] = 1;

            if (txnBean.getPosEntryMode() != null && !txnBean.getPosEntryMode().isEmpty()) {

                if (txnBean.getPosEntryMode().startsWith("05") || txnBean.getPosEntryMode().startsWith("07")) {
                    blockBitMap[1] = 1;
                }
            }
            blockBitMap[2] = 1;

        } else if (txnBean.getTransactionType().equals(Configurations.TXN_TYPE_CASH_ADVANCE)) { // cash advance transaction with transaction code 102
            txnBean.setTransactionCode("102");
            blockBitMap[0] = 1;

            if (txnBean.getPosEntryMode() != null && !txnBean.getPosEntryMode().isEmpty()) {

                if (txnBean.getPosEntryMode().startsWith("05") || txnBean.getPosEntryMode().startsWith("07")) {
                    blockBitMap[1] = 1;
                }
            }
            blockBitMap[2] = 1;
        }
        blockBitMap[1] = 1;

        //for reversal transaction need to find original transaction if exists
        if(txnBean.getTransactionType().equals(Configurations.TXN_TYPE_REVERSAL) || txnBean.getTransactionType().equals(Configurations.TXN_TYPE_CUP_QR_REFUND)){
            originalTxnBean = outgoingCUPFileDao.getOriginalTxnInfoForReversalTxn(txnBean.getOriginalTxnId());
        }

        // create block bitmap hex value
        blockBitmapBuffer = new StringBuffer();
        for (int i = 0; i < blockBitMap.length; i++) {
            blockBitmapBuffer.append(blockBitMap[i]);
        }
        long bitmapBinary = Long.parseLong(blockBitmapBuffer.toString());
        txnBean.setBlockBitmap(decimalToHex(bitmapBinary));

        // iterate blockbitmap and fill the fields which are related with particular block
        for (int i = 0; i < blockBitMap.length; i++) {

            if (blockBitMap[i] == 1) {

                if (Configurations.OUTGOING_CUP_FILE_TXN_BLOCK_FIELD_TABLE.get(txnBean.getTransactionCode() + "|" + "BLOCK" + i) != null) {

                    String[] blockFieldsArr = Configurations.OUTGOING_CUP_FILE_TXN_BLOCK_FIELD_TABLE.get(txnBean.getTransactionCode() + "|" + "BLOCK" + i).split("\\|");
                    String[] fieldValuesBlock = new String[blockFieldsArr.length];

                    for (int j = 0; j < blockFieldsArr.length; j++) {
                        //dynamically find the method to prepare the value
                        Class<?> cls = Class.forName("com.epic.cms");
                        Object ins = cls.newInstance();

                        Object[] parameterObj = {};
                        Method method = cls.getDeclaredMethod("get" + blockFieldsArr[j]);

                        fieldValuesBlock[j] = (String) method.invoke(ins, parameterObj);
                    }

                    outgoingCUPFileDao.insertOutgoingStatementFieldIdentity(txnBean.getTransactionId(), i, txnBean.getTransactionCode(), fieldValuesBlock);

                }else{ // need to configure db
                    throw new Exception("Transaction Code - "+txnBean.getTransactionCode() + " & " + "BLOCK" + i +" Should be Configure in the DB .");
                }
            }
        }
    }

    // method to convert decimal to hexadecimal
    public String decimalToHex(long binary) {
        // variable to store the output of the
        // binaryToDecimal() method
        int decimalNumber = binaryToDecimal(binary);

        // converting the integer to the desired
        // hex string using toHexString() method
        String hexNumber = Integer.toHexString(decimalNumber);

        // converting the string to uppercase
        // for uniformity
        hexNumber = hexNumber.toUpperCase();

        // returning the final hex string
        return hexNumber;
    }

    // method to convert binary to decimal
    public int binaryToDecimal(long binary) {

        // variable to store the converted
        // binary number
        int decimalNumber = 0, i = 0;

        // loop to extract the digits of the binary
        while (binary > 0) {

            // extracting the digits by getting
            // remainder on dividing by 10 and
            // multiplying by increasing integral
            // powers of 2
            decimalNumber += Math.pow(2, i++) * (binary % 10);

            // updating the binary by eliminating
            // the last digit on division by 10
            binary /= 10;
        }

        // returning the decimal number
        return decimalNumber;
    }

}
