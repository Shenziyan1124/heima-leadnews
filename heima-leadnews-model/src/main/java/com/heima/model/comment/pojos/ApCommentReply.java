package com.heima.model.comment.pojos;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论回复表（MongoDB）
 */
@Data
@Document(collection = "ap_comment_repay")
public class ApCommentReply implements Serializable {

    @Id
    private String _id;

    /**
     * 评论id
     */
    @Field("commentId")
    private String commentId;

    /**
     * 作者id
     */
    @Field("authorId")
    private Integer authorId;

    /**
     * 作者名称
     */
    @Field("authorName")
    private String authorName;

    /**
     * 回复内容
     */
    @Field("content")
    private String content;

    /**
     * 点赞数量
     */
    @Field("likes")
    private Integer likes;

    /**
     * 创建时间
     */
    @Field("createdTime")
    private Date createdTime;

    /**
     * 更新时间
     */
    @Field("updatedTime")
    private Date updatedTime;

    /**
     * 类名标记
     */
    @Field("_class")
    private String _class;
}
