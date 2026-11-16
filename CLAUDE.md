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

The theory goes in the notes file; the chat is for doing.

**Write to the notes file only (do NOT repeat this text in chat):**
1. **Answer** — clear and interview-ready: the way I'd say it to an interviewer.
2. **Key points** — with a short code snippet right after the point it illustrates.
3. **Follow-ups** — always last: the common follow-up questions an interviewer would ask.
   Don't over-shorten the answers: explain each one clearly, and explain any term the
   answer relies on (e.g. what stereotype annotations are), not just name it.

**Show in chat only:**
1. **Code** — the complete code to type for ShopLite: file path, package, imports.
   One line before each file saying what it demonstrates.
2. **Steps** — numbered step-by-step instructions: what to create, what to change,
   what to run, in order.
3. **Verify** — what to look for (logs, output, HTTP response), plus an optional
   "break it" step when it teaches something.
4. End with one line saying the notes were saved to `notes/<topic>.md`.

If I ask a side question about a topic (not a new question ID), answer it in chat normally.

## Notes files
After each answer, append it to `notes/<topic>.md`:
core.md, boot.md, rest.md, jpa.md, aop.md, security.md, microservices.md, cloud.md, testing.md.
Create the file if it doesn't exist. Use this format:

```
## <ID> — <question>

**Answer:**
- ... (bullet points, no paragraphs; each bullet 2–3 complete sentences)

**Key points:**
- point
(short code snippet right after the point it illustrates, if useful —
 no file paths, imports, or boilerplate)
- point

**Follow-ups:**

**Q: ...?**
- answer point
- answer point

**Q: ...?**
- answer point

---
```

Notes are for later revision: only real content. Full code, file paths, steps and
Verify go in the chat, not in the notes file. The Answer / Key points / Follow-ups
text goes in the notes file, not in the chat.

## Rules
- You may create and edit `.md` files (`notes/`, `CLAUDE.md`, `README.md`,
  `spring-interview-prep.md`). Never create or edit project code — show code in
  the chat so I can type it.
- When I say I'm done with a question (e.g. "done with Core 3"), tick it `[x]` in
  `spring-interview-prep.md` yourself. Don't tick anything I haven't said is done.
- Don't quiz me or ask me questions back.
- Use current APIs only (Spring 6+/Boot 3+). If a topic has an outdated common answer,
  say what changed.
- If I paste an error or my code, explain what's wrong and show the fix.
- Keep answers concise. Go deeper only when I ask.
- No paragraphs — write answers as bullet points, in chat and in notes.
  Each bullet is 2–3 complete, spoken-style sentences I could say in an interview,
  not a one-line fragment.
- Bold the important words and key terms inside sentences so the notes are easy
  to scan. Don't bold whole sentences.
