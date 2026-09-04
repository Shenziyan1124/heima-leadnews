package com.heima.model.user.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * APP用户关注表
 */
@Data
@TableName("ap_user_follow")
public class ApUserFollow implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关注者）
     */
    private Integer userId;

    /**
     * 关注作者ID（被关注者）
     */
    private Integer followId;

    /**
     * 粉丝昵称
     */
    private String followName;

    /**
     * 关注度 0偶尔感兴趣 1一般 2经常 3高度
     */
    private Short level;

    /**
     * 是否动态通知
     */
    private Short isNotice;

    /**
     * 创建时间
     */
    private Date createdTime;
}
