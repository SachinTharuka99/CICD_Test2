/**
 * Author : yasiru_l
 * Date : 6/30/2023
 * Time : 8:33 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.connector;

import com.epic.cms.common.FileGenProcessBuilder;
import com.epic.cms.dao.OutgoingCUPFileDao;
import com.epic.cms.model.bean.OutgoingCUPFileTransactionBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.service.OutgoingCUPFileService;
import com.epic.cms.util.CommonMethods;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OutgoingCUPFileConnector extends FileGenProcessBuilder {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    @Autowired
    LogManager logManager;
    @Autowired
    OutgoingCUPFileDao outgoingCUPFileDao;
    @Autowired
    OutgoingCUPFileService outgoingCUPFileService;

    public static ArrayList<OutgoingCUPFileTransactionBean> outgoingCUPFileDataList = null;
    int selectedTransactionCount = 0;
    @Override
    public void concreteProcess() throws Exception {

        try{
            processBean = new ProcessBean();
            processBean = commonRepo.getProcessDetails(Configurations.PROCESS_ID_OUTGOING_CUP_FILE_GEN);

            if (processBean != null) {
                Configurations.RUNNING_PROCESS_ID = Configurations.PROCESS_ID_OUTGOING_CUP_FILE_GEN;
                CommonMethods.eodDashboardProgressParametersReset();

                outgoingCUPFileDataList = outgoingCUPFileDao.getOutgoingStatementFileTransactionData();
                selectedTransactionCount = outgoingCUPFileDataList.size();
                Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS = outgoingCUPFileDataList.size();

                if (selectedTransactionCount != 0) {
                    Configurations.OUTGOING_CUP_FILE_TXN_BLOCK_FIELD_TABLE = outgoingCUPFileDao.getStatementFileBlockFields();
                    Configurations.OUTGOING_CUP_FILE_REJECT_REASON_TABLE = outgoingCUPFileDao.getOutgoingRejectReasonTable();

                    for(int i = 0;  i < outgoingCUPFileDataList.size(); i++){
                        outgoingCUPFileService.generateOutgoingCUPFile(outgoingCUPFileDataList);
                    }
                }
            }
            logInfo.info("Outgoing CUP File Generation Process Successfully");
        }catch (Exception e){
            logError.error("Exception occured while processing the transaction : " +e);
        }

    }

    @Override
    public void addSummaries() {
        summery.put("Process Name ", "Outgoing CUP File Generation");
        summery.put("Started Date ", Configurations.EOD_DATE.toString());
        summery.put("Total No of Effected File ", Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS);
        summery.put("File Success Count ", Configurations.PROCESS_SUCCESS_COUNT);
        summery.put("File Failed Count ", Configurations.PROCESS_FAILD_COUNT);
    }
}
