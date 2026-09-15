package com.heima.model.behavior.dtos;


import lombok.Data;

@Data
public class CollectionBehaviorDto {
    private Long entryId; // 文章id
    private Short operation; // 0收藏 1取消收藏
    private Short type = 0; // 0文章 1动态
}




