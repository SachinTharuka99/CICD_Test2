/**
 * Author : yasiru_d
 * Date : 11/14/2022
 * Time : 5:34 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.repository;

import com.epic.cms.Exception.FailedCardException;
import com.epic.cms.dao.MonthlyStatementDao;
import com.epic.cms.model.bean.CalculateMinPaymentBean;
import com.epic.cms.model.bean.CardBean;
import com.epic.cms.model.bean.CardTransactionBean;
import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class MonthlyStatementRepo implements MonthlyStatementDao {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    StatusVarList statusList;
    @Autowired
    LogManager logManager;
    @Autowired
    JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public HashMap<String, ArrayList<CardBean>> getCardAccountListForBilling() throws Exception {

        HashMap<String, ArrayList<CardBean>> hMap = new HashMap<String, ArrayList<CardBean>>();
        String query = null;
        try {

            //query = "SELECT CA.ACCOUNTNO,CDA.BILLINGID,CD.CARDNUMBER,cd.cardcategorycode, CD.CREDITLIMIT,CD.CASHLIMIT,CD.OTBCREDIT,CD.OTBCASH,CD.MAINCARDNO, CDA.NEXTBILLINGDATE,CD.CREATEDTIME,CD.CLOSEFLAG FROM CARDACCOUNTCUSTOMER CA, CARD CD,CARDACCOUNT CDA WHERE ca.cardnumber=CD.CARDNUMBER AND CDA.ACCOUNTNO =CA.ACCOUNTNO AND trunc(CDA.NEXTBILLINGDATE) =TO_DATE(?,'DD-MON-YY') AND (CD.CARDSTATUS NOT  IN(?,?,?) OR (CD.CARDSTATUS = ? AND CD.CLOSEFLAG = ?))";
            query = queryParametersList.getMonthlyStatement_getCardAccountListForBilling();
            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                //query += "and CA.ACCOUNTNO not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? )";
                query+=queryParametersList.getMonthlyStatement_getCardAccountListForBilling_Appender1();
            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {
                //query += "and CA.ACCOUNTNO in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? and EODID < ?  and PROCESSSTEPID <= ? )";
                query = queryParametersList.getMonthlyStatement_getCardAccountListForBilling_Appender2();
            }

            //query += "ORDER BY CA.ACCOUNTNO, CASE WHEN cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? THEN 1 WHEN cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? OR cd.cardcategorycode = ? THEN 2 ELSE 3 END, CD.CARDNUMBER";
            query += queryParametersList.getMonthlyStatement_getCardAccountListForBilling_Appender3();

            SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yy");

            if (Configurations.STARTING_EOD_STATUS.equals(statusList.getINITIAL_STATUS())) {
                String dateEID = format.format(Configurations.EOD_DATE);
                hMap = backendJdbcTemplate.query(query, (ResultSet rs) -> {
                    HashMap<String, ArrayList<CardBean>> Map = new HashMap<String, ArrayList<CardBean>>();
                    CardBean cb = new CardBean();
                    while (rs.next()) {
                        try {
                            String accNo = rs.getString("ACCOUNTNO");
                            boolean replaceStatus = checkReplaceStatus(new StringBuffer(rs.getString("CARDNUMBER")));
                            List<StringBuffer> oldCards = getAllOldCards(new StringBuffer(rs.getString("CARDNUMBER")));
                            cb.setOldCardNumbers(oldCards);
                            cb.setBillingID(rs.getString("BILLINGID"));
                            cb.setHasReplacedCards(replaceStatus);
                            cb.setAccountno(accNo);
                            cb.setCardnumber(new StringBuffer(rs.getString("CARDNUMBER")));
                            cb.setCardCategory(rs.getString("cardcategorycode"));
                            cb.setCashLimit(rs.getDouble("CASHLIMIT"));
                            cb.setCreditLimit(rs.getDouble("CREDITLIMIT"));
                            cb.setCreatedDate(rs.getDate("CREATEDTIME"));
                            cb.setMainCardNo(new StringBuffer(rs.getString("MAINCARDNO")));
                            cb.setNextBillingDate(rs.getDate("NEXTBILLINGDATE"));
                            if (Map.containsKey(accNo)) {
                                ArrayList<CardBean> TempCardAccountList = Map.get(accNo);
                                TempCardAccountList.add(cb);
                                Map.put(accNo, TempCardAccountList);
                            } else {
                                ArrayList<CardBean> cardBeanList = new ArrayList<CardBean>();
                                cardBeanList.add(cb);
                                Map.put(accNo, cardBeanList);
                            }
                            if (statusList.getYES_STATUS_1() == rs.getDouble("CLOSEFLAG")) {
                                updateCloseCardFlag(new StringBuffer(rs.getString("CARDNUMBER"))); //update closeFlag in card table -> 0
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return Map;
                }, dateEID, statusList.getCARD_REPLACED_STATUS(), statusList.getCARD_PRODUCT_CHANGE_STATUS(), statusList.getCARD_CLOSED_STATUS(), statusList.getCARD_CLOSED_STATUS(), statusList.getYES_STATUS_1(), statusList.getEOD_PENDING_STATUS(), Configurations.CARD_CATEGORY_MAIN, Configurations.CARD_CATEGORY_ESTABLISHMENT, Configurations.CARD_CATEGORY_FD, Configurations.CARD_CATEGORY_AFFINITY, Configurations.CARD_CATEGORY_CO_BRANDED, Configurations.CARD_CATEGORY_SUPPLEMENTORY, Configurations.CARD_CATEGORY_CORPORATE, Configurations.CARD_CATEGORY_FD_SUPPLEMENTORY, Configurations.CARD_CATEGORY_AFFINITY_SUPPLEMENTORY, Configurations.CARD_CATEGORY_CO_BRANDED_SUPPLEMENTORY);

            } else if (Configurations.STARTING_EOD_STATUS.equals(statusList.getERROR_STATUS())) {

                String dateEID = format.format(Configurations.EOD_DATE);
                hMap = backendJdbcTemplate.query(query, (ResultSet rs) -> {
                    HashMap<String, ArrayList<CardBean>> Map = new HashMap<String, ArrayList<CardBean>>();
                    while (rs.next()) {
                        try {
                            CardBean cb = new CardBean();
                            String accNo = rs.getString("ACCOUNTNO");
                            boolean replaceStatus = checkReplaceStatus(new StringBuffer(rs.getString("CARDNUMBER")));
                            List<StringBuffer> oldCards = getAllOldCards(new StringBuffer(rs.getString("CARDNUMBER")));
                            cb.setOldCardNumbers(oldCards);
                            cb.setBillingID(rs.getString("BILLINGID"));
                            cb.setHasReplacedCards(replaceStatus);
                            cb.setAccountno(accNo);
                            cb.setCardnumber(new StringBuffer(rs.getString("CARDNUMBER")));
                            cb.setCardCategory(rs.getString("cardcategorycode"));
                            cb.setCashLimit(rs.getDouble("CASHLIMIT"));
                            cb.setCreditLimit(rs.getDouble("CREDITLIMIT"));
                            cb.setCreatedDate(rs.getDate("CREATEDTIME"));
                            cb.setMainCardNo(new StringBuffer(rs.getString("MAINCARDNO")));
                            cb.setNextBillingDate(rs.getDate("NEXTBILLINGDATE"));
                            if (Map.containsKey(accNo)) {
                                ArrayList<CardBean> TempCardAccountList = Map.get(accNo);
                                TempCardAccountList.add(cb);
                                Map.put(accNo, TempCardAccountList);
                            } else {
                                ArrayList<CardBean> cardBeanList = new ArrayList<CardBean>();
                                cardBeanList.add(cb);
                                Map.put(accNo, cardBeanList);
                            }
                            if (statusList.getYES_STATUS_1() == rs.getDouble("CLOSEFLAG")) {
                                updateCloseCardFlag(new StringBuffer(rs.getString("CARDNUMBER"))); //update closeFlag in card table -> 0
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return Map;
                }, dateEID, statusList.getCARD_REPLACED_STATUS(), statusList.getCARD_PRODUCT_CHANGE_STATUS(), statusList.getCARD_CLOSED_STATUS(), statusList.getCARD_CLOSED_STATUS(), statusList.getYES_STATUS_1(), statusList.getEOD_PENDING_STATUS(), Configurations.ERROR_EOD_ID, Configurations.PROCESS_STEP_ID, Configurations.CARD_CATEGORY_MAIN, Configurations.CARD_CATEGORY_ESTABLISHMENT, Configurations.CARD_CATEGORY_FD, Configurations.CARD_CATEGORY_AFFINITY, Configurations.CARD_CATEGORY_CO_BRANDED, Configurations.CARD_CATEGORY_SUPPLEMENTORY, Configurations.CARD_CATEGORY_CORPORATE, Configurations.CARD_CATEGORY_FD_SUPPLEMENTORY, Configurations.CARD_CATEGORY_AFFINITY_SUPPLEMENTORY, Configurations.CARD_CATEGORY_CO_BRANDED_SUPPLEMENTORY);
            }

        } catch (Exception e) {
            throw e;
        }
        return hMap;
    }

    @Override
    public void UpdateStatementDeatils(List<CardBean> CardBeanList, StatementBean stBean, String accountNum) throws Exception {

        StatementBean mainStBean = new StatementBean();
        mainStBean.setHasBillingCycleChangeRequest(stBean.getHasBillingCycleChangeRequest());
        mainStBean.setNewNextBillingDate(stBean.getNewNextBillingDate());
        boolean isInsertedBillingStatement = false;
        Double totalPaymentsAndReversals = 0.0;
        Double TotalSalesAndEasyPayments = 0.0;
        Double TotalInterest = 0.0;
        Double Totalfee = 0.0;
        Double TotalCashAdvance = 0.0;
        Double TotalCrAdj = 0.0;
        Double TotalDrAdj = 0.0;
        Double TotalOtherCreditswithoutPayments = 0.00;
        StringBuffer MainCardNo = null;
        StringBuffer cardNo = null;
        StringBuffer allCardNumbers = null;
        int startEodID = 0;
        int endEodID = 0;
        String statementID = null;
        String accountNo = null;
        Date DueDate = calculateDueDate(CardBeanList.get(0).getAccountno());
        DueDate = DateUtil.getSqldate(this.getHolidayFalseDueDate(DueDate));

        try {
            accountNo = CardBeanList.get(0).getAccountno();
            statementID = new SimpleDateFormat("yyMMHHmmssSSS").format(new java.util.Date()) + accountNum;

            for (int i = 0; i < CardBeanList.size(); i++) {
                totalPaymentsAndReversals = 0.0;
                TotalSalesAndEasyPayments = 0.0;
                TotalInterest = 0.0;
                Totalfee = 0.0;
                TotalCashAdvance = 0.0;
                TotalCrAdj = 0.0;
                TotalDrAdj = 0.0;
                TotalOtherCreditswithoutPayments = 0.00;
                CardBean tempBean = CardBeanList.get(i);
                CardTransactionBean trnsBean = null;
                startEodID = getThisStatementStartandEndEodId(tempBean.getMainCardNo());
                endEodID = Configurations.EOD_ID;

                if (tempBean.getHasReplacedCards()) {
                    int noOfCards = tempBean.getOldCardNumbers().size();
                    for (int j = 0; j < noOfCards; j++) {
                        cardNo = new StringBuffer(tempBean.getOldCardNumbers().get(j));
                        if (allCardNumbers == null) {
                            allCardNumbers = cardNo;
                        } else {
                            allCardNumbers = allCardNumbers.append(",").append(cardNo);
                        }
                        trnsBean = getCardTranactionSummeryBean(startEodID, endEodID, cardNo);
                        TotalSalesAndEasyPayments = TotalSalesAndEasyPayments + trnsBean.getSalesAndEasyPayments();
                        TotalInterest = TotalInterest + trnsBean.getInterest();
                        totalPaymentsAndReversals = totalPaymentsAndReversals + trnsBean.getPaymentsAndRevarsal() + trnsBean.getTotalCrAdj();
                        Totalfee = Totalfee + trnsBean.getFee();
                        TotalCashAdvance = TotalCashAdvance + trnsBean.getCashAdvance();
                        TotalCrAdj = TotalCrAdj + trnsBean.getTotalCrAdj();
                        TotalDrAdj = TotalDrAdj + trnsBean.getTotalDrAdj();
                        TotalOtherCreditswithoutPayments = TotalOtherCreditswithoutPayments + trnsBean.getCreditsWithoutPayments();
                    }
                } else if (!tempBean.getHasReplacedCards()) {
                    cardNo = new StringBuffer(tempBean.getCardnumber());
                    if (allCardNumbers == null) {
                        allCardNumbers = cardNo;
                    } else {
                        allCardNumbers = allCardNumbers.append(",").append(cardNo);
                    }
                    trnsBean = getCardTranactionSummeryBean(startEodID, endEodID, cardNo);
                    TotalSalesAndEasyPayments = trnsBean.getSalesAndEasyPayments();
                    TotalInterest = trnsBean.getInterest();
                    totalPaymentsAndReversals = trnsBean.getPaymentsAndRevarsal() + trnsBean.getTotalCrAdj();
                    Totalfee = trnsBean.getFee();
                    TotalCashAdvance = trnsBean.getCashAdvance();
                    TotalCrAdj = trnsBean.getTotalCrAdj();
                    TotalDrAdj = trnsBean.getTotalDrAdj();
                    TotalOtherCreditswithoutPayments = trnsBean.getCreditsWithoutPayments();
                }

                if (tempBean.getCardCategory().equals(Configurations.CARD_CATEGORY_MAIN) || tempBean.getCardCategory().equals(Configurations.CARD_CATEGORY_ESTABLISHMENT) || tempBean.getCardCategory().equals(Configurations.CARD_CATEGORY_FD) || tempBean.getCardCategory().equals(Configurations.CARD_CATEGORY_AFFINITY) || tempBean.getCardCategory().equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                    MainCardNo = new StringBuffer(tempBean.getCardnumber());
                    mainStBean.setAccountNo(tempBean.getAccountno());
                    mainStBean.setCardNo(MainCardNo);
                    mainStBean.setMainCardNo(MainCardNo);
                    mainStBean.setCardCategory(tempBean.getCardCategory());
                    mainStBean.setStatementDueDate(DueDate);
                    mainStBean.setCreditLimit(tempBean.getCreditLimit());
                    mainStBean.setCashLimit(tempBean.getCashLimit());
                    mainStBean.setOldNextBillingDate(tempBean.getNextBillingDate());
                    mainStBean.setCardCreatedDate(tempBean.getCreatedDate());
                    mainStBean.setStatementID(statementID);
                    mainStBean.setFee(mainStBean.getFee() + Totalfee);
                    mainStBean.setCashAdvance(mainStBean.getCashAdvance() + TotalCashAdvance);
                    mainStBean.setPaymentAndCredit(mainStBean.getPaymentAndCredit() + totalPaymentsAndReversals);
                    mainStBean.setInterest(mainStBean.getInterest() + TotalInterest);
                    mainStBean.setTotalPurchases(mainStBean.getTotalPurchases() + TotalSalesAndEasyPayments);
                    mainStBean.setTotalCrAdj(mainStBean.getTotalCrAdj() + TotalCrAdj);
                    mainStBean.setTotalDrAdj(mainStBean.getTotalDrAdj() + TotalDrAdj);
                    mainStBean.setTotalCreditsWithoutPayments(mainStBean.getTotalCreditsWithoutPayments() + TotalOtherCreditswithoutPayments);
                    mainStBean.setStartEodID(startEodID);
                    mainStBean.setEndEodID(endEodID);
                    mainStBean.setBillingID(tempBean.getBillingID());
                } else {
                    stBean.setAccountNo(tempBean.getAccountno());
                    stBean.setMainCardNo(tempBean.getMainCardNo());
                    stBean.setCardCategory(tempBean.getCardCategory());
                    stBean.setStatementDueDate(DueDate);
                    stBean.setCardNo(tempBean.getCardnumber());
                    stBean.setCashLimit(tempBean.getCashLimit());
                    stBean.setCreditLimit(tempBean.getCreditLimit());
                    stBean.setPaymentAndCredit(totalPaymentsAndReversals);
                    stBean.setTotalPurchases(TotalSalesAndEasyPayments);
                    stBean.setInterest(TotalInterest);
                    stBean.setFee(Totalfee);
                    stBean.setCashAdvance(TotalCashAdvance);
                    stBean.setOldNextBillingDate(tempBean.getNextBillingDate());
                    stBean.setStatementDueDate(DueDate);
                    stBean.setCardCreatedDate(tempBean.getCreatedDate());
                    stBean.setStatementID(statementID);
                    stBean.setStartEodID(startEodID);
                    stBean.setEndEodID(endEodID);
                    stBean.setTotalCrAdj(TotalCrAdj);
                    stBean.setTotalDrAdj(TotalDrAdj);
                    stBean.setTotalCreditsWithoutPayments(TotalOtherCreditswithoutPayments);
                    mainStBean.setPaymentAndCredit(mainStBean.getPaymentAndCredit() + stBean.getPaymentAndCredit());
                    mainStBean.setTotalPurchases(mainStBean.getTotalPurchases() + stBean.getTotalPurchases());
                    mainStBean.setInterest(mainStBean.getInterest() + stBean.getInterest());
                    mainStBean.setFee(mainStBean.getFee() + stBean.getFee());
                    mainStBean.setCashAdvance(mainStBean.getCashAdvance() + stBean.getCashAdvance());
                    mainStBean.setClosingBalance(mainStBean.getClosingBalance() + stBean.getClosingBalance());
                    mainStBean.setTotalCrAdj(mainStBean.getTotalCrAdj() + stBean.getTotalCrAdj());
                    mainStBean.setTotalDrAdj(mainStBean.getTotalDrAdj() + stBean.getTotalDrAdj());
                    mainStBean.setTotalCreditsWithoutPayments(mainStBean.getTotalCreditsWithoutPayments() + stBean.getTotalCreditsWithoutPayments());
                }
            }

            int updatedRows = updateStatementIDByAccNoInEODTxn(statementID, startEodID, endEodID, accountNo);
            mainStBean = getLastStatementDetails(mainStBean);
            isInsertedBillingStatement = insertBillingStatement(mainStBean);

            if (!isInsertedBillingStatement) {
                throw new FailedCardException("card no " + CommonMethods.cardNumberMask(cardNo) + "fails to insert data into billing statement table");
            }

            mainStBean.setInsertedBillingStatement(isInsertedBillingStatement);
            updateNextBillingDate(mainStBean);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean checkReplaceStatus(StringBuffer cardNo) throws Exception {

        boolean hasReplaceCards = false;

        try {
            //String query = "SELECT COUNT(*) AS CARDCOUNT FROM CARDREPLACE WHERE NEWCARDNUMBER =?";
            hasReplaceCards = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_checkReplaceStatus(), (ResultSet rs) -> {
                boolean temp = false;
                while (rs.next()) {
                    if (rs.getInt("CARDCOUNT") > 0) {
                        temp = true;
                    }
                }
                return temp;
            }, cardNo.toString()));

        } catch (Exception e) {
            throw e;
        }
        return hasReplaceCards;
    }

    @Override
    public StatementBean CheckBillingCycleChangeRequest(String accNo) throws Exception {

        StatementBean stBean = new StatementBean();
        stBean.setHasBillingCycleChangeRequest(false);

        try {
            //String query = "SELECT LAST_DAY(ADD_MONTHS(SYSDATE,0))+ NEWBILLINGDATE AS NEWSTATEMENTDATE,NEWBILLINGDATE,REQUESTID FROM DUAL,BILLINGCHANGEREQUEST WHERE ACCOUNTNUMBER=? AND STATUS = ? and EODSTATUS <> ?";
            String query = queryParametersList.getMonthlyStatement_CheckBillingCycleChangeRequest();
            backendJdbcTemplate.query(query, (ResultSet rs) -> {
                while (rs.next()) {
                    stBean.setHasBillingCycleChangeRequest(true);
                    stBean.setNewNextBillingDate(rs.getDate("NEWSTATEMENTDATE"));
                    String reqId = rs.getString("REQUESTID");
                    try {
                        updateBillingCycleRequestBCCP(accNo, reqId);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                return stBean;
            }, accNo, "RQAC", statusList.getBILLING_DONE_STATUS());

            if (!stBean.getHasBillingCycleChangeRequest()) {
                //query = "SELECT LAST_DAY((NEXTBILLINGDATE))+ BILLINGDATE AS NEWNEXTBILLINGDATE,NEXTBILLINGDATE,BILLINGDATE FROM CARDACCOUNT WHERE ACCOUNTNO=?";
                query = queryParametersList.getMonthlyStatement_CheckBillingCycleChangeRequest_Appender1();

                backendJdbcTemplate.query(query, (ResultSet rs) -> {
                    while (rs.next()) {
                        stBean.setNewNextBillingDate(rs.getDate("NEWNEXTBILLINGDATE"));
                    }
                    return stBean;
                }, accNo);
            }

        } catch (Exception e) {
            throw e;
        }
        return stBean;
    }

    @Override
    public List<StringBuffer> getAllOldCards(StringBuffer cardNo) throws Exception {

        List<StringBuffer> oldCards = new ArrayList<StringBuffer>();

        try {
            //String query = "select oldcardnumber from cardreplace cr START WITH cr.newcardnumber =? CONNECT BY PRIOR cr.oldcardnumber = cr.newcardnumber";
            oldCards = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_getAllOldCards(), (ResultSet rs) -> {
                int count = 1;
                List<StringBuffer> tempOldCards = new ArrayList<StringBuffer>();
                while (rs.next()) {
                    StringBuffer cardNumber = new StringBuffer(rs.getString("OLDCARDNUMBER"));
                    tempOldCards.add(cardNumber);
                    count++;
                }
                return tempOldCards;
            }, cardNo));

        } catch (Exception e) {
            throw e;
        }
        return oldCards;
    }

    @Override
    public void updateCloseCardFlag(StringBuffer CardNumbers) throws Exception {

        try {
            //String query = "UPDATE CARD SET CLOSEFLAG = ? WHERE CARDNUMBER = ? AND CLOSEFLAG = ?";
            backendJdbcTemplate.update(queryParametersList.getMonthlyStatement_updateCloseCardFlag(), statusList.getNO_STATUS_0(), CardNumbers.toString(), statusList.getYES_STATUS_1());

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateBillingCycleRequestBCCP(String AccNo, String reqID) throws Exception {

        java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);

        try {
            //String query = "update BILLINGCHANGEREQUEST set EODSTATUS = ? , STATUS = ?,LASTEODUPDATEDDATE=? where ACCOUNTNUMBER =? AND REQUESTID = ?";
            backendJdbcTemplate.update(queryParametersList.getMonthlyStatement_updateBillingCycleRequestBCCP(), statusList.getBILLING_DONE_STATUS(), "RPBC", eodDate, AccNo, reqID);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Date calculateDueDate(String AccNo) throws Exception {

        Date DueDate = null;

        try {
            //String sql_DueDate = "SELECT NEXTBILLINGDATE + (SELECT GRACEPERIOD FROM  BILLINGSTATEMENTPROFILE BSP,CARDACCOUNT CA WHERE CA.ACCOUNTNO = ? AND CA.BILLINGSTATEMENTPROFILECODE = BSP.PROFILECODE) as DUEDATE FROM CARDACCOUNT WHERE ACCOUNTNO = ?";
            DueDate = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_calculateDueDate(), (ResultSet result_temp) -> {
                Date date = null;
                while (result_temp.next()) {
                    date = result_temp.getDate("DUEDATE");
                }
                return date;
            },
                    AccNo,
                    AccNo));

        } catch (Exception e) {
            throw e;
        }
        return DueDate;
    }

    @Override
    public boolean isHoliday(java.util.Date today) throws Exception {

        boolean isHoliday = false;

        try {
            //String query = "SELECT COUNT(*) FROM HOLIDAY WHERE YEAR = ? AND MONTH=? AND DAY=?";
            isHoliday = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_isHoliday(), (ResultSet rs) -> {
                        boolean temp = false;
                        if (rs.next()) {
                            int count = Integer.parseInt(rs.getString(1).trim());
                            if (count > 0) {
                                temp = true;
                                return temp;
                            } else {
                                temp = false;
                                return temp;
                            }
                        } else {
                            return temp;
                        }
                    },

                    String.valueOf(today.getYear() + 1900),
                    String.valueOf(today.getMonth() + 1),
                    String.valueOf(today.getDate())));

        } catch (Exception e) {
            return false;
        }
        return isHoliday;
    }

    @Override
    public int getThisStatementStartandEndEodId(StringBuffer cardNo) throws Exception {

        int startEODId = 0;

        try {
            //String query = "SELECT BS.STARTEODID , BS.ENDEODID FROM BILLINGSTATEMENT BS,BILLINGLASTSTATEMENTSUMMARY BLS WHERE BS.STATEMENTID= BLS.STATEMENTID AND BLS.CARDNO = ?";
            startEODId = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_getThisStatementStartandEndEodId(), (ResultSet rs) -> {
                int eod = 0;
                while (rs.next()) {
                    eod = rs.getInt("ENDEODID");
                }
                return eod;
            }, cardNo.toString()));

        } catch (Exception e) {
            throw e;
        }
        return startEODId;
    }

    @Override
    public CardTransactionBean getCardTranactionSummeryBean(int StartEODID, int EndEODID, StringBuffer cardNo) throws Exception {

        CardTransactionBean cardTransactionBean = new CardTransactionBean();

        try {
            //String sql = "SELECT SalesAndEasyPayments ,interest,PaymentsAndReversal,fees,cashadvances,cradj,dradj,CreditsWithoutPayments FROM ( select NVL(SUM(TRANSACTIONAMOUNT),0) SalesAndEasyPayments FROM  EODTRANSACTION where TRANSACTIONTYPE IN (?,?,?,?,?) and EODID >? AND EODID <=? AND cardnumber =? AND ADJUSTMENTSTATUS= ?) A CROSS JOIN ( select NVL(SUM(FORWARDINTEREST),0) Interest FROM  EOMINTEREST where CARDNO = ?) B CROSS JOIN ( select NVL(SUM(TRANSACTIONAMOUNT),0) PaymentsAndReversal FROM  EODTRANSACTION where TRANSACTIONTYPE IN (?,?,?,?,?) and EODID >? AND EODID <=? AND cardnumber = ? AND ADJUSTMENTSTATUS= ?) C CROSS JOIN (select NVL( SUM(feeamount),0) fees from eodcardfee where cardnumber=? and status = ? and ADJUSTMENTSTATUS = 'NO') D CROSS JOIN ( select NVL(SUM(TRANSACTIONAMOUNT),0) CASHADVANCEs FROM  EODTRANSACTION where TRANSACTIONTYPE =? and EODID >? AND EODID <=? AND cardnumber = ? AND ADJUSTMENTSTATUS= ?) E CROSS JOIN (select NVL( SUM(AMOUNT),0) CRADJ from ADJUSTMENT where UNIQUEID=? and eodstatus = ? AND crdr = ? and ADJUSTMENTTYPE not in(?,?)) F CROSS JOIN (select NVL( SUM(AMOUNT),0) DRADJ from ADJUSTMENT where UNIQUEID=? and eodstatus = ? AND crdr = ? and ADJUSTMENTTYPE not in(?,?)) G CROSS JOIN (select NVL( SUM(TRANSACTIONAMOUNT),0) CreditsWithoutPayments from EODTRANSACTION where TRANSACTIONTYPE IN (?,?) and EODID >? AND EODID <=? AND cardnumber = ? AND ADJUSTMENTSTATUS= ?) H";
            backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_getCardTranactionSummeryBean(), (ResultSet rs) -> {
                while (rs.next()) {
                    cardTransactionBean.setCardNo(cardNo.toString());
                    cardTransactionBean.setSalesAndEasyPayments(rs.getDouble("SalesAndEasyPayments"));
                    cardTransactionBean.setInterest(rs.getDouble("interest"));
                    cardTransactionBean.setPaymentsAndRevarsal(rs.getDouble("PaymentsAndReversal"));
                    cardTransactionBean.setFee(rs.getDouble("fees"));
                    cardTransactionBean.setCashAdvance(rs.getDouble("cashadvances"));
                    cardTransactionBean.setTotalCrAdj(rs.getDouble("cradj"));
                    cardTransactionBean.setTotalDrAdj(rs.getDouble("dradj"));
                    cardTransactionBean.setCreditsWithoutPayments(rs.getDouble("CreditsWithoutPayments"));
                    cardTransactionBean.setStartEodID(StartEODID);
                    cardTransactionBean.setEndEodID(EndEODID);
                }
                return cardTransactionBean;
            }, Configurations.TXN_TYPE_SALE, Configurations.TXN_TYPE_MVISA_ORIGINATOR, Configurations.TXN_TYPE_INSTALLMENT, Configurations.TXN_TYPE_FEE_INSTALLMENT, Configurations.TXN_TYPE_DEBIT_PAYMENT, StartEODID, EndEODID, cardNo.toString(), Configurations.NO_STATUS, cardNo.toString(), Configurations.TXN_TYPE_PAYMENT, Configurations.TXN_TYPE_REVERSAL, Configurations.TXN_TYPE_REFUND, Configurations.TXN_TYPE_MVISA_REFUND, Configurations.TXN_TYPE_REVERSAL_INSTALLMENT, StartEODID, EndEODID, cardNo.toString(), Configurations.NO_STATUS, cardNo.toString(), statusList.getEOD_DONE_STATUS(), Configurations.TXN_TYPE_CASH_ADVANCE, StartEODID, EndEODID, cardNo.toString(), Configurations.NO_STATUS, cardNo.toString(), statusList.getEOD_DONE_STATUS(), Configurations.CREDIT, Configurations.LOYALTY_ADJUSTMENT_TYPE, Configurations.CASHBACK_ADJUSTMENT_TYPE, cardNo.toString(), statusList.getEOD_DONE_STATUS(), Configurations.DEBIT, Configurations.LOYALTY_ADJUSTMENT_TYPE, Configurations.CASHBACK_ADJUSTMENT_TYPE, Configurations.TXN_TYPE_REVERSAL, Configurations.TXN_TYPE_REVERSAL_INSTALLMENT, StartEODID, EndEODID, cardNo.toString(), Configurations.NO_STATUS);

        } catch (Exception e) {
            throw e;
        }
        return cardTransactionBean;
    }

    @Override
    public int updateStatementIDByAccNoInEODTxn(String statementId, int startEodId, int endEodId, String accountNo) throws Exception {

        int count = 0;

        try {
            //String query = "update EODTRANSACTION set STATEMENTID=? where eodid<=? and eodid > ? and ACCOUNTNO = ?";
            count = backendJdbcTemplate.update(queryParametersList.getMonthlyStatement_updateStatementIDByAccNoInEODTxn(), statementId, endEodId, startEodId, accountNo);

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public StatementBean getLastStatementDetails(StatementBean stBean) throws Exception {

        String query = null;
        if (stBean.getCardCategory().equals(Configurations.CARD_CATEGORY_SUPPLEMENTORY) || stBean.getCardCategory().equals(Configurations.CARD_CATEGORY_CORPORATE) || stBean.getCardCategory().equals(Configurations.CARD_CATEGORY_FD_SUPPLEMENTORY) || stBean.getCardCategory().equals(Configurations.CARD_CATEGORY_AFFINITY_SUPPLEMENTORY) || stBean.getCardCategory().equals(Configurations.CARD_CATEGORY_CO_BRANDED_SUPPLEMENTORY)) {
            return stBean;
        }
        //query = "SELECT BL.*,CA.CREDITLIMIT,BS.STARTEODID,BS.ENDEODID FROM BILLINGLASTSTATEMENTSUMMARY BL,BILLINGSTATEMENT BS,CARD CA WHERE BL.CARDNO=? AND CA.CARDNUMBER=BL.CARDNO AND BL.statementid=BS.statementid";
        query = queryParametersList.getMonthlyStatement_getLastStatementDetails_Select1();

        try {
            boolean isFirstStatement = false;
            isFirstStatement = Objects.requireNonNull(backendJdbcTemplate.query(query, (ResultSet rs) -> {
                boolean temp = false;
                while (rs.next()) {
                    temp = (!rs.isBeforeFirst());
                }
                return temp;
            }, stBean.getCardNo().toString()));
            stBean.setFirstStatement(isFirstStatement);

            if (isFirstStatement) {
                stBean.setLastBillclosingBalance(0.0);
                stBean.setOpenBalance(0.0);
                stBean.setStartEodID(0);
                stBean.setEndEodID(Configurations.EOD_ID);
                double ThisBillClsingBal = (stBean.getTotalPurchases() + stBean.getInterest() + stBean.getFee() + stBean.getCashAdvance() + stBean.getTotalDrAdj() - stBean.getPaymentAndCredit());
                stBean.setClosingBalance(ThisBillClsingBal);
                stBean.setAvailablCereditLimit(stBean.getCreditLimit() - ThisBillClsingBal);
                stBean.setAvailableCashLimit(stBean.getCashLimit() - stBean.getCashAdvance());
                double totalMinPaymentOverdue = 0;
                stBean.setTotalMinPaymentDue(totalMinPaymentOverdue);
                double MinPayment = calculateMinPayment(stBean.getAccountNo(), ThisBillClsingBal, stBean.getAvailablCereditLimit(), totalMinPaymentOverdue, stBean.getCreditLimit());
                stBean.setTotalMinPayment(MinPayment);
                stBean.setStatementEndDate(stBean.getOldNextBillingDate());
                stBean.setStatementStartDate(stBean.getCardCreatedDate());
                String cardCat = stBean.getCardCategory();

                if (cardCat.equals(Configurations.CARD_CATEGORY_MAIN) || cardCat.equals(Configurations.CARD_CATEGORY_ESTABLISHMENT) || cardCat.equals(Configurations.CARD_CATEGORY_FD) || cardCat.equals(Configurations.CARD_CATEGORY_AFFINITY) || cardCat.equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                    boolean isInsertedBillingLastStatementSummry = insertBillingLastStatementSummry(stBean);
                    stBean.setInsertedBillingLastStatementSummry(isInsertedBillingLastStatementSummry);
                }

            } else {

               //query = "SELECT BL.*,CA.CREDITLIMIT,BS.STARTEODID,BS.ENDEODID,(select COUNT from MINIMUMPAYMENT where cardno = BL.CARDNO) as MINPAYMENTCOUNT FROM BILLINGLASTSTATEMENTSUMMARY BL,BILLINGSTATEMENT BS,CARD CA WHERE BL.CARDNO=? AND CA.CARDNUMBER=BL.CARDNO AND BL.statementid=BS.statementid";
                query = queryParametersList.getMonthlyStatement_getLastStatementDetails_Select2();

                backendJdbcTemplate.query(query, (ResultSet rs) -> {
                    try {
                        while (rs.next()) {
                            stBean.setStartEodID(rs.getInt("ENDEODID"));
                            stBean.setEndEodID(Configurations.EOD_ID);
                            boolean flag = true;
                            while (flag) {
                                double totalMinpaymentDue = 0;
                                totalMinpaymentDue = rs.getDouble("MINAMOUNT") - (stBean.getPaymentAndCredit() - stBean.getTotalCreditsWithoutPayments() - checkChequeReturns(stBean));
                                totalMinpaymentDue = totalMinpaymentDue <= 0 ? 0.00 : totalMinpaymentDue;
                                stBean.setTotalMinPaymentDue(totalMinpaymentDue);
                                double LastBillClosingBal = rs.getDouble("CLOSINGBALANCE");
                                stBean.setLastBillclosingBalance(LastBillClosingBal);
                                double OpeningBal = LastBillClosingBal;
                                stBean.setOpenBalance(LastBillClosingBal);
                                double ThisBillClsingBal = OpeningBal + (stBean.getTotalPurchases() + stBean.getFee() + stBean.getInterest() + stBean.getCashAdvance() + stBean.getTotalDrAdj()) - (stBean.getPaymentAndCredit());
                                stBean.setClosingBalance(ThisBillClsingBal);
                                stBean.setAvailablCereditLimit(stBean.getCreditLimit() - ThisBillClsingBal);
                                stBean.setAvailableCashLimit(stBean.getCashLimit() - stBean.getCashAdvance());
                                int minPaymentDueCount = rs.getInt("MINPAYMENTCOUNT");
                                double MinPayment = 0;
                                MinPayment = calculateMinPayment(stBean.getAccountNo(), ThisBillClsingBal, stBean.getAvailablCereditLimit(), totalMinpaymentDue, stBean.getCreditLimit());
                                stBean.setTotalMinPayment(MinPayment);
                                stBean.setStatementEndDate(stBean.getOldNextBillingDate());
                                stBean.setStatementStartDate(rs.getDate("STATEMENTENDDATE"));
                                flag = false;
                                String cardCat = stBean.getCardCategory();
                                if (cardCat.equals(Configurations.CARD_CATEGORY_MAIN) || cardCat.equals(Configurations.CARD_CATEGORY_ESTABLISHMENT) || cardCat.equals(Configurations.CARD_CATEGORY_FD) || cardCat.equals(Configurations.CARD_CATEGORY_AFFINITY) || cardCat.equals(Configurations.CARD_CATEGORY_CO_BRANDED)) {
                                    boolean isUpdatedBillingLastStatementSummry = false;
                                    isUpdatedBillingLastStatementSummry = updateBillingLastStatementSummry(stBean);
                                    stBean.setUpdatedBillingLastStatementSummry(isUpdatedBillingLastStatementSummry);
                                }
                            }
                            return stBean;
                        }
                        return stBean;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, stBean.getCardNo().toString());
            }
            stBean.setLastOperationSucessful(true);

        } catch (Exception e) {
            throw e;
        }
        return stBean;
    }

    @Override
    public boolean insertBillingStatement(StatementBean stBean) throws Exception {

        Map<String, Object> details = new LinkedHashMap<String, Object>();
        boolean flag = true;
        double totalStampDuty = getTotalStampDuty(stBean.getAccountNo());
        //String query = "INSERT INTO BILLINGSTATEMENT (STATEMENTID,CARDNO,MAINCARDNO,CREDITLIMIT,CASHLIMIT,AVCREDITLIMIT,AVCASHLIMIT,STATEMENTSTARTDATE,STATEMENTENDDATE,MINPAYMENTDUE,TOTALMINPAYMENT,DUEDATE,LASTBILLCLOSINGBALANCE,CHEQUERETURNAMOUNT,THISBILLOPERNINGBALANCE,PURCHASES,CASHADVANCE,INTEREST,CHARGES,PAYMENT,THISBILLCLOSINGBALANCE,OPENINGLOYALTYPOINT,EARNLOYALTYPOINT,AVLOYALTYPOINT,BILLINGCYCLEID,STATUS,LASTUPDATEUSERID,TIMESTAMP,INTERESTRATE,CASHADVANCEINTERESTRATE,STARTEODID,ENDEODID,STARTPAYID,ENDPAYID,ADJUSTLOYALTYPOINT,REDEEMLOYALTYPOINT,CLOSINGLOYALTYPOINT,NORMALINTERESTAMOUNT,CASHADVANCEINTERESTAMOUNT,TOTALMAILORDERAMOUNT,STAMPDUITY,PASTDUEAMOUNT,ACCOUNTNO,STATEMENTGENERATEDSTATUS,CARDCATEGORYCODE,CRADJUSTMENT,DRADJUSTMENT,BUCKETID,NOOFDAYSINAREERS) Values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            ArrayList<String> Bucketdetails = getBucketIdAndNODIA(stBean.getAccountNo());
            backendJdbcTemplate.update(queryParametersList.getMonthlyStatement_insertBillingStatement(), stBean.getStatementID(), stBean.getCardNo().toString(), stBean.getMainCardNo().toString(), stBean.getCreditLimit(), stBean.getCashLimit(), stBean.getAvailablCereditLimit(), stBean.getAvailableCashLimit(), stBean.getStatementStartDate(), stBean.getStatementEndDate(), stBean.getTotalMinPaymentDue(), stBean.getTotalMinPayment(), stBean.getStatementDueDate(), stBean.getLastBillclosingBalance(), 0, stBean.getOpenBalance(), stBean.getTotalPurchases(), stBean.getCashAdvance(), stBean.getInterest(), stBean.getFee(), stBean.getPaymentAndCredit(), stBean.getClosingBalance(), 0, 0, 0, stBean.getBillingID(), "0", 0, DateUtil.getSqldate(Configurations.EOD_DATE), 0, 0, stBean.getStartEodID(), stBean.getEndEodID(), 0, 0, 0, 0, 0, 0, 0, 0, totalStampDuty, 0, stBean.getAccountNo(), 0, stBean.getCardCategory(), stBean.getTotalCrAdj(), stBean.getTotalDrAdj(), Bucketdetails.isEmpty() ? "0" : Bucketdetails.get(0), Bucketdetails.isEmpty() ? "0" : Bucketdetails.get(1));

            details.put("Card No", CommonMethods.cardNumberMask(stBean.getCardNo()));
            details.put("Card Type", stBean.getCardCategory());
            details.put("Openeing Balance", stBean.getOpenBalance());
            details.put("Closing Balance", stBean.getClosingBalance());
            details.put("Available Credit", stBean.getAvailablCereditLimit() + "/" + stBean.getCreditLimit());
            details.put("Available Cash", stBean.getAvailableCashLimit() + "/" + stBean.getCashLimit());
            details.put("Total Fee", Double.toString(stBean.getFee()));
            details.put("Total Cash Advance", Double.toString(stBean.getFee()));
            details.put("Total Payments", Double.toString(stBean.getPaymentAndCredit()));
            details.put("Total Sales", Double.toString(stBean.getTotalPurchases()));
            details.put("Due Date", stBean.getStatementDueDate().toString());

            logInfo.info(logManager.logDetails(details));
        } catch (Exception e) {
            throw e;
        }
        return flag;
    }

    @Override
    public double getTotalStampDuty(String accNo) throws Exception {

        //String sql = null;
        double stampDuty = 0.00;

        try {
            //sql = "select NVL(sum(FEEAMOUNT),0.00)as stampDuty from EODCARDFEE where ACCOUNTNO = ? and STATUS = ? and feetype = ?";
            stampDuty = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_getTotalStampDuty(), (ResultSet rs) -> {
                double temp = 0.00;
                while (rs.next()) {
                    temp = rs.getDouble("stampDuty");
                }
                return temp;
            }, accNo, Configurations.EOD_DONE_STATUS, Configurations.STAMP_DUTY_FEE));

        } catch (Exception e) {
            throw e;
        }
        return stampDuty;
    }

    @Override
    public ArrayList<String> getBucketIdAndNODIA(String accNo) throws Exception {

        //String sql = null;
        ArrayList<String> details = new ArrayList<String>();

        try {
            //sql = "select RISKCLASS,NDIA from DELINQUENTACCOUNT where ACCOUNTNO = ?";
            backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_getBucketIdAndNODIA(), (ResultSet rs) -> {
                while (rs.next()) {
                    details.add(0, rs.getString("RISKCLASS"));
                    details.add(1, rs.getString("NDIA"));
                }
                return details;
            }, accNo);

        } catch (Exception e) {
            throw e;
        }
        return details;
    }

    @Override
    public void updateNextBillingDate(StatementBean stBean) throws Exception {

        //String sql = null;
        try {
            if (!stBean.getHasBillingCycleChangeRequest()) {
                //sql = "update CARDACCOUNT set NEXTBILLINGDATE = ? where ACCOUNTNO= ?";
                backendJdbcTemplate.update(queryParametersList.getMonthlyStatement_getBucketIdAndNODIA_Update1(), stBean.getNewNextBillingDate(), stBean.getAccountNo());
            } else if (stBean.getHasBillingCycleChangeRequest()) {
                String billingDate = new SimpleDateFormat("dd").format(stBean.getNewNextBillingDate());
                //sql = "update CARDACCOUNT set NEXTBILLINGDATE = ?,BILLINGDATE = ?  where ACCOUNTNO= ?";
                backendJdbcTemplate.update(queryParametersList.getMonthlyStatement_getBucketIdAndNODIA_Update2(), stBean.getNewNextBillingDate(), billingDate, stBean.getAccountNo());
            }

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public double calculateMinPayment(String AccNo, double ClosingBal, double availableCreadit, double minPaymentOverDue, double creditLimit) throws Exception {

        double MinPayment = 0.0;
        CalculateMinPaymentBean calculateMinPaymentBean = new CalculateMinPaymentBean();
        //String sql_MinAmt = "SELECT MINIMUMDUEFLATAMOUNT,MINIMUMDUEPERCENTAGE,MINIMUMDUECOMBINATION ,CCP.PERMENANTBLKTHRESHOLD from BILLINGSTATEMENTPROFILE BSP,CARDACCOUNT CA,COMMONCARDPARAMETER CCP WHERE CA.ACCOUNTNO= ? AND CA.BILLINGSTATEMENTPROFILECODE = BSP.PROFILECODE";
        try {

            calculateMinPaymentBean = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_calculateMinPayment(), (ResultSet result_temp) -> {
                CalculateMinPaymentBean tempCalculateMinPaymentBean = new CalculateMinPaymentBean();
                while (result_temp.next()) {
                    tempCalculateMinPaymentBean.setFlatAmount(result_temp.getDouble("MINIMUMDUEFLATAMOUNT"));
                    tempCalculateMinPaymentBean.setPrecentage(result_temp.getDouble("MINIMUMDUEPERCENTAGE"));
                    tempCalculateMinPaymentBean.setMinOrMax(result_temp.getString("MINIMUMDUECOMBINATION"));
                    tempCalculateMinPaymentBean.setPermenantBlockPeriod(result_temp.getInt("PERMENANTBLKTHRESHOLD"));
                }
                return tempCalculateMinPaymentBean;
            }, AccNo));

            int minPaymentDueCount = checkMinPaymentDueCount(AccNo);
            double tempMinPayment;
            tempMinPayment = (((ClosingBal - minPaymentOverDue) * calculateMinPaymentBean.getPrecentage()) / 100) + minPaymentOverDue;
            tempMinPayment = tempMinPayment <= 0 ? 0 : tempMinPayment;
            if (calculateMinPaymentBean.getMinOrMax().equals("MAX") && ClosingBal != 0) {
                if (calculateMinPaymentBean.getFlatAmount() >= tempMinPayment) {
                    MinPayment = calculateMinPaymentBean.getFlatAmount();
                } else {
                    MinPayment = (tempMinPayment);
                }
            } else if (calculateMinPaymentBean.getMinOrMax().equals("MIN") && ClosingBal != 0) {
                if (calculateMinPaymentBean.getFlatAmount() <= tempMinPayment) {
                    MinPayment = calculateMinPaymentBean.getFlatAmount();
                } else {
                    MinPayment = tempMinPayment;
                }
            } else if (calculateMinPaymentBean.getMinOrMax().equals("COM")) {
                MinPayment = (tempMinPayment) + calculateMinPaymentBean.getFlatAmount();
            }
            if (ClosingBal == 0) {
                MinPayment = tempMinPayment;
            }
            if (availableCreadit < 0) {
                MinPayment = (((creditLimit - minPaymentOverDue) * calculateMinPaymentBean.getPrecentage()) / 100) + (-1 * availableCreadit) + minPaymentOverDue;
            }
            if (MinPayment >= ClosingBal) {
                MinPayment = ClosingBal;
            }
            if (MinPayment < 0) {
                MinPayment = 0;
            }

        } catch (Exception e) {
            throw e;
        }
        return MinPayment;
    }

    @Override
    public int checkMinPaymentDueCount(String accNo) throws Exception {

        int minPaymentDueCount = 0;
        try {

            //String query = "SELECT CA.CARDNUMBER,MP.COUNT FROM CARDACCOUNT CA,MINIMUMPAYMENT MP WHERE CA.ACCOUNTNO = ?  and MP.CARDNO =CA.CARDNUMBER";
            minPaymentDueCount = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_checkMinPaymentDueCount(), (ResultSet rs) -> {
                int temp = 0;
                while (rs.next()) {
                    temp = rs.getInt("COUNT");
                }
                return temp;
            }, accNo));

        } catch (Exception e) {
            throw e;
        }
        return minPaymentDueCount;
    }

    @Override
    public boolean insertBillingLastStatementSummry(StatementBean stBean) throws Exception {

        boolean flag = true;
        try {
            //String query = "INSERT INTO BILLINGLASTSTATEMENTSUMMARY (CARDNO,OPENINGBALANCE,CLOSINGBALANCE,MINAMOUNT,PAYMENT, DUEDATE,STATEMENTSTARTDATE,STATEMENTENDDATE,CLOSINGLOYALTYPOINT,STATEMENTID) VALUES (?,?,?,?,?,?,?,?,?,?)";
            backendJdbcTemplate.update(queryParametersList.getMonthlyStatement_insertBillingLastStatementSummry(), stBean.getCardNo().toString(), stBean.getOpenBalance(), stBean.getClosingBalance(), stBean.getTotalMinPayment(), stBean.getPaymentAndCredit(), stBean.getStatementDueDate(), stBean.getStatementStartDate(), stBean.getStatementEndDate(), "", stBean.getStatementID());

        } catch (Exception e) {
            throw e;
        }
        return flag;
    }

    @Override
    public boolean updateBillingLastStatementSummry(StatementBean StBean) throws Exception {

        boolean flag = true;
        try {
            //String query = "UPDATE BILLINGLASTSTATEMENTSUMMARY SET OPENINGBALANCE=?,CLOSINGBALANCE=?,MINAMOUNT=?,PAYMENT=?,DUEDATE=?,STATEMENTSTARTDATE=?,STATEMENTENDDATE=?,CLOSINGLOYALTYPOINT=?,STATEMENTID=? WHERE CARDNO = ?";

            backendJdbcTemplate.update(queryParametersList.getMonthlyStatement_updateBillingLastStatementSummry(), StBean.getOpenBalance(), StBean.getClosingBalance(), StBean.getTotalMinPayment(), StBean.getPaymentAndCredit(), StBean.getStatementDueDate(), StBean.getStatementStartDate(), StBean.getStatementEndDate(), "", StBean.getStatementID(), StBean.getCardNo().toString());
        } catch (Exception e) {
            throw e;
        }
        return flag;
    }

    @Override
    public double checkChequeReturns(StatementBean stBean) throws Exception {

        boolean flag = true;
        HashMap<String, Double> amountMap = new HashMap<String, Double>();
        double chequeReturnAmount = 0.00;
        try {
            //String query = "select TRANSACTIONAMOUNT,TRANSACTIONID from eodtransaction where eodid >? AND eodid <= ? AND status = ? and transactiontype = ? and accountno = ?";
            backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_checkChequeReturns_Select1(), (ResultSet rs) -> {
                while (rs.next()) {
                    amountMap.put(rs.getString("TRANSACTIONID"), rs.getDouble("TRANSACTIONAMOUNT"));
                }
                return amountMap;
            }, stBean.getStartEodID(), stBean.getEndEodID(), statusList.getCHEQUE_RETURN_STATUS(), Configurations.TXN_TYPE_DEBIT_PAYMENT, stBean.getAccountNo());

            //query = "select TRANSACTIONAMOUNT,TRANSACTIONID from eodtransaction where eodid >? AND eodid <= ? AND status = ? and transactiontype = ? and accountno = ?";
            chequeReturnAmount = Objects.requireNonNull(backendJdbcTemplate.query(queryParametersList.getMonthlyStatement_checkChequeReturns_Select2(), (ResultSet rs) -> {

                double tempChequeReturnAmount = 0.00;
                while (rs.next()) {
                    String txnId = rs.getString("TRANSACTIONID");
                    if (amountMap.containsKey(txnId)) {
                        tempChequeReturnAmount = tempChequeReturnAmount + amountMap.get(txnId);
                    }
                }
                return tempChequeReturnAmount;
            }, stBean.getStartEodID(), stBean.getEndEodID(), statusList.getCHEQUE_RETURN_STATUS(), Configurations.TXN_TYPE_PAYMENT, stBean.getAccountNo()));

        } catch (Exception e) {
            throw e;
        }
        return chequeReturnAmount;
    }

    public java.util.Date getHolidayFalseDueDate(Date DueDate) throws Exception {
        boolean holiday = isHoliday(DueDate);
        java.util.Date nextDate = DueDate;
        int x = 1;
        while (holiday) {
            nextDate = CommonMethods.getNextDateForFreq(DueDate, x);
            if (isHoliday(nextDate)) {
                x = x + 1;
            } else {
                holiday = false;
            }
        }
        return nextDate;
    }
}
