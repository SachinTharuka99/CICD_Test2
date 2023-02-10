package com.epic.cms.service;

import com.epic.cms.repository.ConfigurationsRepo;
import com.epic.cms.util.Configurations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class ConfigurationService {

    @Autowired
    ConfigurationsRepo configurationsRepo;


    @PostConstruct
    public void setConfigurations() throws Exception {
       configurationsRepo.setConfigurations();
    }

    @PostConstruct
    public void loadTxnTypeConfigurations() throws Exception {
        configurationsRepo.loadTxnTypeConfigurations();
    }

    @PostConstruct
    public void loadFilePath() throws Exception {
        configurationsRepo.loadFilePath();
    }

    @PostConstruct
    public void loadBaseCurrency() throws Exception {
        configurationsRepo.loadBaseCurrency();
    }

}
