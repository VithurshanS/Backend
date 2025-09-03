# 🎯 Tutorverse API - Postman Test Cases Collection

## Base URL: `http://localhost:8080`

---

## 📋 **Test Sequence Order**
1. User Registration
2. User Login  
3. Tutor Profile Creation
4. Module Creation
5. Get All Modules
6. Search Modules
7. Schedule Management

---

## 🔐 **1. User Registration (TUTOR)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/register`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john.tutor@example.com",
  "password": "SecurePass123!",
  "role": "TUTOR",
  "name": "John Smith"
}
```

**Expected Response:** `200 OK`
```
"User registered"
```

---

## 🔐 **2. User Registration (STUDENT)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/register`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "sarah.student@example.com",
  "password": "MyPassword456!",
  "role": "STUDENT",
  "name": "Sarah Johnson"
}
```

**Expected Response:** `200 OK`
```
"User registered"
```

---

## 🔑 **3. User Login (TUTOR)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/auth/login`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "john.tutor@example.com",
  "password": "SecurePass123!"
}
```

**Expected Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful",
  "user": {
    "email": "john.tutor@example.com",
    "name": "John Smith",
    "role": "TUTOR"
  }
}
```

**⚠️ IMPORTANT:** Copy the `token` value from response for next requests!

---

## 🔑 **4. User Login (STUDENT)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/auth/login`  
**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "sarah.student@example.com",
  "password": "MyPassword456!"
}
```

**Expected Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login successful",
  "user": {
    "email": "sarah.student@example.com",
    "name": "Sarah Johnson",
    "role": "STUDENT"
  }
}
```

---

## 👤 **5. Get Current User Profile**

**Method:** `GET`  
**URL:** `http://localhost:8080/api/getuser`  
**Headers:**
```
Content-Type: application/json
Cookie: jwt_token=YOUR_JWT_TOKEN_HERE
```

**Expected Response:** `200 OK`
```json
{
  "user": {
    "id": "uuid-here",
    "email": "john.tutor@example.com",
    "name": "John Smith",
    "role": "TUTOR"
  }
}
```

---

## 🎓 **6. Create Tutor Profile**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/tutor-profile`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Smith",
  "phoneNo": "+1-555-0123",
  "gender": "MALE",
  "dob": "1990-05-15",
  "portfolio": "https://johnsmith-portfolio.com",
  "bio": "Experienced mathematics tutor with 8+ years of teaching experience. Specialized in calculus, algebra, and statistics. I help students build confidence in math through personalized learning approaches.",
  "image": "https://example.com/profile-images/john-smith.jpg"
}
```

**Expected Response:** `200 OK`
```json
{
  "tutorId": "uuid-here",
  "firstName": "John",
  "lastName": "Smith",
  "phoneNo": "+1-555-0123",
  "gender": "MALE",
  "dob": "1990-05-15",
  "portfolio": "https://johnsmith-portfolio.com",
  "bio": "Experienced mathematics tutor...",
  "image": "https://example.com/profile-images/john-smith.jpg",
  "createdAt": "2025-09-03T09:30:00",
  "updatedAt": "2025-09-03T09:30:00"
}
```

---

## 🎓 **7. Create Another Tutor Profile**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/tutor-profile`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_DIFFERENT_TUTOR_JWT_TOKEN
```

**Request Body:**
```json
{
  "firstName": "Emily",
  "lastName": "Davis",
  "phoneNo": "+1-555-0456",
  "gender": "FEMALE",
  "dob": "1985-11-22",
  "portfolio": "https://emilydavis-science.com",
  "bio": "PhD in Physics with 10+ years of university teaching experience. Passionate about making complex scientific concepts accessible to students of all levels.",
  "image": "https://example.com/profile-images/emily-davis.jpg"
}
```

---

## 📚 **8. Create Module (Mathematics)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/modules/create`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Request Body:**
```json
{
  "name": "Advanced Calculus Mastery",
  "domain": "Mathematics",
  "fee": 75.50,
  "duration": "PT2H30M",
  "status": "Active"
}
```

