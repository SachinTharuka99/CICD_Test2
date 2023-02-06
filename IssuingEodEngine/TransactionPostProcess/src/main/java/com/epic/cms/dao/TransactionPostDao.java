/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:45 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.dao;

import java.util.ArrayList;
import com.epic.cms.model.bean.OtbBean;

public interface TransactionPostDao {

    ArrayList<OtbBean> getInitEodTxnPostCustAcc() throws Exception;

    ArrayList<OtbBean> getErrorEodTxnPostCustAcc() throws Exception;

    ArrayList<OtbBean> getTxnAmount(String accountnumber) throws Exception;

    int updateCardTemp(StringBuffer cardnumber, double payment) throws Exception;

    int updateCardOtbCredit(OtbBean cardBean) throws Exception;

    int updateAccountOtbCredit(OtbBean bean) throws Exception;

    int updateCustomerOtbCredit(OtbBean bean) throws Exception;

    int updateCardByPostedTransactions(OtbBean cardBean) throws Exception;

    int updateEODCARDBALANCEByTxn(OtbBean cardBean) throws Exception;

    int updateEODTRANSACTION(String accountNumber) throws Exception;

    int updateAccountOtb(OtbBean otbBean) throws Exception;

    int updateCustomerOtb(OtbBean bean) throws Exception;

    StringBuffer getNewCardNumber(StringBuffer oldCardNumber) throws Exception;
}
