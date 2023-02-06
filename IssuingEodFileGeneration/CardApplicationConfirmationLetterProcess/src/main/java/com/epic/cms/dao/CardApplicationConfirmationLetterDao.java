/**
 * Author : yasiru_l
 * Date : 11/21/2022
 * Time : 10:09 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface CardApplicationConfirmationLetterDao {
    ArrayList<StringBuffer> getConfirmedCardToGenerateLetters() throws SQLException;
    int updateLettergenStatus(StringBuffer cardNo, String yes) throws Exception;
}
