/**
 * Author : lahiru_p
 * Date : 1/30/2023
 * Time : 9:49 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.MerchantCommissionCalculationDao;
import com.epic.cms.model.bean.CommissionProfileBean;
import com.epic.cms.model.bean.CommissionTxnBean;
import com.epic.cms.model.bean.MerchantLocationBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Repository
public class MerchantCommissionCalculationRepo implements MerchantCommissionCalculationDao {

    @Autowired
    StatusVarList statusList;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public List<MerchantLocationBean> getAllMerchants() throws Exception {
        List<MerchantLocationBean> merchantList = new ArrayList<>();
        try {
            String query = "SELECT DISTINCT ML.MERCHANTID, CP.CALMETHOD, CP.COMMISSIONPROFILECODE,"
                    + " ML.MERCHANTCUSTOMERNO"
                    + " FROM MERCHANTLOCATION ML"
                    + " LEFT JOIN COMMISSIONPROFILE CP"
                    + " ON CP.COMMISSIONPROFILECODE = ML.COMMITIONPROFILE"
                    + " INNER JOIN EODMERCHANTTRANSACTION EMT"
                    + " ON EMT.MID = ML.MERCHANTID"
                    + " WHERE ML.STATUS NOT IN (?,?)"
                    + " AND EMT.STATUS = ?";

            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                query += " AND ML.MERCHANTID NOT IN (SELECT EM.MID FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + statusList.getEOD_PENDING_STATUS() + "')";
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                query += "AND ML.MERCHANTID IN (SELECT EM.MID FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + statusList.getEOD_PENDING_STATUS() + "' AND EODID < " + Configurations.ERROR_EOD_ID + " AND EM.processstepid <=" + Configurations.PROCESS_STEP_ID + ")";
            }

            merchantList = backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        MerchantLocationBean commissionCalBean = new MerchantLocationBean();
                        commissionCalBean.setMerchantId(rs.getString("MERCHANTID"));
                        commissionCalBean.setCalMethod(rs.getString("CALMETHOD"));
                        commissionCalBean.setComisionProfile(rs.getString("COMMISSIONPROFILECODE"));
                        commissionCalBean.setMerchantCustomerNo(rs.getString("MERCHANTCUSTOMERNO"));
                        return commissionCalBean;
                    }),
                    statusList.getMERCHANT_DELETE_STATUS(),
                    statusList.getMERCHANT_CANCEL_STATUS(),
                    statusList.getEOD_PENDING_STATUS()
            );
        } catch (Exception e) {
            throw e;
        }

        return merchantList;
    }

    @Override
    public Boolean getCustomerCommStatus(String merchantCustomerNo) throws Exception {
        String commissionCalStatus = null;
        String query = "SELECT  COMMISIONCALSTATUS FROM MERCHANTCUSTOMER   WHERE MERCHANTCUSTOMERNO=?";
        try {
            commissionCalStatus = backendJdbcTemplate.queryForObject(query, String.class, merchantCustomerNo);

            return commissionCalStatus != null && commissionCalStatus.equalsIgnoreCase(Configurations.YES_STATUS);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String getCommissionProfile(String merchantCustomerNo) throws Exception {
        String commissionProfile = null;
        try {
            String query = "SELECT COMMISSIONPROFILE FROM MERCHANTCUSTOMER WHERE MERCHANTCUSTOMERNO=?";
            commissionProfile = backendJdbcTemplate.queryForObject(query, String.class, merchantCustomerNo);
        } catch (Exception e) {
            throw e;
        }
        return commissionProfile;
    }

    @Override
    public String getCalMethod(String profileCode) throws Exception {
        String calMethod = null;
        try {
            String query = "SELECT CALMETHOD FROM COMMISSIONPROFILE WHERE COMMISSIONPROFILECODE=?";
            calMethod = backendJdbcTemplate.queryForObject(query, String.class, profileCode);
        } catch (Exception e) {
            throw e;
        }
        return calMethod;
    }

    @Override
    public Queue<CommissionProfileBean> getAllCommCombination(String comisionProfile, String COMMISSION_TABLE, String COMMISSION_SEGMENT, String COMMISSION_DEFAULT_KEY) throws Exception {
        Queue<CommissionProfileBean> commProfileList = new LinkedList<CommissionProfileBean>();

        String query = "SELECT COMMISSIONPROFILECODE, FLATVALUE, PERCENTAGE,COMBINATION, CRDR, BINTYPE,CARDPRODUCT, " + COMMISSION_SEGMENT + ""
                + " FROM  " + COMMISSION_TABLE + ""
                + " WHERE COMMISSIONPROFILECODE=?"
                + " ORDER BY"
                + " CASE"
                + "   WHEN " + COMMISSION_SEGMENT + " != ?"
                + "    THEN 1"
                + "    WHEN (CARDPRODUCT!= ?"
                + "    AND " + COMMISSION_SEGMENT + " = ?)"
                + "    THEN 2"
                + "    WHEN (CARDPRODUCT= ?"
                + "    AND " + COMMISSION_SEGMENT + " = ? )"
                + "    THEN 3"
                + "    ELSE 4"
                + "  END";
        try {
            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            CommissionProfileBean commissionProfileBean = new CommissionProfileBean();
                            commissionProfileBean.setBinType(rs.getString("BINTYPE"));
                            commissionProfileBean.setCardProduct(rs.getString("CARDPRODUCT"));
                            commissionProfileBean.setCombination(rs.getString("COMBINATION"));
                            commissionProfileBean.setFlatValue(rs.getDouble("FLATVALUE"));
                            commissionProfileBean.setPercentage(rs.getDouble("PERCENTAGE"));
                            commissionProfileBean.setProfCode(rs.getString("COMMISSIONPROFILECODE"));
                            commissionProfileBean.setSegment(rs.getString(COMMISSION_SEGMENT));
                            commissionProfileBean.setCrdr(rs.getString("CRDR"));
                            commProfileList.add(commissionProfileBean);
                        }
                        return commProfileList;

                    }, comisionProfile, COMMISSION_DEFAULT_KEY, "PDEF", COMMISSION_DEFAULT_KEY, "PDEF", COMMISSION_DEFAULT_KEY
            );
        } catch (Exception e) {
            throw e;
        }
        return commProfileList;
    }

    @Override
    public ArrayList<CommissionTxnBean> getTransactionForCommission(String merchantId, String binType, String cardProduct, String segment, String calMethod, String segmentColumnName, String COMMISSION_DEFAULT_KEY) throws Exception {
        ArrayList<CommissionTxnBean> commissionTxnList = new ArrayList<>();
        String query;
        try {
            query = "SELECT BATCHNO,CRDR,CURRENCYTYPE,MID,TID,TRANSACTIONAMOUNT,TRANSACTIONDATE,"
                    + " TRANSACTIONID,TRANSACTIONTYPE,BIN,CARDPRODUCT,CARDASSOCIATION FROM EODMERCHANTTRANSACTION WHERE "
                    + " MID =?  "
                    + " AND STATUS = ?"
                    + " AND EODID = ?";

            if (!segment.equalsIgnoreCase(COMMISSION_DEFAULT_KEY)) {
                if (cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_VISA_ALL()) || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_MASTER_ALL())
                        || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_QR_ALL()) || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_CUP_ALL())
                        || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_IPG_VISA()) || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_IPG_MASTER())) {
                    query += " AND  CARDPRODUCT = ? AND " + segmentColumnName + "=?";

                    backendJdbcTemplate.query(query,
                            (ResultSet rs) -> {
                                while (rs.next()) {
                                    CommissionTxnBean commissionTxnBean = new CommissionTxnBean();
                                    commissionTxnBean.setBatchno(rs.getString("BATCHNO"));
                                    commissionTxnBean.setBintype(Integer.parseInt(binType));
                                    commissionTxnBean.setCalmethod(calMethod);
                                    commissionTxnBean.setCrdr(rs.getString("CRDR"));
                                    commissionTxnBean.setCurrencytype(rs.getString("CURRENCYTYPE"));
                                    commissionTxnBean.setMid(rs.getString("MID"));
                                    commissionTxnBean.setProductid(cardProduct);
                                    commissionTxnBean.setSegment(segment);
                                    commissionTxnBean.setTid(rs.getString("TID"));
                                    commissionTxnBean.setTransactionamount(rs.getString("TRANSACTIONAMOUNT"));
                                    commissionTxnBean.setTransactiondate(rs.getDate("TRANSACTIONDATE"));
                                    commissionTxnBean.setTransactionid(rs.getString("TRANSACTIONID"));
                                    commissionTxnBean.setTransactiontype(rs.getString("TRANSACTIONTYPE"));
                                    commissionTxnBean.setBin(rs.getString("BIN"));
                                    commissionTxnBean.setCardProduct(rs.getString("CARDPRODUCT"));
                                    commissionTxnBean.setCardassociation(rs.getString("CARDASSOCIATION"));

                                    commissionTxnList.add(commissionTxnBean);

                                    try {
                                        updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusList.getEOD_DONE_STATUS());
                                    } catch (Exception e) {
                                        throw new SQLException(e);
                                    }
                                }
                                return commissionTxnList;

                            }, merchantId, statusList.getEOD_PENDING_STATUS(), Configurations.EOD_ID, cardProduct, segment
                    );
                }
            } else if (segment.equalsIgnoreCase(COMMISSION_DEFAULT_KEY) && !(cardProduct.equalsIgnoreCase("PDEF"))) {
                if (cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_VISA_ALL()) || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_MASTER_ALL())
                        || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_QR_ALL()) || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_CUP_ALL())
                        || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_IPG_VISA()) || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_IPG_MASTER())) {
                    query += " AND CARDPRODUCT = ?";

                    backendJdbcTemplate.query(query,
                            (ResultSet rs) -> {
                                while (rs.next()) {
                                    CommissionTxnBean commissionTxnBean = new CommissionTxnBean();
                                    commissionTxnBean.setBatchno(rs.getString("BATCHNO"));
                                    commissionTxnBean.setBintype(Integer.parseInt(binType));
                                    commissionTxnBean.setCalmethod(calMethod);
                                    commissionTxnBean.setCrdr(rs.getString("CRDR"));
                                    commissionTxnBean.setCurrencytype(rs.getString("CURRENCYTYPE"));
                                    commissionTxnBean.setMid(rs.getString("MID"));
                                    commissionTxnBean.setProductid(cardProduct);
                                    commissionTxnBean.setSegment(segment);
                                    commissionTxnBean.setTid(rs.getString("TID"));
                                    commissionTxnBean.setTransactionamount(rs.getString("TRANSACTIONAMOUNT"));
                                    commissionTxnBean.setTransactiondate(rs.getDate("TRANSACTIONDATE"));
                                    commissionTxnBean.setTransactionid(rs.getString("TRANSACTIONID"));
                                    commissionTxnBean.setTransactiontype(rs.getString("TRANSACTIONTYPE"));
                                    commissionTxnBean.setBin(rs.getString("BIN"));
                                    commissionTxnBean.setCardProduct(rs.getString("CARDPRODUCT"));
                                    commissionTxnBean.setCardassociation(rs.getString("CARDASSOCIATION"));

                                    commissionTxnList.add(commissionTxnBean);

                                    try {
                                        updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusList.getEOD_DONE_STATUS());
                                    } catch (Exception e) {
                                        throw new SQLException(e);
                                    }
                                }
                                return commissionTxnList;

                            }, merchantId, statusList.getEOD_PENDING_STATUS(), Configurations.EOD_ID, cardProduct
                    );
                }

            } else if (segment.equalsIgnoreCase(COMMISSION_DEFAULT_KEY) && (cardProduct.equalsIgnoreCase("PDEF")) && !(binType.equalsIgnoreCase("BDEF"))) {

                backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                CommissionTxnBean commissionTxnBean = new CommissionTxnBean();
                                commissionTxnBean.setBatchno(rs.getString("BATCHNO"));
                                commissionTxnBean.setBintype(Integer.parseInt(binType));
                                commissionTxnBean.setCalmethod(calMethod);
                                commissionTxnBean.setCrdr(rs.getString("CRDR"));
                                commissionTxnBean.setCurrencytype(rs.getString("CURRENCYTYPE"));
                                commissionTxnBean.setMid(rs.getString("MID"));
                                commissionTxnBean.setProductid(cardProduct);
                                commissionTxnBean.setSegment(segment);
                                commissionTxnBean.setTid(rs.getString("TID"));
                                commissionTxnBean.setTransactionamount(rs.getString("TRANSACTIONAMOUNT"));
                                commissionTxnBean.setTransactiondate(rs.getDate("TRANSACTIONDATE"));
                                commissionTxnBean.setTransactionid(rs.getString("TRANSACTIONID"));
                                commissionTxnBean.setTransactiontype(rs.getString("TRANSACTIONTYPE"));
                                commissionTxnBean.setBin(rs.getString("BIN"));
                                commissionTxnBean.setCardProduct(rs.getString("CARDPRODUCT"));
                                commissionTxnBean.setCardassociation(rs.getString("CARDASSOCIATION"));

                                commissionTxnList.add(commissionTxnBean);

                                try {
                                    updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusList.getEOD_DONE_STATUS());
                                } catch (Exception e) {
                                    throw new SQLException(e);
                                }
                            }
                            return commissionTxnList;

                        }, merchantId, statusList.getEOD_PENDING_STATUS(), Configurations.EOD_ID
                );

            } else if (segment.equalsIgnoreCase(COMMISSION_DEFAULT_KEY) && (cardProduct.equalsIgnoreCase("PDEF")) && (binType.equalsIgnoreCase("BDEF"))) {

                backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                CommissionTxnBean commissionTxnBean = new CommissionTxnBean();
                                commissionTxnBean.setBatchno(rs.getString("BATCHNO"));
                                commissionTxnBean.setBintype(Integer.parseInt(binType));
                                commissionTxnBean.setCalmethod(calMethod);
                                commissionTxnBean.setCrdr(rs.getString("CRDR"));
                                commissionTxnBean.setCurrencytype(rs.getString("CURRENCYTYPE"));
                                commissionTxnBean.setMid(rs.getString("MID"));
                                commissionTxnBean.setProductid(cardProduct);
                                commissionTxnBean.setSegment(segment);
                                commissionTxnBean.setTid(rs.getString("TID"));
                                commissionTxnBean.setTransactionamount(rs.getString("TRANSACTIONAMOUNT"));
                                commissionTxnBean.setTransactiondate(rs.getDate("TRANSACTIONDATE"));
                                commissionTxnBean.setTransactionid(rs.getString("TRANSACTIONID"));
                                commissionTxnBean.setTransactiontype(rs.getString("TRANSACTIONTYPE"));
                                commissionTxnBean.setBin(rs.getString("BIN"));
                                commissionTxnBean.setCardProduct(rs.getString("CARDPRODUCT"));
                                commissionTxnBean.setCardassociation(rs.getString("CARDASSOCIATION"));

                                commissionTxnList.add(commissionTxnBean);

                                try {
                                    updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusList.getEOD_DONE_STATUS());
                                } catch (Exception e) {
                                    throw new SQLException(e);
                                }
                            }
                            return commissionTxnList;

                        }, merchantId, statusList.getEOD_PENDING_STATUS(), Configurations.EOD_ID
                );
            }


        } catch (Exception e) {
            throw e;
        }
        return commissionTxnList;
    }

    @Override
    public void getMerchantDetails(CommissionTxnBean commissionTxnBean) throws Exception {
        try {
            String query = "SELECT MC.MERCHANTCUSTOMERNO AS MERCHANTCUSTOMERNO,MC.ACCOUNTNUMBER AS MCACCOUNTNO,ML.ACCOUNTNUMBER AS MLACCOUNTNO "
                    + " FROM MERCHANTLOCATION ML INNER JOIN MERCHANTCUSTOMER MC"
                    + " ON ML.MERCHANTCUSTOMERNO = MC.MERCHANTCUSTOMERNO WHERE MERCHANTID=?";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            commissionTxnBean.setCustaccountno(rs.getString("MCACCOUNTNO"));
                            commissionTxnBean.setMeraccountno(rs.getString("MLACCOUNTNO"));
                            commissionTxnBean.setMerchantcustid(rs.getString("MERCHANTCUSTOMERNO"));
                        }
                        return commissionTxnBean;

                    }, commissionTxnBean.getMid()
            );
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int insertToEodMerchantComission(String merchantCusNo,
                                            String merhantCusAcountNo, String mId, String merchantAccNo, String tId,
                                            String txnAmount, Double merchantComission, String currency,
                                            String crDr, Date txnDate, String txnTypeCode, String batchNo,
                                            String txnId, String binStatus, String calMethod, String cardAssociation,
                                            String cardProduct, String segment, String originCardProduct, String calculatedMdrPercentage, String calculatedMdrFlatAmount) throws Exception {
        int count = 0;
        String query = null;
        DateFormat formatter;
        try {
            query = "INSERT INTO EODMERCHANTCOMMISSION (EODID,MERCHANTCUSTID,CUSTACCOUNTNO,MID,MERACCOUNTNO,"
                    + "TID,TRANSACTIONAMOUNT,MERCHANTCOMMSSION,MERCHANTDUEAMOUNT,CURRENCYTYPE,CRDR,"
                    + "TRANSACTIONDATE,TRANSACTIONTYPE,BATCHNO,TRANSACTIONID,LASTUPDATEDUSER,CREATEDTIME,"
                    + "LASTUPDATEDTIME,STATUS,BINTYPE,CALMETHOD,CARDASSOCIATION,PRODUCTID,SEGMENT,EODDATE,"
                    + "CARDPRODUCT,MDRPERCENTAGE,MDRFLATAMOUNT) VALUES (?,?,?,?,?,?,?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),?,?,?,?,SYSDATE,SYSDATE,?,?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),?,?,?)";

            formatter = new SimpleDateFormat("dd-MMM-yy");
            count = backendJdbcTemplate.update(query, Configurations.EOD_ID
                    , merchantCusNo
                    , merhantCusAcountNo
                    , mId
                    , merchantAccNo
                    , tId
                    , txnAmount
                    , merchantComission
                    , (Double.parseDouble(txnAmount) - merchantComission)
                    , currency
                    , crDr
                    , txnDate
                    , txnTypeCode
                    , batchNo
                    , txnId
                    , Configurations.EOD_USER
                    , statusList.getINITIAL_STATUS()
                    , binStatus
                    , calMethod
                    , cardAssociation
                    , cardProduct
                    , segment
                    , formatter.format(Configurations.EOD_DATE)
                    , originCardProduct
                    , calculatedMdrPercentage
                    , calculatedMdrFlatAmount
            );
        } catch (Exception e) {
            throw e;
        }
        return count;


    }

    @Override
    public int updateEodMerchantTxnEdon(String transactionid, String status) throws Exception {
        int count = 0;
        try {
            String transactionStatus = "UPDATE EODMERCHANTTRANSACTION SET STATUS=? WHERE TRANSACTIONID = ?";

            count = backendJdbcTemplate.update(transactionStatus, statusList.getCOMMISSION_COMPLETE_STATUS(), transactionid);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public Queue<CommissionProfileBean> getAllCommCombinationForVolume(String commissionProfile, String commissionVolumeTable, String commissionSegmentVolume, String commissionDefaultVolume) throws Exception {
        Queue<CommissionProfileBean> commProfileList = new LinkedList<CommissionProfileBean>();

        try {
            String query = "SELECT COMMISSIONPROFILECODE,"
                    + "  FLATVALUE,"
                    + "  PERCENTAGE,"
                    + "  COMBINATION,"
                    + "  CRDR,"
                    + "  BINTYPE,"
                    + "  CARDPRODUCT,"
                    + "  VOLUMEID"
                    + " FROM COMMISSIONVOLUME"
                    + " WHERE COMMISSIONPROFILECODE=?"
                    + " ORDER BY BINTYPE,"
                    + "  CASE"
                    + "    WHEN CARDPRODUCT != ?"
                    + "    THEN 1"
                    + "    ELSE 2"
                    + "  END,CARDPRODUCT,VOLUMEID";

            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            CommissionProfileBean commissionProfileBean = new CommissionProfileBean();
                            commissionProfileBean.setBinType(rs.getString("BINTYPE"));
                            commissionProfileBean.setCardProduct(rs.getString("CARDPRODUCT"));
                            commissionProfileBean.setCombination(rs.getString("COMBINATION"));
                            commissionProfileBean.setFlatValue(rs.getDouble("FLATVALUE"));
                            commissionProfileBean.setPercentage(rs.getDouble("PERCENTAGE"));
                            commissionProfileBean.setProfCode(rs.getString("COMMISSIONPROFILECODE"));
                            commissionProfileBean.setSegment(rs.getString(Configurations.COMMISSION_SEGMENT_VOLUME));
                            commissionProfileBean.setCrdr(rs.getString("CRDR"));
                            commProfileList.add(commissionProfileBean);
                        }
                        return commProfileList;

                    }, commissionProfile, "PDEF"
            );
        } catch (Exception e) {
            throw e;
        }
        return commProfileList;
    }

    @Override
    public String getVolumeId(double totalTxnAmount) throws Exception {
        String volumeId = null;
        try {
            String query = " SELECT VOLUMEID FROM TRANSACTIONVOLUME WHERE MINLIMIT<=? AND MAXLIMIT >?";
            volumeId = backendJdbcTemplate.queryForObject(query, String.class, totalTxnAmount, totalTxnAmount);
        } catch (Exception e) {
            throw e;
        }
        return volumeId;
    }

    @Override
    public CommissionProfileBean getCommissionProfile(String commissionProfile, String binType, String productCode, String volumeId) throws Exception {

        CommissionProfileBean commissionProfileBean1 = null;
        try {
            String query = " SELECT  FLATVALUE, PERCENTAGE,COMBINATION, CRDR,BINTYPE,CARDPRODUCT, VOLUMEID,COMMISSIONPROFILECODE "
                    + "FROM COMMISSIONVOLUME "
                    + "WHERE BINTYPE=? "
                    + "AND CARDPRODUCT=?"
                    + "AND COMMISSIONPROFILECODE=? "
                    + "AND VOLUMEID=?";

            commissionProfileBean1 = backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        CommissionProfileBean commissionProfileBean = null;
                        while (rs.next()) {
                            commissionProfileBean = new CommissionProfileBean();
                            commissionProfileBean.setBinType(rs.getString("BINTYPE"));
                            commissionProfileBean.setCardProduct(rs.getString("CARDPRODUCT"));
                            commissionProfileBean.setCombination(rs.getString("COMBINATION"));
                            commissionProfileBean.setFlatValue(rs.getDouble("FLATVALUE"));
                            commissionProfileBean.setPercentage(rs.getDouble("PERCENTAGE"));
                            commissionProfileBean.setProfCode(rs.getString("COMMISSIONPROFILECODE"));
                            commissionProfileBean.setSegment(rs.getString(Configurations.COMMISSION_SEGMENT_VOLUME));
                            commissionProfileBean.setCrdr(rs.getString("CRDR"));
                            commissionProfileBean.setVolumeId(volumeId);
                        }
                        return commissionProfileBean;

                    }, Integer.parseInt(binType), productCode, commissionProfile, volumeId
            );

            if (commissionProfileBean1 == null) {
                query = " SELECT  FLATVALUE, PERCENTAGE,COMBINATION, CRDR,BINTYPE,CARDPRODUCT, VOLUMEID,COMMISSIONPROFILECODE "
                        + "FROM COMMISSIONVOLUME "
                        + "WHERE BINTYPE=? "
                        + "AND CARDPRODUCT=?"
                        + "AND COMMISSIONPROFILECODE=? "
                        + "AND VOLUMEID=?";

                commissionProfileBean1 = backendJdbcTemplate.query(query,
                        (ResultSet rs1) -> {
                            CommissionProfileBean commissionProfileBean = null;
                            while (rs1.next()) {
                                commissionProfileBean.setBinType(rs1.getString("BINTYPE"));
                                commissionProfileBean.setCardProduct(rs1.getString("CARDPRODUCT"));
                                commissionProfileBean.setCombination(rs1.getString("COMBINATION"));
                                commissionProfileBean.setFlatValue(rs1.getDouble("FLATVALUE"));
                                commissionProfileBean.setPercentage(rs1.getDouble("PERCENTAGE"));
                                commissionProfileBean.setProfCode(rs1.getString("COMMISSIONPROFILECODE"));
                                commissionProfileBean.setSegment(rs1.getString(Configurations.COMMISSION_SEGMENT_VOLUME));
                                commissionProfileBean.setCrdr(rs1.getString("CRDR"));
                                commissionProfileBean.setVolumeId(Configurations.COMMISSION_DEFAULT_VOLUME);
                            }
                            return commissionProfileBean;

                        }, Integer.parseInt(binType), productCode, commissionProfile, Configurations.COMMISSION_DEFAULT_VOLUME
                );
            }

        } catch (Exception e) {
            throw e;
        }
        return commissionProfileBean1;
    }

    @Override
    public ArrayList<CommissionTxnBean> getTransactionForCommissionVolumeWise(String merchantId, String binType, String cardProduct, String calMethod, ArrayList<CommissionTxnBean> commissionTxnList) throws Exception {
        String query;
        try {
            query = "SELECT * FROM EODMERCHANTTRANSACTION WHERE "
                    + " MID =?  "
                    + " AND STATUS = ?";

            if (!(cardProduct.equalsIgnoreCase("PDEF"))) {
                if (cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_VISA_ALL()) || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_MASTER_ALL())
                        || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_QR_ALL()) || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_CUP_ALL())
                        || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_IPG_VISA()) || cardProduct.equalsIgnoreCase(statusList.getPRODUCT_CODE_IPG_MASTER())) {
                    query += " AND CARDPRODUCT = ?";

                    backendJdbcTemplate.query(query,
                            (ResultSet rs) -> {
                                while (rs.next()) {
                                    CommissionTxnBean commissionTxnBean = new CommissionTxnBean();
                                    commissionTxnBean.setBatchno(rs.getString("BATCHNO"));
                                    commissionTxnBean.setBintype(Integer.parseInt(binType));
                                    commissionTxnBean.setCardassociation(rs.getString("CARDASSOCIATION"));
                                    commissionTxnBean.setCrdr(rs.getString("CRDR"));
                                    commissionTxnBean.setCalmethod(calMethod);
                                    commissionTxnBean.setCurrencytype(rs.getString("CURRENCYTYPE"));
                                    commissionTxnBean.setMid(rs.getString("MID"));
                                    commissionTxnBean.setProductid(cardProduct);
                                    commissionTxnBean.setTid(rs.getString("TID"));
                                    commissionTxnBean.setTransactionamount(rs.getString("TRANSACTIONAMOUNT"));
                                    commissionTxnBean.setTransactiondate(rs.getDate("TRANSACTIONDATE"));
                                    commissionTxnBean.setTransactionid(rs.getString("TRANSACTIONID"));
                                    commissionTxnBean.setTransactiontype(rs.getString("TRANSACTIONTYPE"));

                                    commissionTxnList.add(commissionTxnBean);

                                    try {
                                        updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusList.getEOD_DONE_STATUS());
                                    } catch (Exception e) {
                                        throw new SQLException(e);
                                    }
                                }
                                return commissionTxnList;

                            }, merchantId, statusList.getEOD_PENDING_STATUS(), cardProduct
                    );
                }

            } else if ((cardProduct.equalsIgnoreCase("PDEF")) && !(binType.equalsIgnoreCase("BDEF"))) {

                backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                CommissionTxnBean commissionTxnBean = new CommissionTxnBean();
                                commissionTxnBean.setBatchno(rs.getString("BATCHNO"));
                                commissionTxnBean.setBintype(Integer.parseInt(binType));
                                commissionTxnBean.setCardassociation(rs.getString("CARDASSOCIATION"));
                                commissionTxnBean.setCrdr(rs.getString("CRDR"));
                                commissionTxnBean.setCalmethod(calMethod);
                                commissionTxnBean.setCurrencytype(rs.getString("CURRENCYTYPE"));
                                commissionTxnBean.setMid(rs.getString("MID"));
                                commissionTxnBean.setProductid(cardProduct);
                                commissionTxnBean.setTid(rs.getString("TID"));
                                commissionTxnBean.setTransactionamount(rs.getString("TRANSACTIONAMOUNT"));
                                commissionTxnBean.setTransactiondate(rs.getDate("TRANSACTIONDATE"));
                                commissionTxnBean.setTransactionid(rs.getString("TRANSACTIONID"));
                                commissionTxnBean.setTransactiontype(rs.getString("TRANSACTIONTYPE"));

                                commissionTxnList.add(commissionTxnBean);

                                try {
                                    updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusList.getEOD_DONE_STATUS());
                                } catch (Exception e) {
                                    throw new SQLException(e);
                                }
                            }
                            return commissionTxnList;

                        }, merchantId, statusList.getEOD_PENDING_STATUS()
                );

            } else if ((cardProduct.equalsIgnoreCase("PDEF")) && (binType.equalsIgnoreCase("BDEF"))) {
                backendJdbcTemplate.query(query,
                        (ResultSet rs) -> {
                            while (rs.next()) {
                                CommissionTxnBean commissionTxnBean = new CommissionTxnBean();
                                commissionTxnBean.setBatchno(rs.getString("BATCHNO"));
                                commissionTxnBean.setBintype(Integer.parseInt(binType));
                                commissionTxnBean.setCardassociation(rs.getString("CARDASSOCIATION"));
                                commissionTxnBean.setCrdr(rs.getString("CRDR"));
                                commissionTxnBean.setCalmethod(calMethod);
                                commissionTxnBean.setCurrencytype(rs.getString("CURRENCYTYPE"));
                                commissionTxnBean.setMid(rs.getString("MID"));
                                commissionTxnBean.setProductid(cardProduct);
                                commissionTxnBean.setTid(rs.getString("TID"));
                                commissionTxnBean.setTransactionamount(rs.getString("TRANSACTIONAMOUNT"));
                                commissionTxnBean.setTransactiondate(rs.getDate("TRANSACTIONDATE"));
                                commissionTxnBean.setTransactionid(rs.getString("TRANSACTIONID"));
                                commissionTxnBean.setTransactiontype(rs.getString("TRANSACTIONTYPE"));

                                commissionTxnList.add(commissionTxnBean);

                                try {
                                    updateEodMerchantTxnEdon(commissionTxnBean.getTransactionid(), statusList.getEOD_DONE_STATUS());
                                } catch (Exception e) {
                                    throw new SQLException(e);
                                }
                            }
                            return commissionTxnList;

                        }, merchantId, statusList.getEOD_PENDING_STATUS()
                );
            }
        } catch (Exception e) {
            throw e;
        }
        return commissionTxnList;
    }
}
