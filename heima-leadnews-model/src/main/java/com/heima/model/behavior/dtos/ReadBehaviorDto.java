package com.heima.model.behavior.dtos;

import lombok.Data;

@Data
public class ReadBehaviorDto {
    private Long articleId; // 文章ID
    private Short count;    // 阅读次数
}
