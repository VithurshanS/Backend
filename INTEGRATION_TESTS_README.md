# Tutorverse Integration Tests

This directory contains comprehensive integration tests for the Tutorverse application that cover the complete user flow from account creation to advanced schedule management.

## 🎯 Test Coverage

The integration tests cover the following scenarios:

### 1. User Account Management
- ✅ Create user account with TUTOR role
- ✅ JWT authentication and token handling
- ✅ User information retrieval and validation

### 2. Tutor Profile Management
- ✅ Create comprehensive tutor profile
- ✅ Profile data validation
- ✅ Profile retrieval and verification

### 3. Module Management
- ✅ Create multiple modules with different domains
- ✅ Module data validation
- ✅ Module retrieval by tutor

### 4. Schedule Management
- ✅ Create one-time schedules (week_number = 0)
- ✅ Create weekly schedules (week_number = 1-7)
- ✅ Create daily schedules (week_number = 8)
- ✅ Schedule data validation and persistence

### 5. Schedule Clash Detection
- ✅ **Normal Cases**: Same time conflicts
- ✅ **Boundary Cases**: Midnight crossing schedules
- ✅ **Weekly Conflicts**: Recurring weekly schedule overlaps
- ✅ **Daily Conflicts**: Daily schedule overlaps
- ✅ **Cross-recurrence Conflicts**: Different recurrence types overlapping
- ✅ **Edge Cases**: Time window boundary conflicts

### 6. Upcoming Schedules Retrieval
- ✅ Get upcoming schedules by module
- ✅ Get upcoming schedules by tutor
- ✅ Complex time logic validation with `active` status
- ✅ Date and time filtering

### 7. Boundary Time Cases
- ✅ **Midnight Crossing**: Schedules that span across midnight
- ✅ **Noon Boundary**: Using 12:00:00 as tm boundary
- ✅ **Hour Window Edges**: Testing t0, t1, t2 boundaries
- ✅ **Special Edge Cases**: Exact boundary time conflicts

## 🚀 Running the Tests

### Quick Start
```bash
# Make the script executable
chmod +x run-integration-tests.sh

# Run the tests
./run-integration-tests.sh
```

### Manual Execution
```bash
# Using Maven
mvn test -Dtest=TutorverseIntegrationTest -Dspring.profiles.active=test

# Using Maven Wrapper
./mvnw test -Dtest=TutorverseIntegrationTest -Dspring.profiles.active=test
```

### Prerequisites
- Java 17 or higher
- Maven 3.6+ (or use the included Maven wrapper)
- PostgreSQL database running and accessible
- Database configured in `application-test.properties`

## 📋 Test Flow

The tests run in a specific order to simulate a complete user journey:

1. **Account Creation** → Create tutor account with authentication
2. **Profile Setup** → Complete tutor profile with all details
3. **Module Creation** → Create 3 different modules (Math, Science, CS)
4. **Schedule Creation** → Create all 3 types of schedules
5. **Clash Testing** → Test various conflict scenarios
6. **Retrieval Testing** → Test upcoming schedules functionality
7. **Boundary Testing** → Test edge cases and special scenarios

## 🔍 Understanding the Time Logic

The tests validate the complex time logic used in your scheduling system:

### Time Windows
- **t0** = schedule_time - 1 hour (start of time window)
- **t1** = schedule_time (actual scheduled time)
- **t2** = schedule_time + 1 hour (end of time window)
- **tm** = 12:00:00 (noon boundary for overnight schedules)

### Schedule Types
- **week_number = 0**: One-time schedule (specific date)
- **week_number = 1-7**: Weekly schedule (1=Monday, 7=Sunday)
- **week_number = 8**: Daily schedule (repeats every day)

### Conflict Detection
The tests validate that the trigger correctly detects conflicts in:
- Same time overlaps
- Boundary crossing schedules (midnight)
- Cross-recurrence type conflicts
- Edge cases at exact time boundaries

## 📊 Test Results

When you run the tests, you'll see detailed output including:
- ✅ Step-by-step progress indicators
- 📊 Created entity IDs and details
- 🎯 Conflict detection validation
- 📈 Schedule retrieval results
- 🏁 Final integration test summary

## 🐛 Troubleshooting

### Common Issues

1. **Database Connection Failure**
   - Check PostgreSQL is running
   - Verify connection details in `application-test.properties`
   - Ensure database exists and is accessible

2. **Authentication Issues**
   - JWT configuration in test properties
   - Cookie handling in test setup

3. **Compilation Errors**
   - Ensure all dependencies are installed
   - Check Java version compatibility
   - Run `mvn clean compile` first

4. **Schedule Clash Tests Failing**
   - Verify the SQL trigger is properly installed
   - Check database schema matches expectations
   - Ensure time zone handling is consistent

### Database Schema
Make sure your database has the complete schema with:
- All tables created
- The `check_schedule_clash()` trigger function installed
- The `get_upcoming_schedules()` function installed
- Proper indexes for performance

## 🎉 Success Indicators

When all tests pass, you'll see:
- All 19 test methods completed successfully
- No schedule conflicts missed by the trigger
- Proper upcoming schedule calculations
- Boundary cases handled correctly

This confirms that your Tutorverse backend is working correctly with robust schedule management and conflict detection!