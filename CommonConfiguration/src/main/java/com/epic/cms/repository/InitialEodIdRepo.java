/**
 * Author : rasintha_j
 * Date : 4/11/2023
 * Time : 10:19 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.repository;

import com.epic.cms.dao.InitialEodIdDao;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.CreateEodId;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.epic.cms.util.LogManager.errorLoggerCOM;
import static com.epic.cms.util.LogManager.infoLoggerCOM;

@Repository
public class InitialEodIdRepo implements InitialEodIdDao {

    @Autowired
    StatusVarList varList;
    @Autowired
    private JdbcTemplate backendJdbcTemplate;

    @Override
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW)
    public void setInitialEodId() throws Exception {
        int eodINPR = 0;
        try {
            String query = "SELECT EODID FROM EOD WHERE STATUS = ? OR STATUS = ?";

            Configurations.EOD_ID = backendJdbcTemplate.queryForObject(query, Integer.class, varList.getINITIAL_STATUS(), varList.getERROR_STATUS());
            System.out.println("After Eod Id : " + Configurations.EOD_ID);

            if (Configurations.EOD_ID == 0) {
                System.out.println("---------------------->> No available EOD ID to start....");

                eodINPR = backendJdbcTemplate.queryForObject(query, Integer.class, varList.getINPROGRESS_STATUS(), varList.getINPROGRESS_STATUS());

                if (eodINPR != 0) {
                    System.out.println("Currently EOD ID: [" + eodINPR + "] is in progress..");
                } else {
                    enterFirstEODId();
                }
            }
        } catch (EmptyResultDataAccessException e) {
            eodINPR = 0;
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void enterFirstEODId() throws Exception {
        try {
            LocalDate locDate = Configurations.initDate;//2022410
            Date newEODDate = new org.joda.time.DateTime().withDate(locDate).toDate();
            CreateEodId createFirstEOD = new CreateEodId();
            String strEOD = createFirstEOD.getDate(newEODDate) + "00";

            if (!strEOD.isEmpty()) {
                boolean insertFlag = false;
                String query = "INSERT INTO EOD (EODID,STATUS,NEXTEODSTARTTIME,NEXTSUBEODSTARTTIME,SUBEODSTATUS,CREATEDTIME,LASTUPDATEDTIME,LASTUPDATEDUSER,NOOFSUCCESSPROCESS,NOOFERRORPAROCESS) VALUES (?,?,null,null,null,SYSDATE,SYSDATE,?,0,0)";

                int update = backendJdbcTemplate.update(query, strEOD, varList.getINITIAL_STATUS(), Configurations.EOD_USER);

                if (update == 1) {
                    insertFlag = true;
                }
                if (insertFlag) {
                    Configurations.EOD_ID = Integer.parseInt(strEOD);
                }
            }
        } catch (Exception e) {
            if (e.getMessage().contains("ORA-00001")) {
                System.out.println("EOD Already exists for the current date. Please enter a different date");
            }
            if (e.getMessage().contains("duplicate entry")) {
                System.out.println("EOD Already exists for the current date. Please enter a different date");
            }
            enterFirstEODId();
            throw e;
        }
    }
}
