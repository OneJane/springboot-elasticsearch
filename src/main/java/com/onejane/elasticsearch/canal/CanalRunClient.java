package com.onejane.elasticsearch.canal;

import com.onejane.elasticsearch.client.SimpleCanalClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @program: springboot-elasticsearch
 * @description: CanalTask启动类
 * @author: OneJane
 * @create: 2020-01-18 09:23
 **/
@Component
public class CanalRunClient implements ApplicationRunner {

    @Autowired
    private SimpleCanalClient canalClient;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        canalClient.start();

    }
}