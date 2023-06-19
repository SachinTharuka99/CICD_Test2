package com.epic.cms.repository;

import com.epic.cms.dao.AcqTxnUpdateDao;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Repository
public class AcqTxnUpdateRepo implements AcqTxnUpdateDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Autowired
    QueryParametersList query;

    @Autowired
    StatusVarList status;


    @Override
    public String getForexPercentage() throws Exception {
        String forexRate = "0";

        try {
            forexRate= backendJdbcTemplate.queryForObject(query.getAcqTxnUpdate_getForexPercentage(),String.class);
        }catch (Exception e){
            throw e;
        }
        return forexRate;
    }

    @Override
    public String getFuelSurchargeRatePercentage() throws Exception {
        String fuelSurchargeRate = "0";

        try {
            fuelSurchargeRate= backendJdbcTemplate.queryForObject(query.getAcqTxnUpdate_getFuelSurchargeRatePercentage(),String.class);
        }catch (Exception e){
            throw e;
        }
        return fuelSurchargeRate;
    }

    @Override
    public List<String> getFuelMccList() throws Exception {
        List<String> mccList = new ArrayList<>();
        try {
            List<String> data= backendJdbcTemplate.queryForList(query.getAcqTxnUpdate_getFuelMccList(),String.class);

            for (String s : data){
                String[] mccArrList = s.split("\\|");
                mccList = Arrays.asList(mccArrList);
            }
            return mccList;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public HashMap<String, String> getFinancialStatus() throws Exception {
        HashMap<String, String> visaTxnFields = new HashMap<>();
        HashMap<String, String> result = new HashMap<>();
        try {
            result = backendJdbcTemplate.query(query.getAcqTxnUpdate_getFinancialStatus(),
                    (ResultSet rs) -> {
                        while (rs.next()) {
                            visaTxnFields.put(rs.getString("TRANSACTIONCODE"), rs.getString("FINANCIALSTATUS"));
                        }
                        return visaTxnFields;
                    });

        }catch (Exception e){
            throw e;
        }
        return result;
    }
}
