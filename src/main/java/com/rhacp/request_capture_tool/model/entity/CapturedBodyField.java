package com.rhacp.request_capture_tool.model.entity;

import com.rhacp.request_capture_tool.util.enumeration.BodyValueType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "captured_body_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapturedBodyField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "field_path", nullable = false, columnDefinition = "TEXT")
    private String fieldPath;

    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", length = 20)
    private BodyValueType valueType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "captured_request_id", nullable = false)
    private CapturedRequest capturedRequest;
}
