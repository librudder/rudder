package com.github.rudder.spring;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SomeEntityRepository extends JpaRepository<SomeEntity, Long> {

    SomeEntity findSomeEntityById(Long aLong);

}
