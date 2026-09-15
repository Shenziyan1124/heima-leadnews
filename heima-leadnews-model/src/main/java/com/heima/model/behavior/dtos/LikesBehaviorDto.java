package com.heima.model.behavior.dtos;

import lombok.Data;

@Data
public class LikesBehaviorDto {
    private Long articleId;
    private Short operation; // 0 点赞 1 取消点赞
    private Short type;      // 0 文章 1 动态 2 评论
}
