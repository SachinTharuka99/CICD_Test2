package com.epic.cms.repository;

import com.epic.cms.dao.CollectionAndRecoveryDao;
import com.epic.cms.model.bean.CollectionAndRecoveryBean;
import com.epic.cms.model.rowmapper.CollectionAndRecoveryRowMapper;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.QueryParametersList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;


@Repository
public class CollectionAndRecoveryRepo implements CollectionAndRecoveryDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Override
    public int getNoOfDaysOnTriggerPoint(String triggerPoint) throws Exception {
        int count = 0;
        try {
            String sql = "SELECT NOOFDAYS FROM ALLOCATIONRULE  WHERE TRIGGERPOINT = ? ";

           Integer triggerCount = backendJdbcTemplate.queryForObject(sql, Integer.class, triggerPoint);

            count = (triggerCount != null) ? Integer.parseInt(triggerCount.toString()) : 0;


        }catch (EmptyResultDataAccessException e){
            return 0;
        }
        catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public ArrayList<CollectionAndRecoveryBean> getCardListForCollectionAndRecoveryOnDueDate(int X_DATES, int number, String lastTrigger) throws Exception {
        ArrayList<CollectionAndRecoveryBean> collectionAndRecoveryBeanList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        //String sql = "";
        try {
            if (number == 1) {
                //sql = "select CARDNO,DUEDATE,MINAMOUNT from BILLINGLASTSTATEMENTSUMMARY  where  TO_DATE(DUEDATE, 'DD-MM-YY') -?= TO_DATE(?, 'DD-MM-YY')  AND MINAMOUNT > 0";

                collectionAndRecoveryBeanList = (ArrayList<CollectionAndRecoveryBean>) backendJdbcTemplate.query(queryParametersList.getCollectionAndRecovery_getCardListForCollectionAndRecoveryOnDueDate_Select1(),
                        new CollectionAndRecoveryRowMapper(),X_DATES,sdf.format(Configurations.EOD_DATE));
            }else if(number == 2){
                //sql = "SELECT B.CARDNO,  B.DUEDATE,  B.MINAMOUNT FROM BILLINGLASTSTATEMENTSUMMARY B,  TRIGGERCARDS T WHERE T.LASTTRIGGERPOINT = ? AND TO_DATE(DUEDATE, 'DD-MM-YY') + ? = TO_DATE(?, 'DD-MM-YY') AND T.CARDNO =B.CARDNO";

                collectionAndRecoveryBeanList = (ArrayList<CollectionAndRecoveryBean>) backendJdbcTemplate.query(queryParametersList.getCollectionAndRecovery_getCardListForCollectionAndRecoveryOnDueDate_Select2(),
                        new CollectionAndRecoveryRowMapper(), lastTrigger, X_DATES,sdf.format(Configurations.EOD_DATE));
            }

        }catch (Exception e){
            throw e;
        }
        return collectionAndRecoveryBeanList;
    }

    @Override
    public ArrayList<CollectionAndRecoveryBean> getCardListForCollectionAndRecoveryOnStatmentDate(int X_DATES, int number, String lastTrigger) throws Exception {
        ArrayList<CollectionAndRecoveryBean> collectionAndRecoveryBeanList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
        //String sql = "";
        try {
            if (number == 1) {
                //sql ="SELECT B.CARDNO,  B.DUEDATE,  B.MINAMOUNT FROM CARDACCOUNT C,  BILLINGLASTSTATEMENTSUMMARY B,  TRIGGERCARDS T WHERE TO_DATE(C.NEXTBILLINGDATE, 'DD-MM-YY') = TO_DATE(?, 'DD-MM-YY') AND B.CARDNO = C.CARDNUMBER AND T.LASTTRIGGERPOINT = ? AND T.CARDNO = B.CARDNO";

                collectionAndRecoveryBeanList = (ArrayList<CollectionAndRecoveryBean>) backendJdbcTemplate.query(queryParametersList.getCollectionAndRecovery_getCardListForCollectionAndRecoveryOnStatmentDate_Select1(),
                        new CollectionAndRecoveryRowMapper(),sdf.format(Configurations.EOD_DATE), lastTrigger);
            }else if(number == 2){
                //sql ="SELECT B.CARDNO,  B.DUEDATE,  B.MINAMOUNT FROM  BILLINGLASTSTATEMENTSUMMARY B,  TRIGGERCARDS T WHERE TO_DATE(B.STATEMENTENDDATE, 'DD-MM-YY') + ? = TO_DATE(?, 'DD-MM-YY') AND T.CARDNO = B.CARDNO AND T.LASTTRIGGERPOINT = ?";

                collectionAndRecoveryBeanList = (ArrayList<CollectionAndRecoveryBean>) backendJdbcTemplate.query(queryParametersList.getCollectionAndRecovery_getCardListForCollectionAndRecoveryOnStatmentDate_Select2(),
                        new CollectionAndRecoveryRowMapper(), X_DATES,sdf.format(Configurations.EOD_DATE), lastTrigger);
            }

        }catch (Exception e){
            throw e;
        }
        return collectionAndRecoveryBeanList;
    }

    @Override
    public boolean CheckForTriggerPoint(StringBuffer cardNo) throws Exception {
        boolean result = false;
        //String sql = "Select count(*) from TRIGGERCARDS where CARDNO = ?";
        try{
            Integer triggerCount = backendJdbcTemplate.queryForObject(queryParametersList.getCollectionAndRecovery_CheckForTriggerPoint(), Integer.class , cardNo.toString());
            int count = (triggerCount != null) ? Integer.parseInt(triggerCount.toString()) : 0;
            if (count > 0) {
                result = true;
            }
        }catch (EmptyResultDataAccessException e){
            return  false;
        }
        catch (Exception e){
            throw e;
        }
        return result;
    }

    @Override
    public void addCardToTriggerCards(CollectionAndRecoveryBean collectionAndRecoveryBean) throws Exception {
       try {
           //String sql = "INSERT INTO TRIGGERCARDS (CARDNO,LASTTRIGGERPOINT,CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER,NOTIFICATIONFLAG,LETTERGENSTATUS) VALUES(?,?,SYSDATE,SYSDATE,?,0,?)";

           backendJdbcTemplate.update(queryParametersList.getCollectionAndRecovery_addCardToTriggerCards(),
                   collectionAndRecoveryBean.getCardNo().toString(),
                   collectionAndRecoveryBean.getLastTriger(),
                   Configurations.EOD_USER,
                   Configurations.NO_STATUS
                   );
       }catch (Exception e){
            throw e;
       }

    }

    @Override
    public void updateTriggerCards(CollectionAndRecoveryBean collectionAndRecoveryBean) throws Exception {
        try {
            //String sql = "UPDATE TRIGGERCARDS SET LASTTRIGGERPOINT = ? ,LASTUPDATEDTIME = TO_DATE(SYSDATE, 'DD-MM-YY')  ,LASTUPDATEDUSER = ?,NOTIFICATIONFLAG = 0,LETTERGENSTATUS =? WHERE CARDNO = ?";

            backendJdbcTemplate.update(queryParametersList.getCollectionAndRecovery_updateTriggerCards(),
                    collectionAndRecoveryBean.getLastTriger(),
                    Configurations.EOD_USER,
                    collectionAndRecoveryBean.getCardNo().toString(),
                    Configurations.NO_STATUS
            );
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public int addDetailsToCardLetterNotifyTable(StringBuffer cardNo, String nameInFull, String accNo, String contactNo, String email, String address, double dueAmount, String dueDate, String remark) {
        int count = 0;
        try {
            //String sql = "insert INTO cardletternotify (cardno, name, name, accountno, mobileno, email, address, duedate, duedate, dueamount, remark, notifyflag) values(? ,? ,? ,? ,? ,? ,TO_DATE(?, 'YYYY-MM-DD') ,? ,? ,0)  ";

           count = backendJdbcTemplate.update(queryParametersList.getCollectionAndRecovery_addDetailsToCardLetterNotifyTable(),
                    cardNo.toString(),
                    nameInFull,
                    accNo,
                    contactNo,
                    email,
                    address,
                    dueDate,
                    dueAmount,
                    remark
            );
        }catch (Exception e){
            throw e;
        }
        return count;
    }
}
