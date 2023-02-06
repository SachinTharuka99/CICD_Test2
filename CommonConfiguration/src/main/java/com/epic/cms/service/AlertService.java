package com.epic.cms.service;

import com.epic.cms.model.bean.AlertBean;
import com.epic.cms.model.bean.CashBackAlertBean;
import com.epic.cms.model.bean.LetterGenerationReferanceTableDetailsBean;
import com.epic.cms.repository.AlertRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.LastStatementSummaryRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
@Service
public class AlertService {

    @Autowired
    AlertRepo alertRepo;

    @Autowired
    CommonRepo commonRepo;
    
    public synchronized void alertGeneration(String templateCode, String appID, String triggerPoint, StringBuffer cardNumber, String template, String accNum) throws Exception{
        String RefID = "";
        String remark = "";
        String accountNo;
        String trigger = "";

        try {
            AlertBean alertBean = new AlertBean();
            if (alertRepo.getActiveTemplate(templateCode, template)) {
                LetterGenerationReferanceTableDetailsBean tableBean = new LetterGenerationReferanceTableDetailsBean();
                HashMap<String, String> parameterValues = new HashMap<String, String>();
                ArrayList<String> parameterList = alertRepo.getParametersFromTemplate(templateCode, template);
                tableBean = alertRepo.getFieldDetails(templateCode);

                for (int i = 0; i < parameterList.size(); i++) {

                    if (parameterList.get(i).equals("NAME")) {
                        parameterValues.put("NAME", alertRepo.getParameterValueForCardLetters(tableBean.getNAME(), cardNumber));
                    } else if (parameterList.get(i).equals("ADDRESS1")) {
                        parameterValues.put("ADDRESS1", alertRepo.getParameterValueForCardLetters(tableBean.getADDRESS1(), cardNumber));
                    } else if (parameterList.get(i).equals("ADDRESS2")) {
                        parameterValues.put("ADDRESS2", alertRepo.getParameterValueForCardLetters(tableBean.getADDRESS2(), cardNumber));
                    } else if (parameterList.get(i).equals("ADDRESS3")) {
                        parameterValues.put("ADDRESS3", alertRepo.getParameterValueForCardLetters(tableBean.getADDRESS3(), cardNumber));
                    } else if (parameterList.get(i).equals("CARDNO")) {
                        parameterValues.put("CARDNO", CommonMethods.cardNumberMask(alertRepo.getCardNoParameterValueForCardLetters(tableBean.getCARDNO(), cardNumber)));
                    } else if (parameterList.get(i).equals("CREDITLIMIT")) {
                        parameterValues.put("CREDITLIMIT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tableBean.getCREDITLIMIT(), cardNumber)));
                    } else if (parameterList.get(i).equals("TITLE")) {
                        parameterValues.put("TITLE", alertRepo.getParameterValueForCardLetters(tableBean.getTITLE(), cardNumber));
                    } else if (parameterList.get(i).equals("BODY")) {
                        parameterValues.put("BODY", alertRepo.getParameterValueForCardLetters(tableBean.getBODY(), cardNumber));
                    } else if (parameterList.get(i).equals("TEXT")) {
                        parameterValues.put("TEXT", alertRepo.getParameterValueForCardLetters(tableBean.getTEXT(), cardNumber));
                    } else if (parameterList.get(i).equals("BRANCH")) {
                        parameterValues.put("BRANCH", alertRepo.getParameterValueForCardLetters(tableBean.getBRANCH(), cardNumber));
                    } else if (parameterList.get(i).equals("AMOUNT")) {
                        parameterValues.put("AMOUNT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tableBean.getAMOUNT(), cardNumber)));
                    } else if (parameterList.get(i).equals("TODAY")) {
                        Date date = new Date();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        parameterValues.put("TODAY", dateFormat.format(date));
                    } else if (parameterList.get(i).equals("LETTERREF")) {
                        parameterValues.put("LETTERREF", RefID);
                    } else if (parameterList.get(i).equals("FULLPAYMENT")) {
                        parameterValues.put("FULLPAYMENT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tableBean.getFULLPAYMENT(), cardNumber)));
                    } else if (parameterList.get(i).equals("MINPAYEMENT")) {
                        parameterValues.put("MINPAYEMENT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tableBean.getMINPAYEMENT(), cardNumber)));
                    } else if (parameterList.get(i).equals("OLDCARDNO")) {
                        parameterValues.put("OLDCARDNO", CommonMethods.cardNumberMask(alertRepo.getCardNoParameterValueForCardLetters(tableBean.getOLDCARDNO(), cardNumber)));
                    } else if (parameterList.get(i).equals("CARDPRODUCT")) {
                        parameterValues.put("CARDPRODUCT", alertRepo.getParameterValueForCardLetters(tableBean.getCARDPRODUCT(), cardNumber));
                    } else if (parameterList.get(i).equals("STMTOUSTANDING")) {
                        parameterValues.put("STMTOUSTANDING", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tableBean.getSTMTOUSTANDING(), cardNumber)));
                    } else if (parameterList.get(i).equals("DUEDATE")) {
                        String temp[] = alertRepo.getParameterValueForCardLetters(tableBean.getDUEDATE(), cardNumber).split(" ", 2);
                        parameterValues.put("DUEDATE", temp[0].replace('-', '/'));
                    } else if (parameterList.get(i).equals("STMTENDDATE")) {
                        String temp[] = alertRepo.getParameterValueForCardLetters(tableBean.getSTATEMENTENDDATE(), cardNumber).split(" ", 2);
                        parameterValues.put("STMTENDDATE", temp[0].replace('-', '/'));
                    } else if (parameterList.get(i).equals("CBACCNO")) {
                        parameterValues.put("CBACCNO", alertRepo.getParameterValueForCardLetters(tableBean.getCBACCNO(), new StringBuffer(accNum)));
                    } else if (parameterList.get(i).equals("CBAMOUNT")) {
                        parameterValues.put("CBAMOUNT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tableBean.getCBAMOUNT(), new StringBuffer(accNum))));
                    } else if (parameterList.get(i).equals("CBSTATENDDATE")) {
                        String temp[] = alertRepo.getParameterValueForCardLetters(tableBean.getCBSTATENDDATE(), new StringBuffer(accNum)).split(" ", 2);
                        parameterValues.put("CBSTATENDDATE", temp[0].replace('-', '/'));
                    } else if (parameterList.get(i).equals("CARDAVAILABLE")) {
                        parameterValues.put("CARDAVAILABLE", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tableBean.getCARDAVAILABLE(), cardNumber)));
                    } else if (parameterList.get(i).equals("CARDOUSTANDING")) {
                        parameterValues.put("CARDOUSTANDING", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tableBean.getCARDOUSTANDING(), cardNumber)));
                    } else if (parameterList.get(i).equals("ACCAVAILABLE")) {
                        parameterValues.put("ACCAVAILABLE", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tableBean.getACCAVAILABLE(), new StringBuffer(accNum))));
                    } else if (parameterList.get(i).equals("CURRENTNDIA")) {
                        parameterValues.put("CURRENTNDIA", alertRepo.getParameterValueForCardLetters(tableBean.getCURRENTNDIA(), new StringBuffer(accNum)));
                    } else if (parameterList.get(i).equals("CARDEXPDATE")) {
                        parameterValues.put("CARDEXPDATE", alertRepo.getParameterValueForCardLetters(tableBean.getCARDEXPDATE(), cardNumber));
                    } else if (parameterList.get(i).equals("MINPAYEMENTDUE")) {
                        parameterValues.put("MINPAYEMENTDUE", CommonMethods.getFormattedCurrency(Double.toString(alertRepo.getParameterValueForMinPaymentDue(cardNumber, accNum))));
                    } else if (parameterList.get(i).equals("EODDATE")) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        parameterValues.put("EODDATE", dateFormat.format(Configurations.EOD_DATE));
                    }
                }

                String body = null;
                String bodyHtml = null;

                if (template.equals(Configurations.EMAIL_TEMPLATE)) {
                    body = alertRepo.getTemplateBody(templateCode, template);
                } else if (template.equals(Configurations.SMS_TEMPLATE)) {
                    bodyHtml = alertRepo.getTemplateBody(templateCode, template);
                    body = Jsoup.parse(bodyHtml).text();
                }

                for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value == null) {
                        value = " ";
                    }
                    body = body.replace("|" + key + "|", value);
                }

                alertBean = alertRepo.setValuesToBean(alertBean, cardNumber);

                alertBean.setMessage(body);

                if (triggerPoint.equalsIgnoreCase(Configurations.X_DAYS_BEFORE_1_DUE_DATE)) {
                    trigger = "BEFORE 1ST DUE DATE";
                } else if (triggerPoint.equalsIgnoreCase(Configurations.IMMEDIATE_AFTER_1_DUE_DATE)) {
                    trigger = "AFTER 1ST DUE DATE";
                } else if (triggerPoint.equalsIgnoreCase(Configurations.IMMEDIATE_AFTER_2_DUE_DATE)) {
                    trigger = "AFTER 2ND DUE DATE";
                } else if (triggerPoint.equalsIgnoreCase(Configurations.IMMEDIATE_AFTER_3_DUE_DATE)) {
                    trigger = "AFTER 3RD DUE DATE";
                }
                if (template.equals(Configurations.EMAIL_TEMPLATE)) {
                    alertBean.setAlertType(Configurations.EMAIL_TEMPLATE_CODE);

                    remark = "EMAIL HAS BEEN SENT " + trigger;
                } else if (template.equals(Configurations.SMS_TEMPLATE)) {
                    alertBean.setAlertType(Configurations.SMS_TEMPLATE_CODE);
                    remark = "SMS HAS BEEN SENT " + trigger;
                }

                alertBean.setCardNumber(cardNumber);
                alertBean.setMaskedCardNumber(CommonMethods.cardNumberMask(cardNumber));

                alertRepo.insertAlertIntoOnline(alertBean);

                //TODO insert to delinquent history
                accountNo = commonRepo.getAccountNoOnCard(cardNumber);
                commonRepo.insertIntoDelinquentHistory(cardNumber, accountNo, remark);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public synchronized void  alertGenerationCashBack(String templateCode, CashBackAlertBean cbBean) throws Exception{
        String RefID = "";
        try {
            AlertBean alertBean = new AlertBean();

            if (alertRepo.getActiveTemplate(templateCode, Configurations.SMS_TEMPLATE)) {

                LetterGenerationReferanceTableDetailsBean tbleBean = new LetterGenerationReferanceTableDetailsBean();
                HashMap<String, String> parameterValues = new HashMap<String, String>();

                ArrayList<String> parameterList = alertRepo.getParametersFromTemplate(templateCode, Configurations.SMS_TEMPLATE);
                tbleBean = alertRepo.getFieldDetails(templateCode);

                for (int i = 0; i < parameterList.size(); i++) {

                    if (parameterList.get(i).equals("NAME")) {
                        parameterValues.put("NAME", alertRepo.getParameterValueForCardLetters(tbleBean.getNAME(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("ADDRESS1")) {
                        parameterValues.put("ADDRESS1", alertRepo.getParameterValueForCardLetters(tbleBean.getADDRESS1(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("ADDRESS2")) {
                        parameterValues.put("ADDRESS2", alertRepo.getParameterValueForCardLetters(tbleBean.getADDRESS2(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("ADDRESS3")) {
                        parameterValues.put("ADDRESS3", alertRepo.getParameterValueForCardLetters(tbleBean.getADDRESS3(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("CARDNO")) {
                        parameterValues.put("CARDNO", CommonMethods.cardNumberMask(alertRepo.getCardNoParameterValueForCardLetters(tbleBean.getCARDNO(), cbBean.getMainCardNo())));
                    } else if (parameterList.get(i).equals("CREDITLIMIT")) {
                        parameterValues.put("CREDITLIMIT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tbleBean.getCREDITLIMIT(), cbBean.getMainCardNo())));
                    } else if (parameterList.get(i).equals("TITLE")) {
                        parameterValues.put("TITLE", alertRepo.getParameterValueForCardLetters(tbleBean.getTITLE(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("BODY")) {
                        parameterValues.put("BODY", alertRepo.getParameterValueForCardLetters(tbleBean.getBODY(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("TEXT")) {
                        parameterValues.put("TEXT", alertRepo.getParameterValueForCardLetters(tbleBean.getTEXT(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("BRANCH")) {
                        parameterValues.put("BRANCH", alertRepo.getParameterValueForCardLetters(tbleBean.getBRANCH(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("AMOUNT")) {
                        parameterValues.put("AMOUNT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tbleBean.getAMOUNT(), cbBean.getMainCardNo())));
                    } else if (parameterList.get(i).equals("TODAY")) {
                        Date date = new Date();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        parameterValues.put("TODAY", dateFormat.format(date));
                    } else if (parameterList.get(i).equals("LETTERREF")) {
                        parameterValues.put("LETTERREF", RefID);
                    } else if (parameterList.get(i).equals("FULLPAYMENT")) {
                        parameterValues.put("FULLPAYMENT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tbleBean.getFULLPAYMENT(), cbBean.getMainCardNo())));
                    } else if (parameterList.get(i).equals("MINPAYEMENT")) {
                        parameterValues.put("MINPAYEMENT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tbleBean.getMINPAYEMENT(), cbBean.getMainCardNo())));
                    } else if (parameterList.get(i).equals("OLDCARDNO")) {
                        parameterValues.put("OLDCARDNO", CommonMethods.cardNumberMask(alertRepo.getCardNoParameterValueForCardLetters(tbleBean.getOLDCARDNO(), cbBean.getMainCardNo())));
                    } else if (parameterList.get(i).equals("CARDPRODUCT")) {
                        parameterValues.put("CARDPRODUCT", alertRepo.getParameterValueForCardLetters(tbleBean.getCARDPRODUCT(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("STMTOUSTANDING")) {
                        parameterValues.put("STMTOUSTANDING", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tbleBean.getSTMTOUSTANDING(), cbBean.getMainCardNo())));
                    } else if (parameterList.get(i).equals("DUEDATE")) {
                        String temp[] = alertRepo.getParameterValueForCardLetters(tbleBean.getDUEDATE(), cbBean.getMainCardNo()).split(" ", 2);
                        parameterValues.put("DUEDATE", temp[0].replace('-', '/'));
                    } else if (parameterList.get(i).equals("STMTENDDATE")) {
                        String temp[] = alertRepo.getParameterValueForCardLetters(tbleBean.getSTATEMENTENDDATE(), cbBean.getMainCardNo()).split(" ", 2);
                        parameterValues.put("STMTENDDATE", temp[0].replace('-', '/'));
                    } else if (parameterList.get(i).equals("CBACCNO")) {
                        parameterValues.put("CBACCNO", alertRepo.getParameterValueForCardLetters(tbleBean.getCBACCNO(), new StringBuffer(cbBean.getAccNo())));
                    } else if (parameterList.get(i).equals("CBAMOUNT")) {
                        parameterValues.put("CBAMOUNT", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tbleBean.getCBAMOUNT(), new StringBuffer(cbBean.getAccNo()))));
                    } else if (parameterList.get(i).equals("CBSTATENDDATE")) {
                        String temp[] = alertRepo.getParameterValueForCardLetters(tbleBean.getCBSTATENDDATE(), new StringBuffer(cbBean.getMainCardNo())).split(" ", 2);
                        parameterValues.put("CBSTATENDDATE", temp[0].replace('-', '/'));
                    } else if (parameterList.get(i).equals("CARDAVAILABLE")) {
                        parameterValues.put("CARDAVAILABLE", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tbleBean.getCARDAVAILABLE(), cbBean.getMainCardNo())));
                    } else if (parameterList.get(i).equals("CARDOUSTANDING")) {
                        parameterValues.put("CARDOUSTANDING", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tbleBean.getCARDOUSTANDING(), cbBean.getMainCardNo())));
                    } else if (parameterList.get(i).equals("ACCAVAILABLE")) {
                        parameterValues.put("ACCAVAILABLE", CommonMethods.getFormattedCurrency(alertRepo.getParameterValueForCardLetters(tbleBean.getACCAVAILABLE(), new StringBuffer(cbBean.getAccNo()))));
                    } else if (parameterList.get(i).equals("CURRENTNDIA")) {
                        parameterValues.put("CURRENTNDIA", alertRepo.getParameterValueForCardLetters(tbleBean.getCURRENTNDIA(), new StringBuffer(cbBean.getAccNo())));
                    } else if (parameterList.get(i).equals("CARDEXPDATE")) {
                        parameterValues.put("CARDEXPDATE", alertRepo.getParameterValueForCardLetters(tbleBean.getCARDEXPDATE(), cbBean.getMainCardNo()));
                    } else if (parameterList.get(i).equals("EODDATE")) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        parameterValues.put("EODDATE", dateFormat.format(Configurations.EOD_DATE));
                    }
                }

                String body = null;
                String bodyHtml = null;

                bodyHtml = alertRepo.getTemplateBody(templateCode, Configurations.SMS_TEMPLATE);
                body = Jsoup.parse(bodyHtml).text();

                for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value == null) {
                        value = " ";
                    }
                    body = body.replace("|" + key + "|", value);
                }

                alertBean = alertRepo.setValuesToBean(alertBean, cbBean.getMainCardNo());

                alertBean.setMessage(body);
                alertBean.setAlertType(Configurations.SMS_TEMPLATE_CODE);
                alertBean.setCardNumber(cbBean.getMainCardNo());
                alertBean.setMaskedCardNumber(CommonMethods.cardNumberMask(cbBean.getMainCardNo()));

                alertRepo.insertAlertIntoOnline(alertBean);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public synchronized void alertGenerationAdmin(String templateCode, int noOfCount, String triggerPoint) throws Exception {
        String RefID = "";
        String trigger = "";
        String remark = "";
        String body = null;

        try {
            AlertBean alertBean = new AlertBean();

            if (alertRepo.getActiveTemplate(templateCode, Configurations.EMAIL_TEMPLATE)) {

                LetterGenerationReferanceTableDetailsBean tbleBean = new LetterGenerationReferanceTableDetailsBean();
                HashMap<String, String> parameterValues = new HashMap<String, String>();

                ArrayList<String> parameterList = alertRepo.getParametersFromTemplate(templateCode, Configurations.EMAIL_TEMPLATE);
                tbleBean = alertRepo.getFieldDetails(templateCode);

                for (int i = 0; i < parameterList.size(); i++) {

                    if (parameterList.get(i).equals("NOOFCARDS")) {
                        parameterValues.put("NOOFCARDS", String.valueOf(noOfCount));
                    } else if (parameterList.get(i).equals("TODAY")) {
                        Date date = new Date();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        parameterValues.put("TODAY", dateFormat.format(date));
                    } else if (parameterList.get(i).equals("LETTERREF")) {
                        parameterValues.put("LETTERREF", RefID);
                    } else if (parameterList.get(i).equals("EODDATE")) {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        parameterValues.put("EODDATE", dateFormat.format(Configurations.EOD_DATE));
                    }
                }

                body = alertRepo.getTemplateBody(templateCode, Configurations.EMAIL_TEMPLATE);

                for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value == null) {
                        value = " ";
                    }
                    body = body.replace("|" + key + "|", value);
                }

                alertBean = alertRepo.setEmail_Number(alertBean);

                alertBean.setMessage(body);
                alertBean.setAlertType(Configurations.EMAIL_TEMPLATE_CODE);

                alertRepo.insertAlertIntoOnlineAdmin(alertBean, triggerPoint);
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    public synchronized void alertGenerationAdminBeforeDueDate(String templateCode, int noOfCount, String triggerPoint) throws Exception {
        String RefID = "";
        String trigger = "";
        String remark = "";
        String body = null;

        try {
            AlertBean alertBean = new AlertBean();

            if (alertRepo.getActiveTemplate(templateCode, Configurations.EMAIL_TEMPLATE)) {

                LetterGenerationReferanceTableDetailsBean tbleBean = new LetterGenerationReferanceTableDetailsBean();
                HashMap<String, String> parameterValues = new HashMap<String, String>();

                ArrayList<String> parameterList = alertRepo.getParametersFromTemplate(templateCode, Configurations.EMAIL_TEMPLATE);
                tbleBean = alertRepo.getFieldDetails(templateCode);

                for (int i = 0; i < parameterList.size(); i++) {

                    if (parameterList.get(i).equals("NOOFCARDS")) {
                        parameterValues.put("NOOFCARDS", String.valueOf(noOfCount));
                    } else if (parameterList.get(i).equals("TODAY")) {
                        Date date = new Date();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        parameterValues.put("TODAY", dateFormat.format(date));
                    } else if (parameterList.get(i).equals("LETTERREF")) {
                        parameterValues.put("LETTERREF", RefID);
                    } else if (parameterList.get(i).equals("EODDATE")) {
                        //duedate should be eoddate+1 (trigger got before due date)

                        DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                        Calendar c = Calendar.getInstance();
                        c.setTime(Configurations.EOD_DATE);
                        c.add(Calendar.DATE, 1);  // number of days to add
                        parameterValues.put("EODDATE", sdf.format(c.getTime()));
                    }
                }

                body = alertRepo.getTemplateBody(templateCode, Configurations.EMAIL_TEMPLATE);

                for (Map.Entry<String, String> entry : parameterValues.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value == null) {
                        value = " ";
                    }
                    body = body.replace("|" + key + "|", value);
                }

                alertBean = alertRepo.setEmail_Number(alertBean);

                alertBean.setMessage(body);
                alertBean.setAlertType(Configurations.EMAIL_TEMPLATE_CODE);

                alertRepo.insertAlertIntoOnlineAdmin(alertBean, triggerPoint);
            }

        } catch (Exception ex) {
            throw ex;
        }
    }
}
