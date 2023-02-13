package com.ecms.web.api.tokenservice.repository;

import com.ecms.web.api.tokenservice.model.entity.Systemaudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SystemauditRepository extends JpaRepository<Systemaudit, String>, JpaSpecificationExecutor<Systemaudit> {
}
