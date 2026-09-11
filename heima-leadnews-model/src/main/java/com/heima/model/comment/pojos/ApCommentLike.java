package com.heima.model.comment.pojos;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论点赞表（MongoDB）
 */
@Data
@Document(collection = "ap_comment_like")
public class ApCommentLike implements Serializable {

    @Id
    private String _id;

    /**
     * 评论id
     */
    @Field("commentId")
    private String commentId;

    /**
     * 点赞用户id
     */
    @Field("authorId")
    private Integer authorId;

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
