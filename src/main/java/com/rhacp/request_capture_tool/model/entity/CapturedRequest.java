package com.rhacp.request_capture_tool.model.entity;

import com.rhacp.request_capture_tool.util.enumeration.ContentTypeCategory;
import com.rhacp.request_capture_tool.util.enumeration.SourceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "captured_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapturedRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private SourceType sourceType;

    @Column(name = "group_name", length = 255)
    private String groupName;

    @Column(name = "method", nullable = false, length = 10)
    private String method;

    @Column(name = "path", nullable = false, columnDefinition = "TEXT")
    private String path;

    @Column(name = "content_type", columnDefinition = "TEXT")
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type_category", length = 30)
    private ContentTypeCategory contentTypeCategory;

    @OneToMany(mappedBy = "capturedRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CapturedHeader> headers = new ArrayList<>();

    @OneToMany(mappedBy = "capturedRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CapturedQueryParam> queryParams = new ArrayList<>();

    @OneToMany(mappedBy = "capturedRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CapturedBodyField> bodyFields = new ArrayList<>();

    @Lob
    @Column(name = "body_raw", columnDefinition = "TEXT")
    private String bodyRaw;

    @Lob
    @Column(name = "normalized_structure_json", columnDefinition = "TEXT")
    private String normalizedStructureJson;

    public void addHeader(CapturedHeader header) {
        headers.add(header);
        header.setCapturedRequest(this);
    }

    public void addQueryParam(CapturedQueryParam queryParam) {
        queryParams.add(queryParam);
        queryParam.setCapturedRequest(this);
    }

    public void addBodyField(CapturedBodyField bodyField) {
        bodyFields.add(bodyField);
        bodyField.setCapturedRequest(this);
    }
}
