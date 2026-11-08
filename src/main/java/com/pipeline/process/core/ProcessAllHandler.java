package com.pipeline.process.core;

public abstract class ProcessAllHandler extends ProcessInBoundHandlerAdapter {
    @Override
    public abstract void process(ProcessHandlerContext ctx, Object msg) throws Exception;
}
