package com.rhacp.request_capture_tool.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request,
            Model model
    ) {
        log.warn("Handled IllegalArgumentException for path={}: {}", request.getRequestURI(), ex.getMessage());

        if (isUiRequest(request)) {
            model.addAttribute("status", HttpStatus.NOT_FOUND.value());
            model.addAttribute("error", "Resource not found");
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("path", request.getRequestURI());
            return "error";
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Not found: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Object handleGenericException(
            Exception ex,
            HttpServletRequest request,
            Model model
    ) {
        log.error("Unhandled exception for path={}: {}", request.getRequestURI(), ex.getMessage(), ex);

        if (isUiRequest(request)) {
            model.addAttribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            model.addAttribute("error", "Internal server error");
            model.addAttribute("message", "Something went wrong while processing the request.");
            model.addAttribute("path", request.getRequestURI());
            return "error";
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.TEXT_PLAIN)
                .body("Internal server error");
    }

    @ExceptionHandler(CompareRequestNotFoundException.class)
    public Object handleCompareRequestNotFoundException(
            CompareRequestNotFoundException ex,
            HttpServletRequest request,
            Model model
    ) {
        log.warn("Handled CompareRequestNotFoundException for path={}: {}", request.getRequestURI(), ex.getMessage());

        if (isUiRequest(request)) {
            model.addAttribute("status", 404);
            model.addAttribute("error", "Compare request failed");
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("path", request.getRequestURI());
            return "error";
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.TEXT_PLAIN)
                .body(ex.getMessage());
    }

    private boolean isUiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/ui/");
    }
}