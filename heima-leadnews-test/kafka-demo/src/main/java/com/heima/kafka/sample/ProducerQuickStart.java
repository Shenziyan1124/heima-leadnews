package com.heima.kafka.sample;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;

import java.util.Properties;


@Slf4j
public class ProducerQuickStart {

    public static void main(String[] args) {
        // 1.kafka链接配置信息
        Properties properties = new Properties();
        // kafka的链接地址
        properties.put("bootstrap.servers", "192.168.200.130:9092");
        // key value 序列化
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        //ack 消息确认队列
        properties.put(ProducerConfig.ACKS_CONFIG, "all");

        // 重试次数
        properties.put(ProducerConfig.RETRIES_CONFIG, 3);

        // 数据压缩
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");

        // 2.创建kafka生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        // 3.发送消息
        /**
         * 第一个参数：topic
         * 第二个参数：key
         * 第三个参数：value
         */

        for (int i = 0; i < 10; i++){
            ProducerRecord<String, String> record =
                    new ProducerRecord<String, String>("itcast-topic-input", "hello kafka"+i);
            producer.send(record);
        }
        //ProducerRecord<String, String> record =
        //        new ProducerRecord<String, String>("topic-first", "key-001", "hello kafka");
        // 同步发送,容易产生阻塞
        //producer.send(record);
        // 异步发送
        //producer.send(record, new Callback() {
        //    @Override
        //    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        //        if (e != null){
        //            log.error("发送失败", e,"记录信息异常");
        //        }
        //        log.info("发送成功","记录信息正常");
        //        System.out.println(recordMetadata.offset());
        //    }
        //});

        // 4.关闭通道
        producer.close();
    }
}
