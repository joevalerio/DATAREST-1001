package com.springframework.test.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springframework.test.model.Foo;

public interface FooRepository extends JpaRepository<Foo, String>{

}
