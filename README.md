# es
chmod 777 /opt/data

docker run -d -v /opt/es/config/es.yml:/usr/share/elasticsearch/config/elasticsearch.yml -v /opt/es/data/:/usr/share/elasticsearch/data --name es-node1 --net host --privileged elasticsearch:6.4.0

elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v6.4.0/elasticsearch-analysis-ik-6.4.0.zip

elasticsearch-plugin install  file:///usr/share/elasticsearch/data/elasticsearch-analysis-ik-6.4.0.zip

vim /opt/es/config/es.yml

```
cluster.name: cluster
node.name: es1
network.bind_host: 0.0.0.0
network.publish_host: 10.33.72.81
http.port: 9200
transport.tcp.port: 9300
http.cors.enabled: true
http.cors.allow-origin: "*"
node.master: true
node.data: true
discovery.zen.ping.unicast.hosts: ["10.33.72.81:9300"]  # 可配置多个
discovery.zen.minimum_master_nodes: 1
#cluster.initial_master_nodes: ["es1"]
```

# mysql

docker run -p 3306:3306 --name mysql -v /opt/mysql/conf:/etc/mysql -v /opt/mysql/logs:/var/log/mysql -v /opt/mysql/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=123456 -d mysql:5.7

vim /opt/mysql/conf/my.cnf

```
[mysqld]
log-bin=mysql-bin
binlog-format=ROW
server_id=1
lower_case_table_names=1
```

docker exec -it mysql bash

```
mysql> CREATE USER canal IDENTIFIED BY 'canal';  
mysql> GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%'; 
mysql> GRANT ALL PRIVILEGES ON *.* TO 'canal'@'%' ; 
mysql> FLUSH PRIVILEGES; 
```

docker restart mysql

```
mysql> show variables like 'binlog_format';
mysql> show variables like '%log_bin%';
mysql> show master status;
```

# kafka

docker-compose.yml

```
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
  kafka:
    image: wurstmeister/kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 10.33.72.81 # 此ip是我自己的内网ip
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

docker-compose up -d

docker exec -it kafka_kafka_1 /bin/bash

```bash
$KAFKA_HOME/bin/kafka-topics.sh --create --topic onejane --zookeeper kafka_zookeeper_1:2181 --replication-factor 1 --partitions 1 创建topic
$KAFKA_HOME/bin/kafka-topics.sh --describe --zookeeper kafka_zookeeper_1 --topic onejane  查看topic信息
$KAFKA_HOME/bin/kafka-console-producer.sh --topic=onejane --broker-list kafka_kafka_1:9092  发布消息
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server kafka_kafka_1:9092 --from-beginning --topic onejane  从头消费
$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server kafka_kafka_1:9092 --partition 0 --offset 2 --topic onejane  按offset消费
$KAFKA_HOME/bin/kafka-topics.sh --delete --topic onejane  --zookeeper kafka_zookeeper_1:2181
```

- RabbitMQ：
  1. 消息堆积的支持并不好，当大量消息积压的时候，会导致RabbitMQ的性能急剧下降。
  2. 每秒钟可以处理几万到十几万条消息。
  3. RabbitMQ使用的编程语言Erlang，二次开发难度大。
  4. 最流行的消息中间之一。
- RocketMQ：
  1. RocketMQ响应时延大多数情况下可以做到毫秒级的响应，适合在线业务场景。
  2. 周边生态系统的集成和兼容程度要略逊一筹。
  3. 支持事务消息。
  4. RocketMQ的每秒钟大概能处理几十万条消息。
- Kafka：
  1. Kafka与在大数据和流计算领域支持很好。
  2. Kafka使用Scala和Java语言开发。
  3. 每秒钟可以处理几十万条消息，Kafka的极限处理能力可以超过每秒2000万条
  4. 同步收发消息的响应时延比较高，不太适合在线业务场景。
- ActiveMQ：
  1. 已经脱离正轨。
- ZeroMQ：
  1. 不是一个完整的消息队列产品

bc756213ec02        kafka_kafka_1       wurstmeister/kafka       
091db369d8bd        kafka_zookeeper_1   wurstmeister/zookeeper   
f98f774ca29e        redis               redis                    
d2dffa29917c        es-node1            elasticsearch:6.4.0      
b4abaa1f4514        mysql               mysql:5.7  

# canal

tar zxf jdk-8u60-linux-x64.tar.gz -C /opt/jdk/ && mv jdk1.8.0_60 jdk

vim /etc/profile

```
JAVA_HOME=/opt/jdk/jdk
JRE_HOME=$JAVA_HOME/jre
PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin
CLASSPATH=:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib/dt.jar
export JAVA_HOME JRE_HOME PATH CLASSPATH
```

tar zxf canal.deployer-1.1.4.tar.gz -C canal

vim canal.properties

```
canal.id = 1
canal.serverMode = tcp
canal.mq.servers = 10.33.72.81:9092
canal.destinations = example
canal.instance.filter.druid.ddl = true
canal.instance.filter.query.dcl = true
canal.instance.filter.query.dml = false
canal.instance.filter.query.ddl = true
canal.instance.filter.table.error = false
```

vim example/instance.properties

```
canal.instance.master.address=10.33.72.81:3306
canal.instance.filter.regex=example.student
canal.mq.topic=example
canal.mq.dynamicTopic=example\\..*
```

sh bin/startup.sh









































































