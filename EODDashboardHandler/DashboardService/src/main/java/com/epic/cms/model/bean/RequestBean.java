/**
 * Author : rasintha_j
 * Date : 3/20/2023
 * Time : 5:44 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestBean {
    private String client_ip;
    private String token;
    private String userrole;
    private String username;
    private int userlevel;
    private Object requestBody;
    private int page;
    private int size;
    private String[] sort;
    private boolean search;
    private byte[] multipartFile;
    private String fileNm;
    private long fileSize;

    private String uploadStatus;
    private String fileName;
}
