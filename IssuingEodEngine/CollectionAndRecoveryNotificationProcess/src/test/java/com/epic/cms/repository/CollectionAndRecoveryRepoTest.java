package com.epic.cms.repository;

import com.epic.cms.model.bean.CollectionAndRecoveryBean;
import com.epic.cms.model.rowmapper.CollectionAndRecoveryRowMapper;
import com.epic.cms.util.Configurations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionAndRecoveryRepoTest {

    @Mock
    private JdbcTemplate mockBackendJdbcTemplate;

    @InjectMocks
    private CollectionAndRecoveryRepo collectionAndRecoveryRepoUnderTest;

    @Test
    void testGetNoOfDaysOnTriggerPoint() throws Exception {
        // Setup
        when(mockBackendJdbcTemplate.queryForObject("SELECT NOOFDAYS FROM ALLOCATIONRULE  WHERE TRIGGERPOINT = ? ",
                Integer.class, "triggerPoint")).thenReturn(1);

        // Run the test
        final int result = collectionAndRecoveryRepoUnderTest.getNoOfDaysOnTriggerPoint("triggerPoint");

        // Verify the results
        assertThat(result).isEqualTo(1);
    }

    @Test
    void testGetNoOfDaysOnTriggerPoint_JdbcTemplateReturnsNull() throws Exception {
        // Setup
        when(mockBackendJdbcTemplate.queryForObject("SELECT NOOFDAYS FROM ALLOCATIONRULE  WHERE TRIGGERPOINT = ? ",
                Integer.class, "triggerPoint")).thenReturn(0);

        // Run the test
        final int result = collectionAndRecoveryRepoUnderTest.getNoOfDaysOnTriggerPoint("triggerPoint");

        // Verify the results
        assertThat(result).isEqualTo(0);
    }

    @Test
    void testGetCardListForCollectionAndRecoveryOnDueDate() throws Exception {
        // Setup
        Configurations.EOD_DATE = new Date();
        // Configure JdbcTemplate.query(...).
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("value"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");
        final ArrayList<CollectionAndRecoveryBean> collectionAndRecoveryBeans = new ArrayList<>(List.of(collectionAndRecoveryBean));
        when(mockBackendJdbcTemplate.query(
                any(),
                any(CollectionAndRecoveryRowMapper.class), any(),any())).thenReturn(collectionAndRecoveryBeans);

        // Run the test
        final ArrayList<CollectionAndRecoveryBean> result = collectionAndRecoveryRepoUnderTest.getCardListForCollectionAndRecoveryOnDueDate(
                0, 1, "lastTrigger");

        // Verify the results
        assertThat(result).isEqualTo(collectionAndRecoveryBeans);
    }

    @Test
    void testGetCardListForCollectionAndRecoveryOnStatmentDate() throws Exception {
        // Setup
        Configurations.EOD_DATE = new Date();
        // Configure JdbcTemplate.query(...).
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("value"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");
        final ArrayList<CollectionAndRecoveryBean> collectionAndRecoveryBeans = new ArrayList<>(List.of(collectionAndRecoveryBean));
        when(mockBackendJdbcTemplate.query(
                any(),any(CollectionAndRecoveryRowMapper.class), any(),any())).thenReturn(collectionAndRecoveryBeans);

        // Run the test
        final ArrayList<CollectionAndRecoveryBean> result = collectionAndRecoveryRepoUnderTest.getCardListForCollectionAndRecoveryOnStatmentDate(
                0, 1, "lastTrigger");

        // Verify the results
        assertThat(result).isEqualTo(collectionAndRecoveryBeans);
    }
/**
    @Test
    void testCheckForTriggerPoint() throws Exception {
        // Setup
        final StringBuffer cardNo = new StringBuffer("value");
        when(mockBackendJdbcTemplate.queryForObject(eq(any()), Integer.class,
                any())).thenReturn(0);

        // Run the test
        final boolean result = collectionAndRecoveryRepoUnderTest.CheckForTriggerPoint(cardNo);

        // Verify the results
        assertThat(result).isFalse();
    }

    @Test
    void testAddCardToTriggerCards() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("value"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        when(mockBackendJdbcTemplate.update(
                "INSERT INTO TRIGGERCARDS (CARDNO,LASTTRIGGERPOINT,CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER,NOTIFICATIONFLAG,LETTERGENSTATUS) VALUES(?,?,SYSDATE,SYSDATE,?,0,?)",
                "cardNo")).thenReturn(0);

        // Run the test
        collectionAndRecoveryRepoUnderTest.addCardToTriggerCards(collectionAndRecoveryBean);

        // Verify the results
        verify(mockBackendJdbcTemplate).update(
                "INSERT INTO TRIGGERCARDS (CARDNO,LASTTRIGGERPOINT,CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER,NOTIFICATIONFLAG,LETTERGENSTATUS) VALUES(?,?,SYSDATE,SYSDATE,?,0,?)",
                "cardNo");
    }

    @Test
    void testUpdateTriggerCards() throws Exception {
        // Setup
        final CollectionAndRecoveryBean collectionAndRecoveryBean = new CollectionAndRecoveryBean();
        collectionAndRecoveryBean.setCardNo(new StringBuffer("value"));
        collectionAndRecoveryBean.setDueAmount(0.0);
        collectionAndRecoveryBean.setDueDate("dueDate");
        collectionAndRecoveryBean.setLastTriger("lastTriger");

        when(mockBackendJdbcTemplate.update(
                "UPDATE TRIGGERCARDS SET LASTTRIGGERPOINT = ? ,LASTUPDATEDTIME = TO_DATE(SYSDATE, 'DD-MM-YY')  ,LASTUPDATEDUSER = ?,NOTIFICATIONFLAG = 0,LETTERGENSTATUS =? WHERE CARDNO = ?",
                "lastTriger")).thenReturn(0);

        // Run the test
        collectionAndRecoveryRepoUnderTest.updateTriggerCards(collectionAndRecoveryBean);

        // Verify the results
        verify(mockBackendJdbcTemplate).update(
                "UPDATE TRIGGERCARDS SET LASTTRIGGERPOINT = ? ,LASTUPDATEDTIME = TO_DATE(SYSDATE, 'DD-MM-YY')  ,LASTUPDATEDUSER = ?,NOTIFICATIONFLAG = 0,LETTERGENSTATUS =? WHERE CARDNO = ?",
                "lastTriger");
    }
*/
    @Test
    void testAddDetailsToCardLetterNotifyTable() {
        // Setup
        final StringBuffer cardNo = new StringBuffer("value");
        when(mockBackendJdbcTemplate.update(
                any(),any(),any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(1);

        // Run the test
        final int result = collectionAndRecoveryRepoUnderTest.addDetailsToCardLetterNotifyTable(cardNo, "nameInFull",
                "accNo", "contactNo", "email", "address", 0.0, "dueDate", "remark");

        // Verify the results
        assertThat(result).isEqualTo(1);
    }
}
