# ShopLite — Spring Interview Prep

A hands-on practice project for Java/Spring interviews.
Every feature here exists to answer an interview question in `spring-interview-prep.md`.

## Repo layout
```
├── CLAUDE.md                  # How Claude Code helps in this repo
├── spring-interview-prep.md   # Question bank + roadmap (tick as you go)
├── notes/                     # Revision notes, written by Claude per question
└── shoplite/                  # The Spring Boot app (you type this code)
```

## Setup
1. Install Java 25, IntelliJ IDEA, Docker Desktop, Git.
2. Generate the app at https://start.spring.io
   - Maven, Java 25, latest stable Spring Boot (4.1.x)
   - Group `com.shoplite`, Artifact `shoplite`
   - Dependencies: Spring Web, Spring Data JPA, Validation, PostgreSQL Driver,
     Spring Boot Actuator, Testcontainers, Lombok (optional)
3. Unzip it into the `shoplite/` folder and open the repo in IntelliJ.
4. Open IntelliJ's terminal at the repo root and run `claude`.

## Daily workflow
1. Ask Claude Code the next question by ID, e.g. `Core 1`.
2. Read the answer; it's also saved to `notes/`.
3. Type the code into `shoplite/`, run it, check it works.
4. Tick the question in `spring-interview-prep.md` and commit.

## What I learned
Log bugs you hit and fixed — great interview stories.

| Date | Topic | What happened / what I learned |
|------|-------|--------------------------------|
|      |       |                                |
