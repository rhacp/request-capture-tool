package com.rhacp.request_capture_tool.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "captured_headers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapturedHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "header_name", nullable = false, length = 255)
    private String headerName;

    @Column(name = "header_value", columnDefinition = "TEXT")
    private String headerValue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "captured_request_id", nullable = false)
    private CapturedRequest capturedRequest;
}
