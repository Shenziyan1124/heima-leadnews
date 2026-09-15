package com.heima.model.wemedia.vos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "WmArticleStatVo", description = "文章统计VO")
public class WmArticleStatVo implements Serializable {

    @ApiModelProperty(value = "文章ID")
    private Long articleId;

    @ApiModelProperty(value = "文章标题")
    private String title;

    @ApiModelProperty(value = "阅读量")
    private Integer readCount;

    @ApiModelProperty(value = "评论量")
    private Integer commentCount;

    @ApiModelProperty(value = "收藏量")
    private Integer collectionCount;

    @ApiModelProperty(value = "点赞量")
    private Integer likeCount;

    @ApiModelProperty(value = "发布时间")
    private Date publishTime;
}
