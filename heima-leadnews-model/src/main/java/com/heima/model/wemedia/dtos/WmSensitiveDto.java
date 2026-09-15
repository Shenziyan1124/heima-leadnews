package com.heima.model.wemedia.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel(value = "WmSensitiveDto", description = "敏感信息查询条件")
public class WmSensitiveDto {
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "敏感信息词")
    private String sensitives;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
}
