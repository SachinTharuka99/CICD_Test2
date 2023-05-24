/**
 * Author :
 * Date : 4/8/2023
 * Time : 3:57 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.EODFileProcessingEngineProducerRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.StatusVarList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.epic.cms.util.LogManager.errorLogger;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class EODFileProcessingEngineMainService {

    @Autowired
    EODFileProcessingEngineProducerRepo producerRepo;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    LogManager logManager;
    @Autowired
    StatusVarList status;
    @Autowired
    KafkaMessageUpdator kafkaMessageUpdator;

    public void startProcess() throws Exception {
        HashMap<String, List<String>> fileMap = new HashMap<>();
        try {
            //String uniqueId = generateUniqueId();
            //get processing pending all input files
            fileMap = producerRepo.getAllProcessingPendingFiles();
            this.startFileProcessingEngineScheduler(fileMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void startProcess(String fileType, String fileId) throws Exception {
        HashMap<String, List<String>> fileMap = new HashMap<>();
        try {
            //String uniqueId = generateUniqueId();
            fileMap.put(fileType, Arrays.asList(fileId));
            this.startFileProcessingEngineScheduler(fileMap);
        } catch (Exception ex) {

        }
    }

    public synchronized void startFileProcessingEngineScheduler(HashMap<String, List<String>> fileMap) throws Exception {
        StringBuilder selectQueryBuilder = new StringBuilder();
        StringBuilder updateQueryBuilder = new StringBuilder();
        String tableName = null;
        String fileStatus;
        int processId = 0;

        try {
            for (Map.Entry<String, List<String>> entry : fileMap.entrySet()) {
                try {
                    selectQueryBuilder.append("SELECT STATUS FROM ");
                    updateQueryBuilder.append("UPDATE ");

                    String fileType = entry.getKey();
                    System.out.println("fileType" + fileType);

                    switch (fileType) {
                        case "VISA":
                            tableName = "EODVISAFILE";
                            processId = Configurations.PROCESS_ID_VISA_BASEII_CLEARING;
                            break;
                        case "MASTER":
                            tableName = "EODMASTERFILE";
                            processId = Configurations.PROCESS_ID_MASTER_CLEARING;
                            break;
                        case "ATM":
                            tableName = "EODATMFILE";
                            processId = Configurations.PROCESS_ATM_FILE_VALIDATE;
                            break;
                        case "PAYMENT":
                            tableName = "EODPAYMENTFILE";
                            processId = Configurations.PROCESS_PAYMENT_FILE_VALIDATE;
                            break;
                    }
                    selectQueryBuilder.append(tableName).append(" WHERE FILEID=?");
                    updateQueryBuilder.append(tableName).append(" SET STATUS=? WHERE FILEID=?");

                    //get process bean from process id
                    ProcessBean processBean = commonRepo.getProcessDetails(processId);
                    for (String fileId : entry.getValue()) {
                        System.out.println("fileid" + fileId);
                        fileStatus = producerRepo.getFileStatus(selectQueryBuilder.toString(), fileId);
                        System.out.println("fileStatus" + fileStatus);
                        if (fileStatus != null && !fileStatus.isEmpty() && fileStatus.equals(status.getINITIAL_STATUS())) {
                            System.out.println("--can process--");
                            kafkaMessageUpdator.producerWithNoReturn(fileId, processBean.getKafkaTopic());
                            //can process, update file status to INPR
                            producerRepo.updateFileStatus(updateQueryBuilder.toString(), fileId, status.getINPROGRESS_STATUS());
                        } else {
                            System.out.println("file not found");
                        }
                    }

                    //clear stringbuilders
                    selectQueryBuilder.setLength(0);
                    selectQueryBuilder.setLength(0);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
