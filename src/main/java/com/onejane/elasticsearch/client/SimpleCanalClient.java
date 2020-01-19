package com.onejane.elasticsearch.client;

import com.alibaba.otter.canal.client.impl.SimpleCanalConnector;
import com.onejane.elasticsearch.message.MessageHandler;

import java.net.InetSocketAddress;


public class SimpleCanalClient extends AbstractCanalClient {

    public SimpleCanalClient(String ip, Integer port, String destination, String username, String password, MessageHandler messageHandler) {
        this(ip, port, destination, username, password, 60000, 3600000, messageHandler);
    }

    public SimpleCanalClient(String ip, Integer port, String destination, String username, String password , int soTimeout, int idleTimeout, MessageHandler messageHandler) {
        this.connector = new SimpleCanalConnector(new InetSocketAddress(ip,
                port), username, password, destination , soTimeout, idleTimeout);
        this.messageHandler = messageHandler;
    }

}
