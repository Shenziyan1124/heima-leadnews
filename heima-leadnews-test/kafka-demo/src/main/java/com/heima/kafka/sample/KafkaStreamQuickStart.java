package com.heima.kafka.sample;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

public class KafkaStreamQuickStart {
    public static void main(String[] args) {
        // kafka配置中心
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"192.168.200.130:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,"streams-quickstart");

        // stream构建器
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        //流式计算
        streamProcessor(streamsBuilder);
        

        // kafka对象
        KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(),props);
        // 开启流式计算
        kafkaStreams.start();
    }

    private static void streamProcessor(StreamsBuilder streamsBuilder) {
        // 创建kstream对象,同时指定从哪个topic中接受消息
        KStream<String, String> stream = streamsBuilder.stream("itcast-topic-input");
        stream.flatMapValues(new ValueMapper<String, Iterable<String>>() {
            @Override
            public Iterable<String> apply(String value) {
                return Arrays.asList(value.split(" "));
            }
        })
                // 按照value聚合处理
                .groupBy((key, value) -> value)
                // 时间窗口
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                // value计数
                .count()
                // 转换kstream
                .toStream()
                .map((key, value) ->

                {
                    System.out.println(key.key() + " : " + value);
                    return new KeyValue<>(key.key().toString(), value.toString());
                })
                // 发送消息
                .to("itcast-topic-output");
    }

}
