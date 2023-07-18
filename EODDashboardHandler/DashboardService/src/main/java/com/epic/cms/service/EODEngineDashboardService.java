/**
 * Author : rasintha_j
 * Date : 3/18/2023
 * Time : 6:40 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.*;
import com.epic.cms.model.entity.*;
import com.epic.cms.repository.*;
import com.epic.cms.util.Configurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EODEngineDashboardService {

    public Long dashboardCurrentEodId = 0L;
    @Autowired
    EodIdInfoRepo eodIdInfoRepo;
    @Autowired
    ProcessSummeryRepo processSummeryRepo;
    @Autowired
    RecAtmFileInvalidRepo atmFileInvalidRepo;
    @Autowired
    RecPaymentFileInvalidRepo paymentFileInvalidRepo;
    @Autowired
    EodErrorMerchantListRepo eodErrorMerchantListRepo;
    @Autowired
    EodErrorCardListRepo eodErrorCardListRepo;
    @Autowired
    EodProcessFlowRepo eodProcessFlowRepo;
    @Autowired
    EodOutputFileRepo eodOutputFileRepo;
    @Autowired
    StatementGenSummeryListRepo genSummeryListRepo;
    @Autowired
    EodAtmInputFileRepo eodAtmInputFileRepo;
    @Autowired
    EodMasterInputFileRepo eodMasterInputFileRepo;
    @Autowired
    EodVisaInputFileRepo eodVisaInputFileRepo;
    @Autowired
    EodPaymentInputFileRepo eodPaymentInputFileRepo;
    @Autowired
    FileProcessingSummeryListRepo processingSummeryListRepo;
    @Autowired
    ProcessFailSuccessCountRepo processFailSuccessCountRepo;
    @Autowired
    CommonRepo commonRepo;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private static final Logger logError = LoggerFactory.getLogger("logError");


    public EodBean getEodInfoList(Long eodId) {
        EodBean eodBean = new EodBean();
        try {
            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }
            Optional<EOD> eodInfo = eodIdInfoRepo.findById(eodId);

            //int count1 = eodProcessFlowRepo.countByPROCESSCATEGORYIDNotIn(Collections.singletonList(90));
            int engineCount = eodProcessFlowRepo.countBySTATUSIn(List.of("ACT"));
            SimpleDateFormat outputFormatter = new SimpleDateFormat("dd-MMM-yy hh.mm a");

            List<FileGenFailSuccessCountBean> fileGenSummary = processFailSuccessCountRepo.findFileGenSummary(eodId, Configurations.EOD_FILE_GENERATION);
            List<FileProcessFailSuccessCountBean> fileProcessSummary = processFailSuccessCountRepo.findFileProcessSummary(eodId, Configurations.EOD_FILE_PROCESSING);

            eodInfo.ifPresentOrElse(eod -> {
                eodBean.setEodId(eod.getEODID());

                // Format startTime
                Date startTime = eod.getSTARTTIME();
                String formattedStartTime = outputFormatter.format(startTime);
                eodBean.setStartTime(formattedStartTime);

                // Format endTime
                Date endTime = eod.getENDTIME();
                String formattedEndTime = outputFormatter.format(endTime);
                eodBean.setEndTime(formattedEndTime);

                eodBean.setStatus(eod.getSTATUS());
                eodBean.setFileGenStatus(eod.getFILEGENERATIONSTATUS());
                eodBean.setSubEodStatus(eod.getSUBEODSTATUS());
                eodBean.setEngineNoOfSuccessProcess(eod.getNOOFSUCCESSPROCESS());
                eodBean.setEngineNoOfErrorProcess(eod.getNOOFERRORPAROCESS());
                eodBean.setEnginTotalProcessCount(engineCount);
            }
            , () -> {
                logError.error("EOD not found for ID: " + eodBean.getEodId());
            });

            fileGenSummary.forEach(eod -> {
               eodBean.setFileProcessNoOfSuccessProcess(eod.getFileGenNoOfSuccessProcess());
                eodBean.setFileProcessNoOfErrorProcess(eod.getFileGenNoOfErrorProcess());
            });

            fileProcessSummary.forEach(eod -> {
                eodBean.setFileProcessNoOfSuccessProcess(eod.getFileProcessNoOfSuccessProcess());
                eodBean.setFileProcessNoOfErrorProcess(eod.getFileProcessNoOfErrorProcess());
            });

        } catch (Exception e) {
            throw e;
        }
        return eodBean;
    }

    public NextRunningEodBean getNextRunningEodId() {
        NextRunningEodBean nextRunningEodBean = new NextRunningEodBean();
        try {
            nextRunningEodBean.setEodId(eodIdInfoRepo.findByNextRunnindEodId());

        } catch (Exception e) {
            throw e;
        }
        return nextRunningEodBean;
    }

    public void getCurrentDashboardEodId() {
        try {
            List<EOD> byEODIDDesc = eodIdInfoRepo.findAllByOrderByEODIDDesc();
            dashboardCurrentEodId = byEODIDDesc.stream()
                    .filter(eod -> eod.getSTATUS().equals("INPR"))
                    .map(EOD::getEODID)
                    .findFirst()
                    .orElseGet(() -> byEODIDDesc.stream()
                            .skip(1)
                            .map(EOD::getEODID)
                            .findFirst()
                            .orElse(0L));
        } catch (Exception e) {
            throw e;
        }
    }

    public List<ProcessSummeryBean> getEodProcessSummeryList(Long eodID) {
        try {
            if (eodID == 0) {
                eodID = dashboardCurrentEodId;
            }
            return processSummeryRepo.findProcessSummeryListById(eodID, Configurations.EOD_ENGINE);
        } catch (Exception e) {
            throw e;
        }
    }

    public DataTableBean getEodInvalidTransactionList(RequestBean requestBean, Long eodId) {
        List<Object> invalidTransactionBeanList = new ArrayList<>();
        DataTableBean dataTableBean = new DataTableBean();
        try {

            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            //int fixSize = requestBean.getSize() / 2;
            Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize(), Sort.by("FILEID").ascending());
            //Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize(), Sort.by("FILEID").ascending());

            Page<RECATMFILEINVALID> recAtmFileInvalidList = atmFileInvalidRepo.findRECATMFILEINVALIDByEODID(eodId, paging);
            Page<RECPAYMENTFILEINVALID> recPaymentFileInvalidList = paymentFileInvalidRepo.findRECPAYMENTFILEINVALIDByEODID(eodId, paging);

            int content = recAtmFileInvalidList.getContent().size() + recPaymentFileInvalidList.getContent().size();

            if (content > 0) {
                dataTableBean.setCount(recAtmFileInvalidList.getTotalElements() + recPaymentFileInvalidList.getTotalElements());
                dataTableBean.setPagecount(recAtmFileInvalidList.getTotalPages() + recPaymentFileInvalidList.getTotalPages());

                recAtmFileInvalidList.getContent().forEach(eod -> {
                    EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
                    eodInvalidTransactionBean.setEodId(eod.getEODID());
                    eodInvalidTransactionBean.setFileId(eod.getFILEID());
                    eodInvalidTransactionBean.setFileType("ATM");
                    eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
                    eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

                    invalidTransactionBeanList.add(eodInvalidTransactionBean);
                });

                recPaymentFileInvalidList.getContent().forEach(eod -> {
                    EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
                    eodInvalidTransactionBean.setEodId(eod.getEODID());
                    eodInvalidTransactionBean.setFileId(eod.getFILEID());
                    eodInvalidTransactionBean.setFileType("PAYMENT");
                    eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
                    eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

                    invalidTransactionBeanList.add(eodInvalidTransactionBean);
                });
                dataTableBean.setList(invalidTransactionBeanList);
            } else {
                dataTableBean.setCount(recAtmFileInvalidList.getTotalElements() + recPaymentFileInvalidList.getTotalElements());
                dataTableBean.setPagecount(recAtmFileInvalidList.getTotalPages() + recPaymentFileInvalidList.getTotalPages());
                dataTableBean.setList(invalidTransactionBeanList);
            }
        } catch (Exception e) {
            throw e;
        }
        return dataTableBean;
    }

    public DataTableBean getEodErrorCardList(RequestBean requestBean, Long eodId) {
        DataTableBean dataTableBean = new DataTableBean();
        try {
            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize());

            Page<EODERRORCARDS> eoderrorcards = eodErrorCardListRepo.findAllByEODID(eodId, paging);

            List<Object> eodErrorCardBeans = eoderrorcards.getContent().stream()
                    .map(eod -> createEodErrorCardBean(eod))
                    .collect(Collectors.toList());

            dataTableBean.setCount(eoderrorcards.getTotalElements());
            dataTableBean.setPagecount(eoderrorcards.getTotalPages());
            dataTableBean.setList(eodErrorCardBeans);
        } catch (Exception e) {
            throw e;
        }
        return dataTableBean;
    }

    private EodErrorCardBean createEodErrorCardBean(EODERRORCARDS eod) {
        EodErrorCardBean bean = new EodErrorCardBean();
        bean.setEodId(eod.getEODID());
        bean.setCardNumber(eod.getCARDNO());
        bean.setErrorProcess(eod.getERRORPROCESSID());
        bean.setErrorReason(eod.getERRORREMARK());
        return bean;
    }

    public DataTableBean getEodErrorMerchantList(RequestBean requestBean, Long eodId) {
        DataTableBean dataTableBean = new DataTableBean();
        try {
            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize());

            Page<EODERRORMERCHANT> eoderrormerchant = eodErrorMerchantListRepo.findEODERRORMERCHANTByEODID(eodId, paging);

            List<Object> errorMerchantBeans = eoderrormerchant.getContent().stream()
                    .map(eod -> createEodErrorMerchantBean(eod))
                    .collect(Collectors.toList());

            dataTableBean.setCount(eoderrormerchant.getTotalElements());
            dataTableBean.setPagecount(eoderrormerchant.getTotalPages());
            dataTableBean.setList(errorMerchantBeans);
        } catch (Exception e) {
            throw e;
        }
        return dataTableBean;
    }

    private EodErrorMerchantBean createEodErrorMerchantBean(EODERRORMERCHANT eod) {
        EodErrorMerchantBean bean = new EodErrorMerchantBean();
        bean.setEodId(eod.getEODID());
        bean.setMerchantId(eod.getMID());
        bean.setErrorProcessId(eod.getERRORPROCESSID());
        bean.setErrorReason(eod.getERRORREMARK());
        return bean;
    }


    //File Generation
    public List<EodOutputFileBean> getEodOutputFIleList(Long eodId) {
        List<EodOutputFileBean> outputFileBeanList = new ArrayList<>();

        try {
            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            List<Object[]> eodOutputFilesList = eodOutputFileRepo.findEODOUTPUTFILESByEODID(eodId);

            Long finalEodId = eodId;
            eodOutputFilesList.forEach(eod -> {
                EodOutputFileBean eodBean = createEodOutputFileBean(finalEodId, eod);
                outputFileBeanList.add(eodBean);
            });
        } catch (Exception e) {
            throw e;
        }

        return outputFileBeanList;
    }

    private EodOutputFileBean createEodOutputFileBean(Long eodId, Object[] eod) {
        EodOutputFileBean eodBean = new EodOutputFileBean();
        eodBean.setEodId(eodId);
        eodBean.setCreatedTime((Date) eod[0]);
        eodBean.setFileType((String) eod[1]);
        eodBean.setNoOfRecords(((BigDecimal) eod[3]).intValue());
        eodBean.setFileName((String) eod[4]);
        eodBean.setSubFolder((String) eod[5]);
        return eodBean;
    }

    public List<StatementGenSummeryBean> getStatementGenSummeryList(Long eodId) {
        try {
            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            return genSummeryListRepo.findStmtGenSummeryListByEodId(eodId, Configurations.EOD_FILE_GENERATION);
        } catch (Exception e) {
            throw e;
        }
    }

    //File Processing
    public List<Object> getEodInputFIleList(Long eodId) {
        List<Object> eodInputFileObjectList = new ArrayList<>();

        try {

            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            List<EODATMFILE> atmInputFileList = eodAtmInputFileRepo.findEODATMFILEByEODID(eodId);
            List<EODPAYMENTFILE> paymentInputFileList = eodPaymentInputFileRepo.findEODPAYMENTFILEByEODID(eodId);
            List<EODMASTERFILE> masterInputFileList = eodMasterInputFileRepo.findEODMASTERFILEByEODID(eodId);
            List<EODVISAFILE> visaInputFileList = eodVisaInputFileRepo.findEODVISAFILEByEODID(eodId);


            atmInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("ATM");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            paymentInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("PAYMENT");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            masterInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("VISA");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            visaInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("MASTER");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });
        } catch (Exception e) {
            throw e;
        }
        return eodInputFileObjectList;
    }

    public List<StatementGenSummeryBean> getProcessingSummeryList(Long eodID) {
        try {
            if (eodID == 0) {
                eodID = dashboardCurrentEodId;
            }

            return processingSummeryListRepo.findProcessingSummeryListByEodId(eodID, Configurations.EOD_FILE_PROCESSING);
        } catch (Exception e) {
            throw e;
        }
    }

    public void sendInputFileUploadListener(String fileId, int processId) throws Exception {
        try {
            ProcessBean processDetails = commonRepo.getProcessDetails(processId);
            String kafkaTopic = processDetails.getKafkaTopic();

            kafkaTemplate.send(kafkaTopic, fileId);

        } catch (Exception e) {
            throw e;
        }
    }
}
