package com.onejane.elasticsearch.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;


@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<?, ?> batchKafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactoryConfigurer configurer, ConsumerFactory<Object, Object> kafkaConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setBatchListener(true);
        return factory;
    }

    /**** 启动时默认创建主题 ****/
    @Bean
    public NewTopic topic1() {
        return new NewTopic("thing1", 1, (short) 1);
    }

    @Bean
    public NewTopic topic2() {
        return new NewTopic("thing2", 1, (short) 1);
    }

    @Bean
    public NewTopic topic3() {
        return new NewTopic("thing3", 3, (short) 1);
    }

}
