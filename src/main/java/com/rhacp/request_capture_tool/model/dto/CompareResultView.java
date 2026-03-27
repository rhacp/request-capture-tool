package com.rhacp.request_capture_tool.model.dto;

import java.util.List;

public record CompareResultView(
        Long leftId,
        Long rightId,
        boolean structurallyEqual,

        boolean sameMethod,
        String leftMethod,
        String rightMethod,

        boolean sameSourceType,
        String leftSourceType,
        String rightSourceType,

        boolean sameContentTypeCategory,
        String leftContentTypeCategory,
        String rightContentTypeCategory,

        List<String> ignoredHeaderRules,

        List<String> headersOnlyInLeft,
        List<String> headersOnlyInRight,

        List<String> queryParamsOnlyInLeft,
        List<String> queryParamsOnlyInRight,

        List<String> bodyFieldsOnlyInLeft,
        List<String> bodyFieldsOnlyInRight,

        List<BodyFieldTypeMismatchView> bodyTypeMismatches
) {
}