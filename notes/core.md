# Spring Core — Notes

## Core 1 — What is Spring?

**Answer:**
- Spring is an **open-source framework** for building Java applications. It takes care of the **infrastructure** so I can focus on **business logic**.

- At its heart is the **IoC container**. It creates my objects, which Spring calls **beans**, **connects them to each other**, and manages them from creation to shutdown.

- On top of that, Spring gives **ready-made support** for what most apps need: web and REST APIs, database access, transactions, security and testing. The result is **loosely coupled** code that is easy to change and **easy to test**.

**Key points:**
- **Dependency Injection**: a class doesn't create its collaborators with `new`. It declares what it needs, and Spring **passes it in**.
```java
@Service
public class OrderService {
    private final NotificationService notificationService;

    public OrderService(NotificationService notificationService) {  // Spring passes it in
        this.notificationService = notificationService;
    }
}
```

- **Less boilerplate**: one annotation like **`@Transactional`** replaces hand-written begin/commit/rollback code. **Spring Data** and **Spring MVC** remove most low-level database and servlet code.

- **Modular**: Spring is split into projects such as **Core, MVC, Data, Security and Cloud**. I only add the ones I need.

- **Spring Boot** sits on top of Spring and removes the setup work. It adds **auto-configuration**, **starters** and an **embedded server**.

- **Current versions**: **Spring Framework 7** and **Spring Boot 4**, with **Java 17 as the minimum** and **Jakarta EE** (`jakarta.*` packages, not `javax.*`).

**Follow-ups:**

### Q: What is the difference between Spring and Spring Boot?
- **Spring** is the framework itself: the container, DI, MVC, data access and so on. In classic Spring I had to **configure everything by hand**.

- **Spring Boot** is built on Spring and makes it **quick to start**. It configures beans automatically based on what's on the classpath, provides starter dependencies, and runs the app with an **embedded server** as a simple JAR.

## Core 2 — IoC and DI — how they enable loose coupling

**Answer:**
- **Inversion of Control** means my code **no longer creates and wires its own objects**. The **container** does it instead.

- **Dependency Injection** is **how Spring does IoC**. A class lists its dependencies, usually as **constructor parameters**, and Spring passes them in.

- **Loose coupling** comes from depending on **interfaces, not concrete classes**. `OrderService` only knows the `NotificationService` interface, so I can **swap** email for SMS, or pass a **fake in tests**, without changing `OrderService`.

**Key points:**
- **Tight coupling (no DI)**: the class creates its own dependency, so it's **locked to that one implementation**.
```java
public class OrderService {
    private final EmailNotificationService notifier = new EmailNotificationService(); // hard-wired
}
```

- **Loose coupling (DI)**: the class depends on the **interface** and receives it through the **constructor**.
```java
@Service
public class OrderService {
    private final NotificationService notificationService;

    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```

- **Easy testing**: a unit test passes a **fake** straight into the constructor. **No Spring** is needed.
```java
NotificationService fake = (to, msg) -> sent.add(to + ":" + msg);
new OrderService(fake).placeOrder("bob@shoplite.com", "Mouse");
```

- **Hollywood principle**: IoC is often summed up as **"don't call us, we'll call you"**. The framework calls my code, not the other way round.

**Follow-ups:**

### Q: IoC vs DI, what's the difference?
- **IoC** is the **general idea**: the framework, not my code, controls object creation. **DI** is **one way to do it**: dependencies are passed into the class from outside.

- Short version: IoC is the **what**, DI is the **how**.

---

### Q: DI vs Dependency Inversion Principle?
- The **Dependency Inversion Principle** is the "D" in SOLID. It's a **design rule**: depend on **abstractions**, not concrete classes.

- **DI** is the **technique** that supplies those abstractions at runtime. So DIP is the goal, and DI is how you reach it.

---

### Q: Is `context.getBean(OrderService.class)` dependency injection?
- **No**, that's **dependency lookup**: the class **pulls** the bean from the container itself.

- It ties the code to Spring and **hides what the class needs**, so constructor injection is preferred.

---

### Q: What if two beans implement the same interface?
- Spring injects **by type**, so with two candidates startup fails with **`NoUniqueBeanDefinitionException`**.

- Fix it with **`@Primary`** on the default implementation, or **`@Qualifier("beanName")`** at the injection point (see Core 13). To get **all** of them, inject a `List<NotificationService>`.

## Core 3 — What is a Spring bean? What does the IoC container do?

**Answer:**
- A **bean** is an object that **Spring creates and manages**. It's a normal Java object; it becomes a bean because Spring creates it and injects its dependencies, instead of my code calling `new`.

- The **IoC container**, the `ApplicationContext`, reads my configuration (`@Service`, `@Bean` and so on), **creates the beans**, **wires them together**, and manages their **lifecycle** until shutdown.

- By default every bean is a **singleton**: one shared instance, created at **startup**. Objects I create with `new`, such as entities and DTOs, are **not beans**.

**Key points:**
- **Two main ways to define a bean**: a **stereotype annotation** on my own class, or a **`@Bean` method** for classes I can't annotate, such as library classes.
```java
@Service
public class OrderService { ... }          // found by component scanning

@Configuration
public class AppConfig {
    @Bean
    Clock clock() {                         // library class registered as a bean
        return Clock.systemUTC();
    }
}
```

- **Stereotype annotations** are `@Component` and its specialised versions **`@Service`**, **`@Repository`** and **`@Controller`**. They mark a class as a bean and show its **role**.

- **Bean names**: the class name with a **lowercase first letter** (`orderService`), or the **method name** for a `@Bean` method.

- **Singleton by default**: asking for the same bean twice returns the **same instance**.
```java
context.getBean(OrderService.class) == context.getBean(OrderService.class);   // true
```

- **What the container does, in order**: read bean definitions → **create** beans → **inject** dependencies → run **init callbacks** → beans are **ready** → run **destroy callbacks** on shutdown.

**Follow-ups:**

