package com.onejane.elasticsearch.service;

import com.onejane.elasticsearch.bean.User;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @program: springboot-elasticsearch
 * @description: 用户服务层
 * @author: OneJane
 * @create: 2020-01-12 13:20
 **/
public interface UserService {
    long count();

    User save(User user);

    void delete(User user);

    Iterable<User> getAll();

    List<User> getByName(String name);

    Page<User> pageQuery(Integer pageNum, Integer pageSize, String q);

    Page<User> pageQueryWithHighLight(Integer pageNum, Integer pageSize, String q);
}
