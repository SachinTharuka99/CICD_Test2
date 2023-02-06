/**
 * Author : yasiru_l
 * Date : 12/5/2022
 * Time : 11:35 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.dao;

import java.util.ArrayList;
import java.util.List;

public interface CardApplicationRejectLetterDao {

    ArrayList<String> getRejectApplictionIDsToGenerateLetters(String StartEodStatus, boolean isErrorProcessInLastEod, boolean isProcessCompletlyFail) throws Exception;

    int updateLettergenStatus(StringBuffer cardNo, String yes) throws Exception;

    StringBuffer getCardNo(String applicationI)throws Exception;
}
