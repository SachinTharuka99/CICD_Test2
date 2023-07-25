package com.epic.cms.repository;

import com.epic.cms.dao.KnockOffDao;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Repository
public class KnockOffRepo implements KnockOffDao {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    StatusVarList statusList;
    @Autowired
    LogManager logManager;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;
    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public ArrayList<OtbBean> getInitKnockOffCustAcc() throws Exception {
        ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();

        try {
           // String query = "SELECT DISTINCT cac.customerid, cac.accountno FROM eomcardbalance emcb FULL OUTER JOIN eodcardbalance edcb ON edcb.cardnumber = emcb.cardnumber INNER JOIN eodpayment ep ON ep.cardnumber = emcb.cardnumber INNER JOIN eodpayment ep ON ep.cardnumber = emcb.cardnumber OR ep.cardnumber = edcb.cardnumber INNER JOIN card c ON c.cardnumber = emcb.cardnumber OR c.cardnumber = edcb.cardnumber INNER JOIN cardaccountcustomer cac ON cac.cardnumber = c.maincardno WHERE ( emcb.cumfinancialcharge > 0 OR emcb.cumcashadvance > 0 OR emcb.cumtransaction > 0 OR edcb.financialcharges > 0 OR edcb.cumcashadvances > 0 OR edcb.cumtransactions > 0 ) AND ep.forwardamount > 0 AND ep.status IN ( ?, ? ) AND cac.accountno NOT IN ( SELECT ec.accountno FROM eoderrorcards ec WHERE ec.status = ?)";

            custAccList = (ArrayList<OtbBean>) backendJdbcTemplate.query(queryParametersList.getKnockOff_getInitKnockOffCustAcc(),
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        OtbBean bean = new OtbBean();
                        bean.setCustomerid(result.getString("CUSTOMERID"));
                        bean.setAccountnumber(result.getString("ACCOUNTNO"));
                        return bean;
                    }),
                    statusList.getINITIAL_STATUS(), //INIT
                    statusList.getEOD_PENDING_STATUS(), //EPEN
                    statusList.getEOD_PENDING_STATUS() //EPEN
            );

        } catch (Exception e) {
            throw e;
        }
        return custAccList;
    }

    @Override
    public ArrayList<OtbBean> getErrorKnockOffCustAcc() throws Exception {
        ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();

        try {

           // String query = "SELECT DISTINCT cac.customerid, cac.accountno FROM eomcardbalance emcb FULL OUTER JOIN eodcardbalance edcb ON edcb.cardnumber = emcb.cardnumber INNER JOIN eodpayment ep ON ep.cardnumber = emcb.cardnumber OR ep.cardnumber = edcb.cardnumber INNER JOIN card  c ON c.cardnumber = emcb.cardnumber OR c.cardnumber = edcb.cardnumber INNER JOIN cardaccountcustomer cac ON cac.cardnumber = c.maincardno INNER JOIN eoderrorcards eec ON eec.accountno = cac.accountno WHERE ( emcb.cumfinancialcharge > 0 OR emcb.cumcashadvance > 0 OR emcb.cumtransaction > 0 OR edcb.financialcharges > 0 OR edcb.cumcashadvances > 0 OR edcb.cumtransactions > 0 ) AND ep.forwardamount > 0 AND ep.status IN ( ?, ? ) AND eec.eodid < ? AND eec.processstepid <= ?";

            custAccList = (ArrayList<OtbBean>) backendJdbcTemplate.query(queryParametersList.getKnockOff_getErrorKnockOffCustAcc(),
                    new RowMapperResultSetExtractor<OtbBean>((result, rowNum) -> {
                        OtbBean bean = new OtbBean();
                        bean.setCustomerid(result.getString("CUSTOMERID"));
                        bean.setAccountnumber(result.getString("ACCOUNTNO"));
                        return bean;
                    }),
                    statusList.getINITIAL_STATUS(),
                    statusList.getEOD_PENDING_STATUS(),
                    Configurations.ERROR_EOD_ID,
                    Configurations.PROCESS_STEP_ID
            );

        } catch (Exception e) {
            throw e;
        }
        return custAccList;
    }

    @Override
    public ArrayList<OtbBean> getKnockOffCardList(String customerid, String accountnumber)  {
        ArrayList<OtbBean> cardList = new ArrayList<OtbBean>();

        try {
            //String query = "SELECT DISTINCT ep.cardnumber cardnumber FROM eomcardbalance emcb FULL OUTER JOIN eodcardbalance edcb ON edcb.cardnumber = emcb.cardnumber INNER JOIN eodpayment ep ON ep.cardnumber = emcb.cardnumber OR ep.cardnumber = edcb.cardnumber INNER JOIN card c ON c.cardnumber = emcb.cardnumber OR c.cardnumber = edcb.cardnumber INNER JOIN cardaccountcustomer cac ON cac.cardnumber = c.maincardno WHERE ( emcb.cumfinancialcharge > 0 OR emcb.cumcashadvance > 0 OR emcb.cumtransaction > 0 OR edcb.financialcharges > 0 OR edcb.cumcashadvances > 0 OR edcb.cumtransactions > 0 ) AND ep.forwardamount > 0 AND ep.status IN ( ?, ? ) AND cac.customerid = ? AND cac.accountno = ?";

            cardList = (ArrayList<OtbBean>) backendJdbcTemplate.query(queryParametersList.getKnockOff_getKnockOffCardList(),
                    new RowMapperResultSetExtractor<OtbBean>((result, rowNum) -> {
                        OtbBean otbBean = new OtbBean();
                        otbBean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                        return otbBean;
                    }),
                    statusList.getINITIAL_STATUS(), //INIT
                    statusList.getEOD_PENDING_STATUS(), //EPEN
                    customerid,
                    accountnumber
            );

        } catch (Exception e) {
            throw e;
        }
        return cardList;
    }

    @Override
    public OtbBean getMainCard(String accountnumber)  {
        OtbBean mainCardBean = null;

        try {
            //String query = "SELECT cardnumber FROM cardaccount WHERE accountno = ?";

            mainCardBean = backendJdbcTemplate.queryForObject(queryParametersList.getKnockOff_getMainCard(), new RowMapper<>() {
                        @Override
                        public OtbBean mapRow(ResultSet result, int rowNum) throws SQLException {
                            OtbBean mainCardBean = new OtbBean();
                            mainCardBean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                            return mainCardBean;
                        }
                    },
                    accountnumber
            );
        } catch (EmptyResultDataAccessException e) {
            throw e;
        }
        return mainCardBean;
    }

    @Override
    public ArrayList<OtbBean> getPaymentList(StringBuffer cardnumber) throws Exception {
        ArrayList<OtbBean> paymentList = new ArrayList<OtbBean>();

        try {
            //String query = "SELECT id, forwardamount, isprimary FROM eodpayment WHERE forwardamount > 0 AND status IN ( ?, ? ) AND cardnumber = ? AND eodid = ? ORDER BY lastupdateddate";

            paymentList = backendJdbcTemplate.query(queryParametersList.getKnockOff_getPaymentList(),
                    (ResultSet result) -> {
                        ArrayList<OtbBean> tempPaymentList = new ArrayList<OtbBean>();
                        while (result.next()) {
                            OtbBean paymentBean = new OtbBean();
                            paymentBean.setId(result.getInt("ID"));
                            paymentBean.setPayment(result.getDouble("FORWARDAMOUNT"));
                            paymentBean.setIsPrimary(result.getString("ISPRIMARY"));
                            tempPaymentList.add(paymentBean);
                        }
                        return tempPaymentList;
                    },
                    statusList.getINITIAL_STATUS(), //INIT
                    statusList.getEOD_PENDING_STATUS(), //EPEN
                    cardnumber.toString(),
                    Configurations.EOD_ID
            );

        } catch (Exception e) {
            throw e;
        }
        return paymentList;
    }

    @Override
    public OtbBean getEomKnockOffAmount(StringBuffer cardnumber) throws Exception {
        OtbBean eomBean = null;

        try {
            //String query = "SELECT CARDNUMBER, CUMFINANCIALCHARGE, CUMCASHADVANCE, CUMTRANSACTION FROM EOMCARDBALANCE WHERE (CUMFINANCIALCHARGE > 0 OR CUMCASHADVANCE > 0 OR CUMTRANSACTION > 0) AND CARDNUMBER = ?";

            eomBean = backendJdbcTemplate.queryForObject(queryParametersList.getKnockOff_getEomKnockOffAmount(), new RowMapper<>() {
                        @Override
                        public OtbBean mapRow(ResultSet result, int rowNum) throws SQLException {
                            OtbBean eomBean = new OtbBean();
                            eomBean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                            eomBean.setFinacialcharges(result.getDouble("CUMFINANCIALCHARGE"));
                            eomBean.setCumcashadvance(result.getDouble("CUMCASHADVANCE"));
                            eomBean.setCumtransactions(result.getDouble("CUMTRANSACTION"));
                            return eomBean;
                        }
                    },
                    cardnumber.toString()
            );

        } catch (EmptyResultDataAccessException e) {
            return eomBean;
        } catch (Exception e) {
            throw e;
        }
        return eomBean;
    }

    @Override
    public OtbBean getEodKnockOffAmount(StringBuffer cardnumber) throws Exception {
        OtbBean eodBean = null;

        try {
            //String query = "SELECT cardnumber, financialcharges, cumcashadvances, cumtransactions FROM eodcardbalance WHERE ( financialcharges > 0 OR cumcashadvances > 0 OR cumtransactions > 0 ) AND cardnumber = ?";

            eodBean = backendJdbcTemplate.queryForObject(queryParametersList.getKnockOff_getEodKnockOffAmount(), new RowMapper<>() {
                        @Override
                        public OtbBean mapRow(ResultSet result, int rowNum) throws SQLException {
                            OtbBean eodBean = new OtbBean();
                            eodBean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                            eodBean.setFinacialcharges(result.getDouble("FINANCIALCHARGES"));
                            eodBean.setCumcashadvance(result.getDouble("CUMCASHADVANCES"));
                            eodBean.setCumtransactions(result.getDouble("CUMTRANSACTIONS"));
                            return eodBean;
                        }
                    },
                    cardnumber.toString()
            );

        } catch (EmptyResultDataAccessException e) {
            return eodBean;
        } catch (Exception e) {
            throw e;
        }
        return eodBean;
    }

    @Override
    public int updateEodPayment(int id, double mainFinancialCharges, double mainCashAdvances, double mainTransactions, double supFinancialCharges, double supCashAdvances, double supTransactions, double forwardAmount, String status) throws Exception {
        int count = 0;

        try {
           // String query = "UPDATE EODPAYMENT SET MAINFINCHARGEKNOCKOFF = ?, MAINCASHADVANCEKNOCKOFF = ?, MAINTRANSACTIONKNOCKOFF = ?, SUPFINCHARGEKNOCKOFF = ?, SUPCASHADVANCEKNOCKOFF = ?, SUPTRANSACTIONKNOCKOFF = ?, FORWARDAMOUNT = ?, STATUS = ? WHERE ID = ?";

            count = backendJdbcTemplate.update(queryParametersList.getKnockOff_updateEodPayment(),
                    mainFinancialCharges,
                    mainCashAdvances,
                    mainTransactions,
                    supFinancialCharges,
                    supCashAdvances,
                    supTransactions,
                    forwardAmount,
                    status,
                    id
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCardOtb(OtbBean cardBean) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE CARD SET OTBCREDIT = OTBCREDIT - ?, OTBCASH = OTBCASH - ?, TEMPCASHAMOUNT = TEMPCASHAMOUNT + ?, LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE CARDNUMBER = ?";

            count = backendJdbcTemplate.update(queryParametersList.getKnockOff_updateCardOtb(),
                    cardBean.getOtbcredit(),
                    cardBean.getOtbcash(),
                    cardBean.getOtbcash(),
                    Configurations.EOD_USER,
                    cardBean.getCardnumber().toString()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEodClosingBalance(StringBuffer cardNumber, double closingBalance) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE eodcardbalance SET eodclosingbal = eodclosingbal - ?, lastupdateduser = ?, lastupdatedtime = sysdate WHERE cardnumber = ?";

            count = backendJdbcTemplate.update(queryParametersList.getKnockOff_updateEodClosingBalance(),
                    closingBalance,
                    Configurations.EOD_USER,
                    cardNumber.toString()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEOMCARDBALANCE(OtbBean cardBean) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE eomcardbalance SET cumfinancialcharge = ?, cumcashadvance = ?, cumtransaction = ? WHERE cardnumber = ?";

            count = backendJdbcTemplate.update(queryParametersList.getKnockOff_updateEOMCARDBALANCE(),
                    cardBean.getFinacialcharges(),
                    cardBean.getCumcashadvance(),
                    cardBean.getCumtransactions(),
                    cardBean.getCardnumber().toString()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateEODCARDBALANCE(OtbBean cardBean) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE eodcardbalance SET financialcharges = ?, cumcashadvances = ?, cumtransactions = ?, payments = ? WHERE cardnumber = ?";

            count = backendJdbcTemplate.update(queryParametersList.getKnockOff_updateEODCARDBALANCE(),
                    cardBean.getFinacialcharges(),
                    cardBean.getCumcashadvance(),
                    cardBean.getCumtransactions(),
                    cardBean.getCumpayment(),
                    cardBean.getCardnumber().toString()
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCardComp(OtbBean cardBean) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE card SET otbcredit = otbcredit - ?, otbcash = otbcash - ?, tempcashamount = tempcashamount + ?, lastupdateduser = ?, lastupdatedtime = sysdate WHERE cardnumber = ?";

            count = backendJdbcTemplate.update(queryParametersList.getKnockOff_updateCardComp(),
                    cardBean.getOtbcredit(),
                    cardBean.getOtbcash(),
                    cardBean.getTmpcash(),
                    Configurations.EOD_USER,
                    cardBean.getCardnumber().toString()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateAccountOtb(OtbBean otbBean) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE CARDACCOUNT SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ?,LASTUPDATEDUSER = ?, LASTUPDATEDTIME = SYSDATE WHERE ACCOUNTNO=?";

            count = backendJdbcTemplate.update(queryParametersList.getKnockOff_updateAccountOtb(),
                    otbBean.getOtbcredit(),
                    otbBean.getOtbcash(),
                    Configurations.EOD_USER,
                    otbBean.getAccountnumber()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCustomerOtb(OtbBean bean) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE CARDCUSTOMER SET OTBCREDIT= OTBCREDIT - ?,OTBCASH= OTBCASH - ?,LASTUPDATEDUSER=?,LASTUPDATEDTIME=SYSDATE WHERE CUSTOMERID=?";

            count = backendJdbcTemplate.update(queryParametersList.getKnockOff_updateCustomerOtb(),
                    bean.getOtbcredit(),
                    bean.getOtbcash(),
                    Configurations.EOD_USER,
                    bean.getCustomerid()
            );

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int OnlineupdateCardOtb(OtbBean supCardBean) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE ecms_online_card SET otbcredit = otbcredit - ?,otbcash = otbcash - ?,tempcashamount = tempcashamount + ?,lastupdateuser = ?,lastupdatetime = sysdate WHERE cardnumber = ?";

            count = onlineJdbcTemplate.update(queryParametersList.getKnockOff_OnlineupdateCardOtb(),
                    supCardBean.getOtbcredit(),
                    supCardBean.getOtbcash(),
                    supCardBean.getTmpcash(),
                    Configurations.EOD_USER,
                    supCardBean.getCardnumber().toString()
            );

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateCardCreditLimit ===================" + Configurations.EOD_ID);
                logInfo.info(queryParametersList.getKnockOff_OnlineupdateCardOtb());
                logInfo.info(Double.toString(supCardBean.getOtbcredit()));
                logInfo.info(Double.toString(supCardBean.getOtbcash()));
                logInfo.info(Double.toString(supCardBean.getTmpcredit()));
                logInfo.info(Double.toString(supCardBean.getTmpcash()));
                logInfo.info(Double.toString(supCardBean.getTmpcash()));
                logInfo.info(CommonMethods.cardNumberMask(supCardBean.getCardnumber()));
                logInfo.info("================ updateCardCreditLimit END ===================");
            }

        } catch (Exception e) {
            logError.error("Online update CardOtb Error");
            throw e;
        }
        return count;
    }

    @Override
    public int OnlineupdateAccountOtb(OtbBean custAccBean) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE ECMS_ONLINE_ACCOUNT SET OTBCREDIT = OTBCREDIT - ?,OTBCASH = OTBCASH - ? WHERE ACCOUNTNUMBER=?";

            count = onlineJdbcTemplate.update(queryParametersList.getKnockOff_OnlineupdateAccountOtb(),
                    custAccBean.getOtbcredit(),
                    custAccBean.getOtbcash(),
                    custAccBean.getAccountnumber()
            );

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateCardCreditLimit ===================" + Configurations.EOD_ID);
                logInfo.info(queryParametersList.getKnockOff_OnlineupdateAccountOtb());
                logInfo.info(Double.toString(custAccBean.getOtbcredit()));
                logInfo.info(Double.toString(custAccBean.getOtbcash()));
                logInfo.info(custAccBean.getAccountnumber());
                logInfo.info("================ updateCardCreditLimit END ===================");
            }

        } catch (Exception e) {
            logError.error("Online Update AccountOtb Error");
            throw e;
        }
        return count;
    }

    @Override
    public int OnlineupdateCustomerOtb(OtbBean custAccBean) throws Exception {
        int count = 0;

        try {
            //String query = "UPDATE ECMS_ONLINE_CUSTOMER SET OTBCREDIT= OTBCREDIT - ?,OTBCASH= OTBCASH - ? WHERE CUSTOMERID = ?";

            count = onlineJdbcTemplate.update(queryParametersList.getKnockOff_OnlineupdateCustomerOtb(),
                    custAccBean.getOtbcredit(),
                    custAccBean.getOtbcash(),
                    custAccBean.getCustomerid()
            );

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateCustomerOtb ===================" + Configurations.EOD_ID);
                logInfo.info(queryParametersList.getKnockOff_OnlineupdateCustomerOtb());
                logInfo.info(Double.toString(custAccBean.getOtbcredit()));
                logInfo.info(Double.toString(custAccBean.getOtbcash()));
                logInfo.info(custAccBean.getCustomerid());
                logInfo.info("================ updateCustomerOtb END ===================");
            }

        } catch (Exception e) {
            logError.error("Online Update CustomerOtb Error");
            throw e;
        }
        return count;
    }
}
