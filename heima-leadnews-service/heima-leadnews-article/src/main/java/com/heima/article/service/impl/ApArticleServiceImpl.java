package com.heima.article.service.impl;

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
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import io.micrometer.core.instrument.AbstractTimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
        if (dto == null || dto.getArticleId() == null || dto.getAuthorId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2.判断用户是否登录
        //Integer userId = AppThreadLocalUtil.getUser().getId();
        Integer userId = 4;
        if (userId == null){
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
}
