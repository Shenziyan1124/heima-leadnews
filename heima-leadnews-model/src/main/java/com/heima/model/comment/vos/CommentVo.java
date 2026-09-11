package com.heima.model.comment.vos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论列表响应Vo
 */
@Data
public class CommentVo implements Serializable {

    /**
     * 评论id
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
     * 文章id
     */
    private Long entryId;

    /**
     * 频道id
     */
    private Integer channelId;

    /**
     * 评论类型 0 站内评论 1 站外评论
     */
    private Integer type;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片
     */
    private String image;

    /**
     * 点赞数量
     */
    private Integer likes;

    /**
     * 回复数量
     */
    private Integer reply;

    /**
     * 标记 0 正常 1 删除
     */
    private Integer flag;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 地址
     */
    private String address;

    /**
     * 排序
     */
    private Integer ord;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 当前用户对评论的操作状态  0 点赞  null 未点赞
     */
    private Integer operation;
}
