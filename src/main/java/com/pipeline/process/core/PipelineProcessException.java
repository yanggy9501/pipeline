package com.pipeline.process.core;

class PipelineProcessException extends RuntimeException {

    public PipelineProcessException(Throwable cause) {
        super(cause);
    }

    public PipelineProcessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
