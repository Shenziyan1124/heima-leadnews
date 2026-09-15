package com.heima.model.wemedia.vos;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "评论回复对象")
public class CommentRepayVo implements Serializable {

    /**
     * 回复id
     */
    private String id;

    /**
     * 作者id
     */
    private Integer authorId;

    /**
     * 作者名称
     */
    private String authorName;

    /**
     * 评论id
     */
    private String commentId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 点赞数量
     */
    private Integer likes;

    /**
     * 创建时间
     */
    private Long createdTime;

    /**
     * 更新时间
     */
    private Long updatedTime;
}
