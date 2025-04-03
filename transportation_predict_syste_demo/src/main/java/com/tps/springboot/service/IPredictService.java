package com.tps.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tps.springboot.common.Result;
import com.tps.springboot.entity.TestFiles;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public interface IPredictService {

    boolean uploadPredictFile(MultipartFile file) throws Exception;


    Result beginPredict(String url) throws IOException;

    IPage<TestFiles> findPage(Integer pageNum,
                              Integer pageSize,
                              String name);

    boolean deletePredictData(Integer id);

    boolean deleteBatch( List<Integer> ids);

    TestFiles getById( Integer id);

    /**
     * 当年预测的次数
     * @return
     */
    Integer predictTotle();

    /**
     *
     * @param id
     * @return
     */
    ArrayList<Long> getMalfunctionCount(Integer id);

    /**
     *
     * @param id
     * @return
     */
    Long getCountByFileId(Integer id);

    void downloadResultFile( String jsonUrl, HttpServletResponse response) throws IOException;
}
