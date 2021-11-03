package com.nhn.webflux2021.reactive.r2dbc;


import org.springframework.data.annotation.Id;

public class Member {
    @Id
    Integer id;
    String name;
    String phone;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
