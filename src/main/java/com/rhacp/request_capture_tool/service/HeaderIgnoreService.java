package com.rhacp.request_capture_tool.service;

public interface HeaderIgnoreService {

    boolean shouldIgnore(String headerName);
}