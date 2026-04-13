/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.introspector.CryptoTypeUseAnnotationIntrospector.java
 */
package com.bld.crypto.introspector;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.bld.crypto.aes.annotation.CryptoAes;
import com.bld.crypto.aes.deserializer.DecryptAesDeserializer;
import com.bld.crypto.aes.serializer.EncryptAesSerializer;
import com.bld.crypto.hmac.annotation.CryptoHmac;
import com.bld.crypto.hmac.deserializer.DecryptHmacDeserializer;
import com.bld.crypto.hmac.serializer.EncryptHmacSerializer;
import com.bld.crypto.jks.annotation.CryptoJks;
import com.bld.crypto.jks.deserializer.DecryptJksDeserializer;
import com.bld.crypto.jks.serializer.EncryptJksSerializer;
import com.bld.crypto.pkcs12.annotation.CryptoPkcs12;
import com.bld.crypto.pkcs12.deserializer.DecryptPkcs12Deserializer;
import com.bld.crypto.pkcs12.serializer.EncryptPkcs12Serializer;
import com.bld.crypto.pubkey.annotations.CryptoPubKey;
import com.bld.crypto.pubkey.annotations.DecryptPubKey;
import com.bld.crypto.pubkey.annotations.EncryptPubKey;
import com.bld.crypto.pubkey.deserializer.DecryptPubKeyDeserializer;
import com.bld.crypto.pubkey.serializer.EncryptPubKeySerializer;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;


/**
 * Jackson {@link NopAnnotationIntrospector} that adds support for {@code ElementType.TYPE_USE}
 * on all {@code @Crypto*} annotations.
 *
 * <p>When a {@code @Crypto*} annotation is placed on a type argument instead of directly on
 * the field — e.g. {@code List<@CryptoPkcs12("id") Integer>} — Jackson's standard annotation
 * scanning does not find it and no cryptographic serializer/deserializer is registered.
 * This introspector bridges the gap by inspecting the first type argument of parameterised
 * fields/methods for the presence of a {@code @Crypto*} annotation and returning the
 * corresponding serializer or deserializer class.
 *
 * <h2>Setter / backing-field fallback</h2>
 * <p>Jackson typically resolves the <em>mutator</em> for a property as the public setter method
 * (e.g. {@code setServiceTypeId(List&lt;Integer&gt; v)}), not the private field.  A setter's
 * parameter type rarely carries a {@code TYPE_USE} annotation even when the corresponding
 * field does ({@code private List&lt;@CryptoPkcs12("id") Integer&gt; serviceTypeId}).
 * To handle this common pattern the introspector first inspects the direct member type; if that
 * yields nothing and the member is a setter, it looks up the backing field by name (following
 * the JavaBeans convention {@code setFoo} → {@code foo}, walking the class hierarchy) and
 * repeats the type-argument inspection there.
 *
 * <p>Register it as the <em>secondary</em> member of an
 * {@link com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair} so that the
 * standard {@code JacksonAnnotationIntrospector} (primary) continues to handle all normal
 * Jackson annotations, while this one only kicks in when the primary returns {@code null}.
 *
 * <p>The static helper {@link #findAnnotationOnTypeParam(BeanProperty, Class)} is used by
 * each concrete serializer/deserializer's {@code createContextual} method to retrieve the
 * annotation instance (with its configured {@code value} and {@code url} attributes) from
 * the type argument when the field-level lookup returns {@code null}.
 */
@SuppressWarnings("serial")
public class CryptoTypeUseAnnotationIntrospector extends NopAnnotationIntrospector {

	/**
	 * Returns the deserializer class for the field/method if a {@code @Crypto*} annotation
	 * is found on its first type argument; {@code null} otherwise.
	 */
	@Override
	public Object findDeserializer(Annotated a) {
		Annotation crypto = findCryptoAnnotationOnTypeParam(a);
		if (crypto instanceof CryptoPkcs12) return DecryptPkcs12Deserializer.class;
		if (crypto instanceof CryptoAes) return DecryptAesDeserializer.class;
		if (crypto instanceof CryptoHmac) return DecryptHmacDeserializer.class;
		if (crypto instanceof CryptoJks) return DecryptJksDeserializer.class;
		if (crypto instanceof CryptoPubKey || crypto instanceof DecryptPubKey) return DecryptPubKeyDeserializer.class;
		return null;
	}

	@Override
	public Object findContentDeserializer(Annotated a) {
		Annotation crypto = findCryptoAnnotationOnTypeParam(a);
		if (crypto instanceof CryptoPkcs12) return DecryptPkcs12Deserializer.class;
		if (crypto instanceof CryptoAes) return DecryptAesDeserializer.class;
		if (crypto instanceof CryptoHmac) return DecryptHmacDeserializer.class;
		if (crypto instanceof CryptoJks) return DecryptJksDeserializer.class;
		if (crypto instanceof CryptoPubKey || crypto instanceof DecryptPubKey) return DecryptPubKeyDeserializer.class;
		return null;
	}

	@Override
	public Object findContentSerializer(Annotated a) {
		Annotation crypto = findCryptoAnnotationOnTypeParam(a);
		if (crypto instanceof CryptoPkcs12) return EncryptPkcs12Serializer.class;
		if (crypto instanceof CryptoAes) return EncryptAesSerializer.class;
		if (crypto instanceof CryptoHmac) return EncryptHmacSerializer.class;
		if (crypto instanceof CryptoJks) return EncryptJksSerializer.class;
		if (crypto instanceof CryptoPubKey || crypto instanceof EncryptPubKey) return EncryptPubKeySerializer.class;
		return null;
	}

