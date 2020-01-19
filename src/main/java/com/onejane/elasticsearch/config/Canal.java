package com.onejane.elasticsearch.config;

import com.onejane.elasticsearch.client.SimpleCanalClient;
import com.onejane.elasticsearch.entry.EntryHandlerFactory;
import com.onejane.elasticsearch.support.KafkaSearchEntryHandler;
import com.onejane.elasticsearch.message.SimpleMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;


@Configuration
@EnableConfigurationProperties(CanalProperties.class)
@Slf4j
public class Canal {

    @Autowired
    CanalProperties canalProperties;

    @Bean
    public EntryHandlerFactory entryHandlerFactory(BulkProcessor bulkProcessor, KafkaTemplate kafkaTemplate) {
        EntryHandlerFactory factory = new EntryHandlerFactory();
        // factory.setDefaultHandler(new ElasticSearchEntryHandler(bulkProcessor));
        factory.setDefaultHandler(new KafkaSearchEntryHandler(kafkaTemplate));

        return factory;
    }


    @Bean
    public SimpleMessageHandler messageHandler(EntryHandlerFactory entryHandlerFactory) {
        return new SimpleMessageHandler(entryHandlerFactory);
    }

    @Bean
    public SimpleCanalClient simpleCanalClient(SimpleMessageHandler messageHandler) {
        SimpleCanalClient client = new SimpleCanalClient(canalProperties.getIp(), canalProperties.getPort(), canalProperties.getDestination(), canalProperties.getUsername(), canalProperties.getPassword(), messageHandler);
        client.setBatchSize(canalProperties.getBatchSize());
        client.setFilter(canalProperties.getFilter());
        client.setTimeout(canalProperties.getTimeout());
        client.setUnit(canalProperties.getUnit());
        return client;
    }


    @Bean
    public BulkProcessor bulkProcessor(TransportClient client) {
        return BulkProcessor.builder(
                client,
                new BulkProcessor.Listener() {

                    @Override
                    public void beforeBulk(long executionId, BulkRequest request) {
                        log.info("---尝试插入{}条数据---", request.numberOfActions());
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request, BulkResponse response) {
                        log.info("---尝试插入{}条数据成功---", request.numberOfActions());
                    }

                    @Override
                    public void afterBulk(long executionId,
                                          BulkRequest request, Throwable failure) {
                        log.error("[es错误]---尝试插入数据失败---", failure);
                    }

                })
                .setBulkActions(5000)
                .setBulkSize(new ByteSizeValue(5, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(3))
                .setConcurrentRequests(1)
                .build();
    }

}
