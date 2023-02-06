package com.epic.cms.dao;

public interface AutoSettlementDao {
    int updateAutoSettlementWithPayments() throws Exception;

    void getUnsuccessfullStandingInstructionFeeEligibleCards()  throws Exception;

    String[] generatePartialAutoSettlementFile(String fileDirectory, String fileName, String sequence, String fieldDelimeter) throws Exception;

    String[] generateAutoSettlementFile(String fileDirectory, String fileName, String sequence, String fieldDelimeter) throws Exception;
}
