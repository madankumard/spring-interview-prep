# Spring Interview Prep — Instructions for Claude

I'm preparing for Java/Spring interviews using `spring-interview-prep.md`.
We go through the questions one by one. I type all project code myself.

## Project context
- Practice app: **ShopLite** (products, orders, inventory, customers).
- Stack: Java 25, latest Spring Boot, PostgreSQL, Maven, Docker.
- Base package: `com.shoplite`
- Part 1 of `spring-interview-prep.md` describes how the app grows phase by phase.
  Fit code examples into the current state of the project.

## When I give a question
I'll use an ID like `Core 5`, `JPA 14`, `Security 13`, or paste the question.
IDs: Core, Boot, REST, JPA, AOP, Security, MS, Cloud, Test.

1. **Answer** — clear and interview-ready: a short answer the way I'd say it
   to an interviewer, then key points.
2. **Follow-ups** — the common follow-up questions an interviewer would ask, with short answers.
3. **Code** — if code helps, a small complete example for ShopLite, with the file path it belongs in.
4. **Verify** — how to run it and what to look for (logs, output, HTTP response).
5. **Save notes** — append the answer to the topic notes file (see below).

## Notes files
After each answer, append it to `notes/<topic>.md`:
core.md, boot.md, rest.md, jpa.md, aop.md, security.md, microservices.md, cloud.md, testing.md.
Create the file if it doesn't exist. Use this format:

```
## <ID> — <question>

**Answer:** ...

**Key points:**
- ...

**Follow-ups:**
- Q: ... A: ...

**Code:**
(code block)

**Verify:** ...

---
```

## Rules
- You may create and edit files ONLY inside `notes/`. Never create or edit project
  code — show code in the chat so I can type it.
- Don't quiz me or ask me questions back.
- Use current APIs only (Spring 6+/Boot 3+). If a topic has an outdated common answer,
  say what changed.
- If I paste an error or my code, explain what's wrong and show the fix.
- Keep answers concise. Go deeper only when I ask.
