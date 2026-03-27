package com.rhacp.request_capture_tool.repository;

import com.rhacp.request_capture_tool.model.entity.CapturedQueryParam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CapturedQueryParamRepository extends JpaRepository<CapturedQueryParam, Long> {

    List<CapturedQueryParam> findByCapturedRequestIdOrderByIdAsc(Long capturedRequestId);
}