	/**
	 * Returns the serializer class for the field/method if a {@code @Crypto*} annotation
	 * is found on its first type argument; {@code null} otherwise.
	 */
	@Override
	public Object findSerializer(Annotated a) {
		Annotation crypto = findCryptoAnnotationOnTypeParam(a);
		if (crypto instanceof CryptoPkcs12) return EncryptPkcs12Serializer.class;
		if (crypto instanceof CryptoAes) return EncryptAesSerializer.class;
		if (crypto instanceof CryptoHmac) return EncryptHmacSerializer.class;
		if (crypto instanceof CryptoJks) return EncryptJksSerializer.class;
		if (crypto instanceof CryptoPubKey || crypto instanceof EncryptPubKey) return EncryptPubKeySerializer.class;
		return null;
	}

	// -------------------------------------------------------------------------
	// Internal helpers
	// -------------------------------------------------------------------------

	/**
	 * Looks for a {@code @Crypto*} annotation on the first type argument of the member.
	 * For setter methods, falls back to the backing field when the setter parameter does
	 * not carry the annotation (the common Lombok / manual-setter case).
	 */
	private static Annotation findCryptoAnnotationOnTypeParam(Annotated a) {
		if (!(a instanceof AnnotatedMember)) return null;
		Member raw = ((AnnotatedMember) a).getMember();

		// 1. Check the member directly (field type or first setter/getter parameter)
		Annotation result = firstCryptoAnnotationOnFirstTypeArg(resolveAnnotatedType(raw));
		if (result != null) return result;

		// 2. For setter methods: fall back to the backing field
		//    (handles the case where the TYPE_USE annotation is on the field but the
		//     generated/manual setter parameter does not replicate it)
		if (raw instanceof Method) {
			Field backingField = resolveBackingField((Method) raw);
			if (backingField != null)
				result = firstCryptoAnnotationOnFirstTypeArg(backingField.getAnnotatedType());
		}
		return result;
	}

	private static Annotation firstCryptoAnnotationOnFirstTypeArg(AnnotatedType annotatedType) {
		if (!(annotatedType instanceof AnnotatedParameterizedType)) return null;
		AnnotatedType[] args = ((AnnotatedParameterizedType) annotatedType).getAnnotatedActualTypeArguments();
		if (args.length == 0) return null;
		for (Annotation ann : args[0].getAnnotations()) {
			if (isCryptoAnnotation(ann)) return ann;
		}
		return null;
	}

	private static AnnotatedType resolveAnnotatedType(Member raw) {
		if (raw instanceof Field) return ((Field) raw).getAnnotatedType();
		if (raw instanceof Method) {
			Method m = (Method) raw;
			return m.getParameterCount() > 0 ? m.getAnnotatedParameterTypes()[0] : m.getAnnotatedReturnType();
		}
		return null;
	}

	/**
	 * Derives a field name from a setter name ({@code setFoo} → {@code foo}) and walks
	 * the class hierarchy to find the declared field.  Returns {@code null} when the
	 * method is not a JavaBeans setter or the field cannot be found.
	 */
	private static Field resolveBackingField(Method setter) {
		String name = setter.getName();
		if (name.length() <= 3 || !name.startsWith("set")) return null;
		String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
		return findDeclaredField(setter.getDeclaringClass(), fieldName);
	}

	private static Field findDeclaredField(Class<?> clazz, String fieldName) {
		for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
			try {
				return c.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ignored) {
				// continue up the hierarchy
			}
		}
		return null;
	}

	private static boolean isCryptoAnnotation(Annotation ann) {
		return ann instanceof CryptoPkcs12 || ann instanceof CryptoAes || ann instanceof CryptoHmac
				|| ann instanceof CryptoJks || ann instanceof CryptoPubKey
				|| ann instanceof EncryptPubKey || ann instanceof DecryptPubKey;
	}

	// -------------------------------------------------------------------------
	// Public static utility for createContextual() fallback
	// -------------------------------------------------------------------------

	/**
	 * Finds a specific {@code @Crypto*} annotation on the first type argument of the
	 * field or method referenced by the given {@link BeanProperty}.
	 *
	 * <p>Checks the primary member's type argument first; if that returns {@code null}
	 * and the primary member is a setter, repeats the lookup on the backing field so that
	 * the common pattern of annotating only the field works without requiring the setter
	 * parameter to also carry the {@code TYPE_USE} annotation.
	 *
	 * <p>Use this as a fallback inside {@code createContextual()} implementations when
	 * {@code property.getAnnotation(annotationType)} returns {@code null}.
	 *
	 * @param <A>            the annotation type to look for
	 * @param property       the bean property under inspection
	 * @param annotationType the annotation class to find
	 * @return the annotation instance, or {@code null} if not found
	 */
	public static <A extends Annotation> A findAnnotationOnTypeParam(BeanProperty property, Class<A> annotationType) {
		if (property == null || property.getMember() == null) return null;
		Member raw = property.getMember().getMember();

		// 1. Check the primary member's type argument
		A result = firstAnnotationOnFirstTypeArg(resolveAnnotatedType(raw), annotationType);
		if (result != null) return result;

		// 2. For setter methods: fall back to the backing field
		if (raw instanceof Method) {
			Field backingField = resolveBackingField((Method) raw);
			if (backingField != null)
				result = firstAnnotationOnFirstTypeArg(backingField.getAnnotatedType(), annotationType);
		}
		return result;
	}

	private static <A extends Annotation> A firstAnnotationOnFirstTypeArg(AnnotatedType annotatedType, Class<A> annotationType) {
		if (!(annotatedType instanceof AnnotatedParameterizedType)) return null;
		AnnotatedType[] args = ((AnnotatedParameterizedType) annotatedType).getAnnotatedActualTypeArguments();
		if (args.length == 0) return null;
		return args[0].getAnnotation(annotationType);
	}
}
