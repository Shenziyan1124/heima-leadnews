package com.heima.apis.wemedia;


import com.heima.apis.wemedia.fallback.IWemediaClientFallback;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "leadnews-wemedia",fallbackFactory = IWemediaClientFallback.class)
public interface IWemediaClient {

    /**
     * 创建自媒体用户
     * @return
     */
    @PostMapping("/api/v1/user/create")
    ResponseResult createWmUser(@RequestBody WmUser wmUser);

    /**
     * 根据用户ID查询自媒体用户
     */
    @PostMapping("/api/v1/user/getByUserId")
    ResponseResult getWmUserByUserId(@RequestBody Integer userId);

    /**
     * 获取自媒体频道列表
     * @return
     */
    @GetMapping("/api/v1/channel/list")
    ResponseResult getChannelList();

    /**
     * 根据文章ID查询文章
     * @param articleId
     * @return
     */
    @GetMapping("/api/v1/news/getByArticleId")
    ResponseResult getNewsByArticleId(@RequestParam("articleId") Long articleId);
}
