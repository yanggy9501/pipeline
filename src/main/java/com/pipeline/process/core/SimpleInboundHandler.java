package com.pipeline.process.core;

public abstract class SimpleInboundHandler<I> extends ProcessInBoundHandlerAdapter {

    private static final Object beforeProcessOkFlag = new Object();

    @Override
    public Object beforeProcess(ProcessHandlerContext ctx, Object msg) throws Exception {
        if (beforeHandle(ctx, (I) msg)) {
            return beforeProcessOkFlag;
        }
        return null;
    }

    @Override
    public void process(ProcessHandlerContext processHandlerContext, Object o) throws Exception {
        handle(processHandlerContext, (I) o);
    }

    public Boolean beforeHandle(ProcessHandlerContext ctx, I inputParams) throws Exception {
        return Boolean.TRUE;
    }


    public abstract void handle(ProcessHandlerContext ctx, I inputParams) throws Exception;
}
