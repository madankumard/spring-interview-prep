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
