package com.heima.model.comment.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentRepayLikeBehaviorDto implements Serializable {

    /**
     * 评论回复id
     */
    private String commentRepayId;

    /**
     * 0：点赞 1：取消点赞
     */
    private short operation;
}
