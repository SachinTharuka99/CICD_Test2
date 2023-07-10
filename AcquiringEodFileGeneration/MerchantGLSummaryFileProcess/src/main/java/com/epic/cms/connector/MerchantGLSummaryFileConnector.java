/**
 * Author : sharuka_j
 * Date : 2/1/2023
 * Time : 6:22 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.connector;

import com.epic.cms.common.ProcessBuilder;
import com.epic.cms.dao.MerchantGLSummaryFileDao;
import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.model.EodOuputFileBean;
import com.epic.cms.model.model.GlAccountBean;
import com.epic.cms.model.model.GlBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.MerchantGLSummaryFileService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.epic.cms.util.CommonMethods.*;

/**
 * 1- Consider commission,payment,eodmerchanttxn,eodmerchantfee and update
 * glstatus = 1 2- insert all the txn into eodmerchantglaccount with eodstatus =
 * epen(default value in table) 3- get txn from eodmerchantglaccount and
 * generate the glfile 4- update eodmerchantglaccount eodstatus = edon
 */
@Service
public class MerchantGLSummaryFileConnector extends ProcessBuilder {

    LinkedHashMap accDetails = new LinkedHashMap();
    LinkedHashMap summery = new LinkedHashMap();
    ArrayList<GlAccountBean> list = null;
    ArrayList<Integer> txnId = new ArrayList<Integer>();
    int count;
    int recordCount = 0;
    boolean toDeleteStatus = true;

    @Autowired
    LogManager logManager;
    @Autowired
    CommonRepo commonRepo;

    @Autowired
    MerchantGLSummaryFileDao merchantGLSummaryFileDao;

    @Autowired
    MerchantGLSummaryFileService merchantGLSummaryFileService;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public void concreteProcess() throws Exception {
        processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_MERCHANT_GL_FILE_CREATION);

        if (processBean != null) {
            Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_MERCHANT_GL_FILE_CREATION;
            CommonMethods.eodDashboardProgressParametersReset();
            try {

                /**Create Commission GL file */
                try {
                    list = merchantGLSummaryFileDao.getCommissionDataToEODGL();
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += list.size();
                    accDetails.put("Merchant Gl data for", "Commissions");
                    for (GlAccountBean glaccountBean : list) {
                        merchantGLSummaryFileService.commissionGlFile(glaccountBean);
                    }
                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }
                    accDetails.put("Data Retrieval Status for Commissions", "Passed");
                } catch (Exception e) {
                    accDetails.put("Data Retrieval Status for Commissions", "Failed");
                }
                list.clear();

                /**Create Fee GL file */
                try {
                    list = merchantGLSummaryFileDao.getMerchantFeeDataToEODGL();
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += list.size();
                    accDetails.put("Gl data for", "Merchant Fee Details");
                    for (GlAccountBean glaccountBean : list) {
                        merchantGLSummaryFileService.createFeeGLFile(glaccountBean);
                    }
                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }
                    accDetails.put("Data Retrieval Status for Merchant Fee data", "Passed");
                } catch (Exception e) {
                    accDetails.put("Data Retrieval Status for Merchant Fee data", "Failed");
                }
//                logLevel3.info(LgLvls.processDetailsStyles(accDetails));
//                accDetails.clear();
                list.clear();

                /**Create EODMERCHANTTXN table GL file */
                try {
                    list = merchantGLSummaryFileDao.getEODMerchantTxnDataToGL();
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += list.size();
                    accDetails.put("Gl data for", "EOD Merchant Transaction");
                    for (GlAccountBean glaccountBean : list) {
                        merchantGLSummaryFileService.createEODMerchantTxnTableGLFile(glaccountBean);
                    }
                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }
                    accDetails.put("Data Retrieval Status for EOD Merchant Transaction Data", "Passed");
                } catch (Exception e) {
                    accDetails.put("Data Retrieval Status for EOD Merchant Transaction Data", "Failed");
                }
