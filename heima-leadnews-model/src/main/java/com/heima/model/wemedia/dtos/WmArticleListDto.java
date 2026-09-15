package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "WmArticleListDto", description = "文章列表请求DTO")
public class WmArticleListDto extends PageRequestDto {

    @ApiModelProperty(value = "开始时间")
    private String beginDate;

    @ApiModelProperty(value = "结束时间")
    private String endDate;

    @ApiModelProperty(value = "排序类型", notes = "likes-点赞, readCount-阅读, commentCount-评论, forward-转发, 默认按时间")
    private String orderType;
}
