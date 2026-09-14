package com.heima.model.wemedia.vos;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "WmCommentVo", description = "所有文章评论列表Vo")
public class WmCommentVo {
    private Long id;
    private String title;
    private Integer comments;
    private Boolean isComment;
    private Long createdTime;
}
