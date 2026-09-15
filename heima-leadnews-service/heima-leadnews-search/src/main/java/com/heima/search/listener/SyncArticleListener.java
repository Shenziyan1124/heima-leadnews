package com.heima.search.listener;

import com.alibaba.fastjson.JSON;
import com.heima.model.search.vos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static com.heima.common.constants.ArticleConstants.ARTICLE_ES_SYNC_TOPIC;

@Component
@Slf4j
public class SyncArticleListener {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 接收消息
     *
     * @param message
     */
    @KafkaListener(topics = ARTICLE_ES_SYNC_TOPIC)
    public void syncArticle(String message) throws IOException {
        log.info("search-service接收到Kafka article-service消息：{}", message);
        if (StringUtils.isNotBlank(message)) {
            SearchArticleVo vo = JSON.parseObject(message, SearchArticleVo.class);

            IndexRequest request = new IndexRequest("app_info_article");
            request.id(vo.getId().toString());
            request.source(message, XContentType.JSON);

            client.index(request, RequestOptions.DEFAULT);
            log.info("search-service同步文章到ES成功：{}", vo.getId());
        }
    }
}
