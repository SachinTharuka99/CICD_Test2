package com.epic.cms.dao;

import java.util.HashMap;
import java.util.List;

public interface AcqTxnUpdateDao {
    String getForexPercentage() throws Exception;

    String getFuelSurchargeRatePercentage() throws Exception;

    List<String> getFuelMccList() throws Exception;

    HashMap<String, String> getFinancialStatus() throws Exception;
}
