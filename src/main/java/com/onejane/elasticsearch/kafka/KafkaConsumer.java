package com.onejane.elasticsearch.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @program: springboot-elasticsearch
 * @description: kafka消费者
 * @author: OneJane
 * @create: 2020-01-16 14:06
 **/
@Component
@Slf4j
public class KafkaConsumer {
    @KafkaListener(topics = KafkaConstant.TOPIC_TEST,groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ConsumerRecord<?, String> record) {
        String value = record.value();
        log.info("value = {}", value);
    }
}
