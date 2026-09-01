package com.heima.model.article.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "文章行为dto")
public class ArticleBehaviorDto {
    @ApiModelProperty(value = "文章id", required = true)
    private Long articleId;
    @ApiModelProperty(value = "作者id", required = true)
    private Long authorId;
}
