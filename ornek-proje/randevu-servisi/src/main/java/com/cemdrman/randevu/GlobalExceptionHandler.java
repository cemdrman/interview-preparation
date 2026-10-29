package com.cemdrman.randevu;

import com.cemdrman.randevu.client.DoktorBulunamadiException;
import com.cemdrman.randevu.randevu.IsKuraliException;
import com.cemdrman.randevu.randevu.RandevuBulunamadiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DoktorBulunamadiException.class, RandevuBulunamadiException.class})
    public ProblemDetail bulunamadi(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IsKuraliException.class)
    public ProblemDetail isKurali(IsKuraliException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }
}
