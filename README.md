# ⚡ Know-It-All Bot

An AI-powered quiz application built with Spring Boot 4. Pick any topic, choose your AI engine, set the difficulty, and get challenged with smart, non-repetitive questions generated in real time.

---

## Features

- 🎯 **Topic-based quizzing** — any subject you can think of
- 🤖 **Multi-AI support** — switch between Groq, Gemini, and Cohere
- 🧠 **Smart questions** — no "what is X" trivia; AI generates specific, interesting questions
- 🔁 **No repeated questions** — previous questions are passed to the AI every round
- ⏱ **Timed answers** — time limit scales with difficulty, bonus points for speed
- 📊 **Score & leaderboard** — track your total score across all games
- 📜 **Full game history** — review every question, your answer, and why you were wrong
- 🔐 **JWT authentication** — secure login and registration

---

## Tech Stack

| Layer | Technology                                                       |
|---|------------------------------------------------------------------|
| Backend | Spring Boot 4, Spring Security, Spring Data JPA                  |
| Database | MySQL 8                                                          |
| Auth | JWT (jjwt 0.12.6)                                                |
| AI | Groq (LLaMA 3.3 70B), Gemini 2.0 Flash, cohere command-a-03-2025 |
| Frontend | Vanilla HTML/CSS/JS (served as static files)                     |
| Build | Maven                                                            |

---

## Getting Started

### Prerequisites

- Java 17+
- MySQL 8+
- API key for at least one AI provider (Groq recommended — free)

### 1. Clone the repo

```bash
git clone https://github.com/toktab/knowitall.git
cd knowitall
```

### 2. Create the database

```sql
CREATE DATABASE knowitalldb;
```

### 3. Set environment variables

Create a `.env` file or set these in your IDE's run configuration:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=knowitalldb
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=your-secret-key-at-least-32-characters-long
JWT_EXPIRATION=86400000
GROQ_API_KEY=gsk_your_groq_key_here
GEMINI_API_KEY=your_gemini_key_here
COHERE_API_KEY=your_cohere_key_here
```

> Only one AI API key is required to run the app. Groq has the best free tier — get a key at [console.groq.com](https://console.groq.com).

### 4. Run

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080) in your browser.

---

## Getting AI API Keys

| Provider | Free Tier | Get Key |
|---|---|---|
| **Groq** | ✅ Generous free tier | [console.groq.com](https://console.groq.com) → API Keys → Create |
| **Gemini** | ⚠️ Limited, requires billing for more | [console.cloud.google.com](https://console.cloud.google.com) → APIs & Services → Credentials |
| **Cohere** | ✅ Free trial | [dashboard.cohere.com](https://dashboard.cohere.com) |

---

## API Endpoints

### Auth
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT |

### Game
| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/game/start` | Start a new game session |
| GET | `/api/game/{sessionId}/question` | Get the next AI-generated question |
| POST | `/api/game/{sessionId}/answer` | Submit an answer |
| POST | `/api/game/{sessionId}/end` | End the session |
| GET | `/api/game/history` | Get all past sessions |
| GET | `/api/game/history/{sessionId}` | Get full detail for a session |

### Users
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/me` | Get current user profile |
| PUT | `/api/users/me` | Update email or password |
| GET | `/api/users/leaderboard` | Get top players by score |

---

## Scoring

Points per correct answer are calculated based on difficulty and how fast you answer:

```
base = 10 + (difficulty × 5)
time_bonus = (time_limit - time_taken) / time_limit × 0.5
points = base × (1 + time_bonus)
```

| Difficulty | Time Limit | Base Points |
|---|---|---|
| 1–3 | 45s | 15–25 pts |
| 4–6 | 30s | 30–40 pts |
| 7–10 | 20s | 45–60 pts |

---

## Project Structure

```
src/main/java/com/knowitall/
├── ai/
│   ├── config/         # AiProvider enum
│   └── service/        # AiService interface, GroqAiService, GeminiAiService, CohereAiService, AiServiceRouter
├── auth/
│   ├── controller/     # AuthController
│   ├── dto/            # LoginRequest, RegisterRequest, AuthResponse
│   ├── security/       # JwtAuthenticationFilter
│   └── service/        # AuthService, JwtService
├── config/             # SecurityConfig, PasswordConfig, AppConfig
├── exception/          # GlobalExceptionHandler
├── game/
│   ├── controller/     # GameController
│   ├── dto/            # Request/Response DTOs
│   ├── entity/         # GameSession, QuizQuestion
│   ├── repository/     # GameSessionRepository, QuizQuestionRepository
│   └── service/        # GameService
└── user/
    ├── controller/     # UserController
    ├── dto/            # UserResponse, LeaderboardEntry, UpdateUserRequest
    ├── entity/         # User
    ├── repository/     # UserRepository
    └── service/        # UserService
```

---

## License

MIT
