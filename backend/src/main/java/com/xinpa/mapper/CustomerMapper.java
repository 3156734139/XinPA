package com.xinpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinpa.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    @Select("SELECT COUNT(*) FROM customer WHERE user_id = #{userId} AND is_blacklist = 1 AND deleted = 0")
    long countBlacklistByUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM customer WHERE is_blacklist = 1 AND deleted = 0")
    long countAllBlacklist();
}
