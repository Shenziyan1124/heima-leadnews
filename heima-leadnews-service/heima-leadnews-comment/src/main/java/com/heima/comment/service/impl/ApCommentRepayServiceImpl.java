package com.heima.comment.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heima.apis.user.IUserClient;
import com.heima.comment.service.ApCommentRepayService;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.model.comment.dtos.*;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.comment.pojos.ApCommentLike;
import com.heima.model.comment.pojos.ApCommentReply;
import com.heima.model.comment.vos.CommentVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.pojos.ApUser;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApCommentRepayServiceImpl implements ApCommentRepayService {

    private final MongoTemplate mongoTemplate;
    private final GreenTextScan greenTextScan;
    private final IUserClient iUserClient;

    /**
     * 每页查询条数
     */
    private static final int PAGE_SIZE = 20;


    /**
     * 保存回复
     *
     * @param dto
     */
    @Override
    public ResponseResult saveCommentRepay(CommentRepaySaveDto dto) {
        // 1. 校验参数
        if (dto == null || dto.getCommentId() == null || dto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2. 获取用户
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

        ApCommentReply apCommentReply = new ApCommentReply();
        apCommentReply.setCommentId(dto.getCommentId());
        apCommentReply.setAuthorId(user.getId());
        apCommentReply.setAuthorName(user.getName());
        apCommentReply.setContent(dto.getContent());
        apCommentReply.setLikes(0);
        apCommentReply.setCreatedTime(new Date());
        apCommentReply.setUpdatedTime(new Date());
        mongoTemplate.save(apCommentReply);

        // 5. 更新父评论的回复数量
        Query query = new Query(Criteria.where("_id").is(dto.getCommentId()));
        ApComment apComment = mongoTemplate.findOne(query, ApComment.class);
        if (apComment != null) {
            apComment.setReply(apComment.getReply() + 1);
            mongoTemplate.save(apComment);
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 加载回复列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult loadCommentRepay(CommentRepayListDto dto) {
        if (dto == null || dto.getCommentId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 1. 构建查询条件：按文章id查询，按创建时间倒序
        Query query = new Query();
        Criteria criteria = Criteria.where("commentId").is(dto.getCommentId());
        if (dto.getMinDate() != null) {
            criteria.and("createdTime").lt(dto.getMinDate());
        }
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, "createdTime"));
        query.skip(dto.getSize() == null ? 0 : (long) dto.getSize() * PAGE_SIZE);
        query.limit(PAGE_SIZE);

        List<ApCommentReply> commentReplyList = mongoTemplate.find(query, ApCommentReply.class);

        // 2. 转换为Vo，设置operation字段
        ApUser user = AppThreadLocalUtil.getUser();
        List<CommentVo> voList = new ArrayList<>();
        for (ApCommentReply apCommentReply : commentReplyList) {
            CommentVo vo = new CommentVo();
            BeanUtils.copyProperties(apCommentReply, vo);
            vo.setId(apCommentReply.get_id());

            if (user != null) {
                Query likeQuery = new Query();
                likeQuery.addCriteria(Criteria.where("commentId").is(apCommentReply.get_id())
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
     * 点赞回复
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult likeCommentRepay(CommentRepayLikeBehaviorDto dto) {
        // 1. 校验参数
        if (dto == null || dto.getCommentRepayId() == null
                || dto.getOperation() < 0 || dto.getOperation() > 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 2. 获取用户
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 3. 校验评论是否存在
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(dto.getCommentRepayId()));
        ApCommentReply commentReply = mongoTemplate.findOne(query, ApCommentReply.class);
        if (commentReply == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        // 4. 更新评论点赞数
        if (dto.getOperation() == 0) {
            commentReply.setLikes(commentReply.getLikes() + 1);

            // 往ap_comment_like表插入数据
            ApCommentLike apCommentLike = new ApCommentLike();
            apCommentLike.setCommentId(dto.getCommentRepayId());
            apCommentLike.setAuthorId(user.getId());
            apCommentLike.setCreatedTime(new Date());
            mongoTemplate.save(apCommentLike);

        } else {
            // 取消点赞
            commentReply.setLikes(commentReply.getLikes() - 1);
            // ap_comment_like表数据删除
            Query likeQuery = new Query();
            likeQuery.addCriteria(Criteria.where("commentId").is(dto.getCommentRepayId())
                    .and("authorId").is(user.getId()));
            mongoTemplate.remove(likeQuery, ApCommentLike.class);
        }

        // 5.更新ap_comment_repay表
        mongoTemplate.save(commentReply);

        return ResponseResult.okResult(commentReply.getLikes());
    }

}
