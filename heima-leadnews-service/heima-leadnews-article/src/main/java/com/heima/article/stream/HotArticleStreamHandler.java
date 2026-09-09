package com.heima.article.stream;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.HotArticleConstants;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.mess.UpdateArticleMess;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Update;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class HotArticleStreamHandler {
    @Bean
    public KStream<String, String> process(StreamsBuilder streamsBuilder) {
        // 从hot_article_score_topic主题中读取数据
        KStream<String, String> stream = streamsBuilder.stream(HotArticleConstants.HOT_ARTICLE_SCORE_TOPIC);
        // stream处理
        stream.map((key, value) -> {
                    UpdateArticleMess mess = JSON.parseObject(value, UpdateArticleMess.class);
                    // 重置消息key:1233121文章id和value:likes:1
                    return new KeyValue<>(mess.getArticleId().toString(), mess.getType().name() + ":" + mess.getAdd());
                })
                // 按照文章id进行聚合
                .groupBy((key, value) -> key)
                // 时间窗口 10s
                .windowedBy(TimeWindows.of(Duration.ofSeconds(10)))
                /**
                 * 自行完成聚合计算,之前使用count来帮我们计算,现在我们有1 或 -1,所以需要自行计算
                 */
                .aggregate(new Initializer<String>() {
                    /**
                     * 初始化方法,返回值是消息的value
                     * @return
                     */
                    @Override
                    public String apply() {
                        return "COLLECTION:0,COMMENT:0,LIKES:0,VIEWS:0";
                    }
                    /**
                     * 真正的聚合方法,返回值是消息的value
                     */
                }, new Aggregator<String, String, String>() {
                    @Override
                    public String apply(String key, String value, String aggregate) {
                        // 判断value是否为空,是则返回COLLECTION:0,COMMENT:0,LIKES:0,VIEWS:0
                        if (StringUtils.isBlank(value)) return aggregate;

                        // 解析aggregate
                        String[] aggAry = aggregate.split(",");
                        int col=0, com=0, like=0, view=0;
                        // agg : COLLECTION:0
                        for (String agg : aggAry) {
                            // s[0] COLLECTION, s[1] 0
                            String[] s = agg.split(":");

                            /**
                             * 根据s[0]的值,判断是收藏,评论,点赞,还是查看,获取时间窗口内的数据
                             */
                            switch (UpdateArticleMess.UpdateArticleType.valueOf(s[0])){
                                case COLLECTION:
                                    col = Integer.parseInt(s[1]);
                                    break;
                                case COMMENT:
                                    com = Integer.parseInt(s[1]);
                                    break;
                                case LIKES:
                                    like = Integer.parseInt(s[1]);
                                    break;
                                case VIEWS:
                                    view = Integer.parseInt(s[1]);
                                    break;
                            }
                        }

                        // 解析value
                        /**
                         * 累加操作
                         */
                        String[] valAry = value.split(":");
                        String type = valAry[0];
                        int add = Integer.parseInt(valAry[1]);
                        switch (UpdateArticleMess.UpdateArticleType.valueOf(type)){
                            case COLLECTION:
                                col += add;
                                break;
                            case COMMENT:
                                com += add;
                                break;
                            case LIKES:
                                like += add;
                                break;
                            case VIEWS:
                                view += add;
                                break;
                        }

                        return String.format("COLLECTION:%d,COMMENT:%d,LIKES:%d,VIEWS:%d", col, com, like, view);
                    }
                }, Materialized.as("hot-article-store-001"))
                .toStream()
                // 处理当前文章,发送过去topic,不知道是哪个文章
                .map((key, value) ->{
                    return new KeyValue<>(key.key().toString(), formatObj(key.key().toString(),value));
                })
                // 输出到hot_article_incr_handle_topic主题
                .to(HotArticleConstants.HOT_ARTICLE_INCR_HANDLE_TOPIC);

        // 返回数据
        return stream;
    }


    /**
     * 格式化数据格式的value数据,最后是articleid+value(collection:0,comment:0,likes:0,views:0)
     * @param articleId
     * @param value
     * @return
     */
    private String formatObj(String articleId, String value) {
        ArticleVisitStreamMess mess = new ArticleVisitStreamMess();
        mess.setArticleId(Long.valueOf(articleId));

        String[] valAry = value.split(",");
        for (String val : valAry) {
            String[] split = val.split(":");
            switch (UpdateArticleMess.UpdateArticleType.valueOf(split[0])){
                case COLLECTION:
                    mess.setCollect(Integer.parseInt(split[1]));
                    break;
                case COMMENT:
                    mess.setComment(Integer.parseInt(split[1]));
                    break;
                case LIKES:
                    mess.setLike(Integer.parseInt(split[1]));
                    break;
                case VIEWS:
                    mess.setView(Integer.parseInt(split[1]));
                    break;
            }
        }
        log.info("聚合消息的处理结果是: {}", mess.toString());
        return JSON.toJSONString(mess);
    }
}
