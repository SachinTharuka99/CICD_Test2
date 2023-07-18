package com.epic.cms.service;

import com.epic.cms.model.bean.OtbBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.KnockOffRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KnockOffServiceTest {
    int capacity = 200000;
    BlockingQueue<Integer> successCount = new ArrayBlockingQueue<Integer>(capacity);
    BlockingQueue<Integer> failCount = new ArrayBlockingQueue<Integer>(capacity);
    private KnockOffService knockOffServiceUnderTest;

    @BeforeEach
    void setUp() {
        knockOffServiceUnderTest = new KnockOffService();
        knockOffServiceUnderTest.logManager = mock(LogManager.class);
        knockOffServiceUnderTest.knockOffRepo = mock(KnockOffRepo.class);
        knockOffServiceUnderTest.statusVarList = mock(StatusVarList.class);
        knockOffServiceUnderTest.commonRepo = mock(CommonRepo.class);
    }

    @Test
    void testKnockOff() throws Exception {

        final OtbBean custAccBean = new OtbBean();
        custAccBean.setAccountnumber("accountnumber");
        custAccBean.setCardnumber(new StringBuffer("4890118864436725"));
        custAccBean.setCustomerid("customerid");
        custAccBean.setOtbcredit(0.0);
        custAccBean.setOtbcash(0.0);
        custAccBean.setFinacialcharges(2.0);
        custAccBean.setCumcashadvance(2.0);
        custAccBean.setCumtransactions(0.0);
        custAccBean.setTmpcredit(0.0);
        custAccBean.setTmpcash(0.0);
        custAccBean.setPayment(2.0);
        custAccBean.setIsPrimary("isPrimary");
        custAccBean.setId(0);

        final OtbBean mainCardBean = new OtbBean();
        mainCardBean.setAccountnumber("accountnumber");
        mainCardBean.setCardnumber(new StringBuffer("4890118864436725"));
        mainCardBean.setCustomerid("customerid");
        mainCardBean.setOtbcredit(0.0);
        mainCardBean.setOtbcash(0.0);
        mainCardBean.setFinacialcharges(2.0);
        mainCardBean.setCumcashadvance(2.0);
        mainCardBean.setCumtransactions(0.0);
        mainCardBean.setTmpcredit(0.0);
        mainCardBean.setTmpcash(0.0);
        mainCardBean.setPayment(2.0);
        mainCardBean.setIsPrimary("isPrimary");
        mainCardBean.setId(0);

        final OtbBean eomBean = new OtbBean();
        eomBean.setAccountnumber("accountnumber");
        eomBean.setCardnumber(new StringBuffer("4890118864436725"));
        eomBean.setCustomerid("customerid");
        eomBean.setOtbcredit(0.0);
        eomBean.setOtbcash(0.0);
        eomBean.setFinacialcharges(2.0);
        eomBean.setCumcashadvance(2.0);
        eomBean.setCumtransactions(0.0);
        eomBean.setTmpcredit(0.0);
        eomBean.setTmpcash(0.0);
        eomBean.setPayment(2.0);
        eomBean.setIsPrimary("isPrimary");
        eomBean.setId(0);

        final OtbBean eodBean = new OtbBean();
        eodBean.setAccountnumber("accountnumber");
        eodBean.setCardnumber(new StringBuffer("4890118864436725"));
        eodBean.setCustomerid("customerid");
        eodBean.setOtbcredit(0.0);
        eodBean.setOtbcash(0.0);
        eodBean.setFinacialcharges(2.0);
        eodBean.setCumcashadvance(2.0);
        eodBean.setCumtransactions(0.0);
        eodBean.setTmpcredit(0.0);
        eodBean.setTmpcash(0.0);
        eodBean.setPayment(2.0);
        eodBean.setIsPrimary("isPrimary");
        eodBean.setId(0);

        final OtbBean supCardBean = new OtbBean();
        supCardBean.setAccountnumber("accountnumber");
        supCardBean.setCardnumber(new StringBuffer("4890118864436725"));
        supCardBean.setCustomerid("customerid");
        supCardBean.setOtbcredit(0.0);
        supCardBean.setOtbcash(0.0);
        supCardBean.setFinacialcharges(2.0);
        supCardBean.setCumcashadvance(2.0);
        supCardBean.setCumtransactions(0.0);
        supCardBean.setTmpcredit(0.0);
        supCardBean.setTmpcash(0.0);
        supCardBean.setPayment(2.0);
        supCardBean.setIsPrimary("isPrimary");
        supCardBean.setId(0);

        final ArrayList<OtbBean> cardList = new ArrayList<OtbBean>();
        cardList.add(custAccBean);

        final ArrayList<OtbBean> paymentList = new ArrayList<OtbBean>();
        paymentList.add(supCardBean);

        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        Configurations.EOD_ID = 12112;
        Configurations.NO_STATUS = "isPrimary";

       when(knockOffServiceUnderTest.knockOffRepo.getKnockOffCardList(any(),any())).thenReturn(cardList);
       when(knockOffServiceUnderTest.knockOffRepo.getMainCard(any())).thenReturn(mainCardBean);
       when(knockOffServiceUnderTest.knockOffRepo.getPaymentList(any())).thenReturn(paymentList);
       when(knockOffServiceUnderTest.knockOffRepo.getEomKnockOffAmount(any())).thenReturn(eomBean);
       when(knockOffServiceUnderTest.knockOffRepo.getEodKnockOffAmount(any())).thenReturn(eodBean);
       when(knockOffServiceUnderTest.statusVarList.getEOD_PENDING_STATUS()).thenReturn("EPEN");
       when(knockOffServiceUnderTest.statusVarList.getINITIAL_STATUS()).thenReturn("INIT");
       when(knockOffServiceUnderTest.statusVarList.getINITIAL_STATUS()).thenReturn("INIT");
       when(knockOffServiceUnderTest.statusVarList.getEOD_DONE_STATUS()).thenReturn("EDON");
       when(knockOffServiceUnderTest.knockOffRepo.updateEodPayment(12,20.0,20.0,20.0,20.0,20.0,20.0,20.0,"EPEN")).thenReturn(1);
       when(knockOffServiceUnderTest.knockOffRepo.updateCardOtb(supCardBean)).thenReturn(1);
       when(knockOffServiceUnderTest.knockOffRepo.updateEodClosingBalance(new StringBuffer("4890118864436725"),2.0)).thenReturn(1);
       when(knockOffServiceUnderTest.knockOffRepo.updateEOMCARDBALANCE(eomBean)).thenReturn(1);
       when(knockOffServiceUnderTest.knockOffRepo.updateCardComp(mainCardBean)).thenReturn(1);
       when(knockOffServiceUnderTest.knockOffRepo.updateAccountOtb(custAccBean)).thenReturn(1);
       when(knockOffServiceUnderTest.knockOffRepo.updateCustomerOtb(custAccBean)).thenReturn(1);
       when(knockOffServiceUnderTest.knockOffRepo.OnlineupdateAccountOtb(custAccBean)).thenReturn(1);
       when(knockOffServiceUnderTest.knockOffRepo.OnlineupdateCustomerOtb(custAccBean)).thenReturn(1);

        // Run the test
       knockOffServiceUnderTest.knockOff(custAccBean, cardList, paymentList, successCount, failCount);

        // Verify the results
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).getKnockOffCardList(any(),any());
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).getMainCard(any());
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).getPaymentList(any());
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).getEomKnockOffAmount(any());
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).getEodKnockOffAmount(any());
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).updateEodPayment(0, 2.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "EDON");
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).updateCardOtb(any(OtbBean.class));
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).updateCardComp(any(OtbBean.class));
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).updateAccountOtb(any(OtbBean.class));
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).updateCustomerOtb(any(OtbBean.class));
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).OnlineupdateAccountOtb(any(OtbBean.class));
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).OnlineupdateCustomerOtb(any(OtbBean.class));
        verify(knockOffServiceUnderTest.knockOffRepo,times(2)).updateEodClosingBalance(any(StringBuffer.class),any(Double.class));
        verify(knockOffServiceUnderTest.knockOffRepo,times(1)).updateEOMCARDBALANCE(any(OtbBean.class));

    }
}