### Q: What is a BeanDefinition?
- It's the container's **recipe** for a bean: its class, scope, dependencies and init/destroy methods.

- Spring first collects **all the recipes**, and **only then creates** the beans from them.

---

### Q: When are beans created?
- **Singletons** are created **at startup**, so wiring errors show up **immediately**.

- A **`@Lazy`** bean is created on **first use**, and a **prototype** bean is created **every time it's requested**.

---

### Q: What happens if I create a `@Service` class with `new`?
- I get a **plain object Spring knows nothing about**. The annotation does nothing by itself.

- It gets **no injection**, so `@Autowired` fields stay `null`, and **no proxy**, so `@Transactional` and `@Async` **silently don't work**.

## Core 4 — BeanFactory vs ApplicationContext

**Answer:**
- Both are interfaces for the Spring container. **`BeanFactory`** is the **basic** one: it creates beans and injects dependencies.

- **`ApplicationContext` extends `BeanFactory`** and adds **events**, **i18n messages**, **resource loading**, the **`Environment`** (profiles and properties), and automatic support for annotations like `@Autowired` and `@PostConstruct`.

- Another difference: `BeanFactory` creates beans **lazily**, on `getBean()`, while `ApplicationContext` creates all singletons **at startup**. Real apps always use **`ApplicationContext`**, and Spring Boot creates one for you.

**Key points:**
- **One-liner**: **`BeanFactory` is the core DI engine; `ApplicationContext` is `BeanFactory` plus everything a real app needs.**

- **Lazy vs eager**: with `BeanFactory`, nothing is created until `getBean()`. `ApplicationContext` creates everything at startup, so errors show up **immediately**.
```java
var factory = new DefaultListableBeanFactory();
factory.registerBeanDefinition("loudBean", new RootBeanDefinition(LoudBean.class));
factory.getBean(LoudBean.class);          // created only now, and @PostConstruct is ignored

var context = new AnnotationConfigApplicationContext(LoudBean.class);
// already created at startup, @PostConstruct has run
```

- **Why annotations don't work with a plain `BeanFactory`**: annotations are handled by helper beans called **post-processors**. `ApplicationContext` **registers them automatically**; a plain `BeanFactory` doesn't.

- **In Spring Boot**: `SpringApplication.run()` creates the `ApplicationContext`. For a web app it's a **web server context** that also starts embedded Tomcat.

**Follow-ups:**

### Q: When would you use BeanFactory directly?
- **Almost never.** `ApplicationContext` does everything `BeanFactory` does, and more.

- It used to be recommended for low-memory devices, but that reason **no longer applies**.

---

### Q: Can an application have more than one ApplicationContext?
- **Yes.** Contexts can form a **parent-child hierarchy**, where the child sees the parent's beans but not the other way round.

- Old Spring MVC apps used this. A normal **Spring Boot app has just one** context.

## Core 5 — Full bean lifecycle walkthrough

**Answer:**
- The bean lifecycle is the set of steps from **creation to destruction**. I remember it as four phases: **create, inject, initialize, destroy**.

- Spring **calls the constructor**, **injects dependencies**, runs **init callbacks** like `@PostConstruct`, and then creates a **proxy** if the bean needs one, for example for `@Transactional`.

- The bean is then **used** while the app runs. On **shutdown**, Spring calls destroy callbacks like **`@PreDestroy`**.

**Key points:**
- **The full order**:
```
1. Constructor                      (create + constructor injection)
2. Setter / field injection
3. Aware callbacks                  (BeanNameAware, ApplicationContextAware)
4. BeanPostProcessor — before init
5. @PostConstruct
6. InitializingBean.afterPropertiesSet()
7. Custom init method               (@Bean(initMethod = "..."))
8. BeanPostProcessor — after init   (proxies are created here)
   ---- bean is ready ----
9. @PreDestroy
10. DisposableBean.destroy()
11. Custom destroy method           (@Bean(destroyMethod = "..."))
```

- **`@PostConstruct` and `@PreDestroy`** are the **preferred** callbacks. `@PostConstruct` runs **after injection**, so dependencies are safe to use there.
```java
@Component
public class PriceCache {
    private final ProductRepository repository;

    public PriceCache(ProductRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void load() { ... }        // dependencies are ready

    @PreDestroy
    void clear() { ... }       // clean up on shutdown
}
```

- **`BeanPostProcessor`** is a hook that runs for **every bean**, before and after init. Spring uses it to **wrap beans in proxies**.

- **Proxies come last**: inside `@PostConstruct` the bean is **not yet a proxy**, so `@Transactional` **doesn't apply** there.

- **For library classes**, I set the callbacks on the `@Bean` method.
```java
@Bean(initMethod = "start", destroyMethod = "stop")
ConnectionPool connectionPool() { return new ConnectionPool(); }
```

- **Use `jakarta.annotation`**, not `javax.annotation`. Since Spring 6 the `javax` versions are **ignored**.

**Follow-ups:**

### Q: Why use @PostConstruct instead of the constructor?
- With field or setter injection, dependencies are **still `null`** in the constructor. `@PostConstruct` runs **after injection**.

- It also keeps the constructor **simple**, which makes the class easy to create in tests.

---

### Q: @PostConstruct vs InitializingBean vs initMethod?
- They do the same job and run in this order: **`@PostConstruct`** → **`afterPropertiesSet()`** → **init method**.

- Prefer **`@PostConstruct`**. `InitializingBean` **ties the class to Spring**, and `initMethod` is for **library classes** you can't change.

---

### Q: Are destroy callbacks called for prototype beans?
- **No.** Spring creates a prototype, hands it over and **forgets it**, so `@PreDestroy` **never runs**.

- The code that asked for it must clean up its resources.

---

### Q: How is bean destruction triggered in Spring Boot?
- Boot registers a **JVM shutdown hook** that closes the context on a normal exit, **`Ctrl+C`**, or **SIGTERM** (what Kubernetes sends).

