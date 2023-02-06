/**
 * Author : lahiru_p
 * Date : 11/28/2022
 * Time : 10:05 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.AutoSettlementDao;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.epic.cms.util.CommonMethods.validateLength;
import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class AutoSettlementRepo implements AutoSettlementDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private StatusVarList statusVarList;

    @Autowired
    LogManager logManager;

    @Override

    public int updateAutoSettlementWithPayments() throws Exception {
        Map<String, Object> details = new LinkedHashMap<String, Object>();

        int count = 0;
        infoLogger.info("  STEP 01 - Check Received Payments");
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy");
            String dateEID = format.format(Configurations.EOD_DATE);

            String query = "SELECT A.CARDACCOUNT, A.CARDNO, A.REMAININGAMOUNT AS OLDREMAININGAMOUNT, NVL((A.REMAININGAMOUNT - NVL(Z.TOTALPAYMENT, 0)),0) AS NEWREMAININGAMOUNT FROM AUTOSETTLEMENT A LEFT JOIN (SELECT ACCOUNTNO, NVL(SUM(TRANSACTIONAMOUNT),0) AS TOTALPAYMENT FROM EODTRANSACTION WHERE TRANSACTIONTYPE =? AND TRUNC(SETTLEMENTDATE) = TO_DATE(?,'DD-MM-YY') AND STATUS IN(?,?) GROUP BY ACCOUNTNO) Z ON A.CARDACCOUNT = Z.ACCOUNTNO WHERE A.REMAININGAMOUNT > 0 ";

            Object[] param = null;

            if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getINITIAL_STATUS())) {
                query += " and A.CARDACCOUNT not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? ) AND cardno = 4835591343562774";

                count = Objects.requireNonNull(backendJdbcTemplate.query(query, (ResultSet result) -> {
                    int temp = 0;
                    while (result.next()) {

                        StringBuffer cardNo = new StringBuffer(result.getString("CARDNO"));// todo
                        StringBuffer newCardNo = this.getNewCardNumber(cardNo);
                        double newRemainingAmount = result.getDouble("NEWREMAININGAMOUNT");

                        newRemainingAmount = (newRemainingAmount < 0) ? 0 : newRemainingAmount;

                        updateRemainingAmount(newRemainingAmount, newCardNo);

                        temp++;

                        details.put("Card Number", CommonMethods.cardNumberMask(cardNo));
                        details.put("Outsanding After Payment", Double.toString(newRemainingAmount));
                        details.put("Status", "Successfull");

                        infoLogger.info(logManager.processDetailsStyles(details));
                    }
                    return temp;

                }, Configurations.TXN_TYPE_PAYMENT, dateEID, Configurations.EOD_DONE_STATUS, Configurations.INITIAL_STATUS, statusVarList.getEOD_PENDING_STATUS()));

            } else if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                query += " and A.CARDACCOUNT in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ?  and EODID < ? and PROCESSSTEPID <= ? )";

                count = Objects.requireNonNull(backendJdbcTemplate.query(query, (ResultSet result) -> {
                            int temp = 0;
                            while (result.next()) {

                                StringBuffer cardNo = new StringBuffer(result.getString("CARDNO"));// todo
                                StringBuffer newCardNo = this.getNewCardNumber(cardNo);
                                double newRemainingAmount = result.getDouble("NEWREMAININGAMOUNT");

                                newRemainingAmount = (newRemainingAmount < 0) ? 0 : newRemainingAmount;

                                updateRemainingAmount(newRemainingAmount, newCardNo);

                                temp++;

                                details.put("Card Number", CommonMethods.cardNumberMask(cardNo));
                                details.put("Outsanding After Payment", Double.toString(newRemainingAmount));
                                details.put("Status", "Successfull");

                                infoLogger.info(logManager.processDetailsStyles(details));
                            }
                            return temp;

                        }, Configurations.TXN_TYPE_PAYMENT, dateEID, Configurations.EOD_DONE_STATUS, Configurations.INITIAL_STATUS
                        , statusVarList.getEOD_PENDING_STATUS(), Configurations.ERROR_EOD_ID, Configurations.PROCESS_STEP_ID));

            }
        } catch (Exception e) {
            infoLogger.info("    Status - Payment Update fails");
            throw e;
        }
        infoLogger.info("  STEP 01 - Successfull with " + count + " Payment Updates");
        return count;
    }

    private void updateRemainingAmount(double amount, StringBuffer newCardNo) {
        try {
            String query = "UPDATE autosettlement SET REMAININGAMOUNT = ? WHERE CARDNO = ? ";

            amount = (amount < 0) ? 0 : amount;
            backendJdbcTemplate.update(query, amount, newCardNo.toString());

        } catch (Exception e) {
            throw e;
        }
    }


    public StringBuffer getNewCardNumber(StringBuffer oldCardNumber) {
        StringBuffer cardNumber = oldCardNumber;

        try {
            String sql = "SELECT CR1.NEWCARDNUMBER CARDNUMBER FROM CARDREPLACE CR1 LEFT JOIN CARDREPLACE CR2 ON CR2.OLDCARDNUMBER = CR1.NEWCARDNUMBER INNER JOIN CARD C ON C.CARDNUMBER = CR1.NEWCARDNUMBER WHERE C.CARDSTATUS NOT IN (?,?) START WITH CR1.OLDCARDNUMBER = ? CONNECT BY PRIOR CR1.NEWCARDNUMBER = CR1.OLDCARDNUMBER";
            cardNumber = backendJdbcTemplate.queryForObject(sql,
                    StringBuffer.class,
                    statusVarList.getCARD_REPLACED_STATUS(),
                    statusVarList.getCARD_PRODUCT_CHANGE_STATUS(),
                    oldCardNumber.toString());

        } catch (EmptyResultDataAccessException e) {
            cardNumber = oldCardNumber;

        } catch (Exception e) {
            throw e;
        }
        return cardNumber;
    }

    @Override

    public void getUnsuccessfullStandingInstructionFeeEligibleCards() throws Exception {
        ArrayList<String> cardList = new ArrayList<String>();
        try {
            String query = "SELECT CARDNO FROM AUTOSETTLEMENT WHERE PROCESSINGCOUNT = 2 and REMAININGAMOUNT >0 ";

            if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getINITIAL_STATUS())) {
                query += " and CARDACCOUNT not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? )";

                backendJdbcTemplate.query(query, (ResultSet rs) -> {
                    while (rs.next()) {
                        StringBuffer cardNo = new StringBuffer(rs.getString("CARDNO"));
                        try {
                            addCardFeeCount(cardNo, Configurations.UNSUCCESSFUL_STANDING_INSTRUCTION_CHARGE_FEE, 0.00);
                        } catch (Exception e) {
                            errorLogger.error("Exception in Add Card Fee Count ", e);
                        }
                    }
                }, statusVarList.getEOD_PENDING_STATUS());
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                query += " and CARDACCOUNT in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ?  and EODID < ?  and PROCESSSTEPID <= ? )";

                backendJdbcTemplate.query(query, (ResultSet rs) -> {
                    while (rs.next()) {
                        StringBuffer cardNo = new StringBuffer(rs.getString("CARDNO"));
                        try {
                            addCardFeeCount(cardNo, Configurations.UNSUCCESSFUL_STANDING_INSTRUCTION_CHARGE_FEE, 0.00);
                        } catch (Exception e) {
                            errorLogger.error("Exception in Add Card Fee Count ", e);
                        }
                    }
                }, statusVarList.getEOD_PENDING_STATUS(), Configurations.ERROR_EOD_ID, Configurations.PROCESS_STEP_ID);
            }

        } catch (Exception e) {
            throw e;
        }
    }


    public int addCardFeeCount(StringBuffer cardNumber, String feeCode, double cashAmount) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        int count = 0;
        boolean forward = false;
        String query = null;
        try {
            forward = this.checkFeeExistForCard(cardNumber, feeCode);
            if (forward) {
                boolean isFeeUpdateRequired = this.getFeeCode(cardNumber, feeCode);
                if (isFeeUpdateRequired) {
                    query = "UPDATE CARDFEECOUNT SET FEECOUNT = FEECOUNT + 1,CASHAMOUNT=CASHAMOUNT+?, LASTUPDATEDUSER= ?, LASTUPDATEDTIME= SYSDATE, STATUS =? WHERE CARDNUMBER = ? AND FEECODE = ?";
                    count = backendJdbcTemplate.update(query, cashAmount, Configurations.EOD_USER, statusVarList.getEOD_PENDING_STATUS(), cardNumber.toString(), feeCode);
                } else {
                    query = "INSERT INTO CARDFEECOUNT (CARDNUMBER,FEECODE,FEECOUNT,CASHAMOUNT,STATUS,CREATEDDATE,LASTUPDATEDTIME,LASTUPDATEDUSER) VALUES (?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),SYSDATE,?)";
                    count = backendJdbcTemplate.update(query, cardNumber.toString(), feeCode, 1, cashAmount, statusVarList.getEOD_PENDING_STATUS(), sdf.format(Configurations.EOD_DATE), Configurations.EOD_USER);
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
        return count;
    }


    public Boolean getFeeCode(StringBuffer cardNumber, String feeCode) throws Exception {
        boolean forward = false;
        int feeCount = 0;
        try {
            String query = "SELECT C.FEECOUNT FROM CARDFEECOUNT C WHERE C.CARDNUMBER = ? AND C.FEECODE = ?";
            feeCount = backendJdbcTemplate.queryForObject(query, Integer.class, cardNumber.toString(), feeCode);
            if (feeCount > 0 && (!feeCode.equals(Configurations.LATE_PAYMENT_FEE) && !feeCode.equals(Configurations.ANNUAL_FEE))) {
                forward = true;
            }
        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (Exception ex) {
            throw ex;
        }
        return forward;
    }


    public boolean checkFeeExistForCard(StringBuffer cardNumber, String feeCode) throws Exception {
        int recordCount = 0;
        boolean forward = false;
        try {
            String query = "SELECT COUNT(C.CARDNUMBER) AS RECORDCOUNT FROM CARD C INNER JOIN FEEPROFILEFEE FPF ON C.FEEPROFILECODE  = FPF.FEEPROFILECODE WHERE C.CARDNUMBER   = ? AND FPF.FEECODE NOT IN (SELECT PFPF.FEECODE FROM CARD C INNER JOIN PROMOFEEPROFILE PFP ON C.PROMOFEEPROFILECODE = PFP.PROMOFEEPROFILECODE INNER JOIN PROMOFEEPROFILEFEE PFPF ON C.PROMOFEEPROFILECODE = PFPF.PROMOFEEPROFILECODE WHERE C.CARDNUMBER = ? AND STATUS <> ?) AND FPF.FEECODE = ?";
            recordCount = backendJdbcTemplate.queryForObject(query, Integer.class, cardNumber.toString(), cardNumber.toString(), statusVarList.getFEE_PROMOTION_PROFILE_EXPIRE(), feeCode);
            if (recordCount > 0) {
                forward = true;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return forward;
    }

    @Override

    public String[] generatePartialAutoSettlementFile(String fileDirectory, String fileName, String sequence, String fieldDelimeter) throws Exception {
        String[] partialList = new String[3];
        try {
            infoLogger.info("  STEP 02 - Creating Partial Auto Settlement File");

            String query = "SELECT AST.*,CU.CURRENCYALPHACODE FROM AUTOSETTLEMENT AST, CURRENCY CU WHERE AST.PROCESSINGCOUNT >0 AND AST.RUNNINGSTATUS =? AND AST.AUTOSETTLEMENTSTATUS =? AND AST.STATUS =? AND AST.CURRENCYNUMCODE = CU.CURRENCYNUMCODE ";

            if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getINITIAL_STATUS())) {
                query += " and AST.CARDACCOUNT not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? )";

                backendJdbcTemplate.query(query, (ResultSet result) -> {
                            BigDecimal totalTxnAmount = new BigDecimal(0.0);
                            BigDecimal remainingAmount = new BigDecimal(0.0);
                            BigDecimal remainingAmountRoundOff = new BigDecimal(0.0);

                            BigDecimal headerCreditBig = new BigDecimal(0.0);
                            BigDecimal headerDebitBig = new BigDecimal(0.0);
                            int headerCreditCount = 0;
                            int headerDebitCount = 0;

                            StringBuilder sbContent = new StringBuilder();
                            StringBuilder sbHeader = new StringBuilder();
                            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = 0;
                            Configurations.PROCESS_SUCCESS_COUNT = 0;
                            int txnCount = 0;

                            while (result.next()) {
                                try {
                                    /**
                                     this hashmap is used as input parameter for processDetails method in
                                     ManageLog class
                                     */
                                    Map<String, Object> details = new LinkedHashMap<String, Object>();

                                    remainingAmount = new BigDecimal(result.getString("REMAININGAMOUNT"));
                                    remainingAmountRoundOff = remainingAmount.setScale(2, RoundingMode.DOWN);

                                    if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                                        txnCount = txnCount + 1;
                                        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS++;
                                        sbContent.append(validateLength(sequence, 35)); //SEQ_NO
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("Y", 35)); //PARTIAL_DR
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("N", 1)); //DR_EAR_MARK
                                        sbContent.append(fieldDelimeter);

                                        headerCreditBig = headerCreditBig.add(remainingAmountRoundOff);
                                        headerCreditCount++;
                                        headerDebitBig = headerDebitBig.add(remainingAmountRoundOff);
                                        headerDebitCount++;

                                        sbContent.append(validateLength(result.getString("DEBITACCOUNTNO"), 36)); //DR_ACCOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("LKR", 3)); //DR_CURRENCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(remainingAmountRoundOff.toString(), 18)); //DR_AMOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(remainingAmountRoundOff.toString(), 19)); //DR_AMOUNT_LCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(result.getString("CARDNO"), 16)); //DR_NARRATION
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(Configurations.COLLECTION_ACCOUNT, 36)); //CR_ACCOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("LKR", 3)); //CR_CURRENCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 19)); //CR_AMOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 19)); //CR_AMOUNT_LCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(result.getString("CARDNO"), 27)); //CR_NARRATION
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //EXTERNAL_REF
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //Host Deal Reference
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 6)); //Purpose Code-1
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 10)); //Purpose Code-2
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //additional_reference_no_1
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("AC", 2)); //Transaction Code
                                        sbContent.append(System.lineSeparator());

                                        totalTxnAmount = totalTxnAmount.add(remainingAmountRoundOff);
                                        Configurations.PROCESS_SUCCESS_COUNT++;
                                    }

                                    partialList[0] = sbContent.toString();
                                    partialList[1] = Integer.toString(headerDebitCount);
                                    partialList[2] = headerDebitBig.toString();
                                    updateAutoSettlementTable(remainingAmountRoundOff.toString(), new StringBuffer(result.getString("CARDNO")), result.getInt("PROCESSINGCOUNT"));

                                    details.put("Card Number", CommonMethods.cardNumberMask(new StringBuffer(result.getString("CARDNO"))));
                                    details.put("Amount", remainingAmountRoundOff.toString());
                                    details.put("Debit Acc No", result.getString("DEBITACCOUNTNO"));
                                    details.put("Collection Acc No", Configurations.COLLECTION_ACCOUNT);
                                    infoLogger.info(logManager.processDetailsStyles(details));

                                } catch (Exception e) {
                                    errorLogger.error("Exception in creating partial file content ", e);
                                }

                            }
                            return partialList;
                        }, statusVarList.getRUNNING_STATUS()
                        , statusVarList.getACTIVE_STATUS()
                        , statusVarList.getREQUEST_ACCEPTED()
                        , statusVarList.getEOD_PENDING_STATUS());
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                query += " and AST.CARDACCOUNT in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? and EODID < ? and PROCESSSTEPID <= ?)";

                backendJdbcTemplate.query(query, (ResultSet result) -> {
                            BigDecimal totalTxnAmount = new BigDecimal(0.0);
                            BigDecimal remainingAmount = new BigDecimal(0.0);
                            BigDecimal remainingAmountRoundOff = new BigDecimal(0.0);

                            BigDecimal headerCreditBig = new BigDecimal(0.0);
                            BigDecimal headerDebitBig = new BigDecimal(0.0);
                            int headerCreditCount = 0;
                            int headerDebitCount = 0;

                            StringBuilder sbContent = new StringBuilder();
                            StringBuilder sbHeader = new StringBuilder();
                            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = 0;
                            Configurations.PROCESS_SUCCESS_COUNT = 0;
                            int txnCount = 0;

                            while (result.next()) {
                                try {
                                    /**
                                     this hashmap is used as input parameter for processDetails method in
                                     ManageLog class
                                     */
                                    Map<String, Object> details = new LinkedHashMap<String, Object>();

                                    remainingAmount = new BigDecimal(result.getString("REMAININGAMOUNT"));
                                    remainingAmountRoundOff = remainingAmount.setScale(2, RoundingMode.DOWN);

                                    if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                                        txnCount = txnCount + 1;
                                        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS++;
                                        sbContent.append(validateLength(sequence, 35)); //SEQ_NO
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("Y", 35)); //PARTIAL_DR
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("N", 1)); //DR_EAR_MARK
                                        sbContent.append(fieldDelimeter);

                                        headerCreditBig = headerCreditBig.add(remainingAmountRoundOff);
                                        headerCreditCount++;
                                        headerDebitBig = headerDebitBig.add(remainingAmountRoundOff);
                                        headerDebitCount++;

                                        sbContent.append(validateLength(result.getString("DEBITACCOUNTNO"), 36)); //DR_ACCOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("LKR", 3)); //DR_CURRENCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(remainingAmountRoundOff.toString(), 18)); //DR_AMOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(remainingAmountRoundOff.toString(), 19)); //DR_AMOUNT_LCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(result.getString("CARDNO"), 16)); //DR_NARRATION
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(Configurations.COLLECTION_ACCOUNT, 36)); //CR_ACCOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("LKR", 3)); //CR_CURRENCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 19)); //CR_AMOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 19)); //CR_AMOUNT_LCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(result.getString("CARDNO"), 27)); //CR_NARRATION
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //EXTERNAL_REF
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //Host Deal Reference
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 6)); //Purpose Code-1
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 10)); //Purpose Code-2
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //additional_reference_no_1
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("AC", 2)); //Transaction Code
                                        sbContent.append(System.lineSeparator());

                                        totalTxnAmount = totalTxnAmount.add(remainingAmountRoundOff);
                                        Configurations.PROCESS_SUCCESS_COUNT++;
                                    }

                                    partialList[0] = sbContent.toString();
                                    partialList[1] = Integer.toString(headerDebitCount);
                                    partialList[2] = headerDebitBig.toString();
                                    updateAutoSettlementTable(remainingAmountRoundOff.toString(), new StringBuffer(result.getString("CARDNO")), result.getInt("PROCESSINGCOUNT"));

                                    details.put("Card Number", CommonMethods.cardNumberMask(new StringBuffer(result.getString("CARDNO"))));
                                    details.put("Amount", remainingAmountRoundOff.toString());
                                    details.put("Debit Acc No", result.getString("DEBITACCOUNTNO"));
                                    details.put("Collection Acc No", Configurations.COLLECTION_ACCOUNT);
                                    infoLogger.info(logManager.processDetailsStyles(details));

                                } catch (Exception e) {
                                    errorLogger.error("Exception in creating partial file content ", e);
                                }

                            }
                            return partialList;
                        }, statusVarList.getRUNNING_STATUS()
                        , statusVarList.getACTIVE_STATUS()
                        , statusVarList.getREQUEST_ACCEPTED()
                        , statusVarList.getEOD_PENDING_STATUS()
                        , Configurations.ERROR_EOD_ID
                        , Configurations.PROCESS_STEP_ID);
            }
        } catch (Exception e) {
            throw e;
        }

        return partialList;
    }


    public void updateAutoSettlementTable(String remainingAmount, StringBuffer cardNo, int processingCount) {
        int runningStatus = 0;
        DecimalFormat df = new DecimalFormat("#.00");
        df.setRoundingMode(RoundingMode.FLOOR);
        String query = null;
        try {
            BigDecimal remainAmount = new BigDecimal(remainingAmount);
            if (remainAmount.compareTo(BigDecimal.ZERO) > 0 && processingCount > 0) {
                runningStatus = 1;
                processingCount = processingCount - 1;
                if (processingCount == 0) {
                    runningStatus = 0; /** onhold state*/
                    processingCount = 3; //--todo this value should be original value.
                }

                query = "UPDATE autosettlement SET PROCESSINGCOUNT = ? , RUNNINGSTATUS = ? ,REMAININGAMOUNT=? WHERE CARDNO = ?";

                backendJdbcTemplate.update(query, processingCount, runningStatus, remainingAmount, cardNo.toString());

            } else if (remainAmount.compareTo(BigDecimal.ZERO) <= 0) {
                runningStatus = 2;
                processingCount = 3; /**-to do this should be original value*/

                query = "UPDATE autosettlement SET PROCESSINGCOUNT = ?,RUNNINGSTATUS = ? ,REMAININGAMOUNT=? WHERE CARDNO = ?";

                backendJdbcTemplate.update(query, processingCount, runningStatus, 0.00, cardNo.toString());
            }
        } catch (Exception e) {
            errorLogger.error("updateAutoSettlementTable Failed ", e);
        }
    }

    @Override

    public String[] generateAutoSettlementFile(String fileDirectory, String fileName, String sequence, String fieldDelimeter) throws Exception {
        String[] partialList = new String[3];
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            DecimalFormat df = new DecimalFormat("#.00");
            df.setRoundingMode(RoundingMode.FLOOR);

            infoLogger.info("  STEP 03 - Creating Auto Settlement File");//2 spaces

            String query = "SELECT AST.* ,BLS.DUEDATE AS SETTLEMENTDUEDATE ,BLS.STATEMENTENDDATE AS SETTLEMENTENDDATE ,BLS.CLOSINGBALANCE,CU.CURRENCYALPHACODE,BLS.MINAMOUNT  FROM autosettlement AST , BILLINGLASTSTATEMENTSUMMARY BLS ,CURRENCY CU  WHERE ast.cardno = bls.cardno AND  CU.CURRENCYNUMCODE = AST.CURRENCYNUMCODE AND  BLS.DUEDATE = to_date(?, 'dd-MM-yy')+1 AND  BLS.CLOSINGBALANCE > 0 AND  AST.STATUS = ? AND  AST.AUTOSETTLEMENTSTATUS = ? AND  AST.RUNNINGSTATUS IN ( ? , ?) ";

            if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getINITIAL_STATUS())) {
                query += " and AST.CARDACCOUNT not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ?)";

                backendJdbcTemplate.query(query, (ResultSet result) -> {

                            int recordCount = 0;
                            StringBuilder sbContent = new StringBuilder();
                            StringBuilder sbHeader = new StringBuilder();
                            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = 0;
                            Configurations.PROCESS_SUCCESS_COUNT = 0;
                            int txnCount = 0;
                            BigDecimal headerCreditBig = new BigDecimal(0.0);
                            BigDecimal headerDebitBig = new BigDecimal(0.0);
                            int headerCreditCount = 0;
                            int headerDebitCount = 0;
                            BigDecimal totalTxnAmount = new BigDecimal(0.0);
                            while (result.next()) {
                                try {
                                    recordCount++;
                                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS++;
                                    boolean isInsert = false;
                                    BigDecimal txnAmount = new BigDecimal(0.0);
                                    BigDecimal lastTxnAmount = new BigDecimal(0.0);
                                    BigDecimal lastTxnAmountRoundOff = new BigDecimal(0.0);
                                    LinkedHashMap<String, Object> details = new LinkedHashMap<String, Object>();
                                    int statementDayEODID = 0;
                                    CreateEodId convertToEOD = new CreateEodId();
                                    java.util.Date statementEnd = result.getDate("SETTLEMENTENDDATE");
                                    statementDayEODID = Integer.parseInt(convertToEOD.getDate(statementEnd) + "00");
                                    String accNo = result.getString("CARDACCOUNT");
                                    BigDecimal payments = this.getPaymentAmount(accNo, statementDayEODID);

                                    if (result.getString("FIXORPERCENTAGE").equals("PER")) {
                                        BigDecimal autoSettlemetAmount = new BigDecimal(result.getString("AUTOSETAMOUNT")).
                                                divide(BigDecimal.valueOf(100), MathContext.DECIMAL32);

                                        txnAmount = new BigDecimal(result.getString("CLOSINGBALANCE")).
                                                multiply(autoSettlemetAmount, MathContext.DECIMAL64);

                                        if (new BigDecimal(result.getString("MINAMOUNT")).compareTo(txnAmount) > 0) {
                                            txnAmount = new BigDecimal(result.getString("MINAMOUNT"));
                                        }

                                        if (txnAmount.compareTo(payments) > 0) {
                                            lastTxnAmount = txnAmount.subtract(payments);
                                            lastTxnAmountRoundOff = lastTxnAmount.setScale(2, RoundingMode.DOWN);
                                            totalTxnAmount = totalTxnAmount.add(lastTxnAmountRoundOff);
                                            isInsert = true;//SI order needed to perform
                                            txnCount = txnCount + 1;
                                        }

                                    } else if (result.getString("FIXORPERCENTAGE").equals("FIX")) {
                                        txnAmount = new BigDecimal(result.getString("AUTOSETAMOUNT"));

                                        if (txnAmount.compareTo(payments) > 0) {
                                            lastTxnAmount = txnAmount.subtract(payments);
                                            lastTxnAmountRoundOff = lastTxnAmount.setScale(2, RoundingMode.DOWN);
                                            totalTxnAmount = totalTxnAmount.add(lastTxnAmountRoundOff);
                                            isInsert = true;//SI order needed to perform
                                            txnCount = txnCount + 1;
                                        }

                                    }
                                    /**check whethe there has a value to request SI order*/
                                    if (isInsert) {
                                        sbContent.append(validateLength(sequence, 35)); //SEQ_NO
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("Y", 35)); //PARTIAL_DR
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("N", 1)); //DR_EAR_MARK
                                        sbContent.append(fieldDelimeter);

                                        headerCreditBig = headerCreditBig.add(lastTxnAmountRoundOff);
                                        headerCreditCount++;
                                        headerDebitBig = headerDebitBig.add(lastTxnAmountRoundOff);
                                        headerDebitCount++;

                                        sbContent.append(validateLength(result.getString("DEBITACCOUNTNO"), 36)); //DR_ACCOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("LKR", 3)); //DR_CURRENCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(lastTxnAmountRoundOff.toString(), 18)); //DR_AMOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(lastTxnAmountRoundOff.toString(), 19)); //DR_AMOUNT_LCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(result.getString("CARDNO"), 16)); //DR_NARRATION
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(Configurations.COLLECTION_ACCOUNT, 36)); //CR_ACCOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("LKR", 3)); //CR_CURRENCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 19)); //CR_AMOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 19)); //CR_AMOUNT_LCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(result.getString("CARDNO"), 27)); //CR_NARRATION
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //EXTERNAL_REF
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //Host Deal Reference
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 6)); //Purpose Code-1
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 10)); //Purpose Code-2
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //additional_reference_no_1
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("AC", 2)); //Transaction Code
                                        sbContent.append(System.lineSeparator());

                                        updateAutoSettlementTable(lastTxnAmountRoundOff.toString(),
                                                new StringBuffer(result.getString("CARDNO")), result.getInt("PROCESSINGCOUNT"));
                                    } else {
                                        updateAutoSettlementTable(lastTxnAmountRoundOff.toString(), new StringBuffer(result.getString("CARDNO")), result.getInt("PROCESSINGCOUNT"));//completed the autosettlement request due to early payments
                                        /**between laststatement date and EOD_ID*/
                                    }

                                    partialList[0] = sbContent.toString();
                                    partialList[1] = Integer.toString(headerDebitCount);
                                    partialList[2] = headerDebitBig.toString();

                                    details.put("Card Number", CommonMethods.cardNumberMask(new StringBuffer(result.getString("CARDNO"))));
                                    details.put("Amount", lastTxnAmountRoundOff.toString());
                                    details.put("Debit Acc No", result.getString("DEBITACCOUNTNO"));
                                    details.put("Collection Acc No", Configurations.COLLECTION_ACCOUNT);

                                    infoLogger.info(logManager.processDetailsStyles(details));
                                    Configurations.PROCESS_SUCCESS_COUNT++;
                                } catch (Exception e) {
                                    errorLogger.error("Exception in Creating Full File Content ", e);
                                }
                            }
                            return partialList;
                        }, sdf.format(Configurations.EOD_DATE)
                        , statusVarList.getREQUEST_ACCEPTED() //RQAC
                        , statusVarList.getACTIVE_STATUS() //ACT
                        , statusVarList.getONHOLD_STATUS() //0
                        , statusVarList.getCOMPLETED_STATUS() //2
                        , statusVarList.getEOD_PENDING_STATUS());

            } else if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
                query += " and AST.CARDACCOUNT in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? and EODID < ? and PROCESSSTEPID <= ? )";

                backendJdbcTemplate.query(query, (ResultSet result) -> {

                            int recordCount = 0;
                            StringBuilder sbContent = new StringBuilder();
                            StringBuilder sbHeader = new StringBuilder();
                            Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = 0;
                            Configurations.PROCESS_SUCCESS_COUNT = 0;
                            int txnCount = 0;
                            BigDecimal headerCreditBig = new BigDecimal(0.0);
                            BigDecimal headerDebitBig = new BigDecimal(0.0);
                            int headerCreditCount = 0;
                            int headerDebitCount = 0;
                            BigDecimal totalTxnAmount = new BigDecimal(0.0);
                            while (result.next()) {
                                try {
                                    recordCount++;
                                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS++;
                                    boolean isInsert = false;
                                    BigDecimal txnAmount = new BigDecimal(0.0);
                                    BigDecimal lastTxnAmount = new BigDecimal(0.0);
                                    BigDecimal lastTxnAmountRoundOff = new BigDecimal(0.0);
                                    LinkedHashMap<String, Object> details = new LinkedHashMap<String, Object>();
                                    int statementDayEODID = 0;
                                    CreateEodId convertToEOD = new CreateEodId();
                                    java.util.Date statementEnd = result.getDate("SETTLEMENTENDDATE");
                                    statementDayEODID = Integer.parseInt(convertToEOD.getDate(statementEnd) + "00");
                                    String accNo = result.getString("CARDACCOUNT");
                                    BigDecimal payments = this.getPaymentAmount(accNo, statementDayEODID);

                                    if (result.getString("FIXORPERCENTAGE").equals("PER")) {
                                        BigDecimal autoSettlemetAmount = new BigDecimal(result.getString("AUTOSETAMOUNT")).
                                                divide(BigDecimal.valueOf(100), MathContext.DECIMAL32);

                                        txnAmount = new BigDecimal(result.getString("CLOSINGBALANCE")).
                                                multiply(autoSettlemetAmount, MathContext.DECIMAL64);

                                        if (new BigDecimal(result.getString("MINAMOUNT")).compareTo(txnAmount) > 0) {
                                            txnAmount = new BigDecimal(result.getString("MINAMOUNT"));
                                        }

                                        if (txnAmount.compareTo(payments) > 0) {
                                            lastTxnAmount = txnAmount.subtract(payments);
                                            lastTxnAmountRoundOff = lastTxnAmount.setScale(2, RoundingMode.DOWN);
                                            totalTxnAmount = totalTxnAmount.add(lastTxnAmountRoundOff);
                                            isInsert = true;//SI order needed to perform
                                            txnCount = txnCount + 1;
                                        }

                                    } else if (result.getString("FIXORPERCENTAGE").equals("FIX")) {
                                        txnAmount = new BigDecimal(result.getString("AUTOSETAMOUNT"));

                                        if (txnAmount.compareTo(payments) > 0) {
                                            lastTxnAmount = txnAmount.subtract(payments);
                                            lastTxnAmountRoundOff = lastTxnAmount.setScale(2, RoundingMode.DOWN);
                                            totalTxnAmount = totalTxnAmount.add(lastTxnAmountRoundOff);
                                            isInsert = true;//SI order needed to perform
                                            txnCount = txnCount + 1;
                                        }

                                    }
                                    /**check whethe there has a value to request SI order*/
                                    if (isInsert) {
                                        sbContent.append(validateLength(sequence, 35)); //SEQ_NO
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("Y", 35)); //PARTIAL_DR
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("N", 1)); //DR_EAR_MARK
                                        sbContent.append(fieldDelimeter);

                                        headerCreditBig = headerCreditBig.add(lastTxnAmountRoundOff);
                                        headerCreditCount++;
                                        headerDebitBig = headerDebitBig.add(lastTxnAmountRoundOff);
                                        headerDebitCount++;

                                        sbContent.append(validateLength(result.getString("DEBITACCOUNTNO"), 36)); //DR_ACCOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("LKR", 3)); //DR_CURRENCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(lastTxnAmountRoundOff.toString(), 18)); //DR_AMOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(lastTxnAmountRoundOff.toString(), 19)); //DR_AMOUNT_LCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(result.getString("CARDNO"), 16)); //DR_NARRATION
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(Configurations.COLLECTION_ACCOUNT, 36)); //CR_ACCOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("LKR", 3)); //CR_CURRENCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 19)); //CR_AMOUNT
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 19)); //CR_AMOUNT_LCY
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength(result.getString("CARDNO"), 27)); //CR_NARRATION
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //EXTERNAL_REF
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //Host Deal Reference
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 6)); //Purpose Code-1
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 10)); //Purpose Code-2
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("", 35)); //additional_reference_no_1
                                        sbContent.append(fieldDelimeter);
                                        sbContent.append(validateLength("AC", 2)); //Transaction Code
                                        sbContent.append(System.lineSeparator());

                                        updateAutoSettlementTable(lastTxnAmountRoundOff.toString(),
                                                new StringBuffer(result.getString("CARDNO")), result.getInt("PROCESSINGCOUNT"));
                                    } else {
                                        updateAutoSettlementTable(lastTxnAmountRoundOff.toString(), new StringBuffer(result.getString("CARDNO")), result.getInt("PROCESSINGCOUNT"));//completed the autosettlement request due to early payments
                                        //between laststatement date and EOD_ID
                                    }

                                    partialList[0] = sbContent.toString();
                                    partialList[1] = Integer.toString(headerDebitCount);
                                    partialList[2] = headerDebitBig.toString();

                                    details.put("Card Number", CommonMethods.cardNumberMask(new StringBuffer(result.getString("CARDNO"))));
                                    details.put("Amount", lastTxnAmountRoundOff.toString());
                                    details.put("Debit Acc No", result.getString("DEBITACCOUNTNO"));
                                    details.put("Collection Acc No", Configurations.COLLECTION_ACCOUNT);

                                    infoLogger.info(logManager.processDetailsStyles(details));
                                    Configurations.PROCESS_SUCCESS_COUNT++;
                                } catch (Exception e) {
                                    errorLogger.error("Exception in Creating Full File Content ", e);
                                }
                            }
                            return partialList;
                        }, sdf.format(Configurations.EOD_DATE)
                        , statusVarList.getREQUEST_ACCEPTED() //RQAC
                        , statusVarList.getACTIVE_STATUS() //ACT
                        , statusVarList.getONHOLD_STATUS() //0
                        , statusVarList.getCOMPLETED_STATUS() //2
                        , statusVarList.getEOD_PENDING_STATUS()
                        , Configurations.ERROR_EOD_ID
                        , Configurations.PROCESS_STEP_ID);
            }

        } catch (Exception e) {
            infoLogger.error("  STEP 03 Fail to Create File");
            throw e;
        }
        return partialList;
    }

    public BigDecimal getPaymentAmount(String accNO, int startEOD) throws Exception {
        BigDecimal paymentAmount = new BigDecimal(0.0);
        double amount = 0;

        try {

            String query = "SELECT NVL(SUM(TRANSACTIONAMOUNT),0) AS TOTALPAY FROM EODTRANSACTION WHERE TRANSACTIONTYPE =? AND EODID > ? AND EODID <= ? AND STATUS IN(?,?) AND  ACCOUNTNO IN (?) ";

            paymentAmount = Objects.requireNonNull(backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        BigDecimal temp = new BigDecimal(0.0);
                        while (result.next()) {
                            temp = new BigDecimal(result.getString("TOTALPAY"));
                        }
                        return temp;
                    },
                    Configurations.TXN_TYPE_PAYMENT,
                    startEOD,
                    Configurations.EOD_ID,
                    statusVarList.getINITIAL_STATUS(),
                    statusVarList.getEOD_DONE_STATUS(),
                    accNO
            ));


        } catch (EmptyResultDataAccessException e) {
            return new BigDecimal("0.0");
        } catch (Exception e) {
            errorLogger.error(String.valueOf(e));
            throw e;
        }

        return paymentAmount;
    }
}