**Expected Response:** `201 Created`
```
"Module created successfully"
```

---

## 📚 **9. Create Module (Computer Science)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/modules/create`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Request Body:**
```json
{
  "name": "Java Programming Fundamentals",
  "domain": "Computer Science",
  "fee": 85.00,
  "duration": "PT3H",
  "status": "Active"
}
```

---

## 📚 **10. Create Module (Science)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/modules/create`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Request Body:**
```json
{
  "name": "Quantum Physics for Beginners",
  "domain": "Science",
  "fee": 95.00,
  "duration": "PT2H45M",
  "status": "Active"
}
```

---

## 📋 **11. Get All Modules**

**Method:** `GET`  
**URL:** `http://localhost:8080/api/modules`  
**Headers:**
```
Content-Type: application/json
```

**Expected Response:** `200 OK`
```json
[
  {
    "moduleId": "uuid-here",
    "tutorId": "uuid-here",
    "name": "Advanced Calculus Mastery",
    "domain": "Mathematics",
    "averageRatings": 0.0,
    "fee": 75.50,
    "duration": "PT2H30M",
    "status": "Active"
  },
  {
    "moduleId": "uuid-here",
    "tutorId": "uuid-here", 
    "name": "Java Programming Fundamentals",
    "domain": "Computer Science",
    "averageRatings": 0.0,
    "fee": 85.00,
    "duration": "PT3H",
    "status": "Active"
  }
]
```

---

## 🔍 **12. Search Modules by Name**

**Method:** `GET`  
**URL:** `http://localhost:8080/api/modules/search?query=calculus`  
**Headers:**
```
Content-Type: application/json
```

**Expected Response:** `200 OK`
```json
[
  {
    "moduleId": "uuid-here",
    "tutorId": "uuid-here",
    "name": "Advanced Calculus Mastery",
    "domain": "Mathematics",
    "averageRatings": 0.0,
    "fee": 75.50,
    "duration": "PT2H30M",
    "status": "Active"
  }
]
```

---

## 🔍 **13. Search Modules by Domain**

**Method:** `GET`  
**URL:** `http://localhost:8080/api/modules/search?query=computer`  
**Headers:**
```
Content-Type: application/json
```

---

## 📚 **14. Get Modules by Domain ID**

**Method:** `GET`  
**URL:** `http://localhost:8080/api/modules/domain/1`  
**Headers:**
```
Content-Type: application/json
```

**Note:** Domain IDs are:
- 1: Mathematics
- 2: Science  
- 3: English
- 4: Computer Science
- 5: History
- 6: Art

---

## 🗑️ **15. Delete Module**

**Method:** `DELETE`  
**URL:** `http://localhost:8080/api/modules/delete/{moduleId}`  
**Headers:**
```
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Replace `{moduleId}` with actual module UUID from previous responses**

**Expected Response:** `204 No Content`

---

## 🚫 **Error Test Cases**

### **16. Register with Existing Email**
**Method:** `POST`  
**URL:** `http://localhost:8080/api/register`  
**Body:**
```json
{
  "email": "john.tutor@example.com",
  "password": "AnotherPass123!",
  "role": "STUDENT",
  "name": "Different User"
}
```
**Expected Response:** `409 Conflict` - "Email already exists"

### **17. Login with Wrong Password**
**Method:** `POST`  
**URL:** `http://localhost:8080/api/auth/login`  
**Body:**
```json
{
  "email": "john.tutor@example.com",
  "password": "WrongPassword123!"
}
```
**Expected Response:** `401 Unauthorized` - "Invalid credentials"

### **18. Create Module Without Authorization**
**Method:** `POST`  
**URL:** `http://localhost:8080/api/modules/create`  
**Headers:**
```
Content-Type: application/json
```
**Body:**
```json
{
  "name": "Unauthorized Module",
  "domain": "Mathematics",
  "fee": 50.00
}
```
**Expected Response:** `401 Unauthorized` - "Missing token"

---

## 📝 **Additional Sample Data for Testing**

