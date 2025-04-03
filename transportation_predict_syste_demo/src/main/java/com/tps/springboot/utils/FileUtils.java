package com.tps.springboot.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;


public final  class FileUtils {

    @Value("${files.modelAnalysis.path}")
    private static String filePath;

    static {
        filePath = "D:/newSoftcup/soft-cup/python/model_analysis.docx";
    }


    public static String  writeFile(String path, MultipartFile file) throws Exception {

        String type = FileUtil.extName(file.getOriginalFilename());
        // 定义一个文件唯一的标识码
        String uuid = "";
        synchronized (uuid) {
            uuid = (UUID.randomUUID().toString()).replace("-", "");
        }

        String fileName = uuid + StrUtil.DOT + type;
        File uploadFile = new File(path + fileName);
        // 判断配置的文件目录是否存在，若不存在则创建一个新的文件目录
        File parentFile = uploadFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        try {
            // 上传文件到磁盘
            file.transferTo(uploadFile);
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("写文件失败");
        }
        return fileName;
    }

    public static void downloadResultFile(String fileName,String srcFilePath, HttpServletResponse response) throws IOException {
        System.out.println(fileName);
        // 根据文件的唯一标识码获取文件
        File uploadFile = new File(srcFilePath+fileName);
        System.out.println(uploadFile);
        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentType("application/octet-stream");
        // 读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        System.out.println("下载成功");
        os.flush();
        os.close();
    }

    public static void downloadAnalyzeFile(HttpServletResponse response) throws IOException {

        File file = new File(filePath);

        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=model_analysis.docx");
        response.setContentType("application/octet-stream");
        // 读取文件的字节流
        os.write(FileUtil.readBytes(file));
        System.out.println("下载成功");
        os.flush();
        os.close();
    }
}
