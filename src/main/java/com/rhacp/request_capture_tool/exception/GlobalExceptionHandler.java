package com.rhacp.request_capture_tool.exception;

import com.rhacp.request_capture_tool.model.dto.ErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public Object handleIllegalArgumentException(
      IllegalArgumentException ex,
      HttpServletRequest request,
      Model model
  ) {
    log.warn("Handled IllegalArgumentException for path={}: {}", request.getRequestURI(),
        ex.getMessage());

    if (isHtmlRequest(request)) {
      return buildHtmlError(model, HttpStatus.NOT_FOUND.value(), "Resource not found",
          ex.getMessage(),
          request);
    }

    return buildErrorResponse("Not found: " + ex.getMessage(), null, request.getRequestURI(),
        HttpStatus.NOT_FOUND);
  }


  @ExceptionHandler(NoResourceFoundException.class)
  public Object handleNoResourceFoundException(
      NoResourceFoundException ex,
      HttpServletRequest request,
      Model model
  ) {
    log.warn("Resource not found: {}", request.getRequestURI());
    if (isHtmlRequest(request)) {
      return buildHtmlError(model, 404, "Resource not found",
          "Endpoint not found or required path variable is missing.", request);
    }

    return buildErrorResponse("Endpoint not found or required path variable is missing.", null,
        request.getRequestURI(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(Exception.class)
  public Object handleGenericException(
      Exception ex,
      HttpServletRequest request,
      Model model
  ) {
    log.error("Unhandled exception for path={}: {}", request.getRequestURI(), ex.getMessage(), ex);

    if (isHtmlRequest(request)) {
      return buildHtmlError(model, HttpStatus.INTERNAL_SERVER_ERROR.value(),
          "Internal server error",
          "Something went wrong while processing the request.", request);
    }

    return buildErrorResponse("Internal server error", null, request.getRequestURI(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(CompareRequestNotFoundException.class)
  public Object handleCompareRequestNotFoundException(
      CompareRequestNotFoundException ex,
      HttpServletRequest request,
      Model model
  ) {
    log.warn("Handled CompareRequestNotFoundException for path={}: {}", request.getRequestURI(),
        ex.getMessage());

    if (isHtmlRequest(request)) {
      return buildHtmlError(model, 404, "Compare request failed", ex.getMessage(), request);
    }

    return buildErrorResponse(
        ex.getMessage(),
        null,
        request.getRequestURI(),
        HttpStatus.NOT_FOUND
    );
  }

  private static @NonNull String buildHtmlError(Model model, int attributeValue,
      String Compare_request_failed, String ex, HttpServletRequest request) {
    model.addAttribute("status", attributeValue);
    model.addAttribute("error", Compare_request_failed);
    model.addAttribute("message", ex);
    model.addAttribute("path", request.getRequestURI());
    return "error";
  }

  private ResponseEntity<ErrorDTO> buildErrorResponse(String message,
      Map<String, List<String>> errors, String path, HttpStatus status) {
    ErrorDTO errorDTO = new ErrorDTO();
    errorDTO.setMessage(message);
    errorDTO.setErrors(errors);
    errorDTO.setPath(path);
    errorDTO.setTimestamp(LocalDateTime.now());
    return new ResponseEntity<>(errorDTO, status);
  }

  private boolean isHtmlRequest(HttpServletRequest request) {
    String accept = request.getHeader("Accept");

    return accept != null &&
        (accept.contains(MediaType.TEXT_HTML_VALUE)
            || accept.contains(MediaType.APPLICATION_XHTML_XML_VALUE));
  }
}