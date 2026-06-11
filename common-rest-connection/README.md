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
7. [XML to JsonNode conversion](#xml-to-jsonnode-conversion)
8. [Proxy configuration](#proxy-configuration)
9. [Timeout configuration](#timeout-configuration)
10. [Authentication helpers](#authentication-helpers)

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
    <version>2.2.6</version>
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

### Accessing status code and headers (`ResponseEntity`)

Every body-returning method has a `ResponseEntity` counterpart that exposes the HTTP
status code and response headers in addition to the body. The body-only methods simply
delegate to these.

| Body-only method | `ResponseEntity` counterpart |
|---|---|
| `entityRestTemplate(MapRequest, Class<T>)` | `responseEntity(MapRequest, Class<T>)` |
| `entityRestTemplate(ObjectRequest, Class<T>)` | `entityResponseRestTemplate(ObjectRequest, Class<T>)` |
| `listRestTemplate(MapRequest, Class<T[]>)` | `responseListEntity(MapRequest, Class<T[]>)` |
| `listRestTemplate(ObjectRequest, Class<T[]>)` | `listResponseRestTemplate(ObjectRequest, Class<T[]>)` |

```java
MapRequest request = MapRequest.newInstanceGet("https://api.example.com/users/{id}");
request.addUriParams(42);

ResponseEntity<UserDto> response = restClient.responseEntity(request, UserDto.class);

HttpStatusCode status = response.getStatusCode();   // e.g. 200 OK
HttpHeaders headers   = response.getHeaders();       // full response headers
UserDto user          = response.getBody();
```

> **Content-Type sniffing (`JsonNode` responses):** when the body is parsed as a
> `JsonNode`, the actual format is detected (XML if the payload starts with `<`, JSON
> otherwise) and the returned `Content-Type` is corrected to `application/xml` or
> `application/json` when the server sent a missing or incoherent value. A coherent
> header (including any charset, and vendor suffixes such as `application/hal+json`) is
> preserved as-is.

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

### Accessing status code and headers (`ResponseEntity`)

As for REST, SOAP calls expose `ResponseEntity` counterparts that preserve the HTTP
status code and headers of the underlying response while the body is unmarshalled as
usual.

| Body-only method | `ResponseEntity` counterpart |
|---|---|
| `entitySoapTemplate(SoapRequest, Class<T>)` | `responseEntity(SoapRequest, Class<T>)` |
| `listSoapTemplate(SoapRequest, Class<T[]>)` | `responseListEntity(SoapRequest, Class<T[]>)` |

```java
ResponseEntity<GetDataResponse> response =
        restClient.responseEntity(request, GetDataResponse.class);

HttpStatusCode status = response.getStatusCode();
GetDataResponse body  = response.getBody();
```

---

## XML to JsonNode conversion

When `responseClass` is `JsonNode.class` and the response body is XML (the raw payload
starts with `<`), the body is converted into a Jackson tree by
`com.bld.commons.connection.utils.XmlNodeConverter`. The same converter is also used
internally by `SoapXmlBuilder` to map SOAP envelopes without re-parsing the document.

Two flavours of conversion are exposed:

| Flavour | Method | Activation |
|---|---|---|
| **Legacy** | `XmlNodeConverter.fromXml(String)` / `fromElement(Element)` | Default. Used when `RestBasicRequest#isXmlNormalized()` is `false`. SOAP responses always use this flavour. |
| **Normalized** | `XmlNodeConverter.fromXmlNormalized(String)` / `fromElementNormalized(Element)` | Opt-in. Activated by calling `request.setXmlNormalized(true)` on any `RestBasicRequest` subclass before executing the call. |

```java
ObjectRequest<Void> request = ObjectRequest.newInstanceGet("https://api.example.com/data.xml");
request.setXmlNormalized(true);                // opt into the normalized layout
JsonNode tree = restClient.entityRestTemplate(request, JsonNode.class);
```

### Rules shared by both flavours

- The qualified tag name (`prefix:localName`, or just `localName` if no prefix) is used as the key.
- A single child element with a given qualified name becomes an `ObjectNode`.
- Multiple sibling elements with the same qualified name become an `ArrayNode`.
- Namespace declarations (`xmlns`, `xmlns:*`) are dropped.
- DOCTYPE declarations are stripped from the input before parsing (legacy path) and the parser is hardened against XXE (`disallow-doctype-decl`, no external entities, no entity expansion).

### Attribute mapping — how it has evolved

The way XML attributes (and text content) are projected into the Jackson tree has changed
over time. Both shapes are kept side by side: legacy preserves backward compatibility for
SOAP consumers and existing callers, normalized is the new shape designed for path-based
mapping / harvesting (where a stable, predictable layout matters more than terseness).

| Version | Shape | Attributes | Text on a leaf | Repeated text-only leaves |
|---|---|---|---|---|
| Initial (commit `soap`, 2026-03-30) | Single shape — now called **legacy** | Added as direct string fields on the element's `ObjectNode`, **no prefix**, sharing the namespace with child element keys. `xmlns:*` skipped. | If the leaf has no attributes and no child elements, the parent stores the bare string under the tag name. Otherwise wrapped as `"value": "text"`. | Collapsed to an `ArrayNode` of bare strings. |
| `swagger` (2026-04-13) | Same as initial | (unchanged — only SLF4J debug logging was added) | (unchanged) | (unchanged) |
| `rest` (2026-05-22) | Adds **normalized** flavour alongside legacy | `@` prefix on every attribute name (e.g. `@scheme`, `@xsi:type`). `xmlns:*` still skipped. | Always wrapped under the key `value`, also for plain leaves. | Still `ArrayNode`, but elements are always `ObjectNode`s exposing `value` (and any `@attr`) — never bare strings. |

### Side-by-side example

Source XML:

```xml
<order id="42" currency="EUR">
  <customer>Alice</customer>
  <items>
    <item sku="A1">Book</item>
    <item sku="B2">Pen</item>
  </items>
  <tag>red</tag>
  <tag>urgent</tag>
</order>
```

Legacy output (`fromXml`):

```json
{
  "order": {
    "id": "42",
    "currency": "EUR",
    "customer": "Alice",
    "items": {
      "item": [
        { "sku": "A1", "value": "Book" },
        { "sku": "B2", "value": "Pen" }
      ]
    },
    "tag": ["red", "urgent"]
  }
}
```

Normalized output (`fromXmlNormalized`):

```json
{
  "order": {
    "@id": "42",
    "@currency": "EUR",
    "customer": { "value": "Alice" },
    "items": {
      "item": [
        { "@sku": "A1", "value": "Book" },
        { "@sku": "B2", "value": "Pen" }
      ]
    },
    "tag": [
      { "value": "red" },
      { "value": "urgent" }
    ]
  }
}
```

Key takeaways:

- In **legacy**, attribute names and child tag names live in the same namespace. A child
  element called `id` would collide with an `id="..."` attribute on the same parent. In
  **normalized**, the `@` prefix removes that ambiguity.
- In **legacy**, a path like `order.tag[0]` may resolve to either a string or an object
  depending on whether siblings carry attributes; in **normalized**, the same path
  always resolves to an `ObjectNode`, which is what makes the shape safe for generic
  path-based mapping.

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

## License

See the root [`dev-commons`](../README.md) project for license information (MIT).

---

---

## Italiano

`common-rest-connection` è un modulo Spring Boot che fornisce un client unificato per chiamate **REST** e **SOAP 1.1** tramite un'unica interfaccia iniettabile (`RestClientConnection`).

Gestisce la creazione e la configurazione di `RestTemplate` per ogni singola chiamata, con supporto per:
- timeout configurabile per richiesta
- proxy HTTP opzionale tramite `application.properties`
- helper per autenticazione Bearer e Basic
- deserializzazione trasparente di risposte JSON, XML e YAML
- costruzione di envelope SOAP 1.1 con header personalizzato

### Abilitazione

```java
@Configuration
@EnableRestConnection
public class AppConfig { }

@Autowired
private RestClientConnection restClient;
```

### Gerarchia dei modelli di richiesta

| Classe | Utilizzo |
|---|---|
| `MapRequest` | Parametri di query o corpo form-encoded come mappa chiave-valore |
| `ObjectRequest<T>` | Corpo JSON/XML serializzato da un oggetto Java tipizzato |
| `SoapRequest<B,H>` | Chiamate SOAP 1.1 con corpo JAXB annotato |
| `MapSoapRequest<H>` | Chiamate SOAP 1.1 con corpo come mappa chiave-valore |

### Esempi rapidi

```java
// GET con parametri
MapRequest req = MapRequest.newInstanceGet("https://api.example.com/users/{id}");
req.addUriParams(42);
req.setTimeout(5000);
UserDto user = restClient.entityRestTemplate(req, UserDto.class);

// POST con oggetto tipizzato
ObjectRequest<CreateUserRequest> req2 =
    ObjectRequest.newInstancePost("https://api.example.com/users");
req2.setData(new CreateUserRequest("Alice", "alice@example.com"));
req2.setBearerAuth("eyJhbGci...");
UserDto created = restClient.entityRestTemplate(req2, UserDto.class);

// Chiamata SOAP
MapSoapRequest<Void> soap = new MapSoapRequest<>(
    "https://www.w3schools.com/xml/tempconvert.asmx",
    "CelsiusToFahrenheit",
    "https://www.w3schools.com/xml/");
soap.addData("Celsius", "100");
String result = restClient.soapRestTemplate(soap, String.class);
```

### Configurazione proxy

```properties
com.bld.connection.proxy.ip=proxy.example.com
com.bld.connection.proxy.port=8080
```
