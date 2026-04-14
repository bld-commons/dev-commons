# common-annotations

> **Module:** `com.github.bld-commons:common-annotations`
> **Version:** 2.2.0
> **Parent:** `dev-commons`

## 🇮🇹 [Leggi in italiano](README.it.md)

---

## Description

`common-annotations` is a Spring Boot library module that enables **full Spring dependency injection (DI) inside custom Jackson serializers and deserializers**.

By default, Jackson instantiates custom handlers (serializers, deserializers, type handlers, etc.) on its own, completely bypassing the Spring context. This means that any `@Autowired` field inside a custom handler is never injected and remains `null` at runtime, causing `NullPointerException` errors that are difficult to diagnose.

This module solves the problem by registering a `SpringHandlerInstantiator` — the bridge between Jackson's handler lifecycle and Spring's `AutowireCapableBeanFactory` — applied to both the primary Spring Boot `ObjectMapper` (via a `Jackson2ObjectMapperBuilderCustomizer`) and to dedicated JSON and YAML `MappingJackson2HttpMessageConverter` beans. YAML converter support is activated automatically when `jackson-dataformat-yaml` is on the classpath.

---

## Prerequisites / Dependencies

| Dependency | Scope |
|---|---|
| `org.springframework.boot:spring-boot-starter-validation` | compile |
| `org.springframework:spring-web` | compile |
| `org.apache.commons:commons-lang3` | compile |
| `org.apache.commons:commons-collections4` | compile |
| `io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson` | compile / runtime |
| `org.bouncycastle:bcprov-jdk18on` | compile |

The module is managed by the `dev-commons` parent BOM; no explicit version declaration is needed in consumer projects.

---

## How to Enable

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.bld-commons</groupId>
    <artifactId>common-annotations</artifactId>
</dependency>
```

Then annotate any Spring `@Configuration` class with `@EnableContextAnnotation`:

```java
@Configuration
@EnableContextAnnotation
public class AppConfig {
    // nothing else required
}
```

That's all. The annotation triggers the import of `EnableContextAnnotationConfiguration`, which registers all beans automatically.

---

## The Problem This Module Solves

Consider a custom Jackson deserializer that needs a Spring-managed service:

```java
@JsonComponent
public class ProductDeserializer extends JsonDeserializer<Product> {

    @Autowired          // <-- injected only when Spring DI is enabled for Jackson
    private ProductService productService;

    @Override
    public Product deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String id = p.getText();
        return productService.findById(id);   // NullPointerException without this module!
    }
}
```

Without `common-annotations`, Jackson creates `ProductDeserializer` by calling its no-arg constructor directly. Spring never processes the instance, so `productService` is `null`.

With `@EnableContextAnnotation`, Jackson delegates instantiation to `SpringHandlerInstantiator`, which in turn asks Spring's `AutowireCapableBeanFactory` to create the instance. Spring wires all dependencies before the handler is used.

---

## Example: Deserializer With an Injected Spring Service

```java
// 1. Enable the module in your configuration
@Configuration
@EnableContextAnnotation
public class JacksonConfig { }

// 2. Write a deserializer that uses @Autowired as normal
@JsonComponent
public class OrderDeserializer extends JsonDeserializer<Order> {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Order deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String orderId = p.getValueAsString();
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
}
```

No additional wiring is required. The `OrderRepository` bean is injected automatically.

---

## Beans Registered Automatically

| Bean name | Type | Description |
|---|---|---|
| `contextHandlerInstantiator` | `HandlerInstantiator` | Bridges Jackson's handler lifecycle to Spring's `AutowireCapableBeanFactory`. This is the core bean that enables `@Autowired` inside handlers. |
| *(customizer)* | `Jackson2ObjectMapperBuilderCustomizer` | Applies `SpringHandlerInstantiator` to the **primary** Spring Boot `ObjectMapper` so the default JSON converter also injects handlers correctly. |
| `contextJackson2ObjectMapperBuilder` | `Jackson2ObjectMapperBuilder` | A fully configured mapper builder that applies the Spring-aware instantiator and all registered `Jackson2ObjectMapperBuilderCustomizer` beans. |
| `contextMappingJackson2HttpMessageConverter` | `MappingJackson2HttpMessageConverter` | JSON HTTP message converter built from the customized mapper builder. |
| `contextYamlMappingJackson2HttpMessageConverter` | `MappingJackson2HttpMessageConverter` | YAML HTTP message converter (registered only when `jackson-dataformat-yaml` is on the classpath). Supports `application/yaml`, `application/x-yaml`, `text/yaml`. |

---

## Package Structure

```
com.bld.context.annotation.config
├── EnableContextAnnotation.java           — activation annotation (@Configuration meta-annotation)
└── EnableContextAnnotationConfiguration.java — Spring configuration that registers the three beans
```

---

## License

See the root [`dev-commons`](../README.md) project for license information (MIT).

---

---

## Italiano

`common-annotations` è un modulo Spring Boot che risolve il problema dell'iniezione delle dipendenze Spring all'interno dei gestori Jackson personalizzati (`JsonSerializer`, `JsonDeserializer`, ecc.).

Per impostazione predefinita, Jackson crea i gestori personalizzati direttamente tramite reflection, senza passare per il contesto Spring. Ciò significa che qualsiasi campo annotato con `@Autowired` all'interno di un gestore rimane `null` a runtime, causando un `NullPointerException` difficile da diagnosticare.

Questo modulo risolve il problema registrando uno `SpringHandlerInstantiator` — il ponte tra il ciclo di vita dei gestori Jackson e la `AutowireCapableBeanFactory` di Spring — insieme a un `Jackson2ObjectMapperBuilder` e un `MappingJackson2HttpMessageConverter` completamente personalizzati.

### Abilitazione

```java
@Configuration
@EnableContextAnnotation
public class AppConfig { }
```

### Bean registrati automaticamente

| Nome | Tipo | Descrizione |
|---|---|---|
| `contextHandlerInstantiator` | `HandlerInstantiator` | Delega la creazione dei gestori Jackson alla `AutowireCapableBeanFactory` di Spring |
| `contextJackson2ObjectMapperBuilder` | `Jackson2ObjectMapperBuilder` | Builder configurato con lo `SpringHandlerInstantiator` |
| `contextMappingJackson2HttpMessageConverter` | `MappingJackson2HttpMessageConverter` | Converter HTTP costruito dal builder personalizzato |

### Esempio

```java
@JsonComponent
public class OrderDeserializer extends JsonDeserializer<Order> {

    @Autowired
    private OrderRepository orderRepository;   // iniettato correttamente grazie a questo modulo

    @Override
    public Order deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return orderRepository.findById(p.getValueAsString()).orElseThrow();
    }
}
```
