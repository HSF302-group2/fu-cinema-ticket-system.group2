
package com.fu.cinema.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.support.RequestContextUtils;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ResourceNotFoundException.class,
            NoResourceFoundException.class,
            MethodArgumentTypeMismatchException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Exception ex) {
        log.debug("Not found: {}", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(
            BusinessException ex,
            HttpServletRequest request
    ) {
        RequestContextUtils.getOutputFlashMap(request)
                .put("error", ex.getMessage());

        String referer = request.getHeader("Referer");

        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }

        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOther(Exception ex) {
        log.error("Unexpected error", ex);
        return "error/500";
    }
}
