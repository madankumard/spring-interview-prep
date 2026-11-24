# Spring Boot — Notes

## Boot 1 — What is Spring Boot and how is it different from plain Spring?

**Answer:**
- **Spring Boot** is a layer on top of the Spring Framework that makes it **fast to build and run** Spring apps. It doesn't replace Spring: underneath it's the **same IoC container, MVC and data access**, but Boot does most of the setup for me.

- With **plain Spring**, I have to configure everything myself: the web server, the `DataSource`, Jackson, transactions, and the right **compatible versions** of every library. Then I usually package a **WAR** and deploy it to an external Tomcat.

- Boot replaces that with **auto-configuration**, **starters** and an **embedded server**. I add a dependency, run `main()`, and get a working app as a single **executable JAR**, with sensible defaults that I can override in `application.yaml`.

**Key points:**
- **Auto-configuration**: Boot looks at what's on the **classpath** and creates the usual beans for me. For example, if Spring MVC and Jackson are present, it configures the `DispatcherServlet` and JSON conversion, and it **backs off** if I define my own bean.

- **Starters**: one dependency brings a **tested set of libraries** that work together. For example, `spring-boot-starter-webmvc` pulls in Spring MVC, Jackson and Tomcat, and the **parent POM** manages all their versions.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

- **Embedded server and executable JAR**: Tomcat runs **inside** my app, so I start it with `java -jar shoplite.jar`. That's what makes Boot apps easy to put in **Docker** and run as microservices.

- **Externalized configuration**: settings live in `application.yaml`, environment variables or command-line arguments, so **the same JAR** runs in every environment with different config.

- **Production features**: **Actuator** gives health checks, metrics and info endpoints out of the box, which plain Spring doesn't have.

- **Opinionated defaults**: Boot picks common choices, like **Tomcat**, **HikariCP** and **Hibernate**, but every default can be **overridden or excluded**.

## Boot 2 — What happens step by step in SpringApplication.run()?

**Answer:**
- When I call `SpringApplication.run()` in `main()`, Boot first **reads all the configuration**: `application.yaml`, environment variables, command-line arguments and the active profiles. Then it prints the banner.

- Next it **creates the Spring container** (the `ApplicationContext`). It **scans my packages** for beans, applies **auto-configuration**, creates all the **singleton beans**, and **starts the embedded Tomcat** on port 8080.

- Finally it **runs any `CommandLineRunner` beans** and marks the app as **ready** to take requests. If something fails along the way, startup stops with an **"APPLICATION FAILED TO START"** message explaining the problem.

**Key points:**
- **The short version**: read config → create the container → scan and auto-configure → create beans → start Tomcat → run runners → ready.

- **Config comes first**: because the configuration is loaded **before any bean is created**, every bean can use `@Value` or `@ConfigurationProperties` values right away.

- **Creating beans is the biggest step**: this is where the whole **bean lifecycle** from Core 5 happens, and where most startup errors appear, like a **missing bean** or a **circular dependency**.

- **Runners and the ready event**: `CommandLineRunner` beans run **after** everything is created and Tomcat is up. After that, Boot publishes **`ApplicationReadyEvent`**, which I can listen to for "run this once the app is ready".
```java
@EventListener(ApplicationReadyEvent.class)
void onReady() { log.info("ShopLite is ready"); }
```

- **Readable failures**: Boot turns common errors, like **"port 8080 already in use"**, into a short **Description** and **Action** instead of only a long stack trace.

**Follow-ups:**

### Q: If a CommandLineRunner throws an exception, what happens?
- The app **fails to start** and shuts down, because runners are part of startup.

- That's why runners suit **quick startup tasks**, like loading demo data, and not long-running work.
