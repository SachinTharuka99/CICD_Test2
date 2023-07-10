/**
 * Author : lahiru_p
 * Date : 11/30/2022
 * Time : 8:36 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.GLSummaryFileDao;
import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;


import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Repository
public class GLSummaryFileRepo implements GLSummaryFileDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public ArrayList<GlAccountBean> getCashbackDataToEODGL() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<GlAccountBean>();
        try {
            //String sql = "SELECT CB.ID AS ID ,CB.ACCOUNTNUMBER AS ACCOUNTNUMBER,CA.CARDNUMBER AS CARDNUMBER, CB.CASHBACKAMOUNT AS AMOUNT,CB.EODDATE AS ADJUSTDATE  FROM CASHBACK CB ,CARDACCOUNT CA WHERE GLSTATUS=0 AND CA.ACCOUNTNO= CB.ACCOUNTNUMBER";

            list = (ArrayList<GlAccountBean>) backendJdbcTemplate.query(queryParametersList.getGLSummaryFile_getCashbackDataToEODGL(), new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                GlAccountBean bean = new GlAccountBean();
                bean.setId(rs.getInt("ID"));
                bean.setCardNo(new StringBuffer(rs.getString("CARDNUMBER")));
                bean.setAccNo(rs.getString("ACCOUNTNUMBER"));
                bean.setAmount(rs.getDouble("AMOUNT"));
                bean.setCrDr(Configurations.CREDIT);
                bean.setGlDate(rs.getString("ADJUSTDATE"));
                bean.setGlType(Configurations.TXN_TYPE_CASH_BACK);
                return bean;
            }));
        }catch (Exception e){
            throw e;
        }
        return list;
    }

    @Override
    public int insertIntoEodGLAccount(int eodID, Date glDate, StringBuffer cardNo, String glType, double amount, String cdStatus, String payType) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        int count = 0;
        try {
            //String sql = "INSERT INTO EODGLACCOUNT (EODID,GLDATE,CARDNO,GLTYPE,AMOUNT,CRDR,PAYMENTTYPE) VALUES (?,TO_DATE(?, 'DD-MM-YY'),?,?,to_char(?,'9999999999.99'),?,?)";
            count = backendJdbcTemplate.update(queryParametersList.getGLSummaryFile_insertIntoEodGLAccount(), eodID, sdf.format(glDate), cardNo.toString(), glType, String.valueOf(amount), cdStatus, payType);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateCashback(int key, int i) throws Exception {
        int count = 0;
        try {
            //String sql = "UPDATE CASHBACK SET GLSTATUS = ? WHERE ID = TO_NUMBER(?)";
            count = backendJdbcTemplate.update(queryParametersList.getGLSummaryFile_updateCashback(), i , key);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<GlAccountBean> getCashbackExpAndRedeemDataToEODGL() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<>();
        try {
            //String sql = "SELECT CB.ID AS ID , CB.ACCOUNTNUMBER AS ACCOUNTNUMBER, CA.CARDNUMBER AS CARDNUMBER, CB.AMOUNT AS AMOUNT, CB.EODDATE AS ADJUSTDATE, NVL(CB.STATUS,0) AS TXNTYPE FROM CASHBACKEXPREDEEM  CB ,CARDACCOUNT CA WHERE GLSTATUS  = 0 AND CA.ACCOUNTNO= CB.ACCOUNTNUMBER";

            list = (ArrayList<GlAccountBean>) backendJdbcTemplate.query(queryParametersList.getGLSummaryFile_getCashbackExpAndRedeemDataToEODGL(), new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                GlAccountBean bean = new GlAccountBean();
                int txnType = rs.getInt("TXNTYPE");
                if (txnType == 0) {
                    bean.setGlType(Configurations.TXN_TYPE_CASHBACK_REDEEMED);
                } else if (txnType == 1) {
                    bean.setGlType(Configurations.TXN_TYPE_CASHBACK_EXRIRED);
                } else {
                    bean.setGlType(Configurations.TXN_TYPE_CASHBACK_NP);
                }

                bean.setId(rs.getInt("ID"));
                bean.setCardNo(new StringBuffer(rs.getString("CARDNUMBER")));
                bean.setAccNo(rs.getString("ACCOUNTNUMBER"));
                bean.setAmount(rs.getDouble("AMOUNT"));
                bean.setCrDr(Configurations.DEBIT);
                bean.setGlDate(rs.getString("ADJUSTDATE"));
                return bean;
            }));
        }catch (Exception e){
            throw e;
        }
        return list;
    }

    @Override
    public int updateCashbackExpAndRedeem(int key, int i) throws Exception {
        int count = 0;
        try {
            //String sql = "UPDATE CASHBACKEXPREDEEM SET GLSTATUS = ? WHERE ID = ?";
            count = backendJdbcTemplate.update(queryParametersList.getGLSummaryFile_updateCashbackExpAndRedeem(), i , key);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateAdjusment(String key, int i) throws Exception {
        int count = 0;
        try {
            //String sql = "UPDATE ADJUSTMENT SET GLSTATUS = ? WHERE ID = TO_NUMBER(?)";
            count = backendJdbcTemplate.update(queryParametersList.getGLSummaryFile_updateAdjusment(), i , key);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateFeeTable(String key, int i) throws Exception {
        int count = 0;
        try {
            //String sql = "UPDATE eodcardfee SET GLSTATUS = ? WHERE EODFEEID = TO_NUMBER(?)";
            count = backendJdbcTemplate.update(queryParametersList.getGLSummaryFile_updateFeeTable(), i , key);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<GlAccountBean> getAdjustmentDataToEODGL() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<>();
        try {
            //String sql = "SELECT DISTINCT ID,UNIQUEID,AMOUNT,CRDR,ADJUSTDATE,TRANSACTIONTYPE FROM ADJUSTMENT A WHERE A.STATUS = ? AND A.EODSTATUS IN (?,?) AND A.ADJUSTMENTTYPE NOT IN(?,?,?) AND A.GLSTATUS = 0";

            list = (ArrayList<GlAccountBean>) backendJdbcTemplate.query(queryParametersList.getGLSummaryFile_getAdjustmentDataToEODGL(), new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                GlAccountBean bean = new GlAccountBean();
                bean.setKey(rs.getString("ID"));
                bean.setCardNo(new StringBuffer(rs.getString("UNIQUEID")));
                bean.setAccNo(rs.getString("UNIQUEID"));
                bean.setAmount(rs.getDouble("AMOUNT"));
                bean.setCrDr(rs.getString("CRDR"));
                bean.setGlDate(rs.getString("ADJUSTDATE"));
                bean.setGlType(rs.getString("TRANSACTIONTYPE"));
                return bean;
            })
                ,statusList.getMANUAL_ADJUSTMENT_ACCEPT() //MAAC
                ,Configurations.EOD_DONE_STATUS //EDON
                ,statusList.getBILLING_DONE_STATUS() //BCCP
                ,Integer.toString(Configurations.LOYALTY_ADJUSTMENT_TYPE) //8
                ,Integer.toString(Configurations.PAYMENT_ADJUSTMENT_TYPE) //1
                ,Integer.toString(Configurations.CASHBACK_ADJUSTMENT_TYPE)); //9
        }catch (Exception e){
            throw e;
        }
        return list;
    }

    @Override
    public ArrayList<GlAccountBean> getFeeDataToEODGL() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<>();
        try {
            //String sql = "SELECT DISTINCT ECF.EODFEEID,   ECF.CARDNUMBER,   ECF.ACCOUNTNO,   CA.STATUS,   ECF.CRDR,   ECF.FEEAMOUNT,   ECF.EFFECTDATE,   ECF.FEETYPE FROM EODCARDFEE ECF INNER JOIN CARDACCOUNT CA ON CA.ACCOUNTNO   = ECF.ACCOUNTNO WHERE ECF.STATUS IN (?,?) AND ECF.GLSTATUS  = ?";

            list = (ArrayList<GlAccountBean>) backendJdbcTemplate.query(queryParametersList.getGLSummaryFile_getFeeDataToEODGL(), new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        String accStatus, feeType = null;
                        GlAccountBean bean = new GlAccountBean();
                        bean.setKey(rs.getString("EODFEEID"));
                        bean.setCardNo(new StringBuffer(rs.getString("CARDNUMBER")));
                        bean.setAccNo(rs.getString("ACCOUNTNO"));
                        bean.setAmount(rs.getDouble("FEEAMOUNT"));
                        bean.setCrDr(rs.getString("CRDR"));
                        bean.setGlDate(rs.getString("EFFECTDATE"));
                        bean.setGlType(rs.getString("FEETYPE"));

                        accStatus = rs.getString("STATUS");
                        feeType = rs.getString("FEETYPE");

                        /**separte GL for latepayment & overlimit fees when account got NP. (NP CR 2019/09/16)*/
                        if (accStatus.equalsIgnoreCase(statusList.getACCOUNT_NON_PERFORMING_STATUS()) && feeType.equalsIgnoreCase(Configurations.LATE_PAYMENT_FEE)) {
                            bean.setGlType(Configurations.NP_ACCRUED_LATE_PAYMENT_FEE_GL);
                        } else if (accStatus.equalsIgnoreCase(statusList.getACCOUNT_NON_PERFORMING_STATUS()) && feeType.equalsIgnoreCase(Configurations.OVER_LIMIT_FEE)) {
                            bean.setGlType(Configurations.NP_ACCRUED_OVER_LIMIT_FEE_GL);
                        } else if (accStatus.equalsIgnoreCase(statusList.getACCOUNT_NON_PERFORMING_STATUS())) {
                            bean.setGlType(Configurations.NP_ACCRUED_OTHER_FEE_GL);
                        } else {
                            bean.setGlType(rs.getString("FEETYPE"));
                        }
                        return bean;
                    })
                    ,statusList.getMANUAL_ADJUSTMENT_ACCEPT() //MACC
                    ,statusList.getBILLING_DONE_STATUS()//BCCP
                    ,0);
        }catch (Exception e){
            throw e;
        }
        return list;
    }

    @Override
    public ArrayList<GlAccountBean> getEODTxnDataToGL() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<>();
        try {
            //String sql = "SELECT DISTINCT SETTLEMENTDATE, EODTRANSACTIONID, CARDNUMBER,  ACCOUNTNO,  TRANSACTIONAMOUNT, CRDR,A.TRANSACTIONTYPE AS DESCRIPTION,A.PAYMENTTYPE AS PAYMENTTYPE,  A.REQUESTFROM AS REQUESTFROM, A.ONOFFSTATUS, A.SECOND_PARTY_PAN,A.CARDASSOCIATION  FROM EODTRANSACTION A  WHERE STATUS IN (?,?) AND ADJUSTMENTSTATUS IN(?) AND GLSTATUS  =0";

            list = (ArrayList<GlAccountBean>) backendJdbcTemplate.query(queryParametersList.getGLSummaryFile_getEODTxnDataToGL(), new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        GlAccountBean bean = new GlAccountBean();
                        bean.setKey(rs.getString("EODTRANSACTIONID"));
                        bean.setCardNo(new StringBuffer(rs.getString("CARDNUMBER")));
                        bean.setAccNo(rs.getString("ACCOUNTNO"));
                        bean.setAmount(rs.getDouble("TRANSACTIONAMOUNT"));
                        bean.setCrDr(rs.getString("CRDR"));
                        bean.setGlDate(rs.getString("SETTLEMENTDATE"));
                        bean.setPaymentType(rs.getString("PAYMENTTYPE"));

                        String mPan = rs.getString("SECOND_PARTY_PAN");
                        String acqBin = "";
                        if (mPan != null && !mPan.equals("")) {
                            if (mPan.length() > 5) {
                                acqBin = mPan.substring(0, 6);
                            }
                        }
                        if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_SALE) && rs.getString("CARDASSOCIATION").equalsIgnoreCase(Configurations.VISA_ASSOCIATION)
                                && rs.getInt("REQUESTFROM") == 1) {
                            bean.setGlType(Configurations.TXN_TYPE_ISS_OFF_US);
                        } else if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_SALE) && rs.getString("CARDASSOCIATION").equalsIgnoreCase(Configurations.VISA_ASSOCIATION)
                                && rs.getInt("REQUESTFROM") == 2) {
                            bean.setGlType(Configurations.TXN_TYPE_ISS_ON_US);
                        } else if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE) && rs.getString("CARDASSOCIATION").equalsIgnoreCase(Configurations.VISA_ASSOCIATION)
                                && rs.getInt("REQUESTFROM") == 1) {
                            bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ISS_OFF_US);
                        } else if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE) && rs.getString("CARDASSOCIATION").equalsIgnoreCase(Configurations.VISA_ASSOCIATION)
                                && rs.getInt("REQUESTFROM") == 2) {
                            bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ISS_ON_US);
                        } else if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_SALE) && rs.getString("CARDASSOCIATION").equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)
                                && rs.getInt("REQUESTFROM") == 1) {
                            bean.setGlType(Configurations.TXN_TYPE_ISS_OFF_US_MASTER);
                        } else if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_SALE) && rs.getString("CARDASSOCIATION").equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)
                                && rs.getInt("REQUESTFROM") == 2) {
                            bean.setGlType(Configurations.TXN_TYPE_ISS_ON_US_MASTER);
                        } else if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE) && rs.getString("CARDASSOCIATION").equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)
                                && rs.getInt("REQUESTFROM") == 1) {
                            bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ISS_OFF_US_MASTER);
                        } else if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_CASH_ADVANCE) && rs.getString("CARDASSOCIATION").equalsIgnoreCase(Configurations.MASTER_ASSOCIATION)
                                && rs.getInt("REQUESTFROM") == 2) {
                            bean.setGlType(Configurations.TXN_TYPE_CASH_ADVANCE_ISS_ON_US_MASTER);
                        } else if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_ORIGINATOR)
                                && acqBin.equalsIgnoreCase(Configurations.MVISA_RECIPIENT_BIN)) {
                            bean.setGlType(Configurations.TXN_TYPE_MVISA_ORI_ON_US);
                        } else if (rs.getString("DESCRIPTION").equalsIgnoreCase(Configurations.TXN_TYPE_MVISA_ORIGINATOR)
                                && !acqBin.equalsIgnoreCase(Configurations.MVISA_RECIPIENT_BIN)) {
                            bean.setGlType(Configurations.TXN_TYPE_MVISA_ORI_OFF_US);
                        } else {
                            bean.setGlType(rs.getString("DESCRIPTION"));
                        }
                        return bean;
                    })
                    ,statusList.getEOD_DONE_STATUS()
                    ,statusList.getCHEQUE_RETURN_STATUS()//CQRT
                    ,Configurations.NO_STATUS);
        }catch (Exception e){
            throw e;
        }
        return list;
    }

    @Override
    public void updateEODTxn(String key, int i) throws Exception {
        try {
            //String sql = "UPDATE eodtransaction SET GLSTATUS = ? WHERE EODTRANSACTIONID = TO_NUMBER(?)";
            backendJdbcTemplate.update(queryParametersList.getGLSummaryFile_updateEODTxn(), i , key);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int updateEodGLAccount(int key) throws Exception {
        int count = 0;
        try {
            //String query = "UPDATE EODGLACCOUNT SET EODSTATUS = ? WHERE ID = ?";
            count = backendJdbcTemplate.update(queryParametersList.getGLSummaryFile_updateEodGLAccount(), statusList.getEOD_DONE_STATUS() , key);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public HashMap<String, ArrayList<GlAccountBean>> getDataFromEODGl() throws Exception {
        HashMap<String, ArrayList<GlAccountBean>> hmap = new HashMap<>();
        ArrayList<String> glTypes = new ArrayList<>();
        try {
            //String sql = "SELECT ID,EODID,GLDATE,CARDNO ,NVL(GLTYPE,'--') AS GLTYPE,AMOUNT,CRDR FROM EODGLACCOUNT  WHERE EODSTATUS = ? AND GLTYPE NOT IN (?,?) AND AMOUNT > 0";

            backendJdbcTemplate.query(queryParametersList.getGLSummaryFile_getDataFromEODGl(), (ResultSet rs) -> {
                while (rs.next()) {
                    ArrayList<GlAccountBean> list;
                    GlAccountBean bean = new GlAccountBean();
                    String glType = rs.getString("GLTYPE");
                    bean.setId(rs.getInt("ID"));
                    bean.setAmount(rs.getDouble("AMOUNT"));
                    bean.setGlAmount(rs.getString("AMOUNT"));
                    bean.setKey(rs.getString("EODID"));
                    bean.setCrDr(rs.getString("CRDR"));
                    bean.setGlType(rs.getString("GLTYPE"));

                    if (glTypes.contains(glType)) {
                        list = hmap.get(glType);
                        list.add(bean);
                        hmap.put(glType, list);

                    } else {
                        glTypes.add(glType);
                        ArrayList<GlAccountBean> set = new ArrayList<>();
                        set.add(bean);
                        hmap.put(glType, set);
                    }
                }
                return hmap;
            },
                    statusList.getEOD_PENDING_STATUS(),
                    Configurations.TXN_TYPE_PAYMENT, //27
                    Configurations.TXN_TYPE_DEBIT_PAYMENT);//28
        }catch (Exception e){
            throw e;
        }
        return hmap;
    }

    @Override
    public HashMap<String, String[]> getGLTypesData() throws Exception {
        HashMap<String, String[]> hmap = new HashMap<>();
        try {
            //String sql = "SELECT TRANSACTIONCODE,CREDITACCOUNT,DEBITACCOUNT,CRDR FROM GLTRANSACTION WHERE CATEGORY = ?";

            backendJdbcTemplate.query(queryParametersList.getGLSummaryFile_getGLTypesData(), (ResultSet rs) -> {
                while (rs.next()) {
                    String[] accountNo = new String[3];
                    String glType = rs.getString("TRANSACTIONCODE");
                    accountNo[0] = rs.getString("CREDITACCOUNT");
                    accountNo[1] = rs.getString("DEBITACCOUNT");
                    accountNo[2] = rs.getString("CRDR");

                    hmap.put(glType, accountNo);
                }
                return hmap;
            }, statusList.getNO_STATUS_0());

        }catch (Exception e){
            throw e;
        }
        return hmap;
    }
}
