# Spring Interview Prep — Questions + Hands-On Roadmap

## How to use this file

**Daily workflow (with Claude Code, see CLAUDE.md):**
1. Pick the next question and ask Claude Code by its ID, e.g. `Core 5` or `JPA 14`.
2. Claude answers interview-style, shows code where useful, and appends the answer to `notes/<topic>.md`.
3. Type the code yourself into the ShopLite project and run it.
4. Tick `[x]` here once you can **explain it out loud** AND **point to code** that shows it.

**Question IDs:** `Core`, `Boot`, `REST`, `JPA`, `AOP`, `Security`, `MS` (Microservices), `Cloud`, `Test` + number.

**Suggested order:** Core → Boot → REST → JPA → AOP → Security → Test → MS → Cloud.
This follows the project phases in Part 1, so the code builds up naturally.

⭐ = high priority (do these first if short on time).

---

## Part 1 — Hands-On Roadmap (one project, built in phases)

Build ONE realistic app and grow it: **"ShopLite" — products, orders, inventory, users.**
The phases below are the project context Claude uses to fit code examples into one app.
You don't need to do them separately — answering the questions in order covers them.

### Phase 0 — Setup (½ day)
- [ ] Git repo + this file + README with a "What I learned" log
- [ ] Java 25, latest Spring Boot via start.spring.io (Web, JPA, Validation, PostgreSQL, Actuator, Testcontainers)
- [ ] Docker Desktop running (needed for Postgres + Testcontainers)

### Phase 1 — Spring Core playground (1–2 days) → Core Q1–19
- [ ] Plain module (no web): `NotificationService` with Email/SMS implementations → DI, @Primary, @Qualifier
- [ ] Break it on purpose: trigger NoUniqueBeanDefinitionException and NoSuchBeanDefinitionException, then fix
- [ ] Log every lifecycle callback (constructor, @PostConstruct, BeanPostProcessor, @PreDestroy)
- [ ] Singleton vs prototype demo; inject prototype into singleton, fix with ObjectProvider
- [ ] Create a circular dependency, see Boot fail, fix it
- [ ] @Profile("dev"/"prod") beans + @ConditionalOnProperty

### Phase 2 — Boot + REST API (3–4 days) → Boot + REST sections
- [ ] Product & Order CRUD with @RestController, DTOs (records), ResponseEntity, 201 + Location
- [ ] @Valid validation + @RestControllerAdvice returning ProblemDetail
- [ ] Pagination & sorting with Pageable
- [ ] @ConfigurationProperties for app settings; application-dev.yml / application-prod.yml
- [ ] Idempotency key on POST /orders
- [ ] API versioning (Spring 7 built-in or header-based)
- [ ] springdoc OpenAPI / Swagger UI
- [ ] Actuator health, metrics, custom health indicator; liveness/readiness
- [ ] A HandlerInterceptor AND a Filter that both log a request ID → compare
- [ ] Enable virtual threads, observe thread names in logs

### Phase 3 — Data JPA deep dive (3–4 days) → JPA section
- [ ] Entities: Product, Order, OrderItem, Customer with proper owning side / mappedBy
- [ ] Flyway migrations (ddl-auto = validate)
- [ ] Reproduce N+1 with SQL logging ON, then fix with JOIN FETCH and @EntityGraph
- [ ] Reproduce LazyInitializationException (open-in-view=false), then fix properly
- [ ] Interface + record projections
- [ ] Specifications for dynamic product search
- [ ] @Version optimistic locking on inventory; simulate two concurrent updates
- [ ] Transaction propagation demo: REQUIRED vs REQUIRES_NEW (audit log that survives rollback)
- [ ] Self-invocation bug with @Transactional — reproduce and fix
- [ ] Publish an OrderPlaced event: @EventListener vs @TransactionalEventListener(AFTER_COMMIT)
- [ ] Auditing (@CreatedDate, @LastModifiedBy)
- [ ] Batch insert 10k rows: compare IDENTITY vs SEQUENCE

