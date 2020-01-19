package com.onejane.elasticsearch.support;

import com.onejane.elasticsearch.entry.AbstractEntryHandler;
import org.springframework.kafka.core.KafkaTemplate;


public class KafkaSearchEntryHandler extends AbstractEntryHandler<String> {

    KafkaTemplate kafkaTemplate;

    public KafkaSearchEntryHandler(KafkaTemplate kafkaTemplate) {
        super(new JsonStringRowDataHandler());
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    protected void doInsert(String after) {
        System.out.println("kafka发送消息更新/添加");
        kafkaTemplate.send("thing1", after);
    }

    @Override
    protected void doUpdate(String before, String after) {
        doInsert(after);
    }

    @Override
    protected void doDelete(String before) {
        System.out.println("kafka发送消息删除");
        kafkaTemplate.send("thing2", before);
    }



}
