package com.epic.cms.service;

import com.epic.cms.model.bean.PaymentBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CollectionAndRecoveryAlertRepo;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CollectionAndRecoveryAlertServiceTest {

    private CollectionAndRecoveryAlertService collectionAndRecoveryAlertServiceUnderTest;

    @BeforeEach
    void setUp() {
        collectionAndRecoveryAlertServiceUnderTest = new CollectionAndRecoveryAlertService();
        collectionAndRecoveryAlertServiceUnderTest.logManager = mock(LogManager.class);
        collectionAndRecoveryAlertServiceUnderTest.commonRepo = mock(CommonRepo.class);
        collectionAndRecoveryAlertServiceUnderTest.collectionAndRecoveryAlertRepo = mock(
                CollectionAndRecoveryAlertRepo.class);
        collectionAndRecoveryAlertServiceUnderTest.alert = mock(AlertService.class);
    }

    @Test
    void testProcessCollectionAndRecoveryAlertService() throws Exception {
        // Setup
        final ProcessBean processBean = new ProcessBean();
        processBean.setProcessId(0);
        processBean.setProcessDes("processDes");
        processBean.setCriticalStatus(0);
        processBean.setRollBackStatus(0);
        processBean.setSheduleDate(Timestamp.valueOf(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)));
        processBean.setSheduleTime("sheduleTime");
        processBean.setFrequencyType(0);
        processBean.setContinuousFrequencyType(0);
        processBean.setContinuousFrequency(0);
        processBean.setMultiCycleStatus(0);
        processBean.setProcessCategoryId(0);
        processBean.setDependancyStatus(0);
        processBean.setRunningOnMain(0);
        processBean.setRunningOnSub(0);
        processBean.setProcessType(0);
        StringBuffer cardNumber = new StringBuffer("43804312348012");
        String value = "1";
        Configurations.X_DAYS_BEFORE_1_DUE_DATE = "1";

        final PaymentBean bean = new PaymentBean();
        bean.setCardnumber(new StringBuffer("43804312348012"));
        bean.setTraceid("123456789");

        String maskCardNo = "43804312348012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(bean.getCardnumber())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(bean.getCardnumber()));
        }

        when(collectionAndRecoveryAlertServiceUnderTest.commonRepo.getAccountNoOnCard(
                any(StringBuffer.class))).thenReturn("1233435");
        when(collectionAndRecoveryAlertServiceUnderTest.commonRepo.getTriggerEligibleStatus("X_DAYS_BEFORE_1_DUE_DATE",
                "EMAIL")).thenReturn(false);


        // Run the test
        collectionAndRecoveryAlertServiceUnderTest.processCollectionAndRecoveryAlertService(cardNumber, value, processBean);

        // Verify the results
        verify(collectionAndRecoveryAlertServiceUnderTest.collectionAndRecoveryAlertRepo, times(1)).updateAlertGenStatus(
                any(StringBuffer.class), anyString());
    }

}
