/**
 * Author : sharuka_j
 * Date : 2/2/2023
 * Time : 9:34 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.MerchantPaymentFileDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.MerchantCustomer;
import com.epic.cms.util.StatusVarList;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.epic.cms.util.CommonMethods.*;

@Service
public class MerchantPaymentFileService {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    LogManager logManager;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    StatusVarList statusList;
    @Autowired
    MerchantPaymentFileDao merchantPaymentFileDao;
    boolean insertedTODownloadFile = false;
    LinkedHashMap paymentfilestatus = new LinkedHashMap();
    boolean toDeleteStatusDirect = true;
    HashMap<String, String> currencyList = new HashMap<String, String>();
    ArrayList<String> merchantCustomerList = new ArrayList<>();
    ArrayList<String> merchantLocationList = new ArrayList<>();

    SimpleDateFormat sdf6 = new SimpleDateFormat("yyyyMMdd");

    boolean toDeleteStatusSlip = true;
    private HashMap<String, HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>> totalMerchantListOnPaymod;
    private HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>> totalMerchantListOnPaystatus;
    private HashMap<String, ArrayList<MerchantPaymentCycleBean>> merchantList;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ArrayList<String> paymentFile(Map.Entry<String, HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>> entrySet, String fileNameF1, String fileNameF2) {
        try {
            String payMode = entrySet.getKey();
            totalMerchantListOnPaystatus = entrySet.getValue();

            if (!totalMerchantListOnPaystatus.isEmpty() && totalMerchantListOnPaystatus.size() > 0) {

                if (payMode.equalsIgnoreCase(Configurations.MERCHANT_PAY_MODE_DIRECT)) {
                    insertedTODownloadFile = this.createMerchantPaymentFileForDirectAcc(totalMerchantListOnPaystatus, payMode, fileNameF1, fileNameF2, insertedTODownloadFile);
                } else if (payMode.equalsIgnoreCase(Configurations.MERCHANT_PAY_MODE_SLIPS)) {
                    this.createMerchantPaymentFileForSlips(totalMerchantListOnPaystatus, payMode);
                } else if (payMode.equalsIgnoreCase(Configurations.MERCHANT_PAY_MODE_CHEQUE)) {

                    if (totalMerchantListOnPaystatus.containsKey(statusList.getNO_STATUS_0())) {
                        merchantList = totalMerchantListOnPaystatus.get(statusList.getNO_STATUS_0());
                        if (merchantList.size() > 0) {
                            for (Map.Entry<String, ArrayList<MerchantPaymentCycleBean>> entrySet1 : merchantList.entrySet()) {
                                for (MerchantPaymentCycleBean bean : entrySet1.getValue()) {
                                    String mId = bean.getMerchantId();
                                  logError.error("Invalid Merchant Payment mode for mid : " + mId);
                                }
                            }
                        }
                    }

                    if (totalMerchantListOnPaystatus.containsKey(statusList.getYES_STATUS_1())) {
                        merchantList = totalMerchantListOnPaystatus.get(statusList.getYES_STATUS_1());
                        if (merchantList.size() > 0) {
                            for (Map.Entry<String, ArrayList<MerchantPaymentCycleBean>> entrySet1 : merchantList.entrySet()) {
                                String merCusNo = entrySet1.getKey();
                                logError.error("Invalid Merchant Payment mode for merchant Customer No : " + merCusNo);
                            }
                        }
                    }
                }
            }
            Configurations.PROCESS_SUCCESS_COUNT++;
        } catch (Exception e) {
            Configurations.PROCESS_FAILD_COUNT++;
            logError.error("exception ", e);
        }
        return merchantCustomerList;
    }


    private boolean createMerchantPaymentFileForDirectAcc(HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>> totalMerchantListOnPaystatus, String paymentMode, String fileNameF1, String fileNameF2, boolean insertToDownload) throws Exception {

        FileWriter writer = null, writer2 = null;
        BufferedWriter buffer = null, buffer2 = null;
        File file = null, backupFile = null, file2 = null, backupFile2 = null;
        int noOfRecords = 0;
        int noOfCredit = 0;
        int noOfDedit = 0;
        int noofBatches = 0;
        boolean insertedDownloadFile = false;
        String fieldDelimeter = ",";
        String fieldDelimeterPipe = "|";
        int recordCount = 0;

        StringBuffer sbContent = new StringBuffer();

        //first file header builder
        StringBuffer sbHeader = new StringBuffer();
        //second file header builder
        StringBuffer sbHeader2 = new StringBuffer();

        try {

            SimpleDateFormat sdf5 = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdf9 = new SimpleDateFormat("MMM");

            String filepath;

            if (Configurations.SERVER_RUN_PLATFORM.equals("WINDOWS")) {
                filepath = Configurations.MERCHANT_PAYMENT_FILE_PATH + "\\" + Configurations.MERCHANT_PAYMENT_FILE_PATH_DIRECT + "\\";
            } else {
                filepath = Configurations.MERCHANT_PAYMENT_FILE_PATH + Configurations.MERCHANT_PAYMENT_FILE_PATH_DIRECT + "/";
            }

            file = new File(filepath);
            backupFile = new File(filepath + "BACKUP");

            if (!file.exists()) {
                if (file.mkdirs()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }

            if (!backupFile.exists()) {
                if (backupFile.mkdirs()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }

            //first file
            file = new File(filepath + fileNameF1 + ".csv");
            backupFile = new File(filepath + "BACKUP" + File.separator + fileNameF1 + ".csv");

            writer = new FileWriter(file, true);
            buffer = new BufferedWriter(writer);

            String fileAbsPath = file.getAbsolutePath();
            StringBuilder contentNo = new StringBuilder();
            StringBuilder contentYes = new StringBuilder();
            StringBuilder header = new StringBuilder();

            //second file
            file2 = new File(filepath + fileNameF2 + ".csv");
            backupFile2 = new File(filepath + "BACKUP" + File.separator + fileNameF2 + ".csv");

            writer2 = new FileWriter(file2, true);
            buffer2 = new BufferedWriter(writer2);

            String fileAbsPath2 = file2.getAbsolutePath();
            StringBuilder contentNo2 = new StringBuilder();
            StringBuilder contentYes2 = new StringBuilder();
            StringBuilder header2 = new StringBuilder();

            BigDecimal headerCreditBig = new BigDecimal("0.0");
            int headerCreditCount = 0;

            Date nextWorkingDay = merchantPaymentFileDao.getNextWorkingDay(new Date());
            String today3 = sdf5.format(nextWorkingDay);

            //Get Customer wise  PAYMENTMAINTEINANCESTATUS 'NO' merchant set
            if (totalMerchantListOnPaystatus.containsKey(statusList.getNO_STATUS_0())) {
                BigDecimal totalPaymentAmountBig = new BigDecimal("0.0");
                merchantList = totalMerchantListOnPaystatus.get(statusList.getNO_STATUS_0());
                if (merchantList.size() > 0) {
                    for (Map.Entry<String, ArrayList<MerchantPaymentCycleBean>> entrySet1 : merchantList.entrySet()) {

                        ArrayList<MerchantPaymentCycleBean> value1 = entrySet1.getValue();
                        ArrayList<MerchantPayBean> paymentList = new ArrayList<MerchantPayBean>();

                        for (MerchantPaymentCycleBean bean : value1) {
                            noofBatches++;
                            String mId = "";
                            try {
                                mId = bean.getMerchantId();
                                paymentList = merchantPaymentFileDao.getPaymentsFromEodMerchantpayment(mId);
                                noofBatches++;
                                BigDecimal crNetPaymentsBig = new BigDecimal("0.0");
                                BigDecimal drNetPaymentsBig = new BigDecimal("0.0");
                                boolean crDrStatus = false;
                                noOfRecords = 0;
                                noOfCredit = 0;
                                noOfDedit = 0;
                                ArrayList<String> payIdList = new ArrayList<>();

                                for (MerchantPayBean bean2 : paymentList) {
                                    noOfRecords++;
                                    payIdList.add(String.valueOf((bean2.getEodPayId())));
                                    if (bean2.getCrDrnetPayment().equalsIgnoreCase(Configurations.CREDIT)) {
                                        noOfCredit++;
                                        BigDecimal glAmountBig = new BigDecimal(bean2.getNetPayAmount());
                                        crNetPaymentsBig = crNetPaymentsBig.add(glAmountBig);
                                    } else if (bean2.getCrDrnetPayment().equalsIgnoreCase(Configurations.DEBIT)) {
                                        noOfDedit++;
                                        BigDecimal glAmountBig = new BigDecimal(bean2.getNetPayAmount());
                                        drNetPaymentsBig = drNetPaymentsBig.add(glAmountBig);
                                    } else{
                                        logError.error("Error in CRDR type in EODMERCHANTPAYEMENT file process.");
                                    }
                                }

                                if (crNetPaymentsBig.compareTo(drNetPaymentsBig) > 0) {
                                    totalPaymentAmountBig = crNetPaymentsBig.subtract(drNetPaymentsBig).setScale(2, RoundingMode.DOWN);
                                    crDrStatus = true;
                                    paymentfilestatus.put("Merchant ID", mId);
                                    paymentfilestatus.put("Total Payment amount ", totalPaymentAmountBig.toString() + " Cr");

                                } else if (drNetPaymentsBig.compareTo(crNetPaymentsBig) > 0) {
                                    totalPaymentAmountBig = drNetPaymentsBig.subtract(crNetPaymentsBig).setScale(2, RoundingMode.DOWN);
                                    crDrStatus = false;
                                    paymentfilestatus.put("Merchant ID", mId);
                                    paymentfilestatus.put("Total Payment amount ", totalPaymentAmountBig.toString() + " Dr");
                                }

                                String seqNo = "99" + Configurations.EOD_ID + validate(Integer.toString(noofBatches), 6, '0');

                                if (totalPaymentAmountBig.compareTo(BigDecimal.ZERO) > 0) {
                                    if (crDrStatus) {
                                        headerCreditBig = headerCreditBig.add(totalPaymentAmountBig);
                                        headerCreditCount++;

                                        //make narration

                                        String narration = "MERSet" +
                                                mId.substring(mId.length() - 9);

                                        //first file content - start
                                        contentNo.append(validateLength(seqNo, 35)); //SEQ_NO
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 35)); //PROD_CODE
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength(today3, 11)); //TRANSACTION_DATE
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateCurrencyLength(totalPaymentAmountBig.toString(), 19)); //CR_AMOUNT
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength(currencyList.get(bean.getCurrencyCode()), 3)); //PAYMENT_CURRENCY --validateLength(currencyList.get(bean.getCurrencyCode()).toString(), 3)
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength(bean.getAccountNo(), 19)); //CR_ACCOUNT --bean.getAccountNo()
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 17)); //BEN_ID
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 34)); //BEN_ACCOUNT
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 71)); //BENEFICIARY_NAME
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength(today3, 11)); //CR_VALUE_DATE
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength(narration, 15)); //CR_NARRATION -- mid
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 35)); //Bank Code+Branch Code
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 10)); //Tran Code
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 35)); //Host Deal Reference
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 6)); //Purpose Code-1
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 10)); //Purpose Code-2
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 35)); //additional_reference_no_1
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 3)); //Chargers waive (Y/N)
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 35)); //Charge Bearer
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 75)); //CHARGE TYPE
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 19)); //Charge Amount
                                        contentNo.append(fieldDelimeter);
                                        contentNo.append(validateLength("", 35)); //EXTERNAL_REF
                                        contentNo.append(fieldDelimeter);

                                        contentNo.append(System.lineSeparator());
                                        //first file content - end

                                        //second file content - start
                                        contentNo2.append(validateLength(Configurations.MERCHANT_PAYABLE_GL, 34)); //DEBIT.ACCT.NO -- Configurations.MERCHANT_PAYABLE_GL
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateLength("LKR", 3)); //DEBIT.CCY
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateCurrencyLength(totalPaymentAmountBig.toString(), 19)); //DEBIT.AMOUNT
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateLength(narration, 15));//DEBIT_THEIR_REF
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateLength("", 17)); //CUSTOMER.RATE
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateLength(bean.getAccountNo(), 19)); //CREDIT.ACCT.NO --bean.getAccountNo()
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateLength("", 3)); //CREDIT.CCY --To be pass only for cross currency transaction
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateLength("", 19)); //CREDIT.AMOUNT --Should not pass any value
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateLength(narration, 15));//CREDIT.THEIR.REF
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateLength("MP" + sdf9.format(new Date()).toUpperCase(), 5)); //BATCH REF
                                        contentNo2.append(fieldDelimeterPipe);
                                        String tranRef = String.format("%05d", headerCreditCount);
                                        contentNo2.append(validateLength(tranRef, 15)); //TRAN REF
                                        contentNo2.append(fieldDelimeter);

                                        contentNo2.append(validateLength("DFCC", 4)); //ORDERING.CUST
                                        contentNo2.append(fieldDelimeter);
                                        contentNo2.append(validateLength("3200", 6)); //PROFIT.CENTRE

                                        contentNo2.append(System.lineSeparator());
                                        //second file content - end

                                        merchantPaymentFileDao.updatePaymentFileStatus(payIdList);
                                        toDeleteStatusDirect = false;
                                    }
                                }
                                merchantLocationList.add(mId);
                            } catch (Exception ex) {
                                logError.error("Exception in Merchant Payment File Generation ", ex);
                            }
                        }
                    }
                }
            }
            recordCount = noOfRecords;
            //Get Customer wise  PAYMENTMAINTEINANCESTATUS 'YES' merchant set
            if (totalMerchantListOnPaystatus.containsKey(statusList.getYES_STATUS_1())) {
                BigDecimal totalPaymentAmountBig = new BigDecimal("0.0");
                merchantList = totalMerchantListOnPaystatus.get(statusList.getYES_STATUS_1());
                if (merchantList.size() > 0) {
                    for (Map.Entry<String, ArrayList<MerchantPaymentCycleBean>> entrySet1 : merchantList.entrySet()) {
                        noofBatches++;
                        String merCusId = entrySet1.getKey();//customerno
                        try {
                            ArrayList<MerchantPaymentCycleBean> value1 = entrySet1.getValue();

                            noofBatches++;

                            ArrayList<MerchantPayBean> paymentList = new ArrayList<MerchantPayBean>();

                            MerchantCustomerBean cusBean = merchantPaymentFileDao.getMerchantCustomerDetails(merCusId);

                            paymentList = merchantPaymentFileDao.getPaymentsForCustomerFromEodMerchantpayment(merCusId);
                            BigDecimal crNetPaymentsBig = new BigDecimal("0.0");
                            BigDecimal drNetPaymentsBig = new BigDecimal("0.0");
                            boolean crDrStatus = false;
                            noOfRecords = 0;
                            noOfCredit = 0;
                            noOfDedit = 0;
                            StringBuilder content = new StringBuilder();
                            sbContent.setLength(0);
                            ArrayList<String> payIdList = new ArrayList<>();

                            for (MerchantPayBean bean2 : paymentList) {
                                noOfRecords++;
                                payIdList.add(String.valueOf((bean2.getEodPayId())));
                                if (bean2.getCrDrnetPayment().equalsIgnoreCase(Configurations.CREDIT)) {
                                    noOfCredit++;
                                    BigDecimal glAmountBig = new BigDecimal(bean2.getNetPayAmount());
                                    crNetPaymentsBig = crNetPaymentsBig.add(glAmountBig);
                                } else if (bean2.getCrDrnetPayment().equalsIgnoreCase(Configurations.DEBIT)) {
                                    noOfDedit++;
                                    BigDecimal glAmountBig = new BigDecimal(bean2.getNetPayAmount());
                                    drNetPaymentsBig = drNetPaymentsBig.add(glAmountBig);
                                } else {
                                    logError.error("Exception in Merchant Payment File Generation ");
                                }

                            }

                            if (crNetPaymentsBig.compareTo(drNetPaymentsBig) > 0) {
                                totalPaymentAmountBig = crNetPaymentsBig.subtract(drNetPaymentsBig).setScale(2, RoundingMode.DOWN);
                                crDrStatus = true;
                                paymentfilestatus.put("Merchant Customer No", merCusId);
                                paymentfilestatus.put("Total Payment amount ", totalPaymentAmountBig.toString() + " Cr");

                            } else if (drNetPaymentsBig.compareTo(crNetPaymentsBig) > 0) {
                                totalPaymentAmountBig = drNetPaymentsBig.subtract(crNetPaymentsBig).setScale(2, RoundingMode.DOWN);
                                crDrStatus = false;
                                paymentfilestatus.put("Merchant Customer No", merCusId);
                                paymentfilestatus.put("Total Payment amount ", totalPaymentAmountBig.toString() + " Dr");

                            }

                            String seqNo = "99" + Configurations.EOD_ID + validate(Integer.toString(noofBatches), 6, '0');
                            if (totalPaymentAmountBig.compareTo(BigDecimal.ZERO) > 0) {
                                if (crDrStatus) {
                                    headerCreditBig = headerCreditBig.add(totalPaymentAmountBig);
                                    headerCreditCount++;

                                    //make narration
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("MERSet");

                                    if (merCusId.length() > 9) {
                                        sb.append(merCusId.substring(merCusId.length() - 9));
                                    } else if (merCusId.length() < 9) {
                                        String strLeftPad = String
                                                .format("%9s", merCusId)
                                                .replace(" ", "0");
                                        sb.append(strLeftPad);
                                    } else {
                                        sb.append(merCusId);
                                    }

                                    String narration = sb.toString();

                                    //first file content - start
                                    contentYes.append(validateLength(seqNo, 35)); //SEQ_NO
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 35)); //PROD_CODE
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength(today3, 11)); //TRANSACTION_DATE
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateCurrencyLength(totalPaymentAmountBig.toString(), 19)); //CR_AMOUNT
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength(currencyList.get(cusBean.getCurrencyCode()), 3)); //PAYMENT_CURRENCY --validateLength(currencyList.get(bean.getCurrencyCode()).toString(), 3)
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength(cusBean.getAccountNo(), 19)); //CR_ACCOUNT --bean.getAccountNo()
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 17)); //BEN_ID
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 34)); //BEN_ACCOUNT
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 71)); //BENEFICIARY_NAME
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength(today3, 11)); //CR_VALUE_DATE
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength(narration, 15)); //CR_NARRATION -- merCusId
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 35)); //Bank Code+Branch Code
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 10)); //Tran Code
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 35)); //Host Deal Reference
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 6)); //Purpose Code-1
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 10)); //Purpose Code-2
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 35)); //additional_reference_no_1
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 3)); //Chargers waive (Y/N)
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 35)); //Charge Bearer
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 75)); //CHARGE TYPE
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 19)); //Charge Amount
                                    contentYes.append(fieldDelimeter);
                                    contentYes.append(validateLength("", 35)); //EXTERNAL_REF
                                    contentYes.append(fieldDelimeter);

                                    contentYes.append(System.lineSeparator());
                                    //first file content - end

                                    //second file content - start
                                    contentYes2.append(validateLength(Configurations.MERCHANT_PAYABLE_GL, 34)); //DEBIT.ACCT.NO -- Configurations.MERCHANT_PAYABLE_GL
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateLength("LKR", 3)); //DEBIT.CCY
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateCurrencyLength(totalPaymentAmountBig.toString(), 19)); //DEBIT.AMOUNT
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateLength(narration, 15));//DEBIT_THEIR_REF
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateLength("", 17)); //CUSTOMER.RATE
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateLength(cusBean.getAccountNo(), 19)); //CREDIT.ACCT.NO --bean.getAccountNo()
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateLength("", 3)); //CREDIT.CCY --To be pass only for cross currency transaction
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateLength("", 19)); //CREDIT.AMOUNT --Should not pass any value
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateLength(narration, 15));//CREDIT.THEIR.REF
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateLength("MP" + sdf9.format(new Date()).toUpperCase(), 5)); //BATCH REF
                                    contentYes2.append(fieldDelimeterPipe);

                                    String tranRef = String.format("%05d", headerCreditCount);
                                    contentYes2.append(validateLength(tranRef, 15)); //TRAN REF
                                    contentYes2.append(fieldDelimeter);

                                    contentYes2.append(validateLength("DFCC", 4)); //ORDERING.CUST
                                    contentYes2.append(fieldDelimeter);
                                    contentYes2.append(validateLength("3200", 6)); //PROFIT.CENTRE

                                    contentYes2.append(System.lineSeparator());
                                    //second file content - end
                                    merchantPaymentFileDao.updatePaymentFileStatus(payIdList);
                                    toDeleteStatusDirect = false;

                                }
                            }
                            merchantCustomerList.add(merCusId);
                        } catch (Exception ex) {
                            logError.error("Exception in Merchant Payment File Generation ", ex);
                        }
                    }
                }
            }
            recordCount = recordCount + noOfRecords;
            if (headerCreditBig.compareTo(BigDecimal.ZERO) > 0 || headerCreditCount > 0) {

                //first file header - start
//              sbHeader.append(validateLength(Configurations.BULK_FILE_REFERENCE_MERCH_PAYOUT + sdf6.format(new Date()) + "/" + String.format("%05d", seq), 125)); //BATCH_FILE_REFERENCE
                sbHeader.append(validateLength(fileNameF1, 125)); //BATCH_FILE_REFERENCE
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Configurations.BULK_TYPE_MULTI, 10)); //BULK_TYPE
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(today3, 11)); //PROCESSING_DATE
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength("LKR", 3)); //Payment Currency
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Configurations.MERCHANT_PAYABLE_GL, 34)); //DR_ACCOUNT -- Configurations.MERCHANT_PAYABLE_GL
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(today3, 11)); //DR_VALUE_DATE
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength("Settlement on " + sdf6.format(new Date()), 35)); //DR_NARRATION
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateCurrencyLength(headerCreditBig.toString(), 19)); //Total Transactions
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Integer.toString(headerCreditCount), 19)); //NO of Transactions
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(fileNameF1, 35)); //File Name
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Configurations.CHANNEL_ID_PAYOUT_DIRECT, 30)); //CHANNEL_ID
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength("1", 10)); //CHK_SUM
                sbHeader.append(System.lineSeparator());
                //first file header - end

                //second file header - start
                String fileNamePrefix = Configurations.MERCHANT_PAYMENT_FILE_DIRECT_PREFIX_F2;
                sbHeader2.append(validateLength(fileNamePrefix.split("\\.")[0], 15)); //BATCH FILE REF
                sbHeader2.append(fieldDelimeter);
                sbHeader2.append(validateLength(fileNamePrefix.split("\\.")[0], 15));//DESCRIPTION
                sbHeader2.append(fieldDelimeter);
                sbHeader2.append(validateLength(Configurations.BULK_TYPE_SINGLE, 9)); //BULK TYPE
                sbHeader2.append(fieldDelimeter);
                sbHeader2.append(validateLength(Configurations.TRAN_TYPE_CREDIT, 6)); //TRAN TYPE
                sbHeader2.append(fieldDelimeter);
                sbHeader2.append(validateLength(Configurations.UPLOADED_METHOD_SINGLE, 6)); //UPLOAD METORD
                sbHeader2.append(fieldDelimeter);
                sbHeader2.append(validateLength(today3, 11)); //VALUE DATE
                sbHeader2.append(fieldDelimeter);
                sbHeader2.append(validateLength(fileNamePrefix.split("\\.")[0], 15)); //NARRATION
                sbHeader2.append(fieldDelimeter);

                sbHeader2.append(System.lineSeparator());
                //second file header - end

                //write - first file
                buffer.write(sbHeader.toString());
                buffer.write(contentNo.toString());
                buffer.write(contentYes.toString());

                logInfo.info("Merchant Payment File for Direct account created on '" + fileAbsPath + "'. ");

                //write - second file
                buffer2.write(sbHeader2.toString());
                buffer2.write(contentNo2.toString());
                buffer2.write(contentYes2.toString());

                logInfo.info("Merchant Payment File for Direct account created on '" + fileAbsPath2 + "'. ");

                toDeleteStatusDirect = false;
            } else {

                logInfo.info("Merchant Payment File for Direct account is Deleted due to empty line.");
            }
            int count1 = 0, count2 = 0;

            if (!insertToDownload) {

                //first file
                //insert to DOWNLOADFILE table
                count1 = merchantPaymentFileDao.InsertMerchantPaymentFilesIntoDownloadTable(fileNameF1 + ".dat", "MERCHANTPAYMENTDIRECT");

                //second file
                //insert to DOWNLOADFILE table
                count2 = merchantPaymentFileDao.InsertMerchantPaymentFilesIntoDownloadTable(fileNameF2 + ".dat", "MERCHANTPAYMENTDIRECT");

                EodOuputFileBean eodoutputfilebean2 = new EodOuputFileBean();
                eodoutputfilebean2.setFileName(fileNameF2 + ".csv");
                eodoutputfilebean2.setSubFolder(Configurations.MERCHANT_PAYMENT_FILE_PATH_DIRECT);
                eodoutputfilebean2.setNoOfRecords(recordCount);

                //insert to EODOUTPUTFIELS table
                merchantPaymentFileDao.insertOutputFiles(eodoutputfilebean2, "MERCHANTPAYMENTDIRECT");
            }

            if (count1 > 0 && count2 > 0) {
                insertedDownloadFile = true;
            } else if (insertToDownload) {
                insertedDownloadFile = true;
            }
            logInfo.info("Merchant Payment File for Direct account Process Successfully Completed. ");

        } catch (Exception e) {
            //delete first file
            if (file.exists()) {
                file.delete();
            }
            //delete second file
            if (file2.exists()) {
                file2.delete();
            }
            logError.error("Error Occured while writing Merchant Payment File for Direct account. Exception in Merchant Payment file process: ", e);
            throw e;
        } finally {
            try {
                if (buffer != null) {
                    try {
                        buffer.flush();
                        buffer.close();
                    } catch (Exception e) {
                        throw e;
                    }
                }
                if (buffer2 != null) {
                    try {
                        buffer2.flush();
                        buffer2.close();
                    } catch (Exception e) {
                        throw e;
                    }
                }
                try {
                    //delete files
                    if (toDeleteStatusDirect) {
                        //first file
                        System.out.println(file.getAbsolutePath());
                        file.delete();

                        //second file
                        System.out.println(file2.getAbsolutePath());
                        file2.delete();
                    }
                    //backup first file
                    if (file.exists()) {
                        FileUtils.copyFile(file, backupFile);
                    }
                    //backup second file
                    if (file2.exists()) {
                        FileUtils.copyFile(file2, backupFile2);
                    }

                } catch (Exception e) {
                    throw e;
                }
            } catch (Exception ee) {
                System.out.println("Error " + ee);
            }
        }
        return insertedDownloadFile;
    }

    private void createMerchantPaymentFileForSlips(HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>> totalMerchantListOnPaystatus, String paymentMode) throws Exception {

        FileWriter writer = null;
        BufferedWriter buffer = null;
        File file = null, backupFile = null;
        int noOfRecords = 0;
        int noOfCredit = 0;
        int noOfDedit = 0;
        int noofBatches = 0;
        int txnCount = 0;
        StringBuffer sbHeader = new StringBuffer();
        String fieldDelimeter = Configurations.OUTPUTFILE_FIELD_DELIMETER;

        try {

            SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
            SimpleDateFormat sd = new SimpleDateFormat("yyyy");
            String year = sd.format(new Date());
            String today1 = year + Integer.toString(Configurations.EOD_ID).substring(2, 6);

            String eodSeq = Integer.toString(Configurations.ERROR_EOD_ID).substring(6);
            int seq = Integer.parseInt(eodSeq) + 1;
            String sequence = String.format("%03d", seq);
            String fileName = Configurations.MERCHANT_PAYMENT_FILE_SLIPS_PREFIX + today1 + "." + sequence;

            String filepath;

            if (Configurations.SERVER_RUN_PLATFORM.equals("WINDOWS")) {
                filepath = Configurations.MERCHANT_PAYMENT_FILE_PATH + "\\" + Configurations.MERCHANT_PAYMENT_FILE_PATH_SLIPS + "\\";
            } else {
                filepath = Configurations.MERCHANT_PAYMENT_FILE_PATH + Configurations.MERCHANT_PAYMENT_FILE_PATH_SLIPS + "/";
            }

            file = new File(filepath);
            backupFile = new File(filepath + "BACKUP");

            if (!file.exists()) {
                if (file.mkdirs()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }

            if (!backupFile.exists()) {
                if (backupFile.mkdirs()) {
                    System.out.println("Directory is created!");
                } else {
                    System.out.println("Failed to create directory!");
                }
            }

            file = new File(filepath + fileName + ".txt");
            backupFile = new File(filepath + "BACKUP" + File.separator + fileName + ".txt");

            writer = new FileWriter(file, true);
            buffer = new BufferedWriter(writer);

            String fileAbsPath = file.getAbsolutePath();

            StringBuilder contentNo = new StringBuilder();
            StringBuilder contentYes = new StringBuilder();
            StringBuilder header = new StringBuilder();
            BigDecimal headerCreditBig = new BigDecimal("0.0");
            int headerCreditCount = 0;

            Date nextWorkingDay = merchantPaymentFileDao.getNextWorkingDay(new Date());
            String today = sdf2.format(nextWorkingDay);

            //Get Customer wise  PAYMENTMAINTEINANCESTATUS 'NO' merchant set
            if (totalMerchantListOnPaystatus.containsKey(statusList.getNO_STATUS_0())) {
                merchantList = totalMerchantListOnPaystatus.get(statusList.getNO_STATUS_0());
                if (merchantList.size() > 0) {
                    for (Map.Entry<String, ArrayList<MerchantPaymentCycleBean>> entrySet1 : merchantList.entrySet()) {
                        String key = entrySet1.getKey();//customerno
                        try {
                            ArrayList<MerchantPaymentCycleBean> value1 = entrySet1.getValue();
                            ArrayList<MerchantPayBean> paymentList = new ArrayList<MerchantPayBean>();

                            for (MerchantPaymentCycleBean bean : value1) {
                                noofBatches++;
                                BigDecimal totalPaymentAmountBig = new BigDecimal("0.0");
                                String mId = bean.getMerchantId();
                                paymentList = merchantPaymentFileDao.getPaymentsFromEodMerchantpayment(mId);
                                BigDecimal crNetPaymentsBig = new BigDecimal("0.0");
                                BigDecimal drNetPaymentsBig = new BigDecimal("0.0");
                                boolean crDrStatus = false;
                                noOfRecords = 0;
                                noOfCredit = 0;
                                noOfDedit = 0;
                                ArrayList<String> payIdList = new ArrayList<>();

                                for (MerchantPayBean bean2 : paymentList) {
                                    noOfRecords++;
                                    payIdList.add(String.valueOf((bean2.getEodPayId())));
                                    if (bean2.getCrDrnetPayment().equalsIgnoreCase(Configurations.CREDIT)) {
                                        noOfCredit++;
                                        BigDecimal glAmountBig = new BigDecimal(bean2.getNetPayAmount());
                                        crNetPaymentsBig = crNetPaymentsBig.add(glAmountBig);
                                    } else if (bean2.getCrDrnetPayment().equalsIgnoreCase(Configurations.DEBIT)) {
                                        noOfDedit++;
                                        BigDecimal glAmountBig = new BigDecimal(bean2.getNetPayAmount());
                                        drNetPaymentsBig = drNetPaymentsBig.add(glAmountBig);
                                    } else {
                                        logInfo.info("Error in CRDR type in EODMERCHANTPAYEMENT file process.");
                                    }
                                }

                                if (crNetPaymentsBig.compareTo(drNetPaymentsBig) > 0) {
                                    totalPaymentAmountBig = crNetPaymentsBig.subtract(drNetPaymentsBig).setScale(2, RoundingMode.DOWN);
                                    crDrStatus = true;
                                    paymentfilestatus.put("Merchant ID", mId);
                                    paymentfilestatus.put("Total Payment amount ", totalPaymentAmountBig.toString() + " Cr");

                                } else if (drNetPaymentsBig.compareTo(crNetPaymentsBig) > 0) {
                                    totalPaymentAmountBig = drNetPaymentsBig.subtract(crNetPaymentsBig).setScale(2, RoundingMode.DOWN);
                                    crDrStatus = false;
                                    paymentfilestatus.put("Merchant ID", mId);
                                    paymentfilestatus.put("Total Payment amount ", totalPaymentAmountBig.toString() + " Dr");

                                }

                                String seqNo = "99" + Configurations.EOD_ID + validate(Integer.toString(noofBatches), 6, '0');

                                if (crDrStatus) {
                                    //make narration
                                    String narration = "MERSet" +
                                            mId.substring(mId.length() - 9);

                                    contentNo.append(validateLength(seqNo, 35)); //SEQ_NO
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 35)); //PROD_CODE
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength(today.toUpperCase(), 11)); //TRANSACTION_DATE
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateCurrencyLength(totalPaymentAmountBig.toString(), 19)); //CR_AMOUNT
                                    headerCreditBig = headerCreditBig.add(totalPaymentAmountBig);
                                    headerCreditCount++;
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength(currencyList.get(bean.getCurrencyCode()), 3)); //PAYMENT_CURRENCY --validateLength(currencyList.get(bean.getCurrencyCode()).toString(), 3)
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength(bean.getAccountNo(), 19)); //CR_ACCOUNT --bean.getAccountNo()
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 17)); //BEN_ID
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 34)); //BEN_ACCOUNT
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 71)); //BENEFICIARY_NAME
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength(today.toUpperCase(), 11)); //CR_VALUE_DATE
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength(narration, 15)); //CR_NARRATION -- mid
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 35)); //Bank Code+Branch Code
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 10)); //Tran Code
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 35)); //Host Deal Reference
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 6)); //Purpose Code-1
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 10)); //Purpose Code-2
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 35)); //additional_reference_no_1
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("N", 3)); //Chargers waive (Y/N)
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 35)); //Charge Bearer
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 75)); //CHARGE TYPE
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 19)); //Charge Amount
                                    contentNo.append(fieldDelimeter);
                                    contentNo.append(validateLength("", 35)); //EXTERNAL_REF
                                    contentNo.append(fieldDelimeter);

                                    contentNo.append(System.lineSeparator());

                                    int i = merchantPaymentFileDao.updatePaymentFileStatus(payIdList);
                                    toDeleteStatusSlip = false;

                                }
                                merchantLocationList.add(mId);
                            }
                        } catch (Exception ex) {
                            logError.error("Error Occured while writing Merchant Payment File for slips for Mid : " + key + ". Exception : ", ex);
                        }
                    }
                }
            }
            txnCount = noOfRecords;
            //Get Customer wise  PAYMENTMAINTEINANCESTATUS 'YES' merchant set
            if (totalMerchantListOnPaystatus.containsKey(statusList.getYES_STATUS_1())) {
                merchantList = totalMerchantListOnPaystatus.get(statusList.getYES_STATUS_1());
                if (merchantList.size() > 0) {
                    for (Map.Entry<String, ArrayList<MerchantPaymentCycleBean>> entrySet1 : merchantList.entrySet()) {
                        noofBatches++;
                        BigDecimal totalPaymentAmountBig = new BigDecimal("0.0");
                        String merCusId = entrySet1.getKey();//customerno
                        try {
                            ArrayList<MerchantPaymentCycleBean> value1 = entrySet1.getValue();

                            ArrayList<MerchantPayBean> paymentList = new ArrayList<MerchantPayBean>();

                            MerchantCustomerBean cusBean = merchantPaymentFileDao.getMerchantCustomerDetails(merCusId);

                            paymentList = merchantPaymentFileDao.getPaymentsForCustomerFromEodMerchantpayment(merCusId);
                            BigDecimal crNetPaymentsBig = new BigDecimal("0.0");
                            BigDecimal drNetPaymentsBig = new BigDecimal("0.0");
                            boolean crDrStatus = false;
                            noOfRecords = 0;
                            noOfCredit = 0;
                            noOfDedit = 0;
                            ArrayList<String> payIdList = new ArrayList<>();

                            for (MerchantPayBean bean2 : paymentList) {
                                noOfRecords++;
                                payIdList.add(String.valueOf((bean2.getEodPayId())));
                                if (bean2.getCrDrnetPayment().equalsIgnoreCase(Configurations.CREDIT)) {
                                    noOfCredit++;
                                    BigDecimal glAmountBig = new BigDecimal(bean2.getNetPayAmount());
                                    crNetPaymentsBig = crNetPaymentsBig.add(glAmountBig);
                                } else if (bean2.getCrDrnetPayment().equalsIgnoreCase(Configurations.DEBIT)) {
                                    noOfDedit++;
                                    BigDecimal glAmountBig = new BigDecimal(bean2.getNetPayAmount());
                                    drNetPaymentsBig = drNetPaymentsBig.add(glAmountBig);
                                } else {
                                    logInfo.info("Error in CRDR type in EODMERCHANTPAYEMENT file process.");
                                }
                            }

                            if (crNetPaymentsBig.compareTo(drNetPaymentsBig) > 0) {
                                totalPaymentAmountBig = crNetPaymentsBig.subtract(drNetPaymentsBig).setScale(2, RoundingMode.DOWN);
                                crDrStatus = true;
                                paymentfilestatus.put("Merchant Customer No", merCusId);
                                paymentfilestatus.put("Total Payment amount ", totalPaymentAmountBig.toString() + " Cr");

                            } else if (drNetPaymentsBig.compareTo(crNetPaymentsBig) > 0) {
                                totalPaymentAmountBig = drNetPaymentsBig.subtract(crNetPaymentsBig).setScale(2, RoundingMode.DOWN);
                                crDrStatus = false;
                                paymentfilestatus.put("Merchant Customer No", merCusId);
                                paymentfilestatus.put("Total Payment amount ", totalPaymentAmountBig.toString() + " Dr");

                            }

                            String seqNo = "99" + Configurations.EOD_ID + validate(Integer.toString(noofBatches), 6, '0');

                            if (crDrStatus) {
                                //make narration
                                StringBuilder sb = new StringBuilder();
                                sb.append("MERSet");

                                if (merCusId.length() > 9) {
                                    sb.append(merCusId.substring(merCusId.length() - 9));
                                } else if (merCusId.length() < 9) {
                                    String strLeftPad = String
                                            .format("%9s", merCusId)
                                            .replace(" ", "0");
                                    sb.append(strLeftPad);
                                } else {
                                    sb.append(merCusId);
                                }

                                String narration = sb.toString();

                                contentYes.append(validateLength(seqNo, 35)); //SEQ_NO
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 35)); //PROD_CODE
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength(today.toUpperCase(), 11)); //TRANSACTION_DATE
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateCurrencyLength(totalPaymentAmountBig.toString(), 19)); //CR_AMOUNT
                                headerCreditBig = headerCreditBig.add(totalPaymentAmountBig);
                                headerCreditCount++;
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength(currencyList.get(cusBean.getCurrencyCode()), 3)); //PAYMENT_CURRENCY --validateLength(currencyList.get(bean.getCurrencyCode()).toString(), 3)
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength(cusBean.getAccountNo(), 19)); //CR_ACCOUNT --bean.getAccountNo()
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 17)); //BEN_ID
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 34)); //BEN_ACCOUNT
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 71)); //BENEFICIARY_NAME
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength(today.toUpperCase(), 11)); //CR_VALUE_DATE
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength(narration, 15)); //CR_NARRATION -- merCusId
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 35)); //Bank Code+Branch Code
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 10)); //Tran Code
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 35)); //Host Deal Reference
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 6)); //Purpose Code-1
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 10)); //Purpose Code-2
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 35)); //additional_reference_no_1
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("N", 3)); //Chargers waive (Y/N)
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 35)); //Charge Bearer
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 75)); //CHARGE TYPE
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 19)); //Charge Amount
                                contentYes.append(fieldDelimeter);
                                contentYes.append(validateLength("", 35)); //EXTERNAL_REF
                                contentYes.append(fieldDelimeter);

                                contentYes.append(System.lineSeparator());

                                int i = merchantPaymentFileDao.updatePaymentFileStatus(payIdList);
                                toDeleteStatusSlip = false;
                            }
                            merchantCustomerList.add(merCusId);
                        } catch (Exception ex) {
                            logError.error("Error Occured while writing Merchant Payment File for slips for Merchant Cus No : " + merCusId + ". Exception : ", ex);
                        }
                    }
                }
            }
            txnCount = (txnCount + noOfRecords);
            if (headerCreditBig.compareTo(BigDecimal.ZERO) > 0 || headerCreditCount > 0) {

                sbHeader.append(validateLength(Configurations.BULK_FILE_REFERENCE_MERCH_PAYOUT + sdf6.format(new Date()) + "/" + String.format("%05d", seq), 125)); //BATCH_FILE_REFERENCE
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Configurations.BULK_TYPE_MULTI, 10)); //BULK_TYPE
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(today.toUpperCase(), 11)); //PROCESSING_DATE
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength("LKR", 3)); //Payment Currency
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Configurations.MERCHANT_PAYABLE_GL_SLIPS, 34)); //DR_ACCOUNT -- Configurations.MERCHANT_PAYABLE_GL_SLIPS
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(today.toUpperCase(), 11)); //DR_VALUE_DATE
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength("Settlement on " + sdf6.format(new Date()), 35)); //DR_NARRATION -- mId
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Integer.toString(headerCreditCount), 19)); //NO of Transactions
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateCurrencyLength(headerCreditBig.toString(), 19)); //Total Transactions
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(fileName.concat(".txt"), 35)); //File Name
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Configurations.CHANNEL_ID_PAYOUT_SLIP, 30)); //CHANNEL_ID
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength("-1", 10)); //CHK_SUM
                sbHeader.append(System.lineSeparator());

                buffer.write(sbHeader.toString());
                buffer.write(contentNo.toString());
                buffer.write(contentYes.toString());

                toDeleteStatusSlip = false;
                logInfo.info("Merchant Payment File for Slips created on '" + fileAbsPath + "'. ");
            } else {
                logError.error("Merchant Payment File for Slips is Deleted due to empty line.");

            }

            merchantPaymentFileDao.InsertMerchantFilesIntoDownloadTable(fileName + ".txt", "MERCHANTPAYMENTSLIP");

            EodOuputFileBean eodoutputfilebean = new EodOuputFileBean();
            eodoutputfilebean.setFileName(fileName + ".txt");
            eodoutputfilebean.setSubFolder(Configurations.MERCHANT_PAYMENT_FILE_PATH_SLIPS);
            eodoutputfilebean.setNoOfRecords(txnCount);

            merchantPaymentFileDao.insertOutputFiles(eodoutputfilebean, "MERCHANTPAYMENTSLIP");

            logInfo.info("Merchant Payment File for Slips Process Successfully Completed. ");
        } catch (Exception e) {
            if (file.exists()) {
                file.delete();
            }
            logError.error("Error Occured while writing Merchant Payment File for Slips. Exception in Merchant Payment file process: ", e);
            throw e;
        } finally {
            try {
                if (buffer != null) {
                    try {
                        buffer.flush();
                        buffer.close();
                    } catch (Exception e) {
                        throw e;
                    }
                }
                try {
                    if (toDeleteStatusSlip) {
                        System.out.println(file.getAbsolutePath());
                        file.delete();
                    }
                    if (file.exists()) {
                        FileUtils.copyFile(file, backupFile);
                    }

                } catch (Exception e) {
                    throw e;
                }
            } catch (Exception ee) {
                System.out.println("Error " + ee);
            }
        }
    }
}
