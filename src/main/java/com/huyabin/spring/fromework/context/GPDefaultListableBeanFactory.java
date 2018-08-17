package com.huyabin.spring.fromework.context;

import com.huyabin.spring.fromework.beans.BeanDefinition;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GPDefaultListableBeanFactory extends GPAbstractApplicationContext {
    //beanDefinitionMap用来保存配置信息
    protected Map<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String,BeanDefinition>();
    @Override
    protected void refreshBeanFactory() {

    }

    protected void onRefresh(){

    }

}
