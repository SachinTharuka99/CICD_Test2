package com.epic.cms.dao;

import java.util.ArrayList;

public interface CollectionAndRecoveryLetterDao {
    ArrayList<StringBuffer> getFirstReminderEligibleCards() throws Exception;

    ArrayList<StringBuffer> getSecondReminderEligibleCards() throws Exception;

    boolean getTriggerEligibleStatus(String triggerPoint, String smsOrEmail) throws Exception;

    int updateTriggerCards(StringBuffer cardNumber) throws Exception;

    String getAccountNoOnCard(StringBuffer cardNumber) throws Exception;

    int insertIntoDelinquentHistory(StringBuffer cardNumber, String accountNo, String remark) throws Exception;
}
