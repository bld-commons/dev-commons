# Changelog


## [2.2.6] - 2026-06-11

### common-rest-connection — `ResponseEntity` accessors and Content-Type sniffing

Added new methods that return the full `org.springframework.http.ResponseEntity` (status code + headers + body) for both REST and SOAP calls, alongside the existing body-only methods. The existing methods now delegate to the new ones, so behaviour is unchanged for current callers.

**New interface methods:**

| Interface | Method | Returns |
|---|---|---|
| `RestClientConnection` | `responseEntity(MapRequest, Class<T>)` | `ResponseEntity<T>` |
| `RestClientConnection` | `entityResponseRestTemplate(ObjectRequest<?>, Class<T>)` | `ResponseEntity<T>` |
| `RestClientConnection` | `responseListEntity(MapRequest, Class<T[]>)` | `ResponseEntity<List<T>>` |
| `RestClientConnection` | `listResponseRestTemplate(ObjectRequest<?>, Class<T[]>)` | `ResponseEntity<List<T>>` |
| `SoapClientConnection` | `responseEntity(SoapRequest<?,?>, Class<T>)` | `ResponseEntity<T>` |
| `SoapClientConnection` | `responseListEntity(SoapRequest<?,?>, Class<T[]>)` | `ResponseEntity<List<T>>` |

**Modified classes:**

| Class | Change |
|---|---|
| `RestClientConnection.java`, `SoapClientConnection.java` | Declared the new `ResponseEntity`-returning methods |
| `RestClientConnectionImpl.java`, `SoapClientConnectionImpl.java` | Implemented the new methods; the body-only methods now delegate to them. List variants rebuild the `ResponseEntity` preserving the original status code and headers |
| `RestClientConnectionImpl.java` | Added `Content-Type` sniffing: when `JsonNode` responses are parsed, the body format (XML vs JSON) is detected and a coherent `Content-Type` is set via the new `resolveContentType(HttpHeaders, boolean)` helper. Replaces a missing or incoherent server `Content-Type` with `application/xml` / `application/json`; preserves the original (including charset) when it already matches. Matching uses `JSON_CONTENT_TYPE` / `XML_CONTENT_TYPE` patterns that accept vendor suffixes (e.g. `application/hal+json`, `application/soap+xml`) |
| `AbstractClientConnection.java` | Added protected helper `arrayClass(Class<T>)` that builds the `Class<T[]>` array type for a given element class via reflection |

---

## [2.2.2] - 2026-05-07

### common-rest-connection — Fix request body serialisation for non-form payloads

Fixed a regression where non-form payloads (e.g. JSON) were being converted to a `MultiValueMap`, causing each scalar value to be serialised as a single-element array.

**Root cause:** `RestClientConnectionImpl` unconditionally wrapped `mapRequest.getData()` with `RestConnectionMapper.mapToMultiValueMap(...)` for every HTTP method in `HTTP_METHODS`, regardless of the declared `Content-Type`. For JSON bodies this produced output like `{"field":["value"]}` instead of `{"field":"value"}`.

**Modified class:**

| Class | Change |
|---|---|
| `RestClientConnectionImpl.java` | Added private helper `isFormUrlEncoded(MapRequest)` that inspects the request `Content-Type`. The body is mapped to `MultiValueMap` only when the content type is compatible with `application/x-www-form-urlencoded`; otherwise the original `Map<String,Object>` payload is forwarded unchanged so JSON / other converters serialise it correctly |

---

## [2.2.1] - 2026-05-04

### common-rest-connection — Resilient message converter initialisation

Made the YAML and XML/SOAP `HttpMessageConverter` registration in `AbstractClientConnection` fault-tolerant.

**Modified class:**

| Class | Change |
|---|---|
| `AbstractClientConnection.java` | Wrapped YAML (`application/yaml`, `application/x-yaml`, `text/yaml`) and XML/SOAP (`text/xml`, `application/xml`, `application/soap+xml`) converter setup in individual `try-catch` blocks so that a missing optional dependency (e.g. `jackson-dataformat-yaml`) no longer prevents application startup; a `WARN` log is emitted instead |

---

## [2.2.0] - 2026-04-14

### Version bump

Version aligned to 2.2.0 across all modules.

---

## [2.1.7] - 2026-04-10

