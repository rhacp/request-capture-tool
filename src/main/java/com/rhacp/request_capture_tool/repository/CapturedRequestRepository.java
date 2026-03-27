package com.rhacp.request_capture_tool.repository;

import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.util.enumeration.ContentTypeCategory;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CapturedRequestRepository extends JpaRepository<CapturedRequest, Long> {

    List<CapturedRequest> findAllByOrderByReceivedAtDesc();

    List<CapturedRequest> findByGroupNameOrderByReceivedAtDesc(String groupName);

    List<CapturedRequest> findBySourceTypeOrderByReceivedAtDesc(SourceType sourceType);

    List<CapturedRequest> findByContentTypeCategoryOrderByReceivedAtDesc(ContentTypeCategory contentTypeCategory);

    List<CapturedRequest> findByGroupNameAndSourceTypeOrderByReceivedAtDesc(String groupName, SourceType sourceType);

    List<CapturedRequest> findByGroupNameAndContentTypeCategoryOrderByReceivedAtDesc(
            String groupName,
            ContentTypeCategory contentTypeCategory
    );

    Optional<CapturedRequest> findDetailedById(Long id);
}
