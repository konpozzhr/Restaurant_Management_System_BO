# Server Monitor → Project Management (backend merge)

Everything below is relative to the Project Management project root.

## 1. Files to REPLACE

| File | What changed |
|---|---|
| `pom.xml` | Added `spring-boot-starter-mail`, `spring-boot-starter-thymeleaf`, `sshj 0.39.0`, `openpdf 2.0.3`. **Did not** carry over `spring-boot-h2console` — H2 is gone. |
| `src/main/resources/application.properties` | Added the `serverwatch.*` block (PDF output dir, static email fields, optional server inventory). No H2 datasource, no `spring.h2.console.*`. |
| `src/main/java/com/sathya/project_management/ProjectManagementApplication.java` | `scanBasePackages` / `@EnableJpaRepositories` / `@EntityScan` widened to also cover `com.sathya.server_monitor`, plus `@EnableScheduling` and `@EnableConfigurationProperties(ServerWatchProperties.class)`. |
| `src/main/java/com/sathya/project_management/security/SecurityConfig.java` | SPA route allow-list replaced with one pattern so monitoring pages survive a hard refresh; `/h2-console/**` removed; monitor APIs fall through to `authenticated()`. |

## 2. Files to ADD

Copy `server_monitor_package/com/sathya/server_monitor/` into
`src/main/java/com/sathya/`, and
`src/main/resources/templates/email-report.html` into your resources.

That package is the ServerWatch backend unchanged, except:

- `model/ServerSnapshot.java` — the four `@Lob String` fields are now
  `@Column(columnDefinition = "text")`. On Postgres, `@Lob String` becomes an
  `oid` large object, which fails at runtime with *"Large Objects may not be
  used in auto-commit mode"* and stores the raw `df`/`top`/`dgmgrl` dumps as an
  OID reference instead of readable text.
- `web/ReportController.java` → **`web/ServerMonitorReportController.java`**.
  Project Management already has a `ReportController`; Spring names beans from
  the simple class name, so both would be `reportController` and startup fails
  with `ConflictingBeanDefinitionException`. URLs are unchanged.
- `web/GlobalExceptionHandler.java` — **deleted**. Same bean-name clash with
  `com.sathya.project_management.exception.GlobalExceptionHandler`, which now
  handles monitor exceptions too. One behaviour change: a missing server used
  to return 404, now returns 400 (`IllegalArgumentException` → Bad Request).
  If you want the 404 back, add a handler for `NoSuchElementException` to the
  PM one.
- `ServerMonitorApplication.java` — **deleted**. Two `@SpringBootApplication`
  classes in one scanned tree will not start.

## 3. Database

Nothing to do manually. `spring.jpa.hibernate.ddl-auto=update` creates the five
monitor tables in the same `project_management` Postgres database on first
boot: `server_config`, `server_snapshot`, `filesystem_entry`, `process_entry`,
`app_settings`.

The old `./database/Server_Monitoring_DB.mv.db` file is not read at all. If you
need the historical snapshots, export them from H2 and `INSERT` them into
Postgres separately — the column names are identical.

## 4. Unchanged API paths (frontend needs no edits)

```
GET    /api/servers                       POST   /api/servers
GET    /api/servers/{id}                  PUT    /api/servers/{id}
GET    /api/servers/{id}/history          DELETE /api/servers/{id}
GET    /api/servers/{id}/latest           POST   /api/servers/test-connection
POST   /api/servers/refresh               POST   /api/servers/{id}/refresh
POST   /api/servers/refresh/async         GET    /api/servers/refresh/progress
GET    /api/settings/scheduling           PUT    /api/settings/scheduling
GET    /api/settings/mail                 PUT    /api/settings/mail
POST   /api/settings/mail/test
GET    /api/reports/history               POST   /api/reports/send-now
GET    /api/reports/export/pdf
```

## 5. One thing to check on the frontend

These endpoints are now behind Project Management's JWT filter. The monitoring
screens must go through your authenticated API client (the one that sets
`Authorization: Bearer …`), not a bare `fetch()`. The original ServerWatch
`api.js` used plain `fetch` with no header — if the merged frontend still calls
it that way, every monitoring request comes back 401.
