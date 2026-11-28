# Spring REST — Notes

## REST 1 — What is REST? Constraints, resources, URL design

**Answer:**
- REST stands for **Representational State Transfer**. It's an **architectural style** for building web APIs on top of HTTP, not a protocol or a framework. The core idea is that I model my system as **resources**, like products and orders, and give each one a **URL**.

- The client works with those resources using the **standard HTTP methods**. **GET** reads, **POST** creates, **PUT/PATCH** update, and **DELETE** removes. The data goes back and forth as a **representation** of the resource, usually **JSON**, and the server answers with **standard status codes** like 200, 201 or 404.

- What makes an API truly RESTful are a few **constraints**. The most important one is that it's **stateless**: every request carries everything the server needs, like the auth token, so any instance can handle it and the API **scales easily**. The others are a **uniform interface**, **cacheable** responses, a **client-server** split and a **layered system**.

**Key points:**
- **Resources and URIs**: each business entity is exposed as a **resource** with a stable URI, such as `/api/products/{id}`. The URI identifies **what** is being accessed, and the **HTTP method** defines the operation, so actions never appear in the path.
```http
GET    /api/products        → 200 OK           (list)
GET    /api/products/1      → 200 OK           (single resource)
POST   /api/products        → 201 Created      (new resource)
DELETE /api/products/1      → 204 No Content
GET    /api/products?page=0&size=20&sort=price   (filtering and paging via query params)
```

- **Representations**: clients never handle a resource directly; they exchange a **representation** of its state, typically **JSON**. The format is negotiated through the **`Accept`** and **`Content-Type`** headers, and in Spring this is handled by **`HttpMessageConverter`s**.

- **Statelessness**: the server holds **no client session** between requests, so every request is **self-contained** and carries its own credentials, usually a token in the **`Authorization`** header. This is what enables **horizontal scaling**, because any instance behind the load balancer can serve any request.

- **Uniform interface**: all resources follow the **same method semantics and status codes**. Clients, proxies and gateways can therefore rely on standard HTTP behaviour; for example, a **GET is safe** and can be retried or cached without side effects.

- **Cacheability**: responses explicitly state whether and how long they may be cached, using **`Cache-Control`** and **`ETag`**. This reduces **latency** and **server load** for frequently read data.

- **Client–server and layered system**: separating client and server lets the **frontend and backend evolve independently**. Intermediaries such as an **API gateway, load balancer or CDN** can be added transparently, without the client being aware of them. **Code on demand** is the sixth constraint and the only **optional** one.

**Follow-ups:**

### Q: When would you not use REST?
- **GraphQL** fits when clients need **flexible queries** and want exactly the fields they ask for in one call, like a mobile app with many screens.

- **gRPC** fits **fast service-to-service** calls, using binary Protobuf and HTTP/2. **Messaging** (Kafka, RabbitMQ) fits when the caller **doesn't need an immediate answer**. REST stays the default for **public and CRUD-style APIs**.

## REST 2 — Safe vs idempotent methods; why idempotency matters

**Answer:**
- A **safe** method is **read-only**: it must not change any state on the server. **GET, HEAD and OPTIONS** are safe.

- An **idempotent** method gives the **same server state** whether the request is sent **once or many times**. All safe methods are idempotent, and so are **PUT** and **DELETE**. **POST** is not idempotent, and **PATCH** isn't guaranteed to be.

- This matters because networks fail, and clients, gateways and HTTP libraries **retry requests automatically**. Retrying an idempotent request is harmless. Retrying a POST can create a **duplicate order or a double payment**, so POST needs extra protection, such as an **idempotency key**.

**Key points:**
- **Method overview**:

| Method | Safe | Idempotent |
|---|---|---|
| GET, HEAD, OPTIONS | Yes | Yes |
| PUT | No | Yes |
| DELETE | No | Yes |
| POST | No | No |
| PATCH | No | Not guaranteed |

- **Idempotency is about server state, not the response**: the first `DELETE /api/products/3` returns **204**, and a repeat returns **404**. The method is still idempotent, because after both calls the product is **gone**.

- **Why PUT is idempotent and POST isn't**: PUT **replaces** the resource with the state in the request, so repeating it yields the same result. POST **creates a new resource** each time, so two identical POSTs produce **two records**.
```http
PUT  /api/products/1  {"name":"Keyboard","price":39.99}   → same product, same state every time
POST /api/products    {"name":"Monitor","price":199.99}   → id 3, then id 4 on retry
```

