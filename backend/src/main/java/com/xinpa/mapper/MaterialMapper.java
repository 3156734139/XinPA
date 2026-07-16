package com.xinpa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinpa.entity.Material;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MaterialMapper extends BaseMapper<Material> {

    @Select("SELECT COALESCE(SUM(file_size), 0) FROM material WHERE user_id = #{userId} AND deleted = 0")
    long sumFileSizeByUser(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(file_size), 0) FROM material WHERE deleted = 0")
    long sumAllFileSize();
}
