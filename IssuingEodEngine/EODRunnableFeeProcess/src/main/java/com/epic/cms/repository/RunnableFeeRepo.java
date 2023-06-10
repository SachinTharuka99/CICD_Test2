package com.epic.cms.repository;

import com.epic.cms.dao.RunnableFeeDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.model.rowmapper.CashAdvanceRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RunnableFeeRepo implements RunnableFeeDao {
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    LogManager logManager;

    @Override
    public List<CardBean> getAllActiveCards() throws Exception {
        List<CardBean> cardList = new ArrayList<>();
        try {
            String query = "SELECT C.CARDNUMBER,C.MAINCARDNO,C.CARDSTATUS,C.CREDITLIMIT,C.CASHLIMIT,C.OTBCREDIT,C.OTBCASH,C.PRIORITYLEVEL,TO_DATE(C.NEXTANNIVERSARYDATE,'dd/MON/YY') AS NEXTANNIVERSARYDATE,C.ACTIVATIONDATE,C.CARDCATEGORYCODE,CA.STATUS AS ACCOUNTSTATUS FROM CARD C INNER JOIN CARDACCOUNT CA ON C.MAINCARDNO = CA.CARDNUMBER WHERE C.CARDSTATUS NOT IN (?,?) AND ROWNUM <= 501";
            backendJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        CardBean cardBean = null;
                        while (result.next()) {
                            try {
                                cardBean = new CardBean();
                                cardBean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                                cardBean.setMainCardNo(new StringBuffer(result.getString("MAINCARDNO")));
                                cardBean.setCardStatus(result.getString("CARDSTATUS"));
                                cardBean.setAccStatus(result.getString("ACCOUNTSTATUS"));
                                cardBean.setCreditLimit(result.getDouble("CREDITLIMIT"));
                                cardBean.setCashLimit(result.getDouble("CASHLIMIT"));
                                cardBean.setOtbCredit(result.getDouble("OTBCREDIT"));
                                cardBean.setOtbCash(result.getDouble("OTBCASH"));
                                cardBean.setPriorityLevel(result.getString("PRIORITYLEVEL"));
                                cardBean.setNextAnniversaryDate(result.getDate("NEXTANNIVERSARYDATE"));
                                cardBean.setActivateDate(result.getDate("ACTIVATIONDATE"));
                                cardBean.setCardCategory(result.getString("CARDCATEGORYCODE"));
                                cardList.add(cardBean);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    },
                    "CACL", "CAPC");
        } catch (Exception e) {
            throw e;
        }
        System.out.println("--cardList size--" + cardList.size());
        return cardList;
    }

    @Override
    public List<CashAdvanceBean> findCashAdvances(StringBuffer cardNo) throws Exception {
        List<CashAdvanceBean> cashAdBeans = new ArrayList<>();
        try {
            String query = "SELECT CARDNUMBER,ACCOUNTNO,TRANSACTIONID,TRANSACTIONAMOUNT,SETTLEMENTDATE,TRANSACTIONDATE FROM EODTRANSACTION WHERE CARDNUMBER = ? AND TRANSACTIONTYPE = ? AND STATUS IN(?,?) AND EODID = ?";
            cashAdBeans = backendJdbcTemplate.query(query, new CashAdvanceRowMapper(), cardNo.toString(), "TTC020", status.getINITIAL_STATUS(), status.getEOD_PENDING_STATUS(), Configurations.EOD_ID);
        } catch (Exception e) {
            throw e;
        }
        return cashAdBeans;
    }

    @Override
    public boolean updateNextAnniversaryDate(StringBuffer cardNumber) throws Exception {
        boolean updated = false;
        try {
            String query = "UPDATE CARD SET NEXTANNIVERSARYDATE = ADD_MONTHS(NEXTANNIVERSARYDATE, 12)  WHERE CARDNUMBER =? AND NEXTANNIVERSARYDATE IS NOT NULL";
            int result = backendJdbcTemplate.update(query, cardNumber.toString().trim());
            if (result == 1) {
                updated = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return updated;
    }

    @Override
    public boolean checkFeeExistForCard(StringBuffer cardNumber, String feeCode) throws Exception {
        boolean forward = false;
        int recordCount = 0;
        try {
            String query = "SELECT COUNT(*) AS RECORDCOUNT FROM CARD C INNER JOIN FEEPROFILEFEE FPF ON C.FEEPROFILECODE = FPF.FEEPROFILECODE WHERE C.CARDNUMBER = ? AND FPF.FEECODE NOT IN (SELECT PFPF.FEECODE FROM CARD C INNER JOIN PROMOFEEPROFILE PFP ON C.PROMOFEEPROFILECODE = PFP.PROMOFEEPROFILECODE INNER JOIN PROMOFEEPROFILEFEE PFPF ON C.PROMOFEEPROFILECODE = PFPF.PROMOFEEPROFILECODE WHERE C.CARDNUMBER = ? AND STATUS <> ?) AND FPF.FEECODE = ?";
            recordCount = backendJdbcTemplate.queryForObject(query, Integer.class, cardNumber.toString(), cardNumber.toString(), status.getFEE_PROMOTION_PROFILE_EXPIRE(), feeCode);
            if (recordCount > 0) {
                forward = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return forward;
    }

    @Override
    public int addCardFeeCount(StringBuffer cardNumber, String feeCode, double cashAmount) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        int count = 0;
        boolean forward = false;
        String query = null;
        try {
            forward = this.checkFeeExistForCard(cardNumber, feeCode);
            if (forward) {
                boolean isFeeUpdateRequired = this.getFeeCode(cardNumber, feeCode);
                if (isFeeUpdateRequired) {
                    query = "UPDATE CARDFEECOUNT SET FEECOUNT = FEECOUNT + 1,CASHAMOUNT=CASHAMOUNT+?, LASTUPDATEDUSER= ?, LASTUPDATEDTIME= SYSDATE, STATUS =? WHERE CARDNUMBER = ? AND FEECODE = ?";
                    count = backendJdbcTemplate.update(query, cashAmount, Configurations.EOD_USER, status.getEOD_PENDING_STATUS(), cardNumber.toString(), feeCode);
                } else {
                    query = "INSERT INTO CARDFEECOUNT (CARDNUMBER,FEECODE,FEECOUNT,CASHAMOUNT,STATUS,CREATEDDATE,LASTUPDATEDTIME,LASTUPDATEDUSER) VALUES (?,?,?,?,?,TO_DATE(?,'DD-MM-YY'),SYSDATE,?)";
                    count = backendJdbcTemplate.update(query, cardNumber.toString(), feeCode, 1, cashAmount, status.getEOD_PENDING_STATUS(), sdf.format(Configurations.EOD_DATE), Configurations.EOD_USER);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public Boolean getFeeCode(StringBuffer cardNumber, String feeCode) throws Exception {
        boolean forward = false;
        int feeCount = 0;
        try {
            String query = "SELECT C.FEECOUNT FROM CARDFEECOUNT C WHERE C.CARDNUMBER = ? AND C.FEECODE = ?";
            feeCount = backendJdbcTemplate.queryForObject(query, Integer.class, cardNumber.toString(), feeCode);
            if (feeCount > 0 && (!feeCode.equals(Configurations.LATE_PAYMENT_FEE) && !feeCode.equals(Configurations.ANNUAL_FEE))) {
                forward = true;
            }
        } catch (EmptyResultDataAccessException e) {
            logManager.logError("--fees not found--" + cardNumber,errorLogger);
        } catch (Exception e) {
            throw e;
        }
        return forward;
    }

    @Override
    public LastStmtSummeryBean getLastStatementSummaryInfor(StringBuffer cardNo) throws Exception {
        LastStmtSummeryBean lastStmtSummeryBean = null;
        try {
            String query = "SELECT BSS.STATEMENTID,BSS.OPENINGBALANCE,BSS.CLOSINGBALANCE,BSS.MINAMOUNT,BSS.DUEDATE,BSS.STATEMENTSTARTDATE,BSS.STATEMENTENDDATE,BSS.CLOSINGLOYALTYPOINT,bs.NOOFDAYSINAREERS FROM BILLINGLASTSTATEMENTSUMMARY BSS LEFT JOIN BILLINGSTATEMENT BS ON BS.STATEMENTID = BSS.STATEMENTID WHERE BSS.CARDNO = ? ";
            lastStmtSummeryBean = backendJdbcTemplate.queryForObject(query, new RowMapper<>() {
                        @Override
                        public LastStmtSummeryBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            LastStmtSummeryBean bean = new LastStmtSummeryBean();
                            bean.setOpaningBalance(rs.getDouble("OPENINGBALANCE"));
                            bean.setClosingBalance(rs.getDouble("CLOSINGBALANCE"));
                            bean.setMinAmount(rs.getDouble("MINAMOUNT"));
                            bean.setDueDate(rs.getDate("DUEDATE"));
                            bean.setStatementStartDate(rs.getDate("STATEMENTSTARTDATE"));
                            bean.setStatementEndDate(rs.getDate("STATEMENTENDDATE"));
                            bean.setClosingLoyaltyPoint(rs.getLong("CLOSINGLOYALTYPOINT"));
                            bean.setNDIA(rs.getInt("NOOFDAYSINAREERS"));
                            return bean;
                        }
                    },
                    new Object[]{cardNo.toString()});

            return lastStmtSummeryBean;
        } catch (EmptyResultDataAccessException e) {
            return lastStmtSummeryBean;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Date getNextBillingDateForCard(StringBuffer cardNo) throws Exception {
        java.sql.Date nextBillingDate = null;
        try {
            String query = "SELECT NEXTBILLINGDATE FROM CARDACCOUNT WHERE CARDNUMBER = (SELECT MAINCARDNO FROM CARD WHERE CARDNUMBER =?)";
            nextBillingDate = backendJdbcTemplate.queryForObject(query, java.sql.Date.class, cardNo.toString());
        } catch (EmptyResultDataAccessException e) {
            logManager.logError("--next billing date not found--",errorLogger);
        } catch (Exception e) {
            throw e;
        }
        return nextBillingDate;
    }

    @Override
    public CardFeeBean getCardFeeProfileForCard(StringBuffer cardNumber, String feeCode) throws Exception {
        CardFeeBean cardFeeBean = null;
        try {
            String query = "SELECT CD.FEEPROFILECODE,FF.CURRENCYCODE,FF.CRORDR,FF.FLATFEE,FF.MINIMUMAMOUNT,FF.MAXIMUMAMOUNT,FF.PERSENTAGE, FF.COMBINATION FROM CARD CD,FEEPROFILEFEE FF WHERE  CD.FEEPROFILECODE = FF.FEEPROFILECODE AND CD.CARDNUMBER=? AND FF.FEECODE=? AND CD.CARDSTATUS <> ?";
            cardFeeBean = backendJdbcTemplate.queryForObject(query, new RowMapper<>() {
                        @Override
                        public CardFeeBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            CardFeeBean cardFeeBean = new CardFeeBean();
                            cardFeeBean.setCurrCode(rs.getInt("CURRENCYCODE"));
                            cardFeeBean.setFlatFee(rs.getDouble("FLATFEE"));
                            cardFeeBean.setMinAmount(rs.getDouble("MINIMUMAMOUNT"));
                            cardFeeBean.setMaxAmount(rs.getDouble("MAXIMUMAMOUNT"));
                            cardFeeBean.setCrOrDr(rs.getString("CRORDR"));
                            cardFeeBean.setPercentageAmount(rs.getDouble("PERSENTAGE"));
                            cardFeeBean.setCombination(rs.getString("COMBINATION"));
                            cardFeeBean.setFeeCode(feeCode);
                            cardFeeBean.setCardNumber(cardNumber);
                            return cardFeeBean;
                        }
                    },
                    new Object[]{cardNumber.toString(), feeCode, "CACL"}
            );
        } catch (EmptyResultDataAccessException e) {
            logManager.logError("--fee profile not found--",errorLogger);
        } catch (Exception e) {
            throw e;
        }
        return cardFeeBean;
    }

    @Override
    public void insertToEODcardFee(CardFeeBean cardFeeBean, double amount, Date effectDate) throws Exception {
        boolean forward = false;
        try {
            forward = this.checkFeeExistForCard(cardFeeBean.getCardNumber(), cardFeeBean.getFeeCode());
            if (forward) {
                String query = "INSERT INTO EODCARDFEE(EODID,CARDNUMBER,ACCOUNTNO,CRDR,FEEAMOUNT,CURRENCYTYPE,EFFECTDATE,FEETYPE,LASTUPDATEDUSER,STATUS,TRANSACTIONID ) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                backendJdbcTemplate.update(query, Configurations.EOD_ID, cardFeeBean.getCardNumber().toString(), cardFeeBean.getAccNumber(), "CR", amount, cardFeeBean.getCurrCode(), effectDate, cardFeeBean.getFeeCode(), Configurations.EOD_USER, status.getEOD_PENDING_STATUS(), cardFeeBean.getTxnId());
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void updateCardFeeCount(CardFeeBean cardFeeBean) throws Exception {
        boolean forward = false;
        try {
            forward = true;
            if (forward) {
                String query = "UPDATE CARDFEECOUNT SET FEECOUNT = 0, CASHAMOUNT=0, STATUS=? WHERE CARDNUMBER = ? AND FEECODE = ?";
                backendJdbcTemplate.update(query, status.getEOD_DONE_STATUS(), cardFeeBean.getCardNumber().toString(), cardFeeBean.getFeeCode());
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean checkDuplicateCashAdvances(StringBuffer cardNo, String txnId, String feeCode) throws Exception {
        boolean flag = false;
        int count = 0;
        try {
            String query = "SELECT COUNT(*) AS RECORDCOUNT FROM EODCARDFEE WHERE CARDNUMBER=? AND TRANSACTIONID=? AND EODID=? AND FEETYPE=?";
            count = backendJdbcTemplate.queryForObject(query, Integer.class, cardNo.toString(), txnId, Configurations.EOD_ID, feeCode);
            if (count > 0) {
                flag = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return flag;
    }

    @Override
    public String getAccountNoOnCard(StringBuffer cardNo) throws Exception {
        String accNo = null;
        try {
            String query = "SELECT ACCOUNTNO FROM CARDACCOUNTCUSTOMER WHERE CARDNUMBER=?";
            accNo = backendJdbcTemplate.queryForObject(query, String.class, cardNo.toString());
        } catch (Exception e) {
            throw e;
        }
        return accNo;
    }

    @Override
    public double getTotalPayment(String accNo, int startEodId, int endEodId) throws Exception {
        double payment = 0;
        try {
            String query = "SELECT NVL(SUM(TRANSACTIONAMOUNT),0) AS TOTAL " +
                    " FROM EODTRANSACTION " +
                    " WHERE ACCOUNTNO      = ? " +
                    " AND TRANSACTIONTYPE IN (?) " +
                    " AND EODID            > ? " +
                    " AND EODID           <= ? " +
                    " AND STATUS NOT      IN (?) ";
            payment = backendJdbcTemplate.queryForObject(query, Double.class, accNo, Configurations.TXN_TYPE_PAYMENT, startEodId, endEodId, status.getCHEQUE_RETURN_STATUS());
        }catch (EmptyResultDataAccessException ex) {
            return payment;
        }catch (Exception e) {
            throw e;
        }
        return payment;
    }
}
