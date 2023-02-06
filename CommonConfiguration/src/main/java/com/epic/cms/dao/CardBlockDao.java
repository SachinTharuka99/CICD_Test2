package com.epic.cms.dao;

import com.epic.cms.model.bean.BlockCardBean;

import java.util.ArrayList;

public interface CardBlockDao {
    int getBlockTheshholdPeriod(String blkthreshold) throws Exception;

    ArrayList<BlockCardBean> getCardListFromMinPayment(String status, int noOfMonths)  throws Exception;

    String updateCardTableForBlock(StringBuffer cardNo, String newStatus) throws Exception;

    int deactivateCardBlock(StringBuffer cardNo)  throws Exception;

    int insertIntoCardBlock(StringBuffer cardNo, String oldStatus, String newStatus, String reason) throws Exception;

    int updateMinimumPaymentTable(StringBuffer cardNo, String status) throws Exception;

    int updateOnlineCardStatus(StringBuffer cardNo, int ONLINE_CARD_TEMPORARILY_BLOCKED_STATUS) throws Exception;

    int deactivateCardBlockOnline(StringBuffer cardNo) throws Exception;

    int insertToOnlineCardBlock(StringBuffer cardNo, int statusNo) throws Exception;

    BlockCardBean getCardBlockOldCardStatus(StringBuffer cardNO)  throws Exception;

    int updateCardStatus(StringBuffer cardNO, String status) throws Exception;
}
