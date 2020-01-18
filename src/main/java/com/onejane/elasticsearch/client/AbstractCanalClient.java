package com.onejane.elasticsearch.client;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import com.onejane.elasticsearch.message.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author: shimh
 * @create: 2019年11月
 **/
public abstract class AbstractCanalClient {

    protected final static Logger logger = LoggerFactory.getLogger(AbstractCanalClient.class);
    private static final String DEFAULT_THREAD_NAME = "canal-client-worker";

    protected String threadName = DEFAULT_THREAD_NAME;

    protected String filter = "";
    protected Integer batchSize = 1000;
    protected Long timeout;
    protected TimeUnit unit;

    protected CanalConnector connector;

    protected MessageHandler messageHandler;

    protected Thread thread = null;

    protected volatile boolean running = false;

    protected Thread.UncaughtExceptionHandler handler = (t, e) -> logger.error("parse events has an error", e);

    public void start() {
        thread = new Thread(this :: process);
        thread.setName(threadName);
        thread.setUncaughtExceptionHandler(handler);
        running = true;
        thread.start();
        logger.info("canal client has started");
    }

    protected void process() {
        while (running) {
            try {
                connector.connect();
                connector.subscribe(filter);
                logger.info("canal client subscribe：" + filter);
                while (running) {
                    Message message = connector.getWithoutAck(batchSize, timeout, unit);
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        // try {
                        // Thread.sleep(1000);
                        // } catch (InterruptedException e) {
                        // }
                    } else {
                        messageHandler.handler(message);
                    }

                    if (batchId != -1) {
                        connector.ack(batchId);
                    }
                }
            } catch (Exception e) {
                logger.error("process error!", e);
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e1) {
                    // ignore
                }
            } finally {
                connector.disconnect();
            }
        }
    }

    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public CanalConnector getConnector() {
        return connector;
    }

    public void setConnector(CanalConnector connector) {
        this.connector = connector;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

}
