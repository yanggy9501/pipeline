package com.pipeline.process.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProcessInBoundHandlerAdapter implements ProcessInBoundHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInBoundHandlerAdapter.class);

    @Override
    public Object beforeProcess(ProcessHandlerContext ctx, Object msg) throws Exception {
        return new Object();
    }

    @Override
    public abstract void process(ProcessHandlerContext ctx, Object msg) throws Exception;

    @Override
    public void doProcess(ProcessHandlerContext ctx, Object msg) throws Exception {
        if (this.beforeProcess(ctx, msg) == null) {
            return;
        }
        this.process(ctx, msg);
    }

    @Override
    public void exceptionCaught(ProcessHandlerContext ctx, Throwable cause) {
        LOGGER.error(" user handler is error: {} please impl exceptionCaught to handle error", cause);
        throw new PipelineProcessException(cause);
    }
}