//                logLevel3.info(LgLvls.processDetailsStyles(accDetails));
                accDetails.clear();
                list.clear();

                /**Create MERCHANTPAYMENT table GL file */
                try {
                    list = merchantGLSummaryFileDao.getEODMerchantPaymentDataToGL();
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += list.size();
                    accDetails.put("Gl data for", "EOD Merchant payment");
                    for (GlAccountBean glaccountBean : list) {
                        merchantGLSummaryFileService.createMerchantPaymentTableGLFile(glaccountBean);
                    }
                    while (!(taskExecutor.getActiveCount() == 0)) {
                        Thread.sleep(1000);
                    }
                    accDetails.put("Data Retrieval Status for EOD Merchant payment Data", "Passed");
                } catch (Exception e) {
                    accDetails.put("Data Retrieval Status for EOD Merchant payment Data", "Failed");
                }
//                logLevel3.info(LgLvls.processDetailsStyles(accDetails));
//                WebComHandler.showOnWeb(CommonMethods.eodDashboardDetailsStyle(accDetails));
//                accDetails.clear();
                list.clear();

                accDetails.clear();

                //two places need to be edit in file name
                SimpleDateFormat sd = new SimpleDateFormat("yyyy");
                String year = sd.format(new Date());
                String today1 = String.valueOf(year) + Integer.toString(Configurations.EOD_ID).substring(2, 6);

                String eodSeq = Integer.toString(Configurations.ERROR_EOD_ID).substring(6);
                int seq = Integer.parseInt(eodSeq) + 1;
                String sequence = String.format("%03d", seq);
                String fileName = Configurations.MERCHANT_GL_SUMMARY_FILE_PREFIX + today1 + "." + sequence;
                if (createGLFile()) {
                    for (int key : txnId) {
                        count = merchantGLSummaryFileDao.updateEodMerchantGLAccount(key);
                    }
                    merchantGLSummaryFileDao.InsertMerchantFilesIntoDownloadTable(fileName.concat(".txt"), "MERCHANTGL");

                    EodOuputFileBean eodoutputfilebean = new EodOuputFileBean();
                    eodoutputfilebean.setFileName(fileName.concat(".txt"));
                    eodoutputfilebean.setNoOfRecords(this.recordCount);

                    merchantGLSummaryFileDao.insertOutputFiles(eodoutputfilebean, "MERCHANTGL");
                } else {
                    logInfo.info("Merchant GL File doesn't created. ");
                }
            } catch (Exception e) {
                Configurations.IS_PROCESS_COMPLETELY_FAILED = true;
                logError.error("Exception in Merchant GL File Process", e);
            }
        }
    }

    @Override
    public void addSummaries() {

    }

    public Boolean createGLFile() throws Exception {
        HashMap<String, ArrayList<GlAccountBean>> hmap;
        HashMap<String, GlBean> glAccntsDetail;
        HashMap<String, String[]> gltypes;
        HashMap<String, String[]> gltxnTypes;
        GlBean bean;
        File file = null, backupFile = null;
        int sequenceNo = 01;
        FileWriter writer = null;
        BufferedWriter buffer = null;
        Boolean status = false;
        Boolean fileStatus = true;
        int noofRecords = 0;
        int noofBatches = 0;
        String branchCode = Configurations.GL_BRANCH_CODE;
        String txnDes;
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat sd = new SimpleDateFormat("yyyy");
        String year = sd.format(new Date());
        String today1 = String.valueOf(year) + Integer.toString(Configurations.EOD_ID).substring(2, 6);

        String eodSeq = Integer.toString(Configurations.ERROR_EOD_ID).substring(6);
        int seq = Integer.parseInt(eodSeq) + 1;
        String sequence = String.format("%03d", seq);
        String fieldDelimeter = Configurations.OUTPUTFILE_FIELD_DELIMETER;
        String fileName = Configurations.MERCHANT_GL_SUMMARY_FILE_PREFIX + today1 + "." + sequence;
        try {
            file = new File(Configurations.MERCHANT_GLFILE_FILE_PATH);
            backupFile = new File(Configurations.MERCHANT_GLFILE_FILE_PATH + "BACKUP");

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

            file = new File(Configurations.MERCHANT_GLFILE_FILE_PATH + fileName + ".txt");
            backupFile = new File(Configurations.MERCHANT_GLFILE_FILE_PATH + "BACKUP" + File.separator + fileName + ".txt"); //.dat

            writer = new FileWriter(file, true);
            buffer = new BufferedWriter(writer);
            //get the gl entries for the gl file
            hmap = merchantGLSummaryFileDao.getDataFromEODMERCHANTGl();
            //get active gl account details from GLACCOUNT table
            glAccntsDetail = merchantGLSummaryFileDao.getGLAccData();
            //get gltxn type
            gltxnTypes = merchantGLSummaryFileDao.getGLTxnTypes();
            //get credit acc and debit acc from GLTRANSACTION according to gltxn type
            gltypes = merchantGLSummaryFileDao.getMerchantGLTypesData();

            StringBuilder sbHeader = new StringBuilder();
            StringBuilder sbContent = new StringBuilder();
            BigDecimal headerCreditBig = new BigDecimal(0.0);
            BigDecimal headerDebitBig = new BigDecimal(0.0);
            int headerCreditCount = 0;
            int headerDebitCount = 0;

            for (Map.Entry<String, ArrayList<GlAccountBean>> entrySet : hmap.entrySet()) {
                try {

                    BigDecimal crAmountBig = new BigDecimal(0.0);
                    BigDecimal drAmountBig = new BigDecimal(0.0);
                    BigDecimal amountBig = new BigDecimal(0.0);

                    Date nextWorkingDay = merchantGLSummaryFileDao.getNextWorkingDay(new Date());
                    String today = sdf2.format(nextWorkingDay);
                    int count = 0;
                    boolean crStatus = false;
                    String key = entrySet.getKey();// GLTYPE
                    ArrayList<GlAccountBean> value = entrySet.getValue();//GLBEAN
                    String[] glTypeData = gltypes.get(key);//gltypes -> get credit acc and debit acc from GLTRANSACTION according to gltxn type ([0] - creditaccount [1] - debit account)
                    String[] glTxn = gltxnTypes.get(key);
                    String crDROnBank = merchantGLSummaryFileDao.getCRDRFromGlTxn(key);

                    if (glTypeData != null) {
                        for (GlAccountBean glAccountBean : value) {
                            noofRecords++;

                            if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.CREDIT)) {
                                count++;
                                BigDecimal glAmountBig = new BigDecimal(glAccountBean.getGlAmount());
                                crAmountBig = crAmountBig.add(glAmountBig);

                            } else if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.DEBIT)) {
                                count++;
                                BigDecimal glAmountBig = new BigDecimal(glAccountBean.getGlAmount());
                                drAmountBig = drAmountBig.add(glAmountBig);

                            } else {
//                                logLevel2.info("Error in CRDR type in EODMERCHANTGLACCOUNT table");
//                                WebComHandler.showOnWeb(CommonMethods.eodDashboardProcessInfoStyle("Error in CRDR type in EODMERCHANTGLACCOUNT table"));
                            }
                            txnId.add(glAccountBean.getId());
                        }

                        if (crAmountBig.compareTo(drAmountBig) == 1) {
                            amountBig = crAmountBig.subtract(drAmountBig).setScale(2, RoundingMode.DOWN);
                            crStatus = true;
                        } else if (drAmountBig.compareTo(crAmountBig) == 1) {
                            amountBig = drAmountBig.subtract(crAmountBig).setScale(2, RoundingMode.DOWN);
                            crStatus = false;
                        }

                        if (amountBig.compareTo(BigDecimal.ZERO) > 0) {
                            noofBatches++;
                            String seqNo = "99" + Integer.toString(Configurations.EOD_ID) + validate(Integer.toString(noofBatches), 6, '0');
                            if (crStatus) {
                                count = 0;
                                count++;
                                if (crDROnBank.equalsIgnoreCase("CR")) {

                                    String accountCode = glTypeData[0];
                                    bean = glAccntsDetail.get(accountCode);

                                    sbContent.append(validateLength(seqNo, 20)); //SEQ_NO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(accountCode, 16)); //ACCOUNT_GL_PL
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("C", 1)); //SIGN

                                    headerCreditBig = headerCreditBig.add(amountBig);
                                    headerCreditCount++;

                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[1], 2)); //POSITION_TYPE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[0], 34)); //NARRATION
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 25)); //EXTERNAL_REF
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_01
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_02
                                    sbContent.append(System.lineSeparator());

                                    accountCode = glTypeData[1];
                                    bean = glAccntsDetail.get(accountCode);

                                    sbContent.append(validateLength(seqNo, 20)); //SEQ_NO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(accountCode, 16)); //ACCOUNT_GL_PL
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("D", 1)); //SIGN

                                    headerDebitBig = headerDebitBig.add(amountBig);
                                    headerDebitCount++;

                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[1], 2)); //POSITION_TYPE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[0], 34)); //NARRATION
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 25)); //EXTERNAL_REF
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_01
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_02
                                    sbContent.append(System.lineSeparator());

                                } else {
                                    String accountCode = glTypeData[1];
                                    bean = glAccntsDetail.get(accountCode);

                                    sbContent.append(validateLength(seqNo, 20)); //SEQ_NO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(accountCode, 16)); //ACCOUNT_GL_PL
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("C", 1)); //SIGN

                                    headerCreditBig = headerCreditBig.add(amountBig);
                                    headerCreditCount++;

                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[1], 2)); //POSITION_TYPE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[0], 34)); //NARRATION
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 25)); //EXTERNAL_REF
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_01
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_02
                                    sbContent.append(System.lineSeparator());

                                    accountCode = glTypeData[0];
                                    bean = glAccntsDetail.get(accountCode);

                                    sbContent.append(validateLength(seqNo, 20)); //SEQ_NO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(accountCode, 16)); //ACCOUNT_GL_PL
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("D", 1)); //SIGN

                                    headerDebitBig = headerDebitBig.add(amountBig);
                                    headerDebitCount++;

                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[1], 2)); //POSITION_TYPE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[0], 34)); //NARRATION
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 25)); //EXTERNAL_REF
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_01
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_02
                                    sbContent.append(System.lineSeparator());
                                }

                            } else {
                                count = 0;
                                count++;
                                if (crDROnBank.equalsIgnoreCase("DR")) {
                                    String accountCode = glTypeData[0];
                                    bean = glAccntsDetail.get(accountCode);

                                    sbContent.append(validateLength(seqNo, 20)); //SEQ_NO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(accountCode, 16)); //ACCOUNT_GL_PL
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("C", 1)); //SIGN

                                    headerCreditBig = headerCreditBig.add(amountBig);
                                    headerCreditCount++;

                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[1], 2)); //POSITION_TYPE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[0], 34)); //NARRATION
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 25)); //EXTERNAL_REF
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_01
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_02
                                    sbContent.append(System.lineSeparator());

                                    accountCode = glTypeData[1];
                                    bean = glAccntsDetail.get(accountCode);

                                    sbContent.append(validateLength(seqNo, 20)); //SEQ_NO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(accountCode, 16)); //ACCOUNT_GL_PL
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("D", 1)); //SIGN

                                    headerDebitBig = headerDebitBig.add(amountBig);
                                    headerDebitCount++;

                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[1], 2)); //POSITION_TYPE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[0], 34)); //NARRATION
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 25)); //EXTERNAL_REF
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_01
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_02
                                    sbContent.append(System.lineSeparator());
                                } else {
                                    String accountCode = glTypeData[1];
                                    bean = glAccntsDetail.get(accountCode);

                                    sbContent.append(validateLength(seqNo, 20)); //SEQ_NO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(accountCode, 16)); //ACCOUNT_GL_PL
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("C", 1)); //SIGN

                                    headerCreditBig = headerCreditBig.add(amountBig);
                                    headerCreditCount++;

                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[1], 2)); //POSITION_TYPE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[0], 34)); //NARRATION
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 25)); //EXTERNAL_REF
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_01
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_02
                                    sbContent.append(System.lineSeparator());

                                    accountCode = glTypeData[0];
                                    bean = glAccntsDetail.get(accountCode);

                                    sbContent.append(validateLength(seqNo, 20)); //SEQ_NO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(accountCode, 16)); //ACCOUNT_GL_PL
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("D", 1)); //SIGN

                                    headerDebitBig = headerDebitBig.add(amountBig);
                                    headerDebitCount++;

                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[1], 2)); //POSITION_TYPE
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(glTxn[0], 34)); //NARRATION
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 25)); //EXTERNAL_REF
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_01
                                    sbContent.append(fieldDelimeter);
                                    sbContent.append(validateLength("", 15)); //RESERVED_02
                                    sbContent.append(System.lineSeparator());
                                }
                            }
                        }

                    } else {
//                        logLevel2.info("Merchant GLtype Data Not Configured in the system for Transaction type " + key);
//                        WebComHandler.showOnWeb(CommonMethods.eodDashboardProcessInfoStyle("Merchant GLtype Data Not Configured in the system for Transaction type " + key));
                        for (GlAccountBean glAccountBean : value) {
                            txnId.add(glAccountBean.getId());
                        }
                    }

                } catch (Exception e) {
                    status = false;
                    if (file.exists()) {
                        fileStatus = file.delete();
                    }
//                    errorLog.info("Error while writing gl file.Exit from the process. Exception in GL txn Type " + entrySet.getKey() + "--->" + e);
//                    WebComHandler.showOnWeb(CommonMethods.eodDashboardProcessInfoStyle("Error while writing gl file.Exit from the process. Exception in GL txn Type " + entrySet.getKey()));
                    throw e;
                }
                status = true;
            }
            recordCount = noofRecords;

            if (headerCreditBig.compareTo(BigDecimal.ZERO) > 0 || headerCreditCount > 0) {
                sbHeader.append(fileName.concat(".txt")); //FILE_NAME
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Integer.toString(headerDebitCount), 4)); //NO_OF_DR
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateCurrencyLength(headerDebitBig.toString(), 30)); //DR_TOT_VAL_LCY
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength(Integer.toString(headerCreditCount), 4)); //NO_OF_CR
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateCurrencyLength(headerCreditBig.toString(), 30)); //CR_TOT_VAL_LCY
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength("Y", 1)); //BATCH_REJ
                sbHeader.append(fieldDelimeter);
                sbHeader.append(validateLength("-1", 50)); //CHK_SUM
                sbHeader.append(System.lineSeparator());

                buffer.write(sbHeader.toString());
                buffer.write(sbContent.toString());
                toDeleteStatus = false;
            }

            summery.put("Process Name", processBean.getProcessDes());
            summery.put("File Name", fileName);
            summery.put("No of records ", Configurations.PROCESS_SUCCESS_COUNT);
            summery.put("Created Date ", Configurations.EOD_DATE.toString());
            logInfo.info(logManager.logSummery(summery));
        } catch (Exception e) {
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
                    if (toDeleteStatus) {
                        if (file.exists()) {
                            file.delete();
                        }
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
        return status;
    }

    private double roundDouble(double amount) {

        double number2 = amount;

        number2 = Math.round(number2 * 100);
        number2 = number2 / 100;

        return number2;
    }
}
