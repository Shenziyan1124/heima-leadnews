package com.heima.model.article.vos;

import lombok.Data;

@Data
public class ArticleBehaviorVo {
    private boolean islike;      // 是否点赞
    private boolean isunlike;    // 是否不喜欢
    private boolean iscollection; // 是否收藏
    private boolean isfollow;    // 是否关注作者
}