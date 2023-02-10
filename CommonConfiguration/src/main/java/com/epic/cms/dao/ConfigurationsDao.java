package com.epic.cms.dao;

public interface ConfigurationsDao {

    void loadTxnTypeConfigurations() throws Exception;

    void loadFilePath() throws Exception;

    void setConfigurations();

    void loadBaseCurrency() throws Exception;
}
