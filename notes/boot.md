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

## Boot 3 — What @SpringBootApplication combines and what each part does

**Answer:**
- **`@SpringBootApplication`** is a shortcut that combines **three annotations**: `@SpringBootConfiguration`, `@EnableAutoConfiguration` and `@ComponentScan`. I put it on the **main class**, and it turns that class into the starting point of the app.

- **`@SpringBootConfiguration`** marks the class as a **configuration class**, so I can declare `@Bean` methods in it. **`@EnableAutoConfiguration`** turns on Boot's **auto-configuration**, which creates beans like Tomcat, Jackson and the `DataSource` based on what's on the classpath.

- **`@ComponentScan`** scans the **package of the main class and all its sub-packages** for `@Component`, `@Service` and similar classes. That's why the main class should sit in the **root package**, like `com.shoplite`.

**Key points:**
- **The three parts**: writing them out by hand gives the same result as `@SpringBootApplication`.
```java
@SpringBootConfiguration      // a @Configuration class
@EnableAutoConfiguration      // Boot's auto-configuration
@ComponentScan                // scan this package and below
public class ShopliteApplication { ... }
```

- **`@SpringBootConfiguration`**: it's a special version of **`@Configuration`**. Boot's tests use it to **find the main configuration** automatically, which is why there should be only one per app.

- **`@EnableAutoConfiguration`**: without it, Boot creates **none of its default beans**. A web app then fails to start, because there's **no embedded server** configured.

- **`@ComponentScan`**: classes **outside** the main class's package are **not found**. I can widen the scan with **`scanBasePackages`**, but it's simpler to keep everything under the root package.
```java
@SpringBootApplication(scanBasePackages = {"com.shoplite", "com.partner"})
```

- **Excluding an auto-configuration**: the `exclude` attribute switches off one specific auto-configuration, for example a `DataSource` when the app doesn't use a database yet.

**Follow-ups:**

### Q: Why should the main class be in the root package?
- Because component scanning starts from the **main class's package**. If it sits in a sub-package, like `com.shoplite.app`, then beans in `com.shoplite.order` are **not found**.

- Putting it in the root package means **everything below it** is picked up without any extra config.

---

### Q: Can I have more than one @SpringBootApplication?
- **There should be only one.** Two of them scan overlapping packages and confuse tests, which look for **a single `@SpringBootConfiguration`**.

- For extra configuration, I use normal **`@Configuration`** classes instead.

## Boot 4 — How auto-configuration works (AutoConfiguration.imports, @Conditional*)

**Answer:**
- **Auto-configuration** is a set of normal **configuration classes** that Boot ships inside its jars. Each one knows how to set up one technology, like Tomcat, Jackson or a `DataSource`.

- `@EnableAutoConfiguration` finds them through a list file in every jar: **`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`**. Boot reads all those files and loads every class listed there as a **candidate**.

- Each candidate is guarded by **`@Conditional` annotations**, such as "only if this class is on the classpath" or "only if the user hasn't defined this bean". So only the configs that **match my app** actually create beans, and my own beans **win** because auto-configs are processed **after** my configuration.

**Key points:**
- **The imports file**: it's a plain text file with one **fully qualified class name** per line. It replaced the old **`spring.factories`** mechanism, which no longer works for auto-configuration since **Boot 3**, so that's the outdated answer to avoid.
```text
com.acme.greeting.GreetingAutoConfiguration
```

- **`@AutoConfiguration`**: it marks a class as an auto-configuration. It's a `@Configuration` that can also say **`before`** / **`after`** to order itself relative to other auto-configs.
```java
@AutoConfiguration
@ConditionalOnWebApplication
public class GreetingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    GreetingService greetingService() { return new GreetingService(); }
}
```

- **The common conditions**:
  - **`@ConditionalOnClass`**: a library is on the classpath. This is how adding a starter "switches on" a feature.
  - **`@ConditionalOnMissingBean`**: the user hasn't defined this bean already. This is what makes Boot **back off**.
  - **`@ConditionalOnProperty`** / **`@ConditionalOnBooleanProperty`**: a property has a certain value, so I can switch a feature on or off in `application.yaml`.
  - **`@ConditionalOnWebApplication`**: the app is a web app.

