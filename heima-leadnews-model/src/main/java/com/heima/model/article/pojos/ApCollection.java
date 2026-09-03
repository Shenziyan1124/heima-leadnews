package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * APP收藏表
 */
@Data
@TableName("ap_collection")
public class ApCollection implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 入口ID（可能是用户ID）
     */
    private Integer entryId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 类型 0文章 1动态 2评论
     */
    private Short type;

    /**
     * 是否删除 0未删除 1已删除
     */
    private Short isDelete;

    /**
     * 收藏时间
     */
    private Date collectionTime;

    /**
     * 发布时间
     */
    private Date publishedTime;
}
