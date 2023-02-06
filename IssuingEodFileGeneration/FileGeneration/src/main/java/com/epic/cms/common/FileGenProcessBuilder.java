/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 1:20 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.common;

import com.epic.cms.model.FileGenerationModel;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.service.FileGenerationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public abstract class FileGenProcessBuilder extends ProcessBuilder{

    @Autowired
    public CommonRepo commonRepo;

    @Autowired
    public FileGenerationService fileGenerationService;

    public String fileName = "";
    public String fileExtension = "";
    public String fileDirectory = "";

    public String filePath =null;
    public String backUpFilePath =null;

    public String backUpName = "BACKUP" + File.separator;
    public FileGenerationModel fileGenerationModel = null;
    public boolean toDeleteStatus = true;

    public String fieldDelimeter = "~";

}
