package com.heima.comment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.comment.service.ApCommentManageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmCommentListDto;
import com.heima.model.wemedia.vos.WmCommentVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApCommentManageServiceImpl implements ApCommentManageService {

    private final MongoTemplate mongoTemplate;
    private final IWemediaClient iWemediaClient;

    @Override
    public PageResponseResult findNewsComments(WmCommentListDto dto) {
        dto.checkParam();

        // 1. 构建查询条件
        org.springframework.data.mongodb.core.query.Criteria criteria = new org.springframework.data.mongodb.core.query.Criteria();
        Date beginDate = dto.parseBeginDate();
        Date endDate = dto.parseEndDate();

        if (beginDate != null && endDate != null) {
            criteria.and("createdTime").gte(beginDate).lte(endDate);
        } else if (beginDate != null) {
            criteria.and("createdTime").gte(beginDate);
        } else if (endDate != null) {
            criteria.and("createdTime").lte(endDate);
        }

        // 2. 查询总数（按entryId分组后的组数）
        Aggregation countAggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("entryId")
        );
        List<Map> countResults = mongoTemplate.aggregate(
                countAggregation, "ap_comment", Map.class).getMappedResults();
        int total = countResults.size();

        // 3. 分页查询当前页数据
        Aggregation pageAggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("entryId")
                        .count().as("comments")
                        .min("createdTime").as("createdTime"),
                Aggregation.project()
                        .and("_id").as("id")
                        .and("comments").as("comments")
                        .and("createdTime").as("createdTime"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "createdTime"),
                Aggregation.skip((long) (dto.getPage() - 1) * dto.getSize()),
                Aggregation.limit(dto.getSize())
        );

        AggregationResults<Map> results = mongoTemplate.aggregate(
                pageAggregation, "ap_comment", Map.class);
        List<Map> mappedResults = results.getMappedResults();

        // 4. 转换为VO，查询文章标题
        List<WmCommentVo> voList = new ArrayList<>();
        for (Map map : mappedResults) {
            WmCommentVo vo = new WmCommentVo();
            vo.setId((Long) map.get("id"));
            vo.setComments((Integer) map.get("comments"));
            Object ct = map.get("createdTime");
            if (ct instanceof Date) {
                vo.setCreatedTime(((Date) ct).getTime());
            } else if (ct instanceof Long) {
                vo.setCreatedTime((Long) ct);
            }
            vo.setIsComment(true);

            // 根据articleId查询文章标题
            try {
                ResponseResult titleResult = iWemediaClient.getNewsByArticleId(vo.getId());
                if (titleResult != null && titleResult.getData() != null) {
                    vo.setTitle((String) titleResult.getData());
                }
            } catch (Exception e) {
                log.warn("查询文章标题失败: articleId={}", vo.getId(), e);
            }

            voList.add(vo);
        }

        // 5. 封装分页结果
        PageResponseResult pageResponseResult = new PageResponseResult(
                dto.getPage(), dto.getSize(), total);
        pageResponseResult.setData(voList);
        return pageResponseResult;
    }
}
