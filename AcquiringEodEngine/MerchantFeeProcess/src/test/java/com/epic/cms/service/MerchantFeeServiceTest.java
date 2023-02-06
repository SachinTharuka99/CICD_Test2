package com.epic.cms.service;

import com.epic.cms.model.bean.MerchantFeeBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MerchantFeeRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MerchantFeeServiceTest {

    private MerchantFeeService merchantFeeServiceUnderTest;

    @BeforeEach
    void setUp() {
        merchantFeeServiceUnderTest = new MerchantFeeService();
        merchantFeeServiceUnderTest.logManager = mock(LogManager.class);
        merchantFeeServiceUnderTest.merchantFeeRepo = mock(MerchantFeeRepo.class);
        merchantFeeServiceUnderTest.status = mock(StatusVarList.class);
        merchantFeeServiceUnderTest.commonRepo = mock(CommonRepo.class);
    }

    @Test
    void testMerchantFee() throws Exception {
        // Setup
        final MerchantFeeBean merchantFeeBean = new MerchantFeeBean();
        merchantFeeBean.setMID("1498916");
        merchantFeeBean.setFeeCode("feeCode");
        merchantFeeBean.setFeeCount(0);
        merchantFeeBean.setCurrCode(0);
        merchantFeeBean.setCrORdr("crORdr");
        merchantFeeBean.setFlatFee(0.0);
        merchantFeeBean.setMinAmount(0.0);
        merchantFeeBean.setMaxAmount(0.0);
        merchantFeeBean.setPercentageAmount(0.0);
        merchantFeeBean.setCombination("combination");
        merchantFeeBean.setCashAmount(0.0);
        merchantFeeBean.setCustAccountNo("custAccountNo");
        merchantFeeBean.setMerchantAccountNo("merchantAccountNo");
        merchantFeeBean.setMerchantCustomerNo("merchantCustomerNo");

        Configurations.EOD_DATE = new java.util.Date(System.currentTimeMillis());

        // Run the test
        merchantFeeServiceUnderTest.MerchantFee(merchantFeeBean);

        // Verify the results
        verify(merchantFeeServiceUnderTest.merchantFeeRepo).insertToEODMerchantFee(any(MerchantFeeBean.class), anyDouble(), any(Date.class));
        verify(merchantFeeServiceUnderTest.merchantFeeRepo).updateMerchantFeecount(any(MerchantFeeBean.class));
    }
}
