package com.epic.cms.repository;

import com.epic.cms.dao.LoyaltyPointsCalculationDao;
import com.epic.cms.model.bean.LoyaltyBean;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

@Repository
public class LoyaltyPointsCalculationRepo implements LoyaltyPointsCalculationDao {

    @Autowired
    QueryParametersList queryParametersList;
    @Autowired
    StatusVarList statusList;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    @Transactional("backendDb")
    public ArrayList<LoyaltyBean> getTodayBillingCardSet(Date eodDate) throws Exception {
        ArrayList<LoyaltyBean> loyaltyBeanArrayList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            //String sql = "SELECT B.STATEMENTID,B.CARDNO,B.STARTEODID,B.ENDEODID,B.STATEMENTENDDATE , B.STATEMENTSTARTDATE,B.ACCOUNTNO FROM BILLINGSTATEMENT B WHERE B.STATEMENTENDDATE =TO_DATE(?, 'DD-MM-YY')";
            String sql = queryParametersList.getLoyaltyPointsCalculation_getTodayBillingCardSet();

            sql += CommonMethods.checkForErrorCards("B.CARDNO");

            loyaltyBeanArrayList = (ArrayList<LoyaltyBean>) backendJdbcTemplate.query(sql, new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                LoyaltyBean loyaltyBean = new LoyaltyBean();
                loyaltyBean.setCardNo(new StringBuffer(rs.getString("CARDNO")));
                loyaltyBean.setStatementId(rs.getString("STATEMENTID"));
                loyaltyBean.setStmtEndEodID(rs.getInt("ENDEODID"));
                loyaltyBean.setStmtEndDate(rs.getDate("STATEMENTENDDATE"));
                loyaltyBean.setStmtStartDate(rs.getDate("STATEMENTSTARTDATE"));
                loyaltyBean.setStmtStartEodID(rs.getInt("STARTEODID"));
                loyaltyBean.setAccNo(rs.getString("ACCOUNTNO"));

                return loyaltyBean;
            }), sdf.format(eodDate));
        } catch (Exception e) {
            throw e;
        }
        return loyaltyBeanArrayList;
    }

    @Override
    @Transactional("backendDb")
    public boolean getLoyaltyConfigurations() throws Exception {
        AtomicBoolean accumulationPointVal = new AtomicBoolean(false);

        //String query = "SELECT NVL(ACCUMULATIONPOINTVALUE,0) AS ACCUMULATIONPOINTVALUE,NVL(POINTEXPPERIOD,0) AS POINTEXPPERIOD,NVL(MINNUMOFPOINTS,0) AS  MINNUMOFPOINTS FROM COMMONPARAMETER";

        try {
            backendJdbcTemplate.query(queryParametersList.getLoyaltyPointsCalculation_getLoyaltyConfigurations(), (ResultSet result) -> {
                while (result.next()) {
                    Configurations.LOYALTY_ACCUMILATION_VALUE = result.getInt("ACCUMULATIONPOINTVALUE");
                    Configurations.LOYALTY_EXPIARY_PERIOD = result.getInt("POINTEXPPERIOD");
                    Configurations.LOYALTY_MINIMUM_POINT = result.getInt("MINNUMOFPOINTS");
                    accumulationPointVal.set(true);
                }
            });
        } catch (Exception e) {
            return false;
        }
        return accumulationPointVal.get();
    }

    @Override
    @Transactional("backendDb")
    public double getLastStmtClosingLoyalty(StringBuffer cardNo, String statementId) throws Exception {
        double loyaltyPoints = 0.0;
        try {
            //String sql = "SELECT CLOSINGLOYALTYPOINT FROM BILLINGSTATEMENT WHERE CARDNO = ? AND STATEMENTID = ?";

            loyaltyPoints = backendJdbcTemplate.queryForObject(queryParametersList.getLoyaltyPointsCalculation_getLastStmtClosingLoyalty(), Double.class, cardNo.toString(), statementId);

        } catch (Exception e) {
            throw e;
        }
        return loyaltyPoints;
    }

    @Override
    @Transactional("backendDb")
    public double getThisMonthPurchases(String accNo, int stmtEndEodID, int stmtStartEodID) throws Exception {
        double purchases = 0.0;

        String query = "SELECT NVL(SUM(T.TRANSACTIONAMOUNT),0) AS TRANSACTIONAMOUNT" + " FROM EODTRANSACTION T " + " WHERE T.TRANSACTIONTYPE IN (?,?)" + " AND T.EODID           > ?" + " AND T.EODID           <=?" + " AND T.ADJUSTMENTSTATUS = 'NO'" + " AND T.ACCOUNTNO        =?";

        try {
            purchases = backendJdbcTemplate.queryForObject(query, Double.class, Configurations.TXN_TYPE_SALE, Configurations.TXN_TYPE_MVISA_ORIGINATOR, stmtStartEodID, stmtEndEodID, accNo);
        } catch (Exception e) {
            throw e;
        }

        return purchases;
    }

    @Override
    @Transactional("backendDb")
    public double getThisMonthRedeem(StringBuffer cardNo, Date stmtStartDate, Date stmtEndDate, ArrayList<Integer> requestID) throws Exception {
       // String query = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        double loyaltyPoints = 0.0;

        try {
            //query = "SELECT NVL(REQUESTEDPOINT,0) AS REQUESTEDPOINT , NVL(REQID,0) AS REQID FROM LOYALTYREDEEMREQUEST WHERE CARDNO = ? AND APPROVEDDATE > TO_DATE(?, 'DD-MM-YY') AND APPROVEDDATE <= TO_DATE(?, 'DD-MM-YY')";
            loyaltyPoints = backendJdbcTemplate.queryForObject(queryParametersList.getLoyaltyPointsCalculation_getThisMonthRedeem(), double.class, cardNo.toString(), sdf.format(stmtStartDate), sdf.format(stmtEndDate)
            );
        } catch (Exception e) {
            throw e;
        }
        return loyaltyPoints;
    }

    @Override
    @Transactional("backendDb")
    public double getAdjustLoyalty(String accNo, Date stmtStartDate, Date stmtEndDate) throws Exception {
        double adjustLoyalty = 0.0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        //String sql = "SELECT DISTINCT  NVL(((SELECT NVL(SUM(AMOUNT),0) CR FROM ADJUSTMENT WHERE CRDR  = ? AND ADJUSTMENTTYPE = ? AND UNIQUEID  IN (SELECT CA.CARDNUMBER FROM CARDACCOUNTCUSTOMER CA, CARD CD WHERE CA.CARDNUMBER = CD.CARDNUMBER AND CA.ACCOUNTNO= ? ) AND ADJUSTDATE > TO_DATE(?, 'DD-MM-YY') AND ADJUSTDATE <= TO_DATE(?, 'DD-MM-YY') AND STATUS = ? ) - (SELECT NVL(SUM(AMOUNT),0) DR FROM ADJUSTMENT WHERE CRDR  = ? AND ADJUSTMENTTYPE = ? AND UNIQUEID IN (SELECT CA.CARDNUMBER FROM CARDACCOUNTCUSTOMER CA, CARD CD WHERE CA.CARDNUMBER = CD.CARDNUMBER AND CA.ACCOUNTNO    = ? ) AND ADJUSTDATE > TO_DATE(?, 'DD-MM-YY') AND ADJUSTDATE <= TO_DATE(?, 'DD-MM-YY') AND STATUS = ?) ),0)AS TOTALADJUSTLOYALTYAMOUNT FROM ADJUSTMENT";
        try {
            adjustLoyalty = backendJdbcTemplate.queryForObject(queryParametersList.getLoyaltyPointsCalculation_getAdjustLoyalty(), Double.class, Configurations.CREDIT, Configurations.LOYALTY_ADJUSTMENT_TYPE, accNo, sdf.format(stmtStartDate), sdf.format(stmtEndDate), statusList.getMANUAL_ADJUSTMENT_ACCEPT(), Configurations.DEBIT, Configurations.LOYALTY_ADJUSTMENT_TYPE, accNo, sdf.format(stmtStartDate), sdf.format(stmtEndDate), statusList.getMANUAL_ADJUSTMENT_ACCEPT());
        } catch (Exception e) {
            throw e;
        }
        return adjustLoyalty;
    }

    @Override
    @Transactional(value = "backendDb", propagation = Propagation.NESTED, isolation = Isolation.SERIALIZABLE)
    public void updateBillingStatment(LoyaltyBean loyaltyBean) throws Exception {
        try {
            //String sql = "UPDATE BILLINGSTATEMENT SET OPENINGLOYALTYPOINT = ?, EARNLOYALTYPOINT = ?, AVLOYALTYPOINT = ?, CLOSINGLOYALTYPOINT = ?,REDEEMLOYALTYPOINT = ?,ADJUSTLOYALTYPOINT = ? WHERE STATEMENTID = ? AND CARDNO = ?";

            backendJdbcTemplate.update(queryParametersList.getLoyaltyPointsCalculation_updateBillingStatment(), loyaltyBean.getOpeningLoyaltyPoints(), loyaltyBean.getEarnLoyaltyPoints(), loyaltyBean.getAvailableLoyaltyPoints(), loyaltyBean.getClosingLoyaltyPoints(), loyaltyBean.getRedeemLoyaltyPoints(), loyaltyBean.getAdjustLoyaltyPoints(), loyaltyBean.getStatementId(), loyaltyBean.getCardNo().toString());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional(value = "backendDb", propagation = Propagation.NESTED, isolation = Isolation.SERIALIZABLE)
    public int updateLoyaltyRedeemRequest(ArrayList<Integer> requestID, String EOD_DONE_STATUS) throws Exception {
        int count = 0;
        try {
            for (Integer reqID : requestID) {
                //String query = "UPDATE LOYALTYREDEEMREQUEST SET STATUS = ?,LASTEODUPDATEDDATE=? WHERE REQID = ?";
                java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);
                count = backendJdbcTemplate.update(queryParametersList.getLoyaltyPointsCalculation_updateLoyaltyRedeemRequest(), EOD_DONE_STATUS, eodDate, reqID);
            }

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

}