### Phase 4 — AOP (1 day) → AOP section
- [ ] Custom @LogExecutionTime annotation + @Around aspect
- [ ] Aspect for auditing service calls (@AfterReturning / @AfterThrowing)
- [ ] Two aspects with @Order — print execution order
- [ ] Prove self-invocation skips advice

### Phase 5 — Security (3–4 days) → Security section
- [ ] SecurityFilterChain bean (no WebSecurityConfigurerAdapter)
- [ ] Users in DB via UserDetailsService + BCrypt
- [ ] Stateless JWT using OAuth2 Resource Server (issue tokens from an auth endpoint or Keycloak)
- [ ] Roles vs authorities; @PreAuthorize on service methods
- [ ] Custom AuthenticationEntryPoint (401) and AccessDeniedHandler (403)
- [ ] CORS config for a frontend origin; explain why CSRF is disabled for stateless API
- [ ] Rate-limit login attempts

### Phase 6 — Testing (woven in, plus 2 dedicated days) → Testing section
- [ ] Unit tests with JUnit 5 + Mockito for services
- [ ] @WebMvcTest controller tests with MockMvc/MockMvcTester + @MockitoBean
- [ ] @DataJpaTest with Testcontainers Postgres + @ServiceConnection
- [ ] Security tests: @WithMockUser and jwt()
- [ ] One full @SpringBootTest(RANDOM_PORT) end-to-end test
- [ ] WireMock for an external "payment" API

### Phase 7 — Split into microservices (5–7 days) → Microservices + Cloud sections
- [ ] Split into product-service, order-service, inventory-service
- [ ] Spring Cloud Gateway in front (routes, auth at gateway, rate limiting)
- [ ] Eureka (or Kubernetes services) for discovery; Config Server for shared config
- [ ] Service-to-service calls with HTTP interface clients (@HttpExchange)
- [ ] Resilience4j circuit breaker + retry + timeout with fallback
- [ ] Kafka: order-placed event → inventory reserves stock (idempotent consumer)
- [ ] Saga (choreography) for order → payment → inventory, with compensation
- [ ] Transactional Outbox for reliable event publishing
- [ ] Micrometer Tracing + Zipkin; trace ID in every log line
- [ ] docker-compose for everything; optional: deploy to local Kubernetes (kind/minikube)
- [ ] Contract test between order-service and inventory-service

### Phase 8 — Modern extras (optional, 1–2 days)
- [ ] GraalVM native image of one service; note what broke
- [ ] Buildpacks image (`bootBuildImage`), layered jars
- [ ] Boot 2 → 3 migration notes (javax → jakarta)

---

## Part 2 — Question Bank

### 1. Spring Core (ID: Core)

**Must-know**
- [x] ⭐ **1.** What is Spring?
- [x] ⭐ **2.** IoC and DI — how they enable loose coupling (with code example)
- [x] ⭐ **3.** What is a Spring bean? What does the IoC container do?
- [x] ⭐ **4.** BeanFactory vs ApplicationContext (one-liner)
- [x] ⭐ **5.** Full bean lifecycle walkthrough
- [x] ⭐ **6.** What are the different bean scopes in Spring?
- [x] ⭐ **7.** Singleton vs prototype — the differences
- [x] ⭐ **8.** Are singleton beans thread-safe? How to make them safe?
- [x] ⭐ **9.** Constructor vs setter vs field injection — why constructor is preferred

**Configuration & annotations**
- [x] **10.** How does @ComponentScan find beans?
- [x] ⭐ **11.** @Component vs @Service vs @Repository vs @Controller
- [x] ⭐ **12.** @Bean vs @Component
- [x] ⭐ **13.** How does @Autowired resolve dependencies (type → qualifier → name)?
- [x] **14.** @Value vs @ConfigurationProperties (incl. records)
- [x] **15.** @Profile and @Conditional (foundation of auto-configuration)
- [x] **16.** @Lazy — what and when

