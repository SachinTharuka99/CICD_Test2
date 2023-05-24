package com.epic.cms.dao;

public interface ConfigurationsDao {

    void loadTxnTypeConfigurations() throws Exception;

    void loadFilePath() throws Exception;

    void setConfigurations() throws Exception;

    void loadBaseCurrency() throws Exception;
}
