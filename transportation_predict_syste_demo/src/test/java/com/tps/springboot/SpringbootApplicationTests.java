package com.tps.springboot;

import com.tps.springboot.common.Constants;
import com.tps.springboot.common.Result;
import com.tps.springboot.controller.TrainController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Assertions;



@SpringBootTest
class SpringbootApplicationTests {

//    @Test
//    void contextLoads() {
//
//
//    }

    @Autowired
    private TrainController trainController;

    @Test
    void testBeginTrain() throws Exception {
//        String url = "aa319073582a4863b6ff5e6544768e54.csv";
        String url = "78b8fd001e0d4122bd6deb3bfcc6765d.csv";
//        String type = "model";

        Result result = trainController.beginAnalyze(url);

        // 检查返回结果是否RF成功
        Assertions.assertEquals(Constants.CODE_200, result.getCode());

        // 检查返回结果是否包含数据
        assertNotNull(result.getData());
    }

//    @Autowired
//    private PredictController predictController;
//
//    @Test
//    void testbeginPredict() throws Exception {
////        String url = "aa319073582a4863b6ff5e6544768e54.csv";
//        String url = "aa319073582a4863b6ff5e6544768e54.csv";
////http://localhost:9090/DataTest/0de3039a2f244734bf341ff1b122b610.csv
//        Result result = predictController.beginPredict(url);
//
//        // 检查返回结果是否RF成功
//        assertEquals(Constants.CODE_200, result.getCode());
//
//        // 检查返回结果是否包含数据
//        assertNotNull(result.getData());
//    }

//    @Autowired
//    private ITrainService trainService;
//
//    @Test
//    void testTrain() {
////        String url = "aa319073582a4863b6ff5e6544768e54.csv";
////        String url = "7e2f60830e5648408c36bbe6f14330cc.csv";
//        String url = "78b8fd001e0d4122bd6deb3bfcc6765d.csv";
////        String type = "modelDT";
//
//        try {
////            Result result = trainService.train(url,type);
//            Result result = trainService.analyze(url);
//
//            // 检查返回结果是否成功
//            Assertions.assertEquals(Constants.CODE_200, result.getCode());
//        } catch (IOException e) {
//            Assertions.fail("IOException occurred: " + e.getMessage());
//        }
//    }

//    @Autowired
//    private IPredictService predictService;
//
//    @Test
//    void testPredict() {
////        String url = "aa319073582a4863b6ff5e6544768e54.csv";
//        String url = "0de3039a2f244734bf341ff1b122b610.csv";
//
//        try {
//            Result result = predictService.beginPredict(url);
//
//            // 检查返回结果是否成功
//            Assertions.assertEquals(Constants.CODE_200, result.getCode());
//        } catch (IOException e) {
//            Assertions.fail("IOException occurred: " + e.getMessage());
//        }
//    }

//    @Autowired
//    private IUserService userService;
//
//    @Test
//    void testLogin() {
//        UserDTO userDTO = new UserDTO();
//        userDTO.setUsername("admin");
//        userDTO.setPassword("admin");
//
//        UserDTO result = userService.login(userDTO);
//
//        // 使用断言验证返回结果是否正确
//        assertNotNull(result);
//        assertEquals("admin", result.getUsername());
//    }


}
