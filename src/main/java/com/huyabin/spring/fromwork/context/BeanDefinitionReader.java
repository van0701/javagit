package com.huyabin.spring.fromwork.context;

import com.huyabin.spring.fromework.beans.BeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BeanDefinitionReader {
    private Properties config=new Properties();

    private List<String> registyBeanClasses=new ArrayList<>();

    //在配置文件中，用来获取自动扫瞄的包的key

    private final String SCAN_PACKAGE="scanPackage";



    public BeanDefinitionReader(String ... locations) {
        //在Spring中是通过Reader去查找和定位对不对....这里参数是数组spring是循环取值的
      //  InputStream in=this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath",""));
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:",""));

        try {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null!=in){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
      doScanner(config.getProperty(SCAN_PACKAGE));

    }

    public List<String> loadBeanDefinitions() {

        return this.registyBeanClasses;
    }

    //每注册一个className，就返回一个BeanDefinition，我自己包装
    //只是为了对配置信息进行一个包装

    public BeanDefinition registerBean(String className){
        if (this.registyBeanClasses.contains(className)){
            BeanDefinition beanDefinition=new BeanDefinition();

            beanDefinition.setBeanClassName(className);

            beanDefinition.setFactoryBeanName(lowerFirstCase(className.substring(className.lastIndexOf(".")+1)));

            return beanDefinition;


        }
        return null;
    }



    //递归扫描所有的相关联的class，并且保存到一个List中
    private void doScanner(String packageName){

        URL url=this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.","/"));

        File classDir=new File(url.getFile());
        for (File file:classDir.listFiles()){
            if (file.isDirectory()){
                doScanner(packageName+"."+file.getName());
            }else{

                registyBeanClasses.add(packageName+"."+file.getName().replace(".class",""));

            }

        }


    }


    public Properties getConfig(){
        return this.config;
    }


    private String lowerFirstCase(String str){
        char [] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
