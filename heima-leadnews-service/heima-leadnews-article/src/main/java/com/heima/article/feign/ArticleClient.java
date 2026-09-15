package com.heima.article.feign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.heima.apis.article.IArticleClient;
import com.heima.article.service.ApArticleConfigService;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmArticleListDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ArticleClient implements IArticleClient {

    @Autowired
    private ApArticleService apArticleService;
    @Autowired
    private ApArticleConfigService apArticleConfigService;

    /**
     * 保存文章
     * @param dto
     * @return
     */
    @Override
    @PostMapping("/api/v1/article/save")
    public ResponseResult saveArticle(@RequestBody ArticleDto dto) {
        return apArticleService.saveArticle(dto);
    }

    /**
     * 根据频道id获取文章数量
     * @param id
     * @return
     */
    @Override
    @GetMapping("/api/v1/article/channel/{id}/count")
    public ResponseResult getArticleCountByChannelId(@PathVariable("id") Integer id) {
        return apArticleService.getArticleCountByChannelId(id);
    }


    /**
     * 根据文章id获取文章评论状态
     * @param articleId
     * @return
     */
    @Override
    @GetMapping("/api/v1/article/comment_status/{articleId}")
    public ResponseResult getCommentStatus(@PathVariable("articleId") Long articleId) {
        ApArticleConfig config = apArticleConfigService.lambdaQuery()
                .eq(ApArticleConfig::getArticleId, articleId)
                .one();
        if (config != null) {
            return ResponseResult.okResult(config.getIsComment());
        }
        return ResponseResult.okResult(true);
    }

    /**
     * 更新文章评论状态
     * @param articleId
     * @param isComment
     * @return
     */
    @Override
    @PostMapping("/api/v1/article/comment_status")
    public ResponseResult updateCommentStatus(@RequestParam("articleId") Long articleId, @RequestParam("isComment") Boolean isComment) {
        apArticleConfigService.lambdaUpdate()
                .eq(ApArticleConfig::getArticleId, articleId)
                .set(ApArticleConfig::getIsComment, isComment)
                .update();
        return ResponseResult.okResult(null);
    }


    /**
     * 获取作者数据维度
     * @param beginDate
     * @param endDate
     * @param id
     * @return
     * @throws ParseException
     */
    @Override
    @GetMapping("/api/v1/article/newsDimension")
    public ResponseResult getNewsDimension(@RequestParam(value = "beginDate", required = false) String beginDate,
                                           @RequestParam(value = "endDate", required = false) String endDate,
                                           @RequestParam("id") Integer id) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticle::getAuthorId, id);

        if (beginDate != null && !beginDate.isEmpty()) {
            wrapper.ge(ApArticle::getPublishTime, sdf.parse(beginDate));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(ApArticle::getPublishTime, sdf.parse(endDate));
        }

        // 统计发布量
        int publishNum = apArticleService.count(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("publishNum", publishNum);
        result.put("likesNum", 0);
        result.put("collectNum", 0);
        result.put("readNum", 0);
        result.put("commentNum", 0);

        return ResponseResult.okResult(result);
    }

    /**
     * 获取作者文章分页列表
     *
     * @param dto
     * @param id
     * @return
     */
    @Override
    @PostMapping("/api/v1/article/authorNewsPage")
    public ResponseResult getAuthorNewsPage(@RequestBody WmArticleListDto dto, @RequestParam("id") Integer id,
                                            @RequestParam(value = "orderType", required = false) String orderType) throws ParseException {
        // 参数检查
        dto.checkParam();
        // 检查id
        if (id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 创建分页对象
        IPage page = new Page<>(dto.getPage(), dto.getSize());

        // 创建查询条件
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticle::getAuthorId, id);
        if (dto.getBeginDate() != null && !dto.getBeginDate().isEmpty()) {
            wrapper.ge(ApArticle::getPublishTime, sdf.parse(dto.getBeginDate()));
        }
        if (dto.getEndDate() != null && !dto.getEndDate().isEmpty()) {
            wrapper.le(ApArticle::getPublishTime, sdf.parse(dto.getEndDate()));
        }

        // 根据orderType动态排序
        if ("likes".equals(orderType)) {
            wrapper.orderByDesc(ApArticle::getLikes);
        } else if ("readCount".equals(orderType)) {
            wrapper.orderByDesc(ApArticle::getViews);
        } else if ("commentCount".equals(orderType)) {
            wrapper.orderByDesc(ApArticle::getComment);
        } else if ("collection".equals(orderType)) {
            wrapper.orderByDesc(ApArticle::getCollection);
        } else {
            // 默认按发布时间降序
            wrapper.orderByDesc(ApArticle::getPublishTime);
        }

        page = apArticleService.page(page, wrapper);

        PageResponseResult pageResponseResult =
                new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());

        return pageResponseResult;
    }


}
