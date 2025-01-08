package com.gudy.counter.exception;

import com.gudy.counter.bean.res.CounterRes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
@ResponseBody
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public CounterRes exceptionHandler(HttpServletRequest request, Exception e){
        log.error(e);
        return new CounterRes(CounterRes.FAIL,
                "发生错误",
                null);
    }

}
