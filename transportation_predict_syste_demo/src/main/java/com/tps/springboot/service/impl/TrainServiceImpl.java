package com.tps.springboot.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Files;
import com.tps.springboot.mapper.FileMapper;
import com.tps.springboot.service.ITrainService;
import com.tps.springboot.utils.FileUtils;
import com.tps.springboot.utils.PythonUtils;
import com.tps.springboot.utils.SystemConfigUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Service
public class TrainServiceImpl  implements ITrainService {


    @Resource
    private SystemConfigUtils config;

    @Resource
    private FileMapper fileMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Logger logger = LoggerFactory.getLogger(TrainServiceImpl.class);

    /**
     * 保存上传文件（比如上传训练集）
     *
     * step1:读取基本信息
     * step2：写文件
     * step3: 将数据写入trainfile文件
     * @param file
     * @return
     */
    @Override
    public boolean uploadTrainFile(MultipartFile file) throws Exception {

        logger.info("#######################开始将训练集文件写入#################");

        String trianFileName = FileUtils.writeFile( config.getTrainFilePath(),file);

        Files saveFileRecord = buildTrainFile(file, trianFileName);
        fileMapper.insert(saveFileRecord);

        return true;
    }

    /**
     * 封装入库的
     * @param file
     * @param trianFileName
     * @return
     */
    private Files buildTrainFile(MultipartFile file,  String trianFileName) throws IOException {
        long size = file.getSize();
        // 存储数据库
        Files saveFile = new Files();
        saveFile.setName(file.getOriginalFilename());
        String type = FileUtil.extName(file.getOriginalFilename());
        saveFile.setType(type);
        // 单位 kb
        saveFile.setSize(size / 1024);
        //1.9090应该存放到配置文件中 2.url返回给前端用下载的训练用的url，可以将名字改成modelDownLoadUrl
        String serverIp = config.getServerIp();
        String url = "http://" + serverIp + ":9090/python/" + trianFileName;
        saveFile.setUrl(url);

        // 获取文件的md5，用于校验文件数据是否被删除或改动
       // String md5 = SecureUtil.md5(file.getInputStream());
        saveFile.setMd5(null);
        //获取当前用户的user_id
        String userId = stringRedisTemplate.opsForValue().get("userId");
        saveFile.setUserid(Integer.parseInt(userId));
        return saveFile;
    }

    /**
     * 训练
     * @param url  训练模型的名字
     * @return
     */
    @Override
    public Result train(String url, String type) throws IOException {
        // 查询是否已经完成训练
        LambdaQueryWrapper<Files> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Files::getUrl, "http://localhost:9090/python/" + url);
        Files files = fileMapper.selectOne(queryWrapper);

        //pythonUrl存在而且当前选择的模型类别符合
        if (!ObjectUtils.isEmpty(files.getPythonurl()) && type.equals(files.getModeltype())) {
            return Result.error("505", "训练已完成，请下载");
        }

        //读取文件开始训练
        String trainModelFileName = url.replace("csv", "h5");
        if(type != null && type.equals("modelDT"))
            trainModelFileName = url.replace("csv", "pkl");
        if(type != null && type.equals("modelRF"))
            trainModelFileName = url.replace("csv", "pkl");

        try {
            String[] arguments = new String[] {config.getPythonInterpreter(),
                    config.getPythonTrianCode(),
                    config.getTrainFilePath()+url,
                    config.getTrainFilePath()+trainModelFileName,type};

            int i = PythonUtils.trainByPython(arguments);

            if (i == 1) {
                return Result.error("507", "训练失败");
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        files.setPythonurl(trainModelFileName);
        files.setModeltype(type);
        fileMapper.updateById(files);

        return Result.success();
    }

    @Override
    public Result analyze(String url) throws IOException{

        // 查询是否已经完成生成报告
        LambdaQueryWrapper<Files> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Files::getUrl, "http://localhost:9090/python/" + url);
        Files files = fileMapper.selectOne(queryWrapper);

        //pythonUrl存在而且当前选择的模型类别符合
        if (!ObjectUtils.isEmpty(files.getReport())) {
            return Result.error("505", "报告已完成，请下载");
        }

        try {
            String[] arguments = new String[] {config.getPythonInterpreter(),
                    config.getPythonAnalyzeCode(),
                    config.getTrainFilePath()+url,
                    config.getModelAnalysisPath()};

            int i = PythonUtils.trainByPython(arguments);

            if (i == 1) {
                return Result.error("507", "生成失败");
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        files.setReport("done");
        fileMapper.updateById(files);

        return Result.success();
    }

    @Override
    public void updateById(Files files) {
        fileMapper.updateById(files);
    }

    @Override
    public void downloadTrainFile( String pythonUrl, HttpServletResponse response) throws IOException {
        FileUtils.downloadResultFile(pythonUrl,config.getTrainFilePath(),response);
    }

    @Override
    public void downloadAnalyzeFile(HttpServletResponse response) throws IOException {
        FileUtils.downloadAnalyzeFile(response);
    }

}
