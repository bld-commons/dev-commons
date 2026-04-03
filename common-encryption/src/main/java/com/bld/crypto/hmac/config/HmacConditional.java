/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.hmac.config.HmacConditional.java
 */
package com.bld.crypto.hmac.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.bld.crypto.hmac.config.data.HmacProperties;

/**
 * Spring condition that activates the HMAC module only when
 * {@code com.bld.crypto.hmac.secret} is present and non-blank.
 */
public class HmacConditional extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		HmacProperties config = Binder.get(context.getEnvironment())
				.bind("com.bld.crypto.hmac", HmacProperties.class).orElse(null);
		return new ConditionOutcome(config != null && StringUtils.isNotBlank(config.getSecret()), "");
	}
}