**Advanced**
- [x] **17.** Prototype inside singleton (ObjectProvider)
- [ ] ⭐ **18.** Circular dependencies — resolution, constructor injection failure, Boot 2.6+ behaviour
- [ ] **19.** Design patterns in Spring (Singleton, Factory, Proxy, Template, Observer)

---

### 2. Spring Boot (ID: Boot)

**Must-know**
- [ ] **1.** What is Spring Boot and how is it different from plain Spring?
- [ ] ⭐ **2.** What happens step by step in SpringApplication.run()?
- [ ] ⭐ **3.** What @SpringBootApplication combines and what each part does
- [ ] **4.** Starters and dependency version management (BOM / parent POM)
- [ ] ⭐ **5.** How auto-configuration works (AutoConfiguration.imports, @Conditional*)
- [ ] ⭐ **6.** Overriding auto-configured beans (@ConditionalOnMissingBean) and excluding auto-configs
- [ ] **7.** Embedded servers — swap Tomcat for Jetty/Undertow, change port, disable web server
- [ ] **8.** @RestController vs @Controller

**Configuration**
- [ ] ⭐ **9.** Externalized config precedence (properties, YAML, env vars, CLI args)
- [ ] **10.** @ConfigurationProperties — binding, @Validated, records
- [ ] **11.** Profiles — activation, profile-specific files, @Profile, profile groups
- [ ] **12.** spring.config.import and loading external config/secrets

**Web & data**
- [ ] ⭐ **13.** Global exception handling (@ControllerAdvice) + ProblemDetail (RFC 9457)
- [ ] **14.** Request validation with @Valid / Bean Validation
- [ ] **15.** How Boot configures DataSource + JPA (Hibernate default, HikariCP)
- [ ] **16.** spring.jpa.open-in-view — why Boot warns about it
- [ ] **17.** @Transactional behaviour in Boot apps

**Testing**
- [ ] ⭐ **18.** @SpringBootTest vs slices (@WebMvcTest, @DataJpaTest)
- [ ] **19.** @MockitoBean vs @MockBean (deprecated)
- [ ] **20.** Testcontainers with @ServiceConnection

**Production readiness**
- [ ] ⭐ **21.** Actuator — key endpoints, exposing, securing
- [ ] ⭐ **22.** Liveness and readiness probes for Kubernetes
- [ ] ⭐ **23.** Observability with Micrometer — metrics, tracing, trace IDs in logs
- [ ] **24.** Logging — runtime level changes, log groups, Logback
- [ ] **25.** Graceful shutdown
- [ ] **26.** CommandLineRunner vs ApplicationRunner

**Modern Boot (3+/4)**
- [ ] **27.** Boot 2 → 3 migration (Jakarta, Java 17, breaking changes)
- [ ] **28.** GraalVM native images and AOT
- [ ] **29.** Virtual threads
- [ ] **30.** Executable JAR internals, layered jars, Buildpacks
- [ ] **31.** Docker Compose support in development
- [ ] **32.** DevTools — what it does, why it's off in production

---

### 3. Spring REST, incl. Spring MVC (ID: REST)

**REST fundamentals**
- [ ] **1.** What is REST? Constraints and resources
- [ ] ⭐ **2.** Safe vs idempotent methods; why idempotency matters
- [ ] ⭐ **3.** PUT vs PATCH vs POST
- [ ] **4.** Making POST idempotent (idempotency keys)
- [ ] ⭐ **5.** Key status codes (200, 201+Location, 204, 400, 401 vs 403, 404, 409, 422, 500)
- [ ] **6.** URL design best practices

**Spring MVC core**
- [ ] **7.** @RestController vs @Controller + @ResponseBody
- [ ] **8.** @RequestMapping and shortcuts; consumes/produces
- [ ] ⭐ **9.** @PathVariable vs @RequestParam vs @RequestBody vs @RequestHeader
- [ ] **10.** ResponseEntity vs @ResponseStatus
- [ ] ⭐ **11.** HttpMessageConverter and content negotiation
- [ ] **12.** Request flow through DispatcherServlet (HandlerMapping → HandlerAdapter → converter)
- [ ] **13.** Why DTOs instead of exposing entities
- [ ] **14.** Filter vs HandlerInterceptor vs AOP
- [ ] **15.** Spring MVC vs WebFlux — and how virtual threads change the choice

