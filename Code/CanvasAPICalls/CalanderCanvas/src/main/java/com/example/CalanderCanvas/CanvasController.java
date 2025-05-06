package com.example.CalanderCanvas;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * CanvasController:
 * 
 * This REST controller exposes various endpoints to interact with Canvas API data.
 * It delegates all data fetching logic to the CanvasAPIService. The service interacts
 * with the Canvas LMS API (using a provided API key and base URL) to retrieve courses, 
 * assignments, events, and grades.
 * 
 * Endpoints:
 * - /health-check: Returns a simple "Situation Normal" string to confirm the service is running.
 * - /courses: Returns a JSON representation of all courses the user is enrolled in.
 * - /grades: Returns a list of strings describing grades for all assignments and quizzes across courses.
 * - /active-events: Returns a list of active calendar events (non-blackout dates).
 * - /course-grades: Returns a map of course titles to a list of graded items (assignments/quizzes).
 * - /course-assignments: Returns a list of all assignments (past and current due) for the enrolled courses.
 * 
 * The @CrossOrigin annotation allows cross-origin requests, making the data accessible
 * from front-end applications served from different domains.
 */
@RestController
@CrossOrigin
public class CanvasController {

    // Service that communicates with Canvas API to fetch courses, assignments, grades, etc.
    private final CanvasAPIService canvasService;

    /**
     * Constructor-based dependency injection:
     * The CanvasAPIService is provided by the Spring context, and we store it for use in our endpoints.
     *
     * @param canvasService The service that interacts with Canvas API.
     */
    public CanvasController(CanvasAPIService canvasService) {
        this.canvasService = canvasService;
    }

    /**
     * GET /health-check
     * A simple endpoint to verify that the service is up and running.
     * Useful for monitoring and load balancers to check the application's health.
     *
     * @return A string "Situation Normal" indicating the service is functional.
     */
    @GetMapping("/health-check")
    public String getHealthCheck() {
        return "Situation Normal";
    }

    /**
     * GET /courses
     * Returns a JSON structure representing the user's enrolled courses.
     * This data might be used by the front-end to list courses and filter assignments.
     *
     * @return A JsonNode representing an array of courses with their details.
     */
    @GetMapping("/courses")
    public JsonNode getCourses() {
        return canvasService.getCourses();
    }

    /**
     * GET /grades
     * Retrieves a list of strings describing the grades for all assignments and quizzes.
     * Each entry might contain details like the assignment/quiz name, due date, points earned, and total points.
     *
     * @return A list of strings with grade details for each graded item across all courses.
     */
    @GetMapping("/grades")
    public List<String> getGrades() {
        return canvasService.getGrades();
    }

    /**
     * GET /active-events
     * Fetches a list of active calendar events from Canvas, excluding blackout dates.
     * Events might include upcoming due dates, class events, and other scheduled items.
     *
     * @return A list of strings describing each active calendar event.
     */
    @GetMapping("/active-events")
    public List<String> getActiveCalendarEvents() {
        return canvasService.getActiveCalendarEvents();
    }

    /**
     * GET /course-grades
     * Retrieves a map where each key is a course title and each value is a list of graded items for that course.
     * This structured view is useful for displaying all grades per course in the UI.
     *
     * @return A Map from course title (String) to a list of graded items (List<String>).
     */
    @GetMapping("/course-grades")
    public Map<String, List<String>> getCourseGrades() {
        return canvasService.getCourseGrades(); 
    }

    /**
     * GET /course-assignments
     * Returns all assignments (both past and current due) across all enrolled courses.
     * Unlike /grades, this endpoint does not emphasize grades, focusing on assignment names and due dates.
     * Useful for building calendars or lists of pending work.
     *
     * @return A list of strings describing assignments in the format "Assignment: NAME, Due Date: DATE".
     */
    @GetMapping("/course-assignments")
    public List<String> getAllCourseAssignments() {
        return canvasService.getAllCourseAssignments();
    }
}
