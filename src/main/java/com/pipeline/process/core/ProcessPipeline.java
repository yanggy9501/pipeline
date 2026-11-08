package com.pipeline.process.core;

import java.util.List;
import java.util.Map;

public interface ProcessPipeline extends ProcessInboundInvoker, Iterable<Map.Entry<String, ProcessHandler>> {

    ProcessPipeline addLast(String name, ProcessHandler processHandler);

    ProcessHandler get(String name);

    <T extends ProcessHandler> T get(Class<T> handlerType);

    ProcessHandlerContext context(ProcessHandler handler);

    ProcessHandlerContext context(Class<? extends ProcessHandler> handlerType);

    List<String> names();

    @Override
    ProcessPipeline fireDoProcess(Object msg);

    Object getFinalValue();

    void setFinalValue(Object obj);
}
