package com.epic.cms.dao;

import java.util.ArrayList;

public interface ClearMinAmountAndTempBlockDao {
    ArrayList<StringBuffer[]> getAllCards(StringBuffer cardNo) throws Exception;

    void removeFromMinPayTable(StringBuffer cardNo, double payment)  throws Exception;

    int updateCardBlock(StringBuffer cardNO, String oldStatus, String newStatus) throws Exception;

    ArrayList<Object> getMinimumPaymentExistStatementDate(StringBuffer cardNo, int monthNo) throws Exception;
}
