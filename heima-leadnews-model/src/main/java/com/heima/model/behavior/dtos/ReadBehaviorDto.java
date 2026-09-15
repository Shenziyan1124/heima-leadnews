package com.heima.model.behavior.dtos;

import lombok.Data;

@Data
public class ReadBehaviorDto {
    private Long articleId; // 文章ID
    private Short count;    // 阅读次数
    private Integer equipmentId; // 设备ID
    private Integer readDuration; // 阅读时长
    private Integer percentage; // 阅读进度

}
