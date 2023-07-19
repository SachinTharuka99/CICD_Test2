package com.epic.cms.repository;

import com.epic.cms.dao.EOMInterestDao;
import com.epic.cms.model.bean.CardBillingInfoBean;
import com.epic.cms.model.bean.EomCardBean;
import com.epic.cms.model.rowmapper.GetEOMCardListRowMapper;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.epic.cms.util.CommonMethods.ValuesRoundup;

@Repository
public class EOMInterestRepo implements EOMInterestDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    JdbcTemplate backendJdbcTemplate;

    @Autowired
    LogManager logManager;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public ArrayList<EomCardBean> getEomCardList(int day) throws Exception {
        ArrayList<EomCardBean> cardList = new ArrayList<EomCardBean>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            //String sql = "SELECT CA.ACCOUNTNO, CA.CARDNUMBER, CA.STATUS, IP.INTERESTRATE, IP.INTERESTPERIODVALUE FROM CARDACCOUNT CA INNER JOIN INTERESTPROFILE IP ON CA.INTERESTPROFILECODE = IP.INTERESTPROFILECODE INNER JOIN CARD C ON CA.CARDNUMBER = C.CARDNUMBER WHERE to_number(CA.BILLINGDATE) <= ? AND (C.CARDSTATUS NOT  IN(?) OR (C.CARDSTATUS = ? AND C.CLOSEFLAG = ?)) AND CA.NEXTBILLINGDATE <= TO_DATE(?, 'DD-MM-YY')";
            String sql=queryParametersList.getEOMInterest_getEomCardList();

            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                //sql += " and CA.ACCOUNTNO not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status=?)";
                sql += queryParametersList.getEOMInterest_getEomCardList_Appender1();
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                //sql += " and CA.ACCOUNTNO in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status=? and EODID < ? and PROCESSSTEPID <=?)";
                sql += queryParametersList.getEOMInterest_getEomCardList_Appender2();
            }
            Object[] param = null;
            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                param = new Object[]{
                        day,
                        statusList.getCARD_CLOSED_STATUS(),
                        statusList.getCARD_CLOSED_STATUS(),
                        statusList.getYES_STATUS_1(),
                        sdf.format(Configurations.EOD_DATE),
                        statusList.getEOD_PENDING_STATUS(),
                };
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                param = new Object[]{
                        day,
                        statusList.getCARD_CLOSED_STATUS(),
                        statusList.getCARD_CLOSED_STATUS(),
                        statusList.getYES_STATUS_1(),
                        sdf.format(Configurations.EOD_DATE),
                        statusList.getEOD_PENDING_STATUS(),
                        Configurations.ERROR_EOD_ID,
                        Configurations.PROCESS_STEP_ID
                };
            }
            cardList = (ArrayList<EomCardBean>)backendJdbcTemplate.query(sql, new GetEOMCardListRowMapper(), param);
        } catch (Exception e) {
            throw e;
        }
        return cardList;
    }


    @Override
    public String CheckForCardIncrementStatus(StringBuffer cardNumber) throws Exception {
        boolean status = false;
        String cardStatus = null;
        try {
            //String sql = "select C.CARDSTATUS AS  CARDSTATUS from CARD C where C.CARDNUMBER=?";
            cardStatus = backendJdbcTemplate.queryForObject(queryParametersList.getEOMInterest_CheckForCardIncrementStatus(), String.class, cardNumber);
        } catch (Exception e) {
            throw e;
        }
        return cardStatus;
    }

    @Override
    public int clearEomInterest(StringBuffer cardNo) throws Exception {
        int count = 0;
        String query = null;
        try {
            //query = "DELETE FROM EOMINTEREST WHERE CARDNO = ?";
            query=queryParametersList.getEOMInterest_clearEomInterest();
            count = backendJdbcTemplate.update(query, cardNo.toString());

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<Date> getLastTwoBillingDatesOnAccount(String accNo) throws Exception {
        ArrayList<java.util.Date> lastBillingDates = new ArrayList<java.util.Date>();
        String sql = "";
        try {
            sql = "select * from (select rownum rn,STATEMENTENDDATE from(select STATEMENTENDDATE from BILLINGSTATEMENT where ACCOUNTNO = ? order by STATEMENTENDDATE desc))" + " where rn between 0 and 2";
            //sql= queryParametersList.getEOMInterest_getLastTwoBillingDatesOnAccount();
            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    lastBillingDates.add(rs.getDate("STATEMENTENDDATE"));
                }
                return lastBillingDates;
            }, accNo);
        } catch (Exception e) {
            throw e;
        }
        return lastBillingDates;
    }

    @Override
    public CardBillingInfoBean getLastTwoBillingDatesAndEodIdOnAccount(String accNo) throws Exception {
        CardBillingInfoBean cardInfoBilling = new CardBillingInfoBean();
        String sql = null;
        try {
            //sql = "select * from (select rownum rn,STATEMENTSTARTDATE,THISBILLOPERNINGBALANCE,STARTEODID,ENDEODID,THISBILLCLOSINGBALANCE,STATEMENTENDDATE,MINPAYMENTDUE,DUEDATE from(select STATEMENTSTARTDATE,THISBILLOPERNINGBALANCE,STARTEODID,ENDEODID,THISBILLCLOSINGBALANCE,STATEMENTENDDATE,MINPAYMENTDUE,DUEDATE from BILLINGSTATEMENT where ACCOUNTNO = ? order by STATEMENTENDDATE desc)) where rn between 0 and 1";
            sql = queryParametersList.getEOMInterest_getLastTwoBillingDatesOnAccount();
            cardInfoBilling = Objects.requireNonNull(backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        CardBillingInfoBean temp = new CardBillingInfoBean();
                        while (rs.next()) {
                            temp.setDueDate(rs.getDate("DUEDATE"));
                            temp.setEndEodId(rs.getInt("ENDEODID"));
                            temp.setStartEodId(rs.getInt("STARTEODID"));
                            temp.setStatementStartDate(rs.getDate("STATEMENTSTARTDATE"));
                            temp.setStatementEndDate(rs.getDate("STATEMENTENDDATE"));
                            temp.setThisBillingClosingBalance(rs.getDouble("THISBILLCLOSINGBALANCE"));
                            temp.setThisBillingOpeningBalance(rs.getDouble("THISBILLOPERNINGBALANCE"));
                            temp.setMinPayDue(rs.getDouble("MINPAYMENTDUE"));
                        }
                        return temp;
            },
            accNo));
        } catch (Exception e) {
            throw e;
        }
        return cardInfoBilling;
    }

    @Override
    public ArrayList<Double> getEOMInterest(EomCardBean eomCardBean, CardBillingInfoBean lastBillingDatesAndEodId, int noOfStatement) throws Exception {
        int count = 0;
        String sql = "";
        double lsatBillOpeningBalance = lastBillingDatesAndEodId.getThisBillingOpeningBalance();
        double interest = 0;
        ArrayList<Double> logDetails = new ArrayList<Double>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        LinkedHashMap<Integer, ArrayList<Object>> lhMap = new LinkedHashMap<Integer, ArrayList<Object>>();
        ArrayList<Date> dateList = new ArrayList<Date>();
        ArrayList<Integer> dateGap = new ArrayList<Integer>();
        try {
            if (noOfStatement == 2) {/**This card can follow normal procedure*/
                //sql = "INSERT INTO TEMPTRANSACTIONDETAILS(ACCOUNTNO, EODID, TRANSACTIONID, AMOUNT, TXNTYPE, TXNCHECKDATE, CRDR) SELECT ACCOUNTNO, EODID, TRANSACTIONID, TRANSACTIONAMOUNT, TRANSACTIONTYPE, SETTLEMENTDATE, CRDR FROM EODTRANSACTION WHERE EODID > ? AND EODID < ? AND ACCOUNTNO = ? AND ADJUSTMENTSTATUS = 'NO' AND STATUS NOT IN (?) ORDER BY SETTLEMENTDATE";
                sql = queryParametersList.getEOMInterest_getEOMInterest_Appender1();
                backendJdbcTemplate.update(sql,
                        lastBillingDatesAndEodId.getStartEodId(),
                        lastBillingDatesAndEodId.getEndEodId(),
                        eomCardBean.getAccNo(),
                        statusList.getCHEQUE_RETURN_STATUS());
            } else if (noOfStatement == 1) {/**special procedure--->Select txn details*/
                //sql = "INSERT INTO TEMPTRANSACTIONDETAILS(ACCOUNTNO, EODID, TRANSACTIONID, AMOUNT, TXNTYPE, TXNCHECKDATE, CRDR) SELECT ACCOUNTNO, EODID, TRANSACTIONID, TRANSACTIONAMOUNT, TRANSACTIONTYPE, SETTLEMENTDATE, CRDR FROM EODTRANSACTION WHERE EODID < ? AND ACCOUNTNO = ? AND ADJUSTMENTSTATUS = 'NO' AND STATUS NOT IN (?) ORDER BY SETTLEMENTDATE";
                sql= queryParametersList.getEOMInterest_getEOMInterest_Appender2();
                backendJdbcTemplate.update(sql,
                        lastBillingDatesAndEodId.getEndEodId(),
                        eomCardBean.getAccNo(),
                        statusList.getCHEQUE_RETURN_STATUS());
            }
            if (noOfStatement == 2) {
                //sql = "INSERT INTO TEMPTRANSACTIONDETAILS(ACCOUNTNO, EODID, TRANSACTIONID, AMOUNT, TXNTYPE, TXNCHECKDATE, CRDR) SELECT ?, ?, ID, AMOUNT, TRANSACTIONTYPE, ADJUSTDATE, CRDR FROM ADJUSTMENT WHERE 1 = 1 AND ADJUSTDATE < TO_DATE(?, 'dd-MM-YY') AND ADJUSTDATE > TO_DATE(?, 'dd-MM-YY') AND STATUS IN (?,?) AND EODSTATUS = ? AND ADJUSTMENTTYPE NOT IN (?,?) AND uniqueid IN (SELECT CA.CARDNUMBER FROM CARDACCOUNTCUSTOMER CA, CARD CD WHERE CA.cardnumber = CD.CARDNUMBER AND CA.ACCOUNTNO = ?) ORDER BY ADJUSTDATE";
                sql = queryParametersList.getEOMInterest_getEOMInterest_Appender3();
                backendJdbcTemplate.update(sql,
                        eomCardBean.getAccNo(),
                        Configurations.EOD_ID,
                        sdf.format(lastBillingDatesAndEodId.getStatementEndDate()),
                        sdf.format(lastBillingDatesAndEodId.getStatementStartDate()),
                        statusList.getMANUAL_ADJUSTMENT_ACCEPT(),
                        Configurations.EOD_DONE_STATUS,
                        statusList.getBILLING_DONE_STATUS(),
                        Integer.toString(Configurations.LOYALTY_ADJUSTMENT_TYPE),
                        Integer.toString(Configurations.CASHBACK_ADJUSTMENT_TYPE),
                        eomCardBean.getAccNo());
            } else if (noOfStatement == 1) {
                //sql = "INSERT INTO TEMPTRANSACTIONDETAILS(ACCOUNTNO, EODID, TRANSACTIONID, AMOUNT, TXNTYPE, TXNCHECKDATE, CRDR) SELECT ?, ?, ID, AMOUNT, TRANSACTIONTYPE, ADJUSTDATE, CRDR FROM ADJUSTMENT WHERE 1 = 1 AND ADJUSTDATE < TO_DATE(?, 'dd-MM-YY') AND STATUS = ? AND EODSTATUS = ? AND ADJUSTMENTTYPE NOT IN (?,?) AND uniqueid IN (SELECT CA.CARDNUMBER FROM CARDACCOUNTCUSTOMER CA, CARD CD WHERE CA.cardnumber = CD.CARDNUMBER AND CA.ACCOUNTNO = ? ) ORDER BY ADJUSTDATE";
                sql= queryParametersList.getEOMInterest_getEOMInterest_Appender4();

                backendJdbcTemplate.update(sql,
                        eomCardBean.getAccNo(),
                        Configurations.EOD_ID,
                        sdf.format(lastBillingDatesAndEodId.getStatementEndDate()),
                        statusList.getMANUAL_ADJUSTMENT_ACCEPT(),
                        Configurations.EOD_DONE_STATUS,
                        Integer.toString(Configurations.LOYALTY_ADJUSTMENT_TYPE),
                        Integer.toString(Configurations.CASHBACK_ADJUSTMENT_TYPE),
                        eomCardBean.getAccNo());
            }
            if (noOfStatement == 2) {
                //sql = "INSERT INTO TEMPTRANSACTIONDETAILS(ACCOUNTNO, EODID, TRANSACTIONID, AMOUNT, TXNTYPE, TXNCHECKDATE, CRDR) SELECT ACCOUNTNO, ?, EODFEEID, FEEAMOUNT, FEETYPE, EFFECTDATE, CRDR FROM EODCARDFEE WHERE EFFECTDATE > TO_DATE(?, 'dd-MM-YY') AND EFFECTDATE < TO_DATE(?, 'dd-MM-YY') AND ACCOUNTNO = ? AND ADJUSTMENTSTATUS IN (?) ORDER BY EFFECTDATE";
                sql = queryParametersList.getEOMInterest_getEOMInterest_Appender5();
            }
            if (noOfStatement == 1) {
                //sql = "INSERT INTO TEMPTRANSACTIONDETAILS(ACCOUNTNO, EODID, TRANSACTIONID, AMOUNT, TXNTYPE, TXNCHECKDATE, CRDR) SELECT ACCOUNTNO, ?, EODFEEID, FEEAMOUNT, FEETYPE, EFFECTDATE, CRDR FROM EODCARDFEE WHERE EFFECTDATE > TO_DATE(?, 'dd-MM-YY') AND EFFECTDATE < TO_DATE(?, 'dd-MM-YY') AND ACCOUNTNO = ? AND ADJUSTMENTSTATUS IN (?) ORDER BY EFFECTDATE";
                sql = queryParametersList.getEOMInterest_getEOMInterest_Appender6();
            }
            backendJdbcTemplate.update(sql,
                    Configurations.EOD_ID,
                    sdf.format(lastBillingDatesAndEodId.getStatementStartDate()),
                    sdf.format(lastBillingDatesAndEodId.getStatementEndDate()),
                    eomCardBean.getAccNo(),
                    Configurations.NO_STATUS);

            count = insertIntoTempTxnDetails(eomCardBean.getAccNo(), lastBillingDatesAndEodId.getEndEodId(), "200", lastBillingDatesAndEodId.getThisBillingClosingBalance(), "Statement_CB", "DR", lastBillingDatesAndEodId.getStatementEndDate());

            //sql = "INSERT INTO TEMPTRANSACTIONDETAILS(ACCOUNTNO, EODID, TRANSACTIONID, AMOUNT, TXNTYPE, TXNCHECKDATE, CRDR) SELECT ACCOUNTNO, EODID, TRANSACTIONID, TRANSACTIONAMOUNT, TRANSACTIONTYPE, SETTLEMENTDATE, CRDR FROM EODTRANSACTION WHERE EODID > ? AND EODID <= ? AND ACCOUNTNO = ? AND ADJUSTMENTSTATUS = 'NO' AND TRANSACTIONTYPE IN (?,?,?,?) AND STATUS NOT IN (?) ORDER BY SETTLEMENTDATE";
            sql = queryParametersList.getEOMInterest_getEOMInterest_Appender7();
            backendJdbcTemplate.update(sql,
                    lastBillingDatesAndEodId.getEndEodId(),
                    Configurations.EOD_ID,
                    eomCardBean.getAccNo(),
                    Configurations.TXN_TYPE_PAYMENT,
                    Configurations.TXN_TYPE_REVERSAL,
                    Configurations.TXN_TYPE_REFUND,
                    Configurations.TXN_TYPE_MVISA_REFUND,
                    statusList.getCHEQUE_RETURN_STATUS());
            //sql = "INSERT INTO TEMPTRANSACTIONDETAILS(ACCOUNTNO, EODID, TRANSACTIONID, AMOUNT, TXNTYPE, TXNCHECKDATE, CRDR) SELECT ?, ?, ID, AMOUNT, TRANSACTIONTYPE, ADJUSTDATE, CRDR FROM ADJUSTMENT WHERE ADJUSTDATE <= TO_DATE(?, 'dd-MM-YY') AND ADJUSTDATE > TO_DATE(?, 'dd-MM-YY') AND CRDR IN(?) AND STATUS = ? AND EODSTATUS = ? AND ADJUSTMENTTYPE NOT IN (?,?) AND uniqueid IN (SELECT CA.CARDNUMBER FROM CARDACCOUNTCUSTOMER CA, CARD CD WHERE CA.cardnumber = CD.CARDNUMBER AND CA.ACCOUNTNO = ?) ORDER BY ADJUSTDATE";
            sql= queryParametersList.getEOMInterest_getEOMInterest_Appender8();
            backendJdbcTemplate.update(sql,
                    eomCardBean.getAccNo(),
                    Configurations.EOD_ID,
                    sdf.format(Configurations.EOD_DATE),
                    sdf.format(lastBillingDatesAndEodId.getStatementEndDate()),
                    Configurations.CREDIT,
                    statusList.getMANUAL_ADJUSTMENT_ACCEPT(),
                    Configurations.EOD_DONE_STATUS,
                    Integer.toString(Configurations.LOYALTY_ADJUSTMENT_TYPE),
                    Integer.toString(Configurations.CASHBACK_ADJUSTMENT_TYPE),
                    eomCardBean.getAccNo());

            count = this.insertIntoTempTxnDetails(eomCardBean.getAccNo(), Configurations.EOD_ID, "-1", 0.0, "Day End", "DR", Configurations.EOD_DATE);

            //sql = "SELECT ACCOUNTNO, AMOUNT, CRDR, CREATEDTIME, EODID, LASTUPDATEDTIME, LASTUPDATEDUSER, TRANSACTIONID, TXNCHECKDATE, TXNTYPE FROM TEMPTRANSACTIONDETAILS WHERE ACCOUNTNO = ? ORDER BY TXNCHECKDATE ASC";
            sql = queryParametersList.getEOMInterest_getEOMInterest_Select1();
            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    int key = 0;
                    ArrayList<Object> data;
                    data = new ArrayList<Object>();
                    data.add(rs.getDouble("AMOUNT"));
                    data.add(rs.getString("CRDR"));
                    data.add(rs.getDate("CREATEDTIME"));
                    data.add(rs.getInt("EODID"));
                    data.add(rs.getString("LASTUPDATEDTIME"));
                    data.add(rs.getString("LASTUPDATEDUSER"));
                    data.add(rs.getString("TRANSACTIONID"));
                    data.add(rs.getDate("TXNCHECKDATE"));
                    data.add(rs.getString("TXNTYPE"));
                    data.add(rs.getString("ACCOUNTNO"));
                    lhMap.put(key, data);
                    key++;
                }
            }, eomCardBean.getAccNo());

            count = this.clearTempTxnDetails(eomCardBean.getAccNo());
            for (Map.Entry<Integer, ArrayList<Object>> entrySet : lhMap.entrySet()) {
                Integer key1 = entrySet.getKey();
                ArrayList<Object> value = entrySet.getValue();
                this.insertIntoTempTxnDetails(value.get(9).toString(), (Integer) value.get(3), value.get(6).toString(), (Double) value.get(0), value.get(8).toString(), value.get(1).toString(), (Date) value.get(7));
                System.out.println(value.get(8).toString());
            }
            lhMap.clear();

            //sql = "SELECT TO_DATE(TXNCHECKDATE,'dd-MM-YY') AS TXNCHECKDATE from TEMPTRANSACTIONDETAILS WHERE ACCOUNTNO = ? ORDER BY TXNCHECKDATE ASC";
            sql = queryParametersList.getEOMInterest_getEOMInterest_Select2();
            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    dateList.add(rs.getDate("TXNCHECKDATE"));
                }
                return dateList;
            }, eomCardBean.getAccNo());

            for (int i = 0; i < dateList.size() - 1; i++) {//calculate the date gap for the transaction and save them in int array
                dateGap.add(CommonMethods.getNoOfDaysDifference(dateList.get(i), dateList.get(i + 1)));
            }

            dateGap.add(0);

            //sql = "SELECT AMOUNT,TXNTYPE,CRDR FROM TEMPTRANSACTIONDETAILS WHERE ACCOUNTNO = ? ORDER BY TXNCHECKDATE ASC";
            sql = queryParametersList.getEOMInterest_getEOMInterest_Select3();
            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    double amount;
                    String trnsactionType;
                    double interestAmount = 0.0;
                    String crDr;
                    int dayGapNo = 0;
                    double interestRate = eomCardBean.getInterestRate();
                    double interestPeriod = eomCardBean.getInterestPeriod();
                    MathContext mc = new MathContext(20, RoundingMode.DOWN);
                    BigDecimal totalInterest1 = new BigDecimal("0.0");
                    BigDecimal interestT;
                    amount = rs.getDouble("AMOUNT");
                    trnsactionType = rs.getString("TXNTYPE");
                    crDr = rs.getString("CRDR");

                    if (lastBillingDatesAndEodId.getThisBillingOpeningBalance() > 0 || lastBillingDatesAndEodId.getStartEodId() == 0) { //debit calculation.

                        if (!((trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REFUND) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_REFUND))) && !trnsactionType.equalsIgnoreCase("Statement_OB") && !trnsactionType.equalsIgnoreCase("Statement_CB")) {

                            if (crDr.equalsIgnoreCase(Configurations.DEBIT)) {
                                interestAmount += amount;
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);

                            } else if (crDr.equalsIgnoreCase(Configurations.CREDIT)) {
                                interestAmount -= amount;

                                if (interestAmount > 0) {
                                    interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                    totalInterest1 = totalInterest1.add(interestT);

                                } else {//consider the payment which are going -
                                    interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                    totalInterest1 = totalInterest1.add(interestT);
                                }
                            }
                            dayGapNo++;

                        } else if (((trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REFUND) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_REFUND))) && !trnsactionType.equalsIgnoreCase("Statement_OB") && !trnsactionType.equalsIgnoreCase("Statement_CB")) {

                            if (crDr.equalsIgnoreCase(Configurations.DEBIT)) {
                                interestAmount += amount;
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);

                            } else if (crDr.equalsIgnoreCase(Configurations.CREDIT)) {
                                interestAmount -= amount;

                                if (interestAmount > 0) {
                                    interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                    totalInterest1 = totalInterest1.add(interestT);

                                } else {//consider the payment which are going
                                    interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                    totalInterest1 = totalInterest1.add(interestT);
                                }
                            }
                            dayGapNo++;

                        } else if (!((trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REFUND) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_REFUND))) && trnsactionType.equalsIgnoreCase("Statement_OB") && !trnsactionType.equalsIgnoreCase("Statement_CB")) {
                            /**
                             * from this day onward we have to calculate interest
                             * for total opening balance amount
                             */
                            interestAmount = amount;
                            interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                            totalInterest1 = totalInterest1.add(interestT);
                            dayGapNo++;

                        } else if (trnsactionType.equalsIgnoreCase("Statement_CB")) {
                            /**
                             * from this day onward we have to calculate interest
                             * for total closing balance amount
                             */
                            interestAmount = amount;
                            interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                            totalInterest1 = totalInterest1.add(interestT);
                            dayGapNo++;
                        }

                    } else if (!((trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REFUND) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_REFUND))) && !trnsactionType.equalsIgnoreCase("Statement_OB") && !trnsactionType.equalsIgnoreCase("Statement_CB")) {

                        if (crDr.equalsIgnoreCase(Configurations.DEBIT)) {
                            interestAmount += amount;

                            if (interestAmount > 0) {
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);

                            } else {//consider the payment which are going -
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);
                            }

                        } else if (crDr.equalsIgnoreCase(Configurations.CREDIT)) {
                            interestAmount -= amount;

                            if (interestAmount > 0) {
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);

                            } else {//consider the payment which are going -
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);
                            }
                        }
                        dayGapNo++;

                    } else if (((trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REFUND) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_REFUND))) && !trnsactionType.equalsIgnoreCase("Statement_OB") && !trnsactionType.equalsIgnoreCase("Statement_CB")) {

                        if (crDr.equalsIgnoreCase(Configurations.DEBIT)) {
                            interestAmount += amount;

                            if (interestAmount > 0) {
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);

                            } else {//consider the payment which are going -
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);
                            }

                        } else if (crDr.equalsIgnoreCase(Configurations.CREDIT)) {
                            interestAmount -= amount;

                            if (interestAmount > 0) {
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);

                            } else {//consider the payment which are going -
                                interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                                totalInterest1 = totalInterest1.add(interestT);
                            }
                        }
                        dayGapNo++;

                    } else if (!((trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_PAYMENT) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REVERSAL) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_REFUND) || trnsactionType.equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_REFUND))) && trnsactionType.equalsIgnoreCase("Statement_OB") && !trnsactionType.equalsIgnoreCase("Statement_CB")) {
                        /**
                         * from this day onward we have to calculate interest for
                         * total closing balance amount
                         */
                        interestAmount = amount;
                        interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                        totalInterest1 = totalInterest1.add(interestT);
                        dayGapNo++;

                    } else if (trnsactionType.equalsIgnoreCase("Statement_CB")) {
                        /**
                         * from this day onward we have to calculate interest for
                         * total closing balance amount
                         */
                        interestAmount = amount;
                        interestT = new BigDecimal((interestAmount * interestRate * dateGap.get(dayGapNo) / interestPeriod / 100), mc);
                        totalInterest1 = totalInterest1.add(interestT);
                        dayGapNo++;
                    }
                }
            }, eomCardBean.getAccNo());

            //sql = "SELECT AMOUNT FROM TEMPTRANSACTIONDETAILS WHERE TXNCHECKDATE>TO_DATE(?, 'DD-MM-YY') AND TXNCHECKDATE<=TO_DATE(?, 'DD-MM-YY') AND ACCOUNTNO = ? AND CRDR = ?";
            sql = queryParametersList.getEOMInterest_getEOMInterest_Select4();
            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    BigDecimal totalPaymentBeforeDueDate = new BigDecimal("0.0");
                    BigDecimal payAmount = new BigDecimal(rs.getString("AMOUNT"));
                    totalPaymentBeforeDueDate = totalPaymentBeforeDueDate.add(payAmount);
                }
            }, sdf.format(lastBillingDatesAndEodId.getStatementEndDate()), sdf.format(lastBillingDatesAndEodId.getDueDate()), eomCardBean.getAccNo(), Configurations.CREDIT);

            MathContext mc = new MathContext(20, RoundingMode.DOWN);
            BigDecimal totalInterest1 = new BigDecimal("0.0");
            BigDecimal interestT;
            BigDecimal totalPaymentBeforeDueDate = new BigDecimal("0.0");

            if (totalPaymentBeforeDueDate.doubleValue() < lastBillingDatesAndEodId.getThisBillingClosingBalance()) {

                if (Double.parseDouble(ValuesRoundup(totalInterest1.doubleValue())) < 0) {
                    totalInterest1 = new BigDecimal("0.00");
                }
                this.insertIntoEomInterest(eomCardBean.getCardNo(), eomCardBean.getAccNo(), Double.parseDouble(ValuesRoundup(totalInterest1.doubleValue())), Double.parseDouble(ValuesRoundup(totalInterest1.doubleValue())), Configurations.EOD_ID, statusList.getEOD_PENDING_STATUS());
                interest = Double.parseDouble(totalInterest1.toString());

                if (!eomCardBean.getAccStatus().equalsIgnoreCase(statusList.getACCOUNT_NON_PERFORMING_STATUS())) {
                    this.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, eomCardBean.getCardNo(), Configurations.TXN_TYPE_INTEREST_INCOME, Double.parseDouble(ValuesRoundup(totalInterest1.doubleValue())), Configurations.DEBIT, null);

                } else {
                    this.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, eomCardBean.getCardNo(), Configurations.NP_ACCRUED_INTEREST_GL, Double.parseDouble(ValuesRoundup(totalInterest1.doubleValue())), Configurations.DEBIT, null);
                    this.updateDELINQUENTACCOUNTnpdetails(Double.parseDouble(ValuesRoundup(totalInterest1.doubleValue())), 0.0, 0.0, 0.0, eomCardBean.getAccNo());
                }

            } else {
                this.insertIntoEomInterest(eomCardBean.getCardNo(), eomCardBean.getAccNo(), 0.0, Double.parseDouble(ValuesRoundup(totalInterest1.doubleValue())), Configurations.EOD_ID, statusList.getEOD_PENDING_STATUS());
                interest = 0;
                this.insertIntoEodGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, eomCardBean.getCardNo(), Configurations.TXN_TYPE_INTEREST_INCOME, 0.0, Configurations.DEBIT, null);
            }

            logDetails.add(lsatBillOpeningBalance);
            logDetails.add(Double.parseDouble(ValuesRoundup(interest)));

        } catch (Exception e) {
            throw e;
        }
        return logDetails;
    }

    public int clearTempTxnDetails(String accNo) {
        int count = 0;
        String query = null;
        try {
            //query = "DELETE FROM TEMPTRANSACTIONDETAILS WHERE ACCOUNTNO = ?";
            query = queryParametersList.getEOMInterest_getEOMInterest_Delete();
            count = backendJdbcTemplate.update(query,
                    accNo
                    );
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    public int insertIntoTempTxnDetails(String accNo, int eodId, String txnId, double txnAmount, String txnType, String crDr, java.util.Date txnDate) throws Exception {
        int count = 0;
        String sql = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            //sql = "insert into TEMPTRANSACTIONDETAILS (ACCOUNTNO, EODID,TRANSACTIONID,AMOUNT,TXNTYPE,TXNCHECKDATE,CRDR) values(?,?,?,?,?,TO_DATE(?, 'DD-MM-YY'),?)";
            sql= queryParametersList.getEOMInterest_insertIntoTempTxnDetails();
            count = backendJdbcTemplate.update(sql,
                    accNo,
                    eodId,
                    txnId,
                    txnAmount,
                    txnType,
                    sdf.format(txnDate),
                    crDr
                    );
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    public int updateDELINQUENTACCOUNTnpdetails(double accruedInterest, double accruedOverLimitFees, double accruedLatePayFees, double otherFees, String accNo) throws Exception {
        int count = 0;
        double totalFees = 0;
        try {
            totalFees = otherFees + accruedLatePayFees + accruedOverLimitFees;
            //String query = "UPDATE DELINQUENTACCOUNT SET NPACCRUEDINTEREST = NPACCRUEDINTEREST + ?, NPACCRUEDOVERLIMITFEES = NPACCRUEDOVERLIMITFEES + ?, NPACCRUEDLATEPAYFEES = NPACCRUEDLATEPAYFEES + ?, NPACCRUEDFEES = NPACCRUEDFEES + ? WHERE ACCOUNTNO = ?";
            count = backendJdbcTemplate.update(queryParametersList.getEOMInterest_updateDELINQUENTACCOUNTnpdetails(),
                    accruedInterest,
                    accruedOverLimitFees,
                    accruedLatePayFees,
                    totalFees,
                    accNo
                    );
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public int insertIntoEomInterest(StringBuffer cardNo, String accNO, double FORWARDINTEREST, double INTERESTAMOUNT, int eodId, String status) throws Exception {
        int count = 0;
        try {
            //String sql = "insert into EOMINTEREST (CARDNO,FORWARDINTEREST,INTERESTAMOUNT,EODID,ACCOUNTNO,STATUS) values (?,?,?,?,?,?)";
            count = backendJdbcTemplate.update(queryParametersList.getEOMInterest_insertIntoEomInterest(), cardNo, FORWARDINTEREST, INTERESTAMOUNT, eodId, accNO, status);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertIntoEodGLAccount(int eodID, Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            //String sql = "INSERT INTO EODGLACCOUNT (EODID,GLDATE,CARDNO,GLTYPE,AMOUNT,CRDR,PAYMENTTYPE) VALUES (?,TO_DATE(?, 'DD-MM-YY'),?,?,to_char(?,'9999999999.99'),?,?)";

            count = backendJdbcTemplate.update(queryParametersList.getEOMInterest_insertIntoEodGLAccount(),
                    eodID,
                    sdf.format(glDate),
                    cardNo,
                    glType,
                    String.valueOf(amount),
                    cdStatus,
                    payType);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
