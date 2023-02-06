package com.epic.cms.dao;

import com.epic.cms.model.bean.CardReplaceBean;

import java.util.List;

public interface CardReplaceDao {

    List<CardReplaceBean> getCardListToReplace() throws Exception;

    void updateBackendOldCardFromNewCard(CardReplaceBean cardReplaceBean) throws Exception;

    void updateOnlineOldCardFromNewCard(CardReplaceBean cardReplaceBean) throws Exception;

    void updateCardReplaceStatus(StringBuffer newCardNo) throws Exception;
}
