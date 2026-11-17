# Spring Core — Notes

## Core 1 — What is Spring?

**Answer:**
- Spring is an **open-source framework** for building enterprise Java applications. It gives you a structured way to build large applications without writing all the **infrastructure** yourself.

- At its heart is the **IoC container**. The container creates my objects, which Spring calls **beans**, **wires their dependencies** together, and manages their **lifecycle** from creation to destruction.

- On top of the container, Spring provides **ready-made infrastructure** for the things almost every application needs: transactions, data access, web and REST APIs, security, messaging, and testing support.

- In plain Java I would have to create every object with `new` and hand-write all of that plumbing myself. With Spring I focus on **business logic**, declare what each class needs, and the container connects everything.

- The end result is **loosely coupled** code that is easy to change and **easy to test**, because classes depend on **interfaces** rather than on concrete implementations.

**Key points:**
- **IoC / DI:** The container creates objects and **injects their dependencies**, so a class never has to call `new` on its collaborators. Because classes depend on interfaces, I can **swap an implementation** or replace it with a **mock** without touching the class itself.

```java
// Plain Java — you wire it, implementation is hard-coded
var orderService = new OrderService(new EmailNotificationService());

// Spring — declare beans, the container wires them
@Service
public class EmailNotificationService implements NotificationService { ... }

@Service
public class OrderService {
    private final NotificationService notificationService;

    public OrderService(NotificationService notificationService) { // injected by Spring
        this.notificationService = notificationService;
    }
}
```

- **Declarative cross-cutting concerns:** Features like `@Transactional`, `@Cacheable`, `@Async` and `@PreAuthorize` are applied with a **single annotation**. Spring implements them with **AOP proxies**, so I don't write repetitive begin/commit/rollback or security-check code in every method.

- **Removes boilerplate:** **JdbcTemplate** and **Spring Data** remove the connection, statement and result-set handling of raw JDBC. **Spring MVC** replaces low-level servlet code with simple annotated controller methods.

- **Modular ecosystem:** Spring is split into **modules and projects** such as Core, MVC/WebFlux, Data, Security, Cloud and Batch. I only pull in what the application actually needs.

- **Testability:** With **constructor injection** I can unit-test a class with plain `new` and mocks, **without starting Spring** at all. For integration testing, Spring Test offers **slice tests** and full application-context tests.

- **Non-invasive:** My classes stay **POJOs** and don't have to extend framework base classes. That keeps business code clean and mostly **independent of Spring**.

- **Spring Boot:** Boot sits on top of Spring and adds **auto-configuration**, **starter dependencies**, an **embedded server** and production features like **Actuator**. It removes most of the manual setup that classic Spring required.

- **Current versions:** The current generation is **Spring Framework 7** with **Spring Boot 4**. It has a **Java 17 baseline**, uses **Jakarta EE 11**, adds JSpecify null-safety, and has built-in API versioning and retry/concurrency-limit support in core.

## Core 2 — IoC and DI — how they enable loose coupling

**Answer:**
- **Inversion of Control** is a **design principle** where control over **creating and wiring objects** moves out of my code and into a framework. Instead of my class deciding which objects it needs and building them, the **container** does that for it.

- **Dependency Injection** is the **pattern Spring uses to implement IoC**. A class declares its dependencies, usually as **constructor parameters**, and the container passes them in from outside.

- The key to **loose coupling** is that classes depend on **interfaces, not concrete classes**. For example, my `OrderService` depends on a `NotificationService` interface and doesn't know whether it's email or SMS behind it.

- Which implementation is actually used is decided in **configuration**, not inside the class. That means I can **swap** email for SMS without changing a single line of `OrderService`.

- It also makes **testing** much easier. In a unit test I simply pass a **fake or a mock** through the constructor, without starting Spring at all.

**Key points:**
- **Tight coupling (no DI):** When a class creates its own dependency with `new`, it is **locked to that one implementation**. To change it I have to edit the class, and I can't easily replace it with a fake in tests.

```java
public class OrderService {
    private final EmailNotificationService notifier = new EmailNotificationService(); // hard-wired
}
```

- **Loose coupling (DI):** The class depends on the **interface** and receives it through the **constructor**. The container decides which bean to pass in, so the class only knows about the **contract**.

```java
@Service
public class OrderService {
    private final NotificationService notificationService;

    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```

- **Swapping implementations:** To switch to SMS I add a new implementation and make it the **active bean**. `OrderService` **doesn't change at all**, which is exactly what loose coupling means.

```java
@Service
public class SmsNotificationService implements NotificationService { ... }
// remove @Service from EmailNotificationService → OrderService now gets SMS
```

- **Testability:** Because the dependency comes in through the constructor, a unit test can **pass a fake directly**. **No Spring context** and no mocking framework are needed, so the test runs in milliseconds.

```java
List<String> sent = new ArrayList<>();
NotificationService fake = (to, msg) -> sent.add(to + ":" + msg);
new OrderService(fake).placeOrder("bob@shoplite.com", "Mouse");
assertThat(sent).containsExactly("bob@shoplite.com:Order placed for Mouse");
```

- **How the container knows what to wire:** The container reads **configuration metadata** to decide what to create and inject. In modern apps that's **stereotype annotations** like `@Service` found by **component scanning** plus `@Bean` methods; older apps used XML.

- **Hollywood principle:** IoC is often summed up as **"don't call us, we'll call you."** The framework calls my code at the right time, rather than my code controlling the flow.

- **Relation to SOLID:** DI is the practical technique behind the **Dependency Inversion Principle**, the "D" in SOLID. The principle says to **depend on abstractions**, and DI is how those abstractions get supplied at runtime.

**Follow-ups:**

### Q: IoC vs DI, what's the difference?
- IoC is the **broad principle** that a framework, not my code, controls object creation and program flow. DI is **one specific way** to achieve it, where the dependencies a class needs are handed to it from outside.

