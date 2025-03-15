package com.bright.timetable.controllers;

import com.bright.timetable.DrawTable;
import com.bright.timetable.models.Schedule;
import com.bright.timetable.services.ScheduleService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class StudentsController {

    private final ScheduleService scheduleService;
    private final List<String> dayOrder = List.of(
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    );

    @Autowired
    public StudentsController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/student/course")
    public ModelAndView showLoginPage() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("studentHomePage");
        return mav;
    }

    @GetMapping("/student/timetable")
    public ModelAndView viewTimetable(
            @RequestParam String program,
            @RequestParam String year) {

        ModelAndView mav = new ModelAndView("studentsSchedules");
        List<String> dayOrder = Arrays.asList(
                "Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday"
        );

        try {
            // Get and sort schedules
            List<Schedule> schedules = scheduleService.getSchedulesByProgramAndYear(program, year);
            if (schedules == null) {
                schedules = new ArrayList<>();
            }

            // Define formatter for 12-hour format with AM/PM
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");

            // Format and sort schedules
            List<Schedule> sortedSchedules = schedules.stream()
                    .filter(s -> s.getDay() != null && s.getStartTime() != null && s.getEndTime() != null) // Avoid null values
                    .peek(s -> {
                        // Convert startTime and endTime to 12-hour format
                        s.setStartTime(formatTime(s.getStartTime(), inputFormatter, outputFormatter));
                        s.setEndTime(formatTime(s.getEndTime(), inputFormatter, outputFormatter));
                    })
                    .sorted(Comparator.comparing((Schedule s) -> {
                        String day = s.getDay().toLowerCase();
                        return dayOrder.indexOf(day.substring(0, 1).toUpperCase() + day.substring(1));
                    }).thenComparing(Schedule::getStartTime))
                    .toList();

            System.out.println("These are the schedules: " + sortedSchedules);

            // Pass data to the template
            mav.addObject("program", program);
            mav.addObject("year", year); // Pass as a string, avoid unnecessary parsing
            mav.addObject("schedules", sortedSchedules);
            mav.addObject("dayOrder", dayOrder);
            mav.addObject("emptySchedule", sortedSchedules.isEmpty());

        } catch (NumberFormatException e) {
            mav.addObject("error", "Invalid year format. Please use numbers 1-4");
        }
        return mav;
    }

    // Helper Method to Convert Time
    private String formatTime(String time, DateTimeFormatter inputFormatter, DateTimeFormatter outputFormatter) {
        try {
            return LocalTime.parse(time, inputFormatter).format(outputFormatter);
        } catch (Exception e) {
            e.printStackTrace();
            return time; // Return the original time if parsing fails
        }
    }

    @GetMapping("/student/timetable/download")
    public ResponseEntity<InputStreamResource> downloadTimetablePdf(
            @RequestParam String program,
            @RequestParam String year) throws IOException {

        List<String> dayOrder = Arrays.asList(
                "Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday");

        List<Schedule> schedules = scheduleService.getSchedulesByProgramAndYear(program, year);

        // Sort schedules
        schedules.sort(Comparator.comparingInt((Schedule s) -> dayOrder.indexOf(s.getDay()))
                .thenComparing(Schedule::getStartTime));

        // Define formatters for 12-hour format
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        // Create PDF document
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Set up fonts and layout
            PDFont fontBold = PDType1Font.HELVETICA_BOLD;
            PDFont fontRegular = PDType1Font.HELVETICA;
            float fontSize = 12;
            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;

            // Draw title
            contentStream.beginText();
            contentStream.setFont(fontBold, 16);
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText("Timetable for " + program + " - Year " + year);
            contentStream.endText();
            yStart -= 30;

            // Prepare table data
            List<List<String>> data = new ArrayList<>();
            // Add table headers
            data.add(Arrays.asList("Day", "Time", "Classroom", "Course Code", "Lecturer"));

            // Add table rows
            for (Schedule schedule : schedules) {
                String formattedTime = formatTimeRange(
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        inputFormatter,
                        outputFormatter);
                data.add(Arrays.asList(
                        schedule.getDay(),
                        formattedTime,
                        schedule.getClassRoom(),
                        schedule.getCourseCode(),
                        schedule.getLecturer()
                ));
            }

            // Draw table
            DrawTable.drawTable(document, contentStream, yStart, margin, tableWidth, data, fontBold, fontRegular, fontSize);
        }

        // Save to byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();

        // Prepare response
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=timetable.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
    }

    // Helper Method to Format Time Range
    private String formatTimeRange(String startTime, String endTime, DateTimeFormatter inputFormatter, DateTimeFormatter outputFormatter) {
        try {
            String formattedStart = LocalTime.parse(startTime, inputFormatter).format(outputFormatter);
            String formattedEnd = LocalTime.parse(endTime, inputFormatter).format(outputFormatter);
            return formattedStart + " - " + formattedEnd;
        } catch (Exception e) {
            e.printStackTrace();
            return startTime + " - " + endTime; // Fallback to original values
        }
    }
}