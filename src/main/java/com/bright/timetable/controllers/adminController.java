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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/schedules")
public class adminController {

    private final ScheduleService scheduleService;

    @Autowired
    public adminController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/savePage")
    public ModelAndView showLogin() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("adminHomePage");
        return mav;
    }

    @GetMapping
    public ModelAndView getAllSchedules() {
        ModelAndView mav = new ModelAndView("schedules");

        List<String> dayOrder = Arrays.asList(
                "Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday"
        );

        try {
            // Get and sort schedules
            List<Schedule> schedules = scheduleService.getAllSchedules();
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

            // Pass data to the template
            mav.addObject("schedules", sortedSchedules);
            mav.addObject("dayOrder", dayOrder);
            mav.addObject("emptySchedule", sortedSchedules.isEmpty());
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
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

    // Process form submission
    @PostMapping("/save")
    public ModelAndView saveSchedule(@ModelAttribute Schedule schedule, RedirectAttributes redirectAttributes) {
        String room = schedule.getClassRoom();

        ModelAndView mav = new ModelAndView();

        scheduleService.createSchedule(schedule);
        redirectAttributes.addFlashAttribute("success", "Data saved!");
        return new ModelAndView("redirect:/admin/schedules");
    }

    // Delete schedule
    @GetMapping("/delete/{id}")
    public ModelAndView deleteSchedule(@PathVariable String id, RedirectAttributes redirectAttributes) {
        scheduleService.deleteSchedule(id);
        redirectAttributes.addFlashAttribute("successMessage", "Schedule deleted successfully!");
        return new ModelAndView("redirect:/admin/schedules");
    }
    // In adminController.java update showEditForm method
    @GetMapping("/edit/{id}")
    public ModelAndView showEditForm(@PathVariable String id) {
        ModelAndView mav = new ModelAndView("edit");
        Schedule schedule = scheduleService.getScheduleById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule with ID " + id + " not found"));
        mav.addObject("schedule", schedule);
        return mav;
        // ... rest of the method
    }

    // Process update
    @PostMapping("/update/{id}")
    public ModelAndView updateSchedule(
            @PathVariable String id,
            @ModelAttribute("schedule") Schedule schedule,
            RedirectAttributes redirectAttributes) {

        schedule.setId(id);
        scheduleService.updateSchedule(id, schedule);
        redirectAttributes.addFlashAttribute("successMessage", "Schedule updated successfully!");
        return new ModelAndView("redirect:/admin/schedules");
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadTimetablePdf() throws IOException {

        List<String> dayOrder = Arrays.asList(
                "Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday");

        List<Schedule> schedules = scheduleService.getAllSchedules();

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

//             Draw title
            contentStream.beginText();
            contentStream.setFont(fontBold, 16);
            contentStream.newLineAtOffset(margin, yStart);
            contentStream.showText("GENERAL TIMETABLE");
            contentStream.endText();
            yStart -= 30;

            // Prepare table data
            List<List<String>> data = new ArrayList<>();
            // Add table headers
            data.add(Arrays.asList("Day", "Time", "Program", "Year", "Classroom", "Course Code", "Lecturer"));

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
                        schedule.getProgram(),
                        schedule.getYear(),
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