- A simple way to say it: IoC is the **what** (the framework is in control) and DI is the **how** (dependencies are passed into the class). In Spring, the **IoC container is the thing that performs DI**.

- Other forms of IoC include the **service locator** pattern, the **template method** pattern and **event callbacks**. Spring uses several of these internally, but for wiring objects it relies on DI.

---

### Q: DI vs Dependency Inversion Principle?
- The Dependency Inversion Principle is a **design guideline**, the "D" in SOLID. It says high-level modules should depend on **abstractions**, not on low-level details.

- Dependency Injection is the **mechanism** that actually supplies those abstractions at runtime. So DIP tells you **what to aim for**, and DI is **how you do it**.

- In ShopLite, `OrderService` depending on the `NotificationService` **interface** is DIP. Spring passing an `EmailNotificationService` into its constructor is DI.

- They are related but independent. I can inject a **concrete class** and still be using DI, but I would be **violating DIP** because the class now depends on a detail.

---

### Q: Is `context.getBean(OrderService.class)` dependency injection?
- No, that's **dependency lookup**, also known as the **service locator** pattern. The class actively **pulls** its dependency from the container instead of having it pushed in.

- It **couples business code to Spring's API** and **hides dependencies**: the constructor no longer shows what the class needs, so you have to read the method bodies to find out. It also makes testing harder and moves missing-bean errors from **startup to runtime**.

---

### Q: What types of DI does Spring support?
- **Constructor injection:** dependencies are declared as **constructor parameters**. This is the recommended style because fields can be **final**, required dependencies are **explicit**, and the class can be created in a test with plain `new` (see Core 9).

- **Setter injection:** Spring calls a **setter method** annotated with `@Autowired` after creating the object. It suits **optional** dependencies or ones that may be changed later, but the object can exist for a moment without them.

- **Field injection:** `@Autowired` is placed **directly on a field**. It's the shortest to write, but the field can't be final, the dependencies are hidden, and the class is **hard to test without Spring**, so it's discouraged in production code.

---

### Q: What if two beans implement the same interface?
- Spring injects **by type**, and with two candidates it can't decide which one to use. Startup fails with `NoUniqueBeanDefinitionException`.

- **`@Primary`** on one implementation marks it as the **default** whenever there is a choice. **`@Qualifier("beanName")`** at the injection point picks **one specific bean by name** and overrides `@Primary` (see Core 16).

- If I actually want **all** implementations, I can inject a `List<NotificationService>` and Spring passes every matching bean.

## Core 3 — What is a Spring bean? What does the IoC container do?

**Answer:**
- A Spring bean is simply an **object that the Spring IoC container creates and manages**. It's a normal Java object; what makes it a bean is that Spring **instantiates it, injects its dependencies and controls its lifecycle**, instead of my code calling `new`.

- The IoC container is represented by the **`ApplicationContext`**. At startup it reads my **configuration metadata**, meaning annotations like `@Service` and `@Bean` methods, and builds a **bean definition** for every bean, which is basically a recipe for how to create it.

- From those definitions the container **instantiates the beans** and **wires them together** by injecting each bean's dependencies. By default every bean is a **singleton**, so there is one shared instance per container, and singletons are created **eagerly at startup**.

- The container also manages the **full lifecycle**. It runs initialization callbacks, wraps beans in **proxies** when features like `@Transactional` need them, and calls destroy callbacks when the application shuts down.

- **Not every object is a bean.** Things I create with `new`, such as entities and DTOs, are not managed by Spring, so they get no injection and no proxy features.

**Key points:**
- **Ways to define a bean:** The most common way is a **stereotype annotation** such as `@Component`, `@Service`, `@Repository` or `@Controller`, which **component scanning** picks up. For classes I don't own, such as a JDK or library class, I declare a **`@Bean` method** inside a `@Configuration` class.

```java
@Service
public class OrderService { ... }          // found by component scanning

@Configuration
public class AppConfig {
    @Bean
    Clock clock() {                         // third-party class registered as a bean
        return Clock.systemUTC();
    }
}
```

- **Bean definition:** A `BeanDefinition` is the **metadata** the container holds for each bean: its class, scope, whether it's lazy, its dependencies and its init/destroy methods. The container creates the actual objects **from these recipes**, which is why it can decide when and how many instances to create.

- **Bean names:** Every bean has a **unique name** in the container. For a scanned class the default is the **class name with a lowercase first letter**, so `OrderService` becomes `orderService`, and for a `@Bean` method it's the **method name**.

- **Singleton by default:** Asking the container twice for the same bean returns the **same instance**. This is a singleton **per container**, not the classic GoF singleton enforced by the class itself.

```java
OrderService a = context.getBean(OrderService.class);
OrderService b = context.getBean(OrderService.class);
// a == b → true
```

- **What the container does, in order:** It **loads bean definitions**, **instantiates** the beans, **injects dependencies**, runs **initialization** callbacks and post-processors (this is where proxies are created), keeps the beans **ready for use**, and finally runs **destroy callbacks** on shutdown.

- **In Spring Boot:** I never create the container myself. `SpringApplication.run()` creates the `ApplicationContext`, and most of the beans in it come from **auto-configuration**, not from my own code.

**Follow-ups:**

### Q: How can you define a bean?
- There are three main ways: **stereotype annotations** picked up by component scanning, **`@Bean` methods** in a `@Configuration` class, and **programmatic registration**.

- **Stereotype annotations** mark a class as a Spring-managed component and also describe its **role** in the application. `@Component` is the generic one, and `@Service` (business logic), `@Repository` (data access) and `@Controller` / `@RestController` (web layer) are **specializations** of it.

- **Component scanning** is what makes stereotypes work. Spring scans the base package and its sub-packages, finds every class carrying one of these annotations, and **registers it as a bean automatically**. I use this for classes I write myself.

- A **`@Bean` method** lives inside a `@Configuration` class. Spring calls the method and registers the **returned object** as a bean, which is the right choice for classes I **can't annotate**, such as JDK or library classes, or when building the object needs **custom logic**.

