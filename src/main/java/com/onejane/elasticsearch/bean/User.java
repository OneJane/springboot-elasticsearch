package com.onejane.elasticsearch.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: springboot-elasticsearch
 * @description: 用户信息
 * @author: OneJane
 * @create: 2020-01-12 12:10
 **/
@Data
@Document(indexName = "info_repository", type = "user")
public class User {
    @Id
    private Integer id;
    @Field(type = FieldType.Keyword,store = true) // 模糊查询
    private String name;


    // 是否索引, 就是看这个字段是否能被搜索, 比如: 如果对整篇文章建立了索引,那么从文章中任意抽出一段来,都可以搜索出这个文章
    // 是否分词, 就是表示搜索的时候,是整体匹配还是单词匹配  比如: 如果不分词的话,搜索时,一个词不一样,都搜索不出来结果
    // 是否存储, 就是,是否在页面上展示 , 但是在es中默认字段值已经存储在_source 字段里， 也是能检索出原始字段的
    @Field(type = FieldType.Text, store = true, analyzer = "ik_smart", searchAnalyzer = "ik_max_word",index = true)
    private String address;
    private Integer sex;
    @Field(type = FieldType.Date, store = true, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss:SSS||yyyy-MM-dd||epoch_millis||date_optional_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss:SSS", timezone = "GMT+8")
    private Date createTime;
    @Field(type = FieldType.Text, store = true, analyzer = "ik_smart", searchAnalyzer = "ik_max_word")
    private String comment;
    @Field(type = FieldType.Nested, store = true)
    private Map<String,String> location;

//    @Field(type = FieldType.Nested, store = true)
//    private List<User> users;
//
//    @Field(type = FieldType.Nested, store = true)
//    private User user;

    /**
     * 通过反射获取注解相关属性
     * @param clazz
     * @return
     */
    public static Map<String, String> getFieldName(Class<?> clazz) {
        Map<String, String> map = new ConcurrentHashMap<>();
        Document annotation = clazz.getAnnotation(Document.class);
        java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
        String name = annotation.indexName();
        String type = annotation.type();
        String className = clazz.getSimpleName();
        for (java.lang.reflect.Field field : fields) {
            if (field.isAnnotationPresent(Field.class)) {
                /**
                 * 获取字段名
                 */
                Field declaredAnnotation = field.getDeclaredAnnotation(Field.class);
                String analyzer = declaredAnnotation.analyzer();
                map.put("fieldNames", field.getName());
                map.put("analyzer", analyzer);
                break;
            }
        }
        map.put("name", name);
        map.put("type", type);
        return map;
    }

}