- **PATCH depends on the operation**: "set the price to 39.99" is idempotent, while "**increase stock by 5**" is not, because each repeat changes the result.

- **Retries in practice**: HTTP clients, API gateways and resilience libraries like **Resilience4j** retry **idempotent methods** safely. POST should **never be retried blindly**.

**Follow-ups:**

### Q: How do you make a POST safe to retry?
- The client sends a unique **`Idempotency-Key`** header with the request. The server **stores the key with the result**, and if the same key arrives again it **returns the stored response** instead of creating a new record.

- This is how payment APIs like **Stripe** prevent double charges. It's covered in detail in REST 4.

---

### Q: Why should a GET endpoint never change data?
- Because everyone in the chain treats GET as safe. **Browsers prefetch** links, **crawlers** follow them, and **caches and retries** may repeat them.

- An endpoint like `GET /orders/42/cancel` could therefore cancel orders **without any user action**. State changes belong in **POST, PUT, PATCH or DELETE**.

## REST 3 — PUT vs PATCH vs POST

**Answer:**
- **POST** creates a **new** resource. Each call creates another one, so it's **not idempotent**.

- **PUT** **replaces the whole** resource. Fields you don't send are lost, and repeating it gives the same result, so it's **idempotent**.

- **PATCH** **updates only the fields you send**. Everything else stays the same.

**Key points:**
- **Comparison**:

| | POST | PUT | PATCH |
|---|---|---|---|
| Purpose | Create | Replace whole resource | Update some fields |
| Target URI | Collection `/products` | Resource `/products/1` | Resource `/products/1` |
| Body | New resource, no id | **Complete** resource | **Only changed** fields |
| Idempotent | No | Yes | Not guaranteed |
| Typical response | 201 + Location | 200 (or 204) | 200 (or 204) |

- **PUT vs PATCH with the same body**: PUT wipes the fields you didn't send, while PATCH keeps them.
```http
PUT   /api/products/1  {"price":39.99}   → {"id":1,"name":null,"price":39.99}
PATCH /api/products/1  {"price":39.99}   → {"id":1,"name":"Keyboard","price":39.99}
```

- **Easy way to remember**: **POST** is "New File", **PUT** is overwriting the file, and **PATCH** is editing one line.

**Follow-ups:**

### Q: Can POST be used for updates?
- Technically yes, and it's used for **actions** that don't fit CRUD, such as `POST /api/orders/42/cancel`, which **triggers a process** rather than editing fields.

- For plain field updates, PUT or PATCH is preferred, because their **semantics are clear** and PUT is **safe to retry**.

## REST 4 — Making POST idempotent (idempotency keys)

**Answer:**
- POST isn't idempotent, so if a client **retries** after a timeout, it can create a **duplicate order** or charge a card twice.

- The fix is an **idempotency key**: the client generates a unique ID (usually a **UUID**) and sends it in an **`Idempotency-Key`** header.

- The server **stores the key with the result**. If the same key arrives again, it **returns the stored result** instead of processing the request a second time.

**Key points:**
- **Flow**:
  1. The client creates a UUID **once per user action**, such as one click on "Place order".
  2. The server checks whether it has **seen the key before**.
  3. For a **new key**, it processes the request and saves the key with the response.
  4. For a **known key**, it returns the **saved response**, so no new order is created.
```http
POST /api/orders
Idempotency-Key: 7f3c9a2e-1b4d-4e8f-9a6b-2c5d8e1f0a3b
{"customerEmail":"alice@shoplite.com","product":"keyboard"}
```

- **The client reuses the same key for retries**, and a **new action gets a new key**. If the client generated a new key on every retry, the protection would be gone.

- **Where to store keys**: in production, a **database table with a unique constraint** on the key, or **Redis**, with an **expiry** (for example 24 hours). An in-memory map works only for a single instance and is lost on restart.

- **Concurrent duplicates**: two identical requests can arrive **at the same moment**. The check-and-save must be **atomic**, such as a DB unique constraint or Redis `SET NX`, so only one of them is processed.

- **Same key with a different body**: this is a client bug. The server should reject it, typically with **422 Unprocessable Content**, instead of returning the old result.

**Follow-ups:**

### Q: Why not just detect duplicates by comparing the order details?
- Two orders with the **same details can be legitimate**, for example a customer buying the same keyboard twice on purpose. Only the client knows whether a request is a **retry or a new action**, and the idempotency key is how it tells the server.

