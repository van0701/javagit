package com.huyabin.spring.fromework.beans;

/**
 * Created by Tom on 2018/4/21.
 */
//用做事件监听的
public class BeanPostProcessor {

    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }
    public Object postProcessAfterInitialization(Object bean, String beanName){
        return bean;
    }

}