### common-annotations — Fix Spring DI in Jackson handlers (JSON & YAML)

Fixed a bug where `@Autowired` fields inside custom Jackson serializers and deserializers remained `null` at runtime when responses used the default JSON converter or the YAML converter.

**Root cause:** `SpringHandlerInstantiator` was configured only on the secondary `contextMappingJackson2HttpMessageConverter` bean, not on the primary `ObjectMapper` created by Spring Boot's auto-configuration. The default `MappingJackson2HttpMessageConverter` (and any YAML converter) therefore never went through Spring's `AutowireCapableBeanFactory`, leaving all `@Autowired` fields uninitialised.

**Modified classes:**

| Class | Change |
|---|---|
| `EnableContextAnnotationConfiguration.java` | Added `handlerInstantiatorCustomizer` (`Jackson2ObjectMapperBuilderCustomizer`) to propagate `SpringHandlerInstantiator` to the primary Spring Boot `ObjectMapper` |
| `EnableContextAnnotationConfiguration.java` | Added inner `YamlConverterConfiguration` (`@ConditionalOnClass(YAMLFactory.class)`) that registers a YAML-capable `MappingJackson2HttpMessageConverter` with `SpringHandlerInstantiator` for `application/yaml`, `application/x-yaml` and `text/yaml` |

**Removed (redundant) from all serializer/deserializer subclasses:**

`injectObjectMapper(ObjectMapper)` setter methods added as a workaround in `EncryptAesSerializer`, `DecryptAesDeserializer`, `EncryptHmacSerializer`, `DecryptHmacDeserializer`, `EncryptJksSerializer`, `DecryptJksDeserializer`, `EncryptPkcs12Serializer`, `DecryptPkcs12Deserializer`, `EncryptPubKeySerializer`, `DecryptPubKeyDeserializer` — now unnecessary because `SpringHandlerInstantiator` correctly injects inherited `@Autowired` fields from the superclass.

**New optional dependency:**

`com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` (optional) — version managed by `spring-boot-starter-parent`.

---


## [2.1.6] - 2026-04-05

### common-encryption — HMAC support

Added support for HMAC (Hash-based Message Authentication Code) signing and verification via a new set of classes, alongside the existing AES, RSA/PEM, JKS and PKCS12 mechanisms.

**New classes:**

| Class | Description |
|---|---|
| `CryptoHmac.java` | New Jackson annotation `@CryptoHmac` to mark fields for HMAC signing/verification |
| `CryptoHmacUtils.java` | Utility for computing and verifying HMAC digests |
| `HmacConfiguration.java` | Spring bean that initialises the HMAC context |
| `HmacConditional.java` | Conditional that activates the HMAC context only when configured |
| `HmacFormatterConfiguration.java` | Registers HMAC formatters in the Spring MVC context |
| `CryptoHmacSecret.java` | Data class holding the HMAC secret |
| `HmacProperties.java` | Configuration properties (`hmac.*`) for secret and algorithm |
| `DecryptHmacDeserializer.java` | `JsonDeserializer` that automatically verifies fields annotated with `@CryptoHmac` |
| `EncryptHmacSerializer.java` | `JsonSerializer` that automatically signs fields annotated with `@CryptoHmac` |
| `CryptoHmacAnnotationFormatterFactory.java` | Annotation-driven formatter factory for HMAC |
| `CryptoHmacFormatter.java` | Spring formatter for bidirectional conversion of HMAC-signed values |

**Modified classes:**

- `CryptoJksFormatter.java`, `CryptoPkcs12Formatter.java` — minor adjustments

---

### common-rest-connection — Client refactoring and SOAP split

Refactored the REST/SOAP client to separate REST and SOAP concerns into dedicated interfaces and implementations.

**New classes:**

| Class | Description |
|---|---|
| `AbstractClientConnection.java` | Abstract base class with shared HTTP execution logic |
| `SoapClientConnection.java` | Dedicated interface for SOAP 1.1 calls (extracted from `RestClientConnection`) |
| `SoapClientConnectionImpl.java` | Dedicated implementation for SOAP 1.1 calls |

**Modified / renamed classes:**

