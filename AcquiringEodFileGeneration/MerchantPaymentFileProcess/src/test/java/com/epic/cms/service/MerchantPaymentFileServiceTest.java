package com.epic.cms.service;

import com.epic.cms.dao.MerchantPaymentFileDao;
import com.epic.cms.model.bean.MerchantCustomerBean;
import com.epic.cms.model.bean.MerchantPayBean;
import com.epic.cms.model.bean.MerchantPaymentCycleBean;
import com.epic.cms.model.bean.EodOuputFileBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MerchantPaymentFileServiceTest {

    private MerchantPaymentFileService merchantPaymentFileServiceUnderTest;

    @BeforeEach
    void setUp() {
        merchantPaymentFileServiceUnderTest = new MerchantPaymentFileService();
        merchantPaymentFileServiceUnderTest.logManager = mock(LogManager.class);
        merchantPaymentFileServiceUnderTest.commonRepo = mock(CommonRepo.class);
        merchantPaymentFileServiceUnderTest.statusList = mock(StatusVarList.class);
        merchantPaymentFileServiceUnderTest.merchantPaymentFileDao = mock(MerchantPaymentFileDao.class);
    }

    @Test
    void testPaymentFile() throws Exception {
        // Setup
        final Map.Entry<String, HashMap<Integer, HashMap<String, ArrayList<MerchantPaymentCycleBean>>>> entrySet = null;

        // Configure MerchantPaymentFileDao.getNextWorkingDay(...).
        final Date date = new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime();
        when(merchantPaymentFileServiceUnderTest.merchantPaymentFileDao.getNextWorkingDay(
                new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime())).thenReturn(date);

        when(merchantPaymentFileServiceUnderTest.statusList.getNO_STATUS_0()).thenReturn(0);

        // Configure MerchantPaymentFileDao.getPaymentsFromEodMerchantpayment(...).
        final MerchantPayBean merchantPayBean = new MerchantPayBean();
        merchantPayBean.setEodPayId(0);
        merchantPayBean.setMerchantId("merchantId");
        merchantPayBean.setMerchantCusId("merchantCusId");
        merchantPayBean.setMerchantAccNo("merchantAccNo");
        merchantPayBean.setMerchantCusAccNo("merchantCusAccNo");
        merchantPayBean.setTxncount(0);
        merchantPayBean.setDrTxnAmount(0.0);
        merchantPayBean.setCrTxnAmount(0.0);
        merchantPayBean.setCommAmount(0.0);
        merchantPayBean.setFeeAmount(0.0);
        merchantPayBean.setPaymentAmount(0.0);
        merchantPayBean.setNetPayAmount("netPayAmount");
        merchantPayBean.setCrDrnetPayment("crDrnetPayment");
        merchantPayBean.setCurrencyType(0);
        merchantPayBean.setAccountNo("accountNo");
        final ArrayList<MerchantPayBean> merchantPayBeans = new ArrayList<>(List.of(merchantPayBean));
        when(merchantPaymentFileServiceUnderTest.merchantPaymentFileDao.getPaymentsFromEodMerchantpayment(
                "merchantId")).thenReturn(merchantPayBeans);

        when(merchantPaymentFileServiceUnderTest.merchantPaymentFileDao.updatePaymentFileStatus(
                new ArrayList<>(List.of("value")))).thenReturn(0);
        when(merchantPaymentFileServiceUnderTest.statusList.getYES_STATUS_1()).thenReturn(0);

        // Configure MerchantPaymentFileDao.getMerchantCustomerDetails(...).
        final MerchantCustomerBean merchantCustomerBean = new MerchantCustomerBean();
        merchantCustomerBean.setMerchantCusNo("merchantCusNo");
        merchantCustomerBean.setMerchantCusDes("merchantCusDes");
        merchantCustomerBean.setLegalName("legalName");
        merchantCustomerBean.setAddress1("address1");
        merchantCustomerBean.setAddress2("address2");
        merchantCustomerBean.setAddress3("address3");
        merchantCustomerBean.setStatementCycleCode("statementCycleCode");
        merchantCustomerBean.setStatus("status");
        merchantCustomerBean.setComisionProfile("comisionProfile");
        merchantCustomerBean.setFeeProfile("feeProfile");
        merchantCustomerBean.setRiskProfile("riskProfile");
        merchantCustomerBean.setPaymentmaintananceStatus("paymentmaintananceStatus");
        merchantCustomerBean.setStatementMaintananceStatus("statementMaintananceStatus");
        merchantCustomerBean.setAccountNo("accountNo");
        merchantCustomerBean.setCurrencyCode("currencyCode");
        when(merchantPaymentFileServiceUnderTest.merchantPaymentFileDao.getMerchantCustomerDetails("CusId"))
                .thenReturn(merchantCustomerBean);

        // Configure MerchantPaymentFileDao.getPaymentsForCustomerFromEodMerchantpayment(...).
        final MerchantPayBean merchantPayBean1 = new MerchantPayBean();
        merchantPayBean1.setEodPayId(0);
        merchantPayBean1.setMerchantId("merchantId");
        merchantPayBean1.setMerchantCusId("merchantCusId");
        merchantPayBean1.setMerchantAccNo("merchantAccNo");
        merchantPayBean1.setMerchantCusAccNo("merchantCusAccNo");
        merchantPayBean1.setTxncount(0);
        merchantPayBean1.setDrTxnAmount(0.0);
        merchantPayBean1.setCrTxnAmount(0.0);
        merchantPayBean1.setCommAmount(0.0);
        merchantPayBean1.setFeeAmount(0.0);
        merchantPayBean1.setPaymentAmount(0.0);
        merchantPayBean1.setNetPayAmount("netPayAmount");
        merchantPayBean1.setCrDrnetPayment("crDrnetPayment");
        merchantPayBean1.setCurrencyType(0);
        merchantPayBean1.setAccountNo("accountNo");
        final ArrayList<MerchantPayBean> merchantPayBeans1 = new ArrayList<>(List.of(merchantPayBean1));
        when(merchantPaymentFileServiceUnderTest.merchantPaymentFileDao.getPaymentsForCustomerFromEodMerchantpayment(
                "key")).thenReturn(merchantPayBeans1);

        when(merchantPaymentFileServiceUnderTest.merchantPaymentFileDao.InsertMerchantPaymentFilesIntoDownloadTable(
                "fileId", "MERCHANTPAYMENTDIRECT")).thenReturn(0);
        when(merchantPaymentFileServiceUnderTest.merchantPaymentFileDao.insertOutputFiles(any(EodOuputFileBean.class),
                eq("MERCHANTPAYMENTDIRECT"))).thenReturn(0);

        // Run the test
        //merchantPaymentFileServiceUnderTest.paymentFile(entrySet, "fileNameF1", "fileNameF2");

        // Verify the results
//        verify(merchantPaymentFileServiceUnderTest.merchantPaymentFileDao).insertOutputFiles(
//                any(EodOuputFileBean.class), eq("MERCHANTPAYMENTDIRECT"));
//        verify(merchantPaymentFileServiceUnderTest.merchantPaymentFileDao).InsertMerchantFilesIntoDownloadTable(
//                "fileId", "MERCHANTPAYMENTSLIP");
    }

   
}
