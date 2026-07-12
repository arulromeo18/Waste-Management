package com.wastemanagement.wastesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Waste Segregation Monitoring System application.
 *
 * This class bootstraps the Spring Boot application context, enabling:
 * - Auto-configuration of embedded Tomcat, Spring MVC, and Spring Data MongoDB
 * - Component scanning across the com.wastemanagement.wastesystem base package
 * - Scheduled tasks (used later for automated daily/monthly report generation
 *   and notification dispatch jobs)
 */
@SpringBootApplication
@EnableScheduling
public class WasteSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(WasteSystemApplication.class, args);
    }

}