**Errors & validation**
- [ ] ⭐ **16.** @RestControllerAdvice + @ExceptionHandler
- [ ] **17.** ProblemDetail (RFC 9457)
- [ ] ⭐ **18.** @Valid / @Validated, custom validators, MethodArgumentNotValidException

**API design**
- [ ] **19.** API versioning strategies; Spring Framework 7 built-in versioning
- [ ] **20.** Pagination and sorting (Pageable)
- [ ] **21.** Caching with ETag / Cache-Control
- [ ] **22.** OpenAPI / Swagger (springdoc)
- [ ] **23.** File upload/download (multipart)

**Security**
- [ ] ⭐ **24.** Securing REST APIs — stateless JWT via OAuth2 Resource Server
- [ ] **25.** CORS
- [ ] **26.** Rate limiting and API gateway basics

**HTTP clients**
- [ ] ⭐ **27.** RestTemplate vs WebClient vs RestClient — which to use today
- [ ] **28.** HTTP interface clients (@HttpExchange)
- [ ] **29.** Timeouts, retries, circuit breakers for outbound calls

**Testing**
- [ ] **30.** @WebMvcTest with MockMvc / MockMvcTester
- [ ] **31.** Testing clients with MockRestServiceServer / WireMock

---

### 4. Spring Data JPA (ID: JPA)

**Fundamentals**
- [ ] **1.** JDBC vs JPA vs Hibernate vs Spring Data JPA
- [ ] ⭐ **2.** Entity states (transient, managed, detached, removed)
- [ ] ⭐ **3.** Persistence context and dirty checking
- [ ] **4.** persist() vs merge()
- [ ] **5.** Flush timing and flush modes

**Mapping**
- [ ] **6.** ID generation strategies (IDENTITY, SEQUENCE, UUID) and batching impact
- [ ] **7.** Owning side, mappedBy, bidirectional helper methods
- [ ] **8.** Cascade types vs orphanRemoval
- [ ] **9.** List vs Set for to-many; MultipleBagFetchException
- [ ] **10.** equals()/hashCode() for entities
- [ ] **11.** @Embeddable and inheritance strategies

**Fetching & performance**
- [ ] ⭐ **12.** LAZY vs EAGER; why ManyToOne should be lazy
- [ ] ⭐ **13.** LazyInitializationException — right fixes vs anti-patterns
- [ ] ⭐ **14.** N+1 problem — JOIN FETCH, @EntityGraph, batch size
- [ ] **15.** Projections — interface, DTO, record
- [ ] **16.** Efficient batch inserts/updates
- [ ] **17.** L1 vs L2 cache, query cache

**Repositories**
- [ ] **18.** Repository hierarchy (CrudRepository, ListCrudRepository, JpaRepository)
- [ ] **19.** Derived queries vs @Query (JPQL / native)
- [ ] **20.** @Modifying and clearAutomatically
- [ ] **21.** Specifications / Criteria API / Querydsl
- [ ] **22.** Page vs Slice vs keyset (scroll) pagination
- [ ] **23.** Custom repository implementations
- [ ] **24.** Auditing (@CreatedDate, @LastModifiedBy, @EnableJpaAuditing)

**Transactions & concurrency**
- [ ] ⭐ **25.** How @Transactional works (proxies, self-invocation, checked vs unchecked rollback)
- [ ] ⭐ **26.** Propagation — REQUIRED vs REQUIRES_NEW
- [ ] **27.** Isolation levels and anomalies
- [ ] **28.** What readOnly = true really does
- [ ] ⭐ **29.** Optimistic (@Version) vs pessimistic locking
- [ ] **30.** ApplicationEvent, @EventListener, @TransactionalEventListener (run after commit)

**Production & testing**
- [ ] **31.** HikariCP connection pooling
- [ ] **32.** Flyway / Liquibase vs ddl-auto
- [ ] **33.** @DataJpaTest with Testcontainers instead of H2
- [ ] **34.** Soft delete (Hibernate @SoftDelete)

