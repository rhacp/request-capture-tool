package com.rhacp.request_capture_tool.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "captured_query_params")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapturedQueryParam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "param_name", nullable = false, length = 255)
    private String paramName;

    @Column(name = "param_value", columnDefinition = "TEXT")
    private String paramValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "captured_request_id", nullable = false)
    private CapturedRequest capturedRequest;
}
