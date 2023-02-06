/**
 * Created By Lahiru Sandaruwan
 * Date : 10/24/2022
 * Time : 8:07 PM
 * Project Name : ecms_eod_engine
 * Topic : dailyInterestCalculation
 */

package com.epic.cms.repository;

import com.epic.cms.dao.DailyInterestCalculationDao;
import com.epic.cms.model.bean.DailyInterestBean;
import com.epic.cms.model.bean.InterestDetailBean;
import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.model.rowmapper.DailyInterestRowMapper;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static com.epic.cms.util.LogManager.errorLogger;

@Repository
public class DailyInterestCalculationRepo implements DailyInterestCalculationDao {

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public ArrayList<StatementBean> getLatestStatementAccountList() throws Exception {
        ArrayList<StatementBean> accountList = new ArrayList<>();
        try {
            String query = "SELECT BS.ACCOUNTNO, BS.MAINCARDNO, BS.CARDNO, BS.STATEMENTSTARTDATE, " + " BS.STATEMENTENDDATE, BS.THISBILLOPERNINGBALANCE, BS.THISBILLCLOSINGBALANCE, BS.DUEDATE, BS.STARTEODID, BS.ENDEODID " + " FROM BILLINGLASTSTATEMENTSUMMARY BLS INNER JOIN " + " BILLINGSTATEMENT BS ON BLS.STATEMENTID=BS.STATEMENTID " + " INNER JOIN CARD C ON BS.CARDNO=C.CARDNUMBER WHERE C.CARDSTATUS NOT IN(?)";

            accountList = (ArrayList<StatementBean>) backendJdbcTemplate.query(query, new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        StatementBean bean = new StatementBean();
                        bean.setAccountNo(result.getString("ACCOUNTNO"));
                        bean.setMainCardNo(new StringBuffer(result.getString("MAINCARDNO")));
                        bean.setCardNo(new StringBuffer(result.getString("CARDNO")));
                        bean.setStatementStartDate(result.getDate("STATEMENTSTARTDATE"));
                        bean.setStatementEndDate(result.getDate("STATEMENTENDDATE"));
                        bean.setStartingBalance(result.getDouble("THISBILLOPERNINGBALANCE"));
                        bean.setClosingBalance(result.getDouble("THISBILLCLOSINGBALANCE"));
                        bean.setStatementDueDate(result.getDate("DUEDATE"));
                        bean.setStartEodID(result.getInt("STARTEODID"));
                        bean.setEndEodID(result.getInt("ENDEODID"));
                        return bean;
                    }),
                    statusList.getCARD_CLOSED_STATUS()); //CACL
        } catch (Exception e) {
            errorLogger.error("Get Latest Statement AccountList Error ", e);
            throw e;
        }
        return accountList;
    }

    @Override
    public InterestDetailBean getIntProf(String accountNo) throws Exception {
        InterestDetailBean interestDetailBean = new InterestDetailBean();
        try {
            String query = "SELECT IP.INTERESTRATE, IP.INTERESTPERIODVALUE FROM CARDACCOUNT CA INNER JOIN INTERESTPROFILE IP ON IP.INTERESTPROFILECODE = CA.INTERESTPROFILECODE WHERE CA.ACCOUNTNO= ?";

            interestDetailBean = Objects.requireNonNull(backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        InterestDetailBean bean = new InterestDetailBean();
                        while (rs.next()) {
                            bean.setInterest(rs.getDouble("INTERESTRATE"));
                            bean.setInterestperiod(rs.getDouble("INTERESTPERIODVALUE"));
                        }
                        return bean;
                    },
                    accountNo
            ));

        } catch (Exception e) {
            errorLogger.error("Get IntProf Error ", e);
            throw e;
        }
        return interestDetailBean;
    }

    @Override
    public ArrayList<DailyInterestBean> getTxnOrPaymentDetailByAccount(String accountNumber, int startEodId, int endEodId, Date endDate, Double lastBillOpenningBalance, Date lastBillStartDate, Date lastBillEndDate, int lastBillEndEodId) throws Exception {
        ArrayList<DailyInterestBean> txnList = new ArrayList<>();
        try {
            String query = "SELECT ET.ACCOUNTNO AS ACCOUNTNO, SUM(ET.TRANSACTIONAMOUNT) AS TOTALPAY, (TRUNC(?)-TRUNC(ET.SETTLEMENTDATE)) AS DATEDIFF, "
                    + " SETTLEMENTDATE AS TRANDATE FROM EODTRANSACTION ET "
                    + " INNER JOIN CARDACCOUNT CA ON CA.ACCOUNTNO = ET.ACCOUNTNO "
                    + " INNER JOIN INTERESTPROFILETRANSACTION IPT ON CA.INTERESTPROFILECODE = IPT.INTERESTPROFILE "
                    + " AND IPT.TRANSACTIONCODE = ET.TRANSACTIONTYPE "
                    + " WHERE ET.ACCOUNTNO = ? AND ET.TRANSACTIONTYPE IN (?,?,?,?) "
                    + " AND ET.EODID > ? AND ET.EODID <= ? "
                    + " AND ET.TRANSACTIONID NOT IN(SELECT TXNID FROM EASYPAYMENTREQUEST WHERE STATUS IN(?,?)) "
                    + " GROUP BY ET.ACCOUNTNO, ET.SETTLEMENTDATE  "
                    + " UNION ALL "
                    + " SELECT ET.ACCOUNTNO AS ACCOUNTNO, SUM(ET.TRANSACTIONAMOUNT) AS TOTALPAY, (TRUNC(?)-TRUNC(ET.SETTLEMENTDATE)) AS DATEDIFF, "
                    + " SETTLEMENTDATE AS TRANDATE FROM EODTRANSACTION ET "
                    + " INNER JOIN CARDACCOUNT CA ON CA.ACCOUNTNO = ET.ACCOUNTNO "
                    + " INNER JOIN INTERESTPROFILETRANSACTION IPT ON CA.INTERESTPROFILECODE = IPT.INTERESTPROFILE "
                    + " AND IPT.TRANSACTIONCODE = ET.TRANSACTIONTYPE "
                    + " WHERE ET.ACCOUNTNO = ? AND ET.TRANSACTIONTYPE IN (?) "
                    + " AND ET.EODID > ? AND ET.EODID <= ? "
                    + " GROUP BY ET.ACCOUNTNO, ET.SETTLEMENTDATE  "
                    + " UNION ALL "
                    + " SELECT ACCOUNTNO AS ACCOUNTNO,SUM(FEEAMOUNT) AS TOTALPAY,(TRUNC(?)-TRUNC(EFFECTDATE)) AS DATEDIFF,TRUNC(EFFECTDATE) AS TRANDATE "
                    + " FROM EODCARDFEE "
                    + " WHERE ACCOUNTNO= ? "
                    + " AND EODID > ? AND EODID <= ? "
                    + " GROUP BY ACCOUNTNO, TRUNC(EFFECTDATE) "
                    + " UNION ALL "
                    + " SELECT ET.ACCOUNTNO AS ACCOUNTNO, -SUM(ET.TRANSACTIONAMOUNT) AS TOTALPAY, (TRUNC(?)-TRUNC(ET.SETTLEMENTDATE)) AS DATEDIFF, "
                    + " SETTLEMENTDATE AS TRANDATE "
                    + " FROM EODTRANSACTION ET WHERE ET.ACCOUNTNO = ? AND ET.TRANSACTIONTYPE IN (?) "
                    + " AND ET.EODID > ? AND ET.EODID <= ? "
                    + " GROUP BY ET.ACCOUNTNO, ET.SETTLEMENTDATE"
                    + " UNION ALL "
                    + " SELECT ? AS ACCOUNTNO, ? AS TOTALPAY ,TRUNC(?)-TRUNC(?) AS DATEDIFF, "
                    + " ? AS TRANDATE FROM DUAL"
                    + " UNION ALL "
                    + " SELECT ? AS ACCOUNTNO,INTERESTAMOUNT AS TOTALPAY ,TRUNC(?)-TRUNC(?) AS DATEDIFF, "
                    + " ? AS TRANDATE FROM EOMINTEREST WHERE ACCOUNTNO=?  AND EODID=?"
                    + " ORDER BY TRANDATE";

            txnList = (ArrayList<DailyInterestBean>) backendJdbcTemplate.query(query, new DailyInterestRowMapper(),
                    /**get transaction (but not installment transaction)*/
                    CommonMethods.getSqldate(endDate),
                    accountNumber,
                    Configurations.TXN_TYPE_SALE,
                    Configurations.TXN_TYPE_MVISA_ORIGINATOR,
                    Configurations.TXN_TYPE_CASH_ADVANCE,
                    Configurations.TXN_TYPE_FEE_INSTALLMENT,
                    startEodId,
                    endEodId,
                    statusList.getCOMMON_REQUEST_ACCEPTED(),
                    statusList.getCOMMON_COMPLETED(),

                    /**get monthly installment transaction*/
                    CommonMethods.getSqldate(endDate),
                    accountNumber,
                    Configurations.TXN_TYPE_INSTALLMENT,
                    startEodId,
                    endEodId,

                    /**get fees*/
                    CommonMethods.getSqldate(endDate),
                    accountNumber,
                    startEodId,
                    endEodId,

                    /**get payments*/
                    CommonMethods.getSqldate(endDate),
                    accountNumber,
                    Configurations.TXN_TYPE_PAYMENT,
                    startEodId,
                    endEodId,

                    /**set last bill opening balance as a transaction or payment (if minus openning balance)
                     on last statement end date(not on statement start date since monthly interest calculation consider
                     the opening balance until last statement end date*/
                    accountNumber,
                    lastBillOpenningBalance,
                    CommonMethods.getSqldate(endDate),
                    CommonMethods.getSqldate(lastBillEndDate),
                    CommonMethods.getSqldate(lastBillEndDate),

                    /**set calculated monthly interest as a transaction on last statement date*/
                    accountNumber,
                    CommonMethods.getSqldate(endDate),
                    CommonMethods.getSqldate(lastBillEndDate),
                    CommonMethods.getSqldate(lastBillEndDate),
                    accountNumber,
                    lastBillEndEodId
            );
        } catch (Exception e) {
            errorLogger.error("Get Txn Or Payment Detail ByAccount Error ", e);
            throw e;
        }
        return txnList;
    }

    @Override
    public int updateEodInterest(StatementBean bean, double txnInterest, double interestRate) throws Exception {
        int count = 0;
        try {
            String query = "SELECT COUNT(*) AS RECORD FROM EODINTEREST WHERE ACCOUNTNO = ? ";

            count = backendJdbcTemplate.queryForObject(query, Integer.class, bean.getAccountNo());

            if (count > 0) {
                query = "UPDATE EODINTEREST "
                        + "SET FORWARDAMOUNT = ?, CURRENTINTEREST = ?, ACTUALINTEREST = ?, "
                        + "INTERESTRATE = ?, LASTUPDATEDTIME=SYSDATE, LASTUPDATEDUSER=?, DUEDATE =? "
                        + "WHERE ACCOUNTNO = ? ";

                count = backendJdbcTemplate.update(query,
                        bean.getClosingBalance(),
                        txnInterest,
                        txnInterest,
                        interestRate,
                        Configurations.EOD_USER,
                        bean.getStatementDueDate(),
                        bean.getAccountNo()
                );
            } else {
                query = "INSERT INTO EODINTEREST (CARDNO,EODID,FORWARDAMOUNT,CURRENTINTEREST,CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER,ACTUALINTEREST,INTERESTRATE,DUEDATE,ACCOUNTNO) VALUES (?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?)";

                count = backendJdbcTemplate.update(query,
                        bean.getCardNo().toString(),
                        Configurations.EOD_ID,
                        bean.getClosingBalance(),
                        txnInterest,
                        Configurations.EOD_USER,
                        txnInterest,
                        interestRate,
                        bean.getStatementDueDate(),
                        bean.getAccountNo()
                );
            }
        } catch (EmptyResultDataAccessException ex) {
            return 0;
        } catch (Exception e) {
            errorLogger.error("Update EodInterest Error ", e);
            throw e;
        }
        return count;
    }
}
