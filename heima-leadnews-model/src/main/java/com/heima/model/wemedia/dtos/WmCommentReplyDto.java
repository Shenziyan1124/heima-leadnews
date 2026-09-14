package com.heima.model.wemedia.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "评论回复")
public class WmCommentReplyDto {
    @ApiModelProperty(value = "评论ID", required = true)
    private String commentId;
    @ApiModelProperty(value = "回复内容", required = true)
    private String content;
}
