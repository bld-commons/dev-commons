/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.openapi.CryptoModelConverter.java
 */
package com.bld.crypto.openapi;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;

import org.springframework.core.Ordered;

import com.bld.crypto.aes.annotation.CryptoAes;
import com.bld.crypto.hmac.annotation.CryptoHmac;
import com.bld.crypto.jks.annotation.CryptoJks;
import com.bld.crypto.pkcs12.annotation.CryptoPkcs12;
import com.bld.crypto.pubkey.annotations.CryptoPubKey;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

/**
 * SpringDoc/OpenAPI {@link ModelConverter} that overrides the schema type to
 * {@code string} for fields annotated with any crypto annotation
 * ({@code @CryptoJks}, {@code @CryptoAes}, {@code @CryptoPkcs12},
 * {@code @CryptoHmac}, {@code @CryptoPubKey}).
 *
 * <p>The encrypted/signed wire value is always a Base64-encoded ciphertext or
 * token string, regardless of the underlying Java type (e.g. {@code Integer},
 * {@code Long}). Without this converter, Swagger would document the field with
 * its Java type instead of the actual serialized type.
 *
 * <p>Implements {@link Ordered} with {@link Ordered#LOWEST_PRECEDENCE} so that
 * any {@code ModelConverter} defined in the consuming microservice is placed
 * earlier in the chain. The microservice converter can call {@code chain.next()}
 * to reach this converter, receive back the {@link StringSchema}, and then add
 * further customisations (description, example, etc.) without conflict.
 *
 * <p>This bean is registered automatically by {@code CryptoSwaggerAutoConfiguration}
 * only when {@code swagger-core-jakarta} is on the classpath.
 */
public class CryptoModelConverter implements ModelConverter, Ordered {

    private static final Set<Class<? extends Annotation>> CRYPTO_ANNOTATION_TYPES = Set.of(
            CryptoJks.class,
            CryptoAes.class,
            CryptoPkcs12.class,
            CryptoHmac.class,
            CryptoPubKey.class
    );

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public Schema<?> resolve(AnnotatedType annotatedType, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (annotatedType.getCtxAnnotations() != null) {
            for (Annotation annotation : annotatedType.getCtxAnnotations()) {
                if (CRYPTO_ANNOTATION_TYPES.contains(annotation.annotationType())) {
                    return new StringSchema();
                }
            }
        }
        return chain.hasNext() ? chain.next().resolve(annotatedType, context, chain) : null;
    }
}
