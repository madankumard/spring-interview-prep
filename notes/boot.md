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