- **Programmatic registration** means registering beans in code, for example with the `BeanRegistrar` interface added in **Spring Framework 7**. It's useful when beans must be registered **dynamically**, for example in a loop or based on a condition. XML configuration still works but is legacy.

```java
public class WarehouseRegistrar implements BeanRegistrar {
    @Override
    public void register(BeanRegistry registry, Environment env) {
        String[] codes = env.getProperty("shoplite.warehouses", String[].class, new String[0]);
        for (String code : codes) {                     // one bean per configured code
            registry.registerBean(code + "Warehouse", Warehouse.class,
                    spec -> spec.supplier(context -> new Warehouse(code)));
        }
    }
}

@Configuration
@Import(WarehouseRegistrar.class)                         // activates the registrar
public class AppConfig { ... }
```

---

### Q: What is a BeanDefinition?
- It's the container's **internal recipe** for a bean. It holds the bean's class, scope, lazy flag, constructor arguments, dependencies and init/destroy methods.

- The relationship is like a **class and its objects**: the definition describes how to build the bean, and the container builds one or many instances from it depending on the scope.

- The container first collects **all bean definitions** and **only then creates instances** from them. That separation is what lets a `BeanFactoryPostProcessor` modify definitions before any bean exists, which is how property placeholders like `${...}` get resolved.

---

### Q: What are the default name and scope of a bean?
- The default name is the **class name with the first letter lowercased**, so `OrderService` becomes `orderService`. For a `@Bean` method the default name is the **method name**.

- I can override the name, for example `@Service("myOrderService")` or `@Bean("utcClock")`. Names matter when I select a bean with **`@Qualifier`**, and two beans with the **same name** cause a conflict at startup.

- The default scope is **singleton**, which means **one instance per `ApplicationContext`** shared by everyone who injects it. Other scopes are **prototype** (a new instance per request to the container) and the web scopes **request**, **session** and **application**.

---

### Q: When are beans created?
- **Singleton beans are created eagerly at startup**, when the context refreshes. Spring creates a bean's **dependencies first**, then the bean itself.

- The benefit of eager creation is that wiring and configuration errors show up **immediately at startup** instead of later in production when a user hits that code path.

- A bean marked **`@Lazy`** is created on **first use**, and a **prototype** bean is created **every time it's requested**. Boot also has `spring.main.lazy-initialization=true` to make everything lazy, which speeds up startup but **delays error detection**.

---

### Q: What happens if I create a `@Service` class with `new`?
- I get a **plain object that Spring knows nothing about**. The annotation is only metadata; it does nothing unless the **container** creates the object.

- That object gets **no dependency injection**, so any `@Autowired` fields stay `null`. It also gets **no proxy**, so `@Transactional`, `@Async` and `@Cacheable` **silently don't work** on it.

- This is a common real-world bug, for example calling `new OrderService(...)` inside a controller. The fix is to let Spring **inject the bean** instead of creating it myself.

## Core 4 — BeanFactory vs ApplicationContext

**Answer:**
- Both are interfaces for the Spring IoC container. **`BeanFactory` is the basic container**: it holds bean definitions, creates beans, injects dependencies and hands beans out through `getBean()`.

- **`ApplicationContext` is a sub-interface of `BeanFactory`**, so it does everything `BeanFactory` does and adds the **enterprise features** on top. In practice it is the container every real Spring application uses.

- The first big difference is **when beans are created**. A plain `BeanFactory` is **lazy** and creates a bean only when I ask for it, while an `ApplicationContext` **eagerly creates all singleton beans at startup**, so configuration errors show up immediately.

- The second is **what works out of the box**. `ApplicationContext` automatically registers the **post-processors** that make annotations like `@Autowired`, `@PostConstruct` and `@Transactional` work, and it adds **events**, **internationalization**, **resource loading** and the **`Environment`** with profiles and properties.

- So the one-liner is: **`BeanFactory` is the core DI engine, and `ApplicationContext` is `BeanFactory` plus everything needed to build a real application.** In Spring Boot I never choose, because `SpringApplication.run()` always creates an `ApplicationContext`.

**Key points:**
- **Lazy vs eager:** With a plain `BeanFactory`, registering a definition creates nothing; the object is built on the first `getBean()` call. An `ApplicationContext` builds every singleton during **refresh**, so the application either starts fully wired or **fails fast**.

```java
// Plain BeanFactory — nothing is created until getBean()
var factory = new DefaultListableBeanFactory();
factory.registerBeanDefinition("loudBean", new RootBeanDefinition(LoudBean.class));
factory.getBean(LoudBean.class);          // constructor runs here; @PostConstruct is NOT called

// ApplicationContext — singleton created at startup, annotations processed
var context = new AnnotationConfigApplicationContext(LoudBean.class);
// constructor and @PostConstruct have already run
```

- **Post-processors:** Annotations are not magic; they are handled by **`BeanPostProcessor`s** and **`BeanFactoryPostProcessor`s**. An `ApplicationContext` **detects and registers them automatically**, while with a plain `BeanFactory` I would have to register each one by hand, which is why `@PostConstruct` is ignored there.

- **Extra features of `ApplicationContext`:** It adds **event publishing** (`ApplicationEventPublisher`), **message resolution for i18n** (`MessageSource`), **resource loading** from the classpath or file system (`ResourceLoader`), and the **`Environment`** for profiles and property sources. It also integrates **AOP**, so proxies for `@Transactional` and similar features get created.

- **How they relate internally:** `ApplicationContext` **extends** `BeanFactory`, but it does not reimplement it. Internally it **delegates** to a `DefaultListableBeanFactory`, which does the actual bean creation, and the context adds the surrounding infrastructure.

- **Common implementations:** `AnnotationConfigApplicationContext` is used for standalone annotation-based apps and tests. In a Spring Boot servlet web app the context is **`AnnotationConfigServletWebServerApplicationContext`**, which also starts the embedded web server. `ClassPathXmlApplicationContext` is the legacy XML one.

