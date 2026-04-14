# common-rest-connection

Una libreria Spring Boot che fornisce un client unificato e privo di configurazione per eseguire
chiamate **REST** e **SOAP 1.1** tramite una singola interfaccia iniettabile (`RestClientConnection`).
Avvolge il `RestTemplate` di Spring con supporto al timeout per singola richiesta, proxy HTTP opzionale,
deserializzazione trasparente di JSON/XML/YAML e una gerarchia fluente di modelli di richiesta.

---

## Indice

1. [Prerequisiti](#prerequisiti)
2. [Installazione](#installazione)
3. [Abilitare il modulo](#abilitare-il-modulo)
4. [Gerarchia dei modelli di richiesta](#gerarchia-dei-modelli-di-richiesta)
5. [Esempi REST](#esempi-rest)
   - [GET con parametri query](#get-con-parametri-query)
   - [POST con corpo tipizzato](#post-con-corpo-tipizzato)
   - [POST con corpo a mappa](#post-con-corpo-a-mappa)
   - [Restituzione di una lista](#restituzione-di-una-lista)
6. [Esempio SOAP](#esempio-soap)
7. [Configurazione proxy](#configurazione-proxy)
8. [Configurazione timeout](#configurazione-timeout)
9. [Helper per l'autenticazione](#helper-per-lautenticazione)

---

## Prerequisiti

| Requisito | Versione |
|---|---|
| Java | 17+ |
| Spring Boot | 3.x |
| Jakarta EE | 10+ (namespace Jakarta) |

---

## Installazione

Aggiungere la dipendenza al proprio `pom.xml`:

```xml
<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>common-rest-connection</artifactId>
    <version>2.2.0</version>
</dependency>
```

---

## Abilitare il modulo

Inserire `@EnableRestConnection` su una qualsiasi classe `@Configuration` dell'applicazione.
Questo importa `EnableRestConnectionConfiguration`, che attiva il component scanning
sul package `com.bld.commons.connection` e registra tutti i bean della libreria.

```java
@Configuration
@EnableRestConnection
public class AppConfig {
}
```

Una volta abilitato, è sufficiente iniettare `RestClientConnection` dove serve:

```java
@Autowired
private RestClientConnection restClient;
```

---

## Gerarchia dei modelli di richiesta

```
BasicRequest<T>                 (url, method, headers, timeout, body)
  └── RestBasicRequest<T>       (+ parametri URI, Content-Type, Accept)
        ├── MapRequest           body = Map<String,Object>
        └── ObjectRequest<T>     body = oggetto tipizzato T
  └── SoapRequest<B,H>          (+ operationName, namespace, SOAPAction, header)
        └── MapSoapRequest<H>    body = Map<String,Object>

SoapHeader<H>                   blocco soap:Header
  └── MapSoapHeader              body header = Map<String,Object>
```

| Classe | Quando usarla |
|---|---|
| `MapRequest` | Parametri query o corpo form-encoded espressi come coppie chiave-valore |
| `ObjectRequest<T>` | Corpo JSON / XML serializzato da un oggetto Java tipizzato |
| `SoapRequest<B,H>` | Chiamate SOAP 1.1 con corpo JAXB annotato |
| `MapSoapRequest<H>` | Chiamate SOAP 1.1 con corpo basato su Map |

---

## Esempi REST

### GET con parametri query

I parametri aggiunti tramite `addData` vengono automaticamente aggiunti come query string
nelle richieste GET e DELETE.

```java
MapRequest request = MapRequest.newInstanceGet("https://api.example.com/users/{id}");
request.addUriParams(42);                    // sostituisce {id}
request.addData("includeRoles", true);       // aggiunto come ?includeRoles=true
request.setTimeout(5000);                    // timeout di 5 secondi

UserDto user = restClient.entityRestTemplate(request, UserDto.class);
```

### POST con corpo tipizzato

Usare `ObjectRequest<T>` quando il corpo è un oggetto Java serializzabile (JSON per default).

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

### POST con corpo a mappa

`MapRequest` converte la mappa in una `MultiValueMap` per POST / PUT / PATCH.

```java
MapRequest request = MapRequest.newInstancePost("https://api.example.com/login");
request.addData("username", "alice");
request.addData("password", "secret");

TokenDto token = restClient.entityRestTemplate(request, TokenDto.class);
```

### Restituzione di una lista

Sia `MapRequest` sia `ObjectRequest` supportano la restituzione di liste;
passare la classe **array** come secondo argomento.

```java
MapRequest request = MapRequest.newInstanceGet("https://api.example.com/products");
request.addData("category", "libri");

List<ProductDto> products = restClient.listRestTemplate(request, ProductDto[].class);
```

---

## Esempio SOAP

Usare `MapSoapRequest` quando il corpo dell'operazione può essere espresso come coppie chiave-valore,
oppure `SoapRequest` con un oggetto JAXB per payload fortemente tipizzati.

### Chiamata SOAP con mappa (convertitore temperatura W3Schools)

```java
MapSoapRequest<Void> request = new MapSoapRequest<>(
        "https://www.w3schools.com/xml/tempconvert.asmx",
        "CelsiusToFahrenheit",
        "https://www.w3schools.com/xml/");

request.addData("Celsius", "100");
request.setTimeout(10000);

String response = restClient.soapRestTemplate(request, String.class);
```

### Chiamata SOAP con corpo JAXB e header personalizzato

```java
// Corpo
MyOperation body = new MyOperation();
body.setInput("valore");

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

### Chiamata SOAP con soap:Header

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

> **Risposta SOAP come `JsonNode`**: passare `JsonNode.class` come `responseClass` per ottenere
> l'intero envelope SOAP come albero di nodi Jackson, senza dover scrivere una classe JAXB.

---

## Configurazione proxy

Impostare le seguenti proprietà in `application.properties` (o `application.yml`):

```properties
com.bld.connection.proxy.ip=proxy.aziendale.example.com
com.bld.connection.proxy.port=8080
```

Quando entrambe le proprietà sono presenti, la libreria crea un proxy HTTP e lo applica
a ogni chiamata in uscita. Se una delle due è assente, nessun proxy viene configurato.

---

## Configurazione timeout

Il timeout si configura per singola richiesta, non globalmente:

```java
request.setTimeout(3000); // timeout di connessione + lettura in millisecondi
```

Quando non impostato (o impostato a `null`), `RestTemplate` usa il suo default (nessun timeout).

---

## Helper per l'autenticazione

`BasicRequest` (e quindi tutti i tipi di richiesta) espone due metodi di convenienza:

```java
// Token Bearer
request.setBearerAuth("eyJhbGci...");

// HTTP Basic
request.setBasicAuth("utente", "password");
```

Header personalizzati possono essere aggiunti o rimossi in qualsiasi momento:

```java
request.addHeader("X-Correlation-Id", "abc-123");
request.removeHeader("X-Correlation-Id");
```

---

## 🇬🇧 [Read in English](README.md)
