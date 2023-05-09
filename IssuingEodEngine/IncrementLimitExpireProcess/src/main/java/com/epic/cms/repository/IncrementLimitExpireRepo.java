package com.epic.cms.repository;

import com.epic.cms.dao.IncrementLimitExpireDao;
import com.epic.cms.model.bean.LimitIncrementBean;
import com.epic.cms.model.rowmapper.LimitIncrementRowMapper;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class IncrementLimitExpireRepo implements IncrementLimitExpireDao {
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList statusList;

    @Autowired
    LogManager logManager;

    /**Use BackEnd Database */
    @Override
    public ArrayList<LimitIncrementBean> getLimitExpiredCardList() throws Exception {
        ArrayList<LimitIncrementBean> incrementBeansList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            String sql = "SELECT T.CARDNO,T.AMOUNT,T.INCREMENTTYPE,T.INCORDEC,T.REQUESTID,C.CARDCATEGORYCODE,CAC.ACCOUNTNO,CAC.CUSTOMERID FROM"
                    + " TEMPLIMITINCREMENT T,CARD C,CARDACCOUNTCUSTOMER CAC"
                    + " WHERE T.STATUS = ?"
                    + " AND T.CARDNO = C.CARDNUMBER"
                    + " AND CAC.CARDNUMBER = T.CARDNO"
                    + " AND TRUNC(T.ENDDATE) <= TO_DATE(?, 'DD-MM-YY') ";

            incrementBeansList = (ArrayList<LimitIncrementBean>) backendJdbcTemplate.query(sql,
                    new LimitIncrementRowMapper(),
                    statusList.getCREDIT_LIMIT_ENHANCEMENT_ACTIVE(),
                    sdf.format(Configurations.EOD_DATE));


        }catch (Exception e){
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
        return incrementBeansList;
    }

    @Override
    public int expireCreditLimit(LimitIncrementBean incrementBean) throws Exception {
        int flag = 0;
        String sql = "";
        try {
            //If credit increment expiry reduce OTBCREDIT,TEMPCREDITAMOUNT
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                sql = "UPDATE CARD SET OTBCREDIT = (OTBCREDIT - ? ) , LASTUPDATEDUSER = ? , LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER = ?";
            }
            //If credit decrement expiry increase OTBCREDIT,TEMPCREDITAMOUNT
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                sql = "UPDATE CARD SET OTBCREDIT = (OTBCREDIT + ? ) , LASTUPDATEDUSER = ? , LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER = ? ";
            }

            flag = backendJdbcTemplate.update(sql,
                    Double.parseDouble(incrementBean.getIncrementAmount()),
                    Configurations.EOD_USER,
                    incrementBean.getCardNumber().toString());

        }catch (Exception e){
            throw e;
        }
        return flag;
    }
    @Override
    public void limitExpireOnAccount(LimitIncrementBean incrementBean) throws Exception {
        String query = null;
        try {
            //If credit increment expiry reduce OTBCREDIT,TEMPCREDITAMOUNT
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE CARDACCOUNT SET OTBCREDIT = (OTBCREDIT - ? ) , LASTUPDATEDUSER = ? ,LASTUPDATEDTIME = SYSDATE WHERE ACCOUNTNO = ?";
            }
            //If credit decrement expiry increase OTBCREDIT,TEMPCREDITAMOUNT

            //  System.out.println(query);
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE CARDACCOUNT SET OTBCREDIT = (OTBCREDIT + ? ) , LASTUPDATEDUSER = ? ,LASTUPDATEDTIME = SYSDATE WHERE ACCOUNTNO = ?";
            }

            backendJdbcTemplate.update(query,
                    Double.parseDouble(incrementBean.getIncrementAmount()),
                    Configurations.EOD_USER,
                    incrementBean.getAccountnumber());

        }catch (Exception e){
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    @Override
    public void limitExpireOnCustomer(LimitIncrementBean limitIncrementBean) throws Exception {
        String query = null;
        try {
            //If credit increment expiry reduce OTBCREDIT,TEMPCREDITAMOUNT
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE CARDCUSTOMER SET OTBCREDIT = (OTBCREDIT - ? ) , LASTUPDATEDUSER = ? ,LASTUPDATEDTIME = SYSDATE WHERE CUSTOMERID = ?";
            }
            //If credit decrement expiry increase OTBCREDIT,TEMPCREDITAMOUNT
            //  System.out.println(query);
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE CARDCUSTOMER SET OTBCREDIT = (OTBCREDIT + ? ) , LASTUPDATEDUSER = ? ,LASTUPDATEDTIME = SYSDATE WHERE CUSTOMERID = ?";
            }

            backendJdbcTemplate.update(query,
                    Double.parseDouble(limitIncrementBean.getIncrementAmount()),
                    Configurations.EOD_USER,
                    limitIncrementBean.getCustomerid());

        }catch (Exception e){
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    @Override
    public int expireCashLimit(LimitIncrementBean incrementBean) throws Exception {
        int flag = 0;
        String query = null;
        try {
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE CARD SET  OTBCASH =(OTBCASH - ? ) ,  LASTUPDATEDUSER = ? , LASTUPDATEDTIME =SYSDATE WHERE CARDNUMBER = ?";
            }
            //If cash decrement expiry increase OTBCREDIT,TEMPCREDITAMOUNT,OTBCASH,TEMPCASHAMOUNT

            if (incrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE CARD SET  OTBCASH =(OTBCASH + ? ) , LASTUPDATEDUSER =?,LASTUPDATEDTIME =SYSDATE WHERE CARDNUMBER = ?";
            }

            flag = backendJdbcTemplate.update(query,
                    Double.parseDouble(incrementBean.getIncrementAmount()),
                    Configurations.EOD_USER,
                    incrementBean.getCardNumber().toString());

        }catch (Exception e){
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
        return flag;
    }

    @Override
    public void cashLimitExpireOnAccount(LimitIncrementBean limitIncrementBean) throws Exception {
        String query = null;
        try {
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE CARDACCOUNT SET OTBCASH =(OTBCASH - ? ) ,  LASTUPDATEDUSER = ? , LASTUPDATEDTIME =SYSDATE WHERE ACCOUNTNO = ?";
            }
            //If cash decrement expiry increase OTBCREDIT,TEMPCREDITAMOUNT,OTBCASH,TEMPCASHAMOUNT
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE CARDACCOUNT SET OTBCASH =(OTBCASH + ? ) , LASTUPDATEDUSER =?,LASTUPDATEDTIME =SYSDATE WHERE ACCOUNTNO = ?";
            }

            backendJdbcTemplate.update(query,
                    Double.parseDouble(limitIncrementBean.getIncrementAmount()),
                    Configurations.EOD_USER,
                    limitIncrementBean.getAccountnumber());

        }catch (Exception e){
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    @Override
    public void cashLimitExpireOnCustomer(LimitIncrementBean limitIncrementBean) throws Exception {
        String query = null;
        try {
            //If cash increment expiry reduce OTBCREDIT,TEMPCREDITAMOUNT,OTBCASH,TEMPCASHAMOUNT
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE CARDCUSTOMER SET OTBCASH =(OTBCASH - ? ) ,  LASTUPDATEDUSER = ? , LASTUPDATEDTIME =SYSDATE WHERE CUSTOMERID = ?";
            }
            //If cash decrement expiry increase OTBCREDIT,TEMPCREDITAMOUNT,OTBCASH,TEMPCASHAMOUNT
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE CARDCUSTOMER SET OTBCASH =(OTBCASH + ? ) , LASTUPDATEDUSER =?,LASTUPDATEDTIME =SYSDATE WHERE CUSTOMERID = ?";
            }

            backendJdbcTemplate.update(query,
                    Double.parseDouble(limitIncrementBean.getIncrementAmount()),
                    Configurations.EOD_USER,
                    limitIncrementBean.getCustomerid());

        }catch (Exception e){
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    @Override
    public int updateTempLimitIncrementTable(StringBuffer cardNumber, String status, String requestId, int processId) throws Exception {
        int count = 0;
        String query = null;
        try {
            if (processId == Configurations.PROCESS_LIMIT_ENHANCEMENT) {
                query = "UPDATE TEMPLIMITINCREMENT SET STATUS =?, EFFECTIVESTARTDATE= SYSDATE, LASTUPDATEDUSER= ?, " + " LASTUPDATEDTIME= SYSDATE,LASTEODUPDATEDDATE=? WHERE CARDNO = ? AND REQUESTID=? ";
            }
            else if (processId == Configurations.PROCESS_ID_INCREMENT_LIMIT_EXPIRE) {
                query = "UPDATE TEMPLIMITINCREMENT SET STATUS =?, EFFECTIVEENDDATE= SYSDATE, LASTUPDATEDUSER= ?, " + " LASTUPDATEDTIME= SYSDATE,LASTEODUPDATEDDATE=? WHERE CARDNO = ? AND REQUESTID=? ";
            }
            java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);

            count = backendJdbcTemplate.update(query,
                    status,
                    Configurations.EOD_USER,
                    eodDate,
                    cardNumber.toString(),
                    requestId);

        }catch (Exception e){
           // LogFileCreator.writeErrorToLog(e);
            throw e;
        }
        return count;
    }


    /**Use Online Database */
    @Override
    public int expireOnlineCreditLimit(LimitIncrementBean incrementBean) throws Exception {
        int flag = 0;
        String query = null;
        try {
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE ECMS_ONLINE_CARD SET OTBCREDIT = (OTBCREDIT - ? ) , LASTUPDATEUSER = ? , LASTUPDATETIME = SYSDATE  WHERE CARDNUMBER = ? ";
            }
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE ECMS_ONLINE_CARD SET OTBCREDIT = (OTBCREDIT + ? ) ,LASTUPDATEUSER = ? , LASTUPDATETIME = SYSDATE  WHERE CARDNUMBER = ? ";
            }

            flag = onlineJdbcTemplate.update(query,
                    Double.parseDouble(incrementBean.getIncrementAmount()),
                    Configurations.EOD_USER,
                    incrementBean.getCardNumber().toString());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logManager.logInfo("================ expireOnlineCreditLimit ===================" + Integer.toString(Configurations.EOD_ID),infoLogger);
                logManager.logInfo(query,infoLogger);
                logManager.logInfo(Configurations.EOD_USER,infoLogger);
                logManager.logInfo(CommonMethods.cardNumberMask(incrementBean.getCardNumber()),infoLogger);
                logManager.logInfo("================ expireOnlineCreditLimit END ===================",infoLogger);
            }

        }catch (Exception e){
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
        return flag;
    }

    @Override
    public void limitOnlineExpireOnAccount(LimitIncrementBean incrementBean) throws Exception {
        String query = null;
        try {
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE ECMS_ONLINE_ACCOUNT SET OTBCREDIT = (OTBCREDIT - ? ) WHERE ACCOUNTNUMBER = ? ";
            }
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE ECMS_ONLINE_ACCOUNT SET OTBCREDIT = (OTBCREDIT + ? ) WHERE ACCOUNTNUMBER = ? ";
            }

            onlineJdbcTemplate.update(query,
                    Double.parseDouble(incrementBean.getIncrementAmount()),
                    incrementBean.getAccountnumber());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logManager.logInfo("================ limitExpireOnAccount ===================" + Integer.toString(Configurations.EOD_ID),infoLogger);
                logManager.logInfo(query,infoLogger);
                logManager.logInfo(Configurations.EOD_USER,infoLogger);
                logManager.logInfo(incrementBean.getAccountnumber(),infoLogger);
                logManager.logInfo("================ limitExpireOnAccount END ===================",infoLogger);
            }

        }catch (Exception e){
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
    }

    @Override
    public void limitOnlineExpireOnCustomer(LimitIncrementBean limitIncrementBean) throws Exception {
        String query = null;
        try {
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE ECMS_ONLINE_CUSTOMER SET OTBCREDIT = (OTBCREDIT - ? ) WHERE CUSTOMERID = ? ";
            }
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE ECMS_ONLINE_CUSTOMER SET OTBCREDIT = (OTBCREDIT + ? ) WHERE CUSTOMERID = ? ";
            }

            onlineJdbcTemplate.update(query,
                    Double.parseDouble(limitIncrementBean.getIncrementAmount()),
                    limitIncrementBean.getCustomerid());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logManager.logInfo("================ limitExpireOnAccount ===================" + Integer.toString(Configurations.EOD_ID),infoLogger);
                logManager.logInfo(query,infoLogger);
                logManager.logInfo(Configurations.EOD_USER,infoLogger);
                logManager.logInfo(limitIncrementBean.getCustomerid(),infoLogger);
                logManager.logInfo("================ limitExpireOnAccount END ===================",infoLogger);
            }

        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public int expireOnlineCashLimit(LimitIncrementBean incrementBean) throws Exception {
        int flag = 0;
        String query = null;
        try {
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE ECMS_ONLINE_CARD SET OTBCASH =(OTBCASH - ? ) , LASTUPDATEUSER = ? , LASTUPDATETIME = SYSDATE  WHERE CARDNUMBER = ? ";
            }
            if (incrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE ECMS_ONLINE_CARD SET  OTBCASH =(OTBCASH + ? ) , LASTUPDATEUSER = ? , LASTUPDATETIME = SYSDATE WHERE CARDNUMBER = ? ";
            }

            flag = onlineJdbcTemplate.update(query,
                    Double.parseDouble(incrementBean.getIncrementAmount()),
                    Configurations.EOD_USER,
                    incrementBean.getCardNumber().toString());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logManager.logInfo("================ expireOnlineCashLimit ===================" + Integer.toString(Configurations.EOD_ID),infoLogger);
                logManager.logInfo(query,infoLogger);
                logManager.logInfo(Configurations.EOD_USER,infoLogger);
                logManager.logInfo(CommonMethods.cardNumberMask(incrementBean.getCardNumber()),infoLogger);
                logManager.logInfo("================ expireOnlineCashLimit END ===================",infoLogger);
            }

        }catch (Exception e){
            throw e;
        }
        return flag;
    }

    @Override
    public void cashLimitOnlineExpireOnAccount(LimitIncrementBean limitIncrementBean) throws Exception {
        String query = null;
        try {
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE ECMS_ONLINE_ACCOUNT SET  OTBCASH =(OTBCASH - ? ) WHERE ACCOUNTNUMBER = ? ";
            }
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE ECMS_ONLINE_ACCOUNT SET  OTBCASH =(OTBCASH + ? ) WHERE ACCOUNTNUMBER = ? ";

            }

            onlineJdbcTemplate.update(query,
                    Double.parseDouble(limitIncrementBean.getIncrementAmount()),
                    limitIncrementBean.getAccountnumber());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logManager.logInfo("================ cashLimitExpireOnAccount ===================" + Integer.toString(Configurations.EOD_ID),infoLogger);
                logManager.logInfo(query,infoLogger);
                logManager.logInfo(Configurations.EOD_USER,infoLogger);
                logManager.logInfo(limitIncrementBean.getAccountnumber(),infoLogger);
                logManager.logInfo("================ cashLimitExpireOnAccount END ===================",infoLogger);
            }

        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void cashLimitOnlineExpireOnCustomer(LimitIncrementBean limitIncrementBean) throws Exception {
        String query = null;
        try {
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_INCREMENT)) {
                query = "UPDATE ECMS_ONLINE_CUSTOMER SET  OTBCASH =(OTBCASH - ? ) WHERE CUSTOMERID = ? ";
            }
            if (limitIncrementBean.getIncordec().equals(Configurations.LIMIT_DECREMENT)) {
                query = "UPDATE ECMS_ONLINE_CUSTOMER SET OTBCASH =(OTBCASH + ? ) WHERE CUSTOMERID = ? ";
            }

            onlineJdbcTemplate.update(query,
                    Double.parseDouble(limitIncrementBean.getIncrementAmount()),
                    limitIncrementBean.getCustomerid());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logManager.logInfo("================ cashLimitExpireOnCustomer ===================" + Integer.toString(Configurations.EOD_ID),infoLogger);
                logManager.logInfo(query,infoLogger);
                logManager.logInfo(Configurations.EOD_USER,infoLogger);
                logManager.logInfo(limitIncrementBean.getCustomerid(),infoLogger);
                logManager.logInfo("================ cashLimitExpireOnCustomer END ===================",infoLogger);
            }

        }catch (Exception e){
            throw e;
        }
    }


}
