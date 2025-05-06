package com.example.CalanderCanvas;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service // Marks this class as a Spring service for dependency injection.
public class CanvasAPIService {

    // Injects the API key from the application properties or environment variables.
    @Value("${api.key}")
    private String CanvasAPIkey;

    // Injects the Canvas API base URL from the application properties or environment variables.
    @Value("${canvas.url}")
    private String canvasApiURL;

    // RestTemplate is used to send HTTP requests.
    private final RestTemplate restTemplate;

    // ObjectMapper is used to parse JSON responses into Java objects.
    private final ObjectMapper objectMapper;

    // Constructor to initialize RestTemplate and ObjectMapper via dependency injection.
    public CanvasAPIService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches a list of courses the user is enrolled in as a student.
     *
     * @return A JsonNode object containing the course data.
     */
    public JsonNode getCourses() {
        // Builds the URL for fetching courses using the Canvas API.
        String url = UriComponentsBuilder.fromHttpUrl(canvasApiURL + "/api/v1/courses?enrollment_type=student")
            .queryParam("access_token", CanvasAPIkey) // Adds the API key as a query parameter.
            .toUriString();

        // Sends a GET request to the Canvas API to retrieve the list of courses.
        String response = restTemplate.getForObject(url, String.class);

        try {
            // Parses the JSON response into a JsonNode object.
            return objectMapper.readTree(response);
        } catch (Exception e) {
            // Throws a runtime exception if parsing fails.
            throw new RuntimeException("Failed to parse courses JSON response", e);
        }
    }

    /**
     * Retrieves grades for all assignments in all courses.
     *
     * @return A list of strings describing grades for each assignment.
     */
public List<String> getGrades() {
    // Fetches the list of courses using the getCourses method.
    JsonNode courses = getCourses();
    List<String> grades = new ArrayList<>(); // Stores grade details for assignments and quizzes.

    // Ensures that the response is an array before iterating.
    if (courses.isArray()) {
        for (JsonNode course : courses) {
            // Extracts the course ID from the JSON response.
            String courseId = course.path("id").asText();
            
            // Fetch assignment and quiz grades for the current course using helper functions
            fetchAssignments(courseId, grades);
            fetchQuizzes(courseId, grades);
        }
    }
    return grades; // Returns the list of grades.
}

    /**
     * Retrieves all active calendar events across all courses.
     * An active event is one where blackout_date is set to false.
     *
     * @return A list of strings describing active calendar events.
     */
    public List<String> getActiveCalendarEvents() {
        // Builds the URL to fetch all calendar events from the Canvas API.
        String url = UriComponentsBuilder.fromHttpUrl(canvasApiURL + "/api/v1/calendar_events")
                .queryParam("access_token", CanvasAPIkey) // Adds the API key as a query parameter.
                .toUriString();

        try {
            // Sends a GET request to retrieve calendar events.
            String response = restTemplate.getForObject(url, String.class);

            // Parses the JSON response into a JsonNode.
            JsonNode rootNode = objectMapper.readTree(response);

            // List to store details of active events.
            List<String> activeEvents = new ArrayList<>();

            // Iterates over each calendar event in the response array.
            if (rootNode.isArray()) {
                for (JsonNode eventNode : rootNode) {
                    // Checks if the event is not a blackout date.
                    boolean blackoutDate = eventNode.path("blackout_date").asBoolean(false);
                    if (!blackoutDate) {
                        // Extracts event details.
                        String title = eventNode.path("title").asText("No Title"); // Event title.
                        String description = eventNode.path("description").asText("No Description"); // Event description.

                        // Adds the event details to the list.
                        activeEvents.add("Event: " + title + " | Description: " + description);
                    }
                }
            }
            return activeEvents; // Returns the list of active events.
        } catch (Exception e) {
            // Throws a runtime exception if an error occurs while fetching or parsing events.
            throw new RuntimeException("Error fetching active calendar events", e);
        }
    }

