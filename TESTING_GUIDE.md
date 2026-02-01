# Testing & Swagger Guide

## ✅ Swagger/OpenAPI is NOW Configured!

I just added Swagger to your project. After you run the application, you can access:

### 📚 Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

This provides:
- Interactive API documentation
- **Built-in testing interface** - you can test all endpoints directly from the browser!
- Request/Response examples
- Authentication support (you can add your JWT token)

### 📄 OpenAPI JSON
```
http://localhost:8080/v3/api-docs
```

---

## 🧪 How to Run Tests and View Coverage

### Using Your IDE (Recommended)

#### **IntelliJ IDEA:**

1. **Run All Tests:**
   - Right-click on `src/test/java` folder
   - Select "Run 'All Tests' with Coverage"
   - Or use: `Ctrl+Shift+F10`

2. **View Coverage:**
   - After tests run, the Coverage window opens automatically
   - Shows percentage per package/class/method
   - Green = covered, Red = not covered

3. **Alternative - Maven View:**
   - Open Maven panel (View → Tool Windows → Maven)
   - Expand `auth` → `Lifecycle`
   - Double-click `test`
   - For JaCoCo report, click Plugins → jacoco → jacoco:report

#### **VS Code:**

1. Install extensions:
   - "Java Test Runner"
   - "Coverage Gutters"

2. Run tests:
   - Click "Run Test" above test methods
   - Or use Testing panel (beaker icon)

3. View coverage:
   - Install "JaCoCo" extension
   - Run Maven command: `jacoco:report`

#### **Eclipse:**

1. Right-click project → Coverage As → JUnit Test
2. Coverage view shows results automatically

---

### Using Maven Command Line

If you have Maven in PATH or use IDE terminal:

```bash
# Run tests
mvn test

# Run tests with JaCoCo coverage report
mvn clean test jacoco:report

# View the coverage percentage in console output
# Look for lines like: "Instruction Coverage: 92.3%"
```

---

## 📊 Viewing JaCoCo Coverage Report

### Method 1: HTML Report (Best)

After running tests with coverage:

1. Navigate to: `newtask/target/site/jacoco/index.html`
2. Open this file in your browser
3. You'll see:
   - **Overall coverage percentage** at the top
   - Coverage breakdown by package
   - Click packages to see class-level details
   - Click classes to see line-by-line coverage (green = covered, red = not covered)

### Method 2: IDE Coverage View

Most IDEs show coverage inline:
- **Green background** = line is covered by tests
- **Red background** = line is NOT covered
- **Yellow background** = partially covered (e.g., some conditions in an if statement)

### Method 3: Console Output

When you run tests, Maven prints a summary:
```
[INFO] --- jacoco-maven-plugin:0.8.11:report (report) @ auth ---
[INFO] Loading execution data file C:\...\target\jacoco.exec
[INFO] Analyzed bundle 'auth' with X classes

Coverage Summary:
- Instruction Coverage: XX.X%
- Branch Coverage: XX.X%
- Line Coverage: XX.X%
- Complexity Coverage: XX.X%
```

---

## 🔍 Understanding Test Coverage

### What the Numbers Mean:

- **90%+ = Target** ✅ (Your project is configured to enforce this)
- **80-89% = Good** ⚠️
- **Below 80% = Needs work** ❌

### Coverage Types:

1. **Instruction Coverage**: % of bytecode instructions executed
2. **Branch Coverage**: % of if/else branches tested
3. **Line Coverage**: % of code lines executed
4. **Method Coverage**: % of methods called

---

## 🧪 Your Test Files

All test files are in `src/test/java/com/example/auth/`:

### Unit Tests (Service Layer):
- `service/AuthServiceTest.java` - 6 tests
- `service/UserServiceTest.java` - 10 tests  
- `service/OtpServiceTest.java` - 6 tests

### Integration Tests (Controller Layer):
- `controller/AuthControllerIntegrationTest.java` - 7 tests
- `controller/UserControllerIntegrationTest.java` - 11 tests

**Total: 40 test methods covering all critical scenarios**

---

## 🚀 How to Manually Test with Swagger

### Step 1: Start the Application

