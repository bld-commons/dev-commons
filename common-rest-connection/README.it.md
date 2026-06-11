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
7. [Conversione XML in JsonNode](#conversione-xml-in-jsonnode)
8. [Configurazione proxy](#configurazione-proxy)
9. [Configurazione timeout](#configurazione-timeout)
10. [Helper per l'autenticazione](#helper-per-lautenticazione)

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
    <version>2.2.6</version>
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

## Conversione XML in JsonNode

Quando `responseClass` è `JsonNode.class` e il corpo della risposta è XML (il payload
grezzo inizia con `<`), il corpo viene convertito in un albero Jackson da
`com.bld.commons.connection.utils.XmlNodeConverter`. Lo stesso convertitore è usato
internamente anche da `SoapXmlBuilder` per mappare gli envelope SOAP senza un secondo parse.

Sono esposte **due varianti** di conversione:

| Variante | Metodo | Attivazione |
|---|---|---|
| **Legacy** | `XmlNodeConverter.fromXml(String)` / `fromElement(Element)` | Default. Usata quando `RestBasicRequest#isXmlNormalized()` è `false`. Le risposte SOAP usano sempre questa variante. |
| **Normalized** | `XmlNodeConverter.fromXmlNormalized(String)` / `fromElementNormalized(Element)` | Opt-in. Attivata invocando `request.setXmlNormalized(true)` su una qualsiasi sottoclasse di `RestBasicRequest` prima della chiamata. |

```java
ObjectRequest<Void> request = ObjectRequest.newInstanceGet("https://api.example.com/data.xml");
request.setXmlNormalized(true);                // abilita layout normalizzato
JsonNode tree = restClient.entityRestTemplate(request, JsonNode.class);
```

### Regole comuni alle due varianti

- Il nome qualificato del tag (`prefix:localName`, oppure solo `localName` se non c'è prefisso) è la chiave del nodo.
- Un singolo figlio con un dato nome qualificato diventa un `ObjectNode`.
- Più figli fratelli con lo stesso nome qualificato diventano un `ArrayNode`.
- Le dichiarazioni di namespace (`xmlns`, `xmlns:*`) vengono scartate.
- Le dichiarazioni DOCTYPE vengono rimosse dall'input prima del parsing (variante legacy) e il parser è blindato contro XXE (`disallow-doctype-decl`, nessuna entità esterna, nessuna espansione di entità).

### Storia del mapping degli attributi

Il modo in cui gli attributi XML (e il testo) vengono proiettati nell'albero Jackson è
cambiato nel tempo. Entrambe le forme convivono: la legacy preserva la retrocompatibilità
per i consumatori SOAP e i chiamanti esistenti, la normalized è la nuova forma pensata
per il mapping basato su path / harvesting (dove un layout stabile e prevedibile conta
più della concisione).

| Versione | Forma | Attributi | Testo su foglia | Foglie text-only ripetute |
|---|---|---|---|---|
| Iniziale (commit `soap`, 2026-03-30) | Forma unica — oggi chiamata **legacy** | Aggiunti come campi string diretti sull'`ObjectNode` dell'elemento, **senza prefisso**, condividendo lo namespace con le chiavi dei figli. `xmlns:*` esclusi. | Se la foglia non ha attributi né figli, il padre memorizza la stringa nuda sotto il nome del tag. Altrimenti viene incapsulata come `"value": "text"`. | Compattate in un `ArrayNode` di stringhe nude. |
| `swagger` (2026-04-13) | Identica alla iniziale | (invariato — aggiunto solo logging SLF4J in debug) | (invariato) | (invariato) |
| `rest` (2026-05-22) | Introdotta la variante **normalized** affiancata alla legacy | Prefisso `@` su ogni nome di attributo (es. `@scheme`, `@xsi:type`). `xmlns:*` continuano a essere esclusi. | Sempre incapsulato sotto la chiave `value`, anche per le foglie semplici. | Resta `ArrayNode`, ma gli elementi sono sempre `ObjectNode` che espongono `value` (ed eventuali `@attr`) — mai stringhe nude. |

### Esempio comparato

XML di partenza:

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

Output legacy (`fromXml`):

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

Output normalized (`fromXmlNormalized`):

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

Punti chiave:

- Nella variante **legacy** i nomi degli attributi e quelli dei tag figlio convivono
  nello stesso spazio dei nomi. Un figlio chiamato `id` collide con un attributo
  `id="..."` sullo stesso padre. Nella **normalized** il prefisso `@` rimuove
  l'ambiguità.
- Nella **legacy** un path come `order.tag[0]` può risolvere a una stringa o a un
  oggetto a seconda che i fratelli abbiano attributi; nella **normalized** lo stesso
  path risolve sempre a un `ObjectNode`, ed è proprio questo che rende la forma
  sicura per il mapping generico basato su path.

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
