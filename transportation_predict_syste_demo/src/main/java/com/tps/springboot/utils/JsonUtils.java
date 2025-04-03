package com.tps.springboot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;



public final class JsonUtils {



    public static Object readJsonFile(String filePath) throws IOException {

        File file = new File(filePath);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(file, Object.class);

        //遍历
//        myObject.forEach((key, value) -> {
//            System.out.println(key);
//            System.out.println(value);
//        });
    }
}