- **Seeing the decisions**: running with **`debug: true`** prints the **Conditions Evaluation Report** at startup. It lists **positive matches** (applied) and **negative matches** (skipped), each with the reason, and the Actuator **`/actuator/conditions`** endpoint shows the same at runtime.

- **Boot 4 change**: auto-configuration is now split into **many small modules**, one per technology, like `spring-boot-webmvc`, instead of one big `spring-boot-autoconfigure` jar. The mechanism is the same, but the app only carries the auto-configs for the starters it uses.

**Follow-ups:**

### Q: How do you find out why an auto-configuration did or didn't apply?
- I start the app with **`--debug`** or `debug: true` and read the **Conditions Evaluation Report**. Every auto-config is listed with the exact condition that matched or failed.

- In a running app, I use the Actuator **`/actuator/conditions`** endpoint, which returns the same report as JSON.

---

### Q: What's the difference between spring.factories and AutoConfiguration.imports?
- Older Boot versions listed auto-configs under a key in **`META-INF/spring.factories`**. **Boot 2.7** introduced the dedicated **`AutoConfiguration.imports`** file, and **Boot 3** removed auto-config support from `spring.factories`.

- `spring.factories` still exists, but only for other extension points, like **`EnvironmentPostProcessor`**.

---

### Q: Why shouldn't an auto-configuration class be picked up by component scanning?
- If it's scanned, it loads as **normal configuration**, too early and **before my beans** are registered. Then `@ConditionalOnMissingBean` can't see my beans and **won't back off** correctly.

- That's why auto-configs live in their **own package** and are loaded **only** through the imports file.

## Boot 5 — Embedded servers — swap Tomcat for Jetty, change port, disable web server

**Answer:**
- Spring Boot runs the web server **inside the application**, so I start the app with `java -jar` instead of deploying a WAR to an external server. **Tomcat** is the default, because `spring-boot-starter-webmvc` pulls in `spring-boot-starter-tomcat`.

- To switch to **Jetty**, I **exclude the Tomcat starter** from the web starter and add **`spring-boot-starter-jetty`**. Auto-configuration sees Jetty on the classpath and creates a Jetty server instead, with no code changes.

- The port and other settings come from **`server.*` properties**, like `server.port`. To run **without any web server**, for example a batch job or a CLI tool, I set **`spring.main.web-application-type=none`**.

**Key points:**
- **Swapping the server is a dependency change**: Boot picks the server from **what's on the classpath**, so the swap happens entirely in the POM.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jetty</artifactId>
</dependency>
```

- **Undertow is gone in Boot 4**: Undertow doesn't support **Servlet 6.1**, so Boot 4 dropped it. The choice for servlet apps is now **Tomcat or Jetty**, so "use Undertow for performance" is an outdated answer.

- **Changing the port**: I set `server.port` in `application.yaml`, an environment variable `SERVER_PORT`, or a command-line argument `--server.port=9090`. **`server.port=0`** picks a **random free port**, which is useful in tests and when running several instances.
```yaml
server:
  port: 8081
```

- **Disabling the web server**: `web-application-type: none` starts the Spring container **without** Tomcat. The app runs its runners and then **exits**, because nothing keeps it alive.
```yaml
spring:
  main:
    web-application-type: none
