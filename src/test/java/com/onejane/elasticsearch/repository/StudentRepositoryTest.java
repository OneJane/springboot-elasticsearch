package com.onejane.elasticsearch.repository;

import com.onejane.elasticsearch.bean.Student;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class StudentRepositoryTest {

    @Autowired
    StudentRepository studentRepository;


    @Test
    public void add(){
        Student user = new Student();
        user.setName("onejane"+ RandomStringUtils.randomAlphanumeric(5));
        user.setAge(6);
        studentRepository.save(user);
    }

}