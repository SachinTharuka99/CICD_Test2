/**
 * Author : rasintha_j
 * Date : 7/12/2023
 * Time : 1:13 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import com.epic.cms.util.Configurations;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilePathBean {
    private String path_master_file_windows;
    private String path_master_file_linux;
    private String path_backup_windows;
    private String path_backup_linux;

}
