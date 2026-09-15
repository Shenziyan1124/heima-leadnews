package com.heima.model.wemedia.vos;

import lombok.Data;

import java.util.Date;

@Data
public class WmNewsVo {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private Short type;
    private Integer channelId;
    private String labels;
    private Date createdTime;
    private Date submitedTime;
    private Short status;
    private Date publishTime;
    private String reason;
    private Long articleId;
    private String images;
    private Short enable;
    private String authorName;  // 作者名称
}