- A **forced kill** (`kill -9`) skips it, so don't rely on `@PreDestroy` for anything critical.

## Core 6 — What are the different bean scopes in Spring?

**Answer:**
- A bean's **scope** decides **how many instances** Spring creates and **how long each one lives**.

- The two core scopes are **singleton**, the default with **one shared instance**, and **prototype**, with a **new instance every time** it's requested.

- Web apps add four more: **request** (one per HTTP request), **session** (one per user session), **application** (one per web app) and **websocket** (one per WebSocket connection).

**Key points:**
- **All six at a glance**:

| Scope | One instance per | Annotation |
|---|---|---|
| **singleton** (default) | container | none needed |
| **prototype** | each request for the bean | `@Scope("prototype")` |
| **request** | HTTP request | `@RequestScope` |
| **session** | user's HTTP session | `@SessionScope` |
| **application** | web app (`ServletContext`) | `@ApplicationScope` |
| **websocket** | WebSocket session | `@Scope("websocket")` |

- **Declaring a scope**: add the annotation on the class or on the `@Bean` method.
```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ShoppingCart { ... }

@Component
@RequestScope
public class RequestContext { ... }
```

- **Web scopes** only work in a **web application**.

- **Scoped proxy**: a request-scoped bean can be injected into a singleton because Spring injects a **proxy**, which forwards each call to the **current request's** instance.

- **In practice**: a REST API uses **singletons almost everywhere**.

**Follow-ups:**

### Q: How does a request-scoped bean get injected into a singleton?
- Spring injects a **proxy** at startup, because no request exists yet.

- On every call, the proxy **finds the bean for the current request** and forwards the call to it. `@RequestScope` and `@SessionScope` set this up **automatically**.

---

### Q: Application scope vs singleton?
- Both give **one instance for the app**. A singleton is one per **`ApplicationContext`**, while application scope is one per **`ServletContext`**.

- In a normal Boot app there is one of each, so **just use singleton**.

---

### Q: Which scope should you choose?
- **Singleton**, as long as the bean is **stateless**, which covers almost everything.

- Use **request** or **session** for per-request or per-user data, and **prototype** when each user of the bean needs its own fresh copy.

## Core 7 — Singleton vs prototype — the differences

**Answer:**
- **Singleton** (the default) gives **one instance per container**, created **at startup** and **shared** by everyone who injects it.

- **Prototype** gives a **new instance every time** the bean is requested, created **only when needed**.

- Spring manages the **full lifecycle** of a singleton, but for a prototype it **doesn't call `@PreDestroy`**. Most beans are **stateless singletons**; prototype is for **stateful, short-lived** objects.

**Key points:**
- **Side by side**:

| | Singleton | Prototype |
|---|---|---|
| Instances | One per container | New one each time |
| Created | At startup | When requested |
| `@PostConstruct` | Yes | Yes |
| `@PreDestroy` | Yes | **No** |
| Typical use | Stateless services | Stateful, short-lived objects |

- **Declaring them**: singleton needs nothing; prototype needs `@Scope`.
```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ShoppingCart { ... }

context.getBean(ShoppingCart.class) == context.getBean(ShoppingCart.class);   // false
```

- **Singletons are shared**, so they must **not keep per-user data in fields** (see Core 8).

- **Prototype inside a singleton** is the main trap: it's injected **only once**, so it acts like a singleton. Fix it with **`ObjectProvider`** or **`@Lookup`** (see Core 17).

**Follow-ups:**

### Q: Is a Spring singleton the same as the GoF Singleton pattern?
- **No.** The GoF Singleton is enforced **by the class itself**, with a private constructor and `getInstance()`.

- A Spring singleton is **one per container**, enforced **by Spring**. The class is normal, so it's easy to create with `new` in tests.

---

### Q: What happens when you inject a prototype into a singleton?
- The prototype is created **once**, when the singleton is created, and **the same instance is reused forever**.

- To get a fresh one each time, inject **`ObjectProvider<ShoppingCart>`** and call `getObject()`.

---

### Q: When would you actually use prototype scope?
- When a bean has **state that must not be shared** but still needs **Spring to inject things** into it.

- It's **rare**. If the object needs nothing from Spring, just create it with `new`.

## Core 8 — Are singleton beans thread-safe? How to make them safe?

**Answer:**
- **No, not automatically.** Singleton only means **one instance**, and Spring adds **no thread safety**.

- In a web app, that one instance is used by **many request threads at the same time**. It's safe if it's **stateless**: no fields that change, and all data in **parameters and local variables**.

- It's unsafe if it has a **field that threads change**, like a counter or a list, which leads to a **race condition**. The fix is to **keep beans stateless**, or use **thread-safe types** like `AtomicInteger` and `ConcurrentHashMap`.

**Key points:**
- **Stateless is safe**: each thread has its **own** parameters and local variables, so there's nothing shared to break.
```java
@Service
public class OrderService {
    private final NotificationService notificationService;   // never changes

    public void placeOrder(String email, String product) {   // per-thread data
        notificationService.send(email, "Order placed for " + product);
    }
}
```

- **Race condition**: `count++` is really **read, add, write**. Two threads can do it at once, and **one update is lost**.
```java
@Component
public class UnsafeViewCounter {
    private int count;

    public void increment() { count++; }   // updates get lost under load
}
```

- **Fix 1, remove the state (best)**: pass data as **parameters** or store it in the **database**.

- **Fix 2, thread-safe types**: use **`AtomicInteger`** for counters and **`ConcurrentHashMap`** for caches.
```java
private final AtomicInteger count = new AtomicInteger();

public void increment() { count.incrementAndGet(); }   // safe
```

- **Fix 3, `synchronized`**: lets only one thread in at a time. It's correct but **slower**, because threads wait.

- **Fix 4, per-request state**: use a **request-scoped bean** or a **`ThreadLocal`** so each request has its own copy.

**Follow-ups:**

### Q: If singletons aren't thread-safe, why is singleton the default?
- Because most beans are **stateless services**, and stateless objects are **naturally thread-safe**.

