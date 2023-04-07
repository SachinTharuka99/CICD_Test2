package com.ecms.web.api.tokenservice.repository;


import com.ecms.web.api.tokenservice.model.entity.Passwordpolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordpolicyRepository extends JpaRepository<Passwordpolicy, String> {

    Passwordpolicy findByPasswordpolicycode(String passwordPolicyCode);

}
