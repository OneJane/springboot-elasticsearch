package com.onejane.elasticsearch.repository;

import com.onejane.elasticsearch.bean.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: springboot-elasticsearch
 * @description: 用户基础数据处理层
 * @author: OneJane
 * @create: 2020-01-12 13:17
 **/
@Repository
public interface UserRepository extends ElasticsearchRepository<User, Integer> {

    /**
     根据名称模糊查询
     */
    List<User> findUserByNameLike(String name);

    /**
     * 自定义方法，根据名称精确查询
     * @param name
     * @return
     */
    List<User> findUserByName(String name);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User findUserById(Integer id);

    /**
     * 根据用户名称搜索，并按照时间倒序
     * @param name
     * @param pageable
     * @return
     */
    Page<User> findUserByNameOrderByCreateTimeDesc(String name, Pageable pageable);
}