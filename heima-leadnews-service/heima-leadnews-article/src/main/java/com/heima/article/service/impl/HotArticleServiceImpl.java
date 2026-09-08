package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.article.IArticleClient;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class HotArticleServiceImpl implements HotArticleService {

    private final ApArticleMapper apArticleMapper;
    private final IWemediaClient wemediaClient;
    private final CacheService cacheService;

    /**
     * 计算热门文章
     */
    @Override
    public void computedHotArticle() {
        // 1. 查询前五天的文章
        Date dayParam = DateTime.now().minusDays(5).toDate();
        List<ApArticle> articleList = apArticleMapper.findArticleListByLast5days(dayParam);

        // 2. 计算文章分值
        List<HotArticleVo> hotArticleVoList = computedHotArticleScore(articleList);

        // 3. 为每个频道缓存30条分值较高的文章
        cacheTagToRedis(hotArticleVoList);
    }

    /**
     * 为每个频道缓存30条分值较高的文章
     * @param hotArticleVoList
     */
    private void cacheTagToRedis(List<HotArticleVo> hotArticleVoList) {
        for (HotArticleVo vo : hotArticleVoList) {
            System.out.println("文章id=" + vo.getId() + ", channelId=" + vo.getChannelId());
        }
        System.out.println("进入cacheTagToRedis, 文章数=" + hotArticleVoList.size());
        ResponseResult channelList = wemediaClient.getChannelList();
        System.out.println("channelList=" + channelList);
        System.out.println("channelList.data=" + channelList.getData());

        if (channelList != null && channelList.getData() != null){
           String channelJson = JSON.toJSONString(channelList.getData());
           List<WmChannel> wmChannelList = JSON.parseArray(channelJson, WmChannel.class);
            List<HotArticleVo> hotArticleVos = new ArrayList<>();
            System.out.println("wmChannelList=" + wmChannelList);
            // 检索出每个频道的文章
            for (WmChannel channel : wmChannelList) {
                hotArticleVos =
                        hotArticleVoList.stream().filter(h ->
                                h.getChannelId().equals(channel.getId()))
                                .collect(Collectors.toList());
                System.out.println("channelId=" + channel.getId() + ", 文章数=" + hotArticleVos.size());

                // 给文章进行排序,取30条分值较高的文章存入redis
                sortAndCache(hotArticleVos, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + channel.getId());
            }
        }

        // 设置推荐数据
        // 给文章进行排序,取30条分值较高的文章存入redis
        sortAndCache(hotArticleVoList, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG);

    }

    /**
     * 对文章进行排序，并取前30条存入redis
     * @param hotArticleVos
     * @param HOT_ARTICLE_FIRST_PAGE
     */
    private void sortAndCache(List<HotArticleVo> hotArticleVos, String HOT_ARTICLE_FIRST_PAGE) {
        hotArticleVos = hotArticleVos.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
        if (hotArticleVos.size() > 30) {
            hotArticleVos = hotArticleVos.subList(0, 30);
        }
        cacheService.set(HOT_ARTICLE_FIRST_PAGE, JSON.toJSONString(hotArticleVos));
    }

    /**
     * 计算文章分值
     * @param articleList
     * @return
     */
    private List<HotArticleVo> computedHotArticleScore(List<ApArticle> articleList) {
        List<HotArticleVo> hotArticleVoList = new ArrayList<>();

        if (articleList != null && !articleList.isEmpty()){
            for (ApArticle article : articleList){
                HotArticleVo hot = new HotArticleVo();
                BeanUtils.copyProperties(article, hot);
                Integer score = computedScore(article);
                hot.setScore(score);
                hotArticleVoList.add(hot);
            }
        }
        return hotArticleVoList;
    }

    /**
     * 计算文章具体分值
     * @param article
     * @return
     */
    private Integer computedScore(ApArticle article) {
        Integer score = 0;
        if (article.getLikes() != null) {
            score += article.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (article.getComment() != null) {
            score += article.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (article.getCollection() != null) {
            score += article.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        if (article.getViews() != null) {
            score += article.getViews();
        }
        log.info("score: {}", score);
        return score;
    }
}