### **More User Registrations:**
```json
{
  "email": "mike.teacher@example.com",
  "password": "TeachPass789!",
  "role": "TUTOR", 
  "name": "Mike Wilson"
}
```

```json
{
  "email": "lisa.learner@example.com",
  "password": "LearnPass321!",
  "role": "STUDENT",
  "name": "Lisa Brown"
}
```

### **More Tutor Profiles:**
```json
{
  "firstName": "Alex",
  "lastName": "Chen",
  "phoneNo": "+1-555-0789",
  "gender": "OTHER",
  "dob": "1992-03-08",
  "portfolio": "https://alexchen-coding.dev",
  "bio": "Full-stack developer turned educator. Specializing in modern web technologies, algorithms, and software engineering best practices.",
  "image": "https://example.com/profile-images/alex-chen.jpg"
}
```

### **More Modules:**
```json
{
  "name": "English Literature Analysis",
  "domain": "English",
  "fee": 60.00,
  "duration": "PT2H",
  "status": "Active"
}
```

```json
{
  "name": "World History Deep Dive",
  "domain": "History", 
  "fee": 55.00,
  "duration": "PT2H15M",
  "status": "Active"
}
```

---

## 📅 **SCHEDULE MANAGEMENT**

## 📅 **19. Create Schedule (One-time)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/schedules/create`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Request Body:**
```json
{
  "moduleId": "YOUR_MODULE_UUID_HERE",
  "date": "2025-09-15",
  "time": "10:00:00",
  "duration": 90,
  "weekNumber": 0,
  "recurrentType": null
}
```

**Expected Response:** `201 Created`
```json
{
  "scheduleId": "uuid-here",
  "moduleId": "uuid-here",
  "date": "2025-09-15",
  "time": "10:00:00",
  "duration": 90,
  "weekNumber": 0,
  "recurrentType": null,
  "moduleName": "Advanced Calculus Mastery",
  "tutorName": "John Smith"
}
```

---

## 📅 **20. Create Weekly Recurring Schedule**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/schedules/create`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Request Body:**
```json
{
  "moduleId": "YOUR_MODULE_UUID_HERE",
  "date": "2025-09-16",
  "time": "14:30:00",
  "duration": 120,
  "weekNumber": 1,
  "recurrentType": "Weekly"
}
```

**Note:** weekNumber values:
- 0 = One-time (specific date)
- 1 = Monday, 2 = Tuesday, 3 = Wednesday, 4 = Thursday, 5 = Friday, 6 = Saturday, 7 = Sunday
- 8 = Daily recurring

---

## 📅 **21. Create Daily Recurring Schedule**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/schedules/create`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Request Body:**
```json
{
  "moduleId": "YOUR_MODULE_UUID_HERE",
  "date": "2025-09-20",
  "time": "09:00:00",
  "duration": 60,
  "weekNumber": 8,
  "recurrentType": "Daily"
}
```

---

## 📅 **22. Test Schedule Conflict (Should Fail)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/schedules/test-conflict`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Request Body (Create conflicting schedule):**
```json
{
  "moduleId": "YOUR_MODULE_UUID_HERE",
  "date": "2025-09-15",
  "time": "10:30:00",
  "duration": 90,
  "weekNumber": 0,
  "recurrentType": null
}
```

**Expected Response:** `409 Conflict`
```json
{
  "conflict": true,
  "message": "Schedule conflict: This time slot overlaps with an existing schedule for the same tutor",
  "conflictType": "Schedule overlap detected"
}
```

**Explanation:** This should conflict with the 10:00-11:30 schedule created in test case 19, as 10:30-12:00 overlaps.

---

## 📅 **23. Test Schedule Conflict (Different Tutor - Should Succeed)**

**Method:** `POST`  
**URL:** `http://localhost:8080/api/schedules/create`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer DIFFERENT_TUTOR_JWT_TOKEN
```

**Request Body:**
```json
{
  "moduleId": "DIFFERENT_TUTOR_MODULE_UUID",
  "date": "2025-09-15",
  "time": "10:00:00",
  "duration": 90,
  "weekNumber": 0,
  "recurrentType": null
}
```

**Expected Response:** `201 Created` (No conflict because different tutor)

---

## 📅 **24. Get All Schedules**

**Method:** `GET`  
**URL:** `http://localhost:8080/api/schedules`  
**Headers:**
```
Content-Type: application/json
```

