package com.epic.cms.dao;

import com.epic.cms.model.bean.CardBean;

import java.util.ArrayList;

public interface CardExpireDao {
    ArrayList<CardBean> getExpiredCardList() throws Exception;

    int setCardStatusToExpire(StringBuffer cardNumber) throws Exception;

    void setOnlineCardStatusToExpire(StringBuffer cardNumber) throws Exception;

    int insertToCardBlock(StringBuffer cardNumber, String cardStatus) throws Exception;
}
