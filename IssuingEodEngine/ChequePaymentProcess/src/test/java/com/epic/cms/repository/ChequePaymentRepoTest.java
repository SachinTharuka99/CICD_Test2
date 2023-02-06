package com.epic.cms.repository;

import com.epic.cms.model.bean.ReturnChequePaymentDetailBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.StatusVarList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ChequePaymentRepoTest {
    @Mock
    private JdbcTemplate mockBackendJdbcTemplate;

    @Mock
    StatusVarList statusList;

    @Mock
    CommonRepo commonRepo;

    @Mock
    LastStatementSummaryRepo lastStatementSummaryRepo;

    @Mock
    ChequeProcessRepo chequeProcessRepo;

    @InjectMocks
    ChequePaymentRepo chequePaymentRepo;

    @Test
    void getChequePaymentsBackup() {
    }

    @Test
    @DisplayName("Test Insert Cheque Payments")
    void insertChequePayments() {
        // given - precondition or setup
        Configurations.EOD_USER = "user";
        ReturnChequePaymentDetailBean bean = new ReturnChequePaymentDetailBean();
        bean.setEodid(0);
        bean.setChequeReturnDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        bean.setCardnumber(new StringBuffer("value"));
        bean.setOldcardnumber(new StringBuffer("value"));
        bean.setMaincardno(new StringBuffer("value"));
        bean.setAmount(0.0);
        bean.setChequedate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        bean.setChequenumber("chequenumber");
        bean.setMinamount(0.0);
        bean.setForwardinterest(0.0);
        bean.setInterestrate(0.0);
        bean.setClosingbalance(0.0);
        bean.setOtbcredit(0.0);
        bean.setOtbcash(0.0);
        bean.setDelinquentclass("delinquentclass");
        bean.setCardstatus("cardstatus");
        bean.setDuedate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        bean.setStatementstarteodid(0);
        bean.setStatementendeodid(0);
        bean.setTraceid("traceid");
        bean.setSeqNo("seqNo");
        bean.setTotalNetBalanceForCard(0.0);
        bean.setNdia(0);
        /**
         * when - action or behaviour that we are going test
         * then - verify the result or output using assert statements
         */
        when(mockBackendJdbcTemplate.update(any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),any(),
                any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(1);
        assertEquals(1, chequePaymentRepo.insertChequePayments(bean));
    }

    @Test
    @DisplayName("Test Update Cheque Payments")
    void updateChequePayment() throws Exception {
        // given - precondition or setup
        Configurations.EOD_USER = "user";
        ReturnChequePaymentDetailBean bean = new ReturnChequePaymentDetailBean();
        bean.setEodid(0);
        bean.setChequeReturnDate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        bean.setCardnumber(new StringBuffer("value"));
        bean.setOldcardnumber(new StringBuffer("value"));
        bean.setMaincardno(new StringBuffer("value"));
        bean.setAmount(0.0);
        bean.setChequedate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        bean.setChequenumber("chequenumber");
        bean.setMinamount(0.0);
        bean.setForwardinterest(0.0);
        bean.setInterestrate(0.0);
        bean.setClosingbalance(0.0);
        bean.setOtbcredit(0.0);
        bean.setOtbcash(0.0);
        bean.setDelinquentclass("delinquentclass");
        bean.setCardstatus("cardstatus");
        bean.setDuedate(new GregorianCalendar(2020, Calendar.JANUARY, 1).getTime());
        bean.setStatementstarteodid(0);
        bean.setStatementendeodid(0);
        bean.setTraceid("traceid");
        bean.setSeqNo("seqNo");
        bean.setTotalNetBalanceForCard(0.0);
        bean.setNdia(0);
        /**
         * when - action or behaviour that we are going test
         * then - verify the result or output using assert statements
         */
        when(mockBackendJdbcTemplate.update(any(),any(),any(),any(),any(),any())).thenReturn(1);
        assertEquals(1, chequePaymentRepo.updateChequePayment(bean));
    }
}