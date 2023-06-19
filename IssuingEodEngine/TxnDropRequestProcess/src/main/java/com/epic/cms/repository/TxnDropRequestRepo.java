package com.epic.cms.repository;

import com.epic.cms.dao.TxnDropRequestDao;
import com.epic.cms.model.bean.DropRequestBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class TxnDropRequestRepo implements TxnDropRequestDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    StatusVarList statusList;


    @Override
    public int getTransactionValidityPeriod() throws Exception {
        int txnValidPeriod = 0;
        try {
            String query = "SELECT TXNVALIDPERIOD FROM COMMONPARAMETER";

            txnValidPeriod = backendJdbcTemplate.queryForObject(query, Integer.class);


        } catch (EmptyResultDataAccessException e) {
            return 0;

        } catch (Exception e) {
            throw e;
        }
        return txnValidPeriod;
    }

    @Override
    public List<DropRequestBean> getDropTransactionList(int txnValidityPeriod) throws Exception {
        ArrayList<DropRequestBean> dropTransactionList = new ArrayList<>();
        try {
            String query = "SELECT T.TXNID,T.CARDNO FROM TRANSACTION T WHERE T.EODSTATUS=? AND TRIM(T.TXNDATE) IS NOT NULL AND (TRUNC(TO_DATE(?,'yymmdd'))-TRUNC(TO_DATE(CONCAT(T.TXNDATE,EXTRACT(YEAR FROM T.CREATETIME)),'mmddyy')))>=? AND T.RESPONSECODE='00' AND T.STATUS NOT IN(?,?,?,?) AND T.TXNID NOT IN(SELECT DISTINCT TVR.TXNID FROM TXNVOIDREQUEST TVR) AND T.BACKENDTXNTYPE IN(?,?,?,?) AND ACQORISS=? AND EODCONSIDERSTATUS=? ";

            query += CommonMethods.checkForErrorCards("T.CARDNO");

            dropTransactionList = (ArrayList<DropRequestBean>) backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        DropRequestBean dropTxnBean = new DropRequestBean();
                        dropTxnBean.setTxnId(rs.getString("TXNID"));
                        dropTxnBean.setCardNumber(new StringBuffer(rs.getString("CARDNO")));
                        return dropTxnBean;
                    })
                    , Configurations.EOD_PENDING_STATUS
                    , Integer.toString(Configurations.EOD_ID).substring(0, 6)
                    , txnValidityPeriod
                    , Configurations.ONLINE_REVERSE_STATUS
                    , Configurations.ONLINE_DROP_STATUS
                    , Configurations.ONLINE_PARTIALLY_REVERSE_STATUS
                    , Configurations.ONLINE_TXN_INCOMPLETE_STATUS
                    , Configurations.TXN_TYPE_SALE
                    , Configurations.TXN_TYPE_CASH_ADVANCE
                    , Configurations.TXN_TYPE_ADJUSTMENT_CREDIT
                    , Configurations.TXN_TYPE_QUASI_CASH
                    , Configurations.EOD_ISSUING_STATUS
                    , Configurations.EOD_CONSIDER_STATUS
            );
        } catch (Exception e) {
            throw e;
        }
        return dropTransactionList;
    }

    @Override
    @Qualifier("onlineDb")
    public boolean getTransactionReverseStatus(String txnId) throws Exception {
        boolean isReversed = false;
        try {
            String query = "SELECT STATUS FROM ECMS_ONLINE_TRANSACTION WHERE TXNID=? ";

            isReversed = Objects.requireNonNull(onlineJdbcTemplate.query(query,
                    (ResultSet result) -> {
                        boolean tempIsReversed = false;
                        while (result.next()) {
                            if (result.getInt("STATUS") == Configurations.ONLINE_REVERSE_STATUS) {
                                tempIsReversed = true;
                            }
                        }
                        return tempIsReversed;
                        },
                    txnId
            ));

        } catch (EmptyResultDataAccessException e) {
            return false;

        } catch (Exception e) {
            throw e;
        }
        return isReversed;
    }

    @Override
    public void addTxnDropRequest(String txnId, StringBuffer cardNumber) throws Exception {
        try {
            String sql = "INSERT INTO TXNVOIDREQUEST (CARDNUMBER,TXNID,REMARKS,STATUS,LASTUPDATEDUSER,LASTUPDATEDTIME,CREATEDTIME,REQUESTEDUSER,APPROVEDTIME"
                    + ") VALUES(?,?,?,?,?,SYSDATE,SYSDATE,?,NULL)";

            backendJdbcTemplate.update(sql, cardNumber.toString(),
                    txnId,
                    "Requested from EOD",
                    statusList.getCOMMON_REQUEST_INITIATE(),
                    Configurations.EOD_USER,
                    Configurations.EOD_USER
                    );
        } catch (Exception e) {
            throw e;
        }
    }
}
