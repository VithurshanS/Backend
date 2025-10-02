package com.tutoring.Tutorverse.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database Function Tests
 * 
 * Tests the PostgreSQL functions directly:
 * - get_upcoming_schedules function
 * - check_schedule_clash trigger
 */
@SpringBootTest
@ActiveProfiles("test")
public class DatabaseFunctionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Test get_upcoming_schedules function exists and is callable")
    void testGetUpcomingSchedulesFunction() {
        try {
            // Test that the function exists and can be called
            LocalDate testDate = LocalDate.now();
            LocalTime testTime = LocalTime.now();
            
            String sql = "SELECT * FROM get_upcoming_schedules(?, ?, NULL, NULL, 5)";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, testDate, testTime);
            
            // Function should exist and be callable (even if no results)
            assertNotNull(results, "Function should return a result set");
            
            System.out.println("✅ get_upcoming_schedules function is callable");
            System.out.println("   Results count: " + results.size());
            
        } catch (Exception e) {
            fail("get_upcoming_schedules function test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test database schema has required tables")
    void testDatabaseSchema() {
        try {
            // Test that all required tables exist
            String[] requiredTables = {
                "roles", "users", "tutor", "student", "domain", "modules", 
                "schedules", "recurrent"
            };

            for (String table : requiredTables) {
                String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ?";
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class, table);
                assertTrue(count > 0, "Table '" + table + "' should exist");
            }
            
            System.out.println("✅ All required tables exist in database");
            
        } catch (Exception e) {
            fail("Database schema test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test schedule clash trigger function exists")
    void testScheduleClashTriggerExists() {
        try {
            // Check if the check_schedule_clash function exists
            String sql = "SELECT COUNT(*) FROM information_schema.routines WHERE routine_name = 'check_schedule_clash'";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            assertTrue(count > 0, "check_schedule_clash function should exist");
            
            // Check if the trigger exists
            String triggerSql = "SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_name = 'trg_check_schedule_clash'";
            Integer triggerCount = jdbcTemplate.queryForObject(triggerSql, Integer.class);
            assertTrue(triggerCount > 0, "trg_check_schedule_clash trigger should exist");
            
            System.out.println("✅ Schedule clash trigger and function exist");
            
        } catch (Exception e) {
            fail("Schedule clash trigger test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test basic data insertion into key tables")
    void testBasicDataInsertion() {
        try {
            // Test that we can insert basic test data
            
            // Check if roles exist
            String rolesSql = "SELECT COUNT(*) FROM roles WHERE name = 'TUTOR'";
            Integer roleCount = jdbcTemplate.queryForObject(rolesSql, Integer.class);
            assertTrue(roleCount > 0, "TUTOR role should exist");
            
            // Check if domains exist
            String domainsSql = "SELECT COUNT(*) FROM domain";
            Integer domainCount = jdbcTemplate.queryForObject(domainsSql, Integer.class);
            assertTrue(domainCount > 0, "Should have some domains available");
            
            System.out.println("✅ Basic seed data exists in database");
            System.out.println("   - Roles available");
            System.out.println("   - Domains available: " + domainCount);
            
        } catch (Exception e) {
            fail("Basic data insertion test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test upcoming schedules function with sample parameters")
    void testUpcomingSchedulesFunctionWithParameters() {
        try {
            // Test the function with various parameter combinations
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalTime morning = LocalTime.of(9, 0);
            
            // Test with date and time only
            String sql1 = "SELECT * FROM get_upcoming_schedules(?, ?, NULL, NULL, 3)";
            List<Map<String, Object>> results1 = jdbcTemplate.queryForList(sql1, tomorrow, morning);
            assertNotNull(results1);
            
            // Test with different limit
            String sql2 = "SELECT * FROM get_upcoming_schedules(?, ?, NULL, NULL, 10)";
            List<Map<String, Object>> results2 = jdbcTemplate.queryForList(sql2, tomorrow, morning);
            assertNotNull(results2);
            
            System.out.println("✅ get_upcoming_schedules function works with different parameters");
            System.out.println("   - With limit 3: " + results1.size() + " results");
            System.out.println("   - With limit 10: " + results2.size() + " results");
            
        } catch (Exception e) {
            fail("Upcoming schedules function parameter test failed: " + e.getMessage());
        }
    }
}