- One shared instance is also **cheaper** than creating new objects all the time.

---

### Q: Are controllers and services thread-safe?
- **Yes, if they're stateless**, which is the normal way to write them.

- The bug appears when someone adds a field like `private User currentUser`, because that field is **shared by every request**.

---

### Q: Does synchronized work when the app runs on several instances?
- **No.** `synchronized` only works **inside one JVM**, and each instance has its own copy of the bean.

- Shared data across instances must be protected in the **database**, for example with **transactions** or **optimistic locking (`@Version`)**.

---

### Q: What is ThreadLocal and what's the risk?
- A `ThreadLocal` gives **each thread its own value**. Spring uses it for the **security context** and **transactions**.

- The risk is that threads are **reused by the pool**, so a leftover value can **leak into the next request**. Always **`remove()` it in a `finally`** block.

## Core 9 — Constructor vs setter vs field injection — why constructor is preferred

**Answer:**
- Spring can inject a dependency through the **constructor**, a **setter method**, or **directly into a field** with `@Autowired`.

- **Constructor injection is preferred.** Fields can be **`final`**, the object is **complete as soon as it's created**, and the class is **easy to test** with plain `new`.

- **Setter injection** suits **optional** dependencies. **Field injection** is short but **hides dependencies** and makes the class **hard to test**, so it's discouraged. With a **single constructor**, `@Autowired` isn't even needed.

**Key points:**
- **Constructor injection**: required, `final`, and visible in the constructor.
```java
@Service
public class OrderService {
    private final NotificationService notificationService;

    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```

- **Setter injection**: Spring creates the object first, then calls the setter.
```java
@Autowired
public void setNotificationService(NotificationService notificationService) {
    this.notificationService = notificationService;
}
```

- **Field injection**: Spring sets the private field **by reflection**. Outside Spring it stays `null`.
```java
@Autowired
private NotificationService notificationService;
```

- **Testability**: with constructor injection, a test passes a **fake** directly.
```java
new OrderService(fake).placeOrder("bob@shoplite.com", "Mouse");
```

- **Fails fast**: a missing bean stops the app **at startup**, not with an NPE later.

- **Shows design problems**: a constructor with **many parameters** shows the class does **too much**.

**Follow-ups:**

### Q: Why is field injection bad if it works?
- The class **can't be created properly with `new`**, so tests need Spring or reflection.

- Fields can't be `final`, and the dependencies are **hidden**. It's fine in **test classes**, though.

---

### Q: When would you use setter injection?
- For **optional** dependencies, with `@Autowired(required = false)`.

- A modern alternative is injecting **`ObjectProvider<T>`** through the constructor.

---

### Q: Do you need @Autowired on the constructor?
- **No, if there's only one constructor.** Since **Spring 4.3**, Spring uses it automatically.

- With **several constructors**, Spring falls back to the **no-arg** one, so the bean can end up **without its dependencies**. Mark the right constructor with **`@Autowired`**.

---

### Q: How does constructor injection handle circular dependencies?
- It **can't**: A needs B and B needs A, so startup fails with **`BeanCurrentlyInCreationException`**.

- Since **Boot 2.6**, circular references are **banned by default** anyway. Fix it by **redesigning**, for example by moving shared logic into a third bean.

---

### Q: How does Lombok fit in?
- **`@RequiredArgsConstructor`** generates a constructor for all `final` fields. You get constructor injection with **no boilerplate**.

## Core 10 — How does @ComponentScan find beans?

**Answer:**
- `@ComponentScan` tells Spring **which packages to search** for beans. In Spring Boot you don't write it yourself, because **`@SpringBootApplication` already includes it**, and it scans the **main class's package and all its sub-packages**.

- At startup, Spring looks through every class in those packages. Any class marked with **`@Component`**, or one of its specialised versions like `@Service`, `@Repository`, `@Controller` or `@Configuration`, is **registered as a bean**.

- A class **outside** those packages is **never found**, even if it has `@Component`. That's why the main class should sit in the **root package**, like `com.shoplite`.

**Key points:**
- **Default package**: the scan starts from the package of the `@SpringBootApplication` class. In ShopLite, that means `com.shoplite` and everything under it.
```java
@SpringBootApplication   // includes @ComponentScan
public class ShopliteApplication { }
```

- **Stereotype annotations**: `@Service`, `@Repository`, `@Controller` and `@Configuration` are all **marked with `@Component`** inside, so the scan finds them too. They exist to show **what role a class plays**.

- **Bean names**: by default a bean is named after its class with a **lowercase first letter**. You can give it your own name in the annotation.
```java
@Service class OrderService { }            // bean name: "orderService"
@Service("smsSender") class SmsNotificationService { }
```

- **Scanning extra packages**: use `scanBasePackages` to add packages outside the main one. It **replaces** the default, so you must list the main package too.
```java
@SpringBootApplication(scanBasePackages = {"com.shoplite", "com.partner.audit"})
```

- **Not everything comes from scanning**: beans can also come from **`@Bean` methods**, from **`@Import`**, and from **auto-configuration**, which Boot loads separately.

**Follow-ups:**

### Q: A class has @Component but Spring says NoSuchBeanDefinitionException. Why?
- Most often the class is **outside the scanned packages**. For example, the main class is in `com.shoplite.app` but the service is in `com.shoplite.order`, which isn't under it.

- The fix is to **move the main class up to the root package**, or to add the package with `scanBasePackages`.

---

### Q: What happens if the main class has no package?
- Spring then scans the **whole classpath**, including every library JAR. Boot prints a warning, and the app usually **fails to start**.

- So the main class should always be in a **named root package**.

---

### Q: Does scanning also find classes inside JAR dependencies?
- **Yes.** Scanning searches the whole **classpath**, so `@Component` classes in a JAR are found too if they're in a scanned package.

- That's why libraries **don't rely on scanning**. They provide their beans through **auto-configuration** instead.

