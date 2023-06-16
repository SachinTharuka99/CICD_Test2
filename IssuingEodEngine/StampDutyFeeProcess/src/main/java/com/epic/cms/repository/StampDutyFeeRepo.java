package com.epic.cms.repository;

import com.epic.cms.dao.StampDutyFeeDao;
import com.epic.cms.model.bean.CardFeeBean;
import com.epic.cms.model.bean.StampDutyBean;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


@Repository
public class StampDutyFeeRepo implements StampDutyFeeDao {

    @Autowired
    QueryParametersList query;

    @Autowired
    StatusVarList statusVarList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;


    @Override
    public ArrayList<StampDutyBean> getInitStatementAccountList() throws Exception {
        ArrayList<StampDutyBean> statementCardList = new ArrayList<StampDutyBean>();

        try {
            String query = "SELECT CA.ACCOUNTNO FROM CARDACCOUNT CA  WHERE CA.NEXTBILLINGDATE = ? AND  CA.ACCOUNTNO not in  (select ec.ACCOUNTNO from eoderrorcards ec where ec.status='" + statusVarList.getEOD_PENDING_STATUS() + "')";

            statementCardList = (ArrayList<StampDutyBean>) backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        StampDutyBean bean = new StampDutyBean();
                        bean.setAccountNumber(result.getString("ACCOUNTNO"));
                        return bean;
                    }),
                    DateUtil.getSqldate(Configurations.EOD_DATE)
            );

        } catch (Exception e) {
            throw e;
        }
        return statementCardList;
    }

    @Override
    public ArrayList<StampDutyBean> getErrorStatementAccountList() throws Exception {
        ArrayList<StampDutyBean> statementCardList = new ArrayList<StampDutyBean>();

        try {
            String query = "SELECT CA.ACCOUNTNO FROM CARDACCOUNT CA INNER JOIN EODERRORCARDS EEC ON EEC.ACCOUNTNO = CA.ACCOUNTNO WHERE CA.NEXTBILLINGDATE = ? AND EEC.STATUS = ? AND EEC.EODID < ?  AND EEC.PROCESSSTEPID <= ? ";

            statementCardList = (ArrayList<StampDutyBean>) backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        StampDutyBean bean = new StampDutyBean();
                        bean.setAccountNumber(result.getString("ACCOUNTNO"));
                        return bean;
                    }),
                    DateUtil.getSqldate(Configurations.EOD_DATE),
                    Configurations.EOD_PENDING_STATUS,
                    Configurations.ERROR_EOD_ID,
                    Configurations.PROCESS_STEP_ID
            );

        } catch (Exception e) {
            throw e;
        }
        return statementCardList;
    }

    @Override
    public ArrayList<StringBuffer> getOldCardNumbers(StringBuffer newcardnumber) throws Exception {
        ArrayList<StringBuffer> cardList = new ArrayList<StringBuffer>();
        cardList.add(newcardnumber);

        try {
            String query = "SELECT CR1.OLDCARDNUMBER CARDNUMBER FROM CARDREPLACE CR1 LEFT JOIN CARDREPLACE CR2 ON CR2.OLDCARDNUMBER = CR1.NEWCARDNUMBER START WITH CR1.NEWCARDNUMBER = ? CONNECT BY PRIOR CR1.OLDCARDNUMBER = CR1.NEWCARDNUMBER";

            cardList = backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        ArrayList<StringBuffer> temp = new ArrayList<StringBuffer>();
                        while (rs.next()) {
                            temp.add(new StringBuffer(rs.getString("CARDNUMBER")));
                        }
                        return temp;
                    },
                    newcardnumber.toString()
            );

        } catch (Exception e) {
            throw e;
        }
        return cardList;
    }

    @Override
    public double getTotalForeignTxns(String inClauseString, int startEodID) throws Exception {
        double totalForeignTxnsCr = 0;
        double totalForeignTxnsDr = 0;
        double totalForeignTxns = 0;

        try {
            String query = "SELECT NVL(SUM(ET.TRANSACTIONAMOUNT),0) AS TOTAL FROM EODTRANSACTION ET INNER JOIN CARDACCOUNT CA ON CA.ACCOUNTNO = ET.ACCOUNTNO INNER JOIN INTERESTPROFILETRANSACTION IPT ON CA.INTERESTPROFILECODE = IPT.INTERESTPROFILE AND IPT.TRANSACTIONCODE =  ET.TRANSACTIONTYPE WHERE ET.EODID > ? AND ET.EODID <= ? AND ET.CRDR='DR' AND COUNTRYNUMCODE !=? AND ET.CARDNUMBER IN ( ? )"; //AND ET.CARDNUMBER IN ( ? )

             totalForeignTxnsDr = backendJdbcTemplate.queryForObject(query,Double.class,startEodID, Configurations.EOD_ID, Configurations.COUNTRY_CODE_SRILANKA ,inClauseString);

            String query1 = "SELECT NVL(SUM(ET.TRANSACTIONAMOUNT),0) AS TOTAL FROM EODTRANSACTION ET INNER JOIN CARDACCOUNT CA ON CA.ACCOUNTNO = ET.ACCOUNTNO INNER JOIN INTERESTPROFILETRANSACTION IPT ON CA.INTERESTPROFILECODE = IPT.INTERESTPROFILE AND IPT.TRANSACTIONCODE =  ET.TRANSACTIONTYPE WHERE ET.EODID > ? AND ET.EODID <= ? AND ET.CRDR='CR' AND COUNTRYNUMCODE !=? AND ET.CARDNUMBER IN ( ? )"; //AND ET.CARDNUMBER IN ( ? )

             totalForeignTxnsCr = backendJdbcTemplate.queryForObject(query1,Double.class,startEodID, Configurations.EOD_ID, Configurations.COUNTRY_CODE_SRILANKA,inClauseString);

            totalForeignTxns = totalForeignTxnsDr - totalForeignTxnsCr;

        } catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return totalForeignTxns;
    }

    @Override
    public void insertToEODcardFee(CardFeeBean cardFeeBean, double amount, Date effectDate, String txnType) throws Exception {
        boolean forward = false;
        String query = null;

        try {
            forward = this.checkFeeExixtForCard(cardFeeBean.getCardNumber(), cardFeeBean.getFeeCode());

            if (forward) {
                if (txnType == null) {
                    query = "INSERT INTO EODCARDFEE(EODID,CARDNUMBER,ACCOUNTNO,CRDR,FEEAMOUNT,CURRENCYTYPE,EFFECTDATE,FEETYPE,LASTUPDATEDUSER,STATUS,TXNAMOUNT) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                } else {
                    query = "INSERT INTO EODCARDFEE(EODID,CARDNUMBER,ACCOUNTNO,CRDR,FEEAMOUNT,CURRENCYTYPE,EFFECTDATE,FEETYPE,LASTUPDATEDUSER,STATUS,TXNAMOUNT,ADJUSTMENTSTATUS) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

                    backendJdbcTemplate.update(query, txnType);
                }
                backendJdbcTemplate.update(query,
                        Configurations.EOD_ID,
                        cardFeeBean.getCardNumber().toString(),
                        cardFeeBean.getAccNumber(),
                        cardFeeBean.getCrOrDr(),
                        amount,
                        cardFeeBean.getCurrCode(),
                        effectDate,
                        cardFeeBean.getFeeCode(),
                        Configurations.EOD_USER,
                        Configurations.EOD_PENDING_STATUS,
                        cardFeeBean.getCashAmount()
                );
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public int getStartEodId(String accountNo) throws Exception {
        int startEodId = 0;

        try {
            String query = "SELECT BS.ENDEODID FROM BILLINGSTATEMENT BS INNER JOIN BILLINGLASTSTATEMENTSUMMARY BSS ON BSS.STATEMENTID = BS.STATEMENTID WHERE BS.ACCOUNTNO =? ";

            startEodId = backendJdbcTemplate.queryForObject(query, Integer.class, accountNo);

        } catch (Exception e) {
            throw e;
        }
        return startEodId;
    }

    @Override
    public ArrayList<StampDutyBean> getStatementCardList(String accountNumber) throws Exception {
        ArrayList<StampDutyBean> statementCardList = new ArrayList<StampDutyBean>();

        try {
            String query = "SELECT C.CARDNUMBER, FPF.PERSENTAGE, FPF.CURRENCYCODE FROM CARDACCOUNTCUSTOMER CAC INNER JOIN CARD C ON C.CARDNUMBER = CAC.CARDNUMBER INNER JOIN FEEPROFILEFEE FPF ON FPF.FEEPROFILECODE = C.FEEPROFILECODE INNER JOIN FEE F ON F.FEECODE = FPF.FEECODE WHERE FPF.FEECODE = ? AND CAC.ACCOUNTNO = ? AND C.CARDSTATUS NOT IN (?,?) ";

            statementCardList = (ArrayList<StampDutyBean>) backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        StampDutyBean bean = new StampDutyBean();
                        bean.setCardNumber(new StringBuffer(result.getString("CARDNUMBER")));
                        bean.setPersentage(result.getDouble("PERSENTAGE"));
                        bean.setCurrencycode(result.getInt("CURRENCYCODE"));
                        return bean;
                    }),
                    Configurations.STAMP_DUTY_FEE,
                    accountNumber,
                    statusVarList.getCARD_REPLACED_STATUS(), //CARP
                    statusVarList.getCARD_PRODUCT_CHANGE_STATUS() //CAPC
            );
        } catch (Exception e) {
            throw e;
        }
        return statementCardList;
    }

    @Override
    public boolean checkFeeExixtForCard(StringBuffer cardNumber, String feeCode) throws Exception {
        boolean forward = false;

        try {
            String query = "SELECT C.CARDNUMBER, C.FEEPROFILECODE, FPF.FEECODE FROM CARD C INNER JOIN FEEPROFILEFEE FPF ON C.FEEPROFILECODE = FPF.FEEPROFILECODE WHERE C.CARDNUMBER = ? AND FPF.FEECODE NOT IN (SELECT PFPF.FEECODE FROM CARD C INNER JOIN PROMOFEEPROFILE PFP ON C.PROMOFEEPROFILECODE = PFP.PROMOFEEPROFILECODE INNER JOIN PROMOFEEPROFILEFEE PFPF ON C.PROMOFEEPROFILECODE = PFPF.PROMOFEEPROFILECODE   WHERE C.CARDNUMBER = ? AND STATUS <> ? ) AND FPF.FEECODE = ?";

            forward = Objects.requireNonNull(backendJdbcTemplate.query(query,
                    (ResultSet rs) -> {
                        boolean temp = false;
                        while (rs.next()) {
                            temp = true;
                        }
                        return temp;

                    },
                    cardNumber.toString(),
                    cardNumber.toString(),
                    statusVarList.getFEE_PROMOTION_PROFILE_EXPIRE(),
                    feeCode
            ));
        } catch (Exception e) {
            throw e;
        }
        return forward;
    }
}
