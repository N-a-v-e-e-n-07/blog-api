# Inkflow Blog REST API

A production-ready blog backend built with **Spring Boot 3.2**, **Spring Security + JWT**, **JPA/Hibernate**, and **PostgreSQL**.

---

## Prerequisites

Install these before starting:

| Tool | Download |
|---|---|
| Java 17+ | https://adoptium.net |
| Maven 3.8+ | https://maven.apache.org/download.cgi (or use IntelliJ built-in) |
| PostgreSQL 15+ | https://www.postgresql.org/download |
| IntelliJ IDEA | https://www.jetbrains.com/idea/download |

---

## Step 1 — Install and Set Up PostgreSQL

### Windows
1. Download the installer from https://www.postgresql.org/download/windows
2. Run the installer — use these settings:
   - Port: **5432** (default)
   - Password for `postgres` user: **postgres** (or remember what you set)
   - Check **pgAdmin 4** during install (optional GUI tool)
3. After install, open **pgAdmin 4** or **SQL Shell (psql)**

### Create the database
Open **SQL Shell (psql)** or **pgAdmin Query Tool** and run:

```sql
CREATE DATABASE blogdb;
```

That is all — Hibernate auto-creates all tables on first startup.

### Verify PostgreSQL is running
Open Task Manager → Services → look for **postgresql-x64-15** (or similar) → it should say **Running**.

---

## Step 2 — Configure the Project

Open `src/main/resources/application.yml`.

By default it uses:
- Host: `localhost:5432`
- Database: `blogdb`
- Username: `postgres`
- Password: `postgres`

If your PostgreSQL password is different, change it here:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blogdb
    username: postgres
    password: YOUR_PASSWORD_HERE   # change this
```

Or set environment variables in IntelliJ:
- Go to **Run → Edit Configurations → Environment Variables**
- Add: `DB_USERNAME=postgres` and `DB_PASSWORD=yourpassword`

---

## Step 3 — Run the Project

### Option A — IntelliJ (recommended)
1. Open IntelliJ → **File → Open** → select the `blog-api` folder
2. Wait for Maven to download dependencies (bottom progress bar)
3. Open `BlogApiApplication.java`
4. Click the green **Run** button
5. Watch the console — you should see **Started BlogApiApplication**

### Option B — Terminal (if Maven is installed)
```bash
cd blog-api
mvn spring-boot:run
```

---

## Step 4 — Verify It Worked

Open your browser and go to:

```
http://localhost:8080
```

It will automatically redirect to **Swagger UI** at:

```
http://localhost:8080/swagger-ui/index.html
```

You should see a page with all your API endpoints listed.

---

## Step 5 — Test Using Swagger UI

### 5.1 Register a user
1. Click **Authentication** section → **POST /api/v1/auth/register**
2. Click **Try it out**
3. Paste this in the request body:
```json
{
  "username": "janak",
  "email": "janak@test.com",
  "password": "password123",
  "fullName": "Janak"
}
```
4. Click **Execute** → you should get **201 Created**

### 5.2 Login and get your token
1. Click **POST /api/v1/auth/login** → **Try it out**
2. Paste:
```json
{
  "usernameOrEmail": "janak",
  "password": "password123"
}
```
3. Click **Execute** → copy the `accessToken` value from the response

### 5.3 Authorize Swagger
1. Click the **Authorize** button at the top of the Swagger page (lock icon)
2. In the `bearerAuth` field type: `Bearer ` followed by your token
```
Bearer eyJhbGciOiJIUzI1NiJ9...
```
3. Click **Authorize** → **Close**

### 5.4 Create a post
1. Click **POST /api/v1/posts** → **Try it out**
2. Paste:
```json
{
  "title": "My First Post",
  "summary": "Hello world",
  "content": "This is my first blog post on Inkflow!",
  "status": "PUBLISHED",
  "tagNames": ["java", "spring-boot"]
}
```
3. Click **Execute** → **201 Created** — copy the `id` from the response

### 5.5 Read, Update, Delete
- **Read**: GET /api/v1/posts/{id} → enter the id
- **Update**: PUT /api/v1/posts/{id} → enter id + updated body
- **Delete**: DELETE /api/v1/posts/{id} → enter the id

### 5.6 Add a comment
1. **POST /api/v1/posts/{postId}/comments** → **Try it out**
2. Enter the post id, paste:
```json
{
  "content": "Great post!",
  "parentId": null
}
```

---

## Step 6 — Test Using the Frontend HTML

1. Download `blog-frontend.html` (provided separately)
2. Double-click to open it in your browser
3. The green dot at the top confirms the API is connected
4. Click **Register** → fill in your details → **Create Account**
5. Click **Login** → enter username and password
6. Click **+ Write Post** → fill in the form → **Publish Post**
7. Click any post card to read it, like it, or comment

---

## API Endpoints Quick Reference

### Auth (public)
| Method | URL | Description |
|---|---|---|
| POST | /api/v1/auth/register | Register new user |
| POST | /api/v1/auth/login | Login, get JWT tokens |
| POST | /api/v1/auth/refresh | Refresh access token |

### Posts
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/v1/posts | Public | List all published posts |
| GET | /api/v1/posts?keyword=java | Public | Search posts |
| GET | /api/v1/posts?categoryId=1 | Public | Filter by category |
| GET | /api/v1/posts/{id} | Public | Get post by ID |
| GET | /api/v1/posts/slug/{slug} | Public | Get post by slug |
| POST | /api/v1/posts | Required | Create post |
| PUT | /api/v1/posts/{id} | Owner/Admin | Update post |
| DELETE | /api/v1/posts/{id} | Owner/Admin | Delete post |
| POST | /api/v1/posts/{id}/like | Public | Like a post |

### Comments
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/v1/posts/{id}/comments | Public | Get comments |
| POST | /api/v1/posts/{id}/comments | Required | Add comment/reply |
| PUT | /api/v1/comments/{id} | Owner/Admin | Edit comment |
| DELETE | /api/v1/comments/{id} | Owner/Admin | Delete comment |

### Categories
| Method | URL | Auth | Description |
|---|---|---|---|
| GET | /api/v1/categories | Public | List all categories |
| POST | /api/v1/categories | Any user | Create category |
| PUT | /api/v1/categories/{id} | Admin only | Update category |
| DELETE | /api/v1/categories/{id} | Admin only | Delete category |

---

## Useful URLs

| URL | What it is |
|---|---|
| http://localhost:8080 | Auto-redirects to Swagger UI |
| http://localhost:8080/swagger-ui/index.html | Interactive API docs |
| http://localhost:8080/api-docs | Raw OpenAPI JSON |

---

## Common Errors and Fixes

| Error | Fix |
|---|---|
| `Connection refused` on startup | PostgreSQL is not running — start it from Services |
| `password authentication failed` | Wrong DB password in application.yml |
| `database "blogdb" does not exist` | Run `CREATE DATABASE blogdb;` in psql |
| `User is disabled` | Restart the app — old H2 data issue, fixed in this version |
| `403 on localhost:8080` | Go to `/swagger-ui/index.html` instead, or wait for redirect |
| `401 Unauthorized` in Swagger | Click Authorize button and paste `Bearer <token>` |

---

## Tech Stack

```
Java 17 · Spring Boot 3.2 · Spring Security · JWT (jjwt 0.12)
JPA/Hibernate · PostgreSQL 15 · Swagger/OpenAPI · Lombok · Maven
```
