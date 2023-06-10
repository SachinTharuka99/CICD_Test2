package com.epic.cms.repository;

import com.epic.cms.dao.CheckPaymentForMinimumAmountDao;
import com.epic.cms.model.bean.LastStatementSummeryBean;
import com.epic.cms.model.bean.MinimumPaymentBean;
import com.epic.cms.model.rowmapper.LastStatementSummeryRowMapper;
import com.epic.cms.model.rowmapper.MinimumPaymentRowMapper;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class CheckPaymentForMinimumAmountRepo implements CheckPaymentForMinimumAmountDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    LogManager logManager;

    @Override
    public List<LastStatementSummeryBean> getStatementCardList() throws Exception {
        List<LastStatementSummeryBean> cardList = new ArrayList<LastStatementSummeryBean>();

        try {
            String query = "SELECT BS.CARDNO, CAC.ACCOUNTNO,bs.duedate,bs.minamount,bs.statementstartdate,bs.statementenddate, bs.openingbalance,bs.closingbalance,bs.CLOSINGLOYALTYPOINT FROM CARD C, BILLINGLASTSTATEMENTSUMMARY BS, CARDACCOUNTCUSTOMER CAC WHERE bs.cardno=c.cardnumber AND CAC.CARDNUMBER = c.cardnumber and c.cardstatus not in (?,?,?)";

            query += CommonMethods.checkForErrorCards("c.cardnumber");

            cardList = backendJdbcTemplate.query(query,
                    new LastStatementSummeryRowMapper(),
                    statusList.getCARD_CLOSED_STATUS(), //CACL
                    statusList.getCARD_REPLACED_STATUS(), //CARP
                    statusList.getCARD_PRODUCT_CHANGE_STATUS() //CAPC
            );

        } catch (Exception e) {
            logManager.logError("Statement Card List Error", errorLogger);
            throw e;
        }
        return cardList;
    }

    @Override
    public String getAccountNoOnCard(StringBuffer cardNo) throws Exception {
        String accNo = null;

        try {
            String query = "SELECT ACCOUNTNO FROM CARDACCOUNTCUSTOMER WHERE CARDNUMBER=?";

            accNo = backendJdbcTemplate.queryForObject(query, String.class, cardNo.toString());

        } catch (EmptyResultDataAccessException e) {
            return accNo;
        } catch (Exception e) {
            logManager.logError("AccountNo On Card Error", errorLogger);
            throw e;
        }
        return accNo;
    }

    @Override
    public Boolean insertToMinPayTable(StringBuffer cardNo, double fee, double totalTransactions, Date dueDate, String accNo, int statementDayEODID, double totalPayment, double paymentsBeforeDueDate) throws Exception {
        MinimumPaymentBean minimumPaymentBean = null;
        Boolean flag = false;
        int month = 0;

        String allPayments = "select * from minimumpayment where cardno=?";// and lasteodid=?";
        String allPamentsSameDay = "select * from minimumpayment where cardno=? and lasteodid=?";
        String insertQuery = "INSERT INTO MINIMUMPAYMENT (CARDNO,M1,M1DATE,STATUS,COUNT,LASTEODID) values (?,?,?,?,1,?)";

        try {
            minimumPaymentBean = backendJdbcTemplate.queryForObject(allPayments, new MinimumPaymentRowMapper(), cardNo);

            int count = 0;
            boolean sameDay = false;

            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(allPamentsSameDay, countCallback, cardNo, statementDayEODID);
            int recordCount = countCallback.getRowCount();

            if (recordCount > 0) {
                //EOD is executing in the same day.
                sameDay = true;
            }
            String mFee = null;//0f;

            for (int i = 1; i <= 12; i++) {
                String nextMonth = null;
                switch (i) {
                    case 1:
                        nextMonth = minimumPaymentBean.getM1();
                        break;
                    case 2:
                        nextMonth = minimumPaymentBean.getM2();
                        break;
                    case 3:
                        nextMonth = minimumPaymentBean.getM3();
                        break;
                    case 4:
                        nextMonth = minimumPaymentBean.getM4();
                        break;
                    case 5:
                        nextMonth = minimumPaymentBean.getM5();
                        break;
                    case 6:
                        nextMonth = minimumPaymentBean.getM6();
                        break;
                    case 7:
                        nextMonth = minimumPaymentBean.getM7();
                        break;
                    case 8:
                        nextMonth = minimumPaymentBean.getM8();
                        break;
                    case 9:
                        nextMonth = minimumPaymentBean.getM9();
                        break;
                    case 10:
                        nextMonth = minimumPaymentBean.getM10();
                        break;
                    case 11:
                        nextMonth = minimumPaymentBean.getM11();
                        break;
                    case 12:
                        nextMonth = minimumPaymentBean.getM12();
                        break;
                }
                if ((nextMonth == null) || (0 == Double.parseDouble(nextMonth))) {
                    month = i;
                    count++;
                    break;
                }
                count++;
            }

            if (month == 0) {
                month = 12;
                count = 12;
            }

            if (sameDay) {
                if (month != 1) {
                    if (month == 0) {
                        month = 12;
                        count = 12;
                    } else {
                        month -= 1;
                    }
                }
            }

            String updateQuery = "UPDATE MINIMUMPAYMENT SET M" + month + "=?,M" + month + "DATE=?, STATUS=?, COUNT =?, LASTEODID=?,LASTUPDATEDTIME=sysdate WHERE CARDNO=?";
            String status = statusList.getEOD_PENDING_STATUS();

            if (month >= Configurations.NO_OF_MONTHS_FOR_PERMENANT_BLOCK) {//TODO 3 should be a variable.
                /**
                 * if the pending months are greater than 3, then the min
                 * amount becomes the actual total transaction amount
                 */
                status = statusList.getCARD_TEMPORARY_BLOCK_Status();
            }

            backendJdbcTemplate.update(updateQuery, fee - totalPayment, dueDate, status, count, Configurations.EOD_ID, cardNo.toString());
            flag = true;

        } catch (EmptyResultDataAccessException ex) {
            logManager.logError("--no result found--",errorLogger);
            backendJdbcTemplate.update(insertQuery, cardNo.toString(), fee - totalPayment, dueDate, statusList.getEOD_PENDING_STATUS(), Configurations.EOD_ID);
            flag = true;
        } catch (Exception ex) {
            logManager.logError("Insert To MinPay Table Error", errorLogger);
            throw ex;
        }
        return flag;
    }

    @Override
    public double getPaymentAmount(String accNO, int startEOD) throws Exception {
        double paymentAmount = 0;

        try {
            String query = "SELECT NVL(SUM(TRANSACTIONAMOUNT),0) AS TOTALPAY FROM EODTRANSACTION WHERE TRANSACTIONTYPE IN (?,?,?,?) AND EODID > ? AND EODID <= ? AND STATUS NOT IN(?) AND ACCOUNTNO IN (?) ";

            paymentAmount = backendJdbcTemplate.queryForObject(query, Double.class,
                    Configurations.TXN_TYPE_PAYMENT,
                    Configurations.TXN_TYPE_REVERSAL,
                    Configurations.TXN_TYPE_REFUND,
                    Configurations.TXN_TYPE_MVISA_REFUND,
                    startEOD,
                    Configurations.EOD_ID,
                    statusList.getCHEQUE_RETURN_STATUS(),
                    accNO
            );

        } catch (EmptyResultDataAccessException e) {
            return paymentAmount;
        } catch (Exception e) {
            logManager.logError("Get Payment Amount Error", errorLogger);
            throw e;
        }
        return paymentAmount;
    }

    @Override
    public double getTotalPaymentExceptDueDate(String accNO, int startEOD) throws Exception {
        double paymentAmount = 0;

        try {
            String query = "SELECT NVL(SUM(TRANSACTIONAMOUNT),0) AS TOTALPAY FROM EODTRANSACTION WHERE TRANSACTIONTYPE IN (?,?,?,?) AND EODID > ? AND EODID < ? AND STATUS NOT IN(?) AND ACCOUNTNO IN (?) ";

            paymentAmount = backendJdbcTemplate.queryForObject(query, Double.class,
                    Configurations.TXN_TYPE_PAYMENT,
                    Configurations.TXN_TYPE_REVERSAL,
                    Configurations.TXN_TYPE_REFUND,
                    Configurations.TXN_TYPE_MVISA_REFUND,
                    startEOD,
                    Configurations.EOD_ID,
                    statusList.getCHEQUE_RETURN_STATUS(),
                    accNO
            );

        } catch (EmptyResultDataAccessException e) {
            return paymentAmount;
        } catch (Exception e) {
            logManager.logError("Get Total Payment Except DueDate Error", errorLogger);
            throw e;
        }
        return paymentAmount;
    }
}