- `RestClientConnection.java` — SOAP methods removed; now focused exclusively on REST
- `RestClientConnectionImpl.java` — shared logic extracted to `AbstractClientConnection`
- `MapDataHolder.java` → renamed to `BasicMapRequest.java`
- `MapRequest.java`, `MapSoapHeader.java`, `MapSoapRequest.java`, `RestBasicRequest.java` — minor updates

---

## [2.1.5] - 2026-04-03

### common-encryption — PKCS12 support

Added support for encryption/decryption via **PKCS12** keystores (`.p12` / `.pfx`), alongside the existing AES, RSA/PEM and JKS mechanisms.

**New classes:**

| Class | Description |
|---|---|
| `CryptoPkcs12.java` | New Jackson annotation `@CryptoPkcs12` to mark fields for encryption/decryption |
| `CryptoPkcs12Utils.java` | Utility for loading keys from a PKCS12 keystore and performing cryptographic operations |
| `CryptoPkcs12Configuration.java` | Spring bean that initialises the PKCS12 encryption context |
| `Pkcs12FormatterConfiguration.java` | Registers PKCS12 formatters in the Spring MVC context |
| `Pkcs12KeyProperties.java` | Configuration properties (`pkcs12.key.*`) for keystore path, alias and password |
| `DecryptPkcs12Deserializer.java` | `JsonDeserializer` that automatically decrypts fields annotated with `@CryptoPkcs12` |
| `EncryptPkcs12Serializer.java` | `JsonSerializer` that automatically encrypts fields annotated with `@CryptoPkcs12` |
| `CryptoPkcs12AnnotationFormatterFactory.java` | Annotation-driven formatter factory for PKCS12 |
| `CryptoPkcs12Formatter.java` | Spring formatter for bidirectional conversion of PKCS12-encrypted values |

**Modified classes:**

- `EnableCrypto.java` — imports the new `CryptoPkcs12Configuration`
- `spring-configuration-metadata.json` — added `pkcs12.key` configuration prefix

---

### common-rest-connection — SOAP models and XML utilities

Added new helper classes for SOAP 1.1 calls and XML document handling.

**New classes:**

| Class | Description |
|---|---|
| `SoapRequest.java` | Base model for SOAP 1.1 requests |
| `MapSoapRequest.java` | `SoapRequest` variant with a `Map<String, Object>` payload |
| `SoapHeader.java` | Represents a single SOAP header entry |
| `MapSoapHeader.java` | Key-value map of SOAP headers |
| `MapDataHolder.java` | Generic container for map-based payloads |
| `RestBasicRequest.java` | Common base model for REST requests |
| `SoapXmlBuilder.java` | Builder that constructs SOAP 1.1 envelopes from Java models |
| `XmlNodeConverter.java` | Utility for converting and navigating XML nodes (`org.w3c.dom`) |

---

### bld-common-utils — Javadoc and comments

Added full Javadoc to the main classes of the module:

- `CamelCaseUtils`, `CommonUtility`, `DateUtils`
- `EnableCommonUtilsConfiguration`, `EnableCommonUtils`
- Data models: `BaseModel`, `CollectionResponse`, `ObjectResponse`, `GeoJsonGeometry`, `KMLGeometry`, `PostgisGeometry`, `WKBGeometry`, `WKTGeometry`, `TypologicalModel`
- Formatters: `ClobAnnotationFormatterFactory`, `ClobFormatter`, `DateFilterAnnotationFormatterFactory`, `DateFormatter`

---

### common-annotations — Javadoc

Added Javadoc to `EnableContextAnnotation` and `EnableContextAnnotationConfiguration`.

---

### common-encryption — Javadoc

Added Javadoc to existing classes:
`CryptoKeyData`, `CryptoKeyUtils`, `CryptoUtils`, `KeyUtility`, `CryptoConfiguration`, `DecryptCertificateDeserializer`, `CryptoException`, `CryptoFormatter`, `EncryptCertificateSerializer`, `AesSizeType`, `CryptoType`, `InstanceType`.

---

### Documentation

Added and updated README files for all modules (English and Italian):

- `README.md` (root) — version updated to 2.1.5
- `common-annotations/README.md`, `README.it.md`
- `common-rest-connection/README.md`, `README.it.md`
- `bld-common-utils/README.md`
- `common-encryption/README.md`

---

## [2.1.4] - previous release
