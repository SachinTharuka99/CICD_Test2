package com.epic.cms.dao;

import com.epic.cms.model.bean.TransactionBean;

import java.sql.SQLException;
import java.util.ArrayList;

public interface OnlineToBackendTxnDao {

    int[] callStoredProcedureForTxnSync() throws SQLException;
}
