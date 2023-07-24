package com.epic.cms.repository;

import com.epic.cms.dao.CardBlockDao;
import com.epic.cms.model.bean.BlockCardBean;
import com.epic.cms.util.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;

import static com.epic.cms.util.LogManager.*;

@Repository
public class CardBlockRepo implements CardBlockDao {
    @Autowired
    QueryParametersList query;

    @Autowired
    StatusVarList statusList;

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    LogManager logManager;

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private JdbcTemplate onlineJdbcTemplate;

    @Override
    public int getBlockTheshholdPeriod(String blkthreshold) throws Exception {
        int noOfMonth = 0;

        try {
            String query = "SELECT " + blkthreshold + " AS NOOFMONTHS FROM COMMONCARDPARAMETER";

            noOfMonth = backendJdbcTemplate.queryForObject(query, Integer.class);

        }catch (EmptyResultDataAccessException e) {
            return 0;
        } catch (Exception e) {
            throw e;
        }
        return noOfMonth;
    }

    @Override
    public ArrayList<BlockCardBean> getCardListFromMinPayment(String status, int noOfMonths) throws Exception {
        ArrayList<BlockCardBean> cardList = new ArrayList<BlockCardBean>();

        try {
            String query = "SELECT C.CARDNUMBER FROM MINIMUMPAYMENT M,CARD C WHERE M.STATUS = ? AND M.COUNT >= ? AND C.MAINCARDNO = M.CARDNO AND C.CARDSTATUS NOT IN(?,?,?,?)";

            cardList = (ArrayList<BlockCardBean>) backendJdbcTemplate.query(query,
                    new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        BlockCardBean card = new BlockCardBean();
                        card.setCardNo(new StringBuffer(rs.getString("CARDNUMBER")));
                        return card;
                    })
                    , status
                    , noOfMonths
                    , statusList.getCARD_CLOSED_STATUS()
                    , statusList.getCARD_REPLACED_STATUS()
                    , statusList.getCARD_PERMANENT_BLOCKED_STATUS()
                    , statusList.getCARD_PRODUCT_CHANGE_STATUS()
            );
        } catch (Exception e) {
            throw e;
        }
        return cardList;
    }

    @Override
    public String updateCardTableForBlock(StringBuffer cardNo, String newStatus) throws Exception {
        String status = "";
        int count = 0;
        try {
            String sql1 = "select C.CARDSTATUS from CARD C where C.CARDNUMBER=? ";

            status = backendJdbcTemplate.queryForObject(sql1,
                    String.class,
                    cardNo.toString());

            if (!status.equals(statusList.getCARD_INIT()) || !status.equals(statusList.getCARD_BLOCK_STATUS())
                    || !status.equals(statusList.getCARD_EXPIRED_STATUS())) {

                String sql2 = "update CARD set CARDSTATUS=? , LASTUPDATEDTIME = SYSDATE , LASTUPDATEDUSER = ? where CARDNUMBER = ? ";
                count = backendJdbcTemplate.update(sql2, newStatus, Configurations.EOD_USER, cardNo.toString()); //CAPB
            }
        } catch (Exception e) {
            throw e;
        }
        return status;
    }

    @Override
    public int deactivateCardBlock(StringBuffer cardNo) throws Exception {
        int count = 0;
        Date eodDate = CommonMethods.getSqldate(Configurations.EOD_DATE);
        try {
            String query = "UPDATE CARDBLOCK SET STATUS = ?, LASTUPDATEDUSER= ?, LASTUPDATEDTIME= SYSDATE, LASTEODUPDATEDDATE= ? WHERE CARDNUMBER = ? AND STATUS= ? ";
            count = backendJdbcTemplate.update(query, statusList.getDEACTIVE_STATUS(),
                    Configurations.EOD_USER,
                    eodDate,
                    cardNo.toString(),
                    statusList.getACTIVE_STATUS());

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertIntoCardBlock(StringBuffer cardNo, String oldStatus, String newStatus, String reason) throws Exception {
        int count = 0;

        try {
            String sql = "INSERT INTO CARDBLOCK (CARDNUMBER,OLDSTATUS,NEWSTATUS,BLOCKREASON,LASTUPDATEDUSER,LASTUPDATEDTIME,CREATEDTIME,STATUS,CREATEDUSER,LASTEODUPDATEDDATE) VALUES(?,?,?,?,?,SYSDATE,SYSDATE,?,?,?)";

            java.sql.Date eodDate = DateUtil.getSqldate(Configurations.EOD_DATE);

            count = backendJdbcTemplate.update(sql, cardNo.toString(), oldStatus, newStatus, reason, Configurations.EOD_USER,
                    Configurations.ACTIVE_STATUS,
                    Configurations.EOD_USER,
                    eodDate);
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateMinimumPaymentTable(StringBuffer cardNo, String status) throws Exception {
        int count = 0;

        try {
            String sql = "UPDATE MINIMUMPAYMENT SET STATUS= ?,LASTUPDATEDUSER=? WHERE CARDNO = ?";

            count = backendJdbcTemplate.update(sql, status, Configurations.EOD_USER, cardNo.toString());
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int updateOnlineCardStatus(StringBuffer cardNo, int ONLINE_CARD_TEMPORARILY_BLOCKED_STATUS) throws Exception {
        int flag = 0;

        try {
            String sql = "UPDATE ecms_online_card SET STATUS = ?, LASTUPDATETIME = SYSDATE, LASTUPDATEUSER = ? WHERE CARDNUMBER = ?";

            flag = onlineJdbcTemplate.update(sql, ONLINE_CARD_TEMPORARILY_BLOCKED_STATUS, Configurations.EOD_USER,
                    cardNo.toString());

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateOnlineCardStatus ===================" + Configurations.EOD_ID);
                logInfo.info(sql);
                logInfo.info(Integer.toString(ONLINE_CARD_TEMPORARILY_BLOCKED_STATUS));
                logInfo.info(Configurations.EOD_USER);
                logInfo.info(CommonMethods.cardNumberMask(cardNo));
                logInfo.info("================ updateOnlineCardStatus END ===================");
            }
        } catch (Exception e) {
            throw e;
        }
        return flag;
    }

    @Override
    public int deactivateCardBlockOnline(StringBuffer cardNo) throws Exception {
        int count = 0;

        try {
            String sql = "UPDATE ECMS_ONLINE_CARD_BLOCK SET STATUS = ?, LASTUPDATEUSER = ?, LASTUPDATETIME = SYSDATE WHERE CARDNUMBER = ? AND STATUS=? ";

            count = onlineJdbcTemplate.update(sql, statusList.getONLINE_CARD_DEACTIVE_STATUS(), //2
                    Configurations.EOD_USER,
                    cardNo.toString(),
                    statusList.getONLINE_CARD_ACTIVE_STATUS()); //1

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ deactivateCardBlockOnline ===================" + Configurations.EOD_ID);
                logInfo.info(sql);
                logInfo.info(Integer.toString(statusList.getONLINE_CARD_DEACTIVE_STATUS()));
                logInfo.info(Configurations.EOD_USER);
                logInfo.info(CommonMethods.cardNumberMask(cardNo));
                logInfo.info(Integer.toString(statusList.getONLINE_CARD_ACTIVE_STATUS()));
                logInfo.info("================ deactivateCardBlockOnline END ===================");
            }
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public int insertToOnlineCardBlock(StringBuffer cardNo, int statusNo) throws Exception {
        int count = 0;

        try {
            String sql = "INSERT INTO ECMS_ONLINE_CARD_BLOCK (CARDNUMBER,BLOCKSTATUS,TIMESTAMP,CREATETIME,LASTUPDATETIME,LASTUPDATEUSER,STATUS) VALUES (?,?,SYSDATE,SYSDATE,SYSDATE,?,?)";

            count = onlineJdbcTemplate.update(sql, cardNo.toString(),
                    statusNo,
                    Configurations.EOD_USER,
                    Configurations.ONLINE_ACTIVE_STATUS);

            if (Configurations.ONLINE_LOG_LEVEL == 1) {
                //Only for troubleshoot
                logInfo.info("================ updateOnlineCardTable ===================" + Configurations.EOD_ID);
                logInfo.info(sql);
                logInfo.info(CommonMethods.cardNumberMask(cardNo));
                logInfo.info(String.valueOf(statusNo));
                logInfo.info(Configurations.EOD_USER);
                logInfo.info(Integer.toString(Configurations.ONLINE_ACTIVE_STATUS));
                logInfo.info("================ updateOnlineCardTable END ===================");
            }
        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public BlockCardBean getCardBlockOldCardStatus(StringBuffer cardNO) throws Exception {
        BlockCardBean blockBean;

        try {
            String sql = "SELECT OLDSTATUS,NEWSTATUS,BLOCKREASON FROM CARDBLOCK WHERE CARDNUMBER = ? AND STATUS IN(?)";

            blockBean = backendJdbcTemplate.query(sql,
                    (ResultSet rs) -> {
                        BlockCardBean blockCardBean = null;
                        while (rs.next()) {
                            blockCardBean = new BlockCardBean();
                            String oldStatus = rs.getString("OLDSTATUS");
                            blockCardBean.setOldStatus(oldStatus);
                            blockCardBean.setNewStatus(rs.getString("NEWSTATUS"));
                            blockCardBean.setCardNo(cardNO);
                        }
                        return blockCardBean;
                    },
                    cardNO.toString(),
                    statusList.getACTIVE_STATUS()
            );
        } catch (Exception e) {
            throw e;
        }
        return blockBean;
    }

    @Override
    public int updateCardStatus(StringBuffer cardNO, String status) throws Exception {
        int flag = 0;

        try {
            String sql = "update CARD set CARDSTATUS=? , LASTUPDATEDTIME = SYSDATE , LASTUPDATEDUSER = ? where CARDNUMBER = ?";

            flag = backendJdbcTemplate.update(sql, status, Configurations.EOD_USER, cardNO.toString());

        } catch (Exception e) {
            throw e;
        }
        return flag;
    }
}
