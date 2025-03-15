package com.bright.timetable.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "schedule")
public class Schedule {

    @Id
    private String id;
    private String day;
    private String startTime;
    private String endTime;
    private String classRoom;
    private String year;
    private String program;
    private String courseCode;
    private String lecturer;

    // Constructors
    public Schedule() {}

    public Schedule(String day, String startTime, String endTime, String classRoom,
                    String year, String program, String courseCode, String lecturer) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classRoom = classRoom;
        this.year = year;
        this.program = program;
        this.courseCode = courseCode;
        this.lecturer = lecturer;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public String getClassRoom() { return classRoom; }
    public void setClassRoom(String classRoom) { this.classRoom = classRoom; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getLecturer() { return lecturer; }
    public void setLecturer(String lecturer) { this.lecturer = lecturer; }

}





