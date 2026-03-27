package com.rhacp.request_capture_tool.service.view;

import com.rhacp.request_capture_tool.model.dto.RequestDetailsView;

public interface RequestFormattingService {

    String formatRawBodyForDisplay(RequestDetailsView requestItem);

    String formatDecodedBody(RequestDetailsView requestItem);
}