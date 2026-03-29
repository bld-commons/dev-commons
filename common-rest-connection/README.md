# common-rest-connection

A Spring Boot library that provides a unified, configuration-free client for executing
**REST** and **SOAP 1.1** calls through a single injectable interface (`RestClientConnection`).
It wraps Spring's `RestTemplate` with per-request timeout support, optional HTTP proxy,
transparent JSON/XML/YAML deserialization, and a fluent request-model hierarchy.

---

## Table of contents

1. [Prerequisites](#prerequisites)
2. [Installation](#installation)
3. [Enabling the module](#enabling-the-module)
4. [Request model hierarchy](#request-model-hierarchy)
5. [REST examples](#rest-examples)
   - [GET with query parameters](#get-with-query-parameters)
   - [POST with a typed object body](#post-with-a-typed-object-body)
   - [POST with a map body](#post-with-a-map-body)
   - [Returning a list](#returning-a-list)
6. [SOAP example](#soap-example)
7. [Proxy configuration](#proxy-configuration)
8. [Timeout configuration](#timeout-configuration)
9. [Authentication helpers](#authentication-helpers)

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 3.x |
| Jakarta EE | 10+ (Jakarta namespace) |

---

## Installation

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>common-rest-connection</artifactId>
    <version>2.1.4</version>
</dependency>
```

---

## Enabling the module

Place `@EnableRestConnection` on any `@Configuration` class in your application.
This imports `EnableRestConnectionConfiguration`, which activates component scanning
for the `com.bld.commons.connection` package and registers all library beans.

```java
@Configuration
@EnableRestConnection
public class AppConfig {
}
```

Once enabled, inject `RestClientConnection` wherever you need it:

```java
@Autowired
private RestClientConnection restClient;
```

---

## Request model hierarchy

```
BasicRequest<T>                 (url, method, headers, timeout, body)
  └── RestBasicRequest<T>       (+ URI template params, Content-Type, Accept)
        ├── MapRequest           body = Map<String,Object>
        └── ObjectRequest<T>     body = typed object T
  └── SoapRequest<B,H>          (+ operationName, namespace, SOAPAction, header)
        └── MapSoapRequest<H>    body = Map<String,Object>

SoapHeader<H>                   soap:Header block
  └── MapSoapHeader              header body = Map<String,Object>
```

| Class | Use when |
|---|---|
| `MapRequest` | Query params or form-encoded body expressed as key-value pairs |
| `ObjectRequest<T>` | JSON / XML body serialised from a typed Java object |
| `SoapRequest<B,H>` | SOAP 1.1 calls with a JAXB-annotated body |
| `MapSoapRequest<H>` | SOAP 1.1 calls with a Map-based body |

---

## REST examples

### GET with query parameters

Parameters added via `addData` are automatically appended as URL query string entries
for GET and DELETE requests.

```java
MapRequest request = MapRequest.newInstanceGet("https://api.example.com/users/{id}");
request.addUriParams(42);                    // replaces {id}
request.addData("includeRoles", true);       // appended as ?includeRoles=true
request.setTimeout(5000);                    // 5 s timeout

UserDto user = restClient.entityRestTemplate(request, UserDto.class);
```

### POST with a typed object body

Use `ObjectRequest<T>` when the body is a serialisable Java object (JSON by default).

```java
CreateUserRequest body = new CreateUserRequest("Alice", "alice@example.com");

ObjectRequest<CreateUserRequest> request =
        ObjectRequest.newInstancePost("https://api.example.com/users");
request.setData(body);
request.setContentType(MediaType.APPLICATION_JSON);
request.setAccept(MediaType.APPLICATION_JSON);
request.setBearerAuth("eyJhbGci...");

UserDto created = restClient.entityRestTemplate(request, UserDto.class);
```

### POST with a map body

`MapRequest` converts the map to a `MultiValueMap` for POST / PUT / PATCH.

```java
MapRequest request = MapRequest.newInstancePost("https://api.example.com/login");
request.addData("username", "alice");
request.addData("password", "secret");

TokenDto token = restClient.entityRestTemplate(request, TokenDto.class);
```

### Returning a list

Both `MapRequest` and `ObjectRequest` support list results; pass the **array** class
as the second argument.

```java
MapRequest request = MapRequest.newInstanceGet("https://api.example.com/products");
request.addData("category", "books");

List<ProductDto> products = restClient.listRestTemplate(request, ProductDto[].class);
```

---

## SOAP example

Use `MapSoapRequest` when the operation body can be expressed as key-value pairs,
or `SoapRequest` with a JAXB object for strongly typed payloads.

### Map-based SOAP call (W3Schools temperature converter)

```java
MapSoapRequest<Void> request = new MapSoapRequest<>(
        "https://www.w3schools.com/xml/tempconvert.asmx",
        "CelsiusToFahrenheit",
        "https://www.w3schools.com/xml/");

request.addData("Celsius", "100");
request.setTimeout(10000);

String response = restClient.soapRestTemplate(request, String.class);
```

### SOAP call with a JAXB body and custom header

```java
// Body
MyOperation body = new MyOperation();
body.setInput("value");

SoapRequest<MyOperation, Void> request =
        SoapRequest.newInstance(
                "https://service.example.com/ws",
                "MyOperation",
                "urn:example:service");
request.setData(body);
request.setSoapAction("urn:example:service#MyOperation");
request.setTimeout(8000);

MyOperationResponse response =
        restClient.soapRestTemplate(request, MyOperationResponse.class);
```

### SOAP call with a soap:Header

```java
MapSoapHeader soapHeader = new MapSoapHeader("AuthHeader", "urn:example:auth");
soapHeader.addData("token", "abc123");

MapSoapRequest<Map<String, Object>> request = new MapSoapRequest<>(
        "https://service.example.com/ws",
        "GetData",
        "urn:example:service");
request.addData("id", 99);
request.setHeader(soapHeader);

GetDataResponse response = restClient.soapRestTemplate(request, GetDataResponse.class);
```

> **SOAP response as `JsonNode`**: pass `JsonNode.class` as `responseClass` to obtain
> the full SOAP envelope as a tree of Jackson nodes without writing a JAXB class.

---

## Proxy configuration

Set the following properties in `application.properties` (or `application.yml`):

```properties
com.bld.connection.proxy.ip=proxy.corporate.example.com
com.bld.connection.proxy.port=8080
```

When both properties are present the library creates an HTTP proxy and applies it to
every outbound call. If either property is absent no proxy is used.

---

## Timeout configuration

Timeout is configured per request, not globally:

```java
request.setTimeout(3000); // connect + read timeout in milliseconds
```

When not set (or set to `null`), `RestTemplate` uses its default (no timeout).

---

## Authentication helpers

`BasicRequest` (and therefore all request types) exposes two convenience methods:

```java
// Bearer token
request.setBearerAuth("eyJhbGci...");

// HTTP Basic
request.setBasicAuth("user", "pass");
```

Custom headers can be added or removed at any time:

```java
request.addHeader("X-Correlation-Id", "abc-123");
request.removeHeader("X-Correlation-Id");
```

---

## 🇮🇹 [Leggi in italiano](README.it.md)