```

- **Customizing in code**: when a property isn't enough, a **`WebServerFactoryCustomizer`** bean can change the server factory, like the port or the error pages. Properties are still the **first choice**.
```java
@Bean
WebServerFactoryCustomizer<ConfigurableWebServerFactory> portCustomizer() {
    return factory -> factory.setPort(9091);
}
```

**Follow-ups:**

### Q: Tomcat vs Jetty — how do you choose?
- Both are **mature and fast** for normal apps, so I usually keep the **default Tomcat** unless there's a reason to change.

- Teams pick **Jetty** when they want a **smaller footprint** or already use it elsewhere. With **virtual threads** enabled, raw server performance matters even less.

---

### Q: How do you find the actual port when using server.port=0?
- I listen for the **`WebServerInitializedEvent`** and read `event.getWebServer().getPort()`. In tests, I inject it with **`@LocalServerPort`**.

---

### Q: Can a Boot app still be deployed as a WAR?
- **Yes.** I change the packaging to **`war`**, extend **`SpringBootServletInitializer`**, and mark the embedded Tomcat as **`provided`** so it isn't bundled.

- Today it's rarely done, because an **executable JAR in a container** is simpler to run and scale.

## Boot 6 — Externalized config precedence (properties, YAML, env vars, CLI args)

**Answer:**
- Spring Boot reads configuration from **many sources** at once and merges them into one **`Environment`**. When the same property is set in several places, the source with **higher precedence wins**.

- The short order, **highest first**: **command-line arguments**, then **Java system properties** (`-D`), then **OS environment variables**, then the **config files**. Among the files, **outside the jar beats inside**, and in the same place a **profile file** (`application-prod.yaml`) beats plain `application.yaml`. Below those come `@PropertySource` and defaults.

- The idea is that **the closer a setting is to the moment you run the app, the stronger it is**. So the JAR carries sensible defaults, and each environment overrides them from outside without rebuilding.

**Key points:**
- **The order to remember** (high → low):
  1. Command-line args: `--server.port=9090`
  2. `SPRING_APPLICATION_JSON`
  3. Java system properties: `-Dserver.port=9090`
  4. OS environment variables: `SERVER_PORT=9090`
  5. `application-{profile}.yaml` **outside** the jar
  6. `application.yaml` **outside** the jar
  7. `application-{profile}.yaml` **inside** the jar
  8. `application.yaml` **inside** the jar
  9. `@PropertySource` on a `@Configuration` class
  10. Default properties

- **Profile files beat the base file in the same place**: with `prod` active, a value in `application-prod.yaml` **overrides** the same key in `application.yaml`. Keys that aren't overridden are still taken from the base file.

- **Outside the jar beats inside**: an `application.yaml` in a **`config/` folder** next to the jar, or in the **current directory**, overrides the files packaged in `src/main/resources`, **even the profile files inside the jar**. That's how ops teams change config without rebuilding.

- **Environment variable names**: thanks to **relaxed binding**, `SHOPLITE_STORE_NAME` maps to `shoplite.store.name`. The rule: uppercase the key, replace dots with underscores, and remove dashes.

- **`.properties` vs `.yaml`**: both work the same way. If both exist **in the same location**, `.properties` wins, so it's best to use **just one format**.

**Follow-ups:**

### Q: In Kubernetes, how do you usually override config?
- With **environment variables** from a ConfigMap or Secret, because they beat everything in the packaged `application.yaml`. The same image then runs in every environment.

---

### Q: How do you check which source a property actually came from?
- The Actuator **`/actuator/env`** endpoint lists every property source **in precedence order** and shows which one supplied each value. Values that look secret are masked.

## Boot 7 — spring.config.import and loading external config/secrets

**Answer:**
- **`spring.config.import`** lets one config file **pull in more config** from somewhere else, like another file, a folder of secrets, or a config server. I write it in `application.yaml`, and Boot loads the imported sources as if they were part of that file.

- It's mainly used to keep **secrets out of the jar and out of Git**. The packaged `application.yaml` only says **where** to load secrets from, and the actual passwords and API keys live outside, on the server or in Kubernetes.

- Imported values **override** the values in the file that imports them. A missing import **fails startup**, unless I prefix it with **`optional:`**.

**Key points:**
- **Importing a file**: the path is relative to the **working directory**. Several imports can be listed, and `optional:` means "fine if it's not there", which is handy in development.
```yaml
spring:
  config:
    import:
      - optional:file:./secrets/payment.yaml
```

- **Config trees for Kubernetes and Docker secrets**: secrets are usually **mounted as files**, one file per value. With **`configtree:`**, each **file name becomes the property name** and the file content becomes the value, so a file `/run/secrets/shoplite.payment.webhook-secret` sets `shoplite.payment.webhook-secret`.
```yaml
spring:
  config:
    import: optional:configtree:/run/secrets/