---

### 5. Spring AOP (ID: AOP)

- [ ] ⭐ **1.** What is AOP and what problems does it solve?
- [ ] **2.** Terminology — aspect, advice, join point, pointcut, target, weaving
- [ ] ⭐ **3.** Advice types and when to use each
- [ ] **4.** Why @Around is most powerful; forgetting proceed()
- [ ] **5.** JoinPoint vs ProceedingJoinPoint
- [ ] ⭐ **6.** How Spring AOP works internally — JDK proxy vs CGLIB
- [ ] ⭐ **7.** Self-invocation problem — why it bypasses @Transactional/@Async, and fixes
- [ ] **8.** Why final/private/static methods can't be advised
- [ ] ⭐ **9.** Spring features built on AOP (@Transactional, @Cacheable, @Async, @PreAuthorize, @Retryable)
- [ ] ⭐ **10.** Custom annotation aspect (e.g. @LogExecutionTime)
- [ ] **11.** Pointcut expressions — execution, within, @annotation, args
- [ ] **12.** Ordering multiple aspects (@Order)
- [ ] **13.** Spring AOP vs AspectJ

---

### 6. Spring Security (ID: Security)

**Avoid outdated answers:** WebSecurityConfigurerAdapter → SecurityFilterChain bean; antMatchers/authorizeRequests → requestMatchers/authorizeHttpRequests; @EnableGlobalMethodSecurity → @EnableMethodSecurity; default encoder = DelegatingPasswordEncoder (BCrypt).

**Architecture**
- [ ] ⭐ **1.** Filter chain — DelegatingFilterProxy → FilterChainProxy → SecurityFilterChain
- [ ] ⭐ **2.** Core components — SecurityContextHolder, Authentication, AuthenticationManager, ProviderManager, AuthenticationProvider, UserDetailsService
- [ ] ⭐ **3.** Username/password authentication flow
- [ ] **4.** How ProviderManager picks a provider (supports())
- [ ] **5.** SecurityContextHolder ThreadLocal; @Async and virtual threads
- [ ] ⭐ **6.** Configuring with SecurityFilterChain; multiple chains

**Authorization**
- [ ] **7.** Role vs Authority (ROLE_ prefix)
- [ ] **8.** authorizeHttpRequests + requestMatchers
- [ ] ⭐ **9.** Method security — @EnableMethodSecurity, @PreAuthorize vs @Secured
- [ ] **10.** 401 vs 403 — AuthenticationEntryPoint vs AccessDeniedHandler

**Passwords**
- [ ] **11.** PasswordEncoder / DelegatingPasswordEncoder, BCrypt/Argon2, salting

**Stateful vs stateless**
- [ ] ⭐ **12.** Session vs token auth; SessionCreationPolicy.STATELESS
- [ ] ⭐ **13.** JWT structure (header, payload, signature); signed vs encrypted
- [ ] ⭐ **14.** Implementing JWT — custom filter vs OAuth2 Resource Server
- [ ] **15.** Access vs refresh tokens, expiry, revocation

**OAuth2 / OIDC**
- [ ] **16.** OAuth2 roles
- [ ] ⭐ **17.** Grant types — Authorization Code + PKCE, Client Credentials; deprecated flows
- [ ] **18.** OAuth2 vs OIDC vs JWT
- [ ] **19.** oauth2Login vs oauth2ResourceServer vs Spring Authorization Server
- [ ] **20.** Service-to-service auth with Client Credentials

**Attacks & protection**
- [ ] ⭐ **21.** CSRF — when to disable, when not to
- [ ] **22.** CORS in Spring Security
- [ ] **23.** Brute-force prevention
- [ ] **24.** REST API security best practices

**Modern & testing**
- [ ] **25.** Passkeys/WebAuthn, one-time-token login
- [ ] **26.** Logout in stateless vs stateful apps
- [ ] **27.** Testing with @WithMockUser, jwt(), csrf()

