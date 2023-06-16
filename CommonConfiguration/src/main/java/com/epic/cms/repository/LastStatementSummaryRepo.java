package com.epic.cms.repository;

import com.epic.cms.dao.LastStatmentSummaryDao;
import com.epic.cms.model.bean.LastStatementSummeryBean;
import com.epic.cms.model.bean.StatementBean;
import com.epic.cms.model.rowmapper.LastStatementSummeryRowMapper;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Repository
public class LastStatementSummaryRepo implements LastStatmentSummaryDao {
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Override
    public List<LastStatementSummeryBean> getStatementCardList() throws Exception {
        List<LastStatementSummeryBean> cardList = new ArrayList<>();

        String query = "SELECT BS.CARDNO, CAC.ACCOUNTNO,bs.duedate,bs.minamount,bs.statementstartdate,bs.statementenddate,"
                + " bs.openingbalance,bs.closingbalance,bs.CLOSINGLOYALTYPOINT"
                + " FROM CARD C, BILLINGLASTSTATEMENTSUMMARY BS, CARDACCOUNTCUSTOMER CAC WHERE bs.cardno=c.cardnumber AND CAC.CARDNUMBER = c.cardnumber"
                + " and c.cardstatus not in (?,?,?)";

        query += CommonMethods.checkForErrorCards("c.cardnumber");

        try {
            cardList = backendJdbcTemplate.query(query, new LastStatementSummeryRowMapper(),
                    statusList.getCARD_CLOSED_STATUS(),
                    statusList.getCARD_REPLACED_STATUS(),
                    statusList.getCARD_PRODUCT_CHANGE_STATUS());

        } catch (Exception e) {
            throw e;
        }
        return cardList;
    }

    @Override
    public LastStatementSummeryBean getLastStatementSummaryInfo(StringBuffer cardNo) throws Exception {
        LastStatementSummeryBean lastStatementSummeryBean = null;
        String sql = "SELECT BSS.STATEMENTID,BSS.OPENINGBALANCE,BSS.CLOSINGBALANCE,BSS.MINAMOUNT,BSS.DUEDATE,BSS.STATEMENTSTARTDATE,BSS.STATEMENTENDDATE,BSS.CLOSINGLOYALTYPOINT,bs.NOOFDAYSINAREERS FROM BILLINGLASTSTATEMENTSUMMARY BSS LEFT JOIN BILLINGSTATEMENT BS ON BS.STATEMENTID = BSS.STATEMENTID WHERE BSS.CARDNO = ? ";

        try {
            lastStatementSummeryBean = backendJdbcTemplate.queryForObject(sql,
                    new RowMapper<>() {
                        @Override
                        public LastStatementSummeryBean mapRow(ResultSet rs, int rowNum) throws SQLException {
                            LastStatementSummeryBean bean = new LastStatementSummeryBean();

                            bean.setOpaningBalance(rs.getDouble("OPENINGBALANCE"));
                            bean.setClosingBalance(rs.getDouble("CLOSINGBALANCE"));
                            bean.setMinAmount(rs.getDouble("MINAMOUNT"));
                            bean.setDueDate(rs.getDate("DUEDATE"));
                            bean.setStatementStartDate(rs.getDate("STATEMENTSTARTDATE"));
                            bean.setStatementEndDate(rs.getDate("STATEMENTENDDATE"));
                            bean.setClosingloyaltypoint(rs.getLong("CLOSINGLOYALTYPOINT"));
                            bean.setNDIA(rs.getInt("NOOFDAYSINAREERS"));

                            return bean;
                        }
                    },cardNo.toString());
        } catch (Exception e) {
            throw e;
        }
        return lastStatementSummeryBean;
    }

    @Override
    public StatementBean getLastStatementDetails(StringBuffer cardNumber) throws Exception {
        StatementBean statementBean = null;
        String  sql = "SELECT CARDNO, "
                + "  MINAMOUNT, "
                + "  STATEMENTENDDATE, "
                + "  STATEMENTSTARTDATE, "
                + "  DUEDATE "
                + "FROM BILLINGLASTSTATEMENTSUMMARY "
                + "WHERE CARDNO = ? ";
        try {
            statementBean = (StatementBean) backendJdbcTemplate.query(sql,
                    (rs, rowNum) -> {
                        StatementBean stmtBean = new StatementBean();

                        stmtBean.setCardNo(new StringBuffer(rs.getString("CARDNO")));
                        stmtBean.setTotalMinPayment(rs.getDouble("MINAMOUNT"));
                        stmtBean.setStatementStartDate(rs.getDate("STATEMENTSTARTDATE"));
                        stmtBean.setStatementEndDate(rs.getDate("STATEMENTENDDATE"));
                        stmtBean.setStatementDueDate(rs.getDate("DUEDATE"));

                        return stmtBean;
                    }, cardNumber.toString());
        } catch (Exception e) {
            throw e;
        }
        return statementBean;
    }
}
