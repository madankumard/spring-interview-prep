# Spring Interview Prep — Instructions for Claude

I'm preparing for Java/Spring interviews using `spring-interview-prep.md`.
We go through the questions one by one. I type all project code myself.

## Project context
- Practice app: **ShopLite** (products, orders, inventory, customers).
- Stack: Java 25, latest Spring Boot, PostgreSQL, Maven, Docker.
- Base package: `com.shoplite`
- Project location: `shoplite/` (Maven project inside this repo). You may **read** it
  any time to fit code to what I've actually typed (packages, class names, config).
  Never edit it.
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
   Keep a balance: not too short, not too complicated. Explain each point clearly in
   simple words, and explain any term the answer relies on (e.g. what stereotype
   annotations are), but skip deep internals and rarely-asked details.
   Rough size: Answer ~3 bullets, Key points ~4–6, Follow-ups 2–4 commonly asked ones.

**Show in chat only:**
1. **Code** — the complete code to type for ShopLite: file path, package, imports.
   One line before each file saying what it demonstrates. After every file's code
   block (before the next file, and before Steps & Verify) put this long separator line
   on its own, so files are easy to tell apart in the terminal (`---` renders too short):
   `════════════════════════════════════════════════════════════════════════`
2. **Steps & Verify** — one numbered list: each step says what to create, change or
   run, and right under it (indented "→ Expect:" line) what to look for (logs, output,
   HTTP response). Include an optional "break it" step when it teaches something.
3. End with one line saying the notes were saved to `notes/<topic>.md`.

**Chat stays minimal:** no answer, theory recap, or explanatory asides in chat. I read
the theory in the notes file. Only add a short practical tip in chat when it helps the
code or steps work (e.g. a gotcha, a Windows/IntelliJ quirk).

If I ask a side question about a topic (not a new question ID), answer it in chat normally.

## Notes files
After each answer, append it to `notes/<topic>.md`:
core.md, boot.md, rest.md, jpa.md, aop.md, security.md, microservices.md, cloud.md, testing.md.
Create the file if it doesn't exist.
- One file per topic. Only split into a `<topic>-2.md` if I ask for it. Use this format:

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

### Q: ...?
- answer point

- answer point

---

### Q: ...?
- answer point

```

Dividers: a single `---` line between every two follow-up questions (blank line before
and after it). Put NO `---` between main questions: the `## <ID>` heading is the divider
(my IDE preview draws a thick full-width line above every `##` heading via custom CSS).

Notes are for later revision: only real content. Full code, file paths, steps and
Verify go in the chat, not in the notes file. The Answer / Key points / Follow-ups
text goes in the notes file, not in the chat.

## Rules
- You may create and edit `.md` files (`notes/`, `CLAUDE.md`, `README.md`,
  `spring-interview-prep.md`). Never create or edit project code in `shoplite/` —
  show code in the chat so I can type it.
- Exception: for complicated topics later (e.g. microservices, security setups), I may
  explicitly ask you to **create a sample project**. Only then, write it in a separate
  folder (e.g. `samples/<topic>/`), never inside `shoplite/`. I'll replicate it myself
  in ShopLite to learn, so keep it small, focused on the topic, and runnable.
- **Flag it for me:** when I give a question ID, decide whether it fits as chat code in
  ShopLite or needs a separate sample project. If it needs one, say so in **one line at
  the top of the chat reply** with the reason (e.g. "needs 3 services + Kafka running"),
  then still write the notes as usual. Don't build the sample until I say yes.
  Rule of thumb: needs a sample = more than one running app, extra infrastructure
  (Kafka, Keycloak, Eureka, Config Server, Zipkin), or more than ~8 new files to see it
  work. Otherwise keep it as chat code in ShopLite.
- When I say I'm done with a question (e.g. "done with Core 3"), tick it `[x]` in
  `spring-interview-prep.md` yourself. Don't tick anything I haven't said is done.
- Don't quiz me or ask me questions back.
- Use current APIs only (Spring 6+/Boot 3+). If a topic has an outdated common answer,
  say what changed.
- If I paste an error or my code, explain what's wrong and show the fix.
- Keep answers concise. Go deeper only when I ask.
- No paragraphs — write answers as bullet points, in chat and in notes.
  Each bullet is 1–3 complete, spoken-style sentences I could say in an interview:
  use 2–3 when the point needs explaining, 1 when it's simple. Never a fragment.
- Bold the important words and key terms inside sentences so the notes are easy
  to scan. Don't bold whole sentences.
- In notes files, leave a blank line between every two bullet points so the
  preview shows space between them.
