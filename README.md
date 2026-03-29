# request-capture-tool

A lightweight Spring Boot tool for capturing and inspecting incoming HTTP requests (backUrl & webhook).

---

## What it does

* captures backUrl requests (GET / POST)
* captures webhook requests (GET / POST)
* captures API responses (via Postman script)
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

* Response capture (used internally by Postman script):

```
/capture/response/{group}
```

Example:

```
http://localhost:8080/capture/webhook/test-group
```

---

## Postman Response Capture (NEW)

You can automatically capture API responses from Postman.

### Where to add script

Postman → **Request → Scripts → Post-response**  
(older UI: **Tests tab**)

---

### Script (copy & paste)

```javascript
const baseUrl = "http://localhost:8080";

// extract group from request URL (last segment)
const urlParts = pm.request.url.toString().split("/");
const group = urlParts[urlParts.length - 1];

// prevent infinite loop
if (pm.request.url.toString().includes("/capture/response/")) return;

const headers = {};
pm.response.headers.each(h => headers[h.key] = h.value);

pm.sendRequest({
    url: baseUrl + "/capture/response/" + group,
    method: "POST",
    header: { "Content-Type": "application/json" },
    body: {
        mode: "raw",
        raw: JSON.stringify({
            originalMethod: pm.request.method,
            originalPath: pm.request.url.toString(),
            statusCode: pm.response.code,
            contentType: pm.response.headers.get("Content-Type"),
            headers: headers,
            responseBody: pm.response.text()
        })
    }
});
```

---

### How it works

* Automatically sends every Postman response to your app
* Group is extracted from your request URL
* No environment variables required
* Responses can be compared in UI just like requests

---

## Demo

https://request-capture-tool.onrender.com/ui/requests

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