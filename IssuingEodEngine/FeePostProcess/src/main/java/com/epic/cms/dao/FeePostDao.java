package com.epic.cms.dao;

import com.epic.cms.model.bean.OtbBean;

import java.util.List;

public interface FeePostDao {

    List<OtbBean> getInitEodFeePostCustAcc() throws Exception;

    List<OtbBean> getErrorEodFeePostCustAcc() throws Exception;

    List<OtbBean> getFeeAmount(String accNo) throws Exception;

    int updateCardOtb(OtbBean cardBean) throws Exception;

    void updateEODCARDBALANCEByFee(OtbBean cardBean) throws Exception;

    void updateOnlineCardOtb(OtbBean cardBean) throws Exception;

    void updateAccountOtb(OtbBean otbBean) throws Exception;

    void updateEODCARDFEE(String accNo) throws Exception;

    void updateEOMINTEREST(String accNo) throws Exception;

    void updateOnlineAccountOtb(OtbBean otbBean) throws Exception;

    void updateCustomerOtb(OtbBean bean) throws Exception;

    void updateOnlineCustomerOtb(OtbBean bean) throws Exception;

    public int expireFeePromotionProfile() throws Exception;
}
