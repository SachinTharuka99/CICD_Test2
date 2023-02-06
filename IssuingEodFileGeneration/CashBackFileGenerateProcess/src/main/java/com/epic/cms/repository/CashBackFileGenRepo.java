/**
 * Author : lahiru_p
 * Date : 11/29/2022
 * Time : 11:37 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.CashBackFileGenDao;
import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.ArrayList;


@Repository
public class CashBackFileGenRepo implements CashBackFileGenDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private StatusVarList statusVarList;

    @Autowired
    LogManager logManager;

    @Override
    public ArrayList<GlAccountBean> getCahsBackRedeemList() throws Exception {
        ArrayList<GlAccountBean> list = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT CBR.ID,"
                    + " CBR.ACCOUNTNUMBER AS ACCOUNTNUMBER,"
                    + " CA.CARDNUMBER,"
                    + " CBR.AMOUNT,"
                    + " CA.CBDEBITACCOUNTNO,NVL(B.STATEMENTENDDATE,SYSDATE) AS STATEMENTENDDATE"
                    + " FROM CASHBACKEXPREDEEM CBR"
                    + " INNER JOIN CARDACCOUNT CA"
                    + " ON CA.ACCOUNTNO      =CBR.ACCOUNTNUMBER"
                    + " INNER JOIN BILLINGLASTSTATEMENTSUMMARY B ON CA.CARDNUMBER=B.CARDNO"
                    + " WHERE CBR.FILESTATUS<>1"
                    + " AND CBR.GLSTATUS     =1"
                    + " AND CBR.STATUS       =0 ";

            list = (ArrayList<GlAccountBean>) backendJdbcTemplate.query(sql, new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                GlAccountBean bean = new GlAccountBean();
                bean.setId(rs.getInt("ID"));
                bean.setCardNo(new StringBuffer(rs.getString("ACCOUNTNUMBER")));
                bean.setAccNo(rs.getString("CBDEBITACCOUNTNO"));
                bean.setAmount(rs.getDouble("AMOUNT"));
                bean.setGlAmount(rs.getString("AMOUNT"));
                bean.setGlDate(rs.getDate("STATEMENTENDDATE").toString());
                bean.setCrDr(Configurations.CREDIT);
                bean.setGlType(Configurations.TXN_TYPE_CASH_BACK);
                return bean;
            }));
        } catch (Exception e) {
            throw e;
        }
        return list;
    }

    @Override
    public String getCashBackDebitAccount() throws Exception {
        String debitAccountNumber = null;
        try {
            String sql = "SELECT GLT.TRANSACTIONCODE,"
                    + "(CASE WHEN GL.CRDR=? THEN GLT.DEBITACCOUNT ELSE GLT.CREDITACCOUNT END) AS ACCOUNTNO"
                    + " FROM GLTRANSACTION GLT INNER JOIN GLTXNTYPE GL "
                    + " ON GL.GLTXNTYPECODE=GLT.TRANSACTIONCODE"
                    + " WHERE TRANSACTIONCODE=?";

            debitAccountNumber = backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                String accountNumber = null;
                while (rs.next()) {
                    accountNumber = rs.getString("ACCOUNTNO");
                }
                return accountNumber;
            }, Configurations.CASH_BACK_FILE_CRDR, Configurations.TXN_TYPE_CASHBACK_REDEEMED);

        } catch (Exception e) {
            throw e;
        }
        return debitAccountNumber;
    }

    @Override
    public int updateCashBackRedeemExp(int key) throws Exception {
        int count = 0;
        try {
            String query = "UPDATE CASHBACKEXPREDEEM SET FILESTATUS = 1 WHERE ID = ? ";
            count = backendJdbcTemplate.update(query, key);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
