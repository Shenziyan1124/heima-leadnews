package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@ApiModel(value = "WmCommentListDto", description = "评论列表")
public class WmCommentListDto extends PageRequestDto {

    @ApiModelProperty(value = "beginDate")
    private String beginDate;    // 开始时间
    @ApiModelProperty(value = "endDate")
    private String endDate;      // 结束时间

    public Date parseBeginDate() {
        return parseDate(beginDate);
    }

    public Date parseEndDate() {
        return parseDate(endDate);
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}
