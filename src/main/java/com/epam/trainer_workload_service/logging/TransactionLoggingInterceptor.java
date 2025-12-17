package com.epam.trainer_workload_service.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TransactionLoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(TransactionLoggingInterceptor.class);
    private static final String TRANSACTION_ID = "transactionId";
    private static final String INCOMING_REQUEST = "Incoming request [{}] {} transactionId={}";
    private static final String FAILED_REQUEST = "Request failed [{}] {} transactionId={} status={}";
    private static final String COMPLETED_REQUEST = "Request completed [{}] {} status={} transactionId={}";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response,
                             @NotNull Object handler) {

        String transactionId = request.getHeader(TRANSACTION_ID);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TRANSACTION_ID, transactionId);

        log.info(
                INCOMING_REQUEST,
                request.getMethod(),
                request.getRequestURI(),
                transactionId
        );

        response.setHeader(TRANSACTION_ID, transactionId);

        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request,
                                @NotNull HttpServletResponse response,
                                @NotNull Object handler,
                                Exception ex) {

        if (ex != null) {
            log.error(
                    FAILED_REQUEST,
                    request.getMethod(),
                    request.getRequestURI(),
                    MDC.get(TRANSACTION_ID),
                    response.getStatus(),
                    ex
            );
        } else {
            log.info(
                    COMPLETED_REQUEST,
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    MDC.get(TRANSACTION_ID)
            );
        }

        MDC.clear();
    }
}

