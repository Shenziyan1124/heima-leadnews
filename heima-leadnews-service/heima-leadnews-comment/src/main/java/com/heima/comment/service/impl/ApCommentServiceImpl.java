package com.heima.comment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.apis.user.IUserClient;
import com.heima.comment.service.ApCommentService;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.model.comment.dtos.CommentLikeListDto;
import com.heima.model.comment.dtos.CommentLikeBehaviorDto;
import com.heima.model.comment.dtos.CommentLikeSaveDto;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentLike;
import com.heima.model.comment.vos.CommentVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApCommentServiceImpl implements ApCommentService {

    private final MongoTemplate mongoTemplate;
    private final GreenTextScan greenTextScan;
    private final IUserClient iUserClient;

    /**
     * 每页查询条数
     */
    private static final int PAGE_SIZE = 20;

    /**
     * 保存评论
     *
     */
    @Override
    public ResponseResult saveComment(CommentLikeSaveDto dto) {
        // 1. 校验参数
        if (dto == null || dto.getArticleId() == null || dto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2. 校验用户是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 3. 校验评论内容长度不超过140字
        if (dto.getContent().length() > 140) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论内容不能超过140字");
        }

        // 4. 文本垃圾检测（阿里云内容安全）
        try {
            Map result = greenTextScan.greeTextScan(dto.getContent());
            if (result != null && result.containsKey("suggestion")) {
                String suggestion = (String) result.get("suggestion");
                if (!"pass".equals(suggestion)) {
                    return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "评论内容包含违规信息，审核不通过");
                }
            }
        } catch (Exception e) {
            log.error("评论文本垃圾检测异常", e);
        }

        // 5. 通过feign获取用户完整信息
        ResponseResult result = iUserClient.getById(user.getId());
        if (result != null && result.getData() != null) {
            user = new ObjectMapper().convertValue(result.getData(), ApUser.class);
        }

        // 6. 保存评论
        ApComment apComment = new ApComment();
        apComment.setEntryId(dto.getArticleId());
        apComment.setContent(dto.getContent());
        apComment.setAuthorId(user.getId());
        apComment.setAuthorName(user.getName());
        apComment.setCreatedTime(new Date());
        apComment.setReply(0);
        apComment.setType(0);
        apComment.setFlag(0);
        apComment.setLikes(0);
        mongoTemplate.save(apComment);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 加载评论
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult loadComment(CommentLikeListDto dto) {
        if (dto == null || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 1. 构建查询条件：按文章id查询，按创建时间倒序
        Query query = new Query();
        Criteria criteria = Criteria.where("entryId").is(dto.getArticleId());
        if (dto.getMinDate() != null) {
            criteria.and("createdTime").lt(dto.getMinDate());
        }
        query.addCriteria(criteria);
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdTime"));
        query.skip(dto.getIndex() == null ? 0 : (long) dto.getIndex() * PAGE_SIZE);
        query.limit(PAGE_SIZE);

        List<ApComment> commentList = mongoTemplate.find(query, ApComment.class);

        // 2. 转换为Vo，设置operation字段
        ApUser user = AppThreadLocalUtil.getUser();
        List<CommentVo> voList = new ArrayList<>();
        for (ApComment apComment : commentList) {
            CommentVo vo = new CommentVo();
            BeanUtils.copyProperties(apComment, vo);
            vo.setId(apComment.get_id());

            // 如果用户已登录，查询该用户是否点赞了该评论
            if (user != null) {
                Query likeQuery = new Query();
                likeQuery.addCriteria(Criteria.where("commentId").is(apComment.get_id())
                        .and("authorId").is(user.getId()));
                long count = mongoTemplate.count(likeQuery, ApCommentLike.class);
                if (count > 0) {
                    vo.setOperation(0);
                }
            }

            voList.add(vo);
        }

        return ResponseResult.okResult(voList);
    }

    /**
     * 点赞评论
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult likeComment(CommentLikeBehaviorDto dto) {
        // 1. 校验参数
        if (dto == null || dto.getCommentId() == null ||
                dto.getOperation() < 0 || dto.getOperation() > 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2. 校验用户是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }


        // 3. 查询评论是否存在
        Query query = new Query(Criteria.where("_id").is(dto.getCommentId()));
        ApComment apComment = mongoTemplate.findOne(query, ApComment.class);
        if (apComment == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }


        // 4. 根据operation字段的值，保存或删除点赞记录
        if (dto.getOperation() == 0) {
            ApCommentLike apCommentLike = new ApCommentLike();
            apCommentLike.setCommentId(dto.getCommentId());
            apCommentLike.setAuthorId(user.getId());
            apCommentLike.setCreatedTime(new Date());
            mongoTemplate.save(apCommentLike);
            // 更新点赞数
            apComment.setLikes(apComment.getLikes() + 1);
        } else {
            mongoTemplate.remove(new Query(Criteria.where("commentId").is(dto.getCommentId())
                    .and("authorId").is(user.getId())), ApCommentLike.class);
            // 更新点赞数
            apComment.setLikes(apComment.getLikes() - 1);
        }

        // 5. 更新评论的点赞数
        mongoTemplate.save(apComment);

        return ResponseResult.okResult(apComment.getLikes());
    }
}
