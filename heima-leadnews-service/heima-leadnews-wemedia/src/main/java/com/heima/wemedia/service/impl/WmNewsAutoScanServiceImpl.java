package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hankcs.hanlp.utility.SentencesUtil;
import com.heima.apis.article.IArticleClient;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.tess4j.Tess4jClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmChannelService;
import com.heima.wemedia.service.WmNewsAutoScanService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    private final WmNewsMapper wmNewsMapper;

    private final GreenTextScan greenTextScan;
    private final GreenImageScan greenImageScan;
    private final FileStorageService fileStorageService;
    private final IArticleClient articleClient;
    private final WmChannelMapper wmChannelMapper;
    private final WmUserMapper wmUserMapper;
    private final WmSensitiveMapper wmSensitiveMapper; // 敏感词Mapper
    private final Tess4jClient tess4jClient;

    /**
     * 自动审核文章
     *
     * @param id 文章id
     */
    @Override
    //@Async // 异步处理 标明当前方法是一个异步的
    @GlobalTransactional
    public void autoScanWmNews(Integer id) {
        // 1. 根据id查询自媒体文章
        WmNews wmNews = wmNewsMapper.selectById(id);
        if (wmNews == null) {
            throw new RuntimeException("WmNewsAutoScanServiceImpl-autoScanWmNews-文章不存在");
        }

        if (wmNews.getStatus().equals(WmNews.Status.SUBMIT.getCode())) {
            //从内容中提取文本和图片(封面图片和内容图片)
            Map<String, Object> textAndImages = handleTextAndImages(wmNews);

            // 2. 审核文章 阿里云接口
            Boolean isScanText = handleTextScan((String) textAndImages.get("content"), wmNews);
            if (!isScanText) return;

            // 自管理的敏感词过滤
            Boolean isScanSensitive = handleSensitive((String) textAndImages.get("content"), wmNews);
            if (!isScanSensitive) return;

            // 3. 审核图片 阿里云接口
            Boolean isScanImages = handleImagesScan((List<String>) textAndImages.get("images"), wmNews);
            if (!isScanImages) return;

            // 4. 审核成功,保存app端相关文章数据
            ResponseResult responseResult = saveAppArticle(wmNews);
            if (!responseResult.getCode().equals(AppHttpCodeEnum.SUCCESS.getCode())) {
                throw new RuntimeException("WmNewsAutoScanServiceImpl-autoScanWmNews-保存app端相关文章数据失败");
            }

            wmNews.setArticleId((Long) responseResult.getData());
            updateWmNews(wmNews, WmNews.Status.PUBLISHED.getCode(), "文章审核成功");
            log.info("WmNewsAutoScanServiceImpl-autoScanWmNews-文章审核成功，文章id：{}", wmNews.getId());
        }


    }

    /**
     * 保存app端相关文章数据
     *
     * @param wmNews
     */
    public ResponseResult saveAppArticle(WmNews wmNews) {

        ArticleDto articleDto = new ArticleDto();
        BeanUtils.copyProperties(wmNews, articleDto);

        // 文章的布局
        articleDto.setLayout(wmNews.getType());
        // 频道name
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null) {
            articleDto.setChannelName(wmChannel.getName());
        }
        // 作者
        articleDto.setAuthorId(Long.valueOf(wmNews.getUserId()));
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null) {
            articleDto.setAuthorName(wmUser.getName());
        }
        // 文章id  ==null首次审核通过(ap_article 还没建过),而首次不应该给 dto 设 id
        if (wmNews.getArticleId() != null) {
            articleDto.setId(wmNews.getArticleId());
        }

        articleDto.setCreatedTime(new Date());

        return articleClient.saveArticle(articleDto);
    }


    /**
     * 图片审核
     *
     * @param images
     * @param wmNews
     * @return
     */
    private Boolean handleImagesScan(List<String> images, WmNews wmNews) {

        boolean flag = true;

        // 图片为空的情况,直接为true
        if (images.isEmpty()) return flag;

        // minio下载图片
        // 图片去重
        images = images.stream().distinct().collect(Collectors.toList());
        List<byte[]> imageList = new ArrayList<>();

        try {
            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);

                //ocr图片识别
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                BufferedImage bufferedImage = ImageIO.read(in);
                String result = tess4jClient.doOCR(bufferedImage);
                //过滤文字
                Boolean isScanSensitive = handleSensitive(result, wmNews);
                if (!isScanSensitive) return false;

                imageList.add(bytes);
            }
        } catch (IOException | TesseractException e) {
            throw new RuntimeException(e);
        }


        // 审核图片
        //        try {
        //            Map map = greenImageScan.imageScan(imageList);
        //            if (map != null && map.get("suggestion").equals("block")) {
        //                flag = false;
        //                updateWmNews(wmNews, WmNews.Status.FAIL.getCode(), "当前文章有违规图片");
        //            }
        //            if (map != null && map.get("suggestion").equals("review")) {
        //                flag = false;
        //                updateWmNews(wmNews, WmNews.Status.ADMIN_AUTH.getCode(), "当前文章有违规图片");
        //            }
        //        } catch (Exception e) {
        //            flag = false;
        //            throw new RuntimeException(e);
        //        }


        return flag;
    }

    /**
     * 自管理的敏感词过滤
     *
     * @param content
     * @param wmNews
     * @return
     */
    private Boolean handleSensitive(String content, WmNews wmNews) {
        boolean flag = true;

        // 标题和内容为空的情况,直接为true
        if (StringUtils.isBlank(wmNews.getTitle() + "-" + content)) return flag;

        // 获取所有敏感词
        List<WmSensitive> sensitiveList = wmSensitiveMapper.selectList(Wrappers.<WmSensitive>lambdaQuery()
                .select(WmSensitive::getSensitives));
        List<String> collect = sensitiveList.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());

        // 初始化敏感词库
        SensitiveWordUtil.initMap(collect);
        // 查看文中是否包含敏感词
        Map<String, Integer> matchWords = SensitiveWordUtil.matchWords(wmNews.getTitle() + "-" + content);

        if (!matchWords.isEmpty()) {
            flag = false;
            updateWmNews(wmNews, WmNews.Status.FAIL.getCode(), "当前文章有违规信息" + matchWords);
        }

        return flag;
    }

    /**
     * 文章审核
     *
     * @param content
     * @param wmNews
     * @return
     */
    private Boolean handleTextScan(String content, WmNews wmNews) {
        boolean flag = true;

        // 标题和内容为空的情况,直接为true
        if (StringUtils.isBlank(wmNews.getTitle() + "-" + content)) return flag;


        //        try {
        //            Map map = greenTextScan.greeTextScan(wmNews.getTitle() + "-" + content);
        //            if (map != null) {
        //
        //                // 审核失败
        //                if (map.get("suggestion").equals("block")) {
        //                    flag = false;
        //                    updateWmNews(wmNews, WmNews.Status.FAIL.getCode(), "当前文章有违规信息");
        //                }
        //
        //                // 人工审核
        //                if (map.get("suggestion").equals("review")) {
        //                    flag = false;
        //                    updateWmNews(wmNews, WmNews.Status.ADMIN_AUTH.getCode(), "当前文章需要人工审核");
        //                }
        //
        //            }
        //        } catch (Exception e) {
        //            flag = false;
        //            throw new RuntimeException(e);
        //        }

        return flag;
    }

    /**
     * 更新自媒体文章状态和原因
     *
     * @param wmNews
     * @param status
     * @param reason
     */
    private void updateWmNews(WmNews wmNews, short status, String reason) {
        wmNews.setStatus(status);
        wmNews.setReason(reason);
        wmNewsMapper.updateById(wmNews);
    }

    /**
     * 1.从内容中提取文本和内容图片
     * 2.提取封面图片
     *
     * @param wmNews
     * @return
     */
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {

        // 纯文本
        StringBuilder text = new StringBuilder();
        // 图片
        List<String> images = new ArrayList<>();

        // 1.从内容中提取文本和内容图片
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> maps = JSONArray.parseArray(wmNews.getContent(), Map.class);

            for (Map map : maps) {
                if (map.get("type").equals("text")) {
                    text.append(map.get("value"));
                }

                if (map.get("type").equals("image")) {
                    images.add((String) map.get("value"));
                }
            }
        }

        // 2.提取封面图片
        if (StringUtils.isNotBlank(wmNews.getImages())) {
            String[] imageArray = wmNews.getImages().split(",");
            images.addAll(Arrays.asList(imageArray));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", text.toString());
        result.put("images", images);
        return result;
    }
}
