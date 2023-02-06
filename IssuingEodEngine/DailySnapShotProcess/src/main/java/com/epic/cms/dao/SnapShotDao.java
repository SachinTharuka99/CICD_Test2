package com.epic.cms.dao;

public interface SnapShotDao {
    int checkEodComplete() throws Exception;

    void updateSnapShotTableOfCards() throws Exception;

    void updateSnapShotTableOfAccounts() throws Exception;

    void updateOnlineSnapShotTableOfCards() throws Exception;

    void updateOnlineSnapShotTableOfAccounts() throws Exception;
}
