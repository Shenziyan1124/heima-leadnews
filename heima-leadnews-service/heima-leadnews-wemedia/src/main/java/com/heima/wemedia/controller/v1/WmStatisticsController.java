package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmArticleListDto;
import com.heima.wemedia.service.WmStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/api/v1/statistics")
@Api(tags = "图文统计")
@RequiredArgsConstructor
public class WmStatisticsController {

    private final WmStatisticsService wmStatisticsService;

    @GetMapping("/newsDimension")
    @ApiOperation(value = "根据时间维度查询图文数据", notes = "根据时间维度查询图文数据")
    public ResponseResult getNewsDimension(String beginDate, String endDate) throws ParseException {
        return wmStatisticsService.getNewsDimension(beginDate, endDate);
    }

    @GetMapping("/newsPage")
    @ApiOperation(value = "分页查询图文数据", notes = "分页查询图文数据")
    public ResponseResult getNewsPage(WmArticleListDto dto) {
        return wmStatisticsService.getAuthorNewsPage(dto);
    }
}
