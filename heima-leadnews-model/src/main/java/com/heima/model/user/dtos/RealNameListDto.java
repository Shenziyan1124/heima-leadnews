package com.heima.model.user.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "实名认证列表请求参数")
public class RealNameListDto extends PageRequestDto {
    private Long id;
    private String msg;
    @ApiModelProperty(value = "用户状态", example = "0", notes = "0:创建中 1:待审核 2:审核失败 9:审核通过")
    private Integer status;
}