```

- **Placeholders for environment variables**: another common way is to refer to an env var with a **default for development**. The secret then comes from the deployment platform, and nothing sensitive is in the file.
```yaml
shoplite:
  payment:
    api-key: ${PAYMENT_API_KEY:dev-key}
```

- **Other import types**: Spring Cloud adds **`configserver:`** for Spring Cloud Config and **`vault:`** for HashiCorp Vault. It's the same `spring.config.import` mechanism, so the app code doesn't change.

- **What changed**: `spring.config.import` arrived in **Boot 2.4** and replaced the old **`bootstrap.yml`** approach for Spring Cloud Config. Using `bootstrap.yml` is now the outdated answer.

**Follow-ups:**

### Q: How do you keep secrets out of Git?
- I **never put real secrets** in `application.yaml`. The file only has **placeholders** or **imports**, and the real values come from **environment variables, mounted secret files or a vault**.

- Local secret files, like `secrets/`, go in **`.gitignore`**.

---

### Q: What happens if an imported file is missing?
- Without `optional:`, startup **fails** with a `ConfigDataResourceNotFoundException`. That's good for production, where a missing secrets file is a real problem.

- With **`optional:`**, Boot **skips it silently**, which suits local development, where the file may not exist.

---

### Q: Why is configtree better than environment variables for secrets?
- Env vars can **leak** through process listings, crash dumps or logs that print the environment. **Mounted files** are only readable by the app and can be **rotated** by Kubernetes without rebuilding the image.

## Boot 8 — Actuator — key endpoints, exposing, securing

**Answer:**
- **Actuator** adds **production-ready endpoints** to a Boot app, under `/actuator`, for checking its **health**, **metrics**, **config** and **logs** while it runs. I add `spring-boot-starter-actuator`, and Boot auto-configures the endpoints.

- For safety, only **`/actuator/health`** is **exposed over HTTP** by default. I choose what else to expose with **`management.endpoints.web.exposure.include`**, and I expose only what I really need.

- Endpoints like `env`, `heapdump` or `loggers` reveal **internals** or can **change** the app, so I **secure** them. Usually that means **Spring Security** with an admin role, or putting Actuator on a **separate management port** that only the internal network can reach.

**Key points:**
- **The endpoints worth knowing**:
  - **`health`**: is the app OK, including the database, disk space and custom checks. Load balancers and Kubernetes use it.
  - **`info`**: app name, version, build and git details.
  - **`metrics`** / **`prometheus`**: JVM, HTTP and custom metrics for monitoring.
  - **`loggers`**: view and **change log levels at runtime**.
  - **`env`** / **`configprops`**: the resolved configuration, with secret values **masked**.
  - **`beans`**, **`conditions`**, **`mappings`**: debugging which beans, auto-configs and URLs exist.
  - **`threaddump`** / **`heapdump`**: diagnosing hangs and memory problems.

- **Exposing endpoints**: an endpoint must be **available** and also **exposed** over a transport (HTTP or JMX). Exposure is a simple list in config.
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
  endpoint:
    health:
      show-details: when-authorized
```

- **Custom health indicator**: I implement **`HealthIndicator`** to add my own check, like "is the payment provider reachable". If it reports **DOWN**, the overall status becomes DOWN and `/actuator/health` returns **HTTP 503**.
```java
@Component
public class PaymentGatewayHealthIndicator implements HealthIndicator {
    public Health health() {
        return Health.up().withDetail("provider", "FakePay").build();
    }
}
```

- **Securing with Spring Security**: **`EndpointRequest`** matches Actuator URLs. So I can leave `health` and `info` public and require a role for everything else.
```java
http.authorizeHttpRequests(auth -> auth
        .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ACTUATOR")
        .anyRequest().permitAll());
```

- **Separate management port**: **`management.server.port=9090`** serves Actuator on its own port. The firewall or Kubernetes then keeps that port **internal**, while the API port stays public.

