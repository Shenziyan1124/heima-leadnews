package com.heima.kafka.sample;

import org.apache.kafka.clients.consumer.*;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerQuickStart {
    public static void main(String[] args) {
        //1.kafka的配置信息
        Properties properties = new Properties();
        // kafka的链接地址
        properties.put("bootstrap.servers", "192.168.200.130:9092");
        // consumer的组id
        properties.put("group.id", "consumer-group");
        // consumer的key和value
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        // 手动提交偏移量
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);


        //2.创建消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);


        //3.订阅主题
        consumer.subscribe(Collections.singletonList("topic-first"));

        //4.拉取消息
        while (true) {
            ConsumerRecords<String, String> poll = consumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : poll) {
                System.out.println("topic:" + record.topic() + "|" +
                        "partition:" + record.partition() + "|" +
                        "offset:" + record.offset() + "|" +
                        "key:" + record.key() + "|" +
                        "value:" + record.value());
            }

            //异步失败+同步提交
            try {
                consumer.commitAsync();
            }catch (CommitFailedException e){
                System.out.println("提交失败:{}" + e);
            }
            finally {
                consumer.commitSync();
            }

            // 同步提交偏移量
            //try {
            //    consumer.commitSync();
            //} catch (CommitFailedException e) {
            //    System.out.println("提交失败:{}" + e);
            //}

            // 异步提交偏移量
            //consumer.commitAsync(new OffsetCommitCallback() {
            //    @Override
            //    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
            //        if (e != null) {
            //            System.out.println("记录错误的偏移量:{}" + map + "，错误信息:{}" + e);
            //        }
            //    }
            //});
        }
    }
}