Using your IDE:
- Click "Run" on `AuthApplication.java`
- Or right-click → Run 'AuthApplication'

Wait for console to show:
```
Started AuthApplication in X.XXX seconds
```

### Step 2: Open Swagger UI

Navigate to: http://localhost:8080/swagger-ui/index.html

### Step 3: Test Public Endpoints (No Auth Required)

#### Register a User:
1. Expand `POST /api/users/register`
2. Click "Try it out"
3. Edit the request body:
```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```
4. Click "Execute"
5. Check response (should be 201 Created)

#### Login:
1. Expand `POST /api/auth/login`
2. Click "Try it out"
3. Request body:
```json
{
  "usernameOrEmail": "testuser",
  "password": "password123"
}
```
4. Click "Execute"
5. **Copy the JWT token from the response**

### Step 4: Authorize with JWT

1. Click the 🔓 **"Authorize"** button at the top
2. In the "Value" field, type: `Bearer YOUR_JWT_TOKEN_HERE`
3. Click "Authorize"
4. Click "Close"

Now you're authenticated! All protected endpoints will work.

### Step 5: Test Protected Endpoints

#### Get Current User:
1. Expand `GET /api/users/me`
2. Click "Try it out"
3. Click "Execute"
4. Should return your user profile

#### Update Current User (PUT):
1. Expand `PUT /api/users/me`
2. Click "Try it out"
3. Request body:
```json
{
  "username": "updateduser",
  "email": "updated@example.com"
}
```
4. Click "Execute"

#### Test Admin Endpoints (Will fail with 403 Forbidden):
- `GET /api/users` - because your user has USER role, not ADMIN

### Step 6: Create Admin User & Test Admin Endpoints

To test admin endpoints, you need an ADMIN user:

1. Register another user via Swagger
2. Stop the application
3. Open MySQL and run:
```sql
UPDATE users SET role = 'ADMIN' WHERE username = 'adminuser';
```
4. Restart application
5. Login with admin user
6. Use the new JWT token to authorize
7. Now admin endpoints will work!

---

## 🔐 Testing Password Reset Flow

1. **Request OTP**: `POST /api/auth/forgot-password`
```json
{
  "email": "test@example.com"
}
```

2. **Check Console Logs** - the OTP will be printed:
```
=================================================
MOCK EMAIL SERVICE - OTP EMAIL
=================================================
To: test@example.com
Subject: Password Reset OTP
Body: Your OTP code is: 123456
...
=================================================
```

3. **Reset Password**: `POST /api/auth/reset-password`
```json
{
  "email": "test@example.com",
  "otpCode": "123456",
  "newPassword": "newPassword123"
}
```

4. **Login with new password**

---

## 🛠️ Alternative Testing Tools

### Postman
1. Import API by pasting: `http://localhost:8080/v3/api-docs`
2. Postman will generate all endpoints automatically
3. Add Authorization header: `Bearer YOUR_JWT_TOKEN`

### cURL Examples

**Register:**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\"}"
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"usernameOrEmail\":\"testuser\",\"password\":\"password123\"}"
```

**Get Current User (with JWT):**
```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

## 📋 Quick Reference

| Task | Action |
|------|--------|
| **Run tests** | IDE: Right-click test folder → Run with Coverage |
| **View coverage %** | Open `target/site/jacoco/index.html` |
| **Test APIs visually** | http://localhost:8080/swagger-ui/index.html |
| **Get OpenAPI spec** | http://localhost:8080/v3/api-docs |
| **View test files** | `src/test/java/com/example/auth/` |
| **Create admin user** | Register user, then `UPDATE users SET role='ADMIN'` in MySQL |

---

## ✅ Expected Coverage Results

After running tests, you should see:
- **Overall Coverage: 90%+** ✅
- All service classes: 95%+
- All controllers: 85%+
- Exception handlers: 100%
- DTOs: 100% (no logic, just data)

If coverage is below 90%, the build will **fail** (configured in pom.xml).

---

**Pro Tip**: Use Swagger UI for quick manual testing. It's the fastest way to test all endpoints without writing cURL commands or setting up Postman!
