package com.epic.cms.dao;

import java.sql.SQLException;

public interface EodPaymentUpdateDao {
    int[] callStoredProcedureForEodPaymentUpdate() throws SQLException;
}
