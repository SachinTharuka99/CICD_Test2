package com.epic.cms.dao;

import com.epic.cms.model.bean.EODCardTransactionDetail;
import com.epic.cms.model.bean.EodInterestBean;

import java.util.Date;

public interface ChequeProcessDao {
    EODCardTransactionDetail getEODTotalTransactionDetailsForCard(StringBuffer cardNo, int statementStartEODID, int eodId) throws Exception;

    Date calculateDueDate(String accountNo) throws Exception;

    EodInterestBean getEodInterestForCard(StringBuffer cardNo) throws Exception;

}
