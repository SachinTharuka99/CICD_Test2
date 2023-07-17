package com.epic.cms.repository;

import com.epic.cms.dao.TxnMismatchPostDao;
import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public class TxnMismatchPostRepo implements TxnMismatchPostDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList status;

    @Override
    public ArrayList<OtbBean> getInitEodTxnMismatchPostCustAcc() throws Exception {
        ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();

        String query = "SELECT DISTINCT CAC.CUSTOMERID, CAC.ACCOUNTNO FROM EODTRANSACTION ET INNER JOIN CARD C ON C.CARDNUMBER = ET.CARDNUMBER INNER JOIN CARDACCOUNTCUSTOMER CAC ON CAC.CARDNUMBER = C.MAINCARDNO LEFT OUTER JOIN TRANSACTION T ON T.TXNID = ET.TRANSACTIONID WHERE ET.STATUS = ? AND ET.EODID = ? AND ET.ONLYVISAFALSE NOT IN(?) AND ((ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?)) AND CAC.ACCOUNTNO not in (select ec.ACCOUNTNO from eoderrorcards ec where ec.status= ? ) GROUP BY CAC.CUSTOMERID, CAC.ACCOUNTNO HAVING SUM(NVL(ET.TRANSACTIONAMOUNT,0)-NVL((CASE WHEN T.TXNCURRENCY = ? THEN T.TXNAMOUNT ELSE T.BILLINGAMOUNT END)/100,0)) != 0 ORDER BY CAC.CUSTOMERID, CAC.ACCOUNTNO";

        try {
            custAccList = (ArrayList<OtbBean>) backendJdbcTemplate.query(query,//queryParametersList.getTxnMismatchPost_getInitEodTxnMismatchPostCustAcc(),
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        OtbBean bean = new OtbBean();
                        bean.setCustomerid(result.getString("CUSTOMERID"));
                        bean.setAccountnumber(result.getString("ACCOUNTNO"));
                        return bean;
                    }),
                    Configurations.EOD_DONE_STATUS, //1
                    Configurations.EOD_ID, //2
                    1,  //3
                    Configurations.TXN_TYPE_SALE, //4
                    Configurations.DEBIT, //5
                    Configurations.TXN_TYPE_CASH_ADVANCE, //6
                    Configurations.DEBIT, //7
                    Configurations.TXN_TYPE_PAYMENT, //8
                    Configurations.CREDIT, //9
                    Configurations.TXN_TYPE_REVERSAL, //10
                    Configurations.CREDIT, //11
                    Configurations.TXN_TYPE_REFUND, //12
                    Configurations.CREDIT, //13
                    Configurations.TXN_TYPE_MVISA_REFUND, //14
                    Configurations.CREDIT, //15
                    Configurations.TXN_TYPE_MVISA_ORIGINATOR, //16
                    Configurations.DEBIT, //17
                    Configurations.TXN_TYPE_MONEY_SEND, //18
                    Configurations.CREDIT, //19
                    Configurations.TXN_TYPE_MONEY_SEND_REVERSAL, //20
                    Configurations.DEBIT, //21
                    Configurations.TXN_TYPE_AFT, //22
                    Configurations.DEBIT,//23
                    status.getEOD_PENDING_STATUS(), //24
                    Configurations.BASE_CURRENCY //25
            );
        } catch (Exception e) {
            throw e;
        }
        return custAccList;
    }

    @Override
    public ArrayList<OtbBean> getErrorEodTxnMismatchPostCustAcc() throws Exception {
        ArrayList<OtbBean> custAccList = new ArrayList<OtbBean>();

        try {
            custAccList = (ArrayList<OtbBean>) backendJdbcTemplate.query(queryParametersList.getTxnMismatchPost_getErrorEodTxnMismatchPostCustAcc(),
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        OtbBean bean = new OtbBean();
                        bean.setCustomerid(result.getString("CUSTOMERID"));
                        bean.setAccountnumber(result.getString("ACCOUNTNO"));
                        return bean;
                    })
                    , Configurations.EOD_DONE_STATUS,
                    Configurations.EOD_ID,
                    1,
                    Configurations.PROCESS_STEP_ID,
                    Configurations.TXN_TYPE_SALE,
                    Configurations.DEBIT,
                    Configurations.TXN_TYPE_CASH_ADVANCE,
                    Configurations.DEBIT,
                    Configurations.TXN_TYPE_PAYMENT,
                    Configurations.CREDIT,
                    Configurations.TXN_TYPE_REVERSAL,
                    Configurations.CREDIT,
                    Configurations.TXN_TYPE_REFUND,
                    Configurations.CREDIT,
                    Configurations.TXN_TYPE_MVISA_REFUND,
                    Configurations.CREDIT,
                    Configurations.TXN_TYPE_MVISA_ORIGINATOR,
                    Configurations.DEBIT,
                    Configurations.TXN_TYPE_MONEY_SEND,
                    Configurations.CREDIT,
                    Configurations.TXN_TYPE_MONEY_SEND_REVERSAL,
                    Configurations.DEBIT,
                    Configurations.EOD_PENDING_STATUS,
                    Configurations.ERROR_EOD_ID,
                    Configurations.BASE_CURRENCY
            );
        } catch (Exception e) {
            throw e;
        }
        return custAccList;
    }

    @Override
    public ArrayList<OtbBean> getInitTxnMismatch(String accountNumber) {
        ArrayList<OtbBean> txnList = new ArrayList<OtbBean>();

        String query = "SELECT ET.CARDNUMBER, TT.TRANSACTIONCODE, TT.DESCRIPTION,ET.CRDR, (CASE WHEN ET.CRDR='CR' THEN -1*SUM(NVL(ET.TRANSACTIONAMOUNT,0)-NVL((CASE WHEN T.TXNCURRENCY = ? THEN T.TXNAMOUNT ELSE T.BILLINGAMOUNT END)/100,0))ELSE SUM(NVL(ET.TRANSACTIONAMOUNT,0)-NVL((CASE WHEN T.TXNCURRENCY = ? THEN T.TXNAMOUNT ELSE T.BILLINGAMOUNT END)/100,0)) END) AS TXNMISMATCHAMOUNT  FROM EODTRANSACTION ET LEFT JOIN TRANSACTION T ON T.TXNID = ET.TRANSACTIONID INNER JOIN TRANSACTIONTYPE TT ON TT.TRANSACTIONCODE = ET.TRANSACTIONTYPE WHERE ET.STATUS = ? AND ET.EODID = ? AND ET.ONLYVISAFALSE NOT IN(?)AND ((ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?) OR (ET.TRANSACTIONTYPE = ? AND ET.CRDR = ?)) AND ET.ACCOUNTNO = ? GROUP BY ET.CARDNUMBER, TT.TRANSACTIONCODE, TT.DESCRIPTION, ET.CRDR HAVING SUM(NVL(ET.TRANSACTIONAMOUNT,0)-NVL((CASE WHEN T.TXNCURRENCY = ? THEN T.TXNAMOUNT ELSE T.BILLINGAMOUNT END)/100,0)) != 0";

        try {
            txnList = (ArrayList<OtbBean>) backendJdbcTemplate.query(query,//queryParametersList.getTxnMismatchPost_getInitTxnMismatch(),
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        OtbBean bean = new OtbBean();
//                        bean.setCustomerid(result.getString("CUSTOMERID"));
//                        bean.setAccountnumber(result.getString("ACCOUNTNO"));

                        bean.setTxntype(result.getString("TRANSACTIONCODE"));
                        bean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                        bean.setTxntypedesc(result.getString("DESCRIPTION"));
                        bean.setTxnAmount(result.getDouble("TXNMISMATCHAMOUNT"));
                        return bean;
                    })
                    , Configurations.BASE_CURRENCY //1
                    , Configurations.BASE_CURRENCY //2
                    , Configurations.EOD_DONE_STATUS //3
                    , Configurations.EOD_ID //4
                    , 1 //5
                    , Configurations.TXN_TYPE_SALE //6
                    , Configurations.DEBIT //7
                    , Configurations.TXN_TYPE_CASH_ADVANCE //8
                    , Configurations.DEBIT //9
                    , Configurations.TXN_TYPE_PAYMENT //10
                    , Configurations.CREDIT //11
                    , Configurations.TXN_TYPE_REVERSAL //12
                    , Configurations.CREDIT //13
                    , Configurations.TXN_TYPE_REFUND //14
                    , Configurations.CREDIT //15
                    , Configurations.TXN_TYPE_MVISA_REFUND //16
                    , Configurations.CREDIT //17
                    , Configurations.TXN_TYPE_MVISA_ORIGINATOR //18
                    , Configurations.DEBIT //19
                    , Configurations.TXN_TYPE_MONEY_SEND //20
                    , Configurations.CREDIT //21
                    , Configurations.TXN_TYPE_MONEY_SEND_REVERSAL //22
                    , Configurations.DEBIT //23
                    , Configurations.TXN_TYPE_AFT //24
                    , Configurations.DEBIT //25
                    , accountNumber //26
                    , Configurations.BASE_CURRENCY //27
            );
        } catch (Exception e) {
            throw e;
        }
        return txnList;
    }
}
