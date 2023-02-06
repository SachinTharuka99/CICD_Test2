package com.epic.cms.dao;

public interface EodParameterResetProcessDao {
    int resetTerminalParameters() throws Exception;

    int resetMerchantParameters()throws Exception;
}
