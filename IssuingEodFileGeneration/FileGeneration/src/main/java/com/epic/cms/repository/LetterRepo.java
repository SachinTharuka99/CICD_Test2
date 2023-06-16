package com.epic.cms.repository;

import com.epic.cms.dao.LetterDao;
import com.epic.cms.model.bean.LetterGenerationReferanceTableDetailsBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class LetterRepo implements LetterDao {

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public ArrayList<String> getParametersInLetterTemplate(String tempCode, String cardProduct) throws Exception {
        ArrayList<String> parameterList = new ArrayList<String>();

        try {

            String query = "SELECT BODY FROM LETTERTEMPLATE WHERE TEMPLATECODE=? AND CARDPRODUCT=?";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            String body = rs.getString("BODY");

                            // seperate string by using |
                            Pattern p = Pattern.compile("\\|(.*?)\\|");
                            Matcher m = p.matcher(body);

                            while (m.find()) {
                                parameterList.add(m.group(1));
                                // System.out.println(m.group(1));
                            }
                        }
                        return parameterList;
                    },
                    tempCode,
                    cardProduct
            );

        } catch (Exception e) {
            throw e;
        }
        return parameterList;
    }

    @Override
    public LetterGenerationReferanceTableDetailsBean getLetterFieldDetails(String cardType, String tempCode) throws Exception {
        LetterGenerationReferanceTableDetailsBean bean = new LetterGenerationReferanceTableDetailsBean();

        try {

            String tableName = null;
            String fieldName = null;
            String identificationFeild = null;

            if (tempCode.equals(Configurations.APPLICATION_REJECTION_LETTER_CODE)) {
                identificationFeild = "APPLICATIONID";
                if (cardType.equals(Configurations.CARD_CATEGORY_MAIN)
                        || cardType.equals(Configurations.CARD_CATEGORY_AFFINITY)
                        || cardType.equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                    tableName = "TABLENAMEMAIN";
                    fieldName = "TABLEFIELD";
                } else if (cardType.equals(Configurations.CARD_CATEGORY_SUPPLEMENTORY)
                        || cardType.equals(Configurations.CARD_CATEGORY_AFFINITY_SUPPLEMENTORY)
                        || cardType.equals(Configurations.CARD_CATEGORY_CO_BRANDED_SUPPLEMENTORY)
                        || cardType.equals(Configurations.CARD_CATEGORY_FD_SUPPLEMENTORY)) {
                    tableName = "TABLENAMESUP";
                    fieldName = "TABLEFIELD";
                } else if (cardType.equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)) {
                    tableName = "TABLENAMEEST";
                    fieldName = "TABLEFIELDEST";
                } else if (cardType.equals(Configurations.CARD_CATEGORY_CORPORATE)) {
                    tableName = "TABLENAMECOP";
                    fieldName = "TABLEFIELD";
                } else if (cardType.equals(Configurations.CARD_CATEGORY_FD)) {
                    tableName = "TABLENAMEFD";
                    fieldName = "TABLEFIELD";
                }
            } else {
                identificationFeild = "CUSTOMERID";
                if (cardType.equals(Configurations.CARD_CATEGORY_MAIN)
                        || cardType.equals(Configurations.CARD_CATEGORY_AFFINITY)
                        || cardType.equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                    tableName = "TABLENAMEMAINCARD";
                    fieldName = "TABLEFIELD";
                } else if (cardType.equals(Configurations.CARD_CATEGORY_SUPPLEMENTORY)
                        || cardType.equals(Configurations.CARD_CATEGORY_AFFINITY_SUPPLEMENTORY)
                        || cardType.equals(Configurations.CARD_CATEGORY_CO_BRANDED_SUPPLEMENTORY)
                        || cardType.equals(Configurations.CARD_CATEGORY_FD_SUPPLEMENTORY)) {
                    tableName = "TABLENAMESUPCARD";
                    fieldName = "TABLEFIELD";
                } else if (cardType.equals(Configurations.CARD_CATEGORY_ESTABLISHMENT)) {
                    tableName = "TABLENAMEESTCARD";
                    fieldName = "TABLEFIELDEST";
                } else if (cardType.equals(Configurations.CARD_CATEGORY_CORPORATE)) {
                    tableName = "TABLENAMECOPCARD";
                    fieldName = "TABLEFIELD";
                } else if (cardType.equals(Configurations.CARD_CATEGORY_FD)) {
                    tableName = "TABLENAMEFDCARD";
                    fieldName = "TABLEFIELD";
                }
            }

                String query = "SELECT * FROM LETTERFIELDDETAILS";

                String finalTableName = tableName;
                String finalFieldName = fieldName;
                String finalIdentificationFeild = identificationFeild;

                backendJdbcTemplate.query(query,
                        (ResultSet result) -> {
                            String[] referenceTableDetails;
                            while (result.next()) {
                                if (result.getString("FIELDNAME").equals("NAME")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = finalIdentificationFeild;
                                    bean.setNAME(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("ADDRESS1")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = finalIdentificationFeild;
                                    bean.setADDRESS1(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("ADDRESS2")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = finalIdentificationFeild;
                                    bean.setADDRESS2(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("ADDRESS3")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = finalIdentificationFeild;
                                    bean.setADDRESS3(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("CARDNO")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = "CARDNUMBER";
                                    bean.setCARDNO(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("CREDITLIMIT")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = "CARDNUMBER";
                                    bean.setCREDITLIMIT(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("TITLE")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = finalIdentificationFeild;
                                    bean.setTITLE(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("BODY")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = finalIdentificationFeild;
                                    bean.setBODY(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("TEXT")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = finalIdentificationFeild;
                                    bean.setTEXT(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("BRANCH")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = finalIdentificationFeild;
                                    bean.setBRANCH(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("AMOUNT")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = finalIdentificationFeild;
                                    bean.setAMOUNT(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("FULLPAYMENT")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = "CARDNO";
                                    bean.setFULLPAYMENT(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("MINPAYEMENT")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = "CARDNO";
                                    bean.setMINPAYEMENT(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("OLDCARDNO")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = "NEWCARDNUMBER";
                                    bean.setOLDCARDNO(referenceTableDetails);
                                } else if (result.getString("FIELDNAME").equals("CARDPRODUCT")) {
                                    referenceTableDetails = new String[3];
                                    referenceTableDetails[0] = result.getString(finalTableName);
                                    referenceTableDetails[1] = result.getString(finalFieldName);
                                    referenceTableDetails[2] = "PRODUCTCODE";
                                    bean.setCARDPRODUCT(referenceTableDetails);
                                }

                            }
                            return bean;
                        }
                );


        } catch (Exception e) {
            throw e;
        }

        return bean;
    }

    @Override
    public String getParameterValueForAppLetters(String[] tableIdentificationList, String appID) throws Exception {
        String value = null;
        String query = null;

        try {
            query = "SELECT " + tableIdentificationList[1] + " FROM " + tableIdentificationList[0] + " WHERE " + tableIdentificationList[2] + "=" + appID;

            value = backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        String temp = null;
                        while (rs.next()) {
                            temp = rs.getString(tableIdentificationList[1]);
                        }
                        return temp;
                    }
            );

        } catch (Exception e) {
            throw e;
        }

        return value;
    }

    @Override
    public String getParameterValueForCardLetters(String[] tableIdentificationList, StringBuffer cardNumber) throws Exception {
        String value = "--";
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

            value = backendJdbcTemplate.queryForObject(query, String.class);

        } catch (Exception e) {
            throw e;
        }

        return value;
    }

    @Override
    public String getTemplateBody(String[] inputParam) throws Exception {
        String body = null;

        try {

            String query = "SELECT BODY FROM LETTERTEMPLATE WHERE TEMPLATECODE=? AND CARDPRODUCT=?";

            body = backendJdbcTemplate.queryForObject(query,String.class, inputParam[0],inputParam[1]);

            //body = backendJdbcTemplate.queryForObject(query,String.class,inputParam[0],inputParam[1]);

        } catch (Exception e) {
            throw e;
        }

        return body;
    }

    @Override
    public String getCardTypebyApplicationID(String applicationID) throws SQLException {
        String cardCategory = null;

        try {
            String query = "SELECT CARDCATEGORY FROM CARDAPPLICATION WHERE APPLICATIONID = ? ";

            cardCategory = backendJdbcTemplate.queryForObject(query, String.class, applicationID);

        } catch (Exception e) {
            throw e;
        }

        return cardCategory;
    }

    @Override
    public String getCardTypebyCardNumber(StringBuffer cardNumber) throws SQLException {
        String cardCategory = null;

        try {

            String query = "SELECT CARDCATEGORYCODE FROM CARD WHERE CARDNUMBER = ? ";

            cardCategory = backendJdbcTemplate.queryForObject(query, String.class, cardNumber.toString());

        } catch (Exception e) {
            throw e;
        }

        return cardCategory;
    }
}
