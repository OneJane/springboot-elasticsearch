package com.onejane.elasticsearch.bean;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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
    @Field(type = FieldType.Keyword) // 模糊查询
    private String name;
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String address;
    private Integer sex;

    /**
     * 通过反射获取注解相关属性
     * @param clazz
     * @return
     */
    public static Map<String, String> getTableName(Class<?> clazz) {
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
