package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.nntp.Article;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static com.heima.common.constants.ArticleConstants.ARTICLE_ES_SYNC_TOPIC;

@Service
@Slf4j
@Transactional
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {

    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    @Lazy
    private ApArticleService apArticleService;

    /**
     * 生成静态文件上传到minIO中
     *
     * @param apArticle
     * @param content
     */
    @Override
    @Async
    public void buildArticleToMinIO(ApArticle apArticle, String content) {

        if (StringUtils.isNotBlank(content) && apArticle != null){
            Template template = null;
            StringWriter out = new StringWriter();

            try {
                // 2.通过freemarker生成html文件
                template = configuration.getTemplate("article.ftl");
                // 数据模型
                Map<String , Object> apArticleMap = new HashMap<>();
                apArticleMap.put("content", JSONArray.parseArray(content));
                template.process(apArticleMap,out);

            } catch (IOException | TemplateException e) {
                throw new RuntimeException(e);
            }

            // 3.把html上传到minio中
            ByteArrayInputStream in = new ByteArrayInputStream(out.toString().getBytes());
            String path =
                    fileStorageService.uploadHtmlFile(
                            "", apArticle.getId() + ".html", in);

            // 4,修改ap_article表,保存url字段
            apArticleService.update(Wrappers.<ApArticle>lambdaUpdate()
                    .eq(ApArticle::getId, apArticle.getId())
                    .set(ApArticle::getStaticUrl, path));


            // 5. 发送消息,同步es index索引
            createArticleESIndex(apArticle,content,path);
        }

    }


    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    /**
     * 创建文章ES索引
     * @param apArticle
     * @param content
     * @param path
     */
    private void createArticleESIndex(ApArticle apArticle, String content, String path) {
        SearchArticleVo vo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle, vo);
        vo.setContent(content);
        vo.setStaticUrl(path);

        kafkaTemplate.send(ARTICLE_ES_SYNC_TOPIC, JSON.toJSONString(vo));
    }
}
