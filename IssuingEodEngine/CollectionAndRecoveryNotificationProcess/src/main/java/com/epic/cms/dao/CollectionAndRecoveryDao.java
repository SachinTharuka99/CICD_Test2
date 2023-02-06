package com.epic.cms.dao;

import com.epic.cms.model.bean.CollectionAndRecoveryBean;

import java.util.ArrayList;

public interface CollectionAndRecoveryDao {
    int getNoOfDaysOnTriggerPoint(String triggerPoint) throws Exception;

    ArrayList<CollectionAndRecoveryBean> getCardListForCollectionAndRecoveryOnDueDate(int X_DATES, int number, String lastTrigger) throws Exception;

    ArrayList<CollectionAndRecoveryBean> getCardListForCollectionAndRecoveryOnStatmentDate(int X_DATES, int number, String lastTrigger)  throws Exception;

    boolean CheckForTriggerPoint(StringBuffer cardNo)  throws Exception;

    void addCardToTriggerCards(CollectionAndRecoveryBean collectionAndRecoveryBean) throws Exception;

    void updateTriggerCards(CollectionAndRecoveryBean collectionAndRecoveryBean) throws Exception;

    int addDetailsToCardLetterNotifyTable(StringBuffer cardNo, String nameInFull, String accNo, String contactNo, String email, String address, double dueAmount, String dueDate, String remark);
}
