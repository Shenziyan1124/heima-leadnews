package com.heima.comment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.apis.article.IArticleClient;
import com.heima.apis.user.IUserClient;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.comment.service.ApCommentManageService;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentReply;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.dtos.WmArticleCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentLikeDto;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import com.heima.model.wemedia.dtos.WmCommentReplyDto;
import com.heima.model.wemedia.vos.ArticleCommentVo;
import com.heima.model.wemedia.vos.CommentRepayVo;
import com.heima.model.wemedia.vos.CommentVo;
import com.heima.model.wemedia.vos.WmCommentVo;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApCommentManageServiceImpl implements ApCommentManageService {

    private final MongoTemplate mongoTemplate;
    private final IWemediaClient iWemediaClient;
    private final IArticleClient iArticleClient;
    private final IUserClient iUserClient;

    @Override
    public ResponseResult findNewsComments(WmCommentListDto dto) {
        dto.checkParam();

        // 1. 构建查询条件
        org.springframework.data.mongodb.core.query.Criteria criteria = new org.springframework.data.mongodb.core.query.Criteria();
        Date beginDate = dto.parseBeginDate();
        Date endDate = dto.parseEndDate();

        if (beginDate != null && endDate != null) {
            criteria.and("createdTime").gte(beginDate).lte(endDate);
        } else if (beginDate != null) {
            criteria.and("createdTime").gte(beginDate);
        } else if (endDate != null) {
            criteria.and("createdTime").lte(endDate);
        }

        // 2. 查询总数（按entryId分组后的组数）
        Aggregation countAggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("entryId")
        );
        List<Map> countResults = mongoTemplate.aggregate(
                countAggregation, "ap_comment", Map.class).getMappedResults();
        int total = countResults.size();

        // 3. 分页查询当前页数据
        Aggregation pageAggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("entryId")
                        .count().as("comments")
                        .min("createdTime").as("createdTime"),
                Aggregation.project()
                        .and("_id").as("id")
                        .and("comments").as("comments")
                        .and("createdTime").as("createdTime"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "createdTime"),
                Aggregation.skip((long) (dto.getPage() - 1) * dto.getSize()),
                Aggregation.limit(dto.getSize())
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
                pageAggregation, "ap_comment", Map.class);
        List<Map> mappedResults = results.getMappedResults();

        // 4. 转换为VO，查询文章标题和评论状态
        List<WmCommentVo> voList = new ArrayList<>();
        for (Map map : mappedResults) {
            WmCommentVo vo = new WmCommentVo();
            vo.setId((Long) map.get("id"));
            vo.setComments((Integer) map.get("comments"));
            Object ct = map.get("createdTime");
            if (ct instanceof Date) {
                vo.setCreatedTime(((Date) ct).getTime());
            } else if (ct instanceof Long) {
                vo.setCreatedTime((Long) ct);
            }

            // 查询文章标题
            try {
                ResponseResult titleResult = iWemediaClient.getNewsByArticleId(vo.getId());
                if (titleResult != null && titleResult.getData() != null) {
                    vo.setTitle((String) titleResult.getData());
                }
            } catch (Exception e) {
                log.warn("查询文章标题失败: articleId={}", vo.getId(), e);
            }

            // 查询评论状态
            try {
                ResponseResult commentResult = iArticleClient.getCommentStatus(vo.getId());
                if (commentResult != null && commentResult.getData() != null) {
                    vo.setIsComment((Boolean) commentResult.getData());
                } else {
                    vo.setIsComment(true);
                }
            } catch (Exception e) {
                log.warn("查询评论状态失败: articleId={}", vo.getId(), e);
                vo.setIsComment(true);
            }

            voList.add(vo);
        }

        // 5. 封装分页结果
        PageResponseResult pageResponseResult = new PageResponseResult(
                dto.getPage(), dto.getSize(), total);
        pageResponseResult.setData(voList);
        return pageResponseResult;
    }

    /**
     * 根据dto查询文章详情评论列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findCommentListByArticleId(WmArticleCommentListDto dto) {
        // 1. 参数校验
        dto.checkParam();

        List<ArticleCommentVo> articleCommentList = new ArrayList<>();

        // 2. 根据articleId 请求ap_comment的entryId
        Query query = new Query();
        query.addCriteria(Criteria.where("entryId").is(dto.getArticleId()));
        List<ApComment> commentList = mongoTemplate.find(query, ApComment.class, "ap_comment");


        // 3. 根据entryId 查询ap_comment_repay 每条评论的回复
        for (ApComment apComment : commentList) {

            // 3.1 每次循环创建query
            Query replyQuery = new Query();
            replyQuery.addCriteria(Criteria.where("commentId").is(apComment.get_id()));

            // 3.2 查询该评论的回复列表
            List<ApCommentReply> replyList =
                    mongoTemplate.find(
                            replyQuery,
                            ApCommentReply.class,
                            "ap_comment_reply");

            // 3.3 创建articleCommentVo
            ArticleCommentVo articleCommentVo = new ArticleCommentVo();

            // 3.4 把apcomment转成commentvo,放到articleCommentVo中的apComments字段
            CommentVo commentVo = new CommentVo();
            commentVo.setId(apComment.get_id());
            commentVo.setAuthorId(apComment.getAuthorId());
            commentVo.setAuthorName(apComment.getAuthorName());
            commentVo.setContent(apComment.getContent());
            commentVo.setImage(null);
            commentVo.setLikes(apComment.getLikes());
            commentVo.setReply(apComment.getReply());
            commentVo.setFlag(apComment.getFlag());
            commentVo.setOrd(null);
            commentVo.setCreatedTime(apComment.getCreatedTime().getTime());
            commentVo.setUpdatedTime(apComment.getCreatedTime().getTime());

            articleCommentVo.setApComments(commentVo);

            // 3.5  把ApCommentReply转成commentrepayvo,放到articleCommentVo中的apCommentReplies字段
            List<CommentRepayVo> replyVoList = new ArrayList<>();
            for (ApCommentReply commentReply : replyList){
                CommentRepayVo commentRepayVo = new CommentRepayVo();
                BeanUtils.copyProperties(commentReply, commentRepayVo);
                commentRepayVo.setId(commentReply.get_id());
                commentRepayVo.setLikes(commentReply.getLikes());
                commentRepayVo.setCreatedTime(commentReply.getCreatedTime().getTime());
                commentRepayVo.setUpdatedTime(commentReply.getUpdatedTime().getTime());
                replyVoList.add(commentRepayVo);
            }
            articleCommentVo.setApCommentRepays(replyVoList);

            // 3.6 添加到articleCommentList
            articleCommentList.add(articleCommentVo);

        }


        // 4. 封装分页结果
        PageResponseResult pageResponseResult = new PageResponseResult(
                dto.getPage(), dto.getSize(), articleCommentList.size());
        pageResponseResult.setData(articleCommentList);
        return pageResponseResult;
    }

    /**
     * 评论回复
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult commentReply(WmCommentReplyDto dto) {

        // 1.获取登录信息
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }


        // 2.保存评论回复
        ApCommentReply apCommentReply = new ApCommentReply();
        apCommentReply.setCommentId(dto.getCommentId());
        apCommentReply.setAuthorId(user.getId());
        apCommentReply.setContent(dto.getContent());
        apCommentReply.setCreatedTime(new Date());
        apCommentReply.setUpdatedTime(new Date());


        try {
            ResponseResult userClientById = iUserClient.getById(user.getId());
            if (userClientById != null && userClientById.getData() != null) {
                ObjectMapper mapper = new ObjectMapper();
                ApUser user1 = mapper.convertValue(userClientById.getData(), ApUser.class);
                apCommentReply.setAuthorName(user1.getName());
            }
        } catch (Exception e) {
            log.error("查询用户信息失败: {}", dto, e);
            apCommentReply.setAuthorName("匿名用户");
        }

        mongoTemplate.save(apCommentReply);

        // 3.返回结果
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 作者点赞
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult authorLike(WmCommentLikeDto dto) {
        // 获取当前登录用户
        //ApUser user = AppThreadLocalUtil.getUser();
        //if (user == null) {
        //    return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        //}
        // 查commentid对应的评论
        ApComment apComment = mongoTemplate.findById(dto.getCommentId(), ApComment.class, "ap_comment");
        if (apComment == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        if (dto.getOperation() == 1){
            // 取消点赞
            apComment.setLikes(apComment.getLikes() - 1);
        }else {
            apComment.setLikes(apComment.getLikes() + 1);
        }

        mongoTemplate.save(apComment);

        return ResponseResult.okResult(apComment.getLikes());

    }

    /**
     * 删除评论
     *
     * @param commentId
     * @return
     */
    @Override
    public ResponseResult delComment(String commentId) {
        mongoTemplate.remove(
                Query.query(Criteria.where("_id").is(commentId)),
                ApComment.class, "ap_comment");

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除评论回复
     *
     * @param commentRepayId
     * @return
     */
    @Override
    public ResponseResult delCommentReplay(String commentRepayId) {
        mongoTemplate.remove(
                Query.query(Criteria.where("_id").is(commentRepayId)),
                ApCommentReply.class, "ap_comment_reply");
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
