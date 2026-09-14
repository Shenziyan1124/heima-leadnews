package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ArticleCommentListDto", description = "文章评论列表请求DTO")
public class WmArticleCommentListDto extends PageRequestDto {

    @ApiModelProperty(value = "文章ID")
    private Long articleId;
}
