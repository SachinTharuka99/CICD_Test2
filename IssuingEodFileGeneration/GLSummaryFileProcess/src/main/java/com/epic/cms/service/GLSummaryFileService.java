/**
 * Author : lahiru_p
 * Date : 11/30/2022
 * Time : 8:37 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.FileGenerationModel;
import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.model.bean.GlBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonFileGenProcessRepo;
import com.epic.cms.repository.GLSummaryFileRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.epic.cms.util.CommonMethods.*;
import static com.epic.cms.util.LogManager.*;

@Service
public class GLSummaryFileService {

    @Autowired
    LogManager logManager;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    FileGenerationService fileGenerationService;

    @Autowired
    CommonFileGenProcessRepo commonFileGenProcessRepo;

    @Autowired
    GLSummaryFileRepo glSummaryFileRepo;

    ArrayList<Integer> txnId = new ArrayList<>();

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public FileGenerationModel createGLFile(String fileName, String filePath, String backUpFilePath, ProcessBean processBean) throws Exception {
        LinkedHashMap summery = new LinkedHashMap();
        boolean toDeleteStatus = true;
        FileGenerationModel model = new FileGenerationModel();
        HashMap<String, ArrayList<GlAccountBean>> hmap;
        HashMap<String, GlBean> glAccntsDetail;
        HashMap<String, String[]> gltypes;
        HashMap<String, String[]> gltxnTypes;
        GlBean bean;
        String fieldDelimeter = Configurations.OUTPUTFILE_FIELD_DELIMETER;
        Boolean status = false;
        int noofRecords = 0;
        int noofBatches = 0;

        try {
            /**get the gl entries for the gl file*/
            hmap = glSummaryFileRepo.getDataFromEODGl();
            /**get active gl account details from GLACCOUNT table*/
            glAccntsDetail = commonFileGenProcessRepo.getGLAccData();
            /**get credit acc and debit acc from GLTRANSACTION according to gltxn type*/
            gltypes = glSummaryFileRepo.getGLTypesData();
            /**get gltxn type*/
            gltxnTypes = commonFileGenProcessRepo.getGLTxnTypes();

            StringBuilder sbHeader = new StringBuilder();
            StringBuilder sbContent = new StringBuilder();
            BigDecimal headerCreditBig = BigDecimal.valueOf(0.0);
            BigDecimal headerDebitBig = BigDecimal.valueOf(0.0);
            int headerCreditCount = 0;
            int headerDebitCount = 0;

            for (Map.Entry<String, ArrayList<GlAccountBean>> entrySet : hmap.entrySet()) {
                try {
                    ArrayList<Integer> glIdList = new ArrayList<Integer>();
                    //Date nextWorkingDay = commonFileGenProcessRepo.getNextWorkingDay(new Date());
                    int count = 0;
                    boolean crStatus = false;
                    LinkedHashMap detailsGlSummary = new LinkedHashMap();
                    BigDecimal crAmountBig = BigDecimal.valueOf(0.0);
                    BigDecimal drAmountBig = BigDecimal.valueOf(0.0);
                    BigDecimal amountBig = BigDecimal.valueOf(0.0);
                    String key = entrySet.getKey();
                    ArrayList<GlAccountBean> value = entrySet.getValue();
                    String[] glTypeData = gltypes.get(key);
                    String[] glTxn = gltxnTypes.get(key);
                    String crDROnBank = commonFileGenProcessRepo.getCRDRFromGlTxn(key);
                    if (key.equalsIgnoreCase(Configurations.TXN_TYPE_UNEARNED_INCOME_UPFRONT_FALSE)) {
                        noofBatches++;
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
                                logManager.logInfo("Error in CRDR type in EODGLACCOUNT table", infoLoggerEFGE);
                            }
                            glIdList.add(glAccountBean.getId());
                        }

                        if (crAmountBig.compareTo(drAmountBig) == 1) {
                            amountBig = crAmountBig.subtract(drAmountBig).setScale(2, RoundingMode.DOWN);
                            crStatus = true;
                        } else if (drAmountBig.compareTo(crAmountBig) == 1) {
                            amountBig = drAmountBig.subtract(crAmountBig).setScale(2, RoundingMode.DOWN);
                            crStatus = false;
                        }

                        String seqNo = "99" + Configurations.EOD_ID + validate(Integer.toString(noofBatches), 6, '0');

                        if (crStatus) {
                            count = 0;
                            count++;

                            String accountCode = Configurations.UNEARNED_INCOME_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.UNEARNED_INCOME_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.UNEARNED_INCOME, 34)); //NARRATION
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

                            accountCode = Configurations.INSTALLMENT_LOAN_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.UNEARNED_INCOME_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.UNEARNED_INCOME, 34)); //NARRATION
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
                            count = 0;
                            count++;

                            String accountCode = Configurations.INSTALLMENT_LOAN_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.UNEARNED_INCOME_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.UNEARNED_INCOME, 34)); //NARRATION
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

                            accountCode = Configurations.UNEARNED_INCOME_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.UNEARNED_INCOME_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.UNEARNED_INCOME, 34)); //NARRATION
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
                        txnId.addAll(glIdList);
                    } else if (key.equalsIgnoreCase(Configurations.TXN_TYPE_FEE_INSTALLMENT_UPFRONT_FALSE)) {
                        noofBatches++;
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
                                logManager.logInfo("Error in CRDR type in EODGLACCOUNT table", infoLoggerEFGE);
                            }
                            glIdList.add(glAccountBean.getId());
                        }

                        if (crAmountBig.compareTo(drAmountBig) == 1) {
                            amountBig = crAmountBig.subtract(drAmountBig).setScale(2, RoundingMode.DOWN);
                            crStatus = true;
                        } else if (drAmountBig.compareTo(crAmountBig) == 1) {
                            amountBig = drAmountBig.subtract(crAmountBig).setScale(2, RoundingMode.DOWN);
                            crStatus = false;
                        }

                        String seqNo = "99" + Configurations.EOD_ID + validate(Integer.toString(noofBatches), 6, '0');

                        if (!crStatus) {
                            count = 0;
                            count++;

                            String accountCode = Configurations.INSTALLMENT_LOAN_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.FEE_INSTALLMENT_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.INSTALLMENT_FEE, 34)); //NARRATION
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

                            accountCode = Configurations.PERPORMING_LOAN_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.FEE_INSTALLMENT_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.INSTALLMENT_FEE, 34)); //NARRATION
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

                            count++;

                            accountCode = Configurations.FEE_RECOVER_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.FEE_INSTALLMENT_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.INSTALLMENT_FEE, 34)); //NARRATION
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

                            accountCode = Configurations.UNEARNED_INCOME_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.FEE_INSTALLMENT_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.INSTALLMENT_FEE, 34)); //NARRATION
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
                            count = 0;
                            count++;

                            String accountCode = Configurations.PERPORMING_LOAN_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.FEE_INSTALLMENT_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.INSTALLMENT_FEE, 34)); //NARRATION
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

                            accountCode = Configurations.INSTALLMENT_LOAN_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.FEE_INSTALLMENT_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.INSTALLMENT_FEE, 34)); //NARRATION
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

                            count++;

                            accountCode = Configurations.UNEARNED_INCOME_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.FEE_INSTALLMENT_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.INSTALLMENT_FEE, 34)); //NARRATION
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

                            accountCode = Configurations.FEE_RECOVER_GL;
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
                            sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.FEE_INSTALLMENT_UPFRON_FALSE_POSITION, 2)); //POSITION_TYPE
                            sbContent.append(fieldDelimeter);
                            sbContent.append(validateLength(Configurations.INSTALLMENT_FEE, 34)); //NARRATION
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
                        txnId.addAll(glIdList);
                    } else {
                        if (glTypeData != null) {
                            noofBatches++;
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
                                    logManager.logInfo("Error in CRDR type in EODGLACCOUNT table", infoLoggerEFGE);
                                }
                                glIdList.add(glAccountBean.getId());
                            }

                            if (crAmountBig.compareTo(drAmountBig) == 1) {
                                amountBig = crAmountBig.subtract(drAmountBig).setScale(2, RoundingMode.DOWN);
                                crStatus = true;
                            } else if (drAmountBig.compareTo(crAmountBig) == 1) {
                                amountBig = drAmountBig.subtract(crAmountBig).setScale(2, RoundingMode.DOWN);
                                crStatus = false;
                            }

                            String seqNo = "99" + Configurations.EOD_ID + validate(Integer.toString(noofBatches), 6, '0');

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
                                    sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
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
                                    sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
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
                                    sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
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
                                    sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
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
                                    sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
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
                                    sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
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
                                    sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
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
                                    sbContent.append(validateLength(bean.getCurrencyCode(), 3)); //CURRENCY
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
                        } else {
                            logManager.logInfo("GLtype Data Not Configured in the system for Transaction type " + key, infoLogger);
                            for (GlAccountBean glAccountBean : value) {
                                glIdList.add(glAccountBean.getId());
                            }
                        }
                        txnId.addAll(glIdList);
                    }

                    detailsGlSummary.put("GL type", key);
                    detailsGlSummary.put("CRDR", crStatus);
                    detailsGlSummary.put("GLTXN CRDR", crDROnBank);
                    detailsGlSummary.put("GL Amount", amountBig.toString());
                    logManager.logDetails(detailsGlSummary, infoLoggerEFGE);

                } catch (Exception e) {
                    status = false;
                    logManager.logError("Error while writing gl file.Exit from the process. Exception in GL txn Type " + entrySet.getKey() + "--->" + e, errorLoggerEFGE);
                    throw e;
                }
                status = true;
            }

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

                model.setTxnIdList(txnId);
                model.setFileContent(sbContent);
                model.setFileHeader(sbHeader);
                model.setFinalFile(sbHeader.append(sbContent));
                model.setStatus(status);
                model.setDeleteStatus(false);

                /**generate a file*/
                fileGenerationService.generateFile(model.getFinalFile().toString(), filePath, backUpFilePath);
                toDeleteStatus = false;
            } else {
                logManager.logInfo("Empty line in body. Hence No header section.", infoLoggerEFGE);
                logManager.logError("Empty line in body. Hence No header section.", errorLoggerEFGE);
            }

            summery.put("Process Name", processBean.getProcessDes());
            summery.put("File Name", fileName);
            summery.put("File Path", Configurations.GLFILE_FILE_PATH);
            summery.put("No of records ", noofRecords);
            summery.put("Header CR Amount", headerCreditBig.toString());
            summery.put("Header CR Count", Integer.toString(headerCreditCount));
            summery.put("Created Date ", Configurations.EOD_DATE.toString());

            logManager.logSummery(summery, infoLoggerEFGE);

        } catch (Exception e) {
            logManager.logError("GL File Process Failed. ", e, errorLoggerEFGE);
            throw e;
        } finally {
            try {
                if (toDeleteStatus) {
                    fileGenerationService.deleteExistFile(filePath);
                }
            } catch (Exception ee) {
                System.out.println("Error " + ee);
            }
        }
        return model;
    }
}
