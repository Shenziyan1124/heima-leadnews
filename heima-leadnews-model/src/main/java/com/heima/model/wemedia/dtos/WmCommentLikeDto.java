package com.heima.model.wemedia.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "WmCommentLikeDto", description = "评论点赞请求DTO")
public class WmCommentLikeDto {

    @ApiModelProperty(value = "评论ID")
    private String commentId;

    @ApiModelProperty(value = "操作类型", notes = "0 点赞 1 取消点赞")
    private Integer operation;
}
