package com.rhacp.request_capture_tool.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {

  private String message;

  private Map<String, List<String>> errors;

  private String path;

  private LocalDateTime timestamp;
}
