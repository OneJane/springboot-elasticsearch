package com.onejane.elasticsearch.bean;
 
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
 
@Entity
@Data
@NoArgsConstructor
@Table(name = "student")
public class Student {
    //自增ID
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
 
    @Column(name = "name")
    private String name;
 
    @Column(name="age")
    private Integer age;

}