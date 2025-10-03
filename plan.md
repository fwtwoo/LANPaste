
---

1. Project setup and scaffolding — pick a build tool and repo

* What: Create a Git repo and a Java project skeleton using **Maven** or **Gradle**. Add a simple `main()` entry for the server component and a separate `main()` for the CLI client (two modules or two subprojects is a good pattern).
* Why: A build tool handles dependencies, packaging, and running tests. Separate modules keep server and client code isolated.
* Docs / where to read: Maven or Gradle official guides (search the official docs for quickstart). Also make a short README describing the project goals.

---

2. Hello-world HTTP server (embedded, minimal)

* What: Implement a minimal HTTP server that listens on a port and serves a single static response (e.g., “OK”). Use the JDK’s built-in HTTP server for this first iteration.
* Why: This teaches the low-level HTTP handling lifecycle (listen, accept, read request, write response) with minimal dependencies. It proves you can reach the server from other devices on your LAN.
* Docs / where to read: JDK com.sun.net.httpserver package/class documentation for HttpServer and HttpsServer.

---

3. POST /paste and GET /p/{id} — in-memory MVP

* What: Add two endpoints:

  * `POST /paste` — accepts raw text (or JSON), stores it in a `ConcurrentHashMap<String, Paste>` and returns a short ID (e.g., `abc123`).
  * `GET /p/{id}` — returns the paste text and basic metadata.
* Why: This is the minimum usable product: create + retrieve. Keeping it in memory makes iteration fast.
* Implementation notes:

  * Generate IDs with a secure random base62 or UUID (you can base62-encode a UUID for shorter IDs).
  * Use thread-safe collections.
  * Add size limits for pasted content to avoid memory exhaustion.
* Why test from phone: find your machine’s LAN IP (e.g., `ip addr`) and browse `http://<your-ip>:8080/p/<id>` from another device to verify connectivity.

---

4. Build the CLI client (local command)

* What: A small Java CLI program that reads stdin or arguments and POSTs them to `http://<server>:<port>/paste`. The client prints the returned URL.
* Why: Keeps the developer experience fast — you want a `lanpaste` command on your PC that you can run from terminal pipelines.
* How to implement: Use Java’s `HttpClient` (the newer built-in API) to send requests from the CLI.
* Packaging for usage: For quick testing run via `java -jar`, then later wrap with a small shell script or use `jpackage` to create a native installer. (More on packaging below.)

---

5. Persistence: file storage → simple DB

* What: Replace in-memory storage with on-disk persistence:

  1. Simple first step: write each paste to a file under `data/pastes/<id>.txt` and keep an index (JSON or one file per paste containing metadata).
  2. Later: migrate to SQLite when you want more robust querying and metadata (expiry, search), using a JDBC driver.
* Why: Files survive restarts and are trivial. SQLite is a practical next step for learning JDBC and ACID persistence without installing a server.
* Docs / where to read: Xerial’s SQLite JDBC driver repository and usage docs.

---

6. Helpful features and engineering decisions (iterate in this order)

* Add paste metadata: author (optional), timestamp, optional expiry time.
* Implement expiry/cleanup: either a scheduled cleanup job inside the server or a systemd timer / cron job to delete expired files.
* Add paste size limit and per-IP rate limiting (simple token bucket or fixed window) to avoid abuse.
* Add a small HTML view for `/p/{id}` that displays the paste (static files served by the server); later enhance with JS if you want a richer UI.
* Consider authentication (a single password, or IP allowlist) if you care about privacy on a shared LAN.

---

7. Alternatives and when to switch to a framework

* What: After you understand the manual flow, you may reimplement the server with a framework (Spring Boot) or use an embedded server like Jetty for more features (Servlet API, WebSocket, HTTP/2).
* Why: Frameworks simplify routing, dependency injection, and production features, but they hide low-level learning. Rewriting in Spring Boot is an excellent follow-up project to learn modern Java server tooling.

---

8. Packaging & installing the CLI + server on Linux

* What:

  * For the CLI: produce an executable JAR (add a tiny wrapper script `#!/bin/sh exec java -jar /opt/lanpaste/cli.jar "$@"`), and install that script/symlink into `/usr/local/bin/lanpaste`.
  * For the server: either run `java -jar server.jar` manually or create a systemd unit so it runs at boot and restarts automatically.
  * For a polished native package, use `jpackage` to create platform installers or self-contained images.
* Why: Make the tool convenient and reliably available as a command and a service.
* Where to read: Oracle jpackage docs for packaging; tutorials on systemd service files for Java apps (examples and best practices).

---

9. Deployment & networking considerations

* LAN visibility:

  * Bind only to the interface you want (specific LAN IP) if you want to avoid exposing it to other networks; alternatively bind to `0.0.0.0` to allow any interface.
  * If you need HTTPS on LAN, `HttpsServer` exists in the JDK; for simpler initial use you can stay HTTP but know traffic is unencrypted. The JDK API supports HTTPS via HttpsServer.
* Firewall: open the port only on the host machine if necessary.
* Security: limit paste size, consider a password or token for paste creation, and log suspicious behavior.

---

10. Testing, logging, and CI

* What: Add logging (use `java.util.logging` or a simple SLF4J+Logback setup), unit tests for ID generation and persistence, and a couple of integration tests (server starts, POST returns ID, GET returns the text).
* Why: Makes the project maintainable and teaches testing and logging practices.
* Where to read: JUnit docs, SLF4J/Logback docs.

---

11. Iteration plan / milestone checklist (concrete)

* Milestone A: repo + Hello server + reachable from phone.
* Milestone B: POST/GET in memory + CLI that POSTs and returns URL.
* Milestone C: Disk persistence (files) + server restarts keep pastes.
* Milestone D: Expiry & cleanup + size limits + rate limiting.
* Milestone E: Optional: SQLite backend + simple search.
* Milestone F: Packaging (wrapper script or jpackage) + systemd unit to run server at boot.
* Milestone G: Optional rewrite in Spring Boot or with Jetty to learn frameworks.

---

12. Resources and specific docs to keep handy

* JDK embedded HTTP server (com.sun.net.httpserver) — API and HttpsServer.
* Java HTTP client (java.net.http.HttpClient) — use this from your CLI.
* Spring Boot — if/when you want a higher-level framework and auto-configuration.
* Jetty (embedded server) — alternative if you want a more featureful embedded server.
* SQLite JDBC (Xerial) — simple embedded DB option for persistence.
* jpackage / packaging tool (Oracle) — create native installers or self-contained apps.
* systemd / running Java apps as services — examples for service files and timers.

---

13. Practical implementation tips

* Start tiny and keep iterations fast: get POST + GET working in a single sitting before thinking about persistence or packaging.
* Use clear interfaces for persistence so switching a file store → SQLite is just a new implementation of the same interface.
* Keep the CLI minimal: read stdin, send request, print URL — everything else is optional UI polish.
* Log enough to debug (request arrival, generated ID, error stack). Make logs rotate or send to files in `/var/log/lanpaste` if you run as a service.

---

