package com.rhacp.request_capture_tool.service.common;

import java.util.List;

public interface HeaderIgnoreService {

    boolean shouldIgnore(String headerName);

    List<String> getIgnoreRules();
}