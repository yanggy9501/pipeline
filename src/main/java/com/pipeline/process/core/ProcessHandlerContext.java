package com.pipeline.process.core;

import java.util.Map;

public interface ProcessHandlerContext extends ProcessInboundInvoker {

    String name();

    ProcessHandler handler();

    @Override
    ProcessHandlerContext fireDoProcess(Object msg);

    ProcessPipeline pipeline();

    void attach(Map map);

    Map getAttach();
}
