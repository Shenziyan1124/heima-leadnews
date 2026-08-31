package com.heima.model.wemedia.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(description = "频道信息")
public class WmChannelDto {
    private Integer id;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "是否默认频道", example = "false", notes = "是否默认频道")
    private Boolean isDefault = false;
    @ApiModelProperty(value = "名称")
    private String name;
    @ApiModelProperty(value = "排序")
    private Integer ord;
    @ApiModelProperty(value = "状态",example = "false",notes = "true:启用 false:禁用")
    private Boolean status;
}
