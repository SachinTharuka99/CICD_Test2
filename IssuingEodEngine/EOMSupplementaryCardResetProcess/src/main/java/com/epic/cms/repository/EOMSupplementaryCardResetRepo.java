/**
 * Author : sharuka_j
 * Date : 12/6/2022
 * Time : 9:01 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.EOMSupplementaryCardResetDao;
import com.epic.cms.model.bean.CalculateOTBsDto;
import com.epic.cms.model.bean.EomCardBalanceDto;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Repository
public class EOMSupplementaryCardResetRepo implements EOMSupplementaryCardResetDao {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    StatusVarList statusVarList;
    @Autowired
    LogManager logManager;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public ArrayList getEligibleAccounts() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        ArrayList accNo = new ArrayList();

        //String query = "SELECT CA.ACCOUNTNO FROM CARDACCOUNT CA,CARD C WHERE CA.CARDNUMBER=C.CARDNUMBER AND CA.NEXTBILLINGDATE <= TO_DATE( ?, 'DD-MM-YY') AND C.CARDSTATUS NOT IN (?,?)";
        String query = queryParametersList.getEOMSupplementaryCardReset_getEligibleAccounts();

        if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getINITIAL_STATUS())) {
            //query += " and CA.ACCOUNTNO not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status='"
            query += queryParametersList.getEOMSupplementaryCardReset_getEligibleAccounts_Appender1()
                    + statusVarList.getEOD_PENDING_STATUS() + "')";
        } else if (Configurations.STARTING_EOD_STATUS.equals(statusVarList.getERROR_STATUS())) {
            //query += " and CA.ACCOUNTNO in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status='"
             query += queryParametersList.getEOMSupplementaryCardReset_getEligibleAccounts_Appender2()
                    + statusVarList.getEOD_PENDING_STATUS() + "' and EODID < " + Configurations.ERROR_EOD_ID + " and PROCESSSTEPID <="
                    + Configurations.PROCESS_STEP_ID + ")";
        }
        try {
//            accNo = backendJdbcTemplate.query(query, sdf.format(Configurations.EOD_DATE));
            backendJdbcTemplate.query(query
                    , (ResultSet rs) -> {
                        while (rs.next()) {
                            accNo.add(rs.getString("ACCOUNTNO"));
                        }
                        return accNo;
                    }
                    , sdf.format(Configurations.EOD_DATE)
                    , statusVarList.getCARD_CLOSED_STATUS()
                    , statusVarList.getCARD_PRODUCT_CHANGE_STATUS());
        } catch (Exception e) {
            throw e;
        }
        return accNo;
    }

    @Override
    public ArrayList<StringBuffer> getAllTheCardsForAccount(StringBuffer accNo) throws Exception {

        ArrayList<StringBuffer> cardList = new ArrayList<>();
        //String sql = "SELECT CA.CARDNUMBER FROM CARDACCOUNTCUSTOMER CA,CARD CD WHERE CA.cardnumber = CD.CARDNUMBER AND CD.CARDSTATUS  NOT IN (?,?) AND CA.ACCOUNTNO  = ? ORDER BY CA.ACCOUNTNO,  CASE WHEN cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? THEN 1 WHEN cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? THEN 2 ELSE 3 END, CD.CARDNUMBER";

        try {
            backendJdbcTemplate.query(queryParametersList.getEOMSupplementaryCardReset_getAllTheCardsForAccount()
                    ,(ResultSet rs) -> {
                        while (rs.next()) {
                            cardList.add(new StringBuffer(rs.getString("CARDNUMBER")));
                        }
                        return cardList;
                    }
                    , statusVarList.getCARD_PRODUCT_CHANGE_STATUS()
                    , statusVarList.getCARD_REPLACED_STATUS()
                    , accNo.toString()
                    , Configurations.CARD_CATEGORY_MAIN
                    , Configurations.CARD_CATEGORY_ESTABLISHMENT
                    , Configurations.CARD_CATEGORY_FD
                    , Configurations.CARD_CATEGORY_AFFINITY
                    , Configurations.CARD_CATEGORY_CO_BRANDED
                    , Configurations.CARD_CATEGORY_SUPPLEMENTORY
                    , Configurations.CARD_CATEGORY_CORPORATE
                    , Configurations.CARD_CATEGORY_FD_SUPPLEMENTORY
                    , Configurations.CARD_CATEGORY_AFFINITY_SUPPLEMENTORY
                    , Configurations.CARD_CATEGORY_CO_BRANDED_SUPPLEMENTORY);


        } catch (Exception e) {
            throw e;
        }
        return cardList;
    }

    @Override
    public HashMap<String, Double> getCardBalances(StringBuffer cardNo) throws Exception {
        int count = 0;
        HashMap<String, Double> cardBalance = new HashMap<String, Double>();

        try {
            //String query = "SELECT NVL(EODSTARTINGBAL,0) AS EODSTARTINGBAL ,NVL(EODCLOSINGBAL,0) AS EODCLOSINGBAL ,NVL(FINANCIALCHARGES,0) AS FINANCIALCHARGES ,NVL(CUMCASHADVANCES,0) AS CUMCASHADVANCES ,NVL(CUMTRANSACTIONS,0) AS CUMTRANSACTIONS , (SELECT CREDITLIMIT FROM CARD WHERE CARDNUMBER = ?) AS CREDITLIMIT, (SELECT CASHLIMIT FROM CARD WHERE CARDNUMBER = ?) AS CASHLIMIT FROM EODCARDBALANCE WHERE CARDNUMBER = ?";

            backendJdbcTemplate.query(queryParametersList.getEOMSupplementaryCardReset_getCardBalances()
                    , (ResultSet rs) -> {
                        while (rs.next()) {
                            cardBalance.put("OpeningBal", rs.getDouble("EODSTARTINGBAL"));
                            cardBalance.put("closingBal", rs.getDouble("EODCLOSINGBAL"));
                            cardBalance.put("finCahrges", rs.getDouble("FINANCIALCHARGES"));
                            cardBalance.put("cashAdvanced", rs.getDouble("CUMCASHADVANCES"));
                            cardBalance.put("txns", rs.getDouble("CUMTRANSACTIONS"));
                            cardBalance.put("CREDITLIMIT", rs.getDouble("CREDITLIMIT"));
                            cardBalance.put("CASHLIMIT", rs.getDouble("CASHLIMIT"));
                        }
                        return cardBalance;
                    }
                    , cardNo
                    , cardNo
                    , cardNo);
        } catch (Exception e) {
            throw e;
        }
        return cardBalance;
    }

    @Override
    public void UpdateEOMCardBalance(StringBuffer cardNo, StringBuffer mainCardNo, HashMap<String, Double> CardBal) throws Exception {
        int count = 0;
        boolean hasRecordInEOMBalance = false;
        double OpeningBal = 0.00;
        double ClosingBal = 0.00;
        double fee = 0.00;
        double cashAdvanced = 0.00;
        double sales = 0.00;
        double payments = 0.00;
        HashMap<String, Double> cardBalabce = new HashMap<String, Double>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        EomCardBalanceDto cardBalanceDto = new EomCardBalanceDto();

        try {
            //String query = "SELECT CUMFINANCIALCHARGE,CUMCASHADVANCE,CUMTRANSACTION FROM EOMCARDBALANCE WHERE CARDNUMBER = ?";

            cardBalanceDto = backendJdbcTemplate.queryForObject(queryParametersList.getEOMSupplementaryCardReset_UpdateEOMCardBalance_Select()
                    , new RowMapper<EomCardBalanceDto>() {
                        @Override
                        public EomCardBalanceDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                            EomCardBalanceDto cardBalance = new EomCardBalanceDto();
                            cardBalance.setHasRecordInEOMBalance(true);
                            cardBalance.setCashAdvanced(rs.getDouble("CUMCASHADVANCE"));
                            cardBalance.setFee(rs.getDouble("CUMFINANCIALCHARGE"));
                            cardBalance.setSales(rs.getDouble("CUMTRANSACTION"));
                            return cardBalance;
                        }
                    }
                    , cardNo);

            fee = cardBalanceDto.getFee() + CardBal.get("finCahrges");
            cashAdvanced = cardBalanceDto.getCashAdvanced() + CardBal.get("cashAdvanced");
            sales = cardBalanceDto.getSales() + CardBal.get("txns");

            if (cardBalanceDto.isHasRecordInEOMBalance()) {
                //query = "UPDATE EOMCARDBALANCE SET LASTUPDATEDTIME = TO_DATE(?, 'DD-MM-YY'), LASTUPDATEDUSER=?,CUMFINANCIALCHARGE=?, CUMCASHADVANCE=?, CUMTRANSACTION=?,STATUS=?,EODID=? WHERE CARDNUMBER =?";

                backendJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_UpdateEOMCardBalance_Update()
                        , sdf.format(Configurations.EOD_DATE)
                        , Configurations.EOD_USER
                        , fee
                        , cashAdvanced
                        , sales
                        , Configurations.EOD_PENDING_STATUS
                        , Configurations.EOD_ID
                        , cardNo
                );
            } else {
                //query = "INSERT INTO EOMCARDBALANCE(CARDNUMBER,MAINCARDNUMBER, CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER,CUMFINANCIALCHARGE, CUMCASHADVANCE,CUMTRANSACTION,STATUS,ISPRIMARY,EODID) VALUES (?,?,TO_DATE(?,'DD-MM-YY'),TO_DATE(?, 'DD-MM-YY'),?,?,?,?,?,?,?)";

                backendJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_UpdateEOMCardBalance_Insert()
                        , cardNo
                        , mainCardNo
                        , sdf.format(Configurations.EOD_DATE)
                        , sdf.format(Configurations.EOD_DATE)
                        , Configurations.EOD_USER
                        , fee
                        , cashAdvanced
                        , sales
                        , Configurations.EOD_PENDING_STATUS
                        , (cardNo.equals(mainCardNo)) ? Configurations.YES_STATUS : Configurations.NO_STATUS
                        , Configurations.EOD_ID);
            }
        } catch (EmptyResultDataAccessException e) {
            throw e;
        }
    }

    @Override
    @Transactional("backendDb")
    public HashMap<String, Double> getCardTempBalances(StringBuffer cardNo) throws Exception {
        int count = 0;
        HashMap<String, Double> cardTempBalabce = new HashMap<String, Double>();

        try {
            //String query = "SELECT TEMPCREDITAMOUNT,TEMPCASHAMOUNT FROM CARD WHERE CARDNUMBER = ?";

            backendJdbcTemplate.query(queryParametersList.getEOMSupplementaryCardReset_getCardTempBalances()
                    , (ResultSet rs) -> {
                        while (rs.next()) {
                            cardTempBalabce.put("TEMPCREDITAMOUNT", rs.getDouble("TEMPCREDITAMOUNT"));
                            cardTempBalabce.put("TEMPCASHAMOUNT", rs.getDouble("TEMPCASHAMOUNT"));
                        }
                        return cardTempBalabce;
                    }
                    , cardNo);
        } catch (Exception e) {
            throw e;
        }
        return cardTempBalabce;
    }

    @Override
    public void resetEodCardBallance(StringBuffer cardNo) throws Exception {
        int count = 0;
        HashMap<String, Double> cardBalabce = new HashMap<String, Double>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

            //String query = "UPDATE EODCARDBALANCE SET EODSTARTINGBAL=(SELECT CREDITLIMIT FROM CARD WHERE CARDNUMBER = ?),EODCLOSINGBAL=(SELECT CREDITLIMIT FROM CARD WHERE CARDNUMBER = ?), LASTUPDATEDTIME= TO_DATE(?, 'DD-MM-YY'),LASTUPDATEDUSER=?,FINANCIALCHARGES=?, CUMCASHADVANCES=?,CUMTRANSACTIONS=? WHERE CARDNUMBER = ?";

            backendJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_resetEodCardBallance()
                    , cardNo //1
                    , cardNo //2
                    , sdf.format(Configurations.EOD_DATE) //3
                    , Configurations.EOD_USER //4
                    , 0 //5
                    , 0 //6
                    , 0 //7
                    , cardNo //8
            );
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public HashMap<String, Double> getEOMCardBalanceFromSupplementary(StringBuffer cardNo) throws Exception {
        HashMap<String, Double> supCardBalance = new HashMap<String, Double>();

        //String sql = "SELECT NVL(CUMFINANCIALCHARGE,0) AS CUMFINANCIALCHARGE,NVL(CUMCASHADVANCE,0) AS CUMCASHADVANCE,NVL(CUMTRANSACTION,0) AS CUMTRANSACTION FROM EOMCARDBALANCE WHERE CARDNUMBER = ?";

        try {
            backendJdbcTemplate.query(queryParametersList.getEOMSupplementaryCardReset_getEOMCardBalanceFromSupplementary()
                    , (ResultSet rs) -> {
                        while (rs.next()) {
//                            supCardBalance = new HashMap<String, Double>();
                            supCardBalance.put("finCahrges", rs.getDouble("CUMFINANCIALCHARGE"));
                            supCardBalance.put("cashAdvanced", rs.getDouble("CUMCASHADVANCE"));
                            supCardBalance.put("txns", rs.getDouble("CUMTRANSACTION"));
                        }
                        return supCardBalance;
                    }
                    , cardNo);

        } catch (Exception e) {
            throw e;
        }
        return supCardBalance;
    }

    @Override
    public int resetEOMCardBalance(StringBuffer cardNo) throws Exception {
        int count = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

        try {
            String query = "UPDATE EOMCARDBALANCE SET LASTUPDATEDTIME = TO_DATE(?, 'DD-MM-YY'), LASTUPDATEDUSER=?,CUMFINANCIALCHARGE=?,CUMCASHADVANCE=?, CUMTRANSACTION=?,STATUS=?,EODID=? WHERE CARDNUMBER =?";
            backendJdbcTemplate.update(query
                    , sdf.format(Configurations.EOD_DATE) //1
                    , Configurations.EOD_USER //2
                    , 0 //3
                    , 0 //4
                    , 0 //5
                    , Configurations.EOD_PENDING_STATUS //6
                    , Configurations.EOD_USER //7
                    , cardNo); //8
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public double calculateMainCardForwardPayments(StringBuffer mainCardNumber) throws Exception {
        double amount = 0.00;
        try {
            //String query = "select FORWARDAMOUNT from EODPAYMENT where CARDNUMBER = ? and STATUS = ? ";

            amount = backendJdbcTemplate.queryForObject(queryParametersList.getEOMSupplementaryCardReset_calculateMainCardForwardPayments(), Double.class, mainCardNumber.toString(), Configurations.EOD_PENDING_STATUS);
        } catch (EmptyResultDataAccessException ex) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return amount;
    }

    @Override
    public double calculateSupCardForwardPayments(StringBuffer mainCard) throws Exception {
        double amount = 0.00;

        try {
            //String query = "select FORWARDAMOUNT from EODPAYMENT where CARDNUMBER IN (Select CARDNUMBER from EODPAYMENT where CARDNUMBER <> MAINCARDNO and STATUS IN( ?,?) and MAINCARDNO = ?) and STATUS IN( ?,?)";


            amount = backendJdbcTemplate.queryForObject(queryParametersList.getEOMSupplementaryCardReset_calculateSupCardForwardPayments(), Double.class, Configurations.EOD_PENDING_STATUS
                    , Configurations.INITIAL_STATUS
                    , mainCard.toString()
                    , Configurations.EOD_PENDING_STATUS
                    , Configurations.INITIAL_STATUS);
        } catch (EmptyResultDataAccessException ex) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return amount;
    }

    @Override
    public void updateMainCardBal(StringBuffer mainCardNumber, Double totalSupTempCredit, Double totalSupTempCash, Double supFowardPayments) throws Exception {
        try {
            //String query = "update card set OTBCREDIT = OTBCREDIT - ?, OTBCASH = OTBCASH - ?, TEMPCREDITAMOUNT = TEMPCREDITAMOUNT + ?, TEMPCASHAMOUNT = TEMPCASHAMOUNT + ? where CARDNUMBER =?";

            backendJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_updateMainCardBal()
                    , totalSupTempCredit
                    , totalSupTempCash
                    , totalSupTempCredit
                    , totalSupTempCash
                    , mainCardNumber.toString());
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateMainCardBalOnline(StringBuffer mainCardNumber, Double totalSupTempCredit, Double totalSupTempCash, Double supFowardPayments) throws Exception {
        try {
            String query = "update ECMS_ONLINE_CARD set OTBCREDIT = OTBCREDIT - ? ,OTBCASH = OTBCASH - ?, TEMPCREDITAMOUNT = TEMPCREDITAMOUNT + ?, TEMPCASHAMOUNT = TEMPCASHAMOUNT + ? where CARDNUMBER = ?";

            onlineJdbcTemplate.update(query
                    , totalSupTempCredit
                    , totalSupTempCash
                    , totalSupTempCredit
                    , totalSupTempCash
                    , mainCardNumber.toString());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateMainCardBalOnline ===================" + Configurations.EOD_ID);
                logInfo.info(queryParametersList.getEOMSupplementaryCardReset_updateMainCardBalOnline());
                logInfo.info(Double.toString(totalSupTempCredit));
                logInfo.info(Double.toString(supFowardPayments));
                logInfo.info(Double.toString(totalSupTempCash));
                logInfo.info(Double.toString(totalSupTempCredit));
                logInfo.info(Double.toString(totalSupTempCash));
                logInfo.info(CommonMethods.cardNumberMask(mainCardNumber));
                logInfo.info("================ updateMainCardBalOnline END ===================");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int updateSupplementaryEODPaymentsStatus(StringBuffer mainCardNo) throws Exception {
        int result = 0;
        try {
            //String query = "update EODPAYMENT set status =?  where CARDNUMBER IN (Select CARDNUMBER from EODPAYMENT where CARDNUMBER <> MAINCARDNO and status in(?,?) and MAINCARDNO = ?) and STATUS in(?,?)";

            result = backendJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_updateSupplementaryEODPaymentsStatus()
                    , Configurations.EOD_DONE_STATUS //1
                    , Configurations.EOD_PENDING_STATUS //2
                    , Configurations.INITIAL_STATUS //3
                    , mainCardNo.toString() //4
                    , Configurations.EOD_PENDING_STATUS //5
                    , Configurations.INITIAL_STATUS //6
            );
        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    @Override
    public int insertNewEntryToEodPayment(StringBuffer mainCardNo, double allSupCardFP) {
        int sequenceNo = 100000;
        int count = 0;

        try {
            //String query = "SELECT NVL(MAX(SEQUENCENUMBER),0) AS  SEQUENCENUMBER FROM EODPAYMENT WHERE CARDNUMBER=? AND EODID=?";
            //String query2 = "INSERT INTO EODPAYMENT(SEQUENCENUMBER,EODID,CARDNUMBER,MAINCARDNO,ISPRIMARY,AMOUNT,PAYMENTTYPE,TRACEID,FORWARDAMOUNT,STATUS,CREATEDTIME,LASTUPDATEDUSER,LASTUPDATEDDATE) VALUES(?,?,?,?,?,?,?,?,?,?,sysdate,?,sysdate)";

            sequenceNo = backendJdbcTemplate.queryForObject(queryParametersList.getEOMSupplementaryCardReset_insertNewEntryToEodPayment_Select(), Integer.class, mainCardNo.toString(), Configurations.EOD_ID);
            if (sequenceNo == 0) {
                sequenceNo = 100000;
            } else {
                sequenceNo++;
            }

            backendJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_insertNewEntryToEodPayment_Insert()
                    , sequenceNo
                    , Configurations.EOD_ID //2
                    , mainCardNo.toString() //3
                    , mainCardNo.toString() //4
                    , Configurations.YES_STATUS //5
                    , allSupCardFP //6
                    , "CASH" //7
                    , Integer.toString(sequenceNo) //8
                    , allSupCardFP //9
                    , statusVarList.getINITIAL_STATUS() //10
                    , Configurations.EOD_USER //11
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public void updateEodCardBallance(StringBuffer cardNo, HashMap<String, Double> cardBal) throws Exception {
        int count = 0;
        HashMap<String, Double> cardBalabce = new HashMap<String, Double>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");

            //String query = "UPDATE EODCARDBALANCE SET EODSTARTINGBAL=?,EODCLOSINGBAL= ?, LASTUPDATEDTIME= TO_DATE(?, 'DD-MM-YY'),LASTUPDATEDUSER=?,FINANCIALCHARGES=?, CUMCASHADVANCES=?,CUMTRANSACTIONS=? WHERE CARDNUMBER = ?";

            backendJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_updateEodCardBallance()
                    , cardBal.get("OpeningBal")
                    , cardBal.get("closingBal")
                    , sdf.format(Configurations.EOD_DATE)
                    , Configurations.EOD_USER
                    , 0.00
                    , 0.00
                    , 0.00
                    , cardNo
            );
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public HashMap<StringBuffer, double[]> calculateOTBsAfterResetting(ArrayList<StringBuffer> cardList) throws Exception {

        HashMap<StringBuffer, double[]> OTBsAfterResetting = new HashMap<StringBuffer, double[]>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        double tempCrdtIncORDecAmt = 0.00;
        double tempCashIncORDecAmt = 0.00;
        CalculateOTBsDto dto = new CalculateOTBsDto();

        try {
            //this start from 1. bcz 1st element of this array is main card. So should skip the 1st element
            for (int i = 1; i < cardList.size(); i++) {
                //String query = "select amount,incrementtype,incordec from templimitincrement where cardno =? and status = ?";

                backendJdbcTemplate.queryForObject(queryParametersList.getEOMSupplementaryCardReset_calculateOTBsAfterResetting()
                        , new RowMapper<CalculateOTBsDto>() {
                            @Override
                            public CalculateOTBsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                                if (rs.getString("INCREMENTTYPE").equals("CREDIT")) {
                                    if (rs.getString("INCORDEC").equals(Configurations.LIMIT_INCREMENT)) {
                                        dto.setTempCrdtIncORDecAmt(dto.getTempCrdtIncORDecAmt() + rs.getDouble("amount"));
                                    } else if (rs.getString("INCORDEC").equals(Configurations.LIMIT_DECREMENT)) {
                                        dto.setTempCrdtIncORDecAmt(dto.getTempCrdtIncORDecAmt() - rs.getDouble("amount"));
                                    }
                                } else if (rs.getString("INCREMENTTYPE").equals("CASH")) {
                                    if (rs.getString("INCORDEC").equals(Configurations.LIMIT_INCREMENT)) {
                                        dto.setTempCashIncORDecAmt(dto.getTempCashIncORDecAmt() + rs.getDouble("amount"));
                                    } else if (rs.getString("INCORDEC").equals(Configurations.LIMIT_DECREMENT)) {
                                        dto.setTempCashIncORDecAmt(dto.getTempCashIncORDecAmt() - rs.getDouble("amount"));
                                    }
                                }
                                return dto;
                            }
                        }, cardList.get(i).toString(), statusVarList.getCREDIT_LIMIT_ENHANCEMENT_ACTIVE());

                double[] value = getCreditCashLimit(cardList.get(i));
                value[0] = value[0] + dto.getTempCrdtIncORDecAmt();
                value[1] = value[1] + dto.getTempCashIncORDecAmt();
                OTBsAfterResetting.put(cardList.get(i), value);
            }
        } catch (EmptyResultDataAccessException e) {
           return OTBsAfterResetting;
        }
        return OTBsAfterResetting;
    }

    @Override
    public void resetSuplimentryBalanceInBackendCardTable(HashMap<StringBuffer, double[]> map, StringBuffer mainCardNo) throws Exception {
        double[] value = new double[2];

        try {
            for (Map.Entry<StringBuffer, double[]> entry : map.entrySet()) {
                StringBuffer cardnumber = entry.getKey();
                double[] values = entry.getValue();

                if (!(cardnumber.toString().equalsIgnoreCase(mainCardNo.toString()))) {

                    //String query = "UPDATE CARD SET OTBCREDIT=?,OTBCASH=?,TEMPCREDITAMOUNT=?,TEMPCASHAMOUNT=? where CARDNUMBER=?";

                    backendJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_resetSuplimentryBalanceInBackendCardTable()
                            , values[0]
                            , values[1]
                            , 0
                            , 0
                            , cardnumber);
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void resetSuplimentryBalanceInOnlineCardTable(HashMap<StringBuffer, double[]> map, StringBuffer mainCardNo) throws Exception {
        double[] value = new double[2];

        try {
            for (Map.Entry<StringBuffer, double[]> entry : map.entrySet()) {
                StringBuffer cardnumber = entry.getKey();
                double[] values = entry.getValue();
                if (!(cardnumber.toString().equalsIgnoreCase(mainCardNo.toString()))) {
                    //String query = "UPDATE ECMS_ONLINE_CARD SET OTBCREDIT=?,OTBCASH=?,TEMPCREDITAMOUNT=?,TEMPCASHAMOUNT=? where CARDNUMBER=?";

                    onlineJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_resetSuplimentryBalanceInOnlineCardTable()
                            , values[0]
                            , values[1]
                            , 0
                            , 0
                            , cardnumber);

                    if (Configurations.ONLINE_LOG_LEVEL == 1) {
                        //Only for troubleshoot
                        logInfo.info("================ resetSuplimentryBalanceInOnlineCardTable ===================" + Configurations.EOD_ID);
                        logInfo.info(queryParametersList.getEOMSupplementaryCardReset_resetSuplimentryBalanceInOnlineCardTable());
                        logInfo.info(Double.toString(values[0]));
                        logInfo.info(Double.toString(values[1]));
                        logInfo.info("0,0");
                        logInfo.info(CommonMethods.cardNumberMask(cardnumber));
                        logInfo.info("================ resetSuplimentryBalanceInOnlineCardTable END ===================");
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int updatePreviousEODErrorCardDetails(String prevEODID, int stepId) throws Exception {
        int result = 0;

        int eodID = Integer.parseInt(prevEODID);
        try {
            //String query = "update EODERRORCARDS set STATUS = ? where EODID = ? and PROCESSSTEPID < ?";

            result = backendJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_updatePreviousEODErrorCardDetails(), statusVarList.getEOD_DONE_STATUS(), eodID, stepId);

        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    @Override
    public int updateEodProcessSummery(int eodId, String status, int processId) throws Exception {
        int count = 0;
        //String Query = "UPDATE EODPROCESSSUMMERY SET ENDTIME = SYSDATE , STATUS = ?,LASTUPDATEDTIME = SYSDATE,LASTUPDATEDUSER = ? WHERE EODID = ? AND PROCESSID = ?";
        try {
            count = onlineJdbcTemplate.update(queryParametersList.getEOMSupplementaryCardReset_updateEodProcessSummery(), status, Configurations.EOD_USER, eodId, processId);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    public double[] getCreditCashLimit(StringBuffer cardNo) throws Exception {
        double[] value = new double[2];

        try {
            //String query = "SELECT CREDITLIMIT, CASHLIMIT FROM CARD where CARDNUMBER=?";

            backendJdbcTemplate.query(queryParametersList.getEOMSupplementaryCardReset_getCreditCashLimit()
                    , (ResultSet rs) -> {
                        while (rs.next()) {
                            value[0] = rs.getDouble("CREDITLIMIT");
                            value[1] = rs.getDouble("CASHLIMIT");
                        }
                    }, cardNo);
        } catch (Exception e) {
            throw e;
        }
        return value;
    }
}
