package com.rhacp.request_capture_tool.repository;

import com.rhacp.request_capture_tool.model.entity.CapturedHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CapturedHeaderRepository extends JpaRepository<CapturedHeader, Long> {

    List<CapturedHeader> findByCapturedRequestIdOrderByIdAsc(Long capturedRequestId);
}