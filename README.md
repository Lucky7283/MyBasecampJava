# MyBaseCamp

A Basecamp-style project management web app for small teams. Users sign up, create projects, share files, and discuss work in message threads.

Built with **Spring Boot**, **Thymeleaf**, and **SQLite**.

## Features

- **Accounts** — register, log in, log out, optional “remember me”
- **Projects** — create, edit, and delete projects (owner or site admin)
- **Attachments** — upload files to a project (max 10 MB) and download them
- **Threads** — discussion topics per project, with messages that authors (or the project owner) can edit or delete
- **Thread members** — add users to a thread
- **Admin panel** — list users, promote/demote admins, delete accounts
- **Roles** — regular user (`admin = 0`) vs site admin (`admin = 1`); site admins see every project on the dashboard

## Tech stack

| Layer | Choice |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 4.1 |
| UI | Thymeleaf + static CSS/JS |
| Persistence | Spring Data JPA + Hibernate SQLite dialect |
| Database | SQLite (`./my_database.db`, created automatically) |
| Passwords | BCrypt (`spring-security-crypto`) |
| Sessions | Token in an HTTP-only cookie, stored in `sessions` |

There is no Spring Security filter chain. Access is enforced with `AuthInterceptor` and `AdminInterceptor`.

## Requirements

- JDK 17+
- Maven 3.8+

## Run locally

```bash
mvn spring-boot:run
```

Open [http://localhost:3000](http://localhost:3000).

Alternatively:

```bash
mvn clean package
java -jar target/my-project-0.0.1-SNAPSHOT.jar
```

On first start, Hibernate creates/updates tables (`spring.jpa.hibernate.ddl-auto=update`). Uploaded files go under `src/main/resources/uploads/`.

### First admin

New accounts are regular users. To open `/admin`, set `admin = 1` on a user in SQLite, then log in again:

```sql
UPDATE users SET admin = 1 WHERE email = 'you@example.com';
```

After that, admins can promote others from the admin panel.

## Configuration

See `src/main/resources/application.properties`:

| Property | Default | Meaning |
| --- | --- | --- |
| `server.port` | `3000` | HTTP port |
| `spring.datasource.url` | `jdbc:sqlite:./my_database.db` | SQLite file |
| `spring.servlet.multipart.max-file-size` | `10MB` | Upload limit |
| `app.upload.dir` | `./uploads` | Documented upload dir (controller currently writes to `src/main/resources/uploads`) |

## How it works

```
Browser (Thymeleaf pages)
        │
        ▼
AppController          — HTML routes, forms, file upload/download
        │
AuthInterceptor        — cookie/Bearer token → request user
AdminInterceptor       — /admin and user-role changes
        │
Services               — users, sessions, projects, threads, messages, attachments
        │
JPA repositories       — SQLite
```

Public routes: `/`, `/login`, `/register`, static `/css` and `/js`. Login (`POST /sessions`) and registration (`POST /users`) are allowed without a session. Everything else requires a valid token.

Session cookies last 30 days if “remember me” is checked; otherwise they expire when the browser closes. Server-side sessions default to 24 hours.

## Main routes

| Method | Path | Description |
| --- | --- | --- |
| GET | `/` | Landing page |
| GET/POST | `/register`, `/users` | Sign up |
| GET | `/login` | Login form |
| POST | `/sessions` | Log in |
| DELETE | `/sessions` | Log out |
| GET | `/dashboard` | Home: your projects (all projects if admin) |
| GET/POST | `/projects/new`, `/projects` | Create project |
| GET | `/projects/{id}/show` | Project: files and threads |
| GET/PUT/DELETE | `/projects/{id}/edit`, `/projects/{id}` | Edit / delete (owner or site admin) |
| POST/DELETE | `/projects/{id}/attachments` | Upload / remove file |
| GET | `/uploads/{filename}` | Download file |
| GET/POST | `/projects/{id}/threads/...` | Thread CRUD and messages |
| POST | `/add/{threadId}` | Add a member to a thread |
| GET | `/admin` | User management (site admin) |

Forms that update or delete use a hidden `_method` field (`PUT` / `DELETE`) because `spring.mvc.hiddenmethod.filter.enabled` is on.

## Project layout

```
src/main/java/com/mybasecamp/
  MyBasecampApplication.java
  controller/AppController.java
  config/WebMvcConfig.java
  interceptor/          AuthInterceptor, AdminInterceptor
  model/entity/         User, Session, Project, Thread, Message, Attachment, ThreadsMember
  model/repository/
  model/service/
  ExceptionHandler/
  util/TokenGenerator.java
src/main/resources/
  application.properties
  templates/            index, login, register, dashboard, admin, projects/, threads/
  static/css/  static/js/
```

## Data model

- **users** — name, unique email, hashed password, admin flag
- **sessions** — token, expiry
- **projects** — name, description, owner
- **attachments** — stored filename (UUID), original name, MIME type, size
- **threads** — title, project, creator
- **messages** — content, author, thread
- **thread_members** — users added to a thread

## Notes

- Passwords must be at least 6 characters and must match confirmation on sign-up.
- Project owners manage threads and attachments on that project; site admins can edit/delete any project from the dashboard.
- SQLite and the `uploads` folder are local; do not commit `my_database.db` or uploaded files if you share the repo.
