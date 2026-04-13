# dev-commons

<<<<<<< Updated upstream
> **Group:** `com.github.bld-commons` | **Version:** 2.1.5 | **License:** [MIT](http://www.opensource.org/licenses/mit-license.php)
=======
> **Group:** `com.github.bld-commons` | **Version:** 2.1.7 | **License:** [MIT](http://www.opensource.org/licenses/mit-license.php)
>>>>>>> Stashed changes

A collection of Spring Boot 3.x library modules that provide reusable infrastructure for enterprise Java applications: REST/SOAP clients, Jackson DI bridging, encryption, and general-purpose utilities.

---

## Modules

| Module | Artifact | Description |
|---|---|---|
| [common-annotations](common-annotations/README.md) | `common-annotations` | Enables Spring `@Autowired` inside Jackson serializers/deserializers |
| [common-rest-connection](common-rest-connection/README.md) | `common-rest-connection` | Unified REST and SOAP 1.1 client based on `RestTemplate` |
| [bld-common-utils](bld-common-utils/README.md) | `bld-common-utils` | Date/time, string, geometry utilities, JSON annotations, validators |
| [common-encryption](common-encryption/README.md) | `common-encryption` | AES, RSA and JKS field-level encryption via Jackson annotations |

---

## Requirements

| Requirement | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 3.x |
| Maven | 3.6+ |

---

## Build

```bash
# Full build including Javadoc and sources
mvn clean install

# Skip Javadoc (faster for development)
mvn clean install -Dmaven.javadoc.skip=true

# Resume from a specific module after failure
mvn clean install -rf :common-encryption
```

---

## BOM / Dependency Management

The root POM acts as a Bill of Materials. Add it to your project's `dependencyManagement` section to import all module versions at once:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.github.bld-commons</groupId>
            <artifactId>dev-commons</artifactId>
<<<<<<< Updated upstream
            <version>2.1.5</version>
=======
            <version>2.1.7</version>
>>>>>>> Stashed changes
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then declare individual modules without specifying a version:

```xml
<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>common-annotations</artifactId>
</dependency>

<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>common-rest-connection</artifactId>
</dependency>

<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>bld-common-utils</artifactId>
</dependency>

<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>common-encryption</artifactId>
</dependency>
```

---

## Module overview

### [common-annotations](common-annotations/README.md)

Bridges Jackson's handler instantiation lifecycle to Spring's `AutowireCapableBeanFactory`. Without this module, `@Autowired` fields inside custom `JsonSerializer` / `JsonDeserializer` classes remain `null` because Jackson creates handlers by reflection without going through Spring. A single `@EnableContextAnnotation` on any configuration class activates the fix.

### [common-rest-connection](common-rest-connection/README.md)

Provides `RestClientConnection`, a single injectable interface with methods for GET/POST/PUT/DELETE REST calls and SOAP 1.1 calls. Supports per-request timeout, optional HTTP proxy, Bearer/Basic authentication helpers, and transparent JSON, XML, and YAML deserialization. Fluent request models (`MapRequest`, `ObjectRequest`, `SoapRequest`, `MapSoapRequest`) cover the most common integration patterns.

### [bld-common-utils](bld-common-utils/README.md)

General-purpose toolkit for Spring Boot applications:
- **DateUtils** — 30+ methods for conversion, arithmetic, timezone, and component extraction.
- **JSON annotations** — `@DateTimeZone`, `@UpperLowerCase`, `@TextClob`, `@Base64File`, `@GeometryPostgis`, `@CleanExcessSpaces` applied directly on entity fields.
- **Validators** — `@AllowedString` and `@AllowedNumber` for JSR-380 whitelisting.
- **Geometry** — PostGIS, WKT, WKB, KML, GeoJSON data models backed by JTS.
- **Spring formatters** — annotation-driven formatters registered automatically with `@EnableCommonUtils`.

### [common-encryption](common-encryption/README.md)

Transparent field-level encryption and decryption for JSON payloads through Jackson annotations:
- `@CryptoAes` — AES symmetric encryption (128/192/256 bit keys).
- `@CryptoPubKey` — RSA encryption with PEM public keys.
- `@CryptoJks` — RSA encryption with keys stored in JKS keystores.
- URL-safe double-Base64 encoding for use in query parameters.
- Digital signature support via `SignatureKeyUtils`.
- Configured with `@EnableCrypto` and standard `application.properties` property prefixes.

---

## Author

**Francesco Baldi** — [francesco.baldi1987@gmail.com](mailto:francesco.baldi1987@gmail.com)

---

---

## Italiano

Raccolta di moduli libreria Spring Boot 3.x che forniscono infrastruttura riutilizzabile per applicazioni Java enterprise.

### Moduli

| Modulo | Descrizione |
|---|---|
| [common-annotations](common-annotations/README.md) | Abilita `@Autowired` nei serializer/deserializer Jackson |
| [common-rest-connection](common-rest-connection/README.md) | Client REST e SOAP 1.1 unificato basato su `RestTemplate` |
| [bld-common-utils](bld-common-utils/README.md) | Utilità per date, stringhe, geometrie, annotazioni JSON e validatori |
| [common-encryption](common-encryption/README.md) | Cifratura a livello di campo (AES, RSA, JKS) tramite annotazioni Jackson |

### Requisiti

Java 17+, Spring Boot 3.x, Maven 3.6+.

### Build

```bash
mvn clean install
# oppure, senza Javadoc
mvn clean install -Dmaven.javadoc.skip=true
```

### Panoramica moduli

**common-annotations** — Risolve il problema dell'iniezione Spring nei gestori Jackson: senza questo modulo, qualsiasi campo `@Autowired` in un `JsonSerializer` o `JsonDeserializer` personalizzato rimane `null` a runtime perché Jackson crea le istanze direttamente senza passare per il contesto Spring. Basta aggiungere `@EnableContextAnnotation` a una classe di configurazione.

**common-rest-connection** — Espone `RestClientConnection`, un'interfaccia iniettabile con metodi per chiamate REST (GET, POST, PUT, DELETE) e SOAP 1.1. Supporta timeout per richiesta, proxy HTTP opzionale, helper di autenticazione Bearer/Basic e modelli di richiesta fluenti.

**bld-common-utils** — Libreria di utilità generali: gestione date con fuso orario, annotazioni Jackson per trasformazione automatica dei campi (`@UpperLowerCase`, `@DateTimeZone`, `@GeometryPostgis`, ecc.), validatori JSR-380 personalizzati e modelli di dati geometrici (PostGIS, WKT, WKB, KML, GeoJSON).

**common-encryption** — Cifratura e decifratura trasparente dei campi JSON tramite annotazioni Jackson (`@CryptoAes`, `@CryptoPubKey`, `@CryptoJks`). Supporta AES simmetrico, RSA con chiavi PEM e RSA con keystore JKS. Supporto firma digitale incluso.
