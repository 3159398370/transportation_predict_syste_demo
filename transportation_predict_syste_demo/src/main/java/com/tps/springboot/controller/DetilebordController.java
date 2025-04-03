package com.tps.springboot.controller;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.OnlineDate;
import com.tps.springboot.mapper.ResultMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/detilebord")
public class DetilebordController {
    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Value("${server.ip}")
    private String serverIp;

    @Resource
    private ResultMapper resultMapper;
    //private  FileMapper fileMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/detail/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(resultMapper.selectById(id));
    }

    //清除一条缓存，key为要清空的数据
//    @CacheEvict(value="files",key="'frontAll'")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        resultMapper.deleteById(id);
        flushRedis(Constants.FILES_KEY);
        return Result.success();
    }

    @GetMapping("/totle")
    public Result totle() {
        List<OnlineDate> onlinedates = resultMapper.selectList(new QueryWrapper<OnlineDate>());
        String today = DateUtil.today();
        Integer totle=0;
        for (OnlineDate onlineDate : onlinedates) {
            Date createTime = onlineDate.getCreateTime();
            String format = DateUtil.format(createTime, "yyyy-MM-dd");
            if (format.equals(today)){
                totle++;
            }

        }
        return Result.success(totle);
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        // select * from sys_file where id in (id,id,id...)
        QueryWrapper<OnlineDate> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<OnlineDate> onlinedates = resultMapper.selectList(queryWrapper);
        for (OnlineDate onlineDate : onlinedates) {
            resultMapper.deleteById(onlineDate);
        }
        return Result.success();
    }

    /**
     * 分页查询接口
     * @param pageNum
     * @param pageSize
     * @param result
     * @return
     */

    @GetMapping("/page/{id}")
    public Result findPage(@PathVariable Integer id,
                           @RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") Integer result) {
        //LambdaQueryWrapper<OnlineDate> queryWrapper = new LambdaQueryWrapper<>();
        QueryWrapper<OnlineDate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("testfile_id",id);
//        queryWrapper.eq(OnlineDate::getTestfileid,id);
        //queryWrapper.eq("is_delete", false);
        //queryWrapper.orderByDesc("id");
        //queryWrapper.like("label", label);
       if (result!=null) {
           queryWrapper.like("result", result);
        }
        return Result.success(resultMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper));
    }

    // 设置缓存
    private void setCache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // 删除缓存
    private void flushRedis(String key) {
        stringRedisTemplate.delete(key);
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") Integer result) {
        //LambdaQueryWrapper<OnlineDate> queryWrapper = new LambdaQueryWrapper<>();
        QueryWrapper<OnlineDate> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("testfile_id",id);
//        queryWrapper.eq(OnlineDate::getTestfileid,id);
        //queryWrapper.eq("is_delete", false);
        //queryWrapper.orderByDesc("id");
        //queryWrapper.like("label", label);
        if (result!=null) {
            queryWrapper.like("result", result);
        }
        return Result.success(resultMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper));
    }
}