## Core 11 — @Component vs @Service vs @Repository vs @Controller

**Answer:**
- All four mark a class as a **Spring bean**, so component scanning finds it and registers it. `@Service`, `@Repository` and `@Controller` are **specialised versions of `@Component`**: each one is itself annotated with `@Component`.

- The main difference is **meaning**: they tell the reader **which layer** a class belongs to. `@Controller` is the **web layer**, `@Service` is **business logic**, `@Repository` is **data access**, and `@Component` is for anything else, like helpers or utilities.

- Two of them also add **real behaviour**. `@Repository` turns database exceptions into Spring's **`DataAccessException`**, and `@Controller` makes the class a **web request handler** for Spring MVC. `@Service` adds no extra behaviour today; it only documents the role.

**Key points:**
- **Stereotype annotations**: annotations that mark a class's **role** and make it a bean. Because each one carries `@Component`, scanning treats them all the same way.
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component                       // this is why @Service is found by scanning
public @interface Service { }
```

- **Typical layering** in ShopLite: the controller calls the service, and the service calls the repository.
```java
@RestController  class ProductController { ... }   // web layer: HTTP in, JSON out
@Service         class ProductService { ... }      // business rules
@Repository      class ProductRepository { ... }   // talks to the database
@Component       class SkuGenerator { ... }        // helper that fits no layer
```

- **`@Repository` exception translation**: Spring wraps a `@Repository` bean in a **proxy**. If the database throws a technology-specific exception, like a JPA or JDBC one, the proxy converts it into a **`DataAccessException`**, so the service layer doesn't depend on the database technology.

- **`@Controller` vs `@RestController`**: a `@Controller` method's return value is treated as a **view name**, for example an HTML template. **`@RestController` = `@Controller` + `@ResponseBody`**, so the return value is written straight into the **response body as JSON**, which is what REST APIs need.

- **Spring Data repositories** are interfaces like `ProductRepository extends JpaRepository<...>`. They **don't need `@Repository`**, because Spring Data creates the implementation and already applies exception translation.

- **Using the right one matters**: it makes code easier to read, and tools and AOP can target a layer, for example a pointcut on all `@Service` classes.

**Follow-ups:**

### Q: Can I use @Component everywhere instead?
- **Technically yes**, for services, because the bean would still be created and injected. But you lose the **readable layer information**.

- For repositories and controllers, you also lose **behaviour**: no exception translation without `@Repository`, and no request handling without `@Controller`.

---

### Q: Does @Service do anything special?
- **No**, not today. It behaves exactly like `@Component`.

- It's still used because it clearly marks **where the business logic lives**, and Spring may give it extra meaning in the future.

---

### Q: Can I create my own stereotype annotation?
- **Yes.** You annotate your own annotation with `@Component` (or `@Service`), and scanning picks up every class that uses it.

- Teams sometimes do this to name their own roles, such as `@UseCase` in a clean-architecture project.
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface UseCase { }
```

---

### Q: What happens if I return a String from a @Controller method?
- Spring treats the String as a **view name** and tries to render a template with that name. Without a matching template, you get an **error page** instead of your text.

- To return the text or JSON itself, add **`@ResponseBody`** to the method, or use **`@RestController`** on the class.

## Core 12 — @Bean vs @Component

**Answer:**
- Both register an object as a **Spring bean**, but in different ways. **`@Component`** goes **on a class**, and component scanning finds it and creates the object for me. **`@Bean`** goes **on a method** inside a `@Configuration` class, and Spring registers **whatever that method returns**.

- I use **`@Component`** (or `@Service`, `@Repository`, `@Controller`) for **my own classes**. It's the simplest option because Spring creates the object automatically.

- I use **`@Bean`** when I **can't or don't want to annotate the class**, mainly **library classes** like `Clock` or `ObjectMapper`. It's also the choice when creating the object needs **custom logic**, like reading a property to decide which implementation to build.

**Key points:**
- **`@Component`, on the class**: Spring finds it by scanning and calls the constructor itself.
```java
@Component
public class SkuGenerator { ... }
```

- **`@Bean`, on a method**: I write the creation code myself, and the **returned object** becomes the bean. The method lives in a **`@Configuration`** class.
```java
@Configuration
public class AppConfig {
    @Bean
    Clock clock() {
        return Clock.systemUTC();      // library class: I can't put @Component on it
    }
}
```

- **Custom creation logic**: inside a `@Bean` method I can use `if` statements, builders or properties to decide **what object to create**.
```java
@Bean
TaxCalculator taxCalculator(@Value("${shoplite.tax.region:EU}") String region) {
    return region.equals("US") ? new UsTaxCalculator() : new EuTaxCalculator();
}
```

- **Dependencies of a `@Bean` method** are passed in as **method parameters**, and Spring injects them just like constructor parameters.

- **Bean names**: a `@Component` bean is named after the **class** (`skuGenerator`), and a `@Bean` is named after the **method** (`clock`). I can override either one, for example `@Bean("utcClock")`.

- **Same features for both**: scopes, `@Primary`, `@Lazy`, `@Profile` and lifecycle callbacks work on both. For `@Bean` I can also set `initMethod` and `destroyMethod` for library classes that have no annotations.

**Follow-ups:**

### Q: Can I use @Bean and @Component for the same class?
- **Technically yes**, but then Spring creates **two beans of the same type**. Injecting by type then fails with **`NoUniqueBeanDefinitionException`**.

- So pick **one way per class**: `@Component` for your own classes, and `@Bean` for everything else.

---

### Q: Why put @Bean methods in a @Configuration class and not a @Component?
- In a **`@Configuration`** class, Spring makes sure that calling one `@Bean` method from another returns the **same singleton bean**, instead of creating a new object.

- In a plain `@Component`, `@Bean` methods still work, but that call creates a **new object every time**.

---

### Q: Which one does Spring Boot use for auto-configuration?
- **`@Bean` methods.** Auto-configuration classes are `@Configuration` classes full of `@Bean` methods, usually guarded by conditions like **`@ConditionalOnMissingBean`**.

