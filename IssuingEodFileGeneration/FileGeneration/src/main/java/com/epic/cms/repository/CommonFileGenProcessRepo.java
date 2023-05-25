/**
 * Author : lahiru_p
 * Date : 11/16/2022
 * Time : 1:24 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.CommonFileGenProcessDao;
import com.epic.cms.model.bean.GlBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.*;

import static com.epic.cms.util.LogManager.*;

@Repository
public class CommonFileGenProcessRepo implements CommonFileGenProcessDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Autowired
    LogManager logManager;

    @Override
    public List<String> getCardProductCardType(StringBuffer cardNo) throws Exception {
        List<String> cardDetails = new ArrayList<>();
        try {
            String sql = "SELECT CA.CARDTYPE,CA.CARDPRODUCT,ACC.ACCOUNTNO FROM CARD CA "
                    + "LEFT JOIN CARDACCOUNT ACC ON CA.MAINCARDNO = ACC.CARDNUMBER "
                    + "WHERE CA.CARDNUMBER= ? ";

            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    cardDetails.add(0, rs.getString("CARDTYPE"));
                    cardDetails.add(1, rs.getString("CARDPRODUCT"));
                    cardDetails.add(2, rs.getString("ACCOUNTNO"));
                }
                return cardDetails;
            }, cardNo.toString());

        } catch (Exception e) {
            throw e;
        }
        return cardDetails;
    }

    @Override
    public void InsertIntoDownloadTable(StringBuffer cardNo, String filename, List<String> cardDetails) throws Exception {
        try {
            String query = "Insert into DOWNLOADFILE (FIETYPE,FILENAME,LETTERTYPE, "
                    + "STATUS,GENERATEDUSER,STATEMENTMONTH,STATEMENTYEAR,LASTUPDATEDTIME, "
                    + "CREATEDTIME,LASTUPDATEDUSER,CARDTYPE,CARDPRODUCT,FILEID,ACCNUMBER, "
                    + "APPLICATIONID) values "
                    + "(?,?,?,?,?,?,?,to_date(SYSDATE,'DD-MM-YY'),to_date(SYSDATE, 'DD-MM-YY'),?,?,?,?,?,?) ";
            backendJdbcTemplate.update(query, cardDetails.get(3)
            ,filename
            ,cardDetails.get(4)
            ,Configurations.YES_STATUS
            ,Configurations.EOD_USER
            ,""
            ,""
            ,Configurations.EOD_USER
            ,cardDetails.get(0)
            ,cardDetails.get(1)
            ,filename
            ,cardDetails.get(2)
            ,statusList.getEOD_DONE_STATUS());
        } catch (Exception e) {
            logManager.logError("Exception in Insert into Download Table " + e.getMessage(),errorLoggerEFGE);
            throw e;
        }
    }

    @Override
    public List<String> getCardProductCardTypeByApplicationId(String applicationId) throws Exception {
        List<String> cardDetails = new ArrayList<>();
        try {
            String sql = "SELECT ca.cardtype, ca.cardproduct, acc.accountno FROM card ca LEFT JOIN cardaccount acc ON ca.maincardno = acc.cardnumber INNER JOIN cardapplication cap ON ca.maincardno = cap.cardnumber WHERE cap.applicationid = ?";
            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    cardDetails.add(0, rs.getString("CARDTYPE"));
                    cardDetails.add(1, rs.getString("CARDPRODUCT"));
                    cardDetails.add(2, rs.getString("ACCOUNTNO"));
                }
                return cardDetails;
            }, applicationId);
        } catch (Exception e) {
            e.printStackTrace();
            logManager.logError("Exception in Get Card Product Type Method ", errorLoggerEFGE);
            throw e;
        }
        return cardDetails;
    }

    @Override
    public Date getNextWorkingDay(Date dueDate) throws Exception {
        boolean holiday = this.isHoliday(dueDate);
        Date nextDate = dueDate;
        int x = 1;
        while (holiday) {
            nextDate = CommonMethods.getNextDateForFreq(dueDate, x);
            if (this.isHoliday(nextDate)) {
                x = x + 1;
            } else {
                holiday = false;
            }
        }
        return nextDate;
    }

    @Override
    public HashMap<String, GlBean> getGLAccData() throws Exception {
        HashMap<String, GlBean> hmap = new HashMap<>();
        try {
            String sql = "SELECT NVL(G.GLACCOUNTCODE,'00') AS GLACCOUNTCODE, NVL(G.BRANCH,'00')             AS BRANCH, NVL(G.CLIENTNO,'00')           AS CLIENTNO, NVL(C.CURRENCYALPHACODE,'00')           AS CURRENCY, NVL(G.PROFITCENTRE,'00')       AS PROFITCENTRE, NVL(G.PRODUCTCATEGORY,'00')       AS PRODCATEGORY FROM GLACCOUNT G, CURRENCY C WHERE G.currency=C.CURRENCYNUMCODE AND G.STATUS = ? ";

            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    GlBean bean = new GlBean();
                    String glAcc = rs.getString("GLACCOUNTCODE");
                    bean.setClientNo(rs.getString("CLIENTNO"));
                    bean.setCurrencyCode(rs.getString("CURRENCY"));
                    bean.setProfitCenter(rs.getString("PROFITCENTRE"));
                    bean.setBranch(rs.getString("BRANCH"));
                    bean.setProdCategory(rs.getString("PRODCATEGORY"));

                    hmap.put(glAcc, bean);
                }
                return hmap;
            }, statusList.getACTIVE_STATUS());
        }catch (Exception e){
            throw e;
        }
        return hmap;
    }

    @Override
    public HashMap<String, String[]> getGLTxnTypes() throws Exception {
        HashMap<String, String[]> hmap = new HashMap<>();
        try {
            String sql = "SELECT GLTXNTYPECODE,DESCRIPTION,NVL(POSITIONTYPE, 'TR') AS POSITIONTYPE FROM GLTXNTYPE";
            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    String[] glTxn = new String[2];
                    String txnTypeCode = rs.getString("GLTXNTYPECODE");
                    glTxn[0] = rs.getString("DESCRIPTION");
                    glTxn[1] = rs.getString("POSITIONTYPE");

                    hmap.put(txnTypeCode, glTxn);
                }
                return hmap;
            });

        }catch (Exception e){
            throw e;
        }
        return hmap;
    }

    @Override
    public String getCRDRFromGlTxn(String key) throws Exception {
        String crdr = null;
        try {
            String sql = "SELECT NVL(CRDR,'DR') AS CRDR FROM GLTXNTYPE WHERE GLTXNTYPECODE=? ";
            crdr = backendJdbcTemplate.queryForObject(sql, String.class, key);
        }catch (Exception e){
            throw e;
        }
        return crdr;
    }

    private boolean isHoliday(Date today) {
        String query = "SELECT COUNT(*) FROM HOLIDAY WHERE YEAR = ? AND MONTH=? AND DAY=?";
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(today);
        try {
            int count = 0;
            String year = String.valueOf(calendar.get(Calendar.YEAR));
            String month = String.valueOf(calendar.get(Calendar.MONTH));
            String day = String.valueOf(calendar.get(Calendar.DATE));
            count = backendJdbcTemplate.queryForObject(query, Integer.class, year, month, day);

            if (count > 0) {
                return true;
            } else {
                return false;
            }
        }catch (EmptyResultDataAccessException ex){
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