- **What changed**: since **Boot 3.0**, `env` and `configprops` **mask all values** by default (`show-values: never`). Since **Boot 3.4**, endpoint access is set with **`management.endpoint.<id>.access`** (`none`, `read-only`, `unrestricted`), replacing the old `enabled` flags.

**Follow-ups:**

### Q: Why is only health exposed by default?
- Because the other endpoints can **leak sensitive data**, like config values, heap contents or bean structure, or can **change the app**, like log levels. Boot is **secure by default**, so I have to opt in to each one.

---

### Q: How do you write a custom Actuator endpoint?
- I create a bean annotated with **`@Endpoint(id = "shopstats")`** and add methods with **`@ReadOperation`** (GET), **`@WriteOperation`** (POST) or **`@DeleteOperation`**. It then appears at `/actuator/shopstats` once it's exposed.

---

### Q: What does show-details do on the health endpoint?
- It controls whether `/actuator/health` shows only **`{"status":"UP"}`** or also each **component's details**, like the database and disk space. **`when-authorized`** shows details only to **logged-in users with the right role**, which is the usual production setting.

---

### Q: How do you change a log level without restarting?
- I **POST** to **`/actuator/loggers/<logger-name>`** with a body like `{"configuredLevel": "DEBUG"}`. The change applies **immediately**, but it's lost on restart.

## Boot 9 — Observability with Micrometer — metrics, tracing, trace IDs in logs

**Answer:**
- **Observability** means I can understand what a running app is doing from the outside, using three signals: **metrics** (numbers over time), **traces** (the path of one request) and **logs**. In Spring Boot, all three are built on **Micrometer**, which works like **SLF4J for monitoring**: I write against one API, and it sends data to whatever backend I use.

- **Most of it needs no code.** Once Actuator and the OpenTelemetry starter are on the classpath, Boot **automatically** measures and traces HTTP requests, database calls, `RestClient` calls and messaging. The only code I usually write is a **business metric**, like an orders-placed counter in the service.

- For **tracing**, I add the **OpenTelemetry** starter. Every request then gets a **trace ID**, each step gets a **span ID**, and Boot **puts both into every log line**, so I can find all the logs of one request across services and see the timing in a tool like **Zipkin, Jaeger or Grafana Tempo**.

**Key points:**
- **Metric types**: a **counter** only goes up (orders placed), a **timer** measures how long things take and how often (request duration), and a **gauge** shows a current value (items in the queue). **Tags** split a metric by dimension, like `status=200`.

- **A business metric in practice**: I inject the `MeterRegistry` into the **service** and count the business event. That's typically the only observability code in the app.
```java
public OrderService(MeterRegistry registry) {
    this.ordersPlaced = registry.counter("shoplite.orders.placed");
}

public void placeOrder(...) {
    // business logic
    ordersPlaced.increment();
}
```

- **The Observation API**: an **`Observation`** creates **a timer metric and a trace span at the same time**, and it's what Spring uses internally for HTTP, JDBC and messaging. When I want my own method timed and traced, I just annotate it with **`@Observed`** (it needs the AOP starter) instead of calling the API by hand.
```java
@Observed(name = "shoplite.order.place")
public void placeOrder(...) { ... }
```

- **Trace and span IDs**: the **trace ID** is the same for the **whole request**, even across services, because it travels in the **`traceparent` HTTP header**. Each step gets its own **span ID**. With tracing on, Boot's default log pattern adds **`[traceId-spanId]`** to every line automatically.

- **Sampling**: by default only **10% of requests** are traced (`management.tracing.sampling.probability: 0.1`) to keep the overhead low. In development, I set it to **1.0** to trace everything.
```yaml
management:
  tracing:
    sampling:
      probability: 1.0
```

- **Getting data out, pull vs push**: with **Prometheus**, the monitoring system **pulls** from `/actuator/prometheus`. With **OTLP**, the app **pushes** metrics and traces to an **OpenTelemetry Collector**, which forwards them to tools like Grafana. The OTel starter turns on the metrics push by default, so locally, with no collector, I switch it off.
```yaml
management:
  otlp:
    metrics:
      export:
        enabled: false
```

