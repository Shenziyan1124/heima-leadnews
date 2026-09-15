package com.heima.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.comment.pojos.ApComment;
import com.heima.model.user.pojos.ApUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApCommentLikeMapper extends BaseMapper<ApComment> {

}
