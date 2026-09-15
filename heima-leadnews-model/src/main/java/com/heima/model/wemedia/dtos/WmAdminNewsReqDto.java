package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "WmAdminNewsReqDto", description = "自媒体文章管理请求参数")
public class WmAdminNewsReqDto extends PageRequestDto {

    private Integer id;
    private String msg;
    @ApiModelProperty(value = "文章状态", example = "1",
            notes = "当前状态 0 草稿 1 提交（待审核） 2 审核失败 3 人工审核 4 人工审核通过 8 审核通过（待发布） 9 已发布")
    private Integer status;
    private String title;
}
