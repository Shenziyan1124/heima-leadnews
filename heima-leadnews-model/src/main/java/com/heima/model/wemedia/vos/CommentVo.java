package com.heima.model.wemedia.vos;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "CommentVo", description = "文章详情评论Vo")
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
     * 排序
     */
    private Integer ord;

    /**
     * 创建时间
     */
    private Long createdTime;

    /**
     * 更新时间
     */
    private Long updatedTime;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 当前用户对评论的操作状态  0 点赞  1 取消点赞
     */
    private Integer operation;
}
