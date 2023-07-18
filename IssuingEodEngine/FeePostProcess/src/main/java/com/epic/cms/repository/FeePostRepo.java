package com.epic.cms.repository;

import com.epic.cms.dao.FeePostDao;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.model.rowmapper.OtbRowMapper;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
public class FeePostRepo implements FeePostDao {
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Autowired
    @Qualifier("CustAccRowMapper")
    OtbRowMapper custAccRowMapper;

    @Autowired
    @Qualifier("FeeAmountRowMapper")
    OtbRowMapper feeAmountRowMapper;

    @Override
    public List<OtbBean> getInitEodFeePostCustAcc() throws Exception {
        List<OtbBean> custAccList = new ArrayList<OtbBean>();
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(Configurations.EOD_DATE);
            c.add(Calendar.DATE, 1);
            Date eodDateEndTime = c.getTime();
            java.sql.Date eodDate = CommonMethods.getSqldate(eodDateEndTime);

            //String query = "SELECT DISTINCT CAC.CUSTOMERID, CAC.ACCOUNTNO FROM EODCARDFEE F FULL JOIN EOMINTEREST EI ON EI.CARDNO = F.CARDNUMBER INNER JOIN CARD C ON C.CARDNUMBER = F.CARDNUMBER OR C.CARDNUMBER = EI.CARDNO INNER JOIN CARDACCOUNTCUSTOMER CAC ON CAC.CARDNUMBER = C.MAINCARDNO OR CAC.CARDNUMBER = EI.CARDNO WHERE ((F.STATUS IN (?, ?) AND F.EFFECTDATE <= ?) OR EI.STATUS IN (?)) AND CAC.ACCOUNTNO NOT IN (SELECT EC.ACCOUNTNO FROM EODERRORCARDS EC WHERE EC.STATUS= ?)";
            custAccList = backendJdbcTemplate.query(queryParametersList.getFeePost_getInitEodFeePostCustAcc(), custAccRowMapper, status.getEOD_PENDING_STATUS(), status.getACTIVE_STATUS(), eodDate, status.getEOD_PENDING_STATUS(), status.getEOD_PENDING_STATUS());
        } catch (Exception e) {
            throw e;
        }
        return custAccList;
    }

    @Override
    public List<OtbBean> getErrorEodFeePostCustAcc() throws Exception {
        List<OtbBean> custAccList = new ArrayList<OtbBean>();
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(Configurations.EOD_DATE);
            c.add(Calendar.DATE, 1);
            Date eodDateEndTime = c.getTime();
            java.sql.Date eodDate = CommonMethods.getSqldate(eodDateEndTime);

            //String query = "SELECT DISTINCT CAC.CUSTOMERID, CAC.ACCOUNTNO FROM EODCARDFEE F FULL JOIN EOMINTEREST EI ON EI.CARDNO = F.CARDNUMBER INNER JOIN CARD C ON C.CARDNUMBER = F.CARDNUMBER OR C.CARDNUMBER = EI.CARDNO INNER JOIN CARDACCOUNTCUSTOMER CAC ON CAC.CARDNUMBER = C.MAINCARDNO OR CAC.CARDNUMBER = EI.CARDNO INNER JOIN EODERRORCARDS EEC ON EEC.ACCOUNTNO = CAC.ACCOUNTNO WHERE ((F.STATUS IN (?, ?) AND F.EFFECTDATE <= ?) OR EI.STATUS IN (?) ) AND EEC.STATUS = ? AND EEC.EODID < ?  AND EEC.PROCESSSTEPID <= ? ORDER BY CAC.CUSTOMERID, CAC.ACCOUNTNO";
            custAccList = backendJdbcTemplate.query(queryParametersList.getFeePost_getErrorEodFeePostCustAcc(), custAccRowMapper, status.getEOD_PENDING_STATUS(), status.getACTIVE_STATUS(), eodDate, status.getEOD_PENDING_STATUS(), status.getEOD_PENDING_STATUS(), Configurations.ERROR_EOD_ID, Configurations.PROCESS_STEP_ID);
        } catch (Exception e) {
            throw e;
        }
        return custAccList;
    }

    @Override
    public List<OtbBean> getFeeAmount(String accNo) throws Exception {
        List<OtbBean> feeList = new ArrayList<>();
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(Configurations.EOD_DATE);
            c.add(Calendar.DATE, 1);
            Date eodDateEndTime = c.getTime();
            java.sql.Date eodDate = CommonMethods.getSqldate(eodDateEndTime);

            //String query = "SELECT NVL(FEE.CARDNUMBER, INTE.CARDNO) CARDNUMBER,(NVL(FEE.FEEAMOUNT,0) + NVL(INTE.FORWARDINTEREST, 0)) FINANCIALCHARGES FROM (SELECT F.CARDNUMBER, NVL(SUM(F.FEEAMOUNT),0) FEEAMOUNT FROM EODCARDFEE F WHERE F.STATUS IN (?, ?) AND F.EFFECTDATE <= ? AND F.ACCOUNTNO = ? GROUP BY F.CARDNUMBER ) FEE FULL JOIN (SELECT EI.CARDNO, NVL(SUM(EI.FORWARDINTEREST),0) FORWARDINTEREST FROM EOMINTEREST EI WHERE EI.STATUS IN (?) AND EI.ACCOUNTNO = ? GROUP BY EI.CARDNO) INTE ON FEE.CARDNUMBER = INTE.CARDNO";
            feeList = backendJdbcTemplate.query(queryParametersList.getFeePost_getFeeAmount(), feeAmountRowMapper, status.getEOD_PENDING_STATUS(), status.getACTIVE_STATUS(), eodDate, accNo, status.getEOD_PENDING_STATUS(), accNo);
        } catch (Exception e) {
            throw e;
        }
        return feeList;
    }

    @Override
    public int updateCardOtb(OtbBean cardBean) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE CARD SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ?,TEMPCREDITAMOUNT = TEMPCREDITAMOUNT + ?,TEMPCASHAMOUNT = TEMPCASHAMOUNT + ?,LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER=?";
            count = backendJdbcTemplate.update(queryParametersList.getFeePost_updateCardOtb(), cardBean.getOtbcredit(), cardBean.getOtbcash(), cardBean.getOtbcredit(), cardBean.getOtbcash(), Configurations.EOD_USER, cardBean.getCardnumber().toString());
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public void updateEODCARDBALANCEByFee(OtbBean cardBean) throws Exception {
        try {
            //String query = "UPDATE EODCARDBALANCE SET FINANCIALCHARGES = FINANCIALCHARGES + ?, EODCLOSINGBAL = EODCLOSINGBAL - ?,LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER = ?";
            backendJdbcTemplate.update(queryParametersList.getFeePost_updateEODCARDBALANCEByFee(), cardBean.getOtbcredit(), cardBean.getOtbcredit(), Configurations.EOD_USER, cardBean.getCardnumber().toString());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCardOtb(OtbBean cardBean) throws Exception {
        try {
            //String query = "UPDATE ECMS_ONLINE_CARD SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ?,TEMPCREDITAMOUNT = TEMPCREDITAMOUNT + ?,TEMPCASHAMOUNT = TEMPCASHAMOUNT + ?,LASTUPDATEUSER=?, LASTUPDATETIME=SYSDATE WHERE CARDNUMBER=?";
            onlineJdbcTemplate.update(queryParametersList.getFeePost_updateOnlineCardOtb(), cardBean.getOtbcredit(), cardBean.getOtbcash(), cardBean.getTmpcredit(), cardBean.getTmpcash(), Configurations.EOD_USER, cardBean.getCardnumber().toString());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateAccountOtb(OtbBean otbBean) throws Exception {
        try {
            //String query = "UPDATE CARDACCOUNT SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ?,LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE ACCOUNTNO=?";
            backendJdbcTemplate.update(queryParametersList.getFeePost_updateAccountOtb(), otbBean.getOtbcredit(), otbBean.getOtbcash(), Configurations.EOD_USER, otbBean.getAccountnumber());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateEODCARDFEE(String accNo) throws Exception {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(Configurations.EOD_DATE);
            c.add(Calendar.DATE, 1);
            Date eodDateEndTime = c.getTime();
            java.sql.Date eodDate = CommonMethods.getSqldate(eodDateEndTime);

           // String query = "UPDATE EODCARDFEE SET  STATUS= ?, LASTUPDATEDDATE=SYSDATE, LASTUPDATEDUSER =? WHERE ACCOUNTNO= ? AND STATUS IN (?, ?) AND EFFECTDATE <= ?";
            backendJdbcTemplate.update(queryParametersList.getFeePost_updateEODCARDFEE(), status.getEOD_DONE_STATUS(), Configurations.EOD_USER, accNo, status.getEOD_PENDING_STATUS(), status.getACTIVE_STATUS(), eodDate);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateEOMINTEREST(String accNo) throws Exception {
        try {
            //String query = "UPDATE EOMINTEREST SET STATUS= ? WHERE ACCOUNTNO = ? AND STATUS = ?";
            backendJdbcTemplate.update(queryParametersList.getFeePost_updateEOMINTEREST(), status.getEOD_DONE_STATUS(), accNo, status.getEOD_PENDING_STATUS());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineAccountOtb(OtbBean otbBean) throws Exception {
        try {
            //String query = "UPDATE ECMS_ONLINE_ACCOUNT SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ? WHERE ACCOUNTNUMBER=?";
            onlineJdbcTemplate.update(queryParametersList.getFeePost_updateOnlineAccountOtb(), otbBean.getOtbcredit(), otbBean.getOtbcash(), otbBean.getAccountnumber());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateCustomerOtb(OtbBean bean) throws Exception {
        try {
            //String query = "UPDATE CARDCUSTOMER SET OTBCREDIT= OTBCREDIT - ?,OTBCASH= OTBCASH - ?,LASTUPDATEDUSER=?, LASTUPDATEDTIME=SYSDATE WHERE CUSTOMERID=?";
            backendJdbcTemplate.update(queryParametersList.getFeePost_updateCustomerOtb(), bean.getOtbcredit(), bean.getOtbcash(), Configurations.EOD_USER, bean.getCustomerid());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCustomerOtb(OtbBean bean) throws Exception {
        try {
            //String query = "UPDATE ECMS_ONLINE_CUSTOMER SET OTBCREDIT= OTBCREDIT - ?,OTBCASH= OTBCASH - ? WHERE CUSTOMERID = ?";
            onlineJdbcTemplate.update(queryParametersList.getFeePost_updateOnlineCustomerOtb(), bean.getOtbcredit(), bean.getOtbcash(), bean.getCustomerid());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int expireFeePromotionProfile() throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE PROMOFEEPROFILE SET STATUS = ? WHERE TRUNC(ENDDATE) <= TO_DATE(?,'DD-MM-YY')";
            count = backendJdbcTemplate.update(queryParametersList.getFeePost_expireFeePromotionProfile(), status.getFEE_PROMOTION_PROFILE_EXPIRE(), Configurations.EOD_DATE_String);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
