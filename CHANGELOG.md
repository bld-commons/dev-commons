# Changelog

## [2.1.8] - 2026-04-13

### common-encryption — Swagger/OpenAPI integration

Added optional SpringDoc/OpenAPI support so that fields annotated with any crypto annotation are correctly documented as `string` (Base64-encoded ciphertext or HMAC token) instead of the underlying Java type.

**New classes:**

| Class | Description |
|---|---|
| `CryptoSwaggerAutoConfiguration.java` | Spring Boot auto-configuration that registers `CryptoModelConverter` when `swagger-core-jakarta` is on the classpath |
| `CryptoModelConverter.java` | SpringDoc `ModelConverter` (lowest precedence) that resolves fields annotated with `@CryptoAes`, `@CryptoHmac`, `@CryptoJks`, `@CryptoPkcs12`, `@CryptoPubKey` to a `StringSchema` |

**New optional dependency:**

`io.swagger.core.v3:swagger-core-jakarta:2.2.28` (optional) — added to `common-encryption/pom.xml`.

---

### common-rest-connection — XmlNodeConverter improvements

Minor additions to `XmlNodeConverter.java`.

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
