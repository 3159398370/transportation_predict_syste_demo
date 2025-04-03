package com.tps.springboot.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public final class PythonUtils {


    /**
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public static int trainByPython(String[] arguments) throws IOException,InterruptedException{

        //String[] arguments = new String[] {pythonInterpreter,CodePath,saveFilePath,fileName};


        Process process = Runtime.getRuntime().exec(arguments);
        //获取 Python 脚本的标准输出和标准错误输出
        InputStream stdout = process.getInputStream();
        InputStream stderr = process.getErrorStream();

        //创建读取器并将其分别注册到标准输出和标准错误输出流上
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
        String line = null;
        System.out.println("标准输出:");
        while ((line = stdoutReader.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("标准错误输出:");
        while ((line = stderrReader.readLine()) != null) {
            System.err.println(line);
        }


//        java代码中的process.waitFor()返回值为0表示我们调用python脚本成功，
//         返回值为1表示调用python脚本失败，这和我们通常意义上见到的0与1定义正好相反
        int re = process.waitFor();
        return re;
    }

}
