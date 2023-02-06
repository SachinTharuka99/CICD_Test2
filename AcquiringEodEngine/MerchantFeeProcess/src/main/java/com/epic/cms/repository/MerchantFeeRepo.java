/**
 * Author : rasintha_j
 * Date : 1/24/2023
 * Time : 12:53 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.MerchantFeeDao;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantCustomerBean;
import com.epic.cms.model.bean.MerchantFeeBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.MerchantCustomer;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;

@Repository
public class MerchantFeeRepo implements MerchantFeeDao {
    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public List<MerchantFeeBean> getMerchantFeeCountList() throws Exception {
        List<MerchantFeeBean> merchantFeeCountList = new ArrayList<MerchantFeeBean>();

        try {
            String query = "SELECT MF.MERCHANTID,MF.FEECODE,MF.FEECOUNT,FF.CURRENCYCODE,FF.CRORDR,FF.FLATFEE,FF.MINIMUMAMOUNT,FF.MAXIMUMAMOUNT,FF.COMBINATION, FF.PERSENTAGE, MC.ACCOUNTNUMBER, ML.MERCHANTACCOUNTNO ,ML.MERCHANTCUSTOMERNO FROM MERCHANTFEECOUNT MF LEFT JOIN MERCHANTLOCATION ML  ON ML.MERCHANTID=MF.MERCHANTID INNER JOIN FEEPROFILEFEE FF ON ML.FEEPROFILE=FF.FEEPROFILECODE INNER JOIN MERCHANTCUSTOMER MC ON ML.MERCHANTCUSTOMERNO=MC.MERCHANTCUSTOMERNO WHERE MF.FEECODE=FF.FEECODE AND MF.FEECOUNT<>0 AND MF.STATUS IN (?,?,?) AND";

            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                query += " ML.MERCHANTID NOT IN (SELECT EM.MID FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + statusList.getEOD_PENDING_STATUS() + "')";
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                query += " ML.MERCHANTID IN (SELECT EM.MID FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + statusList.getEOD_PENDING_STATUS() + "' AND EODID < " + Configurations.ERROR_EOD_ID + " AND EM.processstepid < (select er.STEPID from EODPROCESSFLOW er where er.PROCESSID = '" + Configurations.PROCESS_ID_COMMISSION_CALCULATION + "'))";
            }

            merchantFeeCountList = backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        MerchantFeeBean merchantFeeBean = new MerchantFeeBean();
                        merchantFeeBean.setMID(rs.getString("MERCHANTID"));
                        merchantFeeBean.setFeeCode(rs.getString("FEECODE"));
                        merchantFeeBean.setFeeCount(rs.getInt("FEECOUNT"));
                        merchantFeeBean.setCrORdr(rs.getString("CRORDR"));
                        merchantFeeBean.setFlatFee(rs.getDouble("FLATFEE"));
                        merchantFeeBean.setMinAmount(rs.getDouble("MINIMUMAMOUNT"));
                        merchantFeeBean.setMaxAmount(rs.getDouble("MAXIMUMAMOUNT"));
                        merchantFeeBean.setCombination(rs.getString("COMBINATION"));
                        merchantFeeBean.setPercentageAmount(rs.getDouble("PERSENTAGE"));
                        merchantFeeBean.setCustAccountNo(rs.getString("ACCOUNTNUMBER"));
                        merchantFeeBean.setMerchantAccountNo(rs.getString("MERCHANTACCOUNTNO"));
                        merchantFeeBean.setMerchantCustomerNo(rs.getString("MERCHANTCUSTOMERNO"));
                        return merchantFeeBean;
                    }),
                    statusList.getINITIAL_STATUS(),
                    statusList.getACTIVE_STATUS(),
                    statusList.getEOD_PENDING_STATUS()
            );
        } catch (Exception e) {
            errorLogger.error("Get Merchant Fee Count List Error", e);
            throw e;
        }
        return merchantFeeCountList;
    }

    @Override
    public void insertToEODMerchantFee(MerchantFeeBean merchantFeeBean, double amount, Date effectDate) throws Exception {
        try {
            String query = "INSERT INTO EODMERCHANTFEE(EODID,MERCHANTID,CRDR,EFFECTDATE,FEEAMOUNT,CUSTACCOUNTNO,MERACCOUNTNO,MERCHANTCUSTID,STATUS,FEETYPE,LASTUPDATEDUSER,LASTUPDATEDDATE)VALUES (?,?,?,?,?,?,?,?,?,?,?,SYSDATE)";

            backendJdbcTemplate.update(query,
                    Configurations.EOD_ID,
                    merchantFeeBean.getMID(),
                    merchantFeeBean.getCrORdr(),
                    effectDate,
                    amount,
                    merchantFeeBean.getCustAccountNo(),
                    merchantFeeBean.getMerchantCustomerNo(),
                    merchantFeeBean.getMerchantCustomerNo(),
                    Configurations.EOD_PENDING_STATUS,
                    merchantFeeBean.getFeeCode(),
                    Configurations.EOD_USER
            );
        } catch (Exception e) {
            errorLogger.error("Insert To EOD Merchant Fee Error", e);
            throw e;
        }
    }

    @Override
    public void updateMerchantFeecount(MerchantFeeBean merchantFeeBean) throws Exception {
        try {
            String query = "UPDATE MERCHANTFEECOUNT SET FEECOUNT = 0,  STATUS=? WHERE MERCHANTID = ? AND FEECODE = ?";

            backendJdbcTemplate.update(query,
                    Configurations.EOD_DONE_STATUS,
                    merchantFeeBean.getMID(),
                    merchantFeeBean.getFeeCode()
            );
        } catch (Exception e) {
            errorLogger.error("Update Merchant Feecount Error", e);
            throw e;
        }
    }

    @Override
    public int insertErrorEODMerchant(ErrorMerchantBean eBean) throws Exception {
        int count = 0;
        MerchantCustomerBean merchantCustomerBean = null;
        String merchantCustomer = null;
        String merchantLocation = null;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            if (eBean.getMerchantCustomer().equals(MerchantCustomer.MERCHANTCUSTOMER)) {
                //If merchant customer then merchant location will be null
                merchantCustomer = eBean.getMechantID();
            } else {
                merchantLocation = eBean.getMechantID();
                merchantCustomerBean = this.getMerchanCusDetails(merchantLocation);
                merchantCustomer = merchantCustomerBean.getMerchantCusNo();
            }
            String sql = "INSERT INTO EODERRORMERCHANT (CREATEDTIME,EODDATE,EODID,ERRORPROCESSID,ERRORPROCESSNAME,ERRORREMARK,"
                    + "LASTUPDATEDTIME,LASTUPDATEDUSER,MERCHANTCUSTOMERNO,MID,PROCESSSTEPID,STATUS) "
                    + " VALUES (SYSDATE,TO_DATE(?,'DD-MM-YY'),?,?,?,?,SYSDATE,?,?,?,?,?)";

            count = backendJdbcTemplate.update(sql,
                    sdf.format(Configurations.EOD_DATE),
                    Configurations.ERROR_EOD_ID,
                    eBean.getProcessId(),
                    eBean.getProcessName(),
                    eBean.getRemark(),
                    Configurations.EOD_USER,
                    merchantCustomer,
                    merchantLocation,
                    Configurations.PROCESS_STEP_ID,
                    statusList.getEOD_PENDING_STATUS()
            );

        } catch (Exception e) {
            errorLogger.error("Insert Error EOD Merchant Error", e);
            throw e;
        }
        return count;
    }

    public MerchantCustomerBean getMerchanCusDetails(String merchantId) throws Exception {
        MerchantCustomerBean merchantCusDetails = null;

        try {
            String query = "SELECT MERCHANTCUSTOMERNO,COMMISSIONPROFILE,FEEPROFILE,ACCOUNTNUMBER,MERCHANTACCOUNTNO,PAYMENTMAINTEINANCESTATUS,STATEMENTMAINTEINANCESTATUS FROM MERCHANTCUSTOMER WHERE MERCHANTCUSTOMERNO = (SELECT MERCHANTCUSTOMERNO FROM MERCHANTLOCATION WHERE MERCHANTID = ?)";

            merchantCusDetails = backendJdbcTemplate.queryForObject(query,
                    (result, rowNum) -> {
                        MerchantCustomerBean bean = new MerchantCustomerBean();
                        bean.setMerchantCusNo(result.getString("MERCHANTCUSTOMERNO"));
                        bean.setComisionProfile(result.getString("COMMISSIONPROFILE"));
                        bean.setFeeProfile(result.getString("FEEPROFILE"));
                        bean.setAccountNo(result.getString("ACCOUNTNUMBER"));
                        bean.setMerchantAccNo(result.getString("MERCHANTACCOUNTNO"));
                        bean.setPaymentmaintananceStatus(result.getString("PAYMENTMAINTEINANCESTATUS"));
                        bean.setStatementMaintananceStatus(result.getString("STATEMENTMAINTEINANCESTATUS"));
                        return bean;
                    },
                    merchantId);

        } catch (EmptyResultDataAccessException ex) {
            return merchantCusDetails;
        } catch (Exception ex) {
            errorLogger.error("Get MerchanCus Details Error", ex);
            throw ex;
        }
        return merchantCusDetails;
    }
}
