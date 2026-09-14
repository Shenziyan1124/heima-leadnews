package com.heima.model.wemedia.vos;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ArticleCommentVo implements Serializable {

    /**
     * 评论信息
     */
    private CommentVo apComments;

    /**
     * 评论回复列表
     */
    private List<CommentRepayVo> apCommentRepays;
}
