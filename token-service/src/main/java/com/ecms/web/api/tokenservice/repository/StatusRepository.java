package com.ecms.web.api.tokenservice.repository;

import com.ecms.web.api.tokenservice.model.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, String> {
}
