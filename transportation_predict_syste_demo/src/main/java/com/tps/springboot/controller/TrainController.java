package com.tps.springboot.controller;

import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Files;
import com.tps.springboot.service.ITrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 文件上传相关接口
 */
@RestController
//@RequestMapping("/train")
@RequestMapping("/python")
@Transactional
public class TrainController  {

    @Autowired
    private ITrainService trainService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


//    @Resource
//    private SystemConfigUtils config;
    /**
     * 文件上传接口
     *
     * @param file 前端传递过来的文件
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public Result uploadTrainFile(@RequestParam MultipartFile file) throws Exception {

        if(null == file)
        {
            return Result.error(Constants.CODE_400,"上传的文件为空,请上传文件！");
        }

        trainService.uploadTrainFile(file);

        flushRedis(Constants.FILES_KEY);
        return Result.success();
    }

    /***
     * 训练接口
     * @param url
     * @return
     * @throws IOException
     */
    //@Async
    @GetMapping("/getUrl/{url}/{type}")
    public Result beginTrain(@PathVariable String url, @PathVariable String type) throws IOException {
        System.out.println("***********URL********");
        System.out.println(url);
        System.out.println("***********type********");
        System.out.println(type);
        System.out.println("开始多线程,这是模型训练********");
        long statTime = System.currentTimeMillis();

        Result result = trainService.train(url, type);

        System.out.println("训练成功");

        flushRedis(Constants.FILES_KEY);
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("执行时长：%d 毫秒.", (etime - statTime));
        System.out.println("结束多线程,模型训练结束");
        return result;
    }


    /***
     * 模型评估接口
     * @param url
     * @return
     * @throws IOException
     */
    //@Async
    @GetMapping("/analyze/{url}")
    public Result beginAnalyze(@PathVariable String url) throws IOException {
        System.out.println("***********URL********");
        System.out.println(url);
        System.out.println("开始多线程,这是模型评估********");
        long statTime = System.currentTimeMillis();

        Result result = trainService.analyze(url);

        System.out.println("评估成功");

        flushRedis(Constants.FILES_KEY);
        // 结束时间
        long etime = System.currentTimeMillis();
        // 计算执行时间
        System.out.printf("执行时长：%d 毫秒.", (etime - statTime));
        System.out.println("结束多线程,评估报告生成结束");
        return result;
    }

    // 删除缓存
    private void flushRedis(String key) {
        stringRedisTemplate.delete(key);
    }


    /**
     * 文件下载接口   http://localhost:9090/file/{fileUUID}
     *
     * @param pythonUrl
     * @param response
     * @throws IOException
     */
    @GetMapping("/**/{pythonUrl}")
    public void download(@PathVariable String pythonUrl, HttpServletResponse response) throws IOException {
       trainService.downloadTrainFile(pythonUrl, response);
    }

    @GetMapping("/download")
    public void downloadreport(HttpServletResponse response) throws IOException {
        trainService.downloadAnalyzeFile(response);
    }



    //    @CachePut(value = "files", key = "'frontAll'")
    @PostMapping("/update")
    public Result update(@RequestBody Files files) {
        trainService.updateById(files);
        flushRedis(Constants.FILES_KEY);
        return Result.success();
    }

}
