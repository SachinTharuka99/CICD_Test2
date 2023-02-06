package com.epic.cms.repository;

import com.epic.cms.dao.CardExpireDao;
import com.epic.cms.model.bean.CardBean;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

import static com.epic.cms.util.LogManager.infoLogger;

@Repository
public class CardExpireRepo implements CardExpireDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Autowired
    StatusVarList statusList;

    @Override
    public ArrayList<CardBean> getExpiredCardList() throws Exception {

        ArrayList<CardBean> expiredCardList = new ArrayList<>();
        try {
            java.sql.Date eodDate = CommonMethods.getSqldate(Configurations.EOD_DATE);

            String sql = "SELECT  cardnumber, newexpirydate, cardstatus, expierydate FROM card WHERE last_day(to_date(expierydate, 'yymm')) < ?  AND cardstatus NOT IN ( ?, ?, ?, ? ) ";

            expiredCardList = (ArrayList<CardBean>) backendJdbcTemplate.query(sql,
                    new RowMapperResultSetExtractor<>((result, rowNum) -> {
                        CardBean bean = new CardBean();
                        bean.setCardnumber(new StringBuffer(result.getString("CARDNUMBER")));
                        bean.setNewExpireDate(result.getString("NEWEXPIRYDATE"));
                        bean.setCardStatus(result.getString("CARDSTATUS"));
                        bean.setExpiryDate(result.getString("EXPIERYDATE"));
                        return bean;
                    }),
                    eodDate,
                    statusList.getCARD_EXPIRED_STATUS(),
                    statusList.getCARD_CLOSED_STATUS(),
                    statusList.getCARD_REPLACED_STATUS(),
                    statusList.getCARD_PRODUCT_CHANGE_STATUS()
            );
        } catch (Exception e) {
            throw e;
        }
        return expiredCardList;
    }

    /**
     * @param cardNumber
     * @throws Exception
     */
    @Override
    public int setCardStatusToExpire(StringBuffer cardNumber) throws Exception {
        int count = 0;
        String sql = "UPDATE card SET cardstatus = ?, lastupdateduser = ?, lastupdatedtime = sysdate WHERE cardnumber = ? ";
        try {
            count = backendJdbcTemplate.update(sql, statusList.getCARD_EXPIRED_STATUS(), Configurations.EOD_USER, cardNumber.toString());
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    /**
     * @param cardNumber
     * @throws Exception
     */
    @Override
    public void setOnlineCardStatusToExpire(StringBuffer cardNumber) throws Exception {
        String sql = "UPDATE ecms_online_card  SET status = ?, lastupdateuser = ?,  lastupdatetime = sysdate  WHERE cardnumber = ? ";
        try {
            onlineJdbcTemplate.update(sql, statusList.getONLINE_CARD_EXPIRED_STATUS(), Configurations.EOD_USER, cardNumber.toString());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                infoLogger.info("================ setCardStatusToExpire ===================" + Integer.toString(Configurations.EOD_ID));
                infoLogger.info(sql);
                infoLogger.info(Integer.toString(statusList.getONLINE_CARD_EXPIRED_STATUS()));
                infoLogger.info(Configurations.EOD_USER);
                infoLogger.info(CommonMethods.cardNumberMask(cardNumber));
                infoLogger.info("================ setCardStatusToExpire END ===================");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * @param cardNumber
     * @param cardStatus
     * @return
     * @throws Exception
     */
    @Override
    public int insertToCardBlock(StringBuffer cardNumber, String cardStatus) throws Exception {
        int count = 0;

        String sql = "INSERT INTO cardblock ( cardnumber, oldstatus, newstatus, blockreason, lastupdateduser, lastupdatedtime, createdtime, status, createduser, lasteodupdateddate ) "
                + " VALUES (?,?,?,?,?,SYSDATE,SYSDATE,?,?,?) ";
        try {
            java.sql.Date eodDate = CommonMethods.getSqldate(Configurations.EOD_DATE);

            count =  backendJdbcTemplate.update(sql,
                    cardNumber.toString(),
                    cardStatus,
                    statusList.getCARD_EXPIRED_STATUS(),
                    "Card Is Expired",
                    Configurations.EOD_USER,
                    Configurations.ACTIVE_STATUS,
                    Configurations.EOD_USER,
                    eodDate);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }
}
