# request-capture-tool

A lightweight Spring Boot tool for capturing and inspecting incoming HTTP requests (backUrl & webhook).

---

## What it does

* captures backUrl requests (GET / POST)
* captures webhook requests (GET / POST)
* stores:

    * headers
    * query params
    * raw body
    * extracted body fields
* supports:

    * JSON
    * application/x-www-form-urlencoded
    * multipart/form-data
* provides UI to inspect captured requests
* allows comparing 2 requests by **structure (not values)**

---

## Run the Project

1. Download the repository and unzip it.

2. (Optional) Open:

```
src/main/resources/application.yaml
```

and adjust any configuration if needed.

3. Open the project root folder in terminal / command prompt.

4. Build the project (for testing):

```bash
mvn clean package
```

5. Build Docker image:

```bash
docker build -t request-capture-tool .
```

6. Run container:

```bash
docker run -d --name request-capture-tool -p 8080:8080 request-capture-tool
```

---

## Access

* UI: http://localhost:8080/ui/requests
* H2 Console: http://localhost:8080/h2-console

---

## Example endpoints

* BackUrl:

```
/capture/backurl/{group}
```

* Webhook:

```
/capture/webhook/{group}
```

Example:

```
http://localhost:8080/capture/webhook/test-group
```

---

## Tech stack

* Java 21
* Spring Boot 4
* Spring Web
* Spring Data JPA
* Thymeleaf
* H2 Database (file-based)
* Lombok
* Maven

---

