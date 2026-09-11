package com.heima.model.wemedia.vos;

import lombok.Data;

@Data
public class WmCommentVo {
    private Long id;
    private String title;
    private Integer comments;
    private Boolean isComment;
    private Long createdTime;
}
