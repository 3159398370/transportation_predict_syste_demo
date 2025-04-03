package com.tps.springboot.service;

import com.tps.springboot.common.Result;
import com.tps.springboot.entity.Files;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public interface ITrainService {

    /**
     * 上传的文件写入本地文件夹
     * @param file
     * @return
     * @throws Exception
     */
    boolean uploadTrainFile(MultipartFile file) throws Exception;


    /**
     * 调用
     * @param url
     * @return
     * @throws IOException
     */
    Result train(String url, String type) throws IOException;

    Result analyze(String url) throws IOException;
    /**
     * 修改train文件
     * @param files
     */
    void updateById(Files files);

    void downloadTrainFile(String pythonUrl, HttpServletResponse response) throws IOException;

    void downloadAnalyzeFile(HttpServletResponse response) throws IOException;
}