- **Dependencies in Boot 4**: metrics come with **Actuator**. Tracing needs **`spring-boot-starter-opentelemetry`**, and Prometheus needs **`micrometer-registry-prometheus`**. **Spring Cloud Sleuth** is gone since Boot 3, replaced by **Micrometer Tracing**, so "use Sleuth" is the outdated answer.

**Follow-ups:**

### Q: What's the difference between metrics, traces and logs?
- **Metrics** are **cheap aggregated numbers** that tell me **something is wrong**, like the error rate going up. **Traces** show **where** it's wrong, meaning which service or call in a request is slow.

- **Logs** explain **why**, with the details. The **trace ID** links all three together.

---

### Q: Why should tags have low cardinality?
- Each **unique tag value** creates a **separate time series**. A tag like `userId` or `orderId` creates millions of them and can **crash the monitoring system**.

- That's why `lowCardinalityKeyValue` is used for metrics tags, while `highCardinalityKeyValue` goes **only on spans**.

---

### Q: How does the trace ID get passed between microservices?
- Spring's instrumented clients, like **`RestClient`**, **`WebClient`** and Kafka, automatically add the **W3C `traceparent` header** to outgoing calls. The receiving service reads it and **continues the same trace**.

- This only works if the client is built from **Boot's auto-configured builder**, like `RestClient.Builder`, not with `new`.

## Boot 10 — Logging — runtime level changes, log groups, Logback

**Answer:**
- Spring Boot logs through **SLF4J**, the logging API my code uses, and **Logback**, the default implementation underneath. Both come with every starter, so logging works out of the box with a sensible console format.

- I control logging mostly with **properties**: **`logging.level.*`** sets levels per package, **`logging.group.*`** bundles packages under one name, and **`logging.file.name`** writes to a file. I only need a **`logback-spring.xml`** for advanced setups.

- In production I can **change log levels at runtime** through the Actuator **`loggers`** endpoint without restarting. Since **Boot 3.4**, I can also switch to **structured JSON logs** with one property, which log tools like ELK, Loki or Datadog read directly.

**Key points:**
- **Log levels**: from most to least detailed, **TRACE, DEBUG, INFO, WARN, ERROR**. The default is **INFO**, so `log.debug(...)` lines are hidden until I lower the level for that package. Using `{}` placeholders means the message is only built when the level is on.
```java
private static final Logger log = LoggerFactory.getLogger(ProductService.class);
log.debug("Fetching product {}", id);
```

- **Levels and groups in config**: a level applies to a package and **everything below it**. A **group** lets me switch several related packages together, and Boot has built-in groups **`web`** and **`sql`**.
```yaml
logging:
  group:
    shoplite-sales: com.shoplite.order, com.shoplite.product
  level:
    root: INFO
    shoplite-sales: DEBUG
    web: DEBUG
```

- **Runtime changes**: a POST to **`/actuator/loggers/<logger-or-group>`** changes the level **immediately**, which is great for debugging a live problem. It's **lost on restart**, and the endpoint must be secured.

- **Log files**: `logging.file.name` writes to a file, and Logback **rolls** it over by size, by default at **10 MB**, keeping **7 days**. Both can be changed with `logging.logback.rollingpolicy.*`. In containers, I usually just log to the **console** and let the platform collect it.

- **Structured logging (Boot 3.4+)**: **`logging.structured.format.console: ecs`** (or `logstash`, `gelf`) prints each line as **JSON**, including the trace ID. Log tools can then **filter by field** instead of parsing text.
```yaml
logging:
  structured:
    format:
      console: ecs
```

- **`logback-spring.xml`**: when properties aren't enough, like custom appenders or different setups per profile, I add this file to `src/main/resources`. I use the **`-spring`** name rather than `logback.xml`, so Boot loads it **after** its own setup and I can use **`<springProfile>`** and **`<springProperty>`**.

**Follow-ups:**

### Q: Why use SLF4J instead of calling Logback directly?
- SLF4J is just an **API**, so my code doesn't depend on the logging implementation. I could switch to **Log4j2** by changing dependencies, without touching any code.

---