- That's how Boot creates beans like `ObjectMapper` or `DataSource` only if **I haven't defined my own**.

## Core 13 — How does @Autowired resolve dependencies (type → qualifier → name)?

**Answer:**
- Spring first looks for beans by **type**. If exactly **one bean** matches the declared type, it's injected, and if **none** match, startup fails with **`NoSuchBeanDefinitionException`**.

- If **several beans** match, Spring narrows them down in order. A **`@Qualifier`** at the injection point wins first, then a bean marked **`@Primary`**, and finally a bean whose **name matches the field or parameter name**.

- If it still can't pick exactly one, startup fails with **`NoUniqueBeanDefinitionException`**. Both of these exceptions show up wrapped inside an **`UnsatisfiedDependencyException`**, which names the bean and parameter that failed, so the real reason is in the **`Caused by:`** line.

- The same rules apply to constructor parameters, setters and fields, and with a **single constructor** I don't even need the `@Autowired` annotation.

**Key points:**
- **Step 1, by type**: Spring collects every bean that **is assignable to** the declared type, so an interface matches all of its implementations.
```java
public OrderService(NotificationService notificationService) { ... }
// candidates: emailNotificationService, smsNotificationService
```

- **Step 2, `@Qualifier`**: it filters the candidates to the bean with that **name** (or a matching custom qualifier). It's the strongest rule because it's written right at the injection point.
```java
public OrderService(@Qualifier("smsNotificationService") NotificationService notifier) { ... }
```

- **Step 3, `@Primary`**: if candidates are still left, the one marked **`@Primary`** is the **default choice**. Spring 6.2 added the opposite, **`@Fallback`**, which marks a bean to use only when no other candidate is left.
```java
@Primary
@Service
public class EmailNotificationService implements NotificationService { ... }
```

- **Step 4, name match**: as a last resort, Spring matches the **parameter or field name** against the **bean names**. This only works for constructor parameters if the code is compiled with the **`-parameters`** flag, which the Spring Boot Maven parent turns on by default (Spring 6.1 stopped reading names from debug info).
```java
public OrderService(NotificationService emailNotificationService) { ... }  // picks the email bean
```

- **Injecting all matches**: when I want every implementation, I inject a **`List<T>`** (ordered by `@Order`) or a **`Map<String, T>`** keyed by bean name. Then no choosing happens at all.
```java
public NotificationDispatcher(Map<String, NotificationService> channels) { ... }
```

- **Optional dependencies**: if a bean might not exist, I use **`Optional<T>`** or **`ObjectProvider<T>`** instead of failing at startup. `@Autowired(required = false)` also works, but only for fields and setters.

**Follow-ups:**

### Q: Which wins: @Qualifier or @Primary?
- **`@Qualifier` wins.** `@Primary` sets the **global default**, but a `@Qualifier` at a specific injection point **overrides** it for that one place.

- The usual setup is `@Primary` on the common implementation and `@Qualifier` only where I need a different one.

---

### Q: How is @Autowired different from @Resource and @Inject?
- **`@Inject`** (Jakarta) works like `@Autowired`: it resolves **by type first**, but it has no `required` attribute.

- **`@Resource`** (Jakarta) resolves **by name first**, using the field name or its `name` attribute, and only falls back to type. It also can't be used on constructors.

---

### Q: Do I need @Autowired on a constructor?
- **Not if the class has only one constructor.** Spring has used it automatically since **4.3**.

- If there are **several constructors**, I put `@Autowired` on the one Spring should use.

---

### Q: What are the common causes of NoSuchBeanDefinitionException?
- The class is **outside the component-scan packages** (not under the `@SpringBootApplication` package), or it's **missing a stereotype annotation** like `@Service`.

- The bean exists only under a **`@Profile` or condition** that isn't active, or a **`@Qualifier` name doesn't match** any bean. If the dependency is genuinely optional, I inject it as `Optional<T>` or `ObjectProvider<T>`.

## Core 14 — @Value vs @ConfigurationProperties (incl. records)

**Answer:**
- Both read values from **configuration** like `application.yaml`, environment variables or command-line arguments. **`@Value`** injects **one property** at a time using a placeholder like `${shoplite.tax.region}`. **`@ConfigurationProperties`** binds a **whole group of properties** under a prefix into one **type-safe object**.

- I use **`@Value`** for a **single, simple value** in one place, or when I need a **SpEL expression** (Spring Expression Language, written as `#{...}`). For anything with **several related settings**, I use **`@ConfigurationProperties`**, because the settings live in one class, are **validated at startup**, and can be injected anywhere like any other bean.

- In modern Spring Boot, the cleanest form is a **Java record** annotated with `@ConfigurationProperties`. It's **immutable**, Boot binds it through the **constructor** automatically, and since Boot 3 I no longer need `@ConstructorBinding` for that.

**Key points:**
- **`@Value`, one property**: a `${...}` placeholder with an optional **default after the colon**. Without a default, a missing property **fails startup** with *"Could not resolve placeholder"*.
```java
TaxCalculator taxCalculator(@Value("${shoplite.tax.region:EU}") String region) { ... }
```

- **`@ConfigurationProperties` as a record**: the prefix maps to the record, and each **component** (record field) maps to one key. **`@DefaultValue`** gives a fallback when the key is missing.
```java
@ConfigurationProperties("shoplite.orders")
public record OrderProperties(
        int maxItemsPerOrder,
        BigDecimal freeShippingThreshold,
        @DefaultValue("30s") Duration paymentTimeout,
        List<String> allowedCountries) { }
```

- **Registering it**: put **`@ConfigurationPropertiesScan`** on the main class, or `@EnableConfigurationProperties(OrderProperties.class)` on a config class. After that I inject it like any bean.
```java
@SpringBootApplication
@ConfigurationPropertiesScan
public class ShopliteApplication { ... }
```

