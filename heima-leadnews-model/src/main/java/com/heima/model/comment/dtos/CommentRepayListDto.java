package com.heima.model.comment.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentRepayListDto implements Serializable {

    /**
     * 评论id
     */
    private String commentId;

    /**
     * 分页条件 最小时间
     */
    private Date minDate;

    /**
     * 分页页码
     */
    private Integer size;
}
