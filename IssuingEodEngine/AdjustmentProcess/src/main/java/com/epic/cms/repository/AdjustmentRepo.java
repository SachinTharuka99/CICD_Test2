package com.epic.cms.repository;

import com.epic.cms.dao.AdjustmentDao;
import com.epic.cms.model.bean.AdjustmentBean;
import com.epic.cms.model.bean.PaymentBean;
import com.epic.cms.model.rowmapper.AdjutmentRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AdjustmentRepo implements AdjustmentDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Override
    public List<AdjustmentBean> getAdjustmentList() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        List<AdjustmentBean> adjustmentList = new ArrayList<>();
        try {
            System.out.println(status.getMANUAL_ADJUSTMENT_ACCEPT() + "|" + Configurations.TXN_TYPE_ADJUSTMENT_CREDIT + "|" + Configurations.TXN_TYPE_ADJUSTMENT_DEBIT + "|" + Configurations.EOD_DONE_STATUS + "|" + Configurations.LOYALTY_ADJUSTMENT_TYPE + "|" + Configurations.CASHBACK_ADJUSTMENT_TYPE + "|" + Configurations.EOD_DONE_STATUS + "|" + status.getBILLING_DONE_STATUS() + "|" + sdf.format(Configurations.EOD_DATE));
            adjustmentList = backendJdbcTemplate.query(queryParametersList.getAdjustment_getAdjustmentList(), new AdjutmentRowMapper(), status.getMANUAL_ADJUSTMENT_ACCEPT(), "TTC027", "TTC028", Configurations.EOD_DONE_STATUS, Configurations.LOYALTY_ADJUSTMENT_TYPE, Configurations.CASHBACK_ADJUSTMENT_TYPE, Configurations.EOD_DONE_STATUS, status.getBILLING_DONE_STATUS(), sdf.format(Configurations.EOD_DATE));
        } catch (Exception e) {
            throw e;
        }
        return adjustmentList;
    }

    @Override
    public void insertToEODPayments(PaymentBean paymentBean) throws Exception {
        //int result = 0;
        try {
            int result = backendJdbcTemplate.update(queryParametersList.getAdjustment_insertToEODPayments(), paymentBean.getSequencenumber(), paymentBean.getEodid(), paymentBean.getCardnumber().toString(), paymentBean.getMaincardno().toString(), paymentBean.getIsprimary(), paymentBean.getAmount(), paymentBean.getPaymenttype(), paymentBean.getTraceid(), paymentBean.getAmount(), status.getINITIAL_STATUS(), Configurations.EOD_USER);
            if (result == 1) {
                System.out.println("successfully inserted");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String getCardAssociationFromCardBin(String cardBin) throws Exception {
        String cardAssociation = null;
        try {
            cardAssociation = backendJdbcTemplate.queryForObject(queryParametersList.getAdjustment_getCardAssociationFromCardBin(), String.class, new Object[]{cardBin});
        } catch (Exception e) {
            throw e;
        }
        return cardAssociation;
    }

    @Override
    public void insertInToEODTransaction(AdjustmentBean adjustmentBean, String cardAssociation) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        try {
            if (adjustmentBean.getAdjustType() == null) {
                backendJdbcTemplate.update(queryParametersList.getAdjustment_insertInToEODTransaction_AdTypeNull(), Configurations.EOD_ID, adjustmentBean.getCardNumber().toString(), adjustmentBean.getAccNo(), adjustmentBean.getAdjustAmount(), adjustmentBean.getCurruncyType(), sdf.format(Configurations.EOD_DATE), sdf.format(Configurations.EOD_DATE), adjustmentBean.getTxnType(), adjustmentBean.getTxnId(), Configurations.EOD_USER, status.getINITIAL_STATUS(), adjustmentBean.getAdjustDes(), adjustmentBean.getCrDr(), cardAssociation);
            } else {
                backendJdbcTemplate.update(queryParametersList.getAdjustment_insertInToEODTransaction_AdTypeNotNull(), Configurations.EOD_ID, adjustmentBean.getCardNumber().toString(), adjustmentBean.getAccNo(), adjustmentBean.getAdjustAmount(), adjustmentBean.getCurruncyType(), sdf.format(Configurations.EOD_DATE), sdf.format(Configurations.EOD_DATE), adjustmentBean.getTxnType(), adjustmentBean.getTxnId(), Configurations.EOD_USER, status.getINITIAL_STATUS(), adjustmentBean.getAdjustDes(), adjustmentBean.getCrDr(), cardAssociation, adjustmentBean.getAdjustTxnType(), 1);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int updateAdjustmentStatus(String id) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE ADJUSTMENT SET EODSTATUS = ? WHERE ID = ?";
            count = backendJdbcTemplate.update(query, Configurations.EOD_DONE_STATUS, id);
        } catch (Exception e) {
            throw e;
        }
        return count;

    }

    @Override
    public int updateTransactionToEDON(String txnId) throws Exception {
        int count = 0;
        try {
            count = backendJdbcTemplate.update(queryParametersList.getAdjustment_updateTransactionToEDON(), Configurations.EOD_DONE_STATUS, txnId);
        } catch (Exception e) {
            throw e;
        }
        return count;

    }
}