- **Validation at startup**: add **`@Validated`** and Bean Validation constraints like `@Min` or `@NotEmpty`. A bad value then **stops the app at startup** with a clear message, instead of failing later at runtime.
```java
@Validated
@ConfigurationProperties("shoplite.orders")
public record OrderProperties(@Min(1) int maxItemsPerOrder, @NotEmpty List<String> allowedCountries) { }
```

- **Relaxed binding**: `@ConfigurationProperties` matches keys loosely. So `max-items-per-order` in YAML, `maxItemsPerOrder`, and the env variable **`SHOPLITE_ORDERS_MAXITEMSPERORDER`** all bind to the same field. `@Value` is much stricter, so I should always use the exact kebab-case name there.

- **Type conversion**: both convert strings to types like `int`, `BigDecimal` and `List`. `@ConfigurationProperties` also understands values like **`30s`** for `Duration` and **`10MB`** for `DataSize`.

**Follow-ups:**

### Q: Why prefer @ConfigurationProperties over many @Value fields?
- Everything for one feature is in **one typed class**, so there are no repeated **string keys** scattered across the code. Typos in a key fail silently with defaults, or fail late without them.

- It also gives **validation**, **relaxed binding**, IDE **autocompletion** (with `spring-boot-configuration-processor`), and it's easy to **create in unit tests** with `new OrderProperties(...)`.

---

### Q: Can @ConfigurationProperties use SpEL?
- **No.** SpEL like `#{2 * 10}` or `#{@someBean.value}` only works in **`@Value`**.

- That's the main reason to still use `@Value`: computed or bean-dependent values.

---

### Q: Where do the values come from, and which source wins?
- Spring reads many **property sources** and the more specific one wins. **Command-line arguments** beat **environment variables**, which beat **profile-specific files** like `application-dev.yaml`, which beat plain `application.yaml`.

- This is why I can keep defaults in the YAML and **override them per environment** with an env variable, without changing code.

## Core 15 — @Profile and @Conditional (foundation of auto-configuration)

**Answer:**
- Both decide **whether a bean is created or skipped** when the app starts. If the check fails, Spring just ignores that bean.

- **`@Profile`** creates a bean only when a certain **profile** is active. A profile is just a name for an environment, like **`dev`** or **`prod`**, so I can use a fake payment gateway in dev and the real one in prod.

- **`@Conditional`** is the more general version: the bean is created only if a **condition** is true, like a property being set. Spring Boot's **auto-configuration** works this way, creating beans only when they're needed.

**Key points:**
- **`@Profile` on a bean**: the bean exists only in that profile. **`!prod`** means "every profile except prod", so it also works when no profile is set.
```java
@Service
@Profile("!prod")
public class FakePaymentGateway implements PaymentGateway { ... }

@Service
@Profile("prod")
public class CardPaymentGateway implements PaymentGateway { ... }
```

- **Turning a profile on**: I set **`spring.profiles.active=prod`**, for example as a command-line argument or the env variable `SPRING_PROFILES_ACTIVE`.

- **Profile config files**: **`application-prod.yaml`** is loaded only when `prod` is active, and its values **override** the ones in `application.yaml`.

- **`@ConditionalOnProperty`**: creates the bean only if a property has a certain value. It's an easy way to build a **feature flag**, a setting that turns a feature on or off.
```java
@Service
@ConditionalOnProperty(name = "shoplite.features.loyalty.enabled", havingValue = "true")
public class LoyaltyPointsService { ... }
```

- **`@ConditionalOnBooleanProperty`** (new in Boot 3.5): a shorter way to write the same thing when the property is just **true or false**. The bean is created when the property is `true`.
```java
@Service
@ConditionalOnBooleanProperty("shoplite.features.loyalty.enabled")
public class LoyaltyPointsService { ... }
```

- **Custom condition**: I can write my own by implementing **`Condition`** and returning true or false from `matches()`.
```java
public class OnWindowsCondition implements Condition {
    public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
        return ctx.getEnvironment().getProperty("os.name", "").startsWith("Windows");
    }
}
```

**Follow-ups:**

### Q: When do I use @Profile vs @ConditionalOnProperty?
- I use **`@Profile`** for things that change **per environment**, like dev vs prod.

- I use **`@ConditionalOnProperty`** to turn **one feature** on or off, in any environment.

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

## Core 17 — Prototype inside singleton (ObjectProvider)

**Answer:**
- When a **prototype** bean is injected into a **singleton**, it's injected **only once**, when the singleton is created. So the singleton keeps using the **same prototype instance** forever, and the prototype scope is effectively lost.

- The fix is to **ask the container for a new instance each time** I need one, instead of injecting it once. The modern way is to inject an **`ObjectProvider<T>`** and call **`getObject()`** whenever I need a fresh bean.

- This is better than injecting the whole **`ApplicationContext`** and calling `getBean()` myself. `ObjectProvider` asks only for the one bean I need, so the dependency stays **clear** and is easy to **mock in tests**.

**Key points:**
- **The problem**: the cart is created once, so every customer shares **the same cart**.
```java
@Service
public class CheckoutService {
    private final ShoppingCart cart;   // prototype, but injected only once
}
```

- **`ObjectProvider<T>`**: a small **factory handle** that Spring injects. Each **`getObject()`** call asks the container for the bean, so a prototype gives a **new instance** every time.
```java
public CheckoutService(ObjectProvider<ShoppingCart> cartProvider) { ... }

ShoppingCart cart = cartProvider.getObject();   // new cart on every call
```

- **Scoped proxy, a trap**: `@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)` injects a proxy instead. But for a prototype, **every method call** on the proxy creates a **new instance**, so `cart.add()` and `cart.getItems()` hit different carts. It fits request and session scopes much better than prototype.

**Follow-ups:**

### Q: What else can ObjectProvider do?
- It handles **optional** beans with **`getIfAvailable()`**, which returns `null` instead of failing when the bean doesn't exist.

- It also handles **multiple** beans with **`getIfUnique()`** and **`stream()`**, which goes through all matching beans.

