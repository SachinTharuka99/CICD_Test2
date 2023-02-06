/**
 * Author : yasiru_l
 * Date : 11/22/2022
 * Time : 4:38 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface CardReplaceLetterDao {

    ArrayList<StringBuffer> getReplacedToGenerateLetters() throws SQLException;
    int updateLettergenStatusInCardReplace(StringBuffer cardNo, String yes) throws Exception;
    ArrayList<StringBuffer> getProductChangedCardsToGenerateLetters() throws SQLException;
    List<String> getCardProductCardTypeForProductChangeCards(StringBuffer cardNo);
    int updateLettergenStatusInProductChange(StringBuffer cardNo, String yes) throws Exception;

}