### Q: Why is logback-spring.xml preferred over logback.xml?
- `logback.xml` is loaded **by Logback itself**, too early, before Spring has read its configuration. `logback-spring.xml` is loaded **by Boot**, so it supports Spring features like **`<springProfile name="prod">`** and reading properties.

---

### Q: How do you debug one problem in production without flooding the logs?
- I set **only the relevant package or group** to DEBUG through **`/actuator/loggers`**, reproduce the issue, then set it back. The **trace ID** in each line lets me filter the logs of one request.

## Boot 11 — Graceful shutdown

**Answer:**
- **Graceful shutdown** means that when the app is told to stop, it **stops accepting new requests** but **lets the requests already running finish** before it shuts down. Without it, users in the middle of a request, like placing an order, get an **error or a broken connection**.

- Since **Boot 3.4** it's **on by default** (`server.shutdown=graceful`). Before that it was off, and I had to enable it myself, so "you must turn it on" is now the outdated answer.

- Boot waits up to **30 seconds** by default for active requests, configurable with **`spring.lifecycle.timeout-per-shutdown-phase`**. If requests are still running after that, Boot **stops anyway**. Only after the web server has stopped are the beans destroyed, which is when **`@PreDestroy`** methods run.

**Key points:**
- **The shutdown order**:
  1. The app gets a stop signal, like **SIGTERM** from Kubernetes or Docker, or Ctrl+C.
  2. The web server **stops taking new requests**.
  3. It **waits** for the active ones to finish.
  4. Spring **closes the context** and destroys the beans.

- **Configuration**: the timeout should be **shorter than the platform's kill timeout**, which is 30 seconds by default in Kubernetes. Otherwise the platform force-kills the app before Boot has finished.
```yaml
server:
  shutdown: graceful        # default since Boot 3.4; "immediate" turns it off
spring:
  lifecycle:
    timeout-per-shutdown-phase: 20s
```

- **Cleanup in beans**: `@PreDestroy` methods run **after** the requests have finished. That's the right place to close resources the app opened itself, like a client connection or a scheduler.
```java
@PreDestroy
void close() { log.info("Closing payment client"); }
```

- **Kubernetes**: on shutdown, Kubernetes sends **SIGTERM** and removes the pod from the Service **at the same time**. So a short **`preStop` sleep** is often added, giving the load balancer time to stop routing traffic before the app stops accepting it.

**Follow-ups:**

### Q: What happens to a request that's still running when the timeout runs out?
- Boot logs that the graceful shutdown was **aborted** and **closes the connection**, so that request fails. That's why the timeout must suit the **slowest normal request**, and long jobs belong in a queue, not in an HTTP request.

---

### Q: Does graceful shutdown work when the process is killed with kill -9?
- **No.** SIGKILL (`kill -9`) ends the JVM **instantly**, so no shutdown hooks run and nothing is graceful. It only works with signals the JVM can catch, like **SIGTERM** or **Ctrl+C**.

## Boot 12 — Virtual threads

**Answer:**
- **Virtual threads** are a **Java 21** feature: very lightweight threads managed by the JVM instead of the operating system. I can have **millions** of them, and when one **blocks** on I/O, like a database call or an HTTP call, the JVM parks it and frees the real OS thread for other work.

- In Spring Boot (**3.2+**) I turn them on with **one property**: `spring.threads.virtual.enabled=true`. Tomcat or Jetty then runs **every request on its own virtual thread**, and `@Async`, `@Scheduled` and message listeners use them too, with **no code changes**.

- They help **I/O-bound** apps that spend most of their time **waiting**, which describes most REST and database services. They **don't make CPU-heavy work faster**, because they still share the same CPU cores.

**Key points:**
- **Enabling them**: it's one line in config. Afterwards, request threads show up in logs with names like `tomcat-handler-0` instead of `http-nio-8080-exec-1`.
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

- **Why it matters**: with normal threads, Tomcat has a pool of **200 threads**, so 200 slow requests **block everyone else**. With virtual threads there's no such limit, so simple **blocking code scales** like reactive code, without WebFlux's complexity.

