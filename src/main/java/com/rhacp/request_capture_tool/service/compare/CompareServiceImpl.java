package com.rhacp.request_capture_tool.service.compare;

import com.rhacp.request_capture_tool.model.dto.BodyFieldItemView;
import com.rhacp.request_capture_tool.model.dto.BodyFieldTypeMismatchView;
import com.rhacp.request_capture_tool.model.dto.CompareResultView;
import com.rhacp.request_capture_tool.model.dto.HeaderItemView;
import com.rhacp.request_capture_tool.model.dto.QueryParamItemView;
import com.rhacp.request_capture_tool.model.dto.RequestDetailsView;
import com.rhacp.request_capture_tool.service.common.HeaderIgnoreService;
import com.rhacp.request_capture_tool.service.view.RequestViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class CompareServiceImpl implements CompareService {

    private final RequestViewService requestViewService;

    private final HeaderIgnoreService headerIgnoreService;

    public CompareServiceImpl(
            RequestViewService requestViewService,
            HeaderIgnoreService headerIgnoreService
    ) {
        this.requestViewService = requestViewService;
        this.headerIgnoreService = headerIgnoreService;
    }

    @Override
    @Transactional(readOnly = true)
    public CompareResultView compareRequests(Long leftId, Long rightId) {
        validateCompareInput(leftId, rightId);

        log.debug("Comparing requests: leftId={}, rightId={}", leftId, rightId);

        RequestDetailsView left = loadLeftRequest(leftId);
        RequestDetailsView right = loadRightRequest(rightId);

        boolean sameMethod = Objects.equals(left.method(), right.method());
        boolean sameSourceType = Objects.equals(
                safeEnumName(left.sourceType()),
                safeEnumName(right.sourceType())
        );
        boolean sameContentTypeCategory = Objects.equals(
                safeEnumName(left.contentTypeCategory()),
                safeEnumName(right.contentTypeCategory())
        );

        Set<String> leftHeaderNames = extractComparableHeaderNames(left.headers());
        Set<String> rightHeaderNames = extractComparableHeaderNames(right.headers());

        List<String> headersOnlyInLeft = subtract(leftHeaderNames, rightHeaderNames);
        List<String> headersOnlyInRight = subtract(rightHeaderNames, leftHeaderNames);

        Set<String> leftQueryParamNames = extractQueryParamNames(left.queryParams());
        Set<String> rightQueryParamNames = extractQueryParamNames(right.queryParams());

        List<String> queryParamsOnlyInLeft = subtract(leftQueryParamNames, rightQueryParamNames);
        List<String> queryParamsOnlyInRight = subtract(rightQueryParamNames, leftQueryParamNames);

        Map<String, String> leftBodyFieldTypes = extractBodyFieldTypes(left.bodyFields());
        Map<String, String> rightBodyFieldTypes = extractBodyFieldTypes(right.bodyFields());

        List<String> bodyFieldsOnlyInLeft = subtract(leftBodyFieldTypes.keySet(), rightBodyFieldTypes.keySet());
        List<String> bodyFieldsOnlyInRight = subtract(rightBodyFieldTypes.keySet(), leftBodyFieldTypes.keySet());

        List<BodyFieldTypeMismatchView> bodyTypeMismatches = extractBodyTypeMismatches(
                leftBodyFieldTypes,
                rightBodyFieldTypes
        );

        boolean structurallyEqual =
                sameMethod
                        && sameSourceType
                        && sameContentTypeCategory
                        && headersOnlyInLeft.isEmpty()
                        && headersOnlyInRight.isEmpty()
                        && queryParamsOnlyInLeft.isEmpty()
                        && queryParamsOnlyInRight.isEmpty()
                        && bodyFieldsOnlyInLeft.isEmpty()
                        && bodyFieldsOnlyInRight.isEmpty()
                        && bodyTypeMismatches.isEmpty();

        CompareResultView result = new CompareResultView(
                left.id(),
                right.id(),
                structurallyEqual,

                sameMethod,
                left.method(),
                right.method(),

                sameSourceType,
                safeEnumName(left.sourceType()),
                safeEnumName(right.sourceType()),

                sameContentTypeCategory,
                safeEnumName(left.contentTypeCategory()),
                safeEnumName(right.contentTypeCategory()),

                headerIgnoreService.getIgnoreRules(),

                headersOnlyInLeft,
                headersOnlyInRight,

                queryParamsOnlyInLeft,
                queryParamsOnlyInRight,

                bodyFieldsOnlyInLeft,
                bodyFieldsOnlyInRight,

                bodyTypeMismatches
        );

        log.debug(
                "Compare finished: leftId={}, rightId={}, structurallyEqual={}, metadataMismatchCount={}, headerDiffs={}, queryParamDiffs={}, bodyOnlyLeft={}, bodyOnlyRight={}, bodyTypeMismatches={}",
                leftId,
                rightId,
                structurallyEqual,
                countMetadataMismatches(sameMethod, sameSourceType, sameContentTypeCategory),
                headersOnlyInLeft.size() + headersOnlyInRight.size(),
                queryParamsOnlyInLeft.size() + queryParamsOnlyInRight.size(),
                bodyFieldsOnlyInLeft.size(),
                bodyFieldsOnlyInRight.size(),
                bodyTypeMismatches.size()
        );

        return result;
    }

    private void validateCompareInput(Long leftId, Long rightId) {
        if (leftId == null) {
            throw new IllegalArgumentException("Left request ID must not be null.");
        }

        if (rightId == null) {
            throw new IllegalArgumentException("Right request ID must not be null.");
        }

        if (leftId.equals(rightId)) {
            throw new IllegalArgumentException("Left and right request IDs must be different.");
        }
    }

    private RequestDetailsView loadLeftRequest(Long leftId) {
        try {
            return requestViewService.getRequestDetails(leftId);
        } catch (IllegalArgumentException ex) {
            log.warn("Left request not found for comparison. leftId={}", leftId);
            throw new IllegalArgumentException("Left request not found: " + leftId);
        }
    }

    private RequestDetailsView loadRightRequest(Long rightId) {
        try {
            return requestViewService.getRequestDetails(rightId);
        } catch (IllegalArgumentException ex) {
            log.warn("Right request not found for comparison. rightId={}", rightId);
            throw new IllegalArgumentException("Right request not found: " + rightId);
        }
    }

    private Set<String> extractComparableHeaderNames(List<HeaderItemView> headers) {
        Set<String> result = new TreeSet<>();

        for (HeaderItemView header : headers) {
            if (header.headerName() == null || header.headerName().isBlank()) {
                continue;
            }

            String normalized = header.headerName().trim().toLowerCase(Locale.ROOT);

            if (headerIgnoreService.shouldIgnore(normalized)) {
                continue;
            }

            result.add(normalized);
        }

        return result;
    }

    private Set<String> extractQueryParamNames(List<QueryParamItemView> queryParams) {
        Set<String> result = new TreeSet<>();

        for (QueryParamItemView qp : queryParams) {
            if (qp.paramName() != null && !qp.paramName().isBlank()) {
                result.add(qp.paramName().trim());
            }
        }

        return result;
    }

    private Map<String, String> extractBodyFieldTypes(List<BodyFieldItemView> bodyFields) {
        Map<String, String> result = new TreeMap<>();

        for (BodyFieldItemView field : bodyFields) {
            if (field.fieldPath() != null && !field.fieldPath().isBlank()) {
                result.put(field.fieldPath().trim(), safeEnumName(field.valueType()));
            }
        }

        return result;
    }

    private List<String> subtract(Collection<String> left, Collection<String> right) {
        List<String> result = new ArrayList<>();

        for (String value : left) {
            if (!right.contains(value)) {
                result.add(value);
            }
        }

        return result;
    }

    private List<BodyFieldTypeMismatchView> extractBodyTypeMismatches(
            Map<String, String> leftBodyFieldTypes,
            Map<String, String> rightBodyFieldTypes
    ) {
        List<BodyFieldTypeMismatchView> result = new ArrayList<>();

        for (Map.Entry<String, String> entry : leftBodyFieldTypes.entrySet()) {
            String fieldPath = entry.getKey();
            String leftType = entry.getValue();

            if (!rightBodyFieldTypes.containsKey(fieldPath)) {
                continue;
            }

            String rightType = rightBodyFieldTypes.get(fieldPath);

            if (!Objects.equals(leftType, rightType)) {
                result.add(new BodyFieldTypeMismatchView(fieldPath, leftType, rightType));
            }
        }

        return result;
    }

    private int countMetadataMismatches(
            boolean sameMethod,
            boolean sameSourceType,
            boolean sameContentTypeCategory
    ) {
        int count = 0;

        if (!sameMethod) {
            count++;
        }
        if (!sameSourceType) {
            count++;
        }
        if (!sameContentTypeCategory) {
            count++;
        }

        return count;
    }

    private String safeEnumName(Enum<?> value) {
        return value == null ? null : value.name();
    }
}