package com.tps.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tps.springboot.entity.Message;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MessageMapper extends BaseMapper<Message> {
    int insertSelective(Message record);
}
