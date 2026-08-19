package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WmMaterialPageDto extends PageRequestDto {  // 继承分页基类,自带 page/size
    private Short isCollection;  // 0 未收藏 1 已收藏
}