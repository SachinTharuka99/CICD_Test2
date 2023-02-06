package com.epic.cms.dao;

import com.epic.cms.model.bean.LetterGenerationReferanceTableDetailsBean;

import java.sql.SQLException;
import java.util.ArrayList;

public interface LetterDao {

    ArrayList<String> getParametersInLetterTemplate(String tempCode, String cardProduct) throws Exception;

    LetterGenerationReferanceTableDetailsBean getLetterFieldDetails(String cardType, String tempCode) throws Exception;

    String getParameterValueForAppLetters(String[] tableIdentificationList, String appID) throws Exception;

    String getParameterValueForCardLetters(String[] tableIdentificationList, StringBuffer cardNumber) throws Exception;

    String getTemplateBody(String[] inputParam) throws Exception;

    String getCardTypebyApplicationID(String applicationID) throws SQLException;

    String getCardTypebyCardNumber(StringBuffer cardNumber) throws SQLException;
}
