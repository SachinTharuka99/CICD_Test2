package com.epic.cms.repository;

import com.epic.cms.dao.ChequeProcessDao;
import com.epic.cms.model.bean.EODCardTransactionDetail;
import com.epic.cms.model.bean.EodInterestBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.Date;


@Repository
public class ChequeProcessRepo implements ChequeProcessDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;


    @Override
    @Transactional("backendDb")
    public EODCardTransactionDetail getEODTotalTransactionDetailsForCard(StringBuffer cardNo, int statementStartEODID, int eodId) throws Exception {
        EODCardTransactionDetail eodCardTransactionDetail = new EODCardTransactionDetail();
        try {
            int lastStatementEODID = statementStartEODID;
            int currentEODID = eodId;

            String sql = "SELECT TotalSales,TotalInterest,TotalPayment,totalfee,TotalCashAdvance " + "FROM " + "(select nvl(SUM(TRANSACTIONAMOUNT),0) as TotalSales FROM  EODTRANSACTION where TRANSACTIONTYPE IN (?,?) and EODID >= ? AND EODID <= ? AND status='EDON' AND  cardnumber IN (( ? ),?)) A " + "CROSS JOIN ( select nvl(SUM(INTERESTAMOUNT),0) as TotalInterest FROM  EOMINTEREST where CARDNO = ? ) B " + "CROSS JOIN ( select nvl(SUM(TRANSACTIONAMOUNT),0) as TotalPayment FROM  EODTRANSACTION where TRANSACTIONTYPE =? and EODID >= ? AND EODID <= ? and status='EDON' AND cardnumber IN (( ? ),?)) D " + "CROSS JOIN (select nvl(SUM(feeamount),0) as totalfee from eodcardfee where cardnumber IN (( ? ),?) and status='EDON' AND EODID >= ? AND EODID <= ?) E " + "CROSS JOIN (select nvl(SUM(TRANSACTIONAMOUNT),0) as TotalCashAdvance  FROM  EODTRANSACTION where TRANSACTIONTYPE =? and EODID >= ? AND EODID <= ? and  status='EDON' AND cardnumber IN (( ? ),?)) F ";

            eodCardTransactionDetail = backendJdbcTemplate.query(sql,
                    (ResultSet result) -> {
                        EODCardTransactionDetail detailBean = new EODCardTransactionDetail();
                        while (result.next()) {

                            detailBean.setCardNumber(cardNo);
                            detailBean.setEODdate(Configurations.EOD_ID);
                            detailBean.setTotalSales(result.getDouble("TotalSales"));
                            detailBean.setTotalCashAdvances(result.getDouble("TotalCashAdvance"));
                            detailBean.setTotalFees(result.getDouble("totalfee"));
                            detailBean.setTotalInterests(result.getDouble("TotalInterest"));
                            detailBean.setTotalPayments(result.getDouble("TotalPayment"));

                        }
                        return detailBean;
                    }, Configurations.TXN_TYPE_SALE, Configurations.TXN_TYPE_MVISA_ORIGINATOR, lastStatementEODID, currentEODID, getAllOldReplacedCardsString(cardNo), cardNo.toString(), cardNo.toString(), Configurations.TXN_TYPE_PAYMENT, lastStatementEODID, currentEODID, getAllOldReplacedCardsString(cardNo), cardNo.toString(), getAllOldReplacedCardsString(cardNo), cardNo.toString(), lastStatementEODID, currentEODID, Configurations.TXN_TYPE_CASH_ADVANCE, lastStatementEODID, currentEODID, getAllOldReplacedCardsString(cardNo), cardNo.toString());
        } catch (Exception e) {
            throw e;
        }
        return eodCardTransactionDetail;
    }

    public String getAllOldReplacedCardsString(StringBuffer cardNo) {
        return "SELECT oldcardnumber " + "FROM cardreplace " + "  START WITH newcardnumber = '" + cardNo.toString() + "'" + "  CONNECT BY PRIOR oldcardnumber = newcardnumber";
    }

    @Override
    @Transactional("backendDb")
    public Date calculateDueDate(String accountNo) throws Exception {
        Date dueDate;
        // here next billing date is eod date(statement date) it is not updated yet
        // at the end next billing date will update as next billing date+1month

        String sqlDueDate = "SELECT NEXTBILLINGDATE + (SELECT GRACEPERIOD FROM  BILLINGSTATEMENTPROFILE BSP,CARDACCOUNT CA WHERE CA.ACCOUNTNO = ? AND CA.BILLINGSTATEMENTPROFILECODE = BSP.PROFILECODE) as DUEDATE FROM CARDACCOUNT WHERE ACCOUNTNO = ?";

        try {
            dueDate = backendJdbcTemplate.queryForObject(sqlDueDate, Date.class, accountNo, accountNo);
        } catch (Exception e) {
            throw e;
        }
        return dueDate;
    }

    @Override
    @Transactional("backendDb")
    public EodInterestBean getEodInterestForCard(StringBuffer cardNo) throws Exception {
        EodInterestBean eodInterestBean = new EodInterestBean();
        try {
            String query = "SELECT CARDNO, FORWARDAMOUNT, CURRENTINTEREST, " + "ACTUALINTEREST, INTERESTRATE, DUEDATE " + "FROM EODINTEREST WHERE CARDNO=?";

            eodInterestBean = backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        EodInterestBean txnBean = new EodInterestBean();
                        while (result.next()) {
                            txnBean.setCardNumber(new StringBuffer(result.getString("CARDNO")));
                            txnBean.setForwardamount(result.getDouble("FORWARDAMOUNT"));
                            txnBean.setCurrentInterest(result.getDouble("CURRENTINTEREST"));
                            txnBean.setActualInterest(result.getDouble("ACTUALINTEREST"));
                            txnBean.setInterestRate(result.getDouble("INTERESTRATE"));
                            txnBean.setDueDate(result.getDate("DUEDATE"));

                        }
                        return txnBean;
                    }
                   ,cardNo.toString());

        } catch (Exception e) {
            throw e;
        }
        return eodInterestBean;
    }
}
