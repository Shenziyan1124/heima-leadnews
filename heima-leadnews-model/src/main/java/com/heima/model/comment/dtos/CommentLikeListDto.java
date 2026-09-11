package com.heima.model.comment.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CommentLikeListDto implements Serializable {

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 分页条件 最小时间
     */
    private Date minDate;

    /**
     * 分页页码
     */
    private Integer index;
}
