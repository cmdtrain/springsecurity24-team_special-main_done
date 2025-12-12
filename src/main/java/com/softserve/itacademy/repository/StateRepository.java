package com.softserve.itacademy.repository;

import com.softserve.itacademy.model.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

    State findByName(String name);

//    @Query(value = "select * from states order by id", nativeQuery = true)
//    @Query("from State order by id")
    List<State> findAllByOrderById();

//    List<State> findAllByOrderByIdAsc();

}
