# 🖥️ Financify Backend — Ktor REST API

A production-ready Ktor backend for the Financify Personal Finance app with **dual authentication** (Email/Password + Google OAuth), **JWT tokens**, and **Neon PostgreSQL** database.

---

## 🔧 Setup

### 1. Configure Neon PostgreSQL

1. Go to [neon.tech](https://neon.tech) and create a free project
2. Create a database named `financify`
3. Copy the connection string

### 2. Configure `application.conf`

Open `src/main/resources/application.conf` and paste your credentials:

```hocon
database {
    # >>> PASTE YOUR NEON DATABASE URI HERE <<<
    url = "jdbc:postgresql://ep-xyz-123.us-east-1.aws.neon.tech/financify?sslmode=require"
    user = "your_neon_username"
    password = "your_neon_password"
}

jwt {
    secret = "change-this-to-a-strong-random-string"
}

google {
    clientId = "your-google-client-id.apps.googleusercontent.com"
    clientSecret = "your-google-client-secret"
}
```

> **Or** set environment variables: `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD`, `JWT_SECRET`, `GOOGLE_CLIENT_ID`

### 3. Run the Server

```bash
cd backend
./gradlew run
```

Server starts at `http://localhost:8080`

---

## 📡 API Endpoints

### Health Check
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | No | API status |
| GET | `/health` | No | Health check |

### Auth
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | No | Register with email/password |
| POST | `/api/auth/login` | No | Login with email/password |
| POST | `/api/auth/google` | No | Sign in with Google ID token |
| GET | `/api/auth/me` | JWT | Get current user profile |

### Transactions
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/transactions` | JWT | List all transactions |
| GET | `/api/transactions?type=INCOME` | JWT | Filter by type |
| GET | `/api/transactions?category=FOOD` | JWT | Filter by category |
| GET | `/api/transactions?search=swiggy` | JWT | Search transactions |
| GET | `/api/transactions?startDate=2024-01-01&endDate=2024-01-31` | JWT | Date range |
| GET | `/api/transactions/{id}` | JWT | Get single transaction |
| GET | `/api/transactions/summary` | JWT | Income/expense totals |
| POST | `/api/transactions` | JWT | Create transaction |
| PUT | `/api/transactions/{id}` | JWT | Update transaction |
| DELETE | `/api/transactions/{id}` | JWT | Delete transaction |

### Goals
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/goals` | JWT | List all goals |
| GET | `/api/goals?active=true` | JWT | Active goals only |
| GET | `/api/goals/{id}` | JWT | Get single goal |
| POST | `/api/goals` | JWT | Create goal |
| PUT | `/api/goals/{id}` | JWT | Update goal |
| POST | `/api/goals/{id}/savings` | JWT | Add savings to goal |
| DELETE | `/api/goals/{id}` | JWT | Delete goal |

---

## 📝 Request/Response Examples

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "pass123", "name": "John Doe"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "authProvider": "EMAIL",
    "createdAt": "2024-01-15T10:30:00"
  },
  "message": "Registration successful"
}
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "pass123"}'
```

### Google Sign-In
```bash
curl -X POST http://localhost:8080/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{"idToken": "eyJhbG...(Google ID token from mobile app)"}'
```

### Create Transaction (with JWT)
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOi..." \
  -d '{
    "amount": 500.0,
    "type": "EXPENSE",
    "category": "FOOD",
    "date": "2024-01-15",
    "note": "Lunch at restaurant"
  }'
```

### Add Savings to Goal
```bash
curl -X POST http://localhost:8080/api/goals/1/savings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOi..." \
  -d '{"amount": 5000.0}'
```

---

## 🔐 Auth Flow

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│  Mobile App  │────>│  Ktor Server  │────>│  Neon DB     │
│              │     │               │     │  PostgreSQL  │
│  Email Login │     │  Verify Pass  │     │  Users Table │
│  ────────── │     │  ──────────── │     │              │
│  Google Auth │────>│  Verify Token │     │  + Link      │
│              │     │  with Google  │     │  Accounts    │
│              │<────│  Issue JWT    │     │              │
└─────────────┘     └──────────────┘     └─────────────┘
```

Both auth methods issue the same JWT. An email user who later signs in with Google gets their accounts **automatically linked**.

---

## 🗄️ Database Schema

Tables are auto-created on first run:

- **users**: id, email, password_hash, name, google_id, auth_provider, profile_picture_url, created_at, updated_at
- **transactions**: id, user_id (FK), amount, type, category, date, note, created_at, updated_at
- **goals**: id, user_id (FK), title, target_amount, current_amount, deadline, category, is_active, is_no_spend_challenge, streak_days, created_at, updated_at

---

## 🚀 Deployment

### Railway / Render
Set environment variables:
```
DATABASE_URL=jdbc:postgresql://ep-xyz.neon.tech/financify?sslmode=require
DATABASE_USER=your_user
DATABASE_PASSWORD=your_password
JWT_SECRET=strong-random-secret
GOOGLE_CLIENT_ID=your-google-client-id
PORT=8080
```

### Docker (optional)
```dockerfile
FROM gradle:8.7-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM openjdk:17-slim
COPY --from=build /home/gradle/src/build/libs/*-all.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

---

## 📱 Connecting the Android App

In the Android app, update `FinancifyApi.kt`:
```kotlin
companion object {
    // For local dev with Android emulator:
    const val BASE_URL = "http://10.0.2.2:8080/"
    
    // For production:
    // const val BASE_URL = "https://your-deployed-server.com/"
}
```

Then update `NetworkModule.kt` to add an Authorization interceptor with the stored JWT token.
