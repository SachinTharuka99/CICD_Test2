/**
 * Author : sharuka_j
 * Date : 1/26/2023
 * Time : 12:52 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.PreMerchantFeeDao;
import com.epic.cms.model.bean.MerchantBeanForFee;
import com.epic.cms.model.bean.TerminalBeanForFee;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.DateUtil;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Repository
public class PreMerchantFeeRepo implements PreMerchantFeeDao {

    @Autowired
    StatusVarList status;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public HashMap<String, List<String>> getFeeCodeListForFeeProfile() throws Exception {
        String query = "SELECT FEEPROFILECODE,FEECODE FROM FEEPROFILEFEE ORDER BY FEEPROFILECODE";

//        String previousFeeProfileCode = "";

        HashMap<String, List<String>> feeProfileFeeCodeMap = new HashMap<>();

        try {
            backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        int count = 0;
                        String previousFeeProfileCode = "";
                        List<String> feeCodeList = null;
                        feeCodeList = new ArrayList<>();
                        while (rs.next()) {
                            count++;
                            String currentFeeProfileCode = rs.getString("FEEPROFILECODE");
                            if (!currentFeeProfileCode.equals(previousFeeProfileCode)) {
                                if (count == 1) { //very first record

                                    feeCodeList.add(rs.getString("FEECODE"));
                                    previousFeeProfileCode = currentFeeProfileCode;

                                    if (!previousFeeProfileCode.isEmpty()) { // adding the last element. but not if sql result is empty
                                        feeProfileFeeCodeMap.put(previousFeeProfileCode, feeCodeList);
                                    }
                                } else {
                                    //new fee profile
                                    feeProfileFeeCodeMap.put(previousFeeProfileCode, feeCodeList);
                                    feeCodeList = new ArrayList<>();
                                    feeCodeList.add(rs.getString("FEECODE"));
                                    previousFeeProfileCode = currentFeeProfileCode;

                                    if (!previousFeeProfileCode.isEmpty()) { // adding the last element. but not if sql result is empty
                                        feeProfileFeeCodeMap.put(previousFeeProfileCode, feeCodeList);
                                    }
                                }
                            } else { //same fee profile.different fee code
                                feeCodeList.add(rs.getString("FEECODE"));

                                if (!previousFeeProfileCode.isEmpty()) { // adding the last element. but not if sql result is empty
                                    feeProfileFeeCodeMap.put(previousFeeProfileCode, feeCodeList);
                                }
                            }
                        }
                        return feeProfileFeeCodeMap;
                    }
            );

            return feeProfileFeeCodeMap;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public ArrayList<MerchantBeanForFee> getMerchantListForFeeProcess() throws Exception {
        ArrayList<MerchantBeanForFee> merchantBeanList = new ArrayList<>();

        String query = "SELECT ML.MERCHANTID,ML.FEEPROFILE,ML.NEXTANNIVERSARYDATE AS M_NEXTANNIVERSARYDATE,T.TERMINALID,T.TERMINALTYPE,T.TERMINALSTATUS, T.NEXTANNIVERSARYDATE AS T_NEXTANNIVERSARYDATE,T.NEXTRENTALDATE AS T_NEXTRENTALDATE,ML.MERCHANTCUSTOMERNO,ML.NEXTBIMONTHLYDATE AS M_NEXTBIMONTHLYDATE,ML.NEXTQUARTERLYDATE AS M_NEXTQUARTERLYDATE,ML.NEXTHALFYEARLYDATE AS M_NEXTHALFYEARLYDATE,T.NEXTBIMONTHLYDATE AS T_NEXTBIMONTHLYDATE,T.NEXTQUARTERLYDATE AS T_NEXTQUARTERLYDATE,T.NEXTHALFYEARLYDATE AS T_NEXTHALFYEARLYDATE,T.NEXTWEEKLYDATE AS T_NEXTWEEKLYDATE FROM   MERCHANTLOCATION ML LEFT JOIN TERMINAL T ON T.MERCHANTID=ML.MERCHANTID WHERE ML.STATUS NOT IN(?,?)  ";

        if (Configurations.STARTING_EOD_STATUS.equals(status.getINITIAL_STATUS())) {
            query += " AND ML.MERCHANTID NOT IN (SELECT EM.MID FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + status.getEOD_PENDING_STATUS() + "') ORDER BY ML.MERCHANTID";
        } else if (Configurations.STARTING_EOD_STATUS.equals(status.getERROR_STATUS())) {
            query += "AND ML.MERCHANTID IN (SELECT EM.MID FROM EODERRORMERCHANT EM WHERE EM.STATUS='" + status.getEOD_PENDING_STATUS() + "' AND EODID < " + Configurations.ERROR_EOD_ID + " AND EM.processstepid <=" + Configurations.PROCESS_STEP_ID + ") ORDER BY ML.MERCHANTID";
        }
        try {
//            String previousMerchantId = "";
//            MerchantBeanForFee merchantBean = null;
            MerchantBeanForFee merchantBean = new MerchantBeanForFee();
//            List<TerminalBeanForFee> terminalList = null;


            backendJdbcTemplate.query(query
                    , (ResultSet rs) -> {
                        String previousMerchantId = "";
                        int count = 0;
                        List<TerminalBeanForFee> terminalList = null;
                        while (rs.next()) {
                            count++;
                            String merchantId = rs.getString("MERCHANTID");
                            if (!previousMerchantId.equals(merchantId)) { //if two different merchant id
//                                MerchantBeanForFee merchantBean = new MerchantBeanForFee();
                                // a new merchant with different terminal list
                                if (count != 1) { //this will not happen in very first merchant
                                    merchantBean.setTerminalList(terminalList); //add terminal list to existing merchant bean
                                    merchantBeanList.add(merchantBean); //add current merchant bean to merchant bean list
                                }
//                                merchantBean = new MerchantBeanForFee(); //create a new merchant bean
                                terminalList = new ArrayList<>(); // create a new terminal list

                                merchantBean.setMerchantId(rs.getString("MERCHANTID"));
                                merchantBean.setFeeProfile(rs.getString("FEEPROFILE"));
                                merchantBean.setNextAnniversaryDate(rs.getDate("M_NEXTANNIVERSARYDATE"));
                                merchantBean.setMerchantCustomerNo(rs.getString("MERCHANTCUSTOMERNO"));
                                merchantBean.setNextBiMonthlyDate(rs.getDate("M_NEXTBIMONTHLYDATE"));
                                merchantBean.setNextQuarterlyDate(rs.getDate("M_NEXTQUARTERLYDATE"));
                                merchantBean.setNextHalfYearlyDate(rs.getDate("M_NEXTHALFYEARLYDATE"));

                                if (rs.getString("TERMINALID") != null) {
                                    TerminalBeanForFee terminalBean = new TerminalBeanForFee();
                                    terminalBean.setTerminalId(rs.getString("TERMINALID"));
                                    terminalBean.setNextAnniversaryDate(rs.getDate("T_NEXTANNIVERSARYDATE"));
                                    terminalBean.setNextRentalDate(rs.getDate("T_NEXTRENTALDATE"));
                                    terminalBean.setTerminalType(rs.getInt("TERMINALTYPE"));
                                    terminalBean.setTerminalStatus(rs.getString("TERMINALSTATUS"));
                                    terminalBean.setNextBiMonthlyDate(rs.getDate("T_NEXTBIMONTHLYDATE"));
                                    terminalBean.setNextQuarterlyDate(rs.getDate("T_NEXTQUARTERLYDATE"));
                                    terminalBean.setNextHalfYearlyDate(rs.getDate("T_NEXTHALFYEARLYDATE"));
                                    terminalBean.setNextWeeklyDate(rs.getDate("T_NEXTWEEKLYDATE"));

//                                    terminalList.add(terminalBean); // add first terminal bean for terminal list in merchant bean
                                    if (merchantBean != null) {
                                        //add last merchant
                                        if (terminalList != null) {
                                            merchantBean.setTerminalList(terminalList); //add terminal list to existing merchant bean
                                        }
                                        merchantBeanList.add(merchantBean);
                                    }
                                }
                                previousMerchantId = merchantId;
                            } else {
                                //same merchant. different terminal
                                TerminalBeanForFee terminalBean = new TerminalBeanForFee();
                                terminalBean.setTerminalId(rs.getString("TERMINALID"));
                                terminalBean.setNextAnniversaryDate(rs.getDate("T_NEXTANNIVERSARYDATE"));
                                terminalBean.setNextRentalDate(rs.getDate("T_NEXTRENTALDATE"));
                                terminalBean.setTerminalType(rs.getInt("TERMINALTYPE"));
                                terminalBean.setTerminalStatus(rs.getString("TERMINALSTATUS"));
                                terminalBean.setNextBiMonthlyDate(rs.getDate("T_NEXTBIMONTHLYDATE"));
                                terminalBean.setNextQuarterlyDate(rs.getDate("T_NEXTQUARTERLYDATE"));
                                terminalBean.setNextHalfYearlyDate(rs.getDate("T_NEXTHALFYEARLYDATE"));
                                terminalBean.setNextWeeklyDate(rs.getDate("T_NEXTWEEKLYDATE"));

//                                terminalList.add(terminalBean);
                                if (merchantBean != null) {
                                    //add last merchant
                                    if (terminalList != null) {
                                        merchantBean.setTerminalList(terminalList); //add terminal list to existing merchant bean
                                    }
                                    merchantBeanList.add(merchantBean);
                                }
                            }

                        }
                        return merchantBeanList;
                    }
                    , status.getMERCHANT_DELETE_STATUS()
                    , status.getMERCHANT_CANCEL_STATUS()
            );

//            if (merchantBean != null) {
//                //add last merchant
//                if (terminalList != null) {
//                    merchantBean.setTerminalList(terminalList); //add terminal list to existing merchant bean
//                }
//                merchantBeanList.add(merchantBean);
//            }
        } catch (Exception e) {
            throw e;
        }
        return merchantBeanList;
    }

    @Override
    public int updateAllMerchantRecurringDates() throws Exception {
        int result = 0;
        try {
            // update nextanniversarydate=nextanniversarydate+12 months
            String query = "UPDATE MERCHANTLOCATION SET NEXTANNIVERSARYDATE=ADD_MONTHS(NEXTANNIVERSARYDATE,12),LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTANNIVERSARYDATE IS NOT NULL AND TRUNC(NEXTANNIVERSARYDATE)<=TRUNC(?)";

            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

            //update NEXTBIMONTHLYDATE=NEXTBIMONTHLYDATE+2 months
            query = "UPDATE MERCHANTLOCATION SET NEXTBIMONTHLYDATE=ADD_MONTHS(NEXTBIMONTHLYDATE,2),LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTBIMONTHLYDATE IS NOT NULL AND TRUNC(NEXTBIMONTHLYDATE)<=TRUNC(?)";

            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

            //update NEXTQUARTERLYDATE=NEXTQUARTERLYDATE+3 months
            query = "UPDATE MERCHANTLOCATION SET NEXTQUARTERLYDATE=ADD_MONTHS(NEXTQUARTERLYDATE,3),LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTQUARTERLYDATE IS NOT NULL AND TRUNC(NEXTQUARTERLYDATE)<=TRUNC(?)";

            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

            //update NEXTHALFYEARLYDATE=NEXTHALFYEARLYDATE+6 months
            query = "UPDATE MERCHANTLOCATION SET NEXTHALFYEARLYDATE=ADD_MONTHS(NEXTHALFYEARLYDATE,2),LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTHALFYEARLYDATE IS NOT NULL AND TRUNC(NEXTHALFYEARLYDATE)<=TRUNC(?)";

            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    @Override
    public int updateAllTerminalRecurringDates() throws Exception {
        int result = 0;
        try {
            String query = "UPDATE TERMINAL SET NEXTRENTALDATE=ADD_MONTHS(NEXTRENTALDATE,1),LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTRENTALDATE IS NOT NULL AND TRUNC(NEXTRENTALDATE)<=TRUNC(?)";
            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

            query = "UPDATE TERMINAL SET NEXTANNIVERSARYDATE=ADD_MONTHS(NEXTANNIVERSARYDATE,12),LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTANNIVERSARYDATE IS NOT NULL AND TRUNC(NEXTANNIVERSARYDATE)<=TRUNC(?)";
            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

            query = "UPDATE TERMINAL SET NEXTBIMONTHLYDATE=ADD_MONTHS(NEXTBIMONTHLYDATE,2),LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTBIMONTHLYDATE IS NOT NULL AND TRUNC(NEXTBIMONTHLYDATE)<=TRUNC(?)";
            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

            query = "UPDATE TERMINAL SET NEXTQUARTERLYDATE=ADD_MONTHS(NEXTQUARTERLYDATE,3),LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTQUARTERLYDATE IS NOT NULL AND TRUNC(NEXTQUARTERLYDATE)<=TRUNC(?)";
            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

            query = "UPDATE TERMINAL SET NEXTHALFYEARLYDATE=ADD_MONTHS(NEXTHALFYEARLYDATE,6),LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTHALFYEARLYDATE IS NOT NULL AND TRUNC(NEXTHALFYEARLYDATE)<=TRUNC(?)";
            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

            query = "UPDATE TERMINAL SET NEXTWEEKLYDATE=NEXTWEEKLYDATE+7,LASTUPDATEDTIME=SYSDATE,"
                    + "LASTUPDATEDUSER=? WHERE NEXTWEEKLYDATE IS NOT NULL AND TRUNC(NEXTWEEKLYDATE)<=TRUNC(?)";
            result = backendJdbcTemplate.update(query, Configurations.EOD_USER, DateUtil.getSqldate(Configurations.EOD_DATE));

        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    @Override
    public int addMerchantFeeCount(String merchantId, String feeCode) throws Exception {
        int count = 0;
        String query = null;

        try {
            boolean isFeeUpdateRequired = this.isMerchantFeeCountUpdateRequired(merchantId, feeCode);
            if (isFeeUpdateRequired) {
                query = "UPDATE MERCHANTFEECOUNT SET FEECOUNT = FEECOUNT + 1,"
                        + "  LASTUPDATEDUSER= ?, LASTUPDATEDTIME= SYSDATE, STATUS =?"
                        + " WHERE MERCHANTID = ? AND FEECODE = ? ";

                count = backendJdbcTemplate.update(query, Configurations.EOD_USER, Configurations.EOD_PENDING_STATUS, merchantId, feeCode);
            } else {
                query = "INSERT INTO MERCHANTFEECOUNT (MERCHANTID,FEECODE,FEECOUNT,STATUS,CREATEDDATE,LASTUPDATEDTIME,LASTUPDATEDUSER) VALUES (?,?,?,?,TO_DATE(?,'DD-MM-YY'),SYSDATE,?)";

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
                count = backendJdbcTemplate.update(query, merchantId, feeCode, 1, Configurations.EOD_PENDING_STATUS, sdf.format(Configurations.EOD_DATE), Configurations.EOD_USER);
            }

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    private Boolean isMerchantFeeCountUpdateRequired(String merchantId, String feeCode) throws Exception {
//        PreparedStatement ps = null;
//        ResultSet rs = null;
        boolean isUpdateRequired = false;
        try {
            String query = "SELECT COUNT(FEECODE) AS CNT FROM MERCHANTFEECOUNT WHERE MERCHANTID=? AND FEECODE=?";

            int isUpdate = backendJdbcTemplate.queryForObject(query, Integer.class, merchantId, feeCode);
            if (isUpdate > 0) {
                isUpdateRequired = true;
            }

        } catch (Exception e) {
            throw e;
        }
        return isUpdateRequired;
    }

}
