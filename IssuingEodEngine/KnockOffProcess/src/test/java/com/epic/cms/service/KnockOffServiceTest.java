/**
 * Author : rasintha_j
 * Date : 03/11/2022
 * Time : 12:55 PM
 * Project Name : ecms_eod_engine
 */
package com.epic.cms.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class KnockOffServiceTest {

    /*private KnockOffService knockOffServiceUnderTest;

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
        // Setup
        final OtbBean paymentBean = new OtbBean();
        paymentBean.setAccountnumber("accountnumber");
        paymentBean.setCardnumber(new StringBuffer("value"));
        paymentBean.setCustomerid("customerid");
        paymentBean.setOtbcredit(0.0);
        paymentBean.setOtbcash(0.0);
        paymentBean.setFinacialcharges(0.0);
        paymentBean.setCumpayment(0.0);
        paymentBean.setCumcashadvance(0.0);
        paymentBean.setCumtransactions(0.0);
        paymentBean.setTmpcredit(0.0);
        paymentBean.setTmpcash(0.0);
        paymentBean.setTxnAmount(0.0);
        paymentBean.setPayment(0.0);
        paymentBean.setIsPrimary("Yes");
        paymentBean.setId(0);

        final OtbBean eomBean = new OtbBean();
        eomBean.setAccountnumber("accountnumber");
        eomBean.setCardnumber(new StringBuffer("value"));
        eomBean.setCustomerid("customerid");
        eomBean.setOtbcredit(0.0);
        eomBean.setOtbcash(0.0);
        eomBean.setFinacialcharges(0.0);
        eomBean.setCumpayment(0.0);
        eomBean.setCumcashadvance(0.0);
        eomBean.setCumtransactions(0.0);
        eomBean.setTmpcredit(0.0);
        eomBean.setTmpcash(0.0);
        eomBean.setTxnAmount(0.0);
        eomBean.setPayment(0.0);
        eomBean.setIsPrimary("isPrimary");
        eomBean.setId(0);

        final OtbBean eodBean = new OtbBean();
        eodBean.setAccountnumber("accountnumber");
        eodBean.setCardnumber(new StringBuffer("value"));
        eodBean.setCustomerid("customerid");
        eodBean.setOtbcredit(0.0);
        eodBean.setOtbcash(0.0);
        eodBean.setFinacialcharges(0.0);
        eodBean.setCumpayment(0.0);
        eodBean.setCumcashadvance(0.0);
        eodBean.setCumtransactions(0.0);
        eodBean.setTmpcredit(0.0);
        eodBean.setTmpcash(0.0);
        eodBean.setTxnAmount(0.0);
        eodBean.setPayment(0.0);
        eodBean.setIsPrimary("isPrimary");
        eodBean.setId(0);

        final OtbBean supCardBean = new OtbBean();
        supCardBean.setAccountnumber("accountnumber");
        supCardBean.setCardnumber(new StringBuffer("4380431766518012"));
        supCardBean.setCustomerid("customerid");
        supCardBean.setOtbcredit(0.0);
        supCardBean.setOtbcash(0.0);
        supCardBean.setFinacialcharges(0.0);
        supCardBean.setCumpayment(0.0);
        supCardBean.setCumcashadvance(0.0);
        supCardBean.setCumtransactions(0.0);
        supCardBean.setTmpcredit(0.0);
        supCardBean.setTmpcash(0.0);
        supCardBean.setTxnAmount(0.0);
        supCardBean.setPayment(0.0);
        supCardBean.setIsPrimary("isPrimary");
        supCardBean.setId(0);

        final OtbBean custAccBean = new OtbBean();
        custAccBean.setAccountnumber("accountnumber");
        custAccBean.setCardnumber(new StringBuffer("value"));
        custAccBean.setCustomerid("customerid");
        custAccBean.setOtbcredit(0.0);
        custAccBean.setOtbcash(0.0);
        custAccBean.setFinacialcharges(0.0);
        custAccBean.setCumpayment(0.0);
        custAccBean.setCumcashadvance(0.0);
        custAccBean.setCumtransactions(0.0);
        custAccBean.setTmpcredit(0.0);
        custAccBean.setTmpcash(0.0);
        custAccBean.setTxnAmount(0.0);
        custAccBean.setPayment(0.0);
        custAccBean.setIsPrimary("isPrimary");
        custAccBean.setId(0);

        final OtbBean otbBean = new OtbBean();
        otbBean.setAccountnumber("accountnumber");
        otbBean.setCardnumber(new StringBuffer("value"));
        otbBean.setCustomerid("customerid");
        otbBean.setOtbcredit(0.0);
        otbBean.setOtbcash(0.0);
        otbBean.setFinacialcharges(0.0);
        otbBean.setCumpayment(0.0);
        otbBean.setCumcashadvance(0.0);
        otbBean.setCumtransactions(0.0);
        otbBean.setTmpcredit(0.0);
        otbBean.setTmpcash(0.0);
        otbBean.setTxnAmount(0.0);
        otbBean.setPayment(0.0);
        otbBean.setIsPrimary("isPrimary");
        otbBean.setId(0);
        final ArrayList<OtbBean> cardList = new ArrayList<>(List.of(otbBean));
        final ArrayList<OtbBean> paymentList = new ArrayList<>(List.of(otbBean));
        final OtbBean mainCardBean = new OtbBean();
        mainCardBean.setAccountnumber("accountnumber");
        mainCardBean.setCardnumber(new StringBuffer("4380431766518012"));
        mainCardBean.setCustomerid("customerid");
        mainCardBean.setOtbcredit(0.0);
        mainCardBean.setOtbcash(0.0);
        mainCardBean.setFinacialcharges(0.0);
        mainCardBean.setCumpayment(0.0);
        mainCardBean.setCumcashadvance(0.0);
        mainCardBean.setCumtransactions(0.0);
        mainCardBean.setTmpcredit(0.0);
        mainCardBean.setTmpcash(0.0);
        mainCardBean.setTxnAmount(0.0);
        mainCardBean.setPayment(10.0);
        mainCardBean.setIsPrimary("isPrimary");
        mainCardBean.setId(0);

        // Configure
        String maskCardNo = "438043******8012";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";
        Configurations.NO_STATUS = "Yes";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(supCardBean.getCardnumber())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(supCardBean.getCardnumber()));

            theMock.when(() -> CommonMethods.cardNumberMask(mainCardBean.getCardnumber())).thenReturn(maskCardNo);
            assertThat(maskCardNo).isEqualTo(CommonMethods.cardNumberMask(mainCardBean.getCardnumber()));
        }

        when(knockOffServiceUnderTest.statusVarList.getEOD_PENDING_STATUS()).thenReturn("EPEN");
        when(knockOffServiceUnderTest.statusVarList.getEOD_DONE_STATUS()).thenReturn("EDON");
        when(knockOffServiceUnderTest.knockOffRepo.updateEodPayment(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "EOD_DONE_STATUS")).thenReturn(0);
        when(knockOffServiceUnderTest.knockOffRepo.updateCardOtb(any(OtbBean.class))).thenReturn(0);
        when(knockOffServiceUnderTest.knockOffRepo.OnlineupdateCardOtb(any(OtbBean.class))).thenReturn(0);
        when(knockOffServiceUnderTest.knockOffRepo.updateEodClosingBalance(any(StringBuffer.class), eq(0.0))).thenReturn(0);

        // Run the test
        knockOffServiceUnderTest.knockOff(custAccBean, cardList, paymentList);

        // Verify the results
        //verify(knockOffServiceUnderTest.knockOffRepo,times(1)).updateEodPayment(anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyString());
        //verify(knockOffServiceUnderTest.knockOffRepo,times(1)).updateCardOtb(any(OtbBean.class));
        //verify(knockOffServiceUnderTest.knockOffRepo,times(1)).OnlineupdateCardOtb(any(OtbBean.class));
        //verify(knockOffServiceUnderTest.knockOffRepo,times(1)).updateEodClosingBalance(any(StringBuffer.class), anyDouble());
    }*/
}
