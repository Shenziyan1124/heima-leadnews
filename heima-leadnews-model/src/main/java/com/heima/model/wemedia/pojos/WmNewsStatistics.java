package com.heima.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("wm_news_statistics")
public class WmNewsStatistics implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Integer userId;

    @TableField("article")
    private Integer article;

    @TableField("read_count")
    private Integer readCount;

    @TableField("comment")
    private Integer comment;

    @TableField("follow")
    private Integer follow;

    @TableField("collection")
    private Integer collection;

    @TableField("forward")
    private Integer forward;

    @TableField("likes")
    private Integer likes;

    @TableField("unlikes")
    private Integer unlikes;

    @TableField("unfollow")
    private Integer unfollow;

    @TableField("burst")
    private String burst;

    @TableField("created_time")
    private Date createdTime;
}
