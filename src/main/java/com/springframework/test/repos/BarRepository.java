package com.springframework.test.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springframework.test.model.Bar;

public interface BarRepository extends JpaRepository<Bar, String>{

}
