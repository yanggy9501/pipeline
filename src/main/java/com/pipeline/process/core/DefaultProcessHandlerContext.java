package com.pipeline.process.core;

final class DefaultProcessHandlerContext extends AbstractProcessHandlerContext {

    private final ProcessHandler processHandler;

    public DefaultProcessHandlerContext(DefaultProcessPipeline pipeline, String name, ProcessHandler processHandler) {
        super(pipeline, name);
        this.processHandler = processHandler;
    }

    @Override
    public ProcessHandler handler() {
        return processHandler;
    }
}
