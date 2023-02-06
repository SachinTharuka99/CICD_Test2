package com.epic.cms.repository;

import com.epic.cms.dao.AlertDao;
import com.epic.cms.model.bean.AlertBean;
import com.epic.cms.model.bean.CashBackAlertBean;
import com.epic.cms.model.bean.LetterGenerationReferanceTableDetailsBean;
import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.util.*;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCountCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.epic.cms.util.CommonMethods.validate;
import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class AlertRepo implements AlertDao {

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList statusList;

    @Autowired
    LastStatementSummaryRepo statementSummaryRepo;

    @Override
    @Transactional("backendDb")
    public double getParameterValueForMinPaymentDue(StringBuffer cardNumber, String accNo) throws Exception {
        double minPaymentDue = 0.00;
        double payments = 0.00;
        try {

            CreateEodId convertToEOD = new CreateEodId();

            StatementBean stmtBean = statementSummaryRepo.getLastStatementDetails(cardNumber);
            int statementDayEODID = Integer.parseInt(convertToEOD.getDate(stmtBean.getStatementEndDate()) + "00");
            double minAmount = stmtBean.getTotalMinPayment();
            minPaymentDue = minAmount;

            payments = commonRepo.getPaymentAmount(accNo, statementDayEODID, statusList.getINITIAL_STATUS());

            if (payments > 0) {
                minPaymentDue = (minAmount - payments);
                minPaymentDue = minPaymentDue <= 0 ? 0.00 : minPaymentDue;
            }

        } catch (Exception ex) {
            throw ex;
        }
        return minPaymentDue;
    }

    @Override
    @Transactional("backendDb")
    public boolean getActiveTemplate(String templateCode, String template) throws Exception {
        boolean isActive = false;
        String query = "";
        try {
            if (template.equals(Configurations.EMAIL_TEMPLATE)) {
                query = "SELECT TEMPLATECODE FROM EMAILTEMPLATE WHERE TEMPLATECODE = ? AND STATUS = ? ";
            } else if (template.equals(Configurations.SMS_TEMPLATE)) {
                query = "SELECT TEMPLATECODE FROM SMSTEMPLATE WHERE TEMPLATECODE = ? AND STATUS = ? ";
            }

            RowCountCallbackHandler countCallback = new RowCountCallbackHandler();
            backendJdbcTemplate.query(query, countCallback, templateCode, Configurations.ACTIVE_STATUS);
            int rowCount = countCallback.getRowCount();

            if (rowCount > 0) {
                isActive = true;
            }

        } catch (Exception e) {
            infoLogger.error("Exception ", e);
            throw e;
        }
        return isActive;
    }

    @Override
    @Transactional("backendDb")
    public ArrayList<String> getParametersFromTemplate(String templateCode, String template) throws Exception {
        ArrayList<String> parameterList = new ArrayList<>();
        try {
            String query = "";

            if (template.equals(Configurations.EMAIL_TEMPLATE)) {
                query = "SELECT BODY FROM EMAILTEMPLATE WHERE TEMPLATECODE=? ";
            } else if (template.equals(Configurations.SMS_TEMPLATE)) {
                query = "SELECT MESSAGEBODY FROM SMSTEMPLATE WHERE TEMPLATECODE=? ";
            }

            backendJdbcTemplate.query(query, (ResultSet result) -> {
                while (result.next()) {
                    String body = "";
                    if (template.equals(Configurations.EMAIL_TEMPLATE)) {
                        body = result.getString("BODY");
                    } else if (template.equals(Configurations.SMS_TEMPLATE)) {
                        body = result.getString("MESSAGEBODY");
                    }

                    // seperate string by using |
                    Pattern p = Pattern.compile("\\|(.*?)\\|");
                    Matcher m = p.matcher(body);

                    while (m.find()) {
                        parameterList.add(m.group(1));
                    }
                }
                return parameterList;
            }, templateCode);
        } catch (Exception e) {
            errorLogger.error("Exception ", e);
            throw e;
        }
        return parameterList;
    }

    @Override
    @Transactional("backendDb")
    public LetterGenerationReferanceTableDetailsBean getFieldDetails(String templateCode) throws Exception {
        LetterGenerationReferanceTableDetailsBean bean = new LetterGenerationReferanceTableDetailsBean();
        try {
            String identificationFeild = "CUSTOMERID";
            String tableName = "TABLENAMEMAINCARD";// TABLENAMEMAINCARD
            String fieldName = "TABLEFIELD";

            String query = "SELECT * FROM LETTERFIELDDETAILS";

            backendJdbcTemplate.query(query, (result, rowNum) -> {
                //LetterGenerationReferanceTableDetailsBean bean = new LetterGenerationReferanceTableDetailsBean();
                String[] referenceTableDetails;
                if (result.getString("FIELDNAME").equals("NAME")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = identificationFeild;
                    bean.setNAME(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("ADDRESS1")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = identificationFeild;
                    bean.setADDRESS1(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("ADDRESS2")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = identificationFeild;
                    bean.setADDRESS2(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("ADDRESS3")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = identificationFeild;
                    bean.setADDRESS3(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CARDNO")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNUMBER";
                    bean.setCARDNO(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CREDITLIMIT")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNUMBER";
                    bean.setCREDITLIMIT(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("TITLE")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = identificationFeild;
                    bean.setTITLE(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("BODY")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = identificationFeild;
                    bean.setBODY(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("TEXT")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = identificationFeild;
                    bean.setTEXT(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("BRANCH")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = identificationFeild;
                    bean.setBRANCH(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("AMOUNT")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = identificationFeild;
                    bean.setAMOUNT(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("FULLPAYMENT")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNO";
                    bean.setFULLPAYMENT(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("MINPAYEMENT")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNO";
                    bean.setMINPAYEMENT(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("OLDCARDNO")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "OLDCARDNUMBER";
                    bean.setOLDCARDNO(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CARDPRODUCT")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "PRODUCTCODE";
                    bean.setCARDPRODUCT(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("STMTENDDATE")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNO";
                    bean.setSTATEMENTENDDATE(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("STMTOUSTANDING")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNO";
                    bean.setSTMTOUSTANDING(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("DUEDATE")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNO";
                    bean.setDUEDATE(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CBACCNO")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "ACCOUNTNUMBER";
                    bean.setCBACCNO(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CBAMOUNT")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "ACCOUNTNUMBER";
                    bean.setCBAMOUNT(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CBSTATENDDATE")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNO";
                    bean.setCBSTATENDDATE(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CARDAVAILABLE")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNUMBER";
                    bean.setCARDAVAILABLE(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CARDOUSTANDING")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNUMBER";
                    bean.setCARDOUSTANDING(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("ACCAVAILABLE")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "ACCOUNTNO";
                    bean.setACCAVAILABLE(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CURRENTNDIA")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "ACCOUNTNO";
                    bean.setCURRENTNDIA(referenceTableDetails);
                } else if (result.getString("FIELDNAME").equals("CARDEXPDATE")) {
                    referenceTableDetails = new String[3];
                    referenceTableDetails[0] = result.getString(tableName);
                    referenceTableDetails[1] = result.getString(fieldName);
                    referenceTableDetails[2] = "CARDNUMBER";
                    bean.setCARDEXPDATE(referenceTableDetails);
                }
                return bean;
            });
        } catch (Exception e) {
            errorLogger.error("Exception ", e);
            throw e;
        }
        return bean;
    }

    @Override
    @Transactional("backendDb")
    public String getParameterValueForCardLetters(String[] tableIdentificationList, StringBuffer no) throws Exception {
        String value = "--";
        String query = null;
        try {
            if (tableIdentificationList[2].equals("CUSTOMERID")) {
                query = "SELECT x." + tableIdentificationList[1]
                        + " FROM " + tableIdentificationList[0] + " "
                        + "x,CARDACCOUNTCUSTOMER CAC WHERE CAC.CARDNUMBER ="
                        + no.toString() + " " + "AND x." + tableIdentificationList[2]
                        + "= CAC.CUSTOMERID";
            } else if (tableIdentificationList[2].equals("PRODUCTCODE")) {
                query = "SELECT x." + tableIdentificationList[1]
                        + " FROM " + tableIdentificationList[0] + " "
                        + "x,card ca WHERE ca.cardproduct = x." + tableIdentificationList[2] + " and ca.cardnumber=" + no.toString();
            } else if (tableIdentificationList[0].equals("CASHBACK")) {
                query = "SELECT x." + tableIdentificationList[1]
                        + " FROM " + tableIdentificationList[0] + " "
                        + "x WHERE x." + tableIdentificationList[2] + "=" + no.toString() + " and x.eodid=" + Configurations.EOD_ID;
            } else {
                query = "SELECT x." + tableIdentificationList[1]
                        + " FROM " + tableIdentificationList[0] + " "
                        + "x WHERE x." + tableIdentificationList[2] + "=" + no.toString();
            }

            value = backendJdbcTemplate.queryForObject(query, String.class);
        } catch (Exception e) {
            throw e;
        }
        return value;
    }

    @Override
    @Transactional("backendDb")
    public StringBuffer getCardNoParameterValueForCardLetters(String[] tableIdentificationList, StringBuffer cardNumber) throws Exception {
        StringBuffer value = new StringBuffer("--");
        String query = null;
        try {
            if (tableIdentificationList[2].equals("CUSTOMERID")) {
                query = "SELECT x." + tableIdentificationList[1]
                        + " FROM " + tableIdentificationList[0] + " "
                        + "x,CARDACCOUNTCUSTOMER CAC WHERE CAC.CARDNUMBER ="
                        + cardNumber.toString() + " " + "AND x." + tableIdentificationList[2]
                        + "= CAC.CUSTOMERID";
            } else if (tableIdentificationList[2].equals("PRODUCTCODE")) {
                query = "SELECT x." + tableIdentificationList[1]
                        + " FROM " + tableIdentificationList[0] + " "
                        + "x,card ca WHERE ca.cardproduct = x." + tableIdentificationList[2] + " and ca.cardnumber=" + cardNumber.toString();
            } else if (tableIdentificationList[0].equals("CASHBACK")) {
                query = "SELECT x." + tableIdentificationList[1]
                        + " FROM " + tableIdentificationList[0] + " "
                        + "x WHERE x." + tableIdentificationList[2] + "=" + cardNumber.toString() + " and x.eodid=" + Configurations.EOD_ID;
            } else {
                query = "SELECT x." + tableIdentificationList[1]
                        + " FROM " + tableIdentificationList[0] + " "
                        + "x WHERE x." + tableIdentificationList[2] + "=" + cardNumber.toString();
            }

            value = backendJdbcTemplate.queryForObject(query, StringBuffer.class);
        } catch (Exception e) {
            throw e;
        }
        return value;
    }

    @Override
    @Transactional("backendDb")
    public String getTemplateBody(String templateCode, String template) throws Exception {
        String body = "";
        try {
            String query = "";
            if (template.equals(Configurations.EMAIL_TEMPLATE)) {
                query = "SELECT BODY FROM EMAILTEMPLATE WHERE TEMPLATECODE=? ";
                body = backendJdbcTemplate.queryForObject(query, String.class, templateCode);

            } else if (template.equals(Configurations.SMS_TEMPLATE)) {
                query = "SELECT MESSAGEBODY FROM SMSTEMPLATE WHERE TEMPLATECODE=? ";
                body = backendJdbcTemplate.queryForObject(query, String.class, templateCode);
            }
        } catch (Exception e) {
            throw e;
        }
        return body;
    }

    @Override
    @Transactional(value = "onlineDb", propagation = Propagation.NESTED, isolation = Isolation.SERIALIZABLE)
    public void insertAlertIntoOnline(AlertBean alertBean) throws Exception {
        try {
            String alertId = new SimpleDateFormat("yyMMddHHmmssSSS").format(new java.util.Date()) + alertBean.getCardNumber() + alertBean.getAlertType();

            String query = "INSERT INTO ECMS_ONLINE_BACKEND_ALERTS ( ALERT_ID, ALERT_TYPE, CARDNO, MESSAGE, EMAIL_ID, MOBILE_NO, STATUS, CREATETIME, LASTUPDATETIME ) VALUES ( ?, " /*ALERT_ID*/ + "?, " /*ALERT_TYPE*/ + "?, " /*CARDNO*/ + "?, " /*MESSAGE*/ + "?, " /*EMAIL_ID*/ + "?, " /*MOBILE_NO*/ + "?, " /*STATUS*/ + "SYSDATE, " /*CREATETIME*/ + "SYSDATE " /*LASTUPDATETIME*/ + ")";

            onlineJdbcTemplate.update(query, alertId, alertBean.getAlertType(),
                    alertBean.getMaskedCardNumber(),
                    alertBean.getMessage(),
                    alertBean.getEmail(),
                    alertBean.getMobileNo(),
                    alertBean.getStatus());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional("backendDb")
    public AlertBean setValuesToBean(AlertBean alertBean, StringBuffer cardNumber) throws Exception {
        try {
            alertBean.setStatus(Configurations.ALERTUNUNREAD);

            String sql = "SELECT ( CASE WHEN C.CARDCATEGORYCODE IN ('M', 'A', 'CO') THEN (SELECT CM.MOBILENO FROM CARDMAINCUSTOMERDETAIL CM WHERE CM.CUSTOMERID = CAC.CUSTOMERID) WHEN C.CARDCATEGORYCODE IN ('F') THEN (SELECT CM.MOBILENO FROM CARDFDCUSTOMERDETAIL CM WHERE CM.CUSTOMERID = CAC.CUSTOMERID) WHEN C.CARDCATEGORYCODE IN ('E') THEN (SELECT CM.CONTACTNUMBERSMOBILE FROM CARDESTCUSTOMERDETAILS CM WHERE CM.CUSTOMERID = CAC.CUSTOMERID) END ) AS MOBILENO, ( CASE WHEN C.CARDCATEGORYCODE IN ('M', 'A', 'CO') THEN (SELECT CM.EMAIL FROM CARDMAINCUSTOMERDETAIL CM WHERE CM.CUSTOMERID = CAC.CUSTOMERID) WHEN C.CARDCATEGORYCODE IN ('F') THEN (SELECT CM.EMAIL FROM CARDFDCUSTOMERDETAIL CM WHERE CM.CUSTOMERID = CAC.CUSTOMERID ) WHEN C.CARDCATEGORYCODE IN ('E') THEN (SELECT CM.CONTACTEMAIL FROM CARDESTCUSTOMERDETAILS CM WHERE CM.CUSTOMERID = CAC.CUSTOMERID) END ) AS EMAIL FROM CARDACCOUNTCUSTOMER CAC INNER JOIN CARD C ON CAC.CARDNUMBER = C.CARDNUMBER WHERE CAC.CARDNUMBER = ? ";

            alertBean = Objects.requireNonNull(backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                AlertBean bean = new AlertBean();
                while (rs.next()) {
                    bean.setMobileNo(rs.getString("MOBILENO"));
                    bean.setEmail(rs.getString("EMAIL"));
                }
                return bean;
            }, cardNumber.toString()));
        } catch (Exception e) {
            throw e;
        }
        return alertBean;
    }

    @Override
    @Transactional("backendDb")
    public AlertBean setEmail_Number(AlertBean alertBean) throws Exception {
        try {
            alertBean.setStatus(Configurations.ALERTUNUNREAD);
            String query = "SELECT ADMINEMAIL,ADMINTELNO FROM COMMONPARAMETER ";

            alertBean = (AlertBean) backendJdbcTemplate.query(query, (rs, rowNum) -> {
                AlertBean bean = new AlertBean();
                bean.setMobileNo(rs.getString("ADMINTELNO"));
                bean.setEmail(rs.getString("ADMINEMAIL"));
                return bean;
            });
        } catch (Exception e) {
            throw e;
        }
        return alertBean;
    }

    @Override
    @Transactional(value = "onlineDb", propagation = Propagation.NESTED, isolation = Isolation.SERIALIZABLE)
    public void insertAlertIntoOnlineAdmin(AlertBean bean, String triggerPoint) throws Exception {
        String triggerIndex = null;

        if (triggerPoint.equals(Configurations.STATEMENT_DATE)) {
            triggerIndex = "005";
        } else if (triggerPoint.equals(Configurations.BEFORE_DUE_DATE)) {
            triggerIndex = "006";
        } else if (triggerPoint.equals(Configurations.AUTO_SETTLEMENT_GENERATE_DATE)) {
            triggerIndex = "007";
        } else if (triggerPoint.equals(Configurations.CRIB_FILE_GENERATE_DATE)) {
            triggerIndex = "008";
        } else if (triggerPoint.equals(Configurations.CASHBACK_REDEEM)) {
            triggerIndex = "009";
        }

        String alertId = new SimpleDateFormat("yyMMddHHmmssSSS").format(new java.util.Date()) + validate(Integer.toString(bean.getAlertType()), 3, '0') + triggerIndex;

        String query = "INSERT INTO ECMS_ONLINE_BACKEND_ALERTS "
                + "( "
                + "ALERT_ID, "
                + "ALERT_TYPE, "
                + "CARDNO, "
                + "MESSAGE, "
                + "EMAIL_ID, "
                + "MOBILE_NO, "
                + "STATUS, "
                + "CREATETIME, "
                + "LASTUPDATETIME "
                + ") "
                + "VALUES "
                + "( "
                + "?, " //ALERT_ID
                + "?, " //ALERT_TYPE
                + "?, " //CARDNO
                + "?, " //MESSAGE
                + "?, " //EMAIL_ID
                + "?, " //MOBILE_NO
                + "?, " //STATUS
                + "SYSDATE, " //CREATETIME
                + "SYSDATE " //LASTUPDATETIME
                + ")";
        try {
            onlineJdbcTemplate.update(query, alertId, bean.getAlertType(), bean.getMaskedCardNumber(), bean.getMessage(), bean.getEmail(), bean.getMobileNo(), bean.getStatus());
        } catch (Exception e) {
            throw e;
        }
    }

}
