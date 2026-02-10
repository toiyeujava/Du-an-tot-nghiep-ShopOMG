package poly.edu.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Verifies that each exception type maps to the correct error view and message.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleIllegalArgument_returnsCorrectView() {
        ModelAndView result = handler.handleIllegalArgument(
                new IllegalArgumentException("Invalid product ID"));

        assertEquals("error/general", result.getViewName());
        assertEquals("Yêu cầu không hợp lệ", result.getModel().get("errorTitle"));
        assertEquals("Invalid product ID", result.getModel().get("errorMessage"));
    }

    @Test
    void handleIllegalState_returnsCorrectView() {
        ModelAndView result = handler.handleIllegalState(
                new IllegalStateException("Can only approve PENDING orders"));

        assertEquals("error/general", result.getViewName());
        assertEquals("Thao tác không hợp lệ", result.getModel().get("errorTitle"));
        assertEquals("Can only approve PENDING orders", result.getModel().get("errorMessage"));
    }

    @Test
    void handleRuntimeException_returnsCorrectView() {
        ModelAndView result = handler.handleRuntimeException(
                new RuntimeException("Order not found"));

        assertEquals("error/general", result.getViewName());
        assertEquals("Đã xảy ra lỗi", result.getModel().get("errorTitle"));
        assertEquals("Order not found", result.getModel().get("errorMessage"));
    }

    @Test
    void handleGenericException_returnsGenericMessage() {
        ModelAndView result = handler.handleGenericException(
                new Exception("Unexpected database error"));

        assertEquals("error/general", result.getViewName());
        assertEquals("Lỗi hệ thống", result.getModel().get("errorTitle"));
        // Should NOT expose internal error message to users
        assertTrue(result.getModel().get("errorMessage").toString().contains("Vui lòng thử lại"));
    }
}
