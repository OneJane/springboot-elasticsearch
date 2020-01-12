package com.onejane.elasticsearch.bean;/**
 * Created by Administrator on 2019/10/13 0013.
 */

/**
 * 该类是对文档数据的封装
 * @author Administrator
 * @date 2019/10/13 0013 23:32
 * @description
 */
public final class EsEntity<T> {
    //文档id
    private String id;
    //一条文档
    private T data;

    public EsEntity() {
    }

    public EsEntity(String id, T data) {
        this.data = data;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
