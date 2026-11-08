package com.pipeline.process.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;

import com.pipeline.process.util.StringUtil;

public class DefaultProcessPipeline implements ProcessPipeline {


    private static final ThreadLocal<Map<Class<?>, String>> nameCaches =
            new ThreadLocal<Map<Class<?>, String>>() {
                @Override
                protected Map<Class<?>, String> initialValue() {
                    return new ConcurrentHashMap<>(32);
                }
            };
    final AbstractProcessHandlerContext head;
    final AbstractProcessHandlerContext tail;
    final String headName = "head";
    final String tailName = "tail";
    Object resultKey;

    public DefaultProcessPipeline() {
        head = new InitHeadContext(this, headName);
        tail = new InitTailContext(this, tailName);

        head.next = tail;
        tail.prev = head;
    }

    @Override
    public ProcessPipeline addLast(String name, ProcessHandler processHandler) {
        final AbstractProcessHandlerContext newCtx;
        synchronized (this) {
            newCtx = newContext(filterName(name, processHandler), processHandler);
            addLast0(newCtx);
        }
        return this;
    }

    private void addLast0(AbstractProcessHandlerContext newCtx) {
        AbstractProcessHandlerContext prev = tail.prev;
        newCtx.prev = prev;
        newCtx.next = tail;
        prev.next = newCtx;
        tail.prev = newCtx;
    }


    private String filterName(String name, ProcessHandler handler) {
        if (name == null) {
            return generateName(handler);
        }
        checkDuplicateName(name);
        return name;
    }

    private void checkDuplicateName(String name) {
        if (context0(name) != null) {
            throw new IllegalArgumentException("Duplicate handler name: " + name);
        }
    }

    private String generateName(ProcessHandler handler) {
        Map<Class<?>, String> cache = nameCaches.get();
        Class<?> handlerType = handler.getClass();
        String name = cache.get(handlerType);
        if (name == null) {
            name = generateName0(handlerType);
            cache.put(handlerType, name);
        }

        if (context0(name) != null) {
            String baseName = name.substring(0, name.length() - 1);
            for (int i = 1; ; i++) {
                String newName = baseName + i;
                if (context0(newName) == null) {
                    name = newName;
                    break;
                }
            }
        }
        return name;
    }

    private AbstractProcessHandlerContext context0(String name) {
        AbstractProcessHandlerContext context = head.next;
        while (context != tail) {
            if (context.name().equals(name)) {
                return context;
            }
            context = context.next;
        }
        return null;
    }


    private static String generateName0(Class<?> handlerType) {
        return StringUtil.simpleClassName(handlerType) + "#0";
    }

    private DefaultProcessHandlerContext newContext(String name, ProcessHandler processHandler) {
        return new DefaultProcessHandlerContext(this, name, processHandler);
    }


    @Override
    public ProcessHandler get(String name) {
        Assert.notNull(name, "name is not null");
        AbstractProcessHandlerContext handlerContext = context0(name);
        if (handlerContext == null) {
            return null;
        }
        return handlerContext.handler();
    }

    @Override
    public <T extends ProcessHandler> T get(Class<T> handlerType) {
        ProcessHandlerContext context = context(handlerType);
        if (context == null) {
            return null;
        }
        return (T) context.handler();
    }

    @Override
    public ProcessHandlerContext context(ProcessHandler handler) {
        Assert.notNull(handler, "handler is not null");

        AbstractProcessHandlerContext ctx = head.next;
        for (; ; ) {

            if (ctx == null) {
                return null;
            }

            if (ctx.handler() == handler) {
                return ctx;
            }

            ctx = ctx.next;
        }
    }

    @Override
    public final ProcessHandlerContext context(Class<? extends ProcessHandler> handlerType) {
        Assert.notNull(handlerType, "handlerType is not null");

        AbstractProcessHandlerContext ctx = head.next;
        for (; ; ) {
            if (ctx == null) {
                return null;
            }
            if (handlerType.isAssignableFrom(ctx.handler().getClass())) {
                return ctx;
            }
            ctx = ctx.next;
        }
    }

    @Override
    public List<String> names() {
        ArrayList<String> names = new ArrayList<>();
        AbstractProcessHandlerContext ctx = head.next;
        for (; ; ) {
            if (ctx == null || ctx == tail) {
                return names;
            }
            names.add(ctx.name());
            ctx = ctx.next;
        }
    }


    @Override
    public ProcessPipeline fireDoProcess(Object msg) {
        AbstractProcessHandlerContext.invokeDoProcess(head, msg);
        return this;
    }

    @Override
    public Object getFinalValue() {
        return resultKey;
    }

    @Override
    public void setFinalValue(Object obj) {
        this.resultKey = obj;
    }

    @Override
    public Iterator<Map.Entry<String, ProcessHandler>> iterator() {
        return toMap().entrySet().iterator();
    }

    public final Map<String, ProcessHandler> toMap() {
        Map<String, ProcessHandler> map = new LinkedHashMap<String, ProcessHandler>();
        AbstractProcessHandlerContext ctx = head.next;
        for (; ; ) {
            if (ctx == tail) {
                return map;
            }
            map.put(ctx.name(), ctx.handler());
            ctx = ctx.next;
        }
    }

    @Override
    public final String toString() {
        StringBuilder buf = new StringBuilder()
                .append(StringUtil.simpleClassName(this))
                .append('{');
        AbstractProcessHandlerContext ctx = head.next;
        for (; ; ) {
            if (ctx == tail) {
                break;
            }

            buf.append('(')
                    .append(ctx.name())
                    .append(" = ")
                    .append(ctx.handler().getClass().getName())
                    .append(')');

            ctx = ctx.next;
            if (ctx == tail) {
                break;
            }

            buf.append(", ");
        }
        buf.append('}');
        return buf.toString();
    }


    final class InitHeadContext extends AbstractProcessHandlerContext
            implements ProcessInBoundHandler {
        InitHeadContext(DefaultProcessPipeline pipeline, String name) {
            super(pipeline, name);
        }

        @Override
        public Object beforeProcess(ProcessHandlerContext ctx, Object msg) {
            return new Object();
        }

        @Override
        public void process(ProcessHandlerContext ctx, Object msg) {

        }

        @Override
        public void doProcess(ProcessHandlerContext ctx, Object msg) {
            ctx.fireDoProcess(msg);
        }

        @Override
        public void exceptionCaught(ProcessHandlerContext ctx, Throwable cause) {
            throw new RuntimeException(cause);
        }

        @Override
        public ProcessHandler handler() {
            return this;
        }
    }


    final class InitTailContext extends AbstractProcessHandlerContext
            implements ProcessInBoundHandler {
        InitTailContext(DefaultProcessPipeline pipeline, String name) {
            super(pipeline, name);
        }


        @Override
        public Object beforeProcess(ProcessHandlerContext ctx, Object msg) {
            return null;
        }

        @Override
        public void process(ProcessHandlerContext ctx, Object msg) {

        }

        @Override
        public void doProcess(ProcessHandlerContext ctx, Object msg) {
        }

        @Override
        public void exceptionCaught(ProcessHandlerContext ctx, Throwable cause) {
            throw new RuntimeException(cause);
        }

        @Override
        public ProcessHandler handler() {
            return this;
        }
    }
}
