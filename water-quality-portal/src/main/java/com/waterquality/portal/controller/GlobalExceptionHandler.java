package com.waterquality.portal.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Friendly error pages instead of the Whitelabel 500.
 * API paths get JSON bodies; browser paths get the shared error view.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private boolean isApi(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return uri != null && uri.contains("/api/");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object notFound(IllegalArgumentException e, HttpServletRequest req) {
        if (isApi(req)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
        return errorView(HttpStatus.NOT_FOUND, "Not found", e.getMessage(), req);
    }

    @ExceptionHandler(SecurityException.class)
    public Object forbidden(SecurityException e, HttpServletRequest req) {
        if (isApi(req)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
        return errorView(HttpStatus.FORBIDDEN, "Not permitted", e.getMessage(), req);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class})
    public Object badRequest(Exception e, HttpServletRequest req) {
        if (isApi(req)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid request parameter"));
        }
        return errorView(HttpStatus.BAD_REQUEST, "Bad request",
                "One of the request parameters is invalid.", req);
    }

    // NOTE: unknown *routes* (e.g. /nope) fall through to Boot's default /error
    // handling, which renders the same error.html with Boot's own model — only
    // unknown *IDs* that reach a controller are mapped above.

    private ModelAndView errorView(HttpStatus status, String title,
                                   String message, HttpServletRequest req) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("error", title);
        mav.addObject("message", message);
        mav.addObject("path", req.getRequestURI());
        return mav;
    }
}