**Expected Response:** `200 OK`
```json
[
  {
    "scheduleId": "uuid-here",
    "moduleId": "uuid-here",
    "date": "2025-09-15",
    "time": "10:00:00",
    "duration": 90,
    "weekNumber": 0,
    "recurrentType": null,
    "moduleName": "Advanced Calculus Mastery",
    "tutorName": "John Smith"
  }
]
```

---

## 📅 **25. Get My Schedules (Tutor Only)**

**Method:** `GET`  
**URL:** `http://localhost:8080/api/schedules/my-schedules`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Expected Response:** `200 OK` - Returns only schedules for the authenticated tutor

---

## 📅 **26. Get Schedules by Module**

**Method:** `GET`  
**URL:** `http://localhost:8080/api/schedules/module/{moduleId}`  
**Headers:**
```
Content-Type: application/json
```

**Replace `{moduleId}` with actual module UUID**

---

## 📅 **27. Update Schedule**

**Method:** `PUT`  
**URL:** `http://localhost:8080/api/schedules/{scheduleId}`  
**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Request Body:**
```json
{
  "moduleId": "YOUR_MODULE_UUID_HERE",
  "date": "2025-09-15",
  "time": "11:00:00",
  "duration": 60,
  "weekNumber": 0,
  "recurrentType": null
}
```

**Replace `{scheduleId}` with actual schedule UUID**

---

## 📅 **28. Delete Schedule**

**Method:** `DELETE`  
**URL:** `http://localhost:8080/api/schedules/{scheduleId}`  
**Headers:**
```
Authorization: Bearer YOUR_TUTOR_JWT_TOKEN
```

**Expected Response:** `204 No Content`

---

## 🚫 **Schedule Conflict Test Scenarios**

### **Scenario 1: Time Overlap**
1. Create schedule: 10:00-11:30 (90 minutes)
2. Try to create: 10:30-12:00 (90 minutes) → **CONFLICT**

### **Scenario 2: Weekly Recurrence Conflict**
1. Create weekly schedule: Monday 14:00-16:00 (weekNumber = 1)
2. Try to create: Monday 15:00-17:00 (weekNumber = 1) → **CONFLICT**

### **Scenario 3: Daily Recurrence Conflict**
1. Create daily schedule: 09:00-10:00 (weekNumber = 8)
2. Try to create any schedule: 09:30-10:30 → **CONFLICT**

### **Scenario 4: Different Tutors - No Conflict**
1. Tutor A creates: 10:00-11:30
2. Tutor B creates: 10:00-11:30 → **SUCCESS** (different tutors)

---

## 📝 **Schedule Field Explanations**

### **weekNumber Values:**
- `0` = One-time schedule (specific date only)
- `1-7` = Weekly recurring (1=Monday, 2=Tuesday, ..., 7=Sunday)
- `8` = Daily recurring (every day)

### **duration:**
- Value in minutes (e.g., 90 = 1 hour 30 minutes)

### **time Format:**
- Use 24-hour format: "HH:MM:SS" (e.g., "14:30:00" for 2:30 PM)

### **date Format:**
- Use ISO format: "YYYY-MM-DD" (e.g., "2025-09-15")

### **recurrentType:**
- "Weekly" for weekly recurring schedules
- "Daily" for daily recurring schedules
- `null` or omit for one-time schedules

---

## 🎯 **Testing Schedule Conflicts**

**Step-by-step conflict testing:**

1. **Create a base schedule** (Test case 19)
2. **Test overlapping time** (Test case 22) - Should fail
3. **Test exact same time** - Should fail
4. **Test adjacent time** (e.g., 11:30-13:00) - Should succeed
5. **Test different day** - Should succeed
6. **Test with different tutor** - Should succeed
