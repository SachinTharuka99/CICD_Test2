package com.epic.cms.service;

import com.epic.cms.repository.ConfigurationsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service("ConfigurationService")
@ComponentScan(basePackages = {"com.epic.cms.*"})
public class ConfigurationService {

    @Autowired
    ConfigurationsRepo configurationsRepo;

    @PostConstruct
    public void setPreConfigurations() throws Exception {
        this.setConfigurations();
        this.loadTxnTypeConfigurations();
        this.loadFilePath();// this depend on server run platform (configuration data)
        this.loadBaseCurrency();
    }

    public void setConfigurations() throws Exception {
        configurationsRepo.setConfigurations();
    }

    public void loadTxnTypeConfigurations() throws Exception {
        configurationsRepo.loadTxnTypeConfigurations();
    }

    public void loadFilePath() throws Exception {
        configurationsRepo.loadFilePath();
    }

    public void loadBaseCurrency() throws Exception {
        configurationsRepo.loadBaseCurrency();
    }

}
