package com.epic.cms.dao;

import java.util.ArrayList;

public interface CardRenewLetterDao {

    ArrayList<StringBuffer> getRenewalCardsToGenerateLetters() throws Exception;

    int updateLettergenStatusInCardRenew(StringBuffer cardNo, String letterGenStatus) throws Exception;
}
