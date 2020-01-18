package com.onejane.elasticsearch.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author: shimh
 * @create: 2019年10月
 **/
@ConfigurationProperties("canal")
@Data
public class CanalProperties {
    private String ip;

    private Integer port;

    private String destination;

    private String username;
    private String password;

    private String filter = "";
    private Integer batchSize = 1000;
    private Long timeout;
    private TimeUnit unit;

}