## Core 18 — Circular dependencies — resolution, constructor injection failure, Boot 2.6+ behaviour

**Answer:**
- A **circular dependency** is when bean A needs bean B, and bean B also needs bean A, directly or through other beans. Spring can't finish creating either one, because each is **waiting for the other**.

- With **constructor injection**, Spring can never resolve it, since neither object can be created without the other already existing. With field or setter injection, Spring used to **work around it** by injecting a half-built ("early") bean, but **since Spring Boot 2.6 circular references are forbidden by default**, so the app fails at startup either way.

- The real fix is to **change the design**, because a cycle usually means two classes have **mixed responsibilities**. As a quick fix, I can put **`@Lazy`** on one of the constructor parameters, which injects a proxy and breaks the cycle.

**Key points:**
- **The cycle**: `OrderPlacementService` needs `InventoryService` to reserve stock, and `InventoryService` needs `OrderPlacementService` to cancel orders.
```java
public OrderPlacementService(InventoryService inventory) { ... }
public InventoryService(OrderPlacementService orders) { ... }
```

- **What Boot shows**: startup fails with *"The dependencies of some of the beans in the application context form a cycle"*, followed by a small diagram of the cycle. The underlying exception is **`BeanCurrentlyInCreationException`**.

- **Boot 2.6+ change**: circular references are **prohibited by default**, even with field injection. The property **`spring.main.allow-circular-references=true`** brings back the old behaviour, but it only works for field and setter injection and just **hides the design problem**.

- **Quick fix with `@Lazy`**: Spring injects a **proxy** instead of the real bean, so it doesn't need the other bean to exist yet. The real bean is looked up on the first method call.
```java
public InventoryService(@Lazy OrderPlacementService orders) { ... }
```

- **Proper fix, redesign**: remove one direction of the dependency. For example, `InventoryService` just **returns a result**, and `OrderPlacementService` decides what to do with it. Other options are moving the shared logic into a **third class**, or using **events** so one side doesn't call the other directly.
```java
boolean reserved = inventory.reserve(product, qty);
if (!reserved) { cancel(orderId); }   // the decision stays in the order service
```

**Follow-ups:**

### Q: Why does constructor injection fail for cycles, but field injection could work?
- With **constructor injection**, an object can't exist until all its dependencies are passed in, so A and B **wait for each other forever**.

- With **field injection**, Spring can create A as an empty object first, inject that **early reference** into B, then finish A. That's why it used to work, but it means beans can see **half-initialized** objects.

---

### Q: Is constructor injection bad because it fails on cycles?
- **No, it's an advantage.** It **surfaces the design problem** at startup instead of hiding it.

- That's one more reason constructor injection is the recommended style, and why Boot made cycles fail by default.

## Core 19 — Design patterns in Spring (Singleton, Factory, Proxy, Template, Observer)

**Answer:**
- Spring is built on classic **design patterns**, and I use most of them every day without writing them myself. The main ones are **Singleton** for beans, **Factory** for creating beans, **Proxy** for AOP and transactions, **Template Method** for classes like `JdbcTemplate`, and **Observer** for application events.

- On top of those, **Dependency Injection** itself is a pattern, and the **Strategy** pattern shows up whenever I inject an interface with several implementations. In Spring MVC, the **DispatcherServlet** is a **Front Controller**.

- The benefit is that Spring handles the boring, repeated parts of these patterns, so my code stays **small and focused on business logic**.

**Key points:**
- **Singleton**: by default, Spring creates **one instance per bean** in the container. Unlike the classic GoF singleton, it's not enforced with a private constructor and a static field; the **container** guarantees it. In ShopLite, `OrderService` is shared by everyone.

- **Factory**: the **`BeanFactory` / `ApplicationContext`** is a big factory that creates and wires objects for me. A **`@Bean` method** is a small factory method, like my `taxCalculator()` that picks the US or EU implementation.
```java
@Bean
TaxCalculator taxCalculator(@Value("${shoplite.tax.region:EU}") String region) {
    return region.equals("US") ? new UsTaxCalculator() : new EuTaxCalculator();
}
```

- **Proxy**: Spring wraps a bean in a **proxy**, a stand-in object that adds behaviour before and after the real method call. That's how **`@Transactional`**, **`@Async`**, **`@Cacheable`** and **`@Lazy`** work, and why calling a method on `this` **skips** that behaviour.

- **Template Method**: **`JdbcTemplate`** and **`TransactionTemplate`** handle the fixed steps, like opening a connection, handling errors and closing resources. I only supply the part that changes, usually as a **lambda**.
```java
List<String> names = jdbcTemplate.query("select name from product",
        (rs, rowNum) -> rs.getString("name"));   // I only write the mapping
```

- **Observer**: one bean **publishes an event**, and any number of **listeners** react to it, without the publisher knowing who they are. In Spring, that's **`ApplicationEventPublisher`** and **`@EventListener`**.
```java
publisher.publishEvent(new OrderPlaced(orderId));

@EventListener
void on(OrderPlaced event) { ... }
```

- **Strategy**: I depend on an **interface** and Spring injects the right implementation, chosen by `@Primary`, `@Qualifier` or `@Profile`. In ShopLite, `PaymentGateway` has a fake and a card implementation, and `NotificationService` has email and SMS.

**Follow-ups:**

### Q: How is a Spring singleton different from the GoF Singleton pattern?
- The **GoF singleton** means one instance per **class loader (in practice, per JVM)**, enforced by the class itself with a private constructor.

- A **Spring singleton** means one instance **per container and per bean definition**. I can still create other instances with `new`, and I can even define **two beans of the same class** with different names.

---

### Q: Which pattern is behind @Transactional, and what's the catch?
- It's the **Proxy** pattern: Spring wraps my bean, starts a transaction before the method and commits or rolls back after it.

- The catch is **self-invocation**: if a method calls another `@Transactional` method on **`this`**, the call doesn't go through the proxy, so **no transaction** is started.
