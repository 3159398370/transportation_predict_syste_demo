package com.tps.springboot.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Component
@Data
public class SystemConfigUtils {

    @Value("${files.trainFile.path}")
    private String trainFilePath;

    @Value("${files.predictFile.path}")
    private String predictFilePath;

    @Value("${server.ip}")
    private String serverIp;

    @Value("${files.pythonInterpreter.path}")
    private String pythonInterpreter;

    @Value("${files.pythonTrianCode.path}")
    private String pythonTrianCode;

    @Value("${files.pythonPredictCode.path}")
    private String pythonPredictCode;


    @Value("${files.predictModel.path}")
    private String predictModelPath;

    @Value("${files.modelAnalysis.path}")
    private String modelAnalysisPath;

    @Value("${files.pythonAnalyzeCode.path}")
    private String pythonAnalyzeCode;

}
