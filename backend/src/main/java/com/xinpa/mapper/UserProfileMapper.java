package com.xinpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinpa.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
