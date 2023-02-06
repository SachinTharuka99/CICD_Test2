package com.epic.cms.dao;

import com.epic.cms.model.bean.AlertBean;
import com.epic.cms.model.bean.CashBackAlertBean;
import com.epic.cms.model.bean.LetterGenerationReferanceTableDetailsBean;

import java.util.ArrayList;

public interface AlertDao {

   double getParameterValueForMinPaymentDue(StringBuffer cardNumber, String accNo) throws Exception;

    boolean getActiveTemplate(String templateCode, String template) throws Exception;

   ArrayList<String> getParametersFromTemplate(String templateCode, String template) throws Exception;

   LetterGenerationReferanceTableDetailsBean getFieldDetails(String templateCode) throws Exception;

   String getParameterValueForCardLetters(String[] tableIdentificationList, StringBuffer cardNumber) throws Exception;

   StringBuffer getCardNoParameterValueForCardLetters(String[] tableIdentificationList, StringBuffer cardNumber) throws Exception;

   String getTemplateBody(String templateCode, String template) throws Exception;

   void insertAlertIntoOnline(AlertBean alertBean) throws Exception;

   AlertBean setValuesToBean(AlertBean alertBean, StringBuffer cardNumber) throws Exception;

   AlertBean setEmail_Number(AlertBean alertBean) throws Exception;

   void insertAlertIntoOnlineAdmin(AlertBean bean, String triggerPoint) throws Exception;
}