- **Which to use:** **Always `ApplicationContext`.** `BeanFactory` used to be recommended for memory-constrained environments, but today that reason is gone and using it directly is rare.

**Follow-ups:**

### Q: Which ApplicationContext does Spring Boot create?
- It depends on the **type of application**, which Boot detects from the classpath. A **servlet web app** (Spring MVC) gets `AnnotationConfigServletWebServerApplicationContext`, which also creates and starts the **embedded server** such as Tomcat.

- A **reactive app** (WebFlux) gets `AnnotationConfigReactiveWebServerApplicationContext`, and a **non-web app** gets a plain `AnnotationConfigApplicationContext`.

- I rarely need to know the exact class, but it explains why adding the web starter makes the app start a server: the **context type changes**.

---

### Q: Why don't `@Autowired` or `@PostConstruct` work with a plain BeanFactory?
- Those annotations are processed by **`BeanPostProcessor`s**, for example `AutowiredAnnotationBeanPostProcessor` for `@Autowired` and `CommonAnnotationBeanPostProcessor` for `@PostConstruct` and `@PreDestroy`.

- An `ApplicationContext` **registers these automatically** during startup. A plain `BeanFactory` does not, so unless I add them manually the annotations are simply **ignored without any error**.

---

### Q: When would you use BeanFactory directly?
- **Almost never** in application code. `ApplicationContext` gives the same features plus everything else, and the memory difference no longer matters.

- I still see the **type** in a few places: the `BeanFactoryAware` callback, `ObjectProvider` (which is a lazy lookup backed by the factory), and `BeanFactoryPostProcessor`, which receives a `ConfigurableListableBeanFactory` so it can modify bean definitions.

---

### Q: Can an application have more than one ApplicationContext?
- Yes. Contexts can form a **parent-child hierarchy**, where a child can see the parent's beans but the parent cannot see the child's.

- Classic Spring MVC used a **root context** for services and repositories plus a **child context per `DispatcherServlet`** for controllers. A typical **Spring Boot app has a single context**, so this mostly comes up with legacy apps and with Spring Cloud's bootstrap or named child contexts.

## Core 5 — Full bean lifecycle walkthrough

**Answer:**
- The bean lifecycle is the sequence of steps the container takes a bean through, **from creation to destruction**. I usually describe it in four phases: **instantiate, inject, initialize, destroy**.

- First the container **instantiates** the bean by calling its constructor, which is also where **constructor injection** happens. Then it **populates properties**, meaning any setter or field injection.

- Next come the **Aware callbacks**, such as `BeanNameAware` and `ApplicationContextAware`, where Spring hands the bean references to container infrastructure if it asks for them.

- Then the bean is **initialized**. `BeanPostProcessor`s run their **before-initialization** step, then the init callbacks run in the order **`@PostConstruct`**, **`InitializingBean.afterPropertiesSet()`**, and a custom **init method**. After that, `BeanPostProcessor`s run their **after-initialization** step, which is where **AOP proxies** are created.

- Now the bean is **ready and in use** for the life of the application. On shutdown the container calls the destroy callbacks in the order **`@PreDestroy`**, **`DisposableBean.destroy()`**, and a custom **destroy method**.

**Key points:**
- **The full order:** It helps to remember it as one list.

```
1. Constructor                                   (instantiation + constructor injection)
2. Setter / field injection                      (populate properties)
3. Aware callbacks                               (BeanNameAware, ApplicationContextAware, ...)
4. BeanPostProcessor.postProcessBeforeInitialization
5. @PostConstruct
6. InitializingBean.afterPropertiesSet()
7. Custom init method                            (@Bean(initMethod = "..."))
8. BeanPostProcessor.postProcessAfterInitialization   (AOP proxies created here)
   ---- bean is ready and in use ----
9. @PreDestroy
10. DisposableBean.destroy()
11. Custom destroy method                        (@Bean(destroyMethod = "..."))
```

- **Before any bean is created:** The container first loads all **bean definitions**, and **`BeanFactoryPostProcessor`s** get a chance to modify them. This works on the recipes, not on bean instances, and it is how `${...}` property placeholders get resolved.

- **Initialization callbacks:** `@PostConstruct` is the place for logic that needs the bean's **dependencies to be injected already**, such as validating configuration or warming a cache. It is the **preferred** option because it is a standard Jakarta annotation and doesn't tie the class to Spring interfaces.

```java
@Component
public class PriceCache {
    private final ProductRepository repository;

    public PriceCache(ProductRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void load() { ... }        // dependencies are injected, safe to use them

    @PreDestroy
    void clear() { ... }       // release resources on shutdown
}
```

- **`BeanPostProcessor`:** A `BeanPostProcessor` is a hook that sees **every bean** just before and just after its initialization, and it may **return a different object** than it received. That is exactly how Spring implements proxies: the after-initialization step can return a **proxy wrapping the original bean**.

- **Proxies come last:** Because the proxy is created in the **after-initialization** step, code inside `@PostConstruct` runs on the **raw bean**, not the proxy. So proxy-based features such as `@Transactional` **do not apply** to work done inside `@PostConstruct`.

- **Destruction:** Destroy callbacks run when the **context is closed**, which in Spring Boot happens through a **JVM shutdown hook**. Beans are destroyed in the **reverse order of creation**, so a bean is destroyed before the beans it depends on.

- **Third-party classes:** For a class I can't annotate, I declare the callbacks on the `@Bean` method instead.

```java
@Bean(initMethod = "start", destroyMethod = "stop")
ConnectionPool connectionPool() {
    return new ConnectionPool();
}
```

- **`jakarta`, not `javax`:** Since Spring 6 the annotations are `jakarta.annotation.PostConstruct` and `jakarta.annotation.PreDestroy`. The old `javax.annotation` versions are **no longer recognised**.

**Follow-ups:**

