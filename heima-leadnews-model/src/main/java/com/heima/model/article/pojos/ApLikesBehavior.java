package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * APP点赞行为表
 */
@Data
@TableName("ap_likes")
public class ApLikesBehavior implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文章/动态/评论ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 0文章 1动态 2评论
     */
    private Short type;

    /**
     * 0点赞 1取消点赞 2 不喜欢 3 取消不喜欢
     */
    private Short operation;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private Date createdTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 是否删除 0未删除 1已删除
     */
    @TableField("is_delete")
    private Short isDelete;
}
