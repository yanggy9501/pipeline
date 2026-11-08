package com.pipeline.process.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;


abstract class AbstractProcessHandlerContext implements ProcessHandlerContext {


    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProcessHandlerContext.class);
    private final DefaultProcessPipeline pipeline;
    private final String name;
    volatile AbstractProcessHandlerContext next;
    volatile AbstractProcessHandlerContext prev;

    ThreadLocal<Map> attach = ThreadLocal.withInitial(() -> new ConcurrentHashMap<>(32));

    public AbstractProcessHandlerContext(DefaultProcessPipeline pipeline, String name) {
        Assert.notNull(name, "process handler name is null");
        this.pipeline = pipeline;
        this.name = name;
    }

    @Override
    public ProcessPipeline pipeline() {
        return pipeline;
    }

    @Override
    public void attach(Map map) {
        attach.set(map);
    }

    @Override
    public Map getAttach() {
        Map<String, Object> attachMap = attach.get();
        return attachMap;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public ProcessHandlerContext fireDoProcess(Object msg) {
        invokeDoProcess(findContextInBound(), msg);
        return this;
    }

    private AbstractProcessHandlerContext findContextOutBound() {
        AbstractProcessHandlerContext ctx = this;
        return ctx.prev;
    }

    static void invokeDoProcess(AbstractProcessHandlerContext contextInBound, Object msg) {
        contextInBound.invokeDoProcess(msg);
    }

    private void invokeDoProcess(Object msg) {
        try {
            ((ProcessInBoundHandler) handler()).doProcess(this, msg);
        } catch (Throwable t) {
            fireException(t);
        } finally {
            this.attach.remove();
        }
    }

    // FIXME 了解执行机制话，知道context是一个链条。
    //  handler抛出异常后，
    //  context会从后往前抛出异常，但是如果前面的handler实现了exceptionCaught
    //  这样后面的handler的异常就会到前面的handler的catch方法，这不合理。
    //  所以handler抛出异常后，进行包装成特定的异常`PipelineProcessException`,属于特定异常时一直抛出
    private void fireException(Throwable t) {
        if (t instanceof PipelineProcessException) {
            throw new PipelineProcessException(t);
        } else {
            invokeExceptionCaught(t);
        }
    }

    private void invokeExceptionCaught(Throwable cause) {
        try {
            ((ProcessInBoundHandler) handler()).exceptionCaught(this, cause);
        } catch (Throwable error) {
            throw new PipelineProcessException(error);
        }

    }

    private AbstractProcessHandlerContext findContextInBound() {
        AbstractProcessHandlerContext ctx = this;
        return ctx.next;
    }
}
