package com.rhacp.request_capture_tool.service.view;

import com.rhacp.request_capture_tool.model.dto.BodyFieldItemView;
import com.rhacp.request_capture_tool.model.dto.RequestDetailsView;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class RequestFormattingServiceImpl implements RequestFormattingService {

    private final ObjectMapper objectMapper;

    public RequestFormattingServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String formatRawBodyForDisplay(RequestDetailsView requestItem) {
        if (requestItem == null || requestItem.contentTypeCategory() == null) {
            return fallback(requestItem != null ? requestItem.bodyRaw() : null, "No body");
        }

        return switch (requestItem.contentTypeCategory()) {
            case JSON, FORM_URLENCODED, UNKNOWN -> fallback(requestItem.bodyRaw(), "No body");
            case MULTIPART -> formatBodyFieldsInline(requestItem.bodyFields());
        };
    }

    @Override
    public String formatDecodedBody(RequestDetailsView requestItem) {
        if (requestItem == null || requestItem.contentTypeCategory() == null) {
            return fallback(requestItem != null ? requestItem.bodyRaw() : null, "No body");
        }

        return switch (requestItem.contentTypeCategory()) {
            case JSON -> prettyPrintJsonIfPossible(requestItem.bodyRaw(), fallback(requestItem.bodyRaw(), "No body"));
            case FORM_URLENCODED, MULTIPART -> formatBodyFieldsAsDecodedText(requestItem.bodyFields());
            case UNKNOWN -> fallback(requestItem.bodyRaw(), "No body");
        };
    }

    private String prettyPrintJsonIfPossible(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(value);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (Exception e) {
            return fallback;
        }
    }

    private String formatBodyFieldsAsDecodedText(List<BodyFieldItemView> bodyFields) {
        if (bodyFields == null || bodyFields.isEmpty()) {
            return "No body";
        }

        StringBuilder sb = new StringBuilder();

        for (BodyFieldItemView field : bodyFields) {
            sb.append(field.fieldPath())
                    .append(" = ")
                    .append(field.fieldValue() == null ? "null" : field.fieldValue())
                    .append(System.lineSeparator());
        }

        return sb.toString().trim();
    }

    private String formatBodyFieldsInline(List<BodyFieldItemView> bodyFields) {
        if (bodyFields == null || bodyFields.isEmpty()) {
            return "No body";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bodyFields.size(); i++) {
            BodyFieldItemView field = bodyFields.get(i);

            sb.append(field.fieldPath())
                    .append("=")
                    .append(field.fieldValue() == null ? "null" : field.fieldValue());

            if (i < bodyFields.size() - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    private String fallback(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}