package com.epic.cms.service;

import com.epic.cms.model.bean.MerchantLocationBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MerchantStatementRepository;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.*;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MerchantStatementServiceTest {

    private MerchantStatementService merchantStatementServiceUnderTest;

    @BeforeEach
    void setUp() {
        merchantStatementServiceUnderTest = new MerchantStatementService();
        merchantStatementServiceUnderTest.commonRepo = mock(CommonRepo.class);
        merchantStatementServiceUnderTest.statusList = mock(StatusVarList.class);
        merchantStatementServiceUnderTest.merchantStatementDao = mock(MerchantStatementRepository.class);
    }

    @Test
    void testMerchantStatementService() throws Exception {

        // Configure MerchantStatementRepository.insertMerchantStatement(...).
        final MerchantLocationBean merchantLocationBean = new MerchantLocationBean();
        // Setup
        final Map.Entry<String, MerchantLocationBean> entry = new AbstractMap.SimpleEntry<>("merchantKey", merchantLocationBean);




        merchantLocationBean.setMerchantId("merchantId");
        merchantLocationBean.setPostalCode("postalCode");
        merchantLocationBean.setMerchantType("merchantType");
        merchantLocationBean.setMerchantEmail("merchantEmail");
        merchantLocationBean.setMerchantCurrency("merchantCurrency");
        merchantLocationBean.setBankName("bankName");
        merchantLocationBean.setPaymentMode("paymentMode");
        merchantLocationBean.setMerchantDes("merchantDes");
        merchantLocationBean.setAddress1("address1");
        merchantLocationBean.setAddress2("address2");
        merchantLocationBean.setAddress3("address3");
        merchantLocationBean.setBillingDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        merchantLocationBean.setLastBillingDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        merchantLocationBean.setNetPaymentAmount(0.0);
        merchantLocationBean.setPaymentAmount(0.0);
        merchantLocationBean.setCommissionAmount(0.0);
        merchantLocationBean.setFeeAmount(0.0);
        merchantLocationBean.setOpeningBalance(0.0);
        merchantLocationBean.setClosingBalance(0.0);
        merchantLocationBean.setAccNumber("accNumber");

        Configurations.EOD_DATE = new Date();
        Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = 0;
        Configurations.PROCESS_SUCCESS_COUNT = 0;
        Configurations.PROCESS_FAILD_COUNT = 0;

        List<Object[]> resultList = new ArrayList<>();
        Object[] row = new Object[8];
        row[0] = "120712";
        row[1] = "120712";
        row[2] = "12537653761231223";
        row[3] = ("3232");
        row[4] = ("34343");
        row[5] = ("34343434");
        row[6] = ("343434");
        row[7] = ("343434");
        resultList.add(row);

        when(merchantStatementServiceUnderTest.merchantStatementDao.insertMerchantStatement(
                any(MerchantLocationBean.class))).thenReturn(merchantLocationBean);

        when(merchantStatementServiceUnderTest.merchantStatementDao.getMerchantStatementTxnList(
                "merchantId")).thenReturn(resultList);
        when(merchantStatementServiceUnderTest.merchantStatementDao.getMerchantStatementAdjustmentList(
                "merchantId")).thenReturn(resultList);
        when(merchantStatementServiceUnderTest.merchantStatementDao.getMerchantStatementFeesList(
                "merchantId")).thenReturn(resultList);
        when(merchantStatementServiceUnderTest.merchantStatementDao.insertMerchantStatement(
                merchantLocationBean)).thenReturn(merchantLocationBean);

        // Run the test
        merchantStatementServiceUnderTest.merchantStatementService(entry);

        // Verify the results
        verify(merchantStatementServiceUnderTest.merchantStatementDao,times(1)).getMerchantStatementTxnList(any());
        verify(merchantStatementServiceUnderTest.merchantStatementDao,times(1)).getMerchantStatementAdjustmentList(any());
        verify(merchantStatementServiceUnderTest.merchantStatementDao,times(1)).getMerchantStatementFeesList(any());
        verify(merchantStatementServiceUnderTest.merchantStatementDao,times(1)).insertMerchantStatement(any(MerchantLocationBean.class));

    }
}
