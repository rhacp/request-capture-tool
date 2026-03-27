package com.rhacp.request_capture_tool.repository;

import com.rhacp.request_capture_tool.model.entity.CapturedBodyField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CapturedBodyFieldRepository extends JpaRepository<CapturedBodyField, Long> {

    List<CapturedBodyField> findByCapturedRequestIdOrderByIdAsc(Long capturedRequestId);
}