package com.heima.model.wemedia.dtos;

import lombok.Data;

@Data
public class WmNewsDownOrUpDto {
    private Integer id;
    private Short enable; // 0 下架 1 上架
}

