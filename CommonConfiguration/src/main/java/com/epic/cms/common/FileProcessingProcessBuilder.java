/**
 * Author :
 * Date : 12/14/2022
 * Time : 9:36 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.common;

import com.epic.cms.model.bean.ProcessBean;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

import java.util.LinkedHashMap;

public abstract class FileProcessingProcessBuilder {
    public LinkedHashMap details = new LinkedHashMap();
    public LinkedHashMap summery = new LinkedHashMap();
    public ProcessBean processBean = null;

    @Async("ThreadPool_FileHandler")
    public void startProcess(String fileId) throws Exception {
        System.out.println("This the startProcess from parent class");
        System.out.println("Class Name:FileProcessingProcessBuilder,File ID:" + fileId + ",Current Thread:" + Thread.currentThread().getName());
        concreteProcess(fileId);
    }

    public abstract void concreteProcess(String fileId) throws Exception;
}
