package com.tps.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;


@Data
@TableName("sys_testfile")
public class TestFiles {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String type;
    private Long size;
    private String url;
    private String enable;
    private String md5;
    private Boolean isDelete;
    @ApiModelProperty("创建时间")
    private Date createTime;
    @TableField("user_id")
    private int userid;

    @TableField("jsonUrl")
    private String jsonUrl;
}
