package com.pipeline.process.core;

public interface ProcessInBoundHandler extends ProcessHandler {

    Object beforeProcess(ProcessHandlerContext ctx, Object msg) throws Exception;

    void process(ProcessHandlerContext ctx, Object msg) throws Exception;

    void doProcess(ProcessHandlerContext ctx, Object msg) throws Exception;

    void exceptionCaught(ProcessHandlerContext ctx, Throwable cause);
}
