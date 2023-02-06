package com.epic.cms.dao;

import com.epic.cms.model.bean.ExposureFileBean;

import java.util.List;

public interface ExposureFileDao {
    List<ExposureFileBean> getExposureFileDetails() throws Exception;
}
