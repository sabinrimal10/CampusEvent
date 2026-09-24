package com.edap.campusevents.repository;

import com.edap.campusevents.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCategoryIgnoreCaseOrderByStartTimeAsc(String category);

    List<Event> findAllByOrderByStartTimeAsc();

    @Query("SELECT DISTINCT e.category FROM Event e ORDER BY e.category")
    List<String> findDistinctCategories();
}
