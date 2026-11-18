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

- Fix it with **`@Primary`** on the default implementation, or **`@Qualifier("beanName")`** at the injection point (see Core 15). To get **all** of them, inject a `List<NotificationService>`.

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

- **Prototype inside a singleton** is the main trap: it's injected **only once**, so it acts like a singleton. Fix it with **`ObjectProvider`** or **`@Lookup`** (see Core 20).

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
