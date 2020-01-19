package com.onejane.elasticsearch.entry;

import java.util.HashMap;
import java.util.Map;


public class EntryHandlerFactory {

    private Map<String, AbstractEntryHandler> handlerMap = new HashMap<>();

    private AbstractEntryHandler defaultHandler;

    public EntryHandlerFactory() {
    }

    public EntryHandlerFactory(Map<String, AbstractEntryHandler> handlerMap) {
        this(handlerMap, null);
    }

    public EntryHandlerFactory(Map<String, AbstractEntryHandler> handlerMap, AbstractEntryHandler defaultHandler) {
        this.handlerMap = handlerMap;
        this.defaultHandler = defaultHandler;
    }

    public AbstractEntryHandler getOrDefaultHandler(String tableName) {
        AbstractEntryHandler handler = handlerMap.get(tableName);
        if (null != handler)
            return handler;
        return defaultHandler;
    }

    public Map<String, AbstractEntryHandler> getHandlerMap() {
        return handlerMap;
    }

    public void setHandlerMap(Map<String, AbstractEntryHandler> handlerMap) {
        this.handlerMap = handlerMap;
    }

    public AbstractEntryHandler getDefaultHandler() {
        return defaultHandler;
    }

    public void setDefaultHandler(AbstractEntryHandler defaultHandler) {
        this.defaultHandler = defaultHandler;
    }
}
