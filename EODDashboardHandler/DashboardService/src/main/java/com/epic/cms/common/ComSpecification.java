/**
 * Author : rasintha_j
 * Date : 3/20/2023
 * Time : 8:23 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.common;

import com.epic.cms.model.bean.EodErrorCardBean;
import com.epic.cms.model.bean.EodErrorMerchantBean;
import com.epic.cms.model.bean.EodInvalidTransactionBean;
import com.epic.cms.model.entity.EODERRORCARDS;
import com.epic.cms.model.entity.EODERRORMERCHANT;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ComSpecification {

    public Specification<EODERRORMERCHANT> makeMerchantSpecification(EodErrorMerchantBean errorMerchantBean) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (errorMerchantBean.getMerchantId() != null && !errorMerchantBean.getMerchantId().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("MID"), "%" + errorMerchantBean.getMerchantId() + "%"));
            }
            if (errorMerchantBean.getErrorReason() != null && !errorMerchantBean.getErrorReason().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ERRORREMARK"), "%" + errorMerchantBean.getErrorReason() + "%"));
            }
            if (errorMerchantBean.getErrorProcessId() != null && !errorMerchantBean.getErrorProcessId().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ERRORPROCESSID"), "%" + errorMerchantBean.getErrorProcessId() + "%"));
            }
            if (errorMerchantBean.getEodId() != null && !errorMerchantBean.getEodId().toString().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("EODID"), "%" + errorMerchantBean.getEodId() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<EODERRORCARDS> makeErrorCardSpecification(EodErrorCardBean errorCardBean) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (errorCardBean.getEodId() != null && !errorCardBean.getEodId().toString().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("EODID"), "%" + errorCardBean.getEodId() + "%"));
            }
            if (errorCardBean.getCardNumber() != null && !errorCardBean.getCardNumber().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("CARDNO"), "%" + errorCardBean.getCardNumber() + "%"));
            }
            if (errorCardBean.getErrorProcess() != null && !errorCardBean.getErrorProcess().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ERRORPROCESSID"), "%" + errorCardBean.getErrorProcess() + "%"));
            }
            if (errorCardBean.getErrorReason() != null && !errorCardBean.getErrorReason().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ERRORREMARK"), "%" + errorCardBean.getErrorReason() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Specification<EodInvalidTransactionBean> makeInvalidTransactionSpecification(EodInvalidTransactionBean transactionBean) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (transactionBean.getEodId() != null && !transactionBean.getEodId().toString().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("EODID"), "%" + transactionBean.getEodId() + "%"));
            }
            if (transactionBean.getFileId() != null && !transactionBean.getFileId().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("FILEID"), "%" + transactionBean.getFileId() + "%"));
            }
            if (transactionBean.getFileType() != null && !transactionBean.getFileType().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ERRORPROCESSID"), "%" + transactionBean.getFileType() + "%"));
            }
            if (transactionBean.getLineNumber() != 0 ) {
                predicates.add(criteriaBuilder.like(root.get("LINENUMBER"), "%" + transactionBean.getLineNumber() + "%"));
            }
            if (transactionBean.getErrorRemark() != null && !transactionBean.getErrorRemark().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("ERRORDESC"), "%" + transactionBean.getErrorRemark() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
