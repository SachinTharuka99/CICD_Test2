/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 10:27 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.FileGenerationModel;
import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.model.bean.GlBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.epic.cms.util.CommonMethods.validateCurrencyLength;
import static com.epic.cms.util.CommonMethods.validateLength;

@Service
public class RB36FileGenerationService {

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");


    public FileGenerationModel getFileContent(ArrayList<StringBuffer> npCards, HashMap<String, ArrayList<GlAccountBean>> hmap, HashMap<String, GlBean> glAccountsDetail, String fieldDelimeter) throws Exception{
        FileGenerationModel file = new FileGenerationModel();
        GlBean bean;
        ArrayList<Integer> txnId = new ArrayList<>();
        BigDecimal headerCreditBig = BigDecimal.valueOf(0.0);
        BigDecimal headerDebitBig = BigDecimal.valueOf(0.0);
        int headerCreditCount = 0;
        int headerDebitCount = 0;

        SimpleDateFormat sdf3 = new SimpleDateFormat("dd-MMM-yy");
        String today2 = sdf3.format(new Date());

        StringBuilder detailSection = new StringBuilder();
        int noofBatches = 0;
        for (Map.Entry<String, ArrayList<GlAccountBean>> entrySet : hmap.entrySet()) {
            try {
                noofBatches++;
                int count = 0;

                boolean crStatus = false;
                boolean cashStatus = true;
                BigDecimal crAmountBig = BigDecimal.valueOf(0.0);
                BigDecimal drAmountBig = BigDecimal.valueOf(0.0);
                BigDecimal crAmountNpBig = BigDecimal.valueOf(0.0);
                BigDecimal drAmountNpBig = BigDecimal.valueOf(0.0);
                BigDecimal amountBig = BigDecimal.valueOf(0.0);
                BigDecimal amountNpBig = BigDecimal.valueOf(0.0);
                StringBuilder content = new StringBuilder();
                ArrayList<GlAccountBean> value = entrySet.getValue();

                for (GlAccountBean glAccountBean : value) {
                    count++;
                    if (glAccountBean.getPaymentType().equalsIgnoreCase(statusVarList.getCHEQUE_PAYMENT())) {
                        cashStatus = false;
                        if (npCards.toString().contains(glAccountBean.getCardNo().toString())) {
                            if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.CREDIT)) {
                                BigDecimal glAmountNpBig = new BigDecimal(glAccountBean.getGlAmount());
                                crAmountNpBig = crAmountNpBig.add(glAmountNpBig);
                            } else if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.DEBIT)) {
                                BigDecimal glAmountNpBig = new BigDecimal(glAccountBean.getGlAmount());
                                drAmountNpBig = drAmountNpBig.add(glAmountNpBig);
                            }
                        } else {
                            if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.CREDIT)) {
                                BigDecimal glAmountBig = new BigDecimal(glAccountBean.getGlAmount());
                                crAmountBig = crAmountBig.add(glAmountBig);
                            } else if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.DEBIT)) {
                                BigDecimal glAmountBig = new BigDecimal(glAccountBean.getGlAmount());
                                drAmountBig = drAmountBig.add(glAmountBig);
                            }
                        }

                    } else {
                        cashStatus = true;
                        if (npCards.toString().contains(glAccountBean.getCardNo().toString())) {
                            if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.CREDIT)) {
                                BigDecimal glAmountNpBig = new BigDecimal(glAccountBean.getGlAmount());
                                crAmountNpBig = crAmountNpBig.add(glAmountNpBig);
                            } else if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.DEBIT)) {
                                BigDecimal glAmountNpBig = new BigDecimal(glAccountBean.getGlAmount());
                                drAmountNpBig = drAmountNpBig.add(glAmountNpBig);
                            }
                        } else {
                            if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.CREDIT)) {
                                BigDecimal glAmountBig = new BigDecimal(glAccountBean.getGlAmount());
                                crAmountBig = crAmountBig.add(glAmountBig);
                            } else if (glAccountBean.getCrDr().equalsIgnoreCase(Configurations.DEBIT)) {
                                BigDecimal glAmountBig = new BigDecimal(glAccountBean.getGlAmount());
                                drAmountBig = drAmountBig.add(glAmountBig);
                            }
                        }

                    }
                    txnId.add(glAccountBean.getId());
                }

                //Performing
                if (crAmountBig.compareTo(drAmountBig) == 1) {
                    amountBig = crAmountBig.subtract(drAmountBig).setScale(2, RoundingMode.DOWN);
                    crStatus = true;
                } else if (drAmountBig.compareTo(crAmountBig) == 1) {
                    amountBig = drAmountBig.subtract(crAmountBig).setScale(2, RoundingMode.DOWN);
                    crStatus = false;
                }

                String seqNo = "99" + Integer.toString(Configurations.EOD_ID) + CommonMethods.validate(Integer.toString(noofBatches), 6, '0');

                String creditAccountCode;
                String debitAccountCode;
                String narration = null;

                if (amountBig.compareTo(BigDecimal.ZERO) > 0) {
                    if (cashStatus) {
                        if (crStatus) {
                            creditAccountCode = Configurations.PERFORM_LOAN_RB36;
                            debitAccountCode = Configurations.CASH_ACCOUNT_RB36;
                            narration = "CASH PMT RECV PER ";
                        } else {
                            creditAccountCode = Configurations.CASH_ACCOUNT_RB36;
                            debitAccountCode = Configurations.PERFORM_LOAN_RB36;
                            narration = "CASH PMT RETN PER ";
                        }

                    } else {
                        if (crStatus) {
                            creditAccountCode = Configurations.PERFORM_LOAN_RB36;
                            debitAccountCode = Configurations.CHEQUE_ACCOUNT_RB36;
                            narration = "CHEQUE PMT RECV PER ";
                        } else {
                            creditAccountCode = Configurations.CHEQUE_ACCOUNT_RB36;
                            debitAccountCode = Configurations.PERFORM_LOAN_RB36;
                            narration = "CHEQUE PMT RETN PER ";
                        }
                    }

                    bean = glAccountsDetail.get(debitAccountCode);

                    content.append(validateLength(seqNo, 20)); //SEQ_NO
                    content.append(fieldDelimeter);
                    content.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                    content.append(fieldDelimeter);
                    content.append(validateLength(debitAccountCode, 16)); //ACCOUNT_GL_PL
                    content.append(fieldDelimeter);
                    content.append(validateLength("D", 1)); //SIGN

                    headerDebitBig = headerDebitBig.add(amountBig);
                    headerDebitCount++;

                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                    content.append(fieldDelimeter);
                    content.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                    content.append(fieldDelimeter);
                    content.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                    content.append(fieldDelimeter);
                    content.append(validateLength("TR", 2)); //POSITION_TYPE
                    content.append(fieldDelimeter);
                    content.append(validateLength(narration.concat(today2.toUpperCase()), 34)); //NARRATION
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 25)); //EXTERNAL_REF
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 15)); //RESERVED_01
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 15)); //RESERVED_02
                    content.append(System.lineSeparator());

                    bean = glAccountsDetail.get(creditAccountCode);

                    content.append(validateLength(seqNo, 20)); //SEQ_NO
                    content.append(fieldDelimeter);
                    content.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                    content.append(fieldDelimeter);
                    content.append(validateLength(creditAccountCode, 16)); //ACCOUNT_GL_PL
                    content.append(fieldDelimeter);
                    content.append(validateLength("C", 1)); //SIGN

                    headerCreditBig = headerCreditBig.add(amountBig);
                    headerCreditCount++;

                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                    content.append(fieldDelimeter);
                    content.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT
                    content.append(fieldDelimeter);
                    content.append(validateCurrencyLength(amountBig.toString(), 19)); //AMOUNT_LCY
                    content.append(fieldDelimeter);
                    content.append(validateLength("TR", 2)); //POSITION_TYPE
                    content.append(fieldDelimeter);
                    content.append(validateLength(narration.concat(today2.toUpperCase()), 34)); //NARRATION
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 25)); //EXTERNAL_REF
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 15)); //RESERVED_01
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 15)); //RESERVED_02
                    content.append(System.lineSeparator());

                    detailSection = detailSection.append(content);
                }

                content = new StringBuilder();

                //Non performing
                if (crAmountNpBig.compareTo(drAmountNpBig) == 1) {
                    amountNpBig = crAmountNpBig.subtract(drAmountNpBig).setScale(2, RoundingMode.DOWN);
                    crStatus = true;
                } else if (drAmountNpBig.compareTo(crAmountNpBig) == 1) {
                    amountNpBig = drAmountNpBig.subtract(crAmountNpBig).setScale(2, RoundingMode.DOWN);
                    crStatus = false;
                }
                //amountNpBig = new BigDecimal("1.0");
                if (amountNpBig.compareTo(BigDecimal.ZERO) > 0) {
                    if (cashStatus) {
                        if (crStatus) {
                            creditAccountCode = Configurations.NON_PERFORM_LOAN_RB36;
                            debitAccountCode = Configurations.CASH_ACCOUNT_RB36;
                            narration = "CASH PMT RECV PER ";
                        } else {
                            creditAccountCode = Configurations.CASH_ACCOUNT_RB36;
                            debitAccountCode = Configurations.NON_PERFORM_LOAN_RB36;
                            narration = "CASH PMT RETN PER ";
                        }

                    } else {
                        if (crStatus) {
                            creditAccountCode = Configurations.NON_PERFORM_LOAN_RB36;
                            debitAccountCode = Configurations.CHEQUE_ACCOUNT_RB36;
                            narration = "CHEQUE PMT RECV PER ";
                        } else {
                            creditAccountCode = Configurations.CHEQUE_ACCOUNT_RB36;
                            debitAccountCode = Configurations.NON_PERFORM_LOAN_RB36;
                            narration = "CHEQUE PMT RETN PER ";
                        }
                    }

                    bean = glAccountsDetail.get(debitAccountCode);//"LKR1706200010800"

                    content.append(validateLength(seqNo, 20)); //SEQ_NO
                    content.append(fieldDelimeter);
                    content.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                    content.append(fieldDelimeter);
                    content.append(validateLength(debitAccountCode, 16)); //ACCOUNT_GL_PL
                    content.append(fieldDelimeter);
                    content.append(validateLength("D", 1)); //SIGN

                    headerDebitBig = headerDebitBig.add(amountNpBig);
                    headerDebitCount++;

                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                    content.append(fieldDelimeter);
                    content.append(validateCurrencyLength(amountNpBig.toString(), 19)); //AMOUNT
                    content.append(fieldDelimeter);
                    content.append(validateCurrencyLength(amountNpBig.toString(), 19)); //AMOUNT_LCY
                    content.append(fieldDelimeter);
                    content.append(validateLength("TR", 2)); //POSITION_TYPE
                    content.append(fieldDelimeter);
                    content.append(validateLength(narration.concat(today2.toUpperCase()), 34)); //NARRATION
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 25)); //EXTERNAL_REF
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 15)); //RESERVED_01
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 15)); //RESERVED_02
                    content.append(System.lineSeparator());

                    bean = glAccountsDetail.get(creditAccountCode);//"LKR1706200010800"

                    content.append(validateLength(seqNo, 20)); //SEQ_NO
                    content.append(fieldDelimeter);
                    content.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 10)); //PROD_CODE
                    content.append(fieldDelimeter);
                    content.append(validateLength(creditAccountCode, 16)); //ACCOUNT_GL_PL
                    content.append(fieldDelimeter);
                    content.append(validateLength("C", 1)); //SIGN

                    headerCreditBig = headerCreditBig.add(amountNpBig);
                    headerCreditCount++;

                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getCurrencyCode().toString(), 3)); //CURRENCY
                    content.append(fieldDelimeter);
                    content.append(validateCurrencyLength(amountNpBig.toString(), 19)); //AMOUNT
                    content.append(fieldDelimeter);
                    content.append(validateCurrencyLength(amountNpBig.toString(), 19)); //AMOUNT_LCY
                    content.append(fieldDelimeter);
                    content.append(validateLength("TR", 2)); //POSITION_TYPE
                    content.append(fieldDelimeter);
                    content.append(validateLength(narration.concat(today2.toUpperCase()), 34)); //NARRATION
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 25)); //EXTERNAL_REF
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 10)); //CORRESPONDING_CUSTOMER
                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getProdCategory(), 6)); //PROD_CATEG
                    content.append(fieldDelimeter);
                    content.append(validateLength(bean.getProfitCenter(), 8)); //DAO
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 15)); //RESERVED_01
                    content.append(fieldDelimeter);
                    content.append(validateLength("", 15)); //RESERVED_02
                    content.append(System.lineSeparator());

                    detailSection.append(content);
                }
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                Configurations.PROCESS_FAILD_COUNT++;
                logError.error("Error while writing RB36 file.Exit from the process. Exception in  txn Type " + entrySet.getKey() + "--->" + e);
                throw e;
            }
        }
        file.setFileContent(detailSection);
        file.setTxnIdList(txnId);
        file.setHeaderCreditCount(headerCreditCount);
        file.setHeaderDebitCount(headerDebitCount);
        file.setHeaderCreditBig(headerCreditBig);
        file.setHeaderDebitBig(headerDebitBig);

        return file;
    }
}
