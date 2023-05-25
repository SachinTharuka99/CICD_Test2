/**
 * Author : rasintha_j
 * Date : 5/24/2023
 * Time : 11:44 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.entity;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class EODOUTPUTFILESPK implements Serializable {
    @NotNull
    @Column(name = "FILENAME")
    private String FILENAME;

    @NotNull
    @Column(name = "EODID")
    private Long EODID;

    public EODOUTPUTFILESPK() {
    }

    public EODOUTPUTFILESPK(String FILENAME, Long EODID) {
        this.FILENAME = FILENAME;
        this.EODID = EODID;
    }
}
