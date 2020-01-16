package com.onejane.elasticsearch.kafka;

import com.alibaba.fastjson.JSONObject;
import com.onejane.elasticsearch.bean.Student;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * @program: springboot-elasticsearch
 * @description: kafka提供者
 * @author: OneJane
 * @create: 2020-01-16 14:15
 **/
@Slf4j
@Component
@EnableScheduling
public class KafkaProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Scheduled(cron = "0/10 * * * * ?")
    public void send() {
        Student dotaHero = new Student(RandomStringUtils.randomAlphanumeric(5), 1);

        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(KafkaConstant.TOPIC_TEST, JSONObject.toJSONString(dotaHero));

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                log.error("kafka sendMessage error, throwable = {}, topic = {}, data = {}", throwable, KafkaConstant.TOPIC_TEST, dotaHero);
            }

            @Override
            public void onSuccess(SendResult<String, String> stringDotaHeroSendResult) {
                log.info("kafka sendMessage success topic = {}, data = {}",KafkaConstant.TOPIC_TEST, dotaHero);
            }
        });
    }
}
