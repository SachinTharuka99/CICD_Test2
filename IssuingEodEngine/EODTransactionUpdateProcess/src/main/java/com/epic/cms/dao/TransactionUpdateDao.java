package com.epic.cms.dao;

import java.sql.SQLException;

public interface TransactionUpdateDao {
    int[] callStoredProcedureForVisaTxnUpdate() throws SQLException;

    int[] callStoredProcedureForMasterTxnUpdate() throws SQLException;
}
