package com.ecms.web.api.tokenservice.repository;


import com.ecms.web.api.tokenservice.model.entity.Systemuser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemuserRepository extends JpaRepository<Systemuser, String> {
}
