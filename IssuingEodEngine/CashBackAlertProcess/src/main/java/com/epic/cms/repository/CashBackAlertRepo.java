package com.epic.cms.repository;

import com.epic.cms.dao.CashBackAlertDao;
import com.epic.cms.model.bean.CashBackAlertBean;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import static com.epic.cms.util.LogManager.errorLogger;

@Repository
public class CashBackAlertRepo implements CashBackAlertDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Autowired
    LogManager logManager;

    @Override
    public HashMap<String, ArrayList<CashBackAlertBean>> getConfirmedAccountToAlert() throws Exception {
        HashMap<String, ArrayList<CashBackAlertBean>> confirmAccountList = new HashMap<>();

        try {
            String sql = "SELECT BS.CARDNO, BS.ACCOUNTNO, BS.STATEMENTID, CB.ID AS REQID, CB.ACCOUNTNUMBER, CB.CASHBACKAMOUNT, BLS.MINAMOUNT FROM BILLINGSTATEMENT BS INNER JOIN BILLINGLASTSTATEMENTSUMMARY BLS ON BS.STATEMENTID = BLS.STATEMENTID LEFT JOIN CASHBACK CB ON BS.ACCOUNTNO = CB.ACCOUNTNUMBER AND BS.ENDEODID = EODID WHERE BS.NOTIFICATIONFLAG = ? ";

            backendJdbcTemplate.query(sql, (ResultSet result) -> {
                while (result.next()) {
                    CashBackAlertBean bean = new CashBackAlertBean();
                    bean.setReqId(result.getInt("REQID"));
                    bean.setAccNo(result.getString("ACCOUNTNO"));
                    bean.setStatementId(result.getString("STATEMENTID"));
                    bean.setCashBackAmount(result.getDouble("CASHBACKAMOUNT"));
                    bean.setMainCardNo(new StringBuffer(result.getString("CARDNO")));

                    if (result.getString("ACCOUNTNUMBER") == null) {
                        bean.setCBNull(true);
                    }
                    if (result.getDouble("MINAMOUNT") > 0) {
                        bean.setMinPayAvl(true);
                    }

                    if (confirmAccountList.containsKey(bean.getAccNo())) {
                        ArrayList<CashBackAlertBean> TempCardAccountList = confirmAccountList.get(bean.getAccNo());
                        TempCardAccountList.add(bean);
                        confirmAccountList.put(bean.getAccNo(), TempCardAccountList);
                    } else {
                        ArrayList<CashBackAlertBean> accountBeanList = new ArrayList<CashBackAlertBean>();
                        accountBeanList.add(bean);
                        confirmAccountList.put(bean.getAccNo(), accountBeanList);
                    }
                }
                return confirmAccountList;
            }, 0);
        } catch (Exception e) {
            logManager.logError("Exception ", errorLogger);
            throw e;
        }
        return confirmAccountList;
    }

    @Override
    public void updateCashBackAlertGenStatus(int reqId) throws Exception {
        try {
            String updatePay = "UPDATE CASHBACK CB SET CB.NOTIFICATIONFLAG = ? WHERE CB.ID = ? ";

            backendJdbcTemplate.update(updatePay, 1, reqId);
        } catch (Exception e) {
            logManager.logError("Exception ", errorLogger);
            throw e;
        }
    }

    @Override
    public void updateBillingStatementAlertGenStatus(String statementId) throws Exception {
        try {
            String updatePay = "UPDATE BILLINGSTATEMENT BS SET BS.NOTIFICATIONFLAG = ? WHERE BS.STATEMENTID = ? ";

            backendJdbcTemplate.update(updatePay, 1, statementId);
        }catch (Exception e){
            logManager.logError("Exception ", errorLogger);
            throw e;
        }
    }
}
