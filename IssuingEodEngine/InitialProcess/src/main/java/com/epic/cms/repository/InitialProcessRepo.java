package com.epic.cms.repository;

import com.epic.cms.dao.InitialProcessDao;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.model.rowmapper.ProcessBeanRowMapper;
import com.epic.cms.util.QueryParametersList;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import static com.epic.cms.util.LogManager.errorLogger;


@Repository
public class InitialProcessRepo implements InitialProcessDao {
    public InitialProcessRepo(JdbcTemplate onlineJdbcTemplate,JdbcTemplate backendJdbcTemplate,
                              QueryParametersList queryParametersList,StatusVarList statusList ) {
        this.onlineJdbcTemplate = onlineJdbcTemplate;
        this.backendJdbcTemplate = backendJdbcTemplate;
        this.queryParametersList = queryParametersList;
        this.statusList = statusList;

    }

    @Autowired
    private final JdbcTemplate backendJdbcTemplate;

    @Autowired
    @Qualifier("onlineJdbcTemplate")
    private final JdbcTemplate onlineJdbcTemplate;

    @Autowired
    QueryParametersList queryParametersList;

    @Autowired
    StatusVarList statusList;

    @Override
    public int swapEodCardBalance() throws Exception {
        int count = 0;
        try {
            count = backendJdbcTemplate.update(queryParametersList.getInitialUpdateSwapEodCardBalance());
        } catch (Exception e) {
            throw e;
        }
        return count;
    }
    @Override
    public boolean insertIntoOpeningAccBal() throws Exception {

        boolean status = true;
        int rs = 0;
        int npstatus = 1;
        String accountNo ="212140006009";
        String sql = "update cardaccount set npstatus =  ?  where  accountno = ? ";
        try {
            rs= backendJdbcTemplate.update(queryParametersList.getInitialUpdateInsertIntoAccountBalance());
//            if(rs > 0){
//                throw new NullPointerException();
//            }
        } catch (Exception e) {
            status = false;
            throw e;
        }
        if (rs > 0) {
            status = true;
        } else {
            status = false;
        }
        return status;
    }

    /**
     * @param tableName
     */
    @Override
    public void setResetCapsLimit(String tableName) {
        try{
           String sql = queryParametersList.getInitialUpdateSetResetCapsLimit().replace("tableName", tableName);

            onlineJdbcTemplate.update(sql,statusList.getONLINE_PINTRYEXCEED_STATUS(),0,0,0,0,0);
        }catch (Exception e){
            throw e;
        }
    }

    /**
     * @param tableName
     */
    @Override
    public void setResetCapsLimitAccount(String tableName) {
        try {
            String sql = queryParametersList.getInitialUpdateSetResetCapsLimitAccount().replace("tableName", tableName);
            onlineJdbcTemplate.update(sql,0,0,0,0);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public ProcessBean getProcessDetails(int processId) {
        ProcessBean processDetails = new ProcessBean();
        try {
            processDetails = backendJdbcTemplate.queryForObject(queryParametersList.getCommonSelectGetProcessDetails(), new ProcessBeanRowMapper(),processId);
        }catch (Exception e){
            errorLogger.error(String.valueOf(e));
        }
        return processDetails;
    }
}
