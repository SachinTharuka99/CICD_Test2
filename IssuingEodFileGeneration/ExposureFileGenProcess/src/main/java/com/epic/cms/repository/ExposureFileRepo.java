/**
 * Author : lahiru_p
 * Date : 11/17/2022
 * Time : 10:55 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.repository;

import com.epic.cms.dao.ExposureFileDao;
import com.epic.cms.model.bean.ExposureFileBean;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class ExposureFileRepo implements ExposureFileDao {

    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    public List<ExposureFileBean> getExposureFileDetails() throws Exception {
        List<ExposureFileBean> beanList = new ArrayList<>();
        Date date = new Date();

        String currency = "";
        try {
            //get currency
            String query = "SELECT CURRENCYALPHACODE FROM COMMONPARAMETER CP LEFT JOIN CURRENCY CUR ON CUR.CURRENCYNUMCODE=CP.BASECURRENCY ";
            currency = backendJdbcTemplate.queryForObject(query, String.class);

            //get other details and set to bean
            query = "SELECT NVL(SUM(CACC.CREDITLIMIT-CACC.OTBCREDIT),0) AS OUTSTANDING,CAPP.CIF  AS CIF,CACC.CREDITLIMIT AS CREDITLIMIT,CACC.ACCOUNTNO AS ACC_NO, " + "(CASE CACC.STATUS WHEN 'ACT' THEN 'PL' ELSE 'NPL' END) AS ACC_STATUS,C.EXPIERYDATE as MATURITYDATE " + "FROM CARDACCOUNT CACC INNER JOIN CARDAPPLICATION CAPP  ON CACC.CARDNUMBER  =CAPP.CARDNUMBER " + "INNER JOIN CARD C ON CACC.CARDNUMBER=C.MAINCARDNO " + "WHERE CAPP.CIF  IS NOT NULL GROUP BY CACC.ACCOUNTNO,CAPP.CIF,CACC.STATUS,CACC.CREDITLIMIT,C.EXPIERYDATE ";

            String finalCurrency = currency;

            beanList = backendJdbcTemplate.query(query, new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                ExposureFileBean bean = new ExposureFileBean();
                bean.setProduct(rs.getString("CIF").concat("-").concat(Configurations.EXPOSURE_FILE_PRODUCT));
                bean.setExternalRef(rs.getString("ACC_NO"));
                bean.setCapitalOutstanding(rs.getString("OUTSTANDING"));
                bean.setFacilityType(Configurations.EXPOSURE_FILE_FACILITY_TYPE);
                bean.setBranch(Configurations.EXPOSURE_FILE_BRANCH);
                bean.setCurrency(finalCurrency);
                bean.setStatus(rs.getString("ACC_STATUS"));
                bean.setMaturityDate(rs.getString("MATURITYDATE"));
                bean.setCreditLimit(rs.getString("CREDITLIMIT"));
                return bean;
            }));

        } catch (Exception e) {
            throw  e;
        }
        return beanList;
    }
}
