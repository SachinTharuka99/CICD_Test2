package com.epic.cms.repository;

import com.epic.cms.dao.CardLimitEnhancementDao;
import com.epic.cms.model.bean.BalanceComponentBean;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.model.rowmapper.BalanceComponentRowMapper;
import com.epic.cms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


@Repository
public class CardLimitEnhancementRepo implements CardLimitEnhancementDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Autowired
    LogManager logManager;

    @Autowired
    QueryParametersList queryParametersList;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Override
    public ArrayList<OtbBean> getInitLimitEnhanceCustAcc() throws Exception {
        ArrayList<OtbBean> otbBeanList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        //String sql = "SELECT DISTINCT cac.customerid, cac.accountno FROM  templimitincrement ti INNER JOIN card  c ON c.cardnumber = ti.cardno INNER JOIN cardaccountcustomer cac ON cac.cardnumber = ti.cardno WHERE ti.status = ?  AND ti.startdate <= ? AND cac.accountno NOT IN (SELECT ec.accountno  FROM  eoderrorcards ec WHERE ec.status = ? ) ORDER BY cac.customerid, cac.accountno";;
        try {
            otbBeanList = (ArrayList<OtbBean>) backendJdbcTemplate.query(queryParametersList.getCardLimitEnhancement_getInitLimitEnhanceCustAcc(), new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        OtbBean bean = new OtbBean();
                        bean.setCustomerid(rs.getString("CUSTOMERID"));
                        bean.setAccountnumber(rs.getString("ACCOUNTNO"));
                        return bean;
                    })
                    , statusList.getCREDIT_LIMIT_ENHANCEMENT_REQUEST_CONFIRMED()
                    , sdf.format(Configurations.EOD_DATE)
                    , statusList.getEOD_PENDING_STATUS()
            );

        } catch (Exception e) {
            throw e;
        }
        return otbBeanList;
    }

    @Override
    public ArrayList<OtbBean> getErrorLimitEnhanceCustAcc() throws Exception {
        ArrayList<OtbBean> otbBeanList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        //String sql = "SELECT DISTINCT cac.customerid, cac.accountno FROM  templimitincrement ti INNER JOIN card  c ON c.cardnumber = ti.cardno INNER JOIN cardaccountcustomer cac ON cac.cardnumber = c.maincardno  INNER JOIN eoderrorcards eec ON eec.accountno = cac.accountno  WHERE ti.status = ?  AND ti.startdate <= ? AND eec.status = ? AND eec.eodid < ? AND eec.processstepid <= ?  ORDER BY  cac.customerid, cac.accountno ";
        ;
        try {
            otbBeanList =(ArrayList<OtbBean>) backendJdbcTemplate.query(queryParametersList.getCardLimitEnhancement_getErrorLimitEnhanceCustAcc(),new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        OtbBean bean = new OtbBean();
                        bean.setCustomerid(rs.getString("CUSTOMERID"));
                        bean.setAccountnumber(rs.getString("ACCOUNTNO"));
                        return bean;
                    })
                    , statusList.getCREDIT_LIMIT_ENHANCEMENT_REQUEST_CONFIRMED()
                    , sdf.format(Configurations.EOD_DATE)
                    , Configurations.EOD_PENDING_STATUS
                    , Configurations.ERROR_EOD_ID
                    , Configurations.PROCESS_STEP_ID
            );
        } catch (Exception e) {
            throw e;
        }
        return otbBeanList;
    }

    @Override
    public ArrayList<BalanceComponentBean> getLimitEnhanceReqConCardList(String customerId, String accountNumber) {
        ArrayList<BalanceComponentBean> balanceComponentBeanList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        //String query = "SELECT TI.CARDNO, TI.AMOUNT, TI.INCREMENTTYPE, TI.INCORDEC, TI.REQUESTID, C.CARDCATEGORYCODE, TI.STARTDATE, TI.ENDDATE FROM TEMPLIMITINCREMENT TI INNER JOIN CARDACCOUNTCUSTOMER CAC ON CAC.CARDNUMBER = TI.CARDNO INNER JOIN CARD C ON C.CARDNUMBER = CAC.CARDNUMBER WHERE TI.STATUS = ? AND TI.STARTDATE <= ? AND CAC.CUSTOMERID = ? AND CAC.ACCOUNTNO = ? ";
        try {
            balanceComponentBeanList = (ArrayList<BalanceComponentBean>) backendJdbcTemplate.query(queryParametersList.getCardLimitEnhancement_getLimitEnhanceReqConCardList(),
                    new BalanceComponentRowMapper(),
                    statusList.getCREDIT_LIMIT_ENHANCEMENT_REQUEST_CONFIRMED(),
                    sdf.format(Configurations.EOD_DATE),
                    customerId,
                    accountNumber
            );
        } catch (Exception e) {
            throw e;
        }
        return balanceComponentBeanList;
    }

    @Override
    public void updateCardCreditLimit(StringBuffer cardNumber, double otbCredit) throws Exception {
        try {
            //String query = "UPDATE CARD SET OTBCREDIT = OTBCREDIT + ? , LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER = ?";

            backendJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateCardCreditLimit(), otbCredit, Configurations.EOD_USER, cardNumber.toString());

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCardCreditLimit(StringBuffer cardNumber, double otbCredit) throws Exception {
        try {
            //String query = "UPDATE ECMS_ONLINE_CARD SET OTBCREDIT = OTBCREDIT + ?, LASTUPDATEUSER = ?, LASTUPDATETIME = SYSDATE WHERE CARDNUMBER=?";

            onlineJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateOnlineCardCreditLimit(), otbCredit, Configurations.EOD_USER, cardNumber.toString());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateCardCreditLimit ===================" + Integer.toString(Configurations.EOD_ID));
                logInfo.info(queryParametersList.getCardLimitEnhancement_updateOnlineCardCreditLimit());
                logInfo.info(Double.toString(otbCredit));
                logInfo.info(Configurations.EOD_USER);
                logInfo.info(CommonMethods.cardNumberMask(cardNumber));
                logInfo.info("================ updateCardCreditLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateAccountCreditLimit(String accountNumber, double otbCredit) throws Exception {
        try {
            //String query = "UPDATE CARDACCOUNT SET OTBCREDIT = OTBCREDIT + ? , LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE ACCOUNTNO = ? ";

            backendJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateAccountCreditLimit(), otbCredit, Configurations.EOD_USER, accountNumber);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineAccountCreditLimit(String accountNumber, double otbCredit) throws Exception {
        try {
            //String query = "UPDATE ECMS_ONLINE_ACCOUNT SET OTBCREDIT = OTBCREDIT + ? WHERE ACCOUNTNUMBER=?";

            onlineJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateOnlineAccountCreditLimit(), otbCredit, accountNumber);

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateAccountCreditLimit ===================" + Integer.toString(Configurations.EOD_ID));
                logInfo.info(queryParametersList.getCardLimitEnhancement_updateOnlineAccountCreditLimit());
                logInfo.info(Double.toString(otbCredit));
                logInfo.info(Configurations.EOD_USER);
                logInfo.info(accountNumber);
                logInfo.info("================ updateAccountCreditLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateCustomerCreditLimit(String customerId, double otbCredit) throws Exception {
        try {
            //String query = "UPDATE CARDCUSTOMER SET OTBCREDIT = OTBCREDIT + ? , LASTUPDATEDUSER=?, LASTUPDATEDTIME=SYSDATE WHERE CUSTOMERID = ? ";

            backendJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateCustomerCreditLimit(), otbCredit, Configurations.EOD_USER, customerId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCustomerCreditLimit(String customerId, double otbCredit) {
        try {
            //String query = "UPDATE ECMS_ONLINE_CUSTOMER SET OTBCREDIT=OTBCREDIT + ? WHERE CUSTOMERID=?";

            onlineJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateOnlineCustomerCreditLimit(), otbCredit, customerId);

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateCustomerCreditLimit ===================" + Integer.toString(Configurations.EOD_ID));
                logInfo.info(queryParametersList.getCardLimitEnhancement_updateOnlineCustomerCreditLimit());
                logInfo.info(Double.toString(otbCredit));
                logInfo.info(customerId);
                logInfo.info("================ updateCustomerCreditLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateCardCashLimit(StringBuffer cardNumber, double otbCash) throws Exception {
        try {
            //String query = "UPDATE CARD SET OTBCASH = OTBCASH + ? , LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER = ?  ";

            backendJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateCardCashLimit(), otbCash, Configurations.EOD_USER, cardNumber.toString());

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCardCashLimit(StringBuffer cardNumber, double otbCash) throws Exception {
        try {
            //String query = "UPDATE ECMS_ONLINE_CARD SET OTBCASH = OTBCASH + ?, LASTUPDATEUSER = ?, LASTUPDATETIME = SYSDATE WHERE CARDNUMBER=?  ";

            onlineJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateOnlineCardCashLimit(), otbCash, Configurations.EOD_USER, cardNumber.toString());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateCardCashtLimit ===================" + Integer.toString(Configurations.EOD_ID));
                logInfo.info(queryParametersList.getCardLimitEnhancement_updateOnlineCardCashLimit());
                logInfo.info(Double.toString(otbCash));
                logInfo.info(CommonMethods.cardNumberMask(cardNumber));
                logInfo.info("================ updateCardCashtLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateAccountCashLimit(String accountNumber, double otbCash) throws Exception {
        try {
            //String query = "UPDATE CARDACCOUNT SET OTBCASH = OTBCASH + ? , LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE ACCOUNTNO = ? ";

            backendJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateAccountCashLimit(), otbCash, Configurations.EOD_USER, accountNumber);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineAccountCashLimit(String accountNumber, double otbCash) throws Exception {
        try {
            //String query = "UPDATE ECMS_ONLINE_ACCOUNT SET OTBCASH = OTBCASH + ? WHERE ACCOUNTNUMBER=?";

            onlineJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateOnlineAccountCashLimit(), otbCash, accountNumber);

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateAccountCashLimit ===================" + Integer.toString(Configurations.EOD_ID));
                logInfo.info(queryParametersList.getCardLimitEnhancement_updateOnlineAccountCashLimit());
                logInfo.info(Double.toString(otbCash));
                logInfo.info(accountNumber);
                logInfo.info("================ updateAccountCashLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateCustomerCashLimit(String customerId, double otbCash) throws Exception {
        try {
            //String query = "UPDATE CARDCUSTOMER SET OTBCASH = OTBCASH + ? , LASTUPDATEDUSER=?, LASTUPDATEDTIME=SYSDATE WHERE CUSTOMERID = ?  ";

            backendJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateCustomerCashLimit(), otbCash, Configurations.EOD_USER, customerId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCustomerCashLimit(String customerId, double otbCash) throws Exception {
        try {
            //String query = "UPDATE ECMS_ONLINE_CUSTOMER SET OTBCASH=OTBCASH + ? WHERE CUSTOMERID=?  ";

            onlineJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateOnlineCustomerCashLimit(), otbCash, customerId);

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateCustomerCashLimit ===================" + Integer.toString(Configurations.EOD_ID));
                logInfo.info(queryParametersList.getCardLimitEnhancement_updateOnlineCustomerCashLimit());
                logInfo.info(Double.toString(otbCash));
                logInfo.info(customerId);
                logInfo.info("================ updateCustomerCashLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateTempLimitIncrementTable(StringBuffer cardNumber, String status, String requestId) {
        try {
            java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);

            //String query = "UPDATE TEMPLIMITINCREMENT SET STATUS =?, EFFECTIVESTARTDATE= SYSDATE, LASTUPDATEDUSER= ?, LASTUPDATEDTIME= SYSDATE,LASTEODUPDATEDDATE=? WHERE CARDNO = ? AND REQUESTID=? ";

            backendJdbcTemplate.update(queryParametersList.getCardLimitEnhancement_updateTempLimitIncrementTable(), status, Configurations.EOD_USER, eodDate, cardNumber.toString(), requestId);

        } catch (Exception e) {
            throw e;
        }
    }
}