---

### 7. Microservices — concepts & patterns (ID: MS)

**Fundamentals**
- [ ] **1.** Monolith vs microservices; when NOT to use microservices
- [ ] **2.** Decomposition — bounded contexts / DDD
- [ ] **3.** Database per service — why, and the problems it creates
- [ ] **4.** Sync (REST) vs async (messaging) communication

**Data consistency**
- [ ] ⭐ **5.** Why avoid distributed transactions / 2PC
- [ ] ⭐ **6.** Saga — choreography vs orchestration
- [ ] ⭐ **7.** Transactional Outbox
- [ ] ⭐ **8.** Eventual consistency, idempotent consumers
- [ ] **9.** CQRS and event sourcing basics

**Resilience & communication**
- [ ] ⭐ **10.** Circuit breaker, retry, timeout, bulkhead, rate limiter
- [ ] **11.** API Gateway and Backend-for-Frontend
- [ ] **12.** Service discovery — client-side vs server-side
- [ ] **13.** Kafka — topics, partitions, consumer groups, ordering

**Operations**
- [ ] **14.** Distributed tracing, centralized logging, correlation IDs
- [ ] **15.** Externalized config, 12-factor app
- [ ] **16.** Docker, Kubernetes basics, blue-green / canary
- [ ] **17.** Securing service-to-service calls (token relay, Client Credentials, mTLS)
- [ ] **18.** API versioning and backward compatibility

---

### 8. Spring Cloud (ID: Cloud)

- [ ] ⭐ **1.** Retired Netflix stack and replacements (Hystrix → Resilience4j, Ribbon → LoadBalancer, Zuul → Gateway, Sleuth → Micrometer Tracing)
- [ ] **2.** Release trains and Boot compatibility
- [ ] **3.** Config Server, @RefreshScope, Spring Cloud Bus
- [ ] **4.** Eureka — registration, heartbeats, self-preservation
- [ ] **5.** Spring Cloud LoadBalancer
- [ ] ⭐ **6.** Spring Cloud Gateway — routes, predicates, filters, rate limiting, auth
- [ ] ⭐ **7.** Resilience4j circuit breaker states and fallbacks
- [ ] **8.** OpenFeign vs HTTP interface clients
- [ ] **9.** Spring Cloud Stream (Kafka/RabbitMQ binders, functional consumers)
- [ ] **10.** Micrometer Tracing + Zipkin / OpenTelemetry
- [ ] **11.** On Kubernetes: Eureka/Config Server vs K8s Services/ConfigMaps/Secrets

---

### 9. Spring Testing (ID: Test)

**Fundamentals**
- [ ] **1.** Testing pyramid — unit vs integration vs E2E
- [ ] **2.** JUnit 5 — lifecycle, @ParameterizedTest, @Nested, assertions
- [ ] **3.** Mockito — @Mock vs @Spy vs @InjectMocks, verify, captors, void methods

**Spring Boot testing**
- [ ] ⭐ **4.** @SpringBootTest vs slices (@WebMvcTest, @DataJpaTest, @JsonTest, @RestClientTest)
- [ ] ⭐ **5.** @MockitoBean / @MockitoSpyBean vs plain Mockito; @MockBean deprecated
- [ ] **6.** TestContext caching; cost of @DirtiesContext
- [ ] **7.** MockMvc / MockMvcTester / WebTestClient
- [ ] **8.** RANDOM_PORT with TestRestTemplate or a real client
- [ ] **9.** Security tests — @WithMockUser, jwt()

**Integration testing**
- [ ] ⭐ **10.** Testcontainers vs H2; @ServiceConnection, @DynamicPropertySource
- [ ] **11.** WireMock / MockRestServiceServer
- [ ] **12.** Testing Kafka (embedded vs Testcontainers)
- [ ] **13.** @Transactional in tests — rollback and pitfalls

**Microservice testing**
- [ ] **14.** Contract testing — Spring Cloud Contract / Pact
- [ ] **15.** ArchUnit architecture tests

---
