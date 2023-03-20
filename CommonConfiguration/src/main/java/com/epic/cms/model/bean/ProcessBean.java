package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ServiceLoader;

@Getter
@Setter
public class ProcessBean {
    private int processId;
    private int stepId;
    private String processDes;
    private int criticalStatus;
    private int rollBackStatus;
    private Timestamp sheduleDate;
    private String sheduleTime;
    private int frequencyType;
    private int continuousFrequencyType;
    private int continuousFrequency;
    private int multiCycleStatus;
    private int processCategoryId;
    private int dependancyStatus;
    private int runningOnMain;
    private int runningOnSub;
    private int processType;
    private String status;
    private int holidayAction;
    private String kafkaTopic;
    private String kafkaGroupId;
    private String eodmodule;
}
