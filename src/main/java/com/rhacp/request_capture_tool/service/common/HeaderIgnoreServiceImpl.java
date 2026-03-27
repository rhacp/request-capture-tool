package com.rhacp.request_capture_tool.service.common;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class HeaderIgnoreServiceImpl implements HeaderIgnoreService {

    private static final Set<String> EXACT_IGNORED_HEADERS = Set.of(
            "content-length",
            "host",
            "user-agent",
            "date",
            "x-forwarded-for",
            "x-forwarded-host",
            "x-forwarded-port",
            "x-forwarded-proto",
            "forwarded",
            "postman-token",
            "connection",
            "accept-encoding"
    );

    private static final List<String> DISPLAY_IGNORE_RULES = List.of(
            "content-length",
            "host",
            "user-agent",
            "date",
            "x-forwarded-for",
            "x-forwarded-host",
            "x-forwarded-port",
            "x-forwarded-proto",
            "forwarded",
            "postman-token",
            "connection",
            "accept-encoding",
            "x-b3-*",
            "x-amzn-trace-id",
            "cf-*",
            "traceparent",
            "tracestate",
            "baggage"
    );

    @Override
    public boolean shouldIgnore(String headerName) {
        if (headerName == null || headerName.isBlank()) {
            return true;
        }

        String normalized = headerName.trim().toLowerCase(Locale.ROOT);

        if (EXACT_IGNORED_HEADERS.contains(normalized)) {
            return true;
        }

        return normalized.startsWith("x-b3-")
                || normalized.equals("x-amzn-trace-id")
                || normalized.startsWith("cf-")
                || normalized.equals("traceparent")
                || normalized.equals("tracestate")
                || normalized.equals("baggage");
    }

    @Override
    public List<String> getIgnoreRules() {
        return new ArrayList<>(DISPLAY_IGNORE_RULES);
    }
}