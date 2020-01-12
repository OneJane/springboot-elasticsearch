package com.onejane.elasticsearch.client;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @program: springboot-elasticsearch
 * @description: es远程客户端
 * @author: OneJane
 * @create: 2020-01-12 14:30
 **/
@Configuration
@Getter
@Setter
@ComponentScan(basePackageClasses=ESClientSpringFactory.class)
public class ElasticsearchRestClient {

    private  final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchRestClient.class);

    @Value("${spring.data.elasticsearch.connect-num}")
    private Integer connectNum;

    @Value("${spring.data.elasticsearch.connect-per-route}")
    private Integer connectPerRoute;

    @Value("${spring.data.elasticsearch.cluster-outer-nodes}")
    private String hostlist;

    @Bean
    public HttpHost[] httpHost(){
        //解析hostlist配置信息
        String[] split = hostlist.split(",");
        //创建HttpHost数组，其中存放es主机和端口的配置信息
        HttpHost[] httpHostArray = new HttpHost[split.length];
        for(int i=0;i<split.length;i++){
            String item = split[i];
            httpHostArray[i] = new HttpHost(item.split(":")[0], Integer.parseInt(item.split(":")[1]), "http");
        }
        LOGGER.info("init HttpHost");
        return httpHostArray;
    }

    @Bean(initMethod="init",destroyMethod="close")
    public ESClientSpringFactory getFactory(){
        LOGGER.info("ESClientSpringFactory 初始化");
        return ESClientSpringFactory.
                build(httpHost(), connectNum, connectPerRoute);
    }

    @Bean
    @Scope("singleton")
    public RestClient getRestClient(){
        LOGGER.info("RestClient 初始化");
        return getFactory().getClient();
    }

    @Bean(name = "restHighLevelClient")
    @Scope("singleton")
    public RestHighLevelClient getRHLClient(){
        LOGGER.info("RestHighLevelClient 初始化");
        return getFactory().getRhlClient();
    }
}