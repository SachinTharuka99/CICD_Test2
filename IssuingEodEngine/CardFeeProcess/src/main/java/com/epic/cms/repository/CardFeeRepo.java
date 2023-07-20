package com.epic.cms.repository;

import com.epic.cms.dao.CardFeeDao;
import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.model.rowmapper.CardFeeRowMapper;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CardFeeRepo implements CardFeeDao {
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Override
    public List<CardFeeBean> getCardFeeCountList() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        List<CardFeeBean> recordList = new ArrayList<>();
        try {
            StringBuffer sb = new StringBuffer(queryParametersList.getCardFee_getCardFeeCountList());
            CommonMethods commonMethods = new CommonMethods();
            sb.append(commonMethods.checkForErrorCards("CF.CARDNUMBER"));
            recordList = backendJdbcTemplate.query(sb.toString(), new CardFeeRowMapper(), status.getCARD_CLOSED_STATUS(), status.getACTIVE_STATUS(), status.getEOD_PENDING_STATUS(), sdf.format(Configurations.EOD_DATE));
        } catch (Exception e) {
            throw e;
        }
        return recordList;
    }

    @Override
    public CardFeeBean getCardFeeCountForCard(StringBuffer cardNo, String accountNo, String feeCode) throws Exception {
        CardFeeBean cardFeeBean = null;
        try {
            cardFeeBean = backendJdbcTemplate.queryForObject(queryParametersList.getCardFee_getCardFeeCountForCard(), new CardFeeRowMapper(), cardNo.toString(), accountNo, feeCode, status.getCARD_CLOSED_STATUS());
        }catch (EmptyResultDataAccessException ex){
            return null;
        } catch (Exception e) {
            throw e;
        }
        return cardFeeBean;
    }

    @Override
    public Date getNextBillingDateForCard(StringBuffer cardNo) throws Exception {
        Date nextBillingDate = null;
        try {
            nextBillingDate = backendJdbcTemplate.queryForObject(queryParametersList.getCardFee_getNextBillingDateForCard(), Date.class, new Object[]{cardNo});
        } catch (Exception e) {
            throw e;
        }
        return nextBillingDate;
    }

    @Override
    public void insertToEODCardFee(CardFeeBean cardFeeBean, double amount, Date effectDate) throws Exception {
        try {
            backendJdbcTemplate.update(queryParametersList.getCardFee_insertToEODCardFee(), Configurations.EOD_ID, cardFeeBean.getCardNumber().toString(), cardFeeBean.getAccNumber(), cardFeeBean.getCrOrDr(), amount, cardFeeBean.getCurrCode(), effectDate, cardFeeBean.getFeeCode(), Configurations.EOD_USER, status.getEOD_PENDING_STATUS(), cardFeeBean.getTxnId());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateCardFeeCount(CardFeeBean cardFeeBean) throws Exception {
        try {
            backendJdbcTemplate.update(queryParametersList.getCardFee_updateCardFeeCount(), Configurations.EOD_DONE_STATUS, cardFeeBean.getCardNumber().toString(), cardFeeBean.getFeeCode());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int updateDELINQUENTACCOUNTNpDetails(double accruedInterest, double accruedOverLimitFees, double accruedLatePayFees, double otherFees, String accNo) throws Exception {
        int count = 0;
        double totalFees = 0;
        try {
            totalFees = otherFees + accruedLatePayFees + accruedOverLimitFees;
            count = backendJdbcTemplate.update(queryParametersList.getCardFee_updateDELINQUENTACCOUNTNpDetails(), accruedInterest, accruedOverLimitFees, accruedLatePayFees, totalFees, accNo);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
