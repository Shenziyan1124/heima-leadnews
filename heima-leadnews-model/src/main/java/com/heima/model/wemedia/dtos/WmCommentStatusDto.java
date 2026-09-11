package com.heima.model.wemedia.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "WmCommentStatusDto", description = "文章评论状态DTO")
public class WmCommentStatusDto {
    @ApiModelProperty(value = "文章ID")
    private Long articleId;
    @ApiModelProperty(value = "操作类型 0  关闭评论 1  开启评论")
    private Integer operation;
}
