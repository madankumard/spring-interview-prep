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
