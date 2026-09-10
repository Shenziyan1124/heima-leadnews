package com.heima.model.comment.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentRepaySaveDto implements Serializable {

    /**
     * 评论id
     */
    private String commentId;

    /**
     * 评论内容
     */
    private String content;
}