### Q: Why use @PostConstruct instead of putting the logic in the constructor?
- With **field or setter injection**, the dependencies are **not injected yet** when the constructor runs, so using them there gives a `NullPointerException`. `@PostConstruct` runs **after all injection is complete**.

- Even with constructor injection, it's good practice to keep constructors **light and free of side effects**, such as calling a database or a remote service. That keeps the class easy to create in unit tests, where I can simply not call the init method.

- `@PostConstruct` also runs after the **Aware callbacks**, so the bean is fully set up by the container at that point.

---

### Q: @PostConstruct vs InitializingBean vs initMethod, which one and in what order?
- They all do the same job and run in this fixed order: **`@PostConstruct`**, then **`afterPropertiesSet()`**, then the custom **init method**.

- **`@PostConstruct` is preferred** for my own classes because it's a standard annotation and keeps the class independent of Spring.

- **`InitializingBean`** is a Spring interface, so implementing it **couples the class to Spring**. It's mostly seen inside framework code.

- **`initMethod`** on `@Bean` is for **third-party classes** whose source I can't change. The same three options exist for destruction: `@PreDestroy`, `DisposableBean` and `destroyMethod`.

---

### Q: At what point in the lifecycle are AOP proxies created?
- In the **after-initialization** step of a `BeanPostProcessor`, which is the last step before the bean is ready. The post-processor returns a **proxy that wraps the original bean**, and that proxy is what gets injected into other beans.

- The practical consequence is that inside the constructor and `@PostConstruct` I'm working with the **raw object**. Calling a `@Transactional` or `@Async` method from `@PostConstruct` runs it **without** the transaction or async behaviour.

- If I need startup logic that goes through the proxy, I run it **after the context is ready**, for example in an `ApplicationRunner` or an `ApplicationReadyEvent` listener.

---

### Q: Are destroy callbacks called for prototype beans?
- **No.** The container creates a prototype bean, initializes it and hands it over, but it does **not keep a reference** to it afterwards.

- So `@PostConstruct` runs for a prototype bean, but **`@PreDestroy` never does**. If a prototype holds resources such as connections, the **code that requested it** is responsible for cleaning up.

---

### Q: How is bean destruction triggered in a Spring Boot app?
- Spring Boot registers a **JVM shutdown hook** that closes the `ApplicationContext`. It fires on a normal exit or a **SIGTERM**, which is what Kubernetes and `Ctrl+C` send.

- Closing the context runs the **destroy callbacks** in reverse order of creation. With graceful shutdown enabled, the web server first **stops accepting new requests** and lets in-flight ones finish.

- A **forced kill** such as `kill -9` or a power loss skips the shutdown hook, so `@PreDestroy` is **not guaranteed** to run. I shouldn't rely on it for anything critical like saving important data.

---

### Q: What runs after all beans have been created?
- After every singleton is created, Spring calls **`SmartInitializingSingleton.afterSingletonsInstantiated()`** on beans that implement it, and then publishes **`ContextRefreshedEvent`**.

- Spring Boot then runs the **`ApplicationRunner`** and **`CommandLineRunner`** beans and finally publishes **`ApplicationReadyEvent`**, which signals that the application is ready to serve requests.

- This matters because `@PostConstruct` runs while **other beans may still be under construction**. Startup logic that needs the **whole application ready** belongs in a runner or an `ApplicationReadyEvent` listener instead.

## Core 6 — What are the different bean scopes in Spring?

**Answer:**
- A bean's scope defines **how many instances** of it the container creates and **how long each one lives**. Spring has **six built-in scopes**: two core scopes that work in any application, and four **web scopes** that need a web application.

- **Singleton** is the default and gives **one instance per `ApplicationContext`**, shared by the whole application. It's used for stateless beans such as services, repositories and controllers.

- **Prototype** gives a **new instance every time** the bean is requested from the container. It's used for stateful, short-lived objects, and Spring does not run destroy callbacks for it.

- **Request** scope gives **one instance per HTTP request**, and the instance is discarded when the request ends. It suits per-request data, for example a holder for the current request's correlation ID or the details of the logged-in user.

- **Session** scope gives **one instance per HTTP session**, so the same user gets the same instance across several requests until the session expires. The classic example is a **shopping cart** or user preferences in a server-side session application.

- **Application** scope gives **one instance per `ServletContext`**, which means one for the whole web application. **WebSocket** scope gives **one instance per WebSocket session**, living as long as that connection stays open.

**Key points:**
- **All six at a glance:** The scope name, how long an instance lives, and how to declare it.

| Scope | One instance per | Annotation |
|---|---|---|
| **singleton** (default) | `ApplicationContext` | none needed |
| **prototype** | request to the container (injection point or `getBean()`) | `@Scope("prototype")` |
| **request** | HTTP request | `@RequestScope` |
| **session** | HTTP session (one user's session) | `@SessionScope` |
| **application** | `ServletContext` (the whole web app) | `@ApplicationScope` |
| **websocket** | WebSocket session | `@Scope("websocket")` |

- **Declaring a scope:** I put `@Scope` or one of the shortcut annotations on the class, or on the `@Bean` method. Singleton needs nothing because it's the default.

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)   // same as @Scope("prototype")
public class ShoppingCart { ... }

