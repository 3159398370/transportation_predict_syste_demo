package com.tps.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tps.springboot.entity.OnlineDate;
import org.apache.ibatis.annotations.Mapper;



@Mapper
public interface ResultMapper extends BaseMapper<OnlineDate> {
}
