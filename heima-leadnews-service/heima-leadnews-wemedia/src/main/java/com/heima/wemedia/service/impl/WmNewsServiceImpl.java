package com.heima.wemedia.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.common.constants.WmNewsMessageConstants;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmAdminNewsReqDto;
import com.heima.model.wemedia.dtos.WmNewsDownOrUpDto;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.model.wemedia.vos.WmNewsVo;
import com.heima.utils.thread.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Autowired
    private WmMaterialService wmMaterialService;

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private WmNewsTaskService wmNewsTaskService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private WmUserMapper wmUserMapper;

    /**
     * 查询文章列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {

        // 1. 参数校验
        if (dto == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        dto.checkParam();

        // 2. 获取user
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);

        // 3. 分页查询
        IPage page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper
                .eq(WmNews::getUserId, user.getId())
                .eq(dto.getStatus() != null, WmNews::getStatus, dto.getStatus())
                .eq(dto.getChannelId() != null, WmNews::getChannelId, dto.getChannelId())
                .like(StringUtils.isNotBlank(dto.getKeyword()), WmNews::getTitle, dto.getKeyword())
                .between(dto.getBeginPubDate() != null && dto.getEndPubDate() != null,
                        WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate())
                //.ge(dto.getBeginPubDate() != null, WmNews::getPublishTime, dto.getBeginPubDate())
                //.le(dto.getEndPubDate() != null, WmNews::getPublishTime, dto.getEndPubDate())
                .orderByDesc(WmNews::getPublishTime);
        IPage result = page(page, lambdaQueryWrapper);
        log.info("result: {}", result);

        page = page(page, lambdaQueryWrapper);

        PageResponseResult pageResponseResult =
                new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());

        return pageResponseResult;
    }

    /**
     * 发布/修改/保存草稿文章
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        // 0.参数校验
        if (dto == null || dto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        // 1.保存或修改文章
        WmNews wmNews = new WmNews();
        // 属性拷贝,相同的拷贝进去
        BeanUtils.copyProperties(dto, wmNews);
        // 封面图片dto是list, wmNews是string, list -> string,逗号分隔
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            wmNews.setImages(StringUtils.join(dto.getImages(), ","));
        }
        // dto的type当前封面类型自动是-1,wnNews不能存-1,单独转换
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }

        saveOrUpdateWnNews(wmNews);

        // 2.判断是否为草稿,草稿直接结束当前方法
        if (dto.getStatus().equals(WmNews.Status.NORMAL.getCode())) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        // 3.不是草稿,保存文章内容和图片的关系
        // 提取内容中的图片
        List<String> images = extractUrlInfo(dto.getContent());
        // 保存文章内容图片关系
        saveRelativeInfoForContent(wmNews.getId(), images);

        // 4.不是草稿,保存文章主图和图片的关系,如果当前布局是自动,需要匹配封面图片
        saveRelativeInfoForCover(wmNews, dto, images);

        // 5.提交文章进行自动审核
        //wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        wmNewsTaskService.addNewsTask(wmNews.getId(), dto.getPublishTime());
        // 1.判断是否包含文章id
        // 2.包含--是修改
        // 2.1 删除文章图片与素材的关系
        // 2.2 执行修改
        // 2.3 关联文章内容图片与素材的关系
        // 2.4 关联文章主图与素材的关系
        // 3.不包含--是新增
        // 3.1 新增文章操作
        // 3.2 关联文章内容图片与素材的关系
        // 3.3 关联文章主图与素材的关系
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 保存文章主图和素材的关系
     * 第一个功能: 如果当前封面为自动,设置封面类型的数据
     * 匹配规则:
     * 1. 如果内容图片大于等于1,小于3, 单图 type = 1
     * 2. 如果内容图片大于3, 多图 type = 3
     * 3. 如果内容没有图片,无图 type = 0
     * <p>
     * 第二个功能: 保存封面图片和素材的关系
     *
     * @param wmNews
     * @param dto
     * @param images
     */
    private void saveRelativeInfoForCover(WmNews wmNews, WmNewsDto dto, List<String> images) {

        List<String> dtoImages = dto.getImages();

        // 如果当前封面为自动,设置封面类型的数据
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            if (images.size() >= 1 && images.size() < 3) {
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                dtoImages = images.stream().limit(1).collect(Collectors.toList());
                ;
            } else if (images.size() >= 3) {
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                dtoImages = images.stream().limit(3).collect(Collectors.toList());
            } else {
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }

            // 设置封面图片,修改news
            if (dtoImages != null && !dtoImages.isEmpty()) {
                wmNews.setImages(StringUtils.join(dtoImages, ","));
            }
            updateById(wmNews);
        }

        // 保存封面图片和素材的关系
        if (dtoImages != null && !dtoImages.isEmpty()) {
            saveRelativeInfo(dtoImages, wmNews.getId(), WemediaConstants.WM_COVER_REFERENCE);
        }

    }

    /**
     * 保存文章内容和素材的关系
     *
     * @param newsId
     * @param images
     */
    private void saveRelativeInfoForContent(Integer newsId, List<String> images) {
        saveRelativeInfo(images, newsId, WemediaConstants.WM_CONTENT_REFERENCE);
        //saveRelativeInfo(images, newsId, WemediaConstants.WM_COVER_REFERENCE);
    }


    /**
     * 保存文章内容图片和素材的关系
     *
     * @param images
     * @param newsId
     * @param type
     */
    private void saveRelativeInfo(List<String> images, Integer newsId, Short type) {
        if (images != null && !images.isEmpty()) {
            // 通过images的url来获取id
            List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(
                    new LambdaQueryWrapper<WmMaterial>().in(WmMaterial::getUrl, images));

            // 判断素材是否存在,如果不存在则抛出异常,1回滚 2提示素材失效
            if (wmMaterials == null || wmMaterials.isEmpty()) {
                throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
            }
            if (wmMaterials.size() != images.size()) {
                throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
            }

            List<Integer> imageIdList = wmMaterials.stream()
                    .map(WmMaterial::getId).collect(Collectors.toList());

            // 批量保存
            wmNewsMaterialMapper.saveRelations(imageIdList, newsId, type);
        }
    }

    /**
     * 从内容中提取图片
     *
     * @param content
     * @return
     */
    private List<String> extractUrlInfo(String content) {
        List<String> materials = new ArrayList<>();

        List<Map> maps = JSON.parseArray(content, Map.class);
        for (Map map : maps) {
            if (map.get("type").equals("image")) {
                String url = (String) map.get("value");
                materials.add(url);
            }
        }
        return materials;
    }


    /**
     * 保存或修改文章
     *
     * @param wmNews
     */
    private void saveOrUpdateWnNews(WmNews wmNews) {
        // 补全属性
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short) 1); // 默认上架

        if (wmNews.getId() == null)
            save(wmNews);
        else
            // 修改
            // 删除文章与素材的关系
            wmNewsMaterialMapper.delete(
                    new LambdaQueryWrapper<WmNewsMaterial>()
                            .eq(WmNewsMaterial::getNewsId, wmNews.getId())
            );
        updateById(wmNews);
    }


    /**
     * 获取文章信息
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult getNewsDetail(Integer id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        return ResponseResult.okResult(wmNews);
    }

    /**
     * 删除文章
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult delNews(Integer id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "缺少文章Id");
        }

        WmNews wmNews = getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }
        if (wmNews.getStatus() == WmNews.Status.PUBLISHED.getCode()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章已发布，不能删除");
        }

        boolean b = removeById(id);
        if (!b) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 文章上下架
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult downOrUp(WmNewsDownOrUpDto dto) {
        if (dto == null || dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "缺少文章Id");
        }

        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }

        if (wmNews.getStatus() != WmNews.Status.PUBLISHED.getCode()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章未发布，不能上下架");
        }

        // 上架
        if (dto.getEnable() != null && dto.getEnable().equals(WemediaConstants.WM_NEWS_UP)){
            // 多图文章:校验图片数量不能少于3张
            if (
                    wmNews.getType() != null
                    && wmNews.getType().equals(WemediaConstants.WM_NEWS_MANY_IMAGE)
                    && StringUtils.isNotBlank(wmNews.getImages())
            ){

                Integer coverCount = wmNewsMaterialMapper.selectCount(new LambdaQueryWrapper<WmNewsMaterial>()
                        .eq(WmNewsMaterial::getNewsId, wmNews.getId())
                        .eq(WmNewsMaterial::getType, WemediaConstants.WM_COVER_REFERENCE));

                if (coverCount < 3){
                    return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "多图文章必须有三张图片");
                }
            }
            wmNews.setEnable(WemediaConstants.WM_NEWS_UP);
        }else {
            wmNews.setEnable(WemediaConstants.WM_NEWS_DOWN);
        }


        updateById(wmNews);

        if (wmNews.getArticleId() != null){
            // 修改文章状态后,利用kafka 将articleId和状态发送给article服务
            Map<String, Object> map = new HashMap<>();
            map.put("articleId", wmNews.getArticleId());
            map.put("enable", wmNews.getEnable());
            String message = JSON.toJSONString(map);
            log.info("发送Kafka消息: topic={}, message={}", WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC, message);
            kafkaTemplate.send(WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC, message);
        } else {
            log.warn("wmNews.articleId为空,不发送Kafka消息");
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    /**
     * 查询admin端文章列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findAdminList(WmAdminNewsReqDto dto) {
        if (dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.checkParam();


        // 构建查询条件
        IPage page = new Page<>(dto.getPage(), dto.getSize());

        LambdaQueryWrapper<WmNews> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dto.getStatus() != null, WmNews::getStatus, dto.getStatus());
        queryWrapper.like(dto.getTitle() != null, WmNews::getTitle, dto.getTitle());
        queryWrapper.orderByDesc(WmNews::getCreatedTime);

        page = page(page, queryWrapper);
        
        // 获取文章列表
        List<WmNews> wmNewsList = page.getRecords();
        
        // 收集所有的userId
        Set<Integer> userIds = wmNewsList.stream()
                .map(WmNews::getUserId)
                .collect(Collectors.toSet());
        
        // 批量查询用户信息
        Map<Integer, WmUser> userMap;
        if (!userIds.isEmpty()) {
            List<WmUser> wmUsers = wmUserMapper.selectBatchIds(userIds);
            userMap = wmUsers.stream()
                    .collect(Collectors.toMap(WmUser::getId, w -> w));
        } else {
            userMap = new HashMap<>();
        }

        // 转换为WmNewsVo列表，设置authorName
        List<WmNewsVo> wmNewsVoList = wmNewsList.stream().map(wmNews -> {
            WmNewsVo wmNewsVo = new WmNewsVo();
            BeanUtils.copyProperties(wmNews, wmNewsVo);
            WmUser wmUser = userMap.get(wmNews.getUserId());
            if (wmUser != null) {
                wmNewsVo.setAuthorName(wmUser.getName());
            }
            return wmNewsVo;
        }).collect(Collectors.toList());
        
        PageResponseResult pageResponseResult =
                new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(wmNewsVoList);

        return pageResponseResult;

    }

    /**
     * 获取admin端文章详情
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult getAdminNewsDetail(Integer id) {
        if (id == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);

        WmNews wmNews = getById(id);
        if (wmNews == null)
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);

        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        WmNewsVo wmNewsVo = new WmNewsVo();
        BeanUtils.copyProperties(wmNews, wmNewsVo);
        wmNewsVo.setAuthorName(wmUser.getName());

        return ResponseResult.okResult(wmNewsVo);
    }

    /**
     * admin文章审核失败
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult adminAuthFail(WmAdminNewsReqDto dto) {
        if (dto == null || dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        wmNews.setStatus(WmNews.Status.FAIL.getCode());
        wmNews.setReason(dto.getMsg());
        updateById(wmNews);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * admin文章审核通过
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult adminAuthPass(WmAdminNewsReqDto dto) {
        if (dto == null || dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());
        wmNews.setReason(WmNews.Status.SUCCESS.name());
        updateById(wmNews);

        // 更新到app端
        wmNewsAutoScanService.saveAppArticle(wmNews);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
