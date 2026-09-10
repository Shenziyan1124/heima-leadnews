package com.heima.model.comment.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentLikeBehaviorDto implements Serializable {

    /**
     * 评论id
     */
    private String commentId;

    /**
     * 0：点赞 1：取消点赞
     */
    private short operation;
}
