package com.nhn.webflux2021.reactive.r2dbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
public record Member(@Id Integer id, String name, String phone) {
}
