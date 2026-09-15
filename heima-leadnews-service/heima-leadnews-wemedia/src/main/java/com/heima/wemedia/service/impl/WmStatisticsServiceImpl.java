package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.apis.article.IArticleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmArticleListDto;
import com.heima.model.wemedia.pojos.WmNewsStatistics;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vos.WmArticleStatVo;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmNewsStatisticsMapper;
import com.heima.wemedia.service.WmStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class WmStatisticsServiceImpl extends ServiceImpl<WmNewsStatisticsMapper, WmNewsStatistics> implements WmStatisticsService {

    private final IArticleClient iArticleClient;

    /**
     * 根据时间维度查询图文数据
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    @Override
    public ResponseResult getNewsDimension(String beginDate, String endDate) throws ParseException {
        // 0.获取当前用户
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 1. 构建查询条件
        LambdaQueryWrapper<WmNewsStatistics> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WmNewsStatistics::getUserId, user.getId());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (beginDate != null && !beginDate.isEmpty()) {
            Date begin = sdf.parse(beginDate);
            lambdaQueryWrapper.ge(WmNewsStatistics::getCreatedTime, begin);
        }
        if (endDate != null && !endDate.isEmpty()) {
            Date end = sdf.parse(endDate);
            lambdaQueryWrapper.le(WmNewsStatistics::getCreatedTime, end);
        }

        WmNewsStatistics wmNewsStatistics = getOne(lambdaQueryWrapper);

        // 2. 检查缓存是否过期（超过1小时）
        if (wmNewsStatistics != null) {
            long diff = System.currentTimeMillis() - wmNewsStatistics.getCreatedTime().getTime();
            if (diff > 60 * 60 * 1000) {
                // 过期，删除旧记录
                removeById(wmNewsStatistics.getId());
                wmNewsStatistics = null;
            }
        }

        // 3. 没有记录或已过期，调用文章服务查询最新数据
        if (wmNewsStatistics == null) {
            try {
                ResponseResult result = iArticleClient.getNewsDimension(beginDate, endDate, user.getId());
                if (result.getCode() == AppHttpCodeEnum.SUCCESS.getCode() && result.getData() != null) {
                    Map<String, Object> data = (Map<String, Object>) result.getData();
                    wmNewsStatistics = new WmNewsStatistics();
                    wmNewsStatistics.setUserId(user.getId());
                    wmNewsStatistics.setArticle((Integer) data.get("publishNum"));
                    wmNewsStatistics.setLikes((Integer) data.get("likesNum"));
                    wmNewsStatistics.setCollection((Integer) data.get("collectNum"));
                    wmNewsStatistics.setReadCount((Integer) data.get("readNum"));
                    wmNewsStatistics.setComment((Integer) data.get("commentNum"));
                    wmNewsStatistics.setCreatedTime(new Date());
                    save(wmNewsStatistics);
                    return ResponseResult.okResult(wmNewsStatistics);
                }
            } catch (Exception e) {
                log.error("查询文章数据失败: beginDate={}, endDate={}, userId={}", beginDate, endDate, user.getId(), e);
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
            }
        }

        return ResponseResult.okResult(wmNewsStatistics);
    }

    /**
     * 根据作者查询图文数据
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult getAuthorNewsPage(WmArticleListDto dto) {
        dto.checkParam();

        // 0.获取当前用户
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        // 1. 调用文章服务查询作者图文数据
        try {
            ResponseResult result = iArticleClient.getAuthorNewsPage(dto, user.getId(), dto.getOrderType());
            return result;
        } catch (Exception e) {
            log.error("查询作者图文数据失败: {}", dto, e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }
    }
}
