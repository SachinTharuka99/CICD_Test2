package com.epic.cms.repository;

import com.epic.cms.dao.ChequePaymentDao;
import com.epic.cms.model.bean.*;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.CreateEodId;
import com.epic.cms.util.StatusVarList;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.epic.cms.util.LogManager.errorLogger;

@Repository
public class ChequePaymentRepo implements ChequePaymentDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Autowired
    CommonRepo commonRepo;

    @Autowired
    LastStatementSummaryRepo lastStatementSummaryRepo;

    @Autowired
    ChequeProcessRepo chequeProcessRepo;

    @Override
    public List<ReturnChequePaymentDetailBean> getChequePaymentsBackup() throws Exception {
        String checkChequeReturnsQuery = "";
        List<ReturnChequePaymentDetailBean> chqBeanList = new ArrayList<>();
        CommonMethods.resetFailedCardList();

        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");

            checkChequeReturnsQuery = "SELECT CARDNUMBER,EODID,TRANSACTIONAMOUNT,CHEQUENUMBER,TRANSACTIONDATE,TRACEID,SEQUENCENUMBER"
                    + " FROM PAYMENT WHERE TRANSACTIONTYPE=? and STATUS=? ";

            backendJdbcTemplate.query(checkChequeReturnsQuery,
                    (ResultSet result) -> {

                        String accountNo = null;
                        ReturnChequePaymentDetailBean bean = null;
                        LastStatementSummeryBean lastStatement = null;

                        while (result.next()) {
                            StringBuffer cardNo = null;
                            EODCardTransactionDetail detailTxnBean = null;
                            EodInterestBean intBean = null;

                            try {
                                bean = new ReturnChequePaymentDetailBean();
                                cardNo = new StringBuffer(result.getString("CARDNUMBER"));
                                bean.setOldcardnumber(cardNo);//set the old card number before replace
                                cardNo = commonRepo.getNewCardNumber(cardNo);
                                CardBean cardBean = commonRepo.getCardDetails(cardNo);
                                accountNo = commonRepo.getAccountNoOnCard(cardNo);
                                bean.setCardnumber(cardNo);
                                bean.setEodid(result.getInt("EODID"));
                                bean.setAmount(result.getDouble("TRANSACTIONAMOUNT"));
                                bean.setChequenumber(result.getString("CHEQUENUMBER"));
                                bean.setChequedate(result.getDate("TRANSACTIONDATE"));
                                bean.setTraceid(result.getString("TRACEID"));
                                bean.setSeqNo(result.getString("SEQUENCENUMBER"));
                                bean.setOtbcash(cardBean.getOtbCash());
                                bean.setOtbcredit(cardBean.getOtbCredit());
                                bean.setCardstatus(cardBean.getCardStatus());
                                bean.setMaincardno(cardBean.getMainCardNo());
                                bean.setDuedate(cardBean.getDueDate());

                                /**
                                 TODO:
                                 Check if the billing last statement table is empty
                                 Check if the billing statement table has more than one entry.
                                 */

                                lastStatement = lastStatementSummaryRepo.getLastStatementSummaryInfo(cardNo);
                                CreateEodId convertToEOD = new CreateEodId();

                                if (lastStatement != null) {
                                    int count = getStatementCount(cardNo);

                                    if (count >= 1) {
                                        /**
                                         * There are more than one statements.
                                         */
                                        //TODOs add a month to this to get the next billing date.
                                        /**Date nextStatementDay = lastStatement.getStatementEndDate();
                                         Calendar calendar = Calendar.getInstance();
                                         calendar.setTime(nextStatementDay);

                                         int day = calendar.get(Calendar.DAY_OF_MONTH);*/
                                        DateTime nextStatementDay = new DateTime(lastStatement.getStatementEndDate().getTime());
                                        int day = nextStatementDay.getDayOfMonth();
                                        nextStatementDay = nextStatementDay.plusMonths(1).withDayOfMonth(day);
                                        java.util.Date statementEndDate = nextStatementDay.toDate();
                                        int statementEndEODID = Integer.parseInt(convertToEOD.getDate(statementEndDate) + "00");
                                        bean.setStatementendeodid(statementEndEODID);
                                        int statementStartEODID = Integer.parseInt(convertToEOD.getDate(lastStatement.getStatementStartDate()) + "00");
                                        bean.setStatementstarteodid(statementStartEODID);
                                        bean.setClosingbalance(lastStatement.getClosingBalance());
                                        bean.setMinamount(lastStatement.getMinAmount());
                                        detailTxnBean = chequeProcessRepo.getEODTotalTransactionDetailsForCard(cardNo, statementStartEODID, Configurations.EOD_ID); //TODOs transactions from which date?
                                        if (detailTxnBean != null) {
                                            //Get the net total for the day
                                            double totalNetBalance = detailTxnBean.getTotalSales() + detailTxnBean.getTotalFees() + detailTxnBean.getTotalCashAdvances() - detailTxnBean.getTotalPayments();
                                            bean.setTotalNetBalanceForCard(totalNetBalance);
                                        }
                                    }
                                } else {
                                    /* There are no statements*/
                                    String statementStartEndEODId = convertToEOD.getDate(cardBean.getNextBillingDate()) + "00";
                                    bean.setStatementendeodid(Integer.parseInt(statementStartEndEODId));
                                    statementStartEndEODId = convertToEOD.getDate(cardBean.getCreatedDate()) + "00";
                                    bean.setStatementstarteodid(Integer.parseInt(statementStartEndEODId));
                                    bean.setDuedate(chequeProcessRepo.calculateDueDate(accountNo));
                                }
                                intBean = chequeProcessRepo.getEodInterestForCard(cardNo);
                                //set results to bean
                                if (intBean != null) {
                                    bean.setForwardinterest(intBean.getActualInterest());
                                }
                                bean.setInterestrate(cardBean.getInterestrate());
                                /**
                                 * Setting the delinquent class and NDIA
                                 */
                                String[] delinqStatus = getDelinquencyStatus(cardNo);
                                String delinqClass = delinqStatus[0];

                                if (delinqStatus != null) {
                                    int ndia = 0;
                                    if (delinqStatus[1] != null) {
                                        ndia = Integer.parseInt(delinqStatus[1]);
                                    }

                                    bean.setDelinquentclass(delinqClass);
                                    bean.setNdia(ndia);
                                }
                                chqBeanList.add(bean);

                            } catch (Exception e) {
                                errorLogger.error("Exception ", e);
                            }
                        }
                        return chqBeanList;
                    }
                    , statusList.getCHEQUE_INITIATE_STATUS()
                    , statusList.getEOD_PENDING_STATUS()
            );
        } catch (Exception e) {
            throw e;
        }
        return chqBeanList;
    }

    public String[] getDelinquencyStatus(StringBuffer cardNo) {
        String[] delinqStatus = new String[2];
        try {
            String sql = "SELECT RISKCLASS, NDIA FROM DELINQUENTACCOUNT WHERE CARDNUMBER = ? ";

            backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            delinqStatus[0] = rs.getString("RISKCLASS");
                            delinqStatus[1] = rs.getInt("NDIA") + "";
                        }
                        return delinqStatus;

                    }, cardNo.toString());

        } catch (Exception e) {
            //LogFileCreator.writeErrorToLog(e);
            throw e;
        }
        return delinqStatus;
    }

    public int getStatementCount(StringBuffer cardNo) {
        int count = 0;
        try {
            String sql = "select count(b.cardno) as count from billingstatement b "
                    + "where b.cardno=? "
                    + "group by b.cardno";//b.cardno

            count = backendJdbcTemplate.queryForObject(sql, Integer.class, cardNo.toString());

        } catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertChequePayments(ReturnChequePaymentDetailBean bean) {
        int count = 0;
        String sql = "INSERT INTO CHEQUEPAYMENT (EODID, CARDNUMBER,MAINCARDNO, AMOUNT, CHEQUEDATE, MINAMOUNT, FORWARDINTEREST, INTERESTRATE, CHEQUESTATUS, DELINQUENTCLASS, CARDSTATUS, DUEDATE, STATEMENTSTARTEODID, STATEMENTENDEODID, CHEQUERETURNDATE, RETURNREASON, LASTUPDATEDUSER, LASTUPDATEDDATE,CREATEDTIME, SEQUENCENUMBER, NDIA)VALUES( ?,?,?,?,?,?,?,?/*inerstrate*/,?,?/*delclass*/,?,?,?,?,?,?,?,SYSDATE,SYSDATE,?,?)";
        try {
            count = backendJdbcTemplate.update(sql, bean.getEodid()
                    , bean.getCardnumber().toString()
                    , bean.getMaincardno().toString()
                    , bean.getAmount()
                    , bean.getChequedate()
                    , bean.getMinamount()
                    , bean.getForwardinterest()
                    , bean.getInterestrate()
                    , statusList.getINITIAL_STATUS()
                    , bean.getDelinquentclass()
                    , bean.getCardstatus()
                    , bean.getDuedate()
                    , bean.getStatementstarteodid()
                    , bean.getStatementendeodid()
                    , bean.getChequeReturnDate()
                    , "other"
                    , Configurations.EOD_USER
                    , bean.getSeqNo()
                    , bean.getNdia());

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    @Transactional(value = "backendDb", propagation = Propagation.NESTED, isolation = Isolation.SERIALIZABLE)
    public int updateChequePayment(ReturnChequePaymentDetailBean payBean) throws Exception {
        int count = 0;
        try {
            String sql = "UPDATE PAYMENT SET STATUS=? WHERE CARDNUMBER=? AND STATUS=? AND TRACEID=? AND SEQUENCENUMBER=?";

            count = backendJdbcTemplate.update(sql, statusList.getEOD_DONE_STATUS()
                    , payBean.getOldcardnumber().toString()
                    , statusList.getEOD_PENDING_STATUS()
                    , payBean.getTraceid()
                    , payBean.getSeqNo()
            );
        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
