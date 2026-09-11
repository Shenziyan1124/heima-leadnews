package com.heima.model.comment.pojos;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;

/**
 * APP评论信息表（MongoDB）
 */
@Data
@Document(collection = "ap_comment")
public class ApComment implements Serializable {

    @Id
    private String _id;

    /**
     * 作者id
     */
    @Field("authorId")
    private Integer authorId;

    /**
     * 入口id（文章id）
     */
    @Field("entryId")
    private Long entryId;

    /**
     * 作者名称
     */
    @Field("authorName")
    private String authorName;

    /**
     * 评论内容
     */
    @Field("content")
    private String content;

    /**
     * 创建时间
     */
    @Field("createdTime")
    private Date createdTime;

    /**
     * 回复数量
     */
    @Field("reply")
    private Integer reply;

    /**
     * 评论类型 0 站内评论 1 站外评论
     */
    @Field("type")
    private Integer type;

    /**
     * 类名标记
     */
    @Field("_class")
    private String _class;

    /**
     * 标记 0 正常 1 删除
     */
    @Field("flag")
    private Integer flag;

    /**
     * 点赞数量
     */
    @Field("likes")
    private Integer likes;
}