- **The new bottleneck is downstream**: the **database connection pool**, which HikariCP sizes at 10 by default, now limits how many requests really run at once. More threads don't help if they all **wait for a connection**.

- **Gotchas**:
  - **Pinning**: a virtual thread inside a **`synchronized`** block used to hold its OS thread. **JDK 24** fixed that, but native calls still pin.
  - **`ThreadLocal`** caches become wasteful when there are millions of short-lived threads.
  - Virtual threads are **daemon threads**, so an app with no web server, only `@Scheduled` jobs, may **exit right away**. **`spring.main.keep-alive=true`** fixes that.

**Follow-ups:**

### Q: Do virtual threads replace WebFlux?
- **For most apps, yes.** Blocking code on virtual threads now gives similar scalability with **simpler code, normal stack traces and easy debugging**.

- WebFlux still fits **streaming** and **backpressure** use cases, like server-sent events or many long-lived connections.

---

### Q: Should I use virtual threads for a CPU-heavy task?
- **No.** Virtual threads only help when threads **wait**. For CPU-bound work, like image processing or big calculations, a normal **fixed thread pool** sized to the number of cores is still the right tool.

## Boot 13 — Executable JAR internals, layered jars, Buildpacks

**Answer:**
- A Spring Boot **executable JAR** (a "**fat jar**") contains **my classes and all my dependency jars** in one file, so I run the app with just `java -jar app.jar`. Plain Java **can't load jars nested inside a jar**, so Boot adds its own small **launcher** that sets up a class loader for them and then calls my `main()`.

- The jar is also **layered**: it records which files are **dependencies** (rarely change) and which are **my code** (changes every build). When I build a Docker image, I put each group in its **own image layer**, so a code change only rebuilds and pushes a **few KB** instead of the whole 50+ MB.

- **Buildpacks** build a production-ready Docker image **without a Dockerfile**. I run `mvn spring-boot:build-image`, and it picks a JDK, uses the layers, and applies **memory and security best practices** automatically.

**Key points:**
- **Inside the jar**:
  - **`META-INF/MANIFEST.MF`**: `Main-Class` is Boot's **`JarLauncher`**, and `Start-Class` is **my** application class.
  - **`BOOT-INF/classes/`**: my compiled code and `application.yaml`.
  - **`BOOT-INF/lib/`**: every dependency jar, untouched.
  - **`BOOT-INF/layers.idx`**: the list of which files go in which layer.
```text
Main-Class: org.springframework.boot.loader.launch.JarLauncher
Start-Class: com.shoplite.ShopliteApplication
```

- **Why `java -cp app.jar com.shoplite.ShopliteApplication` fails**: my classes sit under `BOOT-INF/classes`, not at the jar root, and the libraries are nested jars. Only the **launcher** knows how to find them.

- **The four layers**, from least to most often changed: **`dependencies`**, **`spring-boot-loader`**, **`snapshot-dependencies`** and **`application`**. Docker **caches** the unchanged lower layers.

- **Layered Dockerfile**: a first stage **extracts** the layers with Boot's **`jarmode=tools`**, and a second stage copies them in **separate `COPY` steps**, so each becomes its own cached layer.
```dockerfile
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./
```

- **Buildpacks vs Dockerfile**: Buildpacks need **no Dockerfile to maintain** and give a secure, well-tuned image, which suits most teams. I write a Dockerfile when I need **full control**, like a special base image or extra OS tools.

- **What changed**: in **Boot 3.2** the launcher moved to the **`org.springframework.boot.loader.launch`** package. **Boot 3.3** replaced the old `jarmode=layertools` with **`jarmode=tools`**, so `layertools` is the outdated answer.

**Follow-ups:**

### Q: Why not just copy the fat jar into the image with one COPY?
- Then the **whole jar is one layer**, so every tiny code change creates a **new 50+ MB layer**. Builds, pushes and pulls get slower and the registry fills up.

- With layers, only the small **`application`** layer changes on most builds.

---

### Q: Fat jar vs WAR — when would you still build a WAR?
- Only when the company must deploy to an **existing external server**, like a shared Tomcat or WebLogic. For containers and the cloud, the **executable jar** is the standard.
