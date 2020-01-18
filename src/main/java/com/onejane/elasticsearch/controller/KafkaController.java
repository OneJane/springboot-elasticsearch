package com.onejane.elasticsearch.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: shimh
 * @create: 2019年11月
 **/
@Slf4j
@RestController
public class KafkaController {


    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    BulkProcessor bulkProcessor;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    volatile int aa = 0; // 统计个大体的数

    @GetMapping("send")
    public String send(String topic, String value) {
        kafkaTemplate.send(topic, value);
        return "";
    }

    @KafkaListener(id="thing1", idIsGroup = false, clientIdPrefix="thing1", topics = {"thing1"}, containerFactory = "batchKafkaListenerContainerFactory")
    public void listenThing1(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        aa += records.size();
        log.info("线程：{} size：{}", Thread.currentThread().getName(), aa);
        for(ConsumerRecord<String, String> record: records) {
            insertEs(record);
        }
        acknowledgment.acknowledge();
    }

    @KafkaListener(id="thing2", idIsGroup = false, clientIdPrefix="thing2", topics = {"thing2"})
    public void listenThing2(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        aa += 1;
        log.info("线程：{} size：{}", Thread.currentThread().getName(), aa);
        String after = record.value();
        JSONObject object = JSON.parseObject(after);
        Object id = object.get("id");
        elasticsearchTemplate.delete("test", "test", id.toString());
        acknowledgment.acknowledge();
    }

    @KafkaListener(id="thing3", idIsGroup = false, clientIdPrefix="thing3", topics = {"thing3"}, concurrency = "3")
    public void listenThing3(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        aa += 1;
        log.info("线程：{} size：{}", Thread.currentThread().getName(), aa);
        insertEs(record);
        acknowledgment.acknowledge();
    }

    private void insertEs(ConsumerRecord<String,String> record) {
        String after = record.value();
        JSONObject object = JSON.parseObject(after);
        Object id = object.get("id");
        if (id != null) {
            IndexRequest request = new IndexRequest("test", "test", id.toString());
            request.source(after, XContentType.JSON);
            bulkProcessor.add(request);
        }
    }

}
