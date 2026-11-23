# Spring Core — Notes (Part 2: Core 16 onwards)

## Core 16 — @Lazy — what and when

**Answer:**
- By default, Spring creates **all singleton beans at startup**, which is called **eager initialization**. It's the default because any wiring or config error shows up **immediately** when the app starts.

- **`@Lazy`** tells Spring to create a bean **only when it's first used**, instead of at startup. I use it for beans that are **expensive to create** and **not always needed**, like a report generator that loads a lot of data.

- `@Lazy` can go **on the bean** or **at the injection point**. It's also a quick fix for a **circular dependency**, because Spring injects a **proxy** first and creates the real bean later.

**Key points:**
- **`@Lazy` on the class** (or on a `@Bean` method): Spring skips the bean at startup and creates it on the **first request** for it.
```java
@Component
@Lazy
public class SalesReportGenerator { ... }
```

- **The common trap**: `@Lazy` on the class **isn't enough** if a normal, eager bean injects it. That bean needs it at startup, so Spring creates it **anyway**.

- **`@Lazy` at the injection point**: Spring injects a **proxy**, a stand-in object that looks like the real bean. The real bean is created only when I **call a method** on the proxy for the first time.
```java
public ReportService(@Lazy SalesReportGenerator generator) {
    this.generator = generator;   // a proxy; the real bean doesn't exist yet
}
```

- **Global lazy initialization**: setting **`spring.main.lazy-initialization=true`** makes **every** bean lazy. Startup gets faster, but errors move from startup to **runtime**, and the **first request** to each part of the app is slower.

- **On a `@Configuration` class**: `@Lazy` on the class makes **all its `@Bean` methods** lazy.

**Follow-ups:**

### Q: What are the downsides of @Lazy?
- Configuration problems, like a missing bean, show up **only when the bean is first used**, maybe in production, instead of at startup.

- The **first call is slower**, because the bean is created at that moment. So I use `@Lazy` only where there's a real benefit, not everywhere.

---

### Q: Is a @Lazy bean the same as a prototype bean?
- **No.** A lazy singleton is still created **only once**, just later.

- A **prototype** bean is created **every time** it's requested. `@Lazy` changes **when** the bean is created, not **how many** are created.
