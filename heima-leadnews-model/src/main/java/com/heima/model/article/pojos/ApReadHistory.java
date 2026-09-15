package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * APP阅读历史表
 */
@Data
@TableName("ap_read_history")
public class ApReadHistory implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 文章ID
     */
    @TableField("article_id")
    private Long articleId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 阅读次数
     */
    @TableField("read_count")
    private Integer readCount;

    /**
     * 设备ID
     */
    @TableField("equipment_id")
    private Integer equipmentId;

    /**
     * 阅读时长(秒)
     */
    @TableField("read_duration")
    private Integer readDuration;

    /**
     * 阅读百分比
     */
    private Integer percentage;

    /**
     * 首次阅读时间
     */
    @TableField("first_read_time")
    private Date firstReadTime;

    /**
     * 最后阅读时间
     */
    @TableField("last_read_time")
    private Date lastReadTime;

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
}
