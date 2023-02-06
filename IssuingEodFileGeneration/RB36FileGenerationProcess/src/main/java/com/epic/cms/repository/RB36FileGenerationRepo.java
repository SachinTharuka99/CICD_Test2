/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 10:34 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.RB36FileGenerationDao;
import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.model.bean.GlBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@Repository
public class RB36FileGenerationRepo implements RB36FileGenerationDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    private StatusVarList statusVarList;

    @Override
    public ArrayList<StringBuffer> getNPCard() throws Exception {
        ArrayList<StringBuffer> npCardList = new ArrayList<>();
        try {
            String sql = "SELECT DISTINCT C.CARDNUMBER AS CARDNUMBER FROM CARD C,DELINQUENTACCOUNT D WHERE D.ACCSTATUS IN (?) AND C.MAINCARDNO=D.CARDNUMBER";

            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                while (rs.next()) {
                    npCardList.add(new StringBuffer(rs.getString("CARDNUMBER")));
                }
                return npCardList;

            }, statusVarList.getACCOUNT_NON_PERFORMING_STATUS());
        } catch (Exception e) {
            throw e;
        }
        return npCardList;
    }

    @Override
    public HashMap<String, ArrayList<GlAccountBean>> getPaymentDataFromEODGl() throws Exception {
        HashMap<String, ArrayList<GlAccountBean>> hmap = new HashMap<>();
        ArrayList<String> glTypes = new ArrayList<>();

        try {
            String sql = "SELECT ID,EODID,CARDNO ,NVL(GLTYPE,'--') AS GLTYPE,NVL(PAYMENTTYPE,'CASH') AS PAYMENTTYPE,AMOUNT,CRDR,CARDNO"
                    + " FROM EODGLACCOUNT "
                    + " WHERE EODSTATUS = ?"
                    + " AND GLTYPE IN (?,?) ";

            backendJdbcTemplate.query(sql, (ResultSet rs) -> {
                        while (rs.next()) {
                            ArrayList<GlAccountBean> list;
                            GlAccountBean bean = new GlAccountBean();
                            String glType = rs.getString("PAYMENTTYPE");
                            bean.setId(rs.getInt("ID"));
                            bean.setAmount(rs.getDouble("AMOUNT"));
                            bean.setGlAmount(rs.getString("AMOUNT"));
                            bean.setKey(rs.getString("EODID"));
                            bean.setCrDr(rs.getString("CRDR"));
                            bean.setGlType(rs.getString("GLTYPE"));
                            bean.setPaymentType(rs.getString("PAYMENTTYPE"));
                            bean.setCardNo(new StringBuffer(rs.getString("CARDNO")));

                            if (glTypes.contains(glType.toString())) {
                                list = hmap.get(glType);
                                list.add(bean);
                                hmap.put(glType, list);

                            } else {
                                glTypes.add(glType);
                                ArrayList<GlAccountBean> set = new ArrayList<GlAccountBean>();
                                set.add(bean);
                                hmap.put(glType, set);
                            }
                        }
                        return hmap;

                    }, statusVarList.getEOD_PENDING_STATUS()
                    , Configurations.TXN_TYPE_PAYMENT
                    , Configurations.TXN_TYPE_DEBIT_PAYMENT
                    /*,"INTINC" , "4890119602758727"*/
            );

        } catch (Exception e) {
            throw e;
        }
        return hmap;
    }

    @Override
    public HashMap<String, GlBean> getGLAccData() throws Exception {
        HashMap<String, GlBean> hmap = new HashMap<>();
        try {
            String sql = "SELECT NVL(G.GLACCOUNTCODE,'00') AS GLACCOUNTCODE,"
                    + " NVL(G.BRANCH,'00')             AS BRANCH,"
                    + " NVL(G.CLIENTNO,'00')           AS CLIENTNO,"
                    + " NVL(C.CURRENCYALPHACODE,'00')           AS CURRENCY,"
                    + " NVL(G.PROFITCENTRE,'00')       AS PROFITCENTRE,"
                    + " NVL(G.PRODUCTCATEGORY,'00')       AS PRODCATEGORY"
                    + " FROM GLACCOUNT G, CURRENCY C"
                    + " WHERE G.currency=C.CURRENCYNUMCODE"
                    + " AND G.STATUS = ? ";

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
            }, statusVarList.getACTIVE_STATUS());

        }catch (Exception e){
            throw e;
        }
        return hmap;
    }

    @Override
    public int updateEodGLAccount(int key) throws Exception {
        int count = 0;
        try {
            String sql = "UPDATE EODGLACCOUNT SET EODSTATUS = ? WHERE ID = ? ";
            count = backendJdbcTemplate.update(sql, statusVarList.getEOD_DONE_STATUS(), key);

        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public Date getNextWorkingDay(Date DueDate) throws Exception {
        boolean holiday = this.isHoliday(DueDate);
        Date nextDate = DueDate;
        int x = 1;
        while (holiday) {
            nextDate = CommonMethods.getNextDateForFreq(DueDate, x);
            if (this.isHoliday(nextDate)) {
                x = x + 1;
            } else {
                holiday = false;
            }
        }
        return nextDate;
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
