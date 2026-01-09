package com.epam.trainer_workload_service.service;

public class ServiceException extends RuntimeException  {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