---

### Q: Where have you seen this in real systems?
- **Payment APIs** like **Stripe** and PayPal require an idempotency key on charge requests, which is why a retry never charges twice.

- The same idea appears in messaging as **idempotent consumers**, which record processed message IDs so a redelivered message isn't applied twice.

## REST 5 — Key status codes

**Answer:**
- Status codes tell the client **what happened** without it having to read the body. **2xx** means success, **4xx** means the **client** made a mistake, and **5xx** means the **server** failed.

- The ones I use most are **200** OK, **201** Created, **204** No Content, **400** Bad Request, **401** Unauthorized, **403** Forbidden, **404** Not Found, **409** Conflict, and **500** Internal Server Error.

- The classic interview pair is **401 vs 403**. 401 means **"I don't know who you are"**, and 403 means **"I know who you are, but you're not allowed."**

**Key points:**
- **Quick reference**:

| Code | Meaning | Typical use |
|---|---|---|
| **200** OK | Success with a body | GET, PUT, PATCH |
| **201** Created | New resource created | POST, with a **`Location`** header |
| **204** No Content | Success, no body | DELETE |
| **400** Bad Request | Request is malformed | Invalid JSON, wrong type (`/products/abc`) |
| **401** Unauthorized | **Not authenticated** | Missing or invalid credentials |
| **403** Forbidden | **Authenticated, but not allowed** | Wrong role |
| **404** Not Found | Resource doesn't exist | `GET /products/99` |
| **409** Conflict | Clashes with current state | Duplicate email, version conflict |
| **422** Unprocessable Content | Valid JSON, but breaks business rules | Quantity is negative |
| **500** Internal Server Error | Unexpected server bug | Unhandled exception |

- **401 vs 403**: **401** is about **authentication** (who are you?), so the fix is to log in or send a valid token. **403** is about **authorization** (what may you do?), so logging in again won't help.

- **400 vs 422**: **400** means the server **couldn't read** the request, such as broken JSON or a wrong type. **422** means it **could read it**, but the data **fails validation or business rules**. Spring returns **400** for validation errors by default, and many teams keep it that way.

- **A missing resource must be 404, not 500**: a 500 tells the client "the server is broken" and triggers alerts, when really the client asked for something that **doesn't exist**.

**Follow-ups:**

### Q: Why not always return 200 with an error message in the body?
- Clients, **gateways, monitoring and retry logic** all rely on the status code. A "200 with an error" looks successful to all of them, so failures go **unnoticed** and caches may even **store the error**.

---

### Q: When would you return 404 instead of 403?
- When even **revealing that the resource exists** is a security risk. For example, if a user asks for another customer's order, returning **404** hides that the order exists at all. GitHub does this for private repositories.

## REST 6 — @RestController vs @Controller + @ResponseBody

**Answer:**
- **`@RestController`** is simply **`@Controller` + `@ResponseBody`** combined into one annotation.

- With plain **`@Controller`**, a returned String is treated as a **view name**, such as a Thymeleaf template, and Spring renders an **HTML page**.

- With **`@ResponseBody`** (or `@RestController`), the return value is **written directly into the response body**, as JSON for objects or plain text for Strings. That's what REST APIs need.

**Key points:**
- **Same method, different result**:
```java
@Controller
class PageController {
    @GetMapping("/hello")
    String hello() { return "hello"; }      // renders the template hello.html
}

@RestController
class ApiController {
    @GetMapping("/hello")
    String hello() { return "hello"; }      // response body is the text: hello
}
```

- **Who converts the return value**: with `@ResponseBody`, Spring uses an **`HttpMessageConverter`**. **Jackson** turns objects into **JSON**, and Strings are written as **plain text**.

- **When to use which**: **`@RestController`** for **REST APIs** (JSON), and **`@Controller`** for **server-rendered web pages** (Thymeleaf, JSP). In modern apps with a React or Angular frontend, almost all controllers are `@RestController`.

- **Mixing in one class**: a `@Controller` can still return data from a single method by putting **`@ResponseBody` on that method only**.

**Follow-ups:**

### Q: What happens if you forget @ResponseBody on a @Controller method that returns a String?
- Spring treats the String as a **view name** and tries to find a template with that name. If there's no template engine, it fails with a **"Circular view path"** error and a **500**, because it tries to forward back to the same URL.
