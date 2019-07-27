package com.record.video.config.able;

import java.lang.reflect.ParameterizedType;

/**
 * detail: 通用结果回调类
 * Created by Ttt 
 */
public abstract class ResultCallback<T> {

    Class<T> clas;

    /**
     * 初始化构造函数,则进行获取泛型Class类
     */
    protected ResultCallback() {
        try {
            clas = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception e){
        }
    }

    /**
     * 获取泛型类Class类型
     * @return
     */
    public final Class<T> getTClass() {
        return clas;
    }

    // ========== 请求回调 ==========

    /**
     * 请求结果回调
     * @param t
     */
    public abstract void onResult(T t);

    /**
     * 请求异常回调
     * @param e
     */
    public void onError(Exception e){
    }

    /**
     * 请求失败回调
     * @param errorCode
     */
    public void onFailure(int errorCode){
    }
}
