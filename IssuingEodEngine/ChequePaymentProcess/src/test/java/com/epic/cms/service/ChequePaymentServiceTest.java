package com.epic.cms.service;

import com.epic.cms.model.bean.ReturnChequePaymentDetailBean;
import com.epic.cms.repository.ChequePaymentRepo;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ChequePaymentServiceTest {

    private ChequePaymentService chequePaymentServiceUnderTest;

    @BeforeEach
    void setUp(){
        chequePaymentServiceUnderTest = new ChequePaymentService();
        chequePaymentServiceUnderTest.chequePaymentRepo = mock(ChequePaymentRepo.class);
        chequePaymentServiceUnderTest.logManager = mock(LogManager.class);
        chequePaymentServiceUnderTest.statusList = mock(StatusVarList.class);

        // Configure
        String maskCardNo = "value";
        Configurations.START_INDEX = 6;
        Configurations.END_INDEX =12;
        Configurations.PATTERN_CHAR = "*";

        try (MockedStatic<CommonMethods> theMock = Mockito.mockStatic(CommonMethods.class)) {
            theMock.when(() -> CommonMethods.cardNumberMask(new StringBuffer("value")))
                    .thenReturn(maskCardNo);
        }
    }

    @Test
    void test_processChequePayment() throws Exception {
        // Setup
        final ReturnChequePaymentDetailBean bean = new ReturnChequePaymentDetailBean();
        bean.setId(0);
        bean.setEodid(0);
        bean.setChequeReturnDate(new Date());
        bean.setCardnumber(new StringBuffer("value"));
        bean.setOldcardnumber(new StringBuffer("value"));
        bean.setMaincardno(new StringBuffer("value"));
        bean.setAccountNo("accountNo");
        bean.setCustomerid("customerid");
        bean.setAmount(0.0);
        bean.setChequedate(new Date());
        bean.setChequenumber("chequenumber");
        bean.setMinamount(0.0);
        bean.setForwardinterest(0.0);
        bean.setInterestrate(0.0);
        bean.setClosingbalance(0.0);

        //Configure
        when(chequePaymentServiceUnderTest.chequePaymentRepo.updateChequePayment(bean)).thenReturn(0);

        //run the test
        chequePaymentServiceUnderTest.processChequePayment(bean);

        //verify the result
        verify(chequePaymentServiceUnderTest.chequePaymentRepo).insertChequePayments(
                any(ReturnChequePaymentDetailBean.class));
    }

    @Test
    void test_processChequePayment_UpdateChequePaymentThrowsException() throws Exception {
        // Setup
        final ReturnChequePaymentDetailBean bean = new ReturnChequePaymentDetailBean();
        bean.setId(0);
        bean.setEodid(0);
        bean.setChequeReturnDate(new Date());
        bean.setCardnumber(new StringBuffer("4380431766518012"));
        bean.setOldcardnumber(new StringBuffer("4380431766518012"));
        bean.setMaincardno(new StringBuffer("4380431766518012"));
        bean.setAccountNo("accountNo");
        bean.setCustomerid("customerid");
        bean.setAmount(0.0);
        bean.setChequedate(new Date());
        bean.setChequenumber("chequenumber");
        bean.setMinamount(0.0);
        bean.setForwardinterest(0.0);
        bean.setInterestrate(0.0);
        bean.setClosingbalance(0.0);

        //Configure
        when(chequePaymentServiceUnderTest.chequePaymentRepo.updateChequePayment(bean)).thenReturn(1);

        //run the test
        chequePaymentServiceUnderTest.processChequePayment(bean);

        //verify the result
        verify(chequePaymentServiceUnderTest.chequePaymentRepo).insertChequePayments(
                any(ReturnChequePaymentDetailBean.class));
    }
}