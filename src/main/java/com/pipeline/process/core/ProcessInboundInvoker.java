package com.pipeline.process.core;

public interface ProcessInboundInvoker {
    ProcessInboundInvoker fireDoProcess(Object msg);
}