@Component
@RequestScope                                      // one instance per HTTP request
public class RequestContext { ... }
```

- **Web scopes need a web context:** Request, session, application and websocket scopes only exist in a **web-aware `ApplicationContext`**. Using them in a non-web application fails with an error saying the scope is not registered.

- **Scoped proxies:** A request-scoped bean can't be injected directly into a singleton, because the singleton is created at startup when **no request exists**. Spring injects a **proxy** instead, and on each call the proxy **delegates to the instance for the current request**. `@RequestScope` and `@SessionScope` enable this proxy by default.

- **In a stateless REST API:** I mostly use **singletons only**. Session scope is uncommon there because state is kept in the token or the database, not in the server's memory.

- **Custom scopes:** Spring lets me register my own scopes through the `Scope` interface. The best-known example is Spring Cloud's **`@RefreshScope`**, which recreates beans when configuration changes at runtime.

**Follow-ups:**

### Q: How does a request-scoped or session-scoped bean get injected into a singleton?
- Through a **scoped proxy**. At startup Spring injects a **proxy object** into the singleton instead of a real instance, because no request or session exists yet.

- Each time a method is called on the proxy, it **looks up the real bean for the current request or session** and delegates the call to it. So one singleton can safely serve many concurrent requests, each one seeing **its own** scoped bean.

- `@RequestScope` and `@SessionScope` set `proxyMode = ScopedProxyMode.TARGET_CLASS` by default, so this works without extra configuration. Calling such a bean **outside a request**, for example from a background thread, fails because there is no active scope.

---

### Q: What is the difference between application scope and singleton?
- They are very close, because both give effectively **one instance for the application**. The difference is what the instance is tied to.

- A **singleton** is one instance per **`ApplicationContext`**. An **application-scoped** bean is one instance per **`ServletContext`**, and it is also exposed as a `ServletContext` attribute.

- It only matters when one web application has **several application contexts** sharing a single `ServletContext`. In a normal Spring Boot app there is one of each, so I just use singleton.

---

### Q: Which scope should you choose for a bean?
- **Singleton by default**, and it fits almost everything, as long as the bean is **stateless**. One shared instance is cheap and there is nothing to clean up.

- I pick a narrower scope only when the bean must hold **state that belongs to something specific**: **request** for data of one HTTP request, **session** for data of one user's session, and **prototype** when each user of the bean needs its own fresh object.

- If the object needs **no injection and no Spring features**, it often shouldn't be a bean at all. Creating it with `new` is simpler.

## Core 7 — Singleton vs prototype — the differences

**Answer:**
- Singleton and prototype are the two **core bean scopes**, and they differ in **how many instances** exist, **when** they are created, and **how much of the lifecycle** Spring manages.

- **Singleton is the default.** The container creates **exactly one instance per `ApplicationContext`**, normally **eagerly at startup**, and every class that injects the bean shares that same instance.

- With **prototype** scope the container creates a **new instance every time the bean is requested**, meaning at every injection point and on every `getBean()` call. Prototypes are created **lazily**, only when something asks for them.

- The lifecycle is also different. Spring manages a singleton's **complete lifecycle** including destruction, but for a prototype it only creates, injects and initializes the object and then **hands it over**, so **`@PreDestroy` is never called**.

- In practice almost every bean is a **stateless singleton**, such as services, repositories and controllers. Prototype is for **stateful, short-lived objects** where each user of the bean needs its own copy.

**Key points:**
- **Side by side:** The differences that interviewers look for.

| | Singleton | Prototype |
|---|---|---|
| Instances | One per container | New one per request to the container |
| Created | Eagerly at startup | Lazily, when requested |
| Init callbacks (`@PostConstruct`) | Yes | Yes |
| Destroy callbacks (`@PreDestroy`) | Yes | **No** |
| Typical use | Stateless services | Stateful, short-lived objects |

- **Declaring them:** Singleton needs nothing because it's the default. For prototype I add `@Scope` on the class or on the `@Bean` method.

```java
@Service
public class OrderService { ... }                       // singleton by default

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ShoppingCart { ... }

context.getBean(OrderService.class) == context.getBean(OrderService.class);   // true
context.getBean(ShoppingCart.class) == context.getBean(ShoppingCart.class);   // false
```

- **Singleton means shared state:** Because one instance serves **all threads and all requests**, a singleton must not keep per-request or per-user data in its fields. Holding mutable state in a singleton is the classic cause of **thread-safety bugs** (see Core 8).

- **Prototype inside a singleton:** This is the main pitfall. A singleton is created **once**, so its dependencies are injected **once**, and a prototype injected into it becomes **effectively a singleton**. The fix is to ask the container for a fresh instance each time, using `ObjectProvider`, `@Lookup` or a scoped proxy (see Core 21).

```java
@Service
public class CheckoutService {
    private final ShoppingCart cart;            // prototype, but injected only once

    public CheckoutService(ShoppingCart cart) {
        this.cart = cart;                       // every caller now shares this one cart
    }
}
```

- **Who cleans up a prototype:** After handing a prototype over, the container **keeps no reference** to it. If it holds resources such as connections or files, the **code that requested it** must release them.

**Follow-ups:**

### Q: Is a Spring singleton the same as the GoF Singleton pattern?
- **No.** The GoF Singleton guarantees **one instance per classloader** and the class enforces it itself, with a **private constructor** and a static `getInstance()` method.

- A Spring singleton is **one instance per bean definition per container**. The class is a normal class with a public constructor, and it's the **container** that makes sure only one is created.

- So I can have **two singleton beans of the same class** if I define two `@Bean` methods, and two application contexts would each have their own instance. Spring's version is also much **easier to test**, because nothing stops me from creating the class with `new` in a unit test.

---

### Q: What happens when you inject a prototype bean into a singleton?
- The prototype is created **once**, at the moment the singleton is created, and that single instance stays in the singleton's field **forever**. So the prototype behaves like a singleton, which is almost never what was intended.

- It happens because **injection occurs at creation time**, not on every method call. The container has no reason to revisit the singleton later.

- The fix is to **look up a new instance when I need one**. I inject an **`ObjectProvider<ShoppingCart>`** and call `getObject()`, or use a **`@Lookup`** method, or put a **scoped proxy** on the prototype (see Core 21).

---

### Q: When would you actually use prototype scope?
- When a bean holds **state that must not be shared**, and each use needs a **fresh instance**. Examples are a non-thread-safe helper, a builder or command object for a single operation, or a per-task processor.

- Honestly it is **rare** in typical applications. If the object doesn't need **injected dependencies or Spring features**, it's simpler to create it with `new` or through a small factory.

- Prototype scope is worth it when the object is stateful **and** needs things from the container, such as injected services or configuration.

## Core 8 — Are singleton beans thread-safe? How to make them safe?

**Answer:**
- **No, not automatically.** Singleton scope only guarantees that there is **one instance** of the bean. It says nothing about thread safety, and Spring adds **no synchronization** of its own.

- In a web application that one instance is called by **many request threads at the same time**. Whether that is safe depends entirely on **how I write the class**.

- A singleton is thread-safe when it is **stateless**. Its fields are only **final references to other beans**, and all request data travels through **method parameters and local variables**, which live on each thread's own stack.

- It becomes unsafe as soon as it has a **mutable field that several threads read and write**, such as a counter, a list or a "current user" field. Then two threads can overwrite each other's values, which is a **race condition**.

- So the main way to make a singleton safe is to **keep it stateless**. If I really need shared state, I use **thread-safe types** like `AtomicInteger` or `ConcurrentHashMap`, or **synchronization**, or I move the state out to the **database or a cache**.

**Key points:**
- **Why stateless is safe:** Each thread has its **own stack**, so parameters and local variables are private to the call. The bean itself holds nothing that changes, so there is **nothing for threads to fight over**.

```java
@Service
public class OrderService {
    private final NotificationService notificationService;   // final, never changes

