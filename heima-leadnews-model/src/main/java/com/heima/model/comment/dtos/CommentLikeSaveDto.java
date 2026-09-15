package com.heima.model.comment.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentLikeSaveDto implements Serializable {

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 评论内容
     */
    private String content;
}
