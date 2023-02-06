package com.epic.cms.dao;

import java.util.HashMap;

public interface CollectionAndRecoveryAlertDao {
    HashMap<StringBuffer, String> getConfirmedCardToAlert() throws Exception;

    void updateAlertGenStatus(StringBuffer cardNumber, String trigger) throws Exception;
}
