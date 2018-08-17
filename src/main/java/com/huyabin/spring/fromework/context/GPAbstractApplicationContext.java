package com.huyabin.spring.fromework.context;

public abstract class GPAbstractApplicationContext {
    //提供给子类重写
    protected void onRefresh(){
        // For subclasses: do nothing by default.
    }

    protected abstract void refreshBeanFactory();

}
