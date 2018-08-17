package com.huyabin.spring.fromework.context;

import com.huyabin.spring.fromework.annotation.GPAutowired;
import com.huyabin.spring.fromework.annotation.GPController;
import com.huyabin.spring.fromework.annotation.GPService;
import com.huyabin.spring.fromework.beans.BeanDefinition;
import com.huyabin.spring.fromework.beans.BeanPostProcessor;
import com.huyabin.spring.fromework.beans.BeanWrapper;
import com.huyabin.spring.fromework.core.BeanFactory;
import com.huyabin.spring.fromwork.context.BeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class GPApplicationContext implements BeanFactory{
    private String[] configLocations;
    private BeanDefinitionReader reader;

    //beanDefinitionMap用来保存配置信息
    private Map<String,BeanDefinition> beanDefinitionMap=new ConcurrentHashMap<String,BeanDefinition>();

  //用来保证注册式单例的容器
    private  Map<String,Object> beanCacheMap=new HashMap<>();

    //用来存储所有的被代理过的对象
    private Map<String,BeanWrapper> beanWrapperMap=new ConcurrentHashMap<String,BeanWrapper>();

    //依赖注入，从这里开始，通过读取BeanDefinition中的信息
    //然后，通过反射机制创建一个实例并返回
    //Spring做法是，不会把最原始的对象放出去，会用一个BeanWrapper来进行一次包装
    //装饰器模式：
    //1、保留原来的OOP关系
    //2、我需要对它进行扩展，增强（为了以后AOP打基础）
    @Override
    public Object getBean(String beanname) {
        BeanDefinition beanDefinition=this.beanDefinitionMap.get(beanname);

        String className=beanDefinition.getBeanClassName();
        try {
            //生成通知事件
            BeanPostProcessor beanPostProcessor=new BeanPostProcessor();

            Object instance=instantionBean(beanDefinition);

            if (null==instance){return null;}

            //在实例初始化以前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance,beanname);

            BeanWrapper beanWrapper=new BeanWrapper(instance);

            beanWrapper.setPostProcessor(beanPostProcessor);

            this.beanWrapperMap.put(beanname,beanWrapper);

            //在实例初始化以后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance,beanname);

            return this.beanWrapperMap.get(beanname).getWrappedInstance();


        }catch (Exception e){
            e.printStackTrace();
        }



        return null;
    }

    private Object instantionBean(BeanDefinition beanDefinition) {
        Object instance=null;
        String className=beanDefinition.getBeanClassName();
        //因为根据Class才能确定一个类是否有实例
        try {
            if (this.beanCacheMap.containsKey(className)){
            instance=this.beanCacheMap.get(className);

        }else {
                Class<?> clazz=Class.forName(className);
                instance=clazz.newInstance();
                this.beanCacheMap.put(className,instance);

        }
        return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public GPApplicationContext(String ... configLocations) {
        this.configLocations=configLocations;
        System.out.println(this.configLocations);
        refresh();

    }


  public void  refresh(){
        //定位
      this.reader=new BeanDefinitionReader(configLocations);
        //加载
      List<String> beanDefinitions=reader.loadBeanDefinitions();
        //注册
      doRegisty(beanDefinitions);

      //依赖注入（lazy-init = false），要是执行依赖注入
      //在这里自动调用getBean方法
      doAutowrited();

    }

    private void doAutowrited() {
        for (Map.Entry<String ,BeanDefinition> beanDefinitionEntry:this.beanDefinitionMap.entrySet()){
            String beanName=beanDefinitionEntry.getKey();
            
            if (!beanDefinitionEntry.getValue().isLazyInit()){
                
                getBean(beanName);
            }
            
        }
       
        for (Map.Entry<String,BeanWrapper> beanWrapperEntry:this.beanWrapperMap.entrySet()){

            populateBean(beanWrapperEntry.getKey(),beanWrapperEntry.getValue().getWrappedInstance());

        }
        

       }

    private void populateBean(String beanName, Object instance) {
        Class clazz=instance.getClass();

        //判断类型
        if (!(clazz.isAnnotationPresent(GPController.class)||clazz.isAnnotationPresent(GPService.class))){

            return;
        }
        Field[] fields=clazz.getDeclaredFields();

        for (Field field:fields){
            if (!field.isAnnotationPresent(GPAutowired.class)){continue;}
            GPAutowired autowired=field.getAnnotation(GPAutowired.class);

            String autowiredBeanName=autowired.value().trim();

            if ("".equals(autowiredBeanName)){

                autowiredBeanName=field.getType().getName();


            }
            field.setAccessible(true);

            try {
                field.set(instance,this.beanWrapperMap.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }


    }


    private void doRegisty(List<String> beanDefinitions) {
        //bean有三种情况
        //1、默认是类名首字母小写
        //2、自定义名字
        // 3、接口注入
        for (String className:beanDefinitions){
            try {
                Class<?> beanClass=Class.forName(className);
                
                //如果是一个接口，是不能实例化的
                //用它的实现类来实例化
                if (beanClass.isInterface()){continue;}
                
                BeanDefinition beanDefinition=reader.registerBean(className);
                
                if (beanDefinition!=null){
                    //将对象转化为BeanDefinition对象添加到beanDefinitionMap中
                    this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);

                }
                Class<?>[] interfaces=beanClass.getInterfaces();
                
                for(Class<?> i:interfaces){
                    //如果是多个实现类，只能覆盖
                    //为什么？因为Spring没那么智能，就是这么傻
                    //这个时候，可以自定义名字 
                    
                    this.beanDefinitionMap.put(i.getName(),beanDefinition);
                }

                //到这里为止，容器初始化完毕
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }


    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig(){
        return this.reader.getConfig();
    }
}
