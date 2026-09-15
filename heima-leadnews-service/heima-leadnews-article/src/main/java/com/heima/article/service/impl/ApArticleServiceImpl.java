package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.apis.user.IUserClient;
import com.heima.article.mapper.*;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.constants.ApUserBehaviorConstants;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.dtos.ArticleBehaviorDto;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.*;
import com.heima.model.article.vos.ArticleBehaviorVo;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.mess.ArticleVisitStreamMess;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import io.micrometer.core.instrument.AbstractTimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.data.Json;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    // 最大分页值
    private final static Short MAX_PAGE_SIZE = 50;

    // 文章Mapper
    private final ApArticleMapper apArticleMapper;
    // 文章内容Mapper
    private final ApArticleContentMapper apArticleContentMapper;
    // 文章配置Mapper
    private final ApArticleConfigMapper apArticleConfigMapper;
    private final ArticleFreemarkerService articleFreemarkerService;


    private final CacheService cacheService;
    private final ApLikesBehaviorMapper apLikesBehaviorMapper;
    private final ApCollectionMapper apCollectionMapper;
    private final IUserClient userClient;

    /**
     * 加载文章列表
     *
     * @param dto
     * @param type 1 加载更多 2 加载最新
     * @return
     */
    @Override
    public ResponseResult loadArticleList(ArticleHomeDto dto, Short type) {
        // 1.参数校验
        // 分页条数校验
        Integer size = dto.getSize();
        if (size == null || size == 0) {
            size = 10;
        }
        // 分页值不超过50
        size = Math.min(size, MAX_PAGE_SIZE);

        // 校验type
        if (!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE) &&
                !type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)) {
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        // 频道参数校验
        if (StringUtils.isNotBlank(dto.getTag())) {
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        // 时间校验
        if (dto.getMaxBehotTime() == null) dto.setMaxBehotTime(new Date());
        if (dto.getMinBehotTime() == null) dto.setMinBehotTime(new Date());

        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto, type);
        return ResponseResult.okResult(apArticles);
    }

    /**
     * 加载文章列表
     *
     * @param dto
     * @param type      1 加载更多 2 加载最新
     *                  firstPage true:查询第一页 false:查询非第一页
     * @param firstPage
     * @return
     */
    @Override
    public ResponseResult loadArticleList2(ArticleHomeDto dto, Short type, Boolean firstPage) {
        if (firstPage) {
            String jsonStr = cacheService.get(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + dto.getTag());
            if (StringUtils.isNotBlank(jsonStr)) {
                List<HotArticleVo> hotArticleVos = JSON.parseArray(jsonStr, HotArticleVo.class);
                return ResponseResult.okResult(hotArticleVos);
            }
        }
        return loadArticleList(dto, type);
    }

    /**
     * 保存app端相关文章
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        // 模拟保存文章内容耗时操作
        //        try {
        //            Thread.sleep(3000);
        //        } catch (InterruptedException e) {
        //            throw new RuntimeException(e);
        //        }

        // 1.参数校验
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }


        // 2.判断是否存在id
        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto, apArticle);

        if (dto.getId() == null) {
            // 2.1 不存在-保存文章 文章配置 文章内容
            // 保存文章
            save(apArticle);

            // 保存配置
            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);

            // 保存内容
            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);

        } else {
            // 2.2 存在 - 修改文章 文章内容
            // 修改文章
            updateById(apArticle);
            // 修改内容

            ApArticleContent articleContent = apArticleContentMapper.selectOne(
                    new QueryWrapper<ApArticleContent>()
                            .lambda().eq(ApArticleContent::getArticleId, apArticle.getId())
            );
            articleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(articleContent);

        }

        // 异步调用,生成静态文件,上传到minio
        articleFreemarkerService.buildArticleToMinIO(apArticle, dto.getContent());


        // 3.结果返回 文章的id
        return ResponseResult.okResult(apArticle.getId());
    }

    /**
     * 根据频道id获取文章数量
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult getArticleCountByChannelId(Integer id) {
        int count = lambdaQuery().eq(ApArticle::getChannelId, id).count();
        return ResponseResult.okResult(count);
    }

    /**
     * 加载文章行为,判断当前用户是否已经关注该文章的作者、是否收藏了此文章、是否点赞了文章、是否不喜欢该文章等
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult loadArticleBehavior(ArticleBehaviorDto dto) {

        // 1.参数校验
        if (dto == null || dto.getArticleId() == null || dto.getAuthorId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2.判断用户是否登录
        //Integer userId = AppThreadLocalUtil.getUser().getId();
        Integer userId = 4;
        if (userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 3.查询
        // 3.1 查点赞
        boolean islike = cacheService.sIsMember(ApUserBehaviorConstants.LIKES_ARTICLE_KEY + dto.getArticleId(), userId.toString());
        if (!islike) {
            // redis没有,查mysql
            ApLikesBehavior apLikesBehavior = apLikesBehaviorMapper.selectOne(new LambdaQueryWrapper<ApLikesBehavior>()
                    .eq(ApLikesBehavior::getOperation, ApUserBehaviorConstants.LIKE)
                    .eq(ApLikesBehavior::getArticleId, dto.getArticleId())
                    .eq(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.CANCEL_DELETE)
                    .eq(ApLikesBehavior::getUserId, userId));

            if (apLikesBehavior != null) {
                islike = true;
                cacheService.sAdd(ApUserBehaviorConstants.LIKES_ARTICLE_KEY + dto.getArticleId(), String.valueOf(userId));
                cacheService.sAdd(ApUserBehaviorConstants.LIKES_USER_KEY + userId, String.valueOf(dto.getArticleId()));
            }
        }

        // 3.2 查不喜欢 0+2不喜欢 1+2取消不喜欢
        boolean isunlike = cacheService.sIsMember(ApUserBehaviorConstants.UN_LIKE_ARTICLE_KEY + dto.getArticleId(), userId.toString());
        if (!isunlike) {
            // redis没有,查mysql
            ApLikesBehavior apLikesBehavior = apLikesBehaviorMapper.selectOne(new LambdaQueryWrapper<ApLikesBehavior>()
                    .eq(ApLikesBehavior::getOperation, ApUserBehaviorConstants.UN_LIKE)
                    .eq(ApLikesBehavior::getArticleId, dto.getArticleId())
                    .eq(ApLikesBehavior::getIsDelete, ApUserBehaviorConstants.CANCEL_DELETE)
                    .eq(ApLikesBehavior::getUserId, userId));

            if (apLikesBehavior != null) {
                isunlike = true;
                cacheService.sAdd(ApUserBehaviorConstants.UN_LIKE_ARTICLE_KEY + dto.getArticleId(), String.valueOf(userId));
            }
        }

        // 3.3 查收藏
        boolean iscollection = cacheService.sIsMember(ApUserBehaviorConstants.COLLECT_ARTICLE_KEY + dto.getArticleId(), userId.toString());
        if (!iscollection) {
            // redis没有,查mysql
            ApCollection apCollection = apCollectionMapper.selectOne(new LambdaQueryWrapper<ApCollection>()
                    .eq(ApCollection::getIsDelete, ApUserBehaviorConstants.COLLECT)
                    .eq(ApCollection::getArticleId, dto.getArticleId())
                    .eq(ApCollection::getUserId, userId));

            if (apCollection != null) {
                iscollection = true;
                cacheService.sAdd(ApUserBehaviorConstants.COLLECT_ARTICLE_KEY + dto.getArticleId(), String.valueOf(userId));
                cacheService.sAdd(ApUserBehaviorConstants.COLLECT_USER_KEY + userId, String.valueOf(dto.getArticleId()));
            }
        }

        // 3.4 查关注
        boolean isfollow = cacheService.sIsMember(ApUserBehaviorConstants.FOLLOW_USER_KEY + userId, String.valueOf(dto.getAuthorId()));
        if (!isfollow) {
            // redis没有,查mysql
            ResponseResult responseResult = userClient.checkFollow(userId, dto.getAuthorId());
            if (responseResult.getCode() == AppHttpCodeEnum.SUCCESS.getCode()
                    && responseResult.getData() != null
                    && responseResult.getData() == Boolean.TRUE
            ) {
                isfollow = true;
                cacheService.sAdd(ApUserBehaviorConstants.FOLLOW_USER_KEY + userId, String.valueOf(dto.getAuthorId()));
                cacheService.sAdd(ApUserBehaviorConstants.FOLLOW_FANS_KEY + dto.getAuthorId(), String.valueOf(userId));
            }
        }

        // TODO 获取文章行为
        // 4.结果返回
        ArticleBehaviorVo vo = new ArticleBehaviorVo();
        vo.setIslike(islike);
        vo.setIsunlike(isunlike);
        vo.setIscollection(iscollection);
        vo.setIsfollow(isfollow);
        return ResponseResult.okResult(vo);
    }

    /**
     * 更新文章的分值  同时更新缓存中的热点文章数据
     *
     * @param mess
     */
    @Override
    public void updateScore(ArticleVisitStreamMess mess) {
        // 1.更新文章的 阅读 点赞 评论 收藏数量
        ApArticle apArticle = updateArticle(mess);

        // 2.计算文章的分值
        Integer score = computeScore(apArticle);
        score = score * 3;
        log.info("文章分值计算结果：{}", score);

        // 3.替换当前文章对应的频道热点
        replaceDataToRedis(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId(), apArticle, score);

        // 4.替换推荐文章
        replaceDataToRedis(ArticleConstants.HOT_ARTICLE_FIRST_PAGE + ArticleConstants.DEFAULT_TAG, apArticle, score);

    }

    /**
     * 替换数据并且存入到redis
     * @param apArticle
     * @param score
     * @param
     */
    private void replaceDataToRedis(String HOT_ARTICLE_FIRST_PAGE, ApArticle apArticle, Integer score) {
        String channelHotArticles = cacheService.get(HOT_ARTICLE_FIRST_PAGE);
        if (StringUtils.isNotBlank(channelHotArticles)) {
            List<HotArticleVo> hotArticleVoList = JSON.parseArray(channelHotArticles, HotArticleVo.class);
            boolean isFound = true;

            for (HotArticleVo hotArticleVo : hotArticleVoList) {
                // 遍历热点文章列表，找到当前文章，替换分值
                if (hotArticleVo.getId().equals(apArticle.getId())) {
                    hotArticleVo.setScore(score);
                    isFound = false;
                    break;
                }
            }

            // 看isFound是否为true,true表示没有找到文章，需要替换
            // 如果缓存不存在,查找缓存中分值最小的一条数据,去对比,如果大于最小的分值,则进行替换
            if (isFound) {
                if (hotArticleVoList.size() >= 30) {
                    log.info("没有找到文章：{},去查找最小的一条数据进行比较", apArticle.getId());
                    hotArticleVoList = hotArticleVoList.stream()
                            .sorted(
                                    Comparator.comparing(HotArticleVo::getScore)
                                            .reversed())
                            .collect(Collectors.toList());
                    HotArticleVo lastHot = hotArticleVoList.get(hotArticleVoList.size() - 1);
                    if (lastHot.getScore() < score) {
                        hotArticleVoList.remove(lastHot);
                        HotArticleVo hotArticleVo = new HotArticleVo();
                        BeanUtils.copyProperties(apArticle, hotArticleVo);
                        hotArticleVo.setScore(score);
                        hotArticleVoList.add(hotArticleVo);
                    }
                } else {
                    HotArticleVo hotArticleVo = new HotArticleVo();
                    BeanUtils.copyProperties(apArticle, hotArticleVo);
                    hotArticleVo.setScore(score);
                    hotArticleVoList.add(hotArticleVo);
                }
            }

            // 缓存到redis中
            hotArticleVoList = hotArticleVoList.stream().sorted(
                            Comparator.comparing(HotArticleVo::getScore).reversed())
                    .collect(Collectors.toList());
            cacheService.set(
                    ArticleConstants.HOT_ARTICLE_FIRST_PAGE + apArticle.getChannelId(),
                    JSON.toJSONString(hotArticleVoList));
        }
    }

    /**
     * 更新文章的阅读 点赞 评论 收藏数量
     *
     * @param mess
     */
    private ApArticle updateArticle(ArticleVisitStreamMess mess) {
        // 1.查询当前文章
        ApArticle apArticle = apArticleMapper.selectById(mess.getArticleId());
        if (apArticle == null) return null;
        // 2. 更新文章的阅读 点赞 评论 收藏数量
        apArticle.setViews(
                apArticle.getViews() == null ? 0 : apArticle.getViews() + mess.getView());
        apArticle.setLikes(
                apArticle.getLikes() == null ? 0 : apArticle.getLikes() + mess.getLike());
        apArticle.setComment(
                apArticle.getComment() == null ? 0 : apArticle.getComment() + mess.getComment());
        apArticle.setCollection(
                apArticle.getCollection() == null ? 0 : apArticle.getCollection() + mess.getCollect());
        apArticleMapper.updateById(apArticle);
        return apArticle;
    }

    /**
     * 计算文章的具体分值
     *
     * @param apArticle
     * @return
     */
    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if (apArticle.getLikes() != null) {
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (apArticle.getViews() != null) {
            score += apArticle.getViews();
        }
        if (apArticle.getComment() != null) {
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (apArticle.getCollection() != null) {
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }
        return score;
    }
}