    public void placeOrder(String email, String product) {   // per-thread parameters
        String message = "Order placed for " + product;      // per-thread local variable
        notificationService.send(email, message);
    }
}
```

- **What a race condition looks like:** `count++` looks like one step but is really three: **read, add one, write back**. Two threads can both read the same value and both write the same result, so **one update is lost**.

```java
@Component
public class UnsafeViewCounter {
    private int count;                 // shared mutable state

    public void increment() {
        count++;                       // not atomic: updates get lost under load
    }
}
```

- **Fix 1, remove the state (best):** Most of the time the field shouldn't exist. I pass the data as a **parameter**, keep it in a **local variable**, or store it in the **database**.

- **Fix 2, immutable state:** State that is set once and **never changes** is safe to share. Configuration values injected through the constructor into **`final` fields** are the typical example.

- **Fix 3, thread-safe types:** When shared mutable state is really needed, such as an in-memory counter or cache, I use classes from `java.util.concurrent`. **`AtomicInteger`** and **`LongAdder`** for counters, **`ConcurrentHashMap`** for maps.

```java
@Component
public class SafeViewCounter {
    private final AtomicInteger count = new AtomicInteger();

    public void increment() {
        count.incrementAndGet();       // atomic, no lost updates
    }
}
```

- **Fix 4, synchronization:** A **`synchronized`** method or a `ReentrantLock` lets only one thread in at a time. It's correct but it makes threads **wait for each other**, so I keep the locked section small and prefer the concurrent types where they fit.

- **Fix 5, per-thread or per-request state:** A **`ThreadLocal`** gives each thread its own copy, and a **request-scoped bean** gives each HTTP request its own instance. Spring itself uses `ThreadLocal` for the security context and for transactions.

- **Classic unsafe fields:** A `HashMap` or `ArrayList` used as a cache, a plain `int` or `long` counter, a `SimpleDateFormat`, and any field that stores **data about the current request or user**.

**Follow-ups:**

### Q: If singletons are not thread-safe by default, why is singleton the default scope?
- Because the typical Spring bean is a **stateless service**, and a stateless object is **naturally thread-safe**. One shared instance is all that's needed.

- It's also **efficient**. Creating one instance at startup is cheaper than creating objects for every request, and there is less work for the garbage collector.

- So the design assumption is that beans hold **behaviour** and data lives elsewhere, in **method parameters, entities and the database**. Thread safety is the developer's responsibility, and staying stateless is how to meet it.

---

### Q: Controllers, services and repositories are all singletons. Are they thread-safe?
- **Yes, as long as I keep them stateless**, which is the normal way to write them. A controller receives the request data as **method arguments**, calls a service, and returns a result, without storing anything in fields.

- The bug appears when someone adds a field such as `private User currentUser` or `private List<Item> items` to one of these classes. That field is then **shared by every request**.

- Spring's own infrastructure beans are written to be thread-safe. For example `RestClient`, `JdbcTemplate` and `ObjectMapper` are **safe to share** once they are configured.

---

### Q: How can a singleton inject an EntityManager or HttpServletRequest, which are per-request objects?
- Spring doesn't inject the real object. It injects a **thread-safe proxy**, and that single proxy is safe to keep in a singleton's field.

- On each call the proxy **looks up the real object for the current thread**. For `EntityManager` that is the one bound to the current **transaction**, and for `HttpServletRequest` it's the **current request**.

- So many threads share the same proxy but each one is routed to **its own underlying object**. This is the same idea as the scoped proxy used for request-scoped beans.

---

### Q: synchronized, AtomicInteger or ConcurrentHashMap, when do you use which?
- **`AtomicInteger`, `AtomicLong` and `LongAdder`** are for a **single number** such as a counter. They are lock-free, so they stay fast under contention.

- **`ConcurrentHashMap`** is for a **shared map**, such as an in-memory cache. Methods like `computeIfAbsent()` and `merge()` make check-then-update operations atomic.

- **`synchronized` or a `ReentrantLock`** is for when **several fields or several steps** must change together as one unit. It's the most general tool but also the one that makes threads wait.

- A common mistake is combining two thread-safe calls and assuming the result is safe. `if (!map.containsKey(k)) map.put(k, v)` is **still a race**, because another thread can get in between the two calls.

---

### Q: Does synchronized make the bean safe when the application runs on several instances?
- **No.** `synchronized`, locks and atomic classes only work **inside one JVM**. Each instance of the application has its **own copy of the singleton** and its own lock.

- In production there are usually several instances behind a load balancer, so shared data must be protected **where it is stored**. That means **database transactions**, **optimistic locking with `@Version`**, pessimistic locks, or a **distributed lock** in something like Redis.

- This is another reason to keep beans stateless: state held in memory is **neither shared nor consistent** across instances.

---

### Q: What is ThreadLocal and what are the risks of using it?
- A `ThreadLocal` is a variable where **each thread has its own independent value**. It's used for context that belongs to the current request but isn't passed as a parameter, such as the **security context**, the **current transaction** and logging **MDC** values.

- The main risk comes from **thread pools**. Threads are reused, so a value left behind by one request can **leak into the next request** handled by the same thread. The rule is to always **`remove()` it in a `finally` block**.

- The value also **doesn't follow the work to another thread**, so an `@Async` method or a new thread won't see it unless the context is propagated explicitly.
## Core 9 — Constructor vs setter vs field injection — why constructor is preferred

**Answer:**
- Spring can inject a dependency in three ways: through the **constructor**, through a **setter method**, or **directly into a field** with `@Autowired`. All three give the bean the same object, but they differ in **when** the dependency is set, whether it can be **`final`**, and how easy the class is to **test without Spring**.

- **Constructor injection** is the recommended way, and the Spring team itself says so. The dependencies become **`final` fields**, so the object is **fully built and immutable** the moment it exists, and a missing dependency makes the app **fail at startup** instead of throwing a `NullPointerException` later.

- **Setter injection** is for **optional** dependencies, or ones that may legitimately be **changed after creation**. The downside is that the object can exist in a **half-initialised state** until the setter is called.

- **Field injection** is the shortest to write, but it **hides dependencies**, can't use `final`, and the class **can't be created with `new` in a unit test** without reflection. That's why IntelliJ flags it with a warning and it's considered bad practice in application code.

- Since **Spring 4.3**, if a class has **only one constructor**, `@Autowired` on it is **optional**. So in modern code constructor injection is also the least verbose option, especially with **Java records** or Lombok's `@RequiredArgsConstructor`.

**Key points:**
- **Constructor injection**: dependencies are required, `final`, and visible in the constructor signature. With a single constructor, no annotation is needed.
```java
@Service
public class OrderService {
    private final NotificationService notificationService;

    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```

- **Setter injection**: Spring first creates the object with the no-arg constructor, then calls the setter. Use **`@Autowired(required = false)`** to make the dependency optional.
```java
private NotificationService notificationService;

@Autowired
public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
}
```

- **Field injection**: Spring sets the private field **through reflection** after construction. Outside Spring the field simply stays `null`.
```java
@Autowired
private NotificationService notificationService;
```

- **Testability** is the strongest practical argument. With constructor injection, a unit test passes a **fake or mock** straight into `new`, with no Spring context and no reflection.
```java
NotificationService fake = (to, msg) -> sent.add(msg);
new OrderService(fake).placeOrder("bob@shoplite.com", "Mouse");
```

- **Fail-fast**: with constructor injection, a missing bean is reported as **`UnsatisfiedDependencyException` at startup**. With field injection in a plain `new` call, you only find out with an **NPE at runtime**.

- **Design smell detector**: a constructor with **eight parameters** is an obvious signal that the class does too much. Field injection hides that, because adding another `@Autowired` field costs nothing visually.

- **Circular dependencies**: with constructor injection, Spring **can't** resolve A → B → A and fails with `BeanCurrentlyInCreationException`. This is a good thing, because since **Boot 2.6 circular references are banned by default** anyway, and the error points at a design problem.

**Follow-ups:**

### Q: Why is field injection considered bad if it works fine?
- It works inside Spring, but it **couples the class to the container**. The class can't be created correctly with `new`, so unit tests need Spring, Mockito's `@InjectMocks`, or `ReflectionTestUtils` just to set a field.

- The fields can't be **`final`**, so the object is **mutable** and could be in an incomplete state. Its dependencies are also **invisible from the outside**, because nothing in the public API tells you what the class needs.

- It's still acceptable in **test classes**, for example `@Autowired` in a `@SpringBootTest`, because nobody creates test classes with `new`.

---

### Q: When would you actually use setter injection?
- For **optional dependencies** with a sensible default, marked with `@Autowired(required = false)`. If no matching bean exists, Spring simply doesn't call the setter and the class uses its fallback.

- For dependencies that may need to be **reconfigured after creation**, which is rare in application code but common in framework and infrastructure classes.

- A modern alternative for optional dependencies is to inject **`ObjectProvider<T>`** or **`Optional<T>`** through the constructor. That keeps the field `final` and still allows the bean to be missing.

---

### Q: Do you need @Autowired on the constructor?
- **No, not if the class has only one constructor.** Since Spring 4.3, Spring automatically uses the single constructor for injection.

- If the class has **several constructors**, you must mark the one Spring should use with **`@Autowired`**. Otherwise Spring tries the no-arg constructor and fails if there isn't one.

---

### Q: How does constructor injection handle circular dependencies?
- It **can't resolve them**. To create A, Spring needs B, and to create B it needs A, so neither can be constructed first and startup fails with **`BeanCurrentlyInCreationException`**.

- Setter and field injection could work around it, because Spring can create A half-done and inject it into B. But since **Spring Boot 2.6**, `spring.main.allow-circular-references` is **`false` by default**, so Boot rejects those cycles too.

- The right fix is to **redesign**: extract the shared logic into a third bean, use an **event**, or as a last resort mark one parameter **`@Lazy`** so Spring injects a proxy.

---

### Q: How does Lombok or a record fit into this?
- Lombok's **`@RequiredArgsConstructor`** generates a constructor for all `final` fields. You get constructor injection with **no boilerplate**, and it's very common in real projects.

- A Spring bean can't usually be a **record**, but records are perfect for the **configuration properties** and **DTOs** those beans use. `@ConfigurationProperties` records are bound through their constructor in the same spirit.

---

### Q: Does injection style affect @Value or configuration properties?
- `@Value` works on **constructor parameters**, setters and fields, exactly like `@Autowired`. Putting it on a constructor parameter keeps the value **`final`**.

- For groups of related settings, a **`@ConfigurationProperties` record** injected through the constructor is cleaner than many separate `@Value` fields.
