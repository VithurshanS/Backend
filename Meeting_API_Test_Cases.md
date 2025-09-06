# Meeting API Test Cases

## Create Meeting Endpoint

### POST /api/meeting/join

Creates a Jitsi meeting room with proper authentication and role-based access.

#### Request Headers
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

#### Request Body
```json
{
  "moduleId": "6082f12a-2859-4ae5-93df-920ff6804fcf",
  "requestedDate": "2025-09-10",
  "requestedTime": "10:30:00"
}
```

#### Success Response (200 OK)
```json
{
  "success": true,
  "meetingLink": "https://jit.shancloudservice.com/{schedule-id}?jwt={jitsi-token}",
  "scheduleId": "schedule-uuid-here",
  "roomId": "schedule-uuid-here",
  "userRole": "TUTOR",
  "isModerator": true,
  "token": "jitsi-jwt-token-here",
  "userDetails": {
    "userId": "user-uuid-here",
    "email": "tutor@example.com",
    "name": "John Doe"
  },
  "meetingDetails": {
    "moduleId": "6082f12a-2859-4ae5-93df-920ff6804fcf",
    "requestedDate": "2025-09-10",
    "requestedTime": "10:30:00"
  }
}
```

#### Error Responses

**401 Unauthorized - Invalid Token**
```json
{
  "success": false,
  "error": "Error creating meeting: Invalid token"
}
```

**404 Not Found - No Matching Schedule**
```json
{
  "success": false,
  "error": "Error creating meeting: No matching schedule found for the requested date, time and module"
}
```

**404 Not Found - User Not Found**
```json
{
  "success": false,
  "error": "Error creating meeting: User not found"
}
```

## Test Scenarios

### 1. Tutor Creating Meeting
- **Role**: TUTOR
- **Expected**: isModerator = true, can control meeting
- **Token**: Contains moderator privileges

### 2. Student Joining Meeting
- **Role**: STUDENT  
- **Expected**: isModerator = false, guest access
- **Token**: Contains guest privileges

### 3. Schedule Validation
- **Input**: Valid module, date, and time
- **Expected**: Matching schedule found using database function
- **Database Call**: `find_matching_schedule('2025-09-10', '10:30:00', 'module-uuid')`

### 4. Token Generation
- **Jitsi Config**: 
  - App ID: mydeploy1
  - Domain: jit.shancloudservice.com
  - Room ID: Uses schedule UUID
  - Expiration: 1 hour

### 5. Meeting Link Format
```
https://jit.shancloudservice.com/{schedule-id}?jwt={jitsi-token}
```

## Implementation Features

✅ **User Authentication**: Validates JWT token from Authorization header
✅ **Role-based Access**: TUTOR = moderator, STUDENT = guest
✅ **Schedule Matching**: Uses database function to find valid schedules
✅ **Jitsi Integration**: Generates proper JWT tokens for Jitsi Meet
✅ **Error Handling**: Comprehensive error responses
✅ **Security**: Token validation and user verification

## Postman Test Collection

### Setup
1. Set base URL: `http://localhost:8080`
2. Add Authorization header with valid JWT token
3. Set Content-Type to application/json

### Test Data
```json
{
  "moduleId": "6082f12a-2859-4ae5-93df-920ff6804fcf",
  "requestedDate": "2025-09-10", 
  "requestedTime": "10:30:00"
}
```

### cURL Example
```bash
curl -X POST http://localhost:8080/api/meeting/join \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "moduleId": "6082f12a-2859-4ae5-93df-920ff6804fcf",
    "requestedDate": "2025-09-10",
    "requestedTime": "10:30:00"
  }'
```
