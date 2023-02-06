package com.epic.cms.dao;

import java.sql.SQLException;

public interface AtmCashAdvanceUpdateDao {
    int[] callStoredProcedureForCashAdvUpdate() throws SQLException;
}