        /**
     * Retrieves grades split by each course.
     *
     * @return A Map where the key is the course title, and the value is a list of assignments and their grades.
     */
/**
 * Retrieves grades split by each course, including both assignments and quizzes.
 *
 * @return A Map where the key is the course title, and the value is a list of assignments, quizzes, and their grades.
 */
public Map<String, List<String>> getCourseGrades() {
    JsonNode courses = getCourses(); // Fetch the list of courses.
    Map<String, List<String>> courseGrades = new HashMap<>(); // Stores grades per course.

    if (courses.isArray()) {
        for (JsonNode course : courses) {
            // Extract course details.
            String courseId = course.path("id").asText();
            String courseTitle = course.path("name").asText("Unknown Course");

            // Initialize a list to hold assignment and quiz grades for this course.
            List<String> gradesList = new ArrayList<>();

            // Fetch assignments data for this course.
            fetchAssignments(courseId, gradesList);

            // Fetch quizzes data for this course.
            fetchQuizzes(courseId, gradesList);

            // Add this course's grades (assignments and quizzes) to the courseGrades map.
            courseGrades.put(courseTitle, gradesList);
        }
    }
    return courseGrades; // Returns the map of grades split by course.
}
    /**
     * New method to fetch all assignments (without grade info) from all courses.
     * Returns a list of strings in the format:
     * "Assignment: {name}, Due Date: {YYYY-MM-DDTHH:MM:SSZ}"
     */
    public List<String> getAllCourseAssignments() {
        JsonNode courses = getCourses();
        List<String> allAssignments = new ArrayList<>();

        if (courses.isArray()) {
            for (JsonNode course : courses) {
                String courseId = course.path("id").asText();
                fetchAssignments(courseId, allAssignments);
            }
        }
        return allAssignments;
    }

 
/**
 * Fetch assignments for a specific course and add their grades to the list.
 */
private void fetchAssignments(String courseId, List<String> gradesList) {
    String assignmentsUrl = UriComponentsBuilder.fromHttpUrl(canvasApiURL + "/api/v1/courses/" + courseId + "/assignments")
        .queryParam("access_token", CanvasAPIkey)
        .toUriString();

    try {
        String response = restTemplate.getForObject(assignmentsUrl, String.class);
        JsonNode assignments = objectMapper.readTree(response);

        if (assignments.isArray()) {
            for (JsonNode assignment : assignments) {
                String name = assignment.path("name").asText("No Name");
                String pointsPossible = assignment.path("points_possible").asText("0");
                String assignmentId = assignment.path("id").asText();
                String dueDate = assignment.path("due_at").asText("No Due Date");

                // Fetch submission for assignment
                String submissionUrl = UriComponentsBuilder.fromHttpUrl(canvasApiURL + "/api/v1/courses/" + courseId + "/assignments/" + assignmentId + "/submissions/self")
                    .queryParam("access_token", CanvasAPIkey)
                    .toUriString();

                try {
                    String submissionResponse = restTemplate.getForObject(submissionUrl, String.class);
                    JsonNode submission = objectMapper.readTree(submissionResponse);
                    String pointsEarned = submission.path("score").asText("Not Available");

                    // Add assignment grade and due date to the list
                    gradesList.add("Assignment: " + name + ", Due Date: " + dueDate + ", Points Earned: " + pointsEarned + ", Total Points: " + pointsPossible);
                } catch (Exception e) {
                    gradesList.add("Assignment: " + name + ", Due Date: " + dueDate + ", Points Earned: Not Available, Total Points: " + pointsPossible);
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Failed to fetch assignments for course ID: " + courseId);
    }
}

/**
 * Fetch quizzes for a specific course and add their grades to the list.
 */
private void fetchQuizzes(String courseId, List<String> gradesList) {
    String quizzesUrl = UriComponentsBuilder.fromHttpUrl(canvasApiURL + "/api/v1/courses/" + courseId + "/quizzes")
        .queryParam("access_token", CanvasAPIkey)
        .toUriString();

    try {
        String response = restTemplate.getForObject(quizzesUrl, String.class);
        JsonNode quizzes = objectMapper.readTree(response);

        if (quizzes.isArray()) {
            for (JsonNode quiz : quizzes) {
                String title = quiz.path("title").asText("No Title");
                String pointsPossible = quiz.path("points_possible").asText("0");
                String quizId = quiz.path("id").asText();
                String dueDate = quiz.path("due_at").asText("No Due Date");

                // Fetch submission for quiz
                String submissionUrl = UriComponentsBuilder.fromHttpUrl(canvasApiURL + "/api/v1/courses/" + courseId + "/quizzes/" + quizId + "/submissions/self")
                    .queryParam("access_token", CanvasAPIkey)
                    .toUriString();

                try {
                    String submissionResponse = restTemplate.getForObject(submissionUrl, String.class);
                    JsonNode submission = objectMapper.readTree(submissionResponse);
                    String pointsEarned = submission.path("score").asText("Not Available");

                    // Add quiz grade and due date to the list
                    gradesList.add("Quiz: " + title + ", Due Date: " + dueDate + ", Points Earned: " + pointsEarned + ", Total Points: " + pointsPossible);
                } catch (Exception e) {
                    gradesList.add("Quiz: " + title + ", Due Date: " + dueDate + ", Points Earned: Not Available, Total Points: " + pointsPossible);
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Failed to fetch quizzes for course ID: " + courseId);
    }
}
}