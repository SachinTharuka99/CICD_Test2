package com.epic.cms.common;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.KafkaMessageUpdator;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class ProcessBuilder {

    public LinkedHashMap details = new LinkedHashMap();
    public LinkedHashMap summery = new LinkedHashMap();
    public ProcessBean processBean = null;

    public List<ErrorCardBean> cardErrorList = Collections.synchronizedList(new ArrayList<ErrorCardBean>());
    public List<ErrorMerchantBean> merchantErrorList = Collections.synchronizedList(new ArrayList<ErrorMerchantBean>());

    @Autowired
    KafkaMessageUpdator kafkaMessageUpdator;

    @Autowired
    CommonRepo commonRepo;

    public void startProcess() throws Exception {
        System.out.println("This the startProcess from parent class");
        concreteProcess();
        kafkaMessageUpdator.producerWithNoReturn("true", "processStatus");
        System.out.println("Send the process success status");
        commonRepo.updateEODProcessCount(Configurations.eodUniqueId);
    }
    public abstract void concreteProcess() throws Exception;
}
