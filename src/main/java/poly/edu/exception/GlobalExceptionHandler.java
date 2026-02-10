package poly.edu.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * GlobalExceptionHandler - Centralized exception handling for the application.
 *
 * Rubber Duck Explanation:
 * -------------------------
 * "Why use @ControllerAdvice for exception handling?"
 *
 * Without this, each controller needs its own try/catch blocks, leading to:
 * 1. Duplicated error handling code across 14+ controllers
 * 2. Inconsistent error messages and HTTP status codes
 * 3. Risk of unhandled exceptions showing raw stack traces to users
 *
 * With @ControllerAdvice:
 * 1. Single place to define error handling logic
 * 2. Consistent error pages and messages
 * 3. Centralized logging for debugging
 *
 * "Why not handle REST API errors here?"
 * - AddressController uses @RestController and needs JSON responses
 * - This handler returns HTML error pages (ModelAndView)
 * - Mixing them would break the frontend JavaScript
 * - REST-specific errors are handled locally in AddressController
 *
 * "Why catch RuntimeException separately from Exception?"
 * - RuntimeException subtypes (IllegalArgumentException, IllegalStateException)
 * are usually validation/business logic errors → 400-level
 * - Generic Exception is usually an unexpected server error → 500-level
 * - Different logging levels: WARN for business errors, ERROR for unexpected
 * ones
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle business logic errors (invalid arguments).
     * Example: "Product not found", "Invalid email format"
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Business logic error: {}", ex.getMessage());
        return buildErrorView("Yêu cầu không hợp lệ", ex.getMessage());
    }

    /**
     * Handle invalid state transitions.
     * Example: "Can only approve orders with PENDING status"
     */
    @ExceptionHandler(IllegalStateException.class)
    public ModelAndView handleIllegalState(IllegalStateException ex) {
        log.warn("Invalid state: {}", ex.getMessage());
        return buildErrorView("Thao tác không hợp lệ", ex.getMessage());
    }

    /**
     * Handle general runtime exceptions.
     * Example: "Order not found", "Cart is empty"
     */
    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error: {}", ex.getMessage(), ex);
        return buildErrorView("Đã xảy ra lỗi", ex.getMessage());
    }

    /**
     * Handle all unhandled exceptions (fallback).
     * This is the safety net - no exception should ever leak to the user
     * as a raw stack trace.
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorView(
                "Lỗi hệ thống",
                "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.");
    }

    // ===== PRIVATE HELPER =====

    private ModelAndView buildErrorView(String title, String message) {
        ModelAndView mav = new ModelAndView("error/general");
        mav.addObject("errorTitle", title);
        mav.addObject("errorMessage", message);
        return mav;
    }
}
