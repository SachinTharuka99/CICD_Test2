package com.epic.cms.repository;

import com.epic.cms.dao.PaymentReversalDao;
import com.epic.cms.model.bean.PaymentBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;

@Repository
public class PaymentReversalRepo implements PaymentReversalDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Autowired
    LogManager logManager;

    @Override
    public List<PaymentBean> getPaymentReversals() throws Exception {

        ArrayList<PaymentBean> chqBeanList = new ArrayList<PaymentBean>();
        try {
            chqBeanList = (ArrayList<PaymentBean>) backendJdbcTemplate.query(queryParametersList.getPaymentReversal_getPaymentReversals(),
                        new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        PaymentBean bean = new PaymentBean();
                        bean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                        bean.setEodid(result.getInt("EODID"));
                        bean.setAmount(result.getDouble("TRANSACTIONAMOUNT"));
                        bean.setPaymenttype(result.getString("TRANSACTIONTYPE"));
                        bean.setReference(result.getString("REFERENCE"));
                        bean.setSequencenumber(result.getString("SEQUENCENUMBER"));
                        bean.setChequenumber(result.getString("CHEQUENUMBER"));
                        bean.setCrdrmaintind(result.getString("CRDRMAINTIND"));
                        bean.setTraceid(result.getString("TRACEID"));

                        return bean;
                    })
                    , "CADP", Configurations.EOD_ID, status.getINITIAL_STATUS());
        } catch (Exception e) {
            logManager.logError("Exception in Get Payments Reversals ", errorLogger);
            throw e;
        }
        return chqBeanList;
    }

    @Override
    public int updatePaymentsForCashReversals(StringBuffer cardNumber, String traceNo) throws Exception {

        int count = 0;
        try {
            count = backendJdbcTemplate.update(queryParametersList.getPaymentReversal_insertPaymentsForCashReversals(),
                    status.getCASH_REVERSAL_STATUS(),
                    traceNo,
                    cardNumber.toString(),
                    Configurations.EOD_ID);
            if (count == 1) {
                count = backendJdbcTemplate.update(queryParametersList.getPaymentReversal_updatePaymentsForCashReversals(),
                        status.getCASH_REVERSAL_STATUS(),
                        traceNo,
                        cardNumber.toString(),
                        status.getCASH_REVERSAL_TYPE(),
                        Configurations.EOD_ID);
            }
        } catch (Exception e) {
            logManager.logError("Exception in Update Payments For Cash Reversals ", errorLogger);
            throw e;
        }
        return count;
    }
}
