package com.heima.search.service.impl;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ApUserSearchServiceImpl implements ApUserSearchService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 保存用户搜索历史记录
     *
     * @param keyword
     * @param userId
     */
    @Override
    @Async
    public void insert(String keyword, Integer userId) {

        // 1.查询当前用户搜索的关键词
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        // 2.存在 更新创建时间
        if (apUserSearch != null) {
            apUserSearch.setCreatedTime(new java.util.Date());
            mongoTemplate.save(apUserSearch);
            return;
        }

        // 3.不存在 判断当前用户的记录是否存在10条,存在替换 否添加
        ApUserSearch apUserSearchNew = new ApUserSearch();
        apUserSearchNew.setUserId(userId);
        apUserSearchNew.setKeyword(keyword);
        apUserSearchNew.setCreatedTime(new java.util.Date());

        Query query1 = new Query(Criteria.where("userId").is(userId));
        query1.with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApUserSearch> apUserSearchList = mongoTemplate.find(query1, ApUserSearch.class);

        if (apUserSearchList.size() < 10 || apUserSearchList == null) {
            mongoTemplate.save(apUserSearchNew);
        } else {
            ApUserSearch apUserSearch1 = apUserSearchList.get(apUserSearchList.size() - 1);
            mongoTemplate.findAndReplace(
                    Query.query(Criteria.where("id").is(apUserSearch1.getId())), apUserSearchNew);
        }

    }

    /**
     * 查询用户搜索历史记录
     *
     * @return
     */
    @Override
    public ResponseResult findUserSearch() {
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        Query query = new Query(Criteria.where("userId").is(user.getId()));
        query.with(Sort.by(Sort.Direction.DESC, "createdTime"));
        List<ApUserSearch> apUserSearchList = mongoTemplate.find(query, ApUserSearch.class);
        return ResponseResult.okResult(apUserSearchList);
    }

    /**
     * 删除搜索历史
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult delUserSearch(HistorySearchDto dto) {
        //1.判断参数
        if (dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.判断是否登录
        ApUser user = AppThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        // 匹配userid和历史记录id
        mongoTemplate.remove(
                Query.query(Criteria.where("userId").is(user.getId()).and("id").is(dto.getId())),
                ApUserSearch.class);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
