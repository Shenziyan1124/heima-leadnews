package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "WmChannelslistDto", description = "频道列表查询")
public class WmChannelslistDto extends PageRequestDto {

    @ApiModelProperty(value = "频道名称")
    private String name;
}
