package com.epic.cms.repository;

import com.epic.cms.dao.CardLimitEnhancementDao;
import com.epic.cms.model.bean.BalanceComponentBean;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.model.rowmapper.BalanceComponentRowMapper;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DateUtil;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class CardLimitEnhancementRepo implements CardLimitEnhancementDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Override
    public ArrayList<OtbBean> getInitLimitEnhanceCustAcc() throws Exception {
        ArrayList<OtbBean> otbBeanList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        String sql = "SELECT DISTINCT cac.customerid, cac.accountno FROM  templimitincrement ti INNER JOIN card  c ON c.cardnumber = ti.cardno "
                + "INNER JOIN cardaccountcustomer cac ON cac.cardnumber = ti.cardno WHERE ti.status = ?  AND ti.startdate <= ? "
                + " AND cac.accountno NOT IN (SELECT ec.accountno  FROM  eoderrorcards ec WHERE ec.status = ? ) ORDER BY cac.customerid, cac.accountno";
        ;
        try {
            otbBeanList = (ArrayList<OtbBean>) backendJdbcTemplate.query(sql, new RowMapperResultSetExtractor<>((rs, rowNum) -> {
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

        String sql = "SELECT DISTINCT cac.customerid, cac.accountno FROM  templimitincrement ti INNER JOIN card  c ON c.cardnumber = ti.cardno "
                + "INNER JOIN cardaccountcustomer cac ON cac.cardnumber = c.maincardno  INNER JOIN eoderrorcards eec ON eec.accountno = cac.accountno  WHERE ti.status = ?  AND ti.startdate <= ? "
                + " AND eec.status = ? AND eec.eodid < ? AND eec.processstepid <= ?  ORDER BY  cac.customerid, cac.accountno ";
        ;
        try {
            otbBeanList =(ArrayList<OtbBean>) backendJdbcTemplate.query(sql,new RowMapperResultSetExtractor<>((rs, rowNum) -> {
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
    public ArrayList<BalanceComponentBean> getLimitEnhanceReqConCardList(String customerId, String accountNumber) throws Exception {
        ArrayList<BalanceComponentBean> balanceComponentBeanList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        String query = "SELECT TI.CARDNO, TI.AMOUNT, TI.INCREMENTTYPE, "
                + "TI.INCORDEC, TI.REQUESTID, C.CARDCATEGORYCODE, "
                + "TI.STARTDATE, TI.ENDDATE "
                + "FROM TEMPLIMITINCREMENT TI "
                + "INNER JOIN CARDACCOUNTCUSTOMER CAC ON CAC.CARDNUMBER = TI.CARDNO "
                + "INNER JOIN CARD C ON C.CARDNUMBER = CAC.CARDNUMBER "
                + "WHERE TI.STATUS = ? AND TI.STARTDATE <= ? "
                + "AND CAC.CUSTOMERID = ? AND CAC.ACCOUNTNO = ? ";
        try {
            balanceComponentBeanList = (ArrayList<BalanceComponentBean>) backendJdbcTemplate.query(query,
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
            String query = "UPDATE CARD "
                    + "SET OTBCREDIT = OTBCREDIT + ? , LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE "
                    + "WHERE CARDNUMBER = ?  ";

            backendJdbcTemplate.update(query, otbCredit, Configurations.EOD_USER, cardNumber.toString());

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCardCreditLimit(StringBuffer cardNumber, double otbCredit) throws Exception {
        try {
            String query = "UPDATE ECMS_ONLINE_CARD "
                    + "SET OTBCREDIT = OTBCREDIT + ?, LASTUPDATEUSER = ?, LASTUPDATETIME = SYSDATE "
                    + "WHERE CARDNUMBER=?  ";

            onlineJdbcTemplate.update(query, otbCredit, Configurations.EOD_USER, cardNumber.toString());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                infoLogger.info("================ updateCardCreditLimit ===================" + Integer.toString(Configurations.EOD_ID));
                infoLogger.info(query);
                infoLogger.info(Double.toString(otbCredit));
                infoLogger.info(Configurations.EOD_USER);
                infoLogger.info(CommonMethods.cardNumberMask(cardNumber));
                infoLogger.info("================ updateCardCreditLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateAccountCreditLimit(String accountNumber, double otbCredit) throws Exception {
        try {
            String query = "UPDATE CARDACCOUNT "
                    + "SET OTBCREDIT = OTBCREDIT + ? , LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE "
                    + "WHERE ACCOUNTNO = ? ";

            backendJdbcTemplate.update(query, otbCredit, Configurations.EOD_USER, accountNumber);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineAccountCreditLimit(String accountNumber, double otbCredit) throws Exception {
        try {
            String query = "UPDATE ECMS_ONLINE_ACCOUNT "
                    + "SET OTBCREDIT = OTBCREDIT + ? "
                    + "WHERE ACCOUNTNUMBER=?  ";

            onlineJdbcTemplate.update(query, otbCredit, accountNumber);

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                infoLogger.info("================ updateAccountCreditLimit ===================" + Integer.toString(Configurations.EOD_ID));
                infoLogger.info(query);
                infoLogger.info(Double.toString(otbCredit));
                infoLogger.info(Configurations.EOD_USER);
                infoLogger.info(accountNumber);
                infoLogger.info("================ updateAccountCreditLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateCustomerCreditLimit(String customerId, double otbCredit) throws Exception {
        try {
            String query = "UPDATE CARDCUSTOMER "
                    + "SET OTBCREDIT = OTBCREDIT + ? , LASTUPDATEDUSER=?, LASTUPDATEDTIME=SYSDATE "
                    + "WHERE CUSTOMERID = ?  ";

            backendJdbcTemplate.update(query, otbCredit, Configurations.EOD_USER, customerId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCustomerCreditLimit(String customerId, double otbCredit) {
        try {
            String query = "UPDATE ECMS_ONLINE_CUSTOMER "
                    + "SET OTBCREDIT=OTBCREDIT + ? "
                    + "WHERE CUSTOMERID=?  ";

            onlineJdbcTemplate.update(query, otbCredit, customerId);

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                infoLogger.info("================ updateCustomerCreditLimit ===================" + Integer.toString(Configurations.EOD_ID));
                infoLogger.info(query);
                infoLogger.info(Double.toString(otbCredit));
                infoLogger.info(customerId);
                infoLogger.info("================ updateCustomerCreditLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateCardCashLimit(StringBuffer cardNumber, double otbCash) throws Exception {
        try {
            String query = "UPDATE CARD "
                    + "SET OTBCASH = OTBCASH + ? , LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE "
                    + "WHERE CARDNUMBER = ?  ";

            backendJdbcTemplate.update(query, otbCash, Configurations.EOD_USER, cardNumber.toString());

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCardCashLimit(StringBuffer cardNumber, double otbCash) throws Exception {
        try {
            String query = "UPDATE ECMS_ONLINE_CARD "
                    + "SET OTBCASH = OTBCASH + ?, LASTUPDATEUSER = ?, LASTUPDATETIME = SYSDATE "
                    + "WHERE CARDNUMBER=?  ";

            onlineJdbcTemplate.update(query, otbCash, Configurations.EOD_USER, cardNumber.toString());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                infoLogger.info("================ updateCardCashtLimit ===================" + Integer.toString(Configurations.EOD_ID));
                infoLogger.info(query);
                infoLogger.info(Double.toString(otbCash));
                infoLogger.info(CommonMethods.cardNumberMask(cardNumber));
                infoLogger.info("================ updateCardCashtLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateAccountCashLimit(String accountNumber, double otbCash) throws Exception {
        try {
            String query = "UPDATE CARDACCOUNT "
                    + "SET OTBCASH = OTBCASH + ? , LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE "
                    + "WHERE ACCOUNTNO = ? ";

            backendJdbcTemplate.update(query, otbCash, Configurations.EOD_USER, accountNumber);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineAccountCashLimit(String accountNumber, double otbCash) throws Exception {
        try {
            String query = "UPDATE ECMS_ONLINE_ACCOUNT "
                    + "SET OTBCASH = OTBCASH + ? "
                    + "WHERE ACCOUNTNUMBER=?  ";

            onlineJdbcTemplate.update(query, otbCash, accountNumber);

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                infoLogger.info("================ updateAccountCashLimit ===================" + Integer.toString(Configurations.EOD_ID));
                infoLogger.info(query);
                infoLogger.info(Double.toString(otbCash));
                infoLogger.info(accountNumber);
                infoLogger.info("================ updateAccountCashLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateCustomerCashLimit(String customerId, double otbCash) throws Exception {
        try {
            String query = "UPDATE CARDCUSTOMER "
                    + "SET OTBCASH = OTBCASH + ? , LASTUPDATEDUSER=?, LASTUPDATEDTIME=SYSDATE "
                    + "WHERE CUSTOMERID = ?  ";

            backendJdbcTemplate.update(query, otbCash, Configurations.EOD_USER, customerId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateOnlineCustomerCashLimit(String customerId, double otbCash) throws Exception {
        try {
            String query = "UPDATE ECMS_ONLINE_CUSTOMER "
                    + "SET OTBCASH=OTBCASH + ? "
                    + "WHERE CUSTOMERID=?  ";

            onlineJdbcTemplate.update(query, otbCash, customerId);

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                infoLogger.info("================ updateCustomerCashLimit ===================" + Integer.toString(Configurations.EOD_ID));
                infoLogger.info(query);
                infoLogger.info(Double.toString(otbCash));
                infoLogger.info(customerId);
                infoLogger.info("================ updateCustomerCashLimit END ===================");
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateTempLimitIncrementTable(StringBuffer cardNumber, String status, String requestId) {
        try {
            java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);

            String query = "UPDATE TEMPLIMITINCREMENT SET STATUS =?, EFFECTIVESTARTDATE= SYSDATE, LASTUPDATEDUSER= ?, "
                    + " LASTUPDATEDTIME= SYSDATE,LASTEODUPDATEDDATE=? WHERE CARDNO = ? AND REQUESTID=? ";

            backendJdbcTemplate.update(query, status, Configurations.EOD_USER, eodDate, cardNumber.toString(), requestId);

        } catch (Exception e) {
            throw e;
        }
    }
}
