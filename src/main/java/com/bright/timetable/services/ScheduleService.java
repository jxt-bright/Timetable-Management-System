package com.bright.timetable.services;

import com.bright.timetable.models.Schedule;
import com.bright.timetable.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public Schedule createSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> getScheduleById(String id) {
        return scheduleRepository.findById(id);
    }

    public List<Schedule> getSchedulesByProgramAndYear(String program, String year) {
        return scheduleRepository.findByProgramAndYear(program, year);
    }

    public Schedule updateSchedule(String id, Schedule scheduleDetails) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));

        schedule.setDay(scheduleDetails.getDay());
        schedule.setStartTime(scheduleDetails.getStartTime());
        schedule.setEndTime(scheduleDetails.getEndTime());
        schedule.setClassRoom(scheduleDetails.getClassRoom());
        schedule.setYear(scheduleDetails.getYear());
        schedule.setProgram(scheduleDetails.getProgram());
        schedule.setCourseCode(scheduleDetails.getCourseCode());
        schedule.setLecturer(scheduleDetails.getLecturer());

        return scheduleRepository.save(schedule);
    }

    public void deleteSchedule(String id) {
        scheduleRepository.deleteById(id);
    }
}
