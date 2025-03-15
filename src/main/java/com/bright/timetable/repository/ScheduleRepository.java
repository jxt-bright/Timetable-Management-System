package com.bright.timetable.repository;

import com.bright.timetable.models.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    List<Schedule> findByProgramAndYear(String program, String year);
}


