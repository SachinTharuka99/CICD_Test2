package com.epic.cms.repository;

import com.epic.cms.dao.CashBackDao;
import com.epic.cms.model.bean.CashBackBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.epic.cms.util.CommonMethods.ValuesRoundup;

@Repository
public class CashBackRepo implements CashBackDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    LogManager logManager;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public ProcessBean getProcessDetails(int processId) throws Exception {
        SimpleDateFormat sdf = null;
        String DATE_FORMAT = null;
        Calendar cal = null;
        ProcessBean processBean = null;

        cal = Calendar.getInstance(TimeZone.getDefault());

        DATE_FORMAT = "MM/dd/yyyy hh:mm:ss aaa";

        sdf = new SimpleDateFormat(DATE_FORMAT);

        sdf.setTimeZone(TimeZone.getDefault());

        try {

            //String query = "SELECT PROCESSID,DESCRIPTION,CRITICALSTATUS,ROLLBACKSTATUS,SHEDULEDATE,SHEDULETIME,FREQUENCYTYPE,CONTINUESFREQUENCYTYPE,CONTINUESFREQUENCY,MULTIPLECYCLESTATUS,PROCESSCATEGORYID,DEPENDANCYSTATUS,RUNNINGONMAIN,RUNNINGONSUB,PROCESSTYPE,STATUS,SHEDULEDATETIME,HOLIDAYACTION FROM EODPROCESS WHERE PROCESSID = ? ";//AND SHEDULEDATETIME <= to_date(?,'MM/dd/YYYY HH:mi:ss AM') ";
            processBean = backendJdbcTemplate.queryForObject(queryParametersList.getCashBack_getProcessDetails(), new RowMapper<>() {
                @Override
                public ProcessBean mapRow(ResultSet result, int rowNum) throws SQLException {
                    ProcessBean processDetails = new ProcessBean();

                    processDetails.setProcessId(result.getInt("PROCESSID"));
                    processDetails.setProcessDes(result.getString("DESCRIPTION"));
                    processDetails.setCriticalStatus(result.getInt("CRITICALSTATUS"));
                    processDetails.setRollBackStatus(result.getInt("ROLLBACKSTATUS"));
                    processDetails.setSheduleDate(result.getTimestamp("SHEDULEDATETIME"));
                    processDetails.setSheduleTime(result.getString("SHEDULETIME"));
                    processDetails.setFrequencyType(result.getInt("FREQUENCYTYPE"));
                    processDetails.setContinuousFrequencyType(result.getInt("CONTINUESFREQUENCYTYPE"));
                    processDetails.setContinuousFrequency(result.getInt("CONTINUESFREQUENCY"));
                    processDetails.setMultiCycleStatus(result.getInt("MULTIPLECYCLESTATUS"));
                    processDetails.setProcessCategoryId(result.getInt("PROCESSCATEGORYID"));
                    processDetails.setDependancyStatus(result.getInt("DEPENDANCYSTATUS"));
                    processDetails.setRunningOnMain(result.getInt("RUNNINGONMAIN"));
                    processDetails.setRunningOnSub(result.getInt("RUNNINGONSUB"));
                    processDetails.setProcessType(result.getInt("PROCESSTYPE"));
                    processDetails.setStatus(result.getString("STATUS"));
                    processDetails.setHolidayAction(result.getInt("HOLIDAYACTION"));
                    return processDetails;
                }
            }, processId);

        } catch (EmptyResultDataAccessException e) {
            return processBean;
        } catch (Exception e) {
            throw e;
        }
        return processBean;
    }

    @Override
    public void loadInitialConfigurationsForCashback() throws Exception {
        try {
            //String query = "SELECT CBREDEEMDAYCOUNT FROM COMMONPARAMETER";

            backendJdbcTemplate.query(queryParametersList.getCashBack_loadInitialConfigurationsForCashback(), (ResultSet rs) -> {
                while (rs.next()) {
                    Configurations.CBREDEEMDAYCOUNT = rs.getInt("CBREDEEMDAYCOUNT");
                }
            });
            backendJdbcTemplate.query(queryParametersList.getCashBack_loadInitialConfigurationsForCashback(),
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            Configurations.CBREDEEMDAYCOUNT = rs.getInt("CBREDEEMDAYCOUNT");
                        }
                    }
            );

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<CashBackBean> getEligibleAccountsForCashback() throws Exception {
        List<CashBackBean> accountList = new ArrayList<>();

        String query = "SELECT CA.ACCOUNTNO,CA.STATUS AS ACCOUNTSTATUS,TRUNC(CA.NEXTBILLINGDATE) AS NEXTBILLINGDATE,CBP.CREDITOPTION, CBP.MINACCUMULATEDTOCLAIM,CA.CASHBACKPROFILECODE,CA.CASHBACKSTARTDATE,ADD_MONTHS(CA.CASHBACKSTARTDATE,12) AS NEXTCASHBACKSTARTDATE, CA.AVLCASHBACKAMOUNT,NVL(CBP.CASHBACKRATE,0) AS CASHBACKRATE,CBP.MAXCASHBACKPERYEAR,CBP.MINSPENDPERMONTH,CBP.EXPIRYPERIOD AS EXPIRYPERIOD, TRUNC(CA.LASTCASHBACKDATE) AS LASTCASHBACKDATE,CA.CARDNUMBER AS MAINCARDNUMBER,C.CARDSTATUS,CA.NEXTCBREDEEMDATE,CBP.REDEEMRATIO FROM CARDACCOUNT CA LEFT JOIN CASHBACKPROFILE CBP ON CA.CASHBACKPROFILECODE=CBP.PROFILECODE LEFT JOIN CARD C ON CA.CARDNUMBER=C.CARDNUMBER WHERE CASHBACKPROFILECODE IS NOT NULL AND CA.CASHBACKSTARTDATE IS NOT NULL";
        //String query = queryParametersList.getCashBack_getEligibleAccountsForCashback();


        // INIT == INIT
        if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
            query += " and CA.ACCOUNTNO not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? )";
            //query+= queryParametersList.getCashBack_getEligibleAccountsForCashback_appender1();


            // INIT == EROR
        } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
            query += " and CA.ACCOUNTNO in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? and EODID < ? and PROCESSSTEPID <= ? )";
            //query+= queryParametersList.getCashBack_getEligibleAccountsForCashback_appender2();

        }

        try {
            // INIT == INIT
            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                accountList = backendJdbcTemplate.query(query, new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                    CashBackBean cashbackbean = new CashBackBean();
                    cashbackbean.setAccountNumber(rs.getString("ACCOUNTNO"));
                    cashbackbean.setAccountStatus(rs.getString("ACCOUNTSTATUS"));
                    cashbackbean.setMainCardNumber(new StringBuffer(rs.getString("MAINCARDNUMBER")));
                    cashbackbean.setMainCardStatus(rs.getString("CARDSTATUS"));
                    cashbackbean.setStatementDate(rs.getDate("NEXTBILLINGDATE"));
                    cashbackbean.setNextCBRedeemDate(rs.getDate("NEXTCBREDEEMDATE"));
                    cashbackbean.setCashbackProfileCode(rs.getString("CASHBACKPROFILECODE"));
                    cashbackbean.setCashbackExpiryPeriod(rs.getInt("EXPIRYPERIOD"));
                    cashbackbean.setMinAccumulatedToClaim(rs.getDouble("MINACCUMULATEDTOCLAIM"));
                    cashbackbean.setCreditOption(rs.getString("CREDITOPTION"));
                    cashbackbean.setMaxCashbackPerYear(rs.getDouble("MAXCASHBACKPERYEAR"));

                    cashbackbean.setCashbackStartDate(rs.getDate("CASHBACKSTARTDATE"));
                    cashbackbean.setNextCashbackStartDate(rs.getDate("NEXTCASHBACKSTARTDATE"));
                    cashbackbean.setAvailableCashbackAmount(new BigDecimal(rs.getString("AVLCASHBACKAMOUNT")));

                    cashbackbean.setMinSpendPerMonth(rs.getDouble("MINSPENDPERMONTH"));
                    cashbackbean.setCashbackRate(rs.getDouble("CASHBACKRATE"));
                    cashbackbean.setLastCashbackDate(rs.getDate("LASTCASHBACKDATE"));
                    cashbackbean.setRedeemRatio(rs.getDouble("REDEEMRATIO"));
                    return cashbackbean;
                }), statusList.getEOD_PENDING_STATUS());

            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                accountList = backendJdbcTemplate.query(query, new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                    CashBackBean cashbackbean = new CashBackBean();
                    cashbackbean.setAccountNumber(rs.getString("ACCOUNTNO"));
                    cashbackbean.setAccountStatus(rs.getString("ACCOUNTSTATUS"));
                    cashbackbean.setMainCardNumber(new StringBuffer(rs.getString("MAINCARDNUMBER")));
                    cashbackbean.setMainCardStatus(rs.getString("CARDSTATUS"));
                    cashbackbean.setStatementDate(rs.getDate("NEXTBILLINGDATE"));
                    cashbackbean.setNextCBRedeemDate(rs.getDate("NEXTCBREDEEMDATE"));
                    cashbackbean.setCashbackProfileCode(rs.getString("CASHBACKPROFILECODE"));
                    cashbackbean.setCashbackExpiryPeriod(rs.getInt("EXPIRYPERIOD"));
                    cashbackbean.setMinAccumulatedToClaim(rs.getDouble("MINACCUMULATEDTOCLAIM"));
                    cashbackbean.setCreditOption(rs.getString("CREDITOPTION"));
                    cashbackbean.setMaxCashbackPerYear(rs.getDouble("MAXCASHBACKPERYEAR"));

                    cashbackbean.setCashbackStartDate(rs.getDate("CASHBACKSTARTDATE"));
                    cashbackbean.setNextCashbackStartDate(rs.getDate("NEXTCASHBACKSTARTDATE"));
                    cashbackbean.setAvailableCashbackAmount(new BigDecimal(rs.getString("AVLCASHBACKAMOUNT")));

                    cashbackbean.setMinSpendPerMonth(rs.getDouble("MINSPENDPERMONTH"));
                    cashbackbean.setCashbackRate(rs.getDouble("CASHBACKRATE"));
                    cashbackbean.setLastCashbackDate(rs.getDate("LASTCASHBACKDATE"));
                    cashbackbean.setRedeemRatio(rs.getDouble("REDEEMRATIO"));
                    return cashbackbean;
                }), statusList.getEOD_PENDING_STATUS(), Configurations.ERROR_EOD_ID, Configurations.PROCESS_STEP_ID);
            }
        } catch (Exception e) {
            throw e;
        }
        return accountList;
    }

    @Override
    public BigDecimal getCashbackAmount(CashBackBean cashbackBean) {
        BigDecimal calculatedCashBackAmt = null;
        List<Date> statementDateList = new ArrayList<>();

        try {
            //get statement dates for given date range(from lastcashbackdate to SYSDATE)
            String query = "SELECT TRUNC(STATEMENTENDDATE) AS STATEMENTENDDATE FROM BILLINGSTATEMENT BS WHERE BS.MAINCARDNO=? AND TRUNC(STATEMENTENDDATE)>=TRUNC(?) ORDER BY STATEMENTENDDATE";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            statementDateList.add(rs.getDate("STATEMENTENDDATE"));
                        }
                        return statementDateList;
                    },
                    cashbackBean.getMainCardNumber().toString(),
                    DateUtil.getSqldate(cashbackBean.getLastCashbackDate())
            );
            statementDateList.add(DateUtil.getSqldate(Configurations.EOD_DATE)); //add eoddate as last element of the list

            int listSize = statementDateList.size();

            if (listSize == 1) { //no previous statement dates for given last cashback date

                query = "SELECT (CASE WHEN SUM(ET.TRANSACTIONAMOUNT)> ? THEN SUM(ET.TRANSACTIONAMOUNT * ? / 100) ELSE 0 END) AS CASHBACKAMOUNT FROM EODTRANSACTION ET WHERE ET.STATUS=? AND TRUNC(TO_DATE(SETTLEMENTDATE,'DD-MM-YY')) > TRUNC(?) AND TRUNC(TO_DATE(SETTLEMENTDATE,'DD-MM-YY')) <= TO_DATE(?,'DD-MM-YY') AND ET.ORIGINALTRANSACTIONTYPE NOT IN(SELECT CBPT.TXNTYPECODE FROM CASHBACKPROFILETXNTYPE CBPT WHERE CBPT.PROFILECODE=?) AND ET.MCC NOT IN(SELECT CBPM.MCCCODE FROM CASHBACKPROFILEMCC CBPM WHERE CBPM.PROFILECODE=?) AND ET.ACCOUNTNO=? AND ET.TRANSACTIONID NOT IN(SELECT TXNID FROM EASYPAYMENTREQUEST WHERE STATUS IN(?,?)) AND ET.ADJUSTMENTSTATUS='NO'";

                calculatedCashBackAmt = Objects.requireNonNull(backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            BigDecimal temp = null;
                            while (rs.next()) {
                                temp = BigDecimal.valueOf(rs.getDouble("CASHBACKAMOUNT"));
                            }
                            return temp;
                        }, cashbackBean.getMinSpendPerMonth(), //total transaction amount must be greater than minimum spend per month value
                        cashbackBean.getCashbackRate(),      //cashback percentage
                        statusList.getEOD_DONE_STATUS(), DateUtil.getSqldate(cashbackBean.getLastCashbackDate()),   //date the last cashback calculated
                        Configurations.EOD_DATE_String, cashbackBean.getCashbackProfileCode(),       // To exclude TXN Type list
                        cashbackBean.getCashbackProfileCode(),       // To exclude MCC list
                        cashbackBean.getAccountNumber(), statusList.getCOMMON_REQUEST_ACCEPTED(), statusList.getCOMMON_COMPLETED()));

            } else { // many statement dates need to consider for given last cashback date. cash back will calculate statement months separately and then accumilate

                //for first range (from lastcashbackdate to first statement date)
                query = "SELECT (CASE WHEN SUM(ET.TRANSACTIONAMOUNT)> ? THEN SUM(ET.TRANSACTIONAMOUNT * ? / 100) ELSE 0 END) AS CASHBACKAMOUNT FROM EODTRANSACTION ET WHERE ET.STATUS=? AND TRUNC(TO_DATE(SETTLEMENTDATE,'DD-MM-YY')) > TRUNC(?) AND  TRUNC(TO_DATE(SETTLEMENTDATE,'DD-MM-YY')) <= TRUNC(?) AND ET.ORIGINALTRANSACTIONTYPE NOT IN(SELECT CBPT.TXNTYPECODE FROM CASHBACKPROFILETXNTYPE CBPT WHERE CBPT.PROFILECODE=?) AND ET.MCC NOT IN(SELECT CBPM.MCCCODE FROM CASHBACKPROFILEMCC CBPM WHERE CBPM.PROFILECODE=?) AND ET.ACCOUNTNO=? AND ET.TRANSACTIONID NOT IN(SELECT TXNID FROM EASYPAYMENTREQUEST WHERE STATUS IN(?,?)) AND ET.ADJUSTMENTSTATUS='NO'";

                calculatedCashBackAmt = Objects.requireNonNull(backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            BigDecimal temp = null;
                            while (rs.next()) {
                                temp = BigDecimal.valueOf(rs.getDouble("CASHBACKAMOUNT"));
                            }
                            return temp;
                        }, cashbackBean.getMinSpendPerMonth(), //total transaction amount must be greater than minimum spend per month value
                        cashbackBean.getCashbackRate(),      //cashback percentage
                        statusList.getEOD_DONE_STATUS(), //EDON
                        DateUtil.getSqldate(cashbackBean.getLastCashbackDate()),   //date the last cashback calculated
                        statementDateList.get(1), cashbackBean.getCashbackProfileCode(),       // To exclude TXN Type list
                        cashbackBean.getCashbackProfileCode(),       // To exclude MCC list
                        cashbackBean.getAccountNumber(), statusList.getCOMMON_REQUEST_ACCEPTED(), //RQAC
                        statusList.getCOMMON_COMPLETED() //COMP
                ));

                //from other statement months
                for (int i = 1; i < listSize - 1; i++) {

                    query = "SELECT (CASE WHEN SUM(ET.TRANSACTIONAMOUNT)> ? THEN SUM(ET.TRANSACTIONAMOUNT * ? / 100) ELSE 0 END) AS CASHBACKAMOUNT FROM EODTRANSACTION ET WHERE ET.STATUS=? AND TRUNC(TO_DATE(SETTLEMENTDATE,'DD-MM-YY')) > TRUNC(?) AND  TRUNC(TO_DATE(SETTLEMENTDATE,'DD-MM-YY')) <= TRUNC(?) AND ET.ORIGINALTRANSACTIONTYPE NOT IN(SELECT CBPT.TXNTYPECODE FROM CASHBACKPROFILETXNTYPE CBPT WHERE CBPT.PROFILECODE=?) AND ET.MCC NOT IN(SELECT CBPM.MCCCODE FROM CASHBACKPROFILEMCC CBPM WHERE CBPM.PROFILECODE=?) AND ET.ACCOUNTNO=? AND ET.TRANSACTIONID NOT IN(SELECT TXNID FROM EASYPAYMENTREQUEST WHERE STATUS IN(?,?)) AND ET.ADJUSTMENTSTATUS='NO'";

                    calculatedCashBackAmt = Objects.requireNonNull(backendJdbcTemplate.query(query,
                            (ResultSet rs) -> {
                                BigDecimal temp = new BigDecimal(0.00);
                                while (rs.next()) {
                                    temp = temp.add(BigDecimal.valueOf(rs.getDouble("CASHBACKAMOUNT"))); //calculated total cashback amount for this statement cycle
                                }
                                return temp;
                            }, cashbackBean.getMinSpendPerMonth(), //total transaction amount must be greater than minimum spend per month value
                            cashbackBean.getCashbackRate(),      //cashback percentage
                            statusList.getEOD_DONE_STATUS(), statementDateList.get(1), statementDateList.get(i + 1), cashbackBean.getCashbackProfileCode(),       // To exclude TXN Type list
                            cashbackBean.getCashbackProfileCode(),       // To exclude MCC list
                            cashbackBean.getAccountNumber(), statusList.getCOMMON_REQUEST_ACCEPTED(), statusList.getCOMMON_COMPLETED()));
                }
            }
        } catch (EmptyResultDataAccessException e) {
            return new BigDecimal(0);
        } catch (Exception e) {
            throw e;
        }
        return calculatedCashBackAmt;
    }

    @Override
    public BigDecimal getCashbackAdjustmentAmount(CashBackBean cashbackBean) {
        BigDecimal adjustmentAmount = new BigDecimal(BigInteger.ZERO);

        try {
            //select cashback adjustement with given card number
            //String query = "SELECT NVL(SUM(CASE CRDR WHEN 'DR' THEN -AMOUNT ELSE AMOUNT END),0) AS TOTAL FROM ADJUSTMENT WHERE ADJUSTMENTTYPE=? AND EODSTATUS=? AND STATUS=? AND UNIQUEID IN(SELECT CARDNUMBER FROM CARDACCOUNTCUSTOMER WHERE ACCOUNTNO=?) ";

            double cashbackAdjustmentAmount = backendJdbcTemplate.queryForObject(queryParametersList.getCashBack_getCashbackAdjustmentAmount(), Double.class, Configurations.CASHBACK_ADJUSTMENT_TYPE, //9
                    Configurations.EOD_PENDING_STATUS, //EPEN
                    statusList.getMANUAL_ADJUSTMENT_ACCEPT(), //MAAC
                    cashbackBean.getAccountNumber());

            adjustmentAmount = new BigDecimal(cashbackAdjustmentAmount);

        } catch (EmptyResultDataAccessException e) {
            return new BigDecimal(0);
        } catch (Exception e) {
            throw e;
        }
        return adjustmentAmount;
    }

    @Override
    public int addNewCashBack(CashBackBean cashbackBean, BigDecimal cashbackAmount, BigDecimal cashbackAdjustmentAmount, String txnVolume) {
        int count = 0;
        String roundedCashbackAmount = cashbackAmount.setScale(2, RoundingMode.FLOOR).toString();
        String roundedAdjustedAmount = cashbackAdjustmentAmount.setScale(2, RoundingMode.FLOOR).toString();

        java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);

        try {
            //insert cashback amount in CASHBACK table
            //String query = "INSERT INTO CASHBACK (CASHBACKAMOUNT,EODID,ACCOUNTNUMBER,CREATEDTIME,LASTUPDATEDTIME,EODDATE,FROMDATE,TODATE,TXNVOLUME,ADJUSTEDAMOUNT) VALUES (?,?,?,SYSDATE,SYSDATE,?,?,?,?,?)";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_addNewCashBack_Insert(),
                    roundedCashbackAmount,
                    Configurations.EOD_ID,
                    cashbackBean.getAccountNumber(),
                    eodDate,
                    DateUtil.getSqldate(cashbackBean.getLastCashbackDate()),
                    eodDate,
                    txnVolume,
                    roundedAdjustedAmount
            );

            // increase available cashback amount and LASTCASHBACKDATE in CARDACCOUNT table
            //query = "UPDATE CARDACCOUNT SET AVLCASHBACKAMOUNT=AVLCASHBACKAMOUNT+?,LASTCASHBACKDATE=? WHERE ACCOUNTNO=?";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_addNewCashBack_Update(), roundedAdjustedAmount, eodDate, cashbackBean.getAccountNumber());
            count = backendJdbcTemplate.update(queryParametersList.getCashBack_addNewCashBack_Update(),
                    roundedAdjustedAmount,
                    eodDate,
                    cashbackBean.getAccountNumber()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCashbackAdjustmentStatus(String accountNumber, String billing_done_status) {
        int count = 0;

        try {
            //String query = "UPDATE ADJUSTMENT SET EODSTATUS = ? WHERE UNIQUEID IN(SELECT CARDNUMBER FROM CARDACCOUNTCUSTOMER WHERE ACCOUNTNO=?) AND EODSTATUS=? AND ADJUSTMENTTYPE=?";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_updateCashbackAdjustmentStatus(),
                    billing_done_status,
                    accountNumber,
                    Configurations.EOD_PENDING_STATUS,
                    Configurations.CASHBACK_ADJUSTMENT_TYPE
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCashbackStartDate(String accountNumber, Date nextCashbackStartDate) {
        int count = 0;

        try {
            // update new cashbackstartdate
            //String query = "UPDATE CARDACCOUNT SET CASHBACKSTARTDATE=? WHERE ACCOUNTNO=?";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_updateCashbackStartDate(),
                    nextCashbackStartDate,
                    accountNumber
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public BigDecimal getRedeemRequestAmount(String accountNumber) {
        BigDecimal redeemAmount = null;

        try {
            //no previous statement dates for given last cashback date
            //String query = "SELECT NVL(SUM(REQUESTEDAMOUNT),0) AS TOTALREDEEMAMOUNT FROM CASHBACKREQUEST WHERE ACCOUNTNO=? AND STATUS='RQAC' AND EODSTATUS=?";

            redeemAmount = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getCashBack_getRedeemRequestAmount(),
                    (ResultSet rs) -> {
                        BigDecimal temp = null;
                        while (rs.next()) {
                            temp = new BigDecimal(rs.getDouble("TOTALREDEEMAMOUNT"));
                        }
                        return temp;
                    },
                    accountNumber,
                    Configurations.EOD_PENDING_STATUS
            ));
        } catch (EmptyResultDataAccessException e) {
            return new BigDecimal(0);
        } catch (Exception e) {
            throw e;
        }
        return redeemAmount;
    }

    @Override
    public int redeemCashbacks(CashBackBean cashbackBean, BigDecimal redeemCashbackAmount) {
        int count = 0;
        java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);
        Double roundedCashbackAmount = Double.parseDouble(ValuesRoundup(redeemCashbackAmount.doubleValue()));

        try {
            //get redeemabal cashback amount (CASHBACKAMOUNT-REDEEMAMOUNT) and its ID that not expired and not zero
            //String query = "SELECT ID,(CASHBACKAMOUNT-REDEEMAMOUNT) AS CASHBACKAMOUNT2 FROM CASHBACK WHERE ACCOUNTNUMBER=? AND  ISEXPIRED=0 AND (CASHBACKAMOUNT-REDEEMAMOUNT)>0 ORDER BY EODID";

            LinkedHashMap<Integer, Double> hm = new LinkedHashMap<Integer, Double>();

            backendJdbcTemplate.query(queryParametersList.getCashBack_redeemCashbacks_Select(),
                    (ResultSet rs) -> {
                        Double totalAmount = 0.0;
                        //logic to redeem cashback records one by one until requested amount deducted
                        while (rs.next()) {
                            Integer id = rs.getInt("ID");
                            Double amount = rs.getDouble("CASHBACKAMOUNT2"); //cashback value

                            if ((totalAmount + amount) > roundedCashbackAmount) { // if when add full cashback value then exceed the request limit
                                amount = roundedCashbackAmount - totalAmount; //partially reduce the cashback amount
                            }
                            totalAmount += amount;
                            hm.put(id, amount);
                        }
                    }, cashbackBean.getAccountNumber());
            //deduct redeem amount
            //query = "UPDATE CASHBACK SET REDEEMAMOUNT=REDEEMAMOUNT+?,REDEEMDATE=?,LASTUPDATEDTIME=SYSDATE,REMARK=? WHERE ID=?";

            for (Map.Entry m : hm.entrySet()) {

                count = backendJdbcTemplate.update(queryParametersList.getCashBack_redeemCashbacks_Update(),
                        ValuesRoundup((Double) m.getValue()),
                        eodDate,
                        "Redeemed",
                        m.getKey()
                );
            }
            // insert new record to cashbackexpredeem table with total redeemed amount
            //query = "INSERT INTO CASHBACKEXPREDEEM (ACCOUNTNUMBER, EODID,EODDATE, CREATEDTIME, STATUS, AMOUNT) VALUES (?, ?, ?,SYSDATE, '0', ?)";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_redeemCashbacks_Insert(),
                    cashbackBean.getAccountNumber(),
                    Configurations.EOD_ID,
                    eodDate,
                    roundedCashbackAmount
            );

            // decrease available cashback amount in CARDACCOUNT table
            //query = "UPDATE CARDACCOUNT SET AVLCASHBACKAMOUNT=AVLCASHBACKAMOUNT-? WHERE ACCOUNTNO=?";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_redeemCashbacks_Update_2(),
                    roundedCashbackAmount,
                    cashbackBean.getAccountNumber()
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEodStatusInCashbackRequest(String accountNumber) {
        int count = 0;

        try {
            //String query = "UPDATE CASHBACKREQUEST SET EODSTATUS=? WHERE ACCOUNTNO=? AND EODSTATUS=?";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_updateEodStatusInCashbackRequest(),
                    Configurations.EOD_DONE_STATUS,
                    accountNumber,
                    Configurations.EOD_PENDING_STATUS
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public BigDecimal getRedeemableAmount(CashBackBean cashbackBean) {
        Double m1 = 0.0;
        BigDecimal remainingCashBackForYear = null;
        BigDecimal availableCashbackAmount = null;
        BigDecimal redeemRatio = BigDecimal.valueOf(cashbackBean.getRedeemRatio());
        BigDecimal fullRedeemableAmount = null;
        BigDecimal ActualAmountCanRedeem = new BigDecimal(0);

        try {
            //check customer has a due amount to pay
            String query = "SELECT M1 FROM MINIMUMPAYMENT WHERE CARDNO=?";

            try {
                m1 = backendJdbcTemplate.queryForObject(query, Double.class,
                        cashbackBean.getMainCardNumber().toString()
                );
            } catch (EmptyResultDataAccessException ex) {
                m1 = 0.0;
            }
            if (m1 > 0) {
                //customer has a due amount and cannot redeem cashback
                ActualAmountCanRedeem = new BigDecimal(0);
            } else { //customer not has any due amount
                // query = "SELECT (?-NVL(SUM(AMOUNT),0)) AS REMAININGCASHBACKFORYEAR FROM CASHBACKEXPREDEEM WHERE TRUNC(EODDATE)>=TRUNC(?) AND ACCOUNTNUMBER=?";

                double remainingCashBack = backendJdbcTemplate.queryForObject(queryParametersList.getCashBack_getRedeemableAmount_Select_2(), Double.class,
                        cashbackBean.getMaxCashbackPerYear(),
                        DateUtil.getSqldate(cashbackBean.getCashbackStartDate()),
                        cashbackBean.getAccountNumber()
                );
                remainingCashBackForYear = new BigDecimal(remainingCashBack);
            }

            query = "SELECT AVLCASHBACKAMOUNT FROM CARDACCOUNT WHERE ACCOUNTNO=? ";

            double availableCashBackAmt = backendJdbcTemplate.queryForObject(query, Double.class,
                    cashbackBean.getAccountNumber()
            );
            availableCashbackAmount = new BigDecimal(availableCashBackAmt);
            if (remainingCashBackForYear == null) {
                remainingCashBackForYear = new BigDecimal("0.0");
            }
            if (availableCashbackAmount == null) {
                availableCashbackAmount = new BigDecimal("0.0");
            }

            if (availableCashBackAmt > 0) {
                availableCashbackAmount = new BigDecimal(availableCashBackAmt);

                if (availableCashbackAmount.compareTo(remainingCashBackForYear) == 1) { //availableCashbackAmount > remainingCashBackForYear
                    fullRedeemableAmount = remainingCashBackForYear; //minimum amount can redeem is remainingCashBackForYear
                } else {
                    fullRedeemableAmount = availableCashbackAmount; // can redeem full availableCashbaackAmount with portion
                }
                // this will get the maximum amount can redeem in portion wise
                if (availableCashbackAmount.compareTo(BigDecimal.valueOf(cashbackBean.getMinAccumulatedToClaim())) == 1) { //availableCashbackAmount > minAccumulatedToClaim
                    ActualAmountCanRedeem = fullRedeemableAmount.divide(redeemRatio, RoundingMode.CEILING).setScale(0, RoundingMode.FLOOR).multiply(redeemRatio);
                } else {
                    ActualAmountCanRedeem = new BigDecimal(0);
                }
            }
        } catch (EmptyResultDataAccessException e) {
            return new BigDecimal("0.0");
        }
        return ActualAmountCanRedeem;
    }

    @Override
    public int updateNextCBRedeemDate(String accountNumber, String creditOption) {
        int count = 0;

        try {
            String query = "";
            switch (creditOption) {
                case "0":
                    // monthly
                    query = "UPDATE CARDACCOUNT SET NEXTCBREDEEMDATE=NEXTBILLINGDATE+? WHERE ACCOUNTNO=?";
                    //queryParametersList.getCashBack_updateNextCBRedeemDate_Update_1();
                    break;
                case "1":
                    //quartally
                    query = "UPDATE CARDACCOUNT SET NEXTCBREDEEMDATE=ADD_MONTHS(NEXTBILLINGDATE,3)+? WHERE ACCOUNTNO=?";
                    //queryParametersList.getCashBack_updateNextCBRedeemDate_Update_2();
                    break;
                case "2":
                    //anually
                    query = "UPDATE CARDACCOUNT SET NEXTCBREDEEMDATE=ADD_MONTHS(NEXTBILLINGDATE,12)+? WHERE ACCOUNTNO=?";
                    //queryParametersList.getCashBack_updateNextCBRedeemDate_Update_3();
                    break;
                default:
                    break;
            }

            count = backendJdbcTemplate.update(query, Configurations.CBREDEEMDAYCOUNT, accountNumber);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public BigDecimal getCashbackAmountToBeExpireForAccount(String accountNumber) {
        BigDecimal expireAmount = new BigDecimal(0);

        try {
            //String query = "SELECT NVL(SUM(CASHBACKAMOUNT - REDEEMAMOUNT), 0) AS TOTALEXPIREAMOUNT FROM CASHBACK WHERE ACCOUNTNUMBER=? AND  ISEXPIRED='0' AND (CASHBACKAMOUNT-REDEEMAMOUNT)>0";

            double cashBackExpireAmount = backendJdbcTemplate.queryForObject(queryParametersList.getCashBack_getCashbackAmountToBeExpireForAccount(), Double.class, accountNumber);

            expireAmount = new BigDecimal(cashBackExpireAmount);

        } catch (EmptyResultDataAccessException e) {
            return expireAmount;
        } catch (Exception e) {
            throw e;
        }
        return expireAmount;
    }

    @Override
    public int expireNonPerformingCashbacks(CashBackBean cashbackBean, BigDecimal expireCashbackAmount) {
        int count = 0;
        java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);
        Double roundedCashbackAmount = Double.parseDouble(ValuesRoundup(expireCashbackAmount.doubleValue()));

        try {
            // update cashback table (ISEXPIRED,LASTUPDATEDTIME,EXPIREDDATE,REMARK columns)
            //String query = "UPDATE CASHBACK SET ISEXPIRED=2,LASTUPDATEDTIME=SYSDATE,EXPIREDDATE=?,REMARK=? WHERE ISEXPIRED=0 AND ACCOUNTNUMBER=? AND (CASHBACKAMOUNT-REDEEMAMOUNT)>0";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_expireNonPerformingCashbacks_Update_1(),
                    eodDate,
                    "Replenished Due to Non Performing Account",
                    cashbackBean.getAccountNumber()
            );

            // insert new record to cashbackexpredeem table
            //query = "INSERT INTO CASHBACKEXPREDEEM (ACCOUNTNUMBER, EODID,EODDATE, CREATEDTIME, STATUS, AMOUNT) VALUES (?, ?, ?,SYSDATE, '2', ?)";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_expireNonPerformingCashbacks_Insert(),
                    cashbackBean.getAccountNumber(),
                    Configurations.EOD_ID,
                    eodDate,
                    roundedCashbackAmount
            );

            // set available cashback amount to zero in CARDACCOUNT table
            //query = "UPDATE CARDACCOUNT SET AVLCASHBACKAMOUNT=0 WHERE ACCOUNTNO=?";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_expireNonPerformingCashbacks_Update_2(),
                    cashbackBean.getAccountNumber()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int expireCardCloseCashbacks(CashBackBean cashbackBean, BigDecimal expireCashbackAmount) {
        int count = 0;
        java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);
        Double roundedCashbackAmount = Double.parseDouble(ValuesRoundup(expireCashbackAmount.doubleValue()));

        try {
            // update cashback table (ISEXPIRED,LASTUPDATEDTIME,EXPIREDDATE,REMARK columns)
            //String query = "UPDATE CASHBACK SET ISEXPIRED=1,LASTUPDATEDTIME=SYSDATE,EXPIREDDATE=?,REMARK=? WHERE ISEXPIRED=0 AND ACCOUNTNUMBER=? AND (CASHBACKAMOUNT-REDEEMAMOUNT)>0";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_expireCardCloseCashbacks_Update_1(),
                    eodDate,
                    "Expired Due to Card Close",
                    cashbackBean.getAccountNumber()
            );

            // insert new record to cashbackexpredeem table
            //query = "INSERT INTO CASHBACKEXPREDEEM (ACCOUNTNUMBER, EODID,EODDATE, CREATEDTIME, STATUS, AMOUNT) VALUES (?, ?, ?,SYSDATE, '1', ?)";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_expireNonPerformingCashbacks_Insert(),
                    cashbackBean.getAccountNumber(),
                    Configurations.EOD_ID,
                    eodDate,
                    roundedCashbackAmount
            );

            // set available cashback amount to zero in CARDACCOUNT table
            // query = "UPDATE CARDACCOUNT SET AVLCASHBACKAMOUNT=0 WHERE ACCOUNTNO=?";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_expireCardCloseCashbacks_Update_2(),
                    cashbackBean.getAccountNumber()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int expireCashbacks(CashBackBean cashbackBean) {
        int count = 0;
        java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);

        try {
            BigDecimal expireAmount = null;

            //String query = "SELECT NVL(SUM(CASHBACKAMOUNT-REDEEMAMOUNT),0) AS TOTALEXPIREAMOUNT FROM CASHBACK WHERE ACCOUNTNUMBER=? AND  ISEXPIRED='0' AND TRUNC(ADD_MONTHS(EODDATE,?))<=TRUNC(?) AND (CASHBACKAMOUNT-REDEEMAMOUNT)>0";

            //count = backendJdbcTemplate.queryForObject(query, Integer.class, expireAmount);

            expireAmount = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getCashBack_expireCashbacks_Select(),
                    (ResultSet rs) -> {
                        BigDecimal temp = null;
                        while (rs.next()) {
                            temp = new BigDecimal(rs.getDouble("TOTALEXPIREAMOUNT")); //calculated total cashback amount to be expire
                        }
                        return temp;
                    },
                    cashbackBean.getAccountNumber(),
                    cashbackBean.getCashbackExpiryPeriod(),
                    eodDate
            ));

            Double roundedCashbackAmount = Double.parseDouble(ValuesRoundup(expireAmount.doubleValue()));

            //query = "UPDATE CASHBACK SET ISEXPIRED=1,LASTUPDATEDTIME=SYSDATE,EXPIREDDATE=?,REMARK=? WHERE ISEXPIRED=0 AND ACCOUNTNUMBER=? AND TRUNC(ADD_MONTHS(EODDATE,?))<=TRUNC(?) AND (CASHBACKAMOUNT-REDEEMAMOUNT)>0 ";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_expireCashbacks_Update(),
                    eodDate,
                    "Expired Due Exceeding Expiry Period",
                    cashbackBean.getAccountNumber(),
                    cashbackBean.getCashbackExpiryPeriod(),
                    eodDate
            );

            if (expireAmount.signum() != 0) {
                // insert new record to cashbackexpredeem table
                //query = "INSERT INTO CASHBACKEXPREDEEM (ACCOUNTNUMBER, EODID,EODDATE, CREATEDTIME, STATUS, AMOUNT) VALUES (?, ?, ?,SYSDATE, '1', ?)";

                count = backendJdbcTemplate.update(queryParametersList.getCashBack_expireCashbacks_Insert(),
                        cashbackBean.getAccountNumber(),
                        Configurations.EOD_ID,
                        eodDate,
                        roundedCashbackAmount
                );
            }
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateTotalCBAmount(String accountNumber) {
        int count = 0;

        try {
            //String query = "UPDATE CASHBACK SET TOTALCBAMOUNT=(SELECT AVLCASHBACKAMOUNT FROM CARDACCOUNT WHERE ACCOUNTNO=?) WHERE EODID=? AND ACCOUNTNUMBER=?";

            count = backendJdbcTemplate.update(queryParametersList.getCashBack_updateTotalCBAmount(),
                    accountNumber,
                    Configurations.EOD_ID,
                    accountNumber
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
