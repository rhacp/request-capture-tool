package com.rhacp.request_capture_tool.repository;

import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CapturedRequestRepository extends JpaRepository<CapturedRequest, Long>, JpaSpecificationExecutor<CapturedRequest> {

}