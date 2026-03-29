package com.rhacp.request_capture_tool.service.capture;

import com.rhacp.request_capture_tool.model.entity.CapturedBodyField;
import com.rhacp.request_capture_tool.model.entity.CapturedRequest;
import com.rhacp.request_capture_tool.util.enumeration.BodyValueType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class NormalizationServiceImpl implements NormalizationService {

    private final ObjectMapper objectMapper;

    public NormalizationServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void normalizeJsonBody(HttpServletRequest request, CapturedRequest capturedRequest) {
        String rawBody = readRequestBodySafely(request);
        capturedRequest.setBodyRaw(rawBody);

        if (rawBody == null || rawBody.isBlank()) {
            log.debug("JSON body is empty for path={}", request.getRequestURI());
            capturedRequest.setNormalizedStructureJson("{}");
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(rawBody);
            Map<String, String> normalizedStructure = new LinkedHashMap<>();

            extractJsonNode(rootNode, "", capturedRequest, normalizedStructure);

            capturedRequest.setNormalizedStructureJson(writeJsonSafely(normalizedStructure));

            log.debug(
                    "JSON normalization completed for path={}. bodyFieldsCount={}, normalizedKeysCount={}",
                    request.getRequestURI(),
                    capturedRequest.getBodyFields().size(),
                    normalizedStructure.size()
            );
        } catch (Exception e) {
            log.warn("Failed to parse JSON body for path={}: {}", request.getRequestURI(), e.getMessage());
            capturedRequest.setNormalizedStructureJson("{\"_parseError\":\"INVALID_JSON\"}");
        }
    }

    @Override
    public void normalizeFormUrlEncodedBody(HttpServletRequest request, CapturedRequest capturedRequest) {
        String rawBody = readRequestBodySafely(request);
        capturedRequest.setBodyRaw(rawBody);

        if (rawBody == null || rawBody.isBlank()) {
            log.debug("FORM_URLENCODED body is empty for path={}", request.getRequestURI());
            capturedRequest.setNormalizedStructureJson("{}");
            return;
        }

        List<NameValuePair> bodyParams = parseUrlEncodedString(rawBody);
        Map<String, String> normalizedStructure = new LinkedHashMap<>();

        for (NameValuePair pair : bodyParams) {
            capturedRequest.addBodyField(
                    CapturedBodyField.builder()
                            .fieldPath(pair.name())
                            .fieldValue(pair.value())
                            .valueType(BodyValueType.STRING)
                            .build()
            );

            normalizedStructure.put(pair.name(), BodyValueType.STRING.name());
        }

        capturedRequest.setNormalizedStructureJson(writeJsonSafely(normalizedStructure));

        log.debug(
                "FORM_URLENCODED normalization completed for path={}. bodyFieldsCount={}, normalizedKeysCount={}",
                request.getRequestURI(),
                capturedRequest.getBodyFields().size(),
                normalizedStructure.size()
        );
    }

    @Override
    public void normalizeMultipartBody(HttpServletRequest request, CapturedRequest capturedRequest) {
        Map<String, String> normalizedStructure = new LinkedHashMap<>();
        Map<String, Object> bodyPreview = new LinkedHashMap<>();

        try {
            Collection<Part> parts = request.getParts();
            int textPartsCount = 0;
            int skippedFilePartsCount = 0;

            for (Part part : parts) {
                if (part.getSubmittedFileName() != null) {
                    skippedFilePartsCount++;
                    continue;
                }

                String fieldName = part.getName();
                String fieldValue = readInputStream(part.getInputStream());

                capturedRequest.addBodyField(
                        CapturedBodyField.builder()
                                .fieldPath(fieldName)
                                .fieldValue(fieldValue)
                                .valueType(BodyValueType.STRING)
                                .build()
                );

                normalizedStructure.put(fieldName, BodyValueType.STRING.name());
                bodyPreview.put(fieldName, fieldValue);
                textPartsCount++;
            }

            capturedRequest.setBodyRaw(writeJsonSafely(bodyPreview));
            capturedRequest.setNormalizedStructureJson(writeJsonSafely(normalizedStructure));

            log.debug(
                    "MULTIPART normalization completed for path={}. textPartsCount={}, skippedFilePartsCount={}, normalizedKeysCount={}",
                    request.getRequestURI(),
                    textPartsCount,
                    skippedFilePartsCount,
                    normalizedStructure.size()
            );
        } catch (Exception e) {
            log.warn("Failed to read MULTIPART body for path={}: {}", request.getRequestURI(), e.getMessage());
            capturedRequest.setBodyRaw("{\"_parseError\":\"MULTIPART_READ_FAILED\"}");
            capturedRequest.setNormalizedStructureJson("{\"_parseError\":\"MULTIPART_READ_FAILED\"}");
        }
    }

    @Override
    public void normalizeJsonBody(String rawBody, CapturedRequest capturedRequest) {
        capturedRequest.setBodyRaw(rawBody);

        if (rawBody == null || rawBody.isBlank()) {
            log.debug("JSON body is empty for captured response. path={}", capturedRequest.getPath());
            capturedRequest.setNormalizedStructureJson("{}");
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(rawBody);
            Map<String, String> normalizedStructure = new LinkedHashMap<>();

            extractJsonNode(rootNode, "", capturedRequest, normalizedStructure);

            capturedRequest.setNormalizedStructureJson(writeJsonSafely(normalizedStructure));

            log.debug(
                    "JSON normalization completed for captured response. path={}, bodyFieldsCount={}, normalizedKeysCount={}",
                    capturedRequest.getPath(),
                    capturedRequest.getBodyFields().size(),
                    normalizedStructure.size()
            );
        } catch (Exception e) {
            log.warn("Failed to parse JSON response body for path={}: {}", capturedRequest.getPath(), e.getMessage());
            capturedRequest.setNormalizedStructureJson("{\"_parseError\":\"INVALID_JSON\"}");
        }
    }

    @Override
    public void normalizeFormUrlEncodedBody(String rawBody, CapturedRequest capturedRequest) {
        capturedRequest.setBodyRaw(rawBody);

        if (rawBody == null || rawBody.isBlank()) {
            log.debug("FORM_URLENCODED body is empty for captured response. path={}", capturedRequest.getPath());
            capturedRequest.setNormalizedStructureJson("{}");
            return;
        }

        List<NameValuePair> bodyParams = parseUrlEncodedString(rawBody);
        Map<String, String> normalizedStructure = new LinkedHashMap<>();

        for (NameValuePair pair : bodyParams) {
            capturedRequest.addBodyField(
                    CapturedBodyField.builder()
                            .fieldPath(pair.name())
                            .fieldValue(pair.value())
                            .valueType(BodyValueType.STRING)
                            .build()
            );

            normalizedStructure.put(pair.name(), BodyValueType.STRING.name());
        }

        capturedRequest.setNormalizedStructureJson(writeJsonSafely(normalizedStructure));

        log.debug(
                "FORM_URLENCODED normalization completed for captured response. path={}, bodyFieldsCount={}, normalizedKeysCount={}",
                capturedRequest.getPath(),
                capturedRequest.getBodyFields().size(),
                normalizedStructure.size()
        );
    }

    private void extractJsonNode(
            JsonNode node,
            String currentPath,
            CapturedRequest capturedRequest,
            Map<String, String> normalizedStructure
    ) {
        if (node.isObject()) {
            if (!currentPath.isBlank()) {
                normalizedStructure.put(currentPath, BodyValueType.OBJECT.name());
            }

            for (Map.Entry<String, JsonNode> field : node.properties()) {
                String nextPath = currentPath.isBlank() ? field.getKey() : currentPath + "." + field.getKey();
                extractJsonNode(field.getValue(), nextPath, capturedRequest, normalizedStructure);
            }
            return;
        }

        if (node.isArray()) {
            if (!currentPath.isBlank()) {
                normalizedStructure.put(currentPath, BodyValueType.ARRAY.name());
            }

            for (JsonNode element : node) {
                String nextPath = currentPath.isBlank() ? "[]" : currentPath + "[]";
                extractJsonNode(element, nextPath, capturedRequest, normalizedStructure);
            }
            return;
        }

        BodyValueType valueType = resolveJsonValueType(node);
        String fieldValue = node.isNull() ? null : node.asText();

        capturedRequest.addBodyField(
                CapturedBodyField.builder()
                        .fieldPath(currentPath)
                        .fieldValue(fieldValue)
                        .valueType(valueType)
                        .build()
        );

        normalizedStructure.put(currentPath, valueType.name());
    }

    private BodyValueType resolveJsonValueType(JsonNode node) {
        if (node == null || node.isNull()) {
            return BodyValueType.NULL;
        }
        if (node.isTextual()) {
            return BodyValueType.STRING;
        }
        if (node.isNumber()) {
            return BodyValueType.NUMBER;
        }
        if (node.isBoolean()) {
            return BodyValueType.BOOLEAN;
        }
        if (node.isObject()) {
            return BodyValueType.OBJECT;
        }
        if (node.isArray()) {
            return BodyValueType.ARRAY;
        }
        return BodyValueType.UNKNOWN;
    }

    private String readRequestBodySafely(HttpServletRequest request) {
        try {
            return readInputStream(request.getInputStream());
        } catch (Exception e) {
            log.warn("Failed to read raw request body for path={}: {}", request.getRequestURI(), e.getMessage());
            return null;
        }
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }

        return builder.toString();
    }

    private List<NameValuePair> parseUrlEncodedString(String raw) {
        List<NameValuePair> result = new ArrayList<>();

        if (raw == null || raw.isBlank()) {
            return result;
        }

        String[] pairs = raw.split("&");

        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }

            String[] keyValue = pair.split("=", 2);

            String key = decode(keyValue[0]);
            String value = keyValue.length > 1 ? decode(keyValue[1]) : "";

            result.add(new NameValuePair(key, value));
        }

        return result;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String writeJsonSafely(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize normalized structure/body preview to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    private record NameValuePair(String name, String value) {
    }
}