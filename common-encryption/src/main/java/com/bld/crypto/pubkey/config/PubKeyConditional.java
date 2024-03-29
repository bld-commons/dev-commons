/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.pubkey.config.PubKeyConditional.java
 */
package com.bld.crypto.pubkey.config;

import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.bld.crypto.pubkey.config.data.PublicKeyProperties;


/**
 * The Class PubKeyConditional.
 */
public class PubKeyConditional extends SpringBootCondition {
	

	
	/**
	 * Gets the match outcome.
	 *
	 * @param context the context
	 * @param metadata the metadata
	 * @return the match outcome
	 */
	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		//AbstractEnvironment env=(AbstractEnvironment)context.getEnvironment();
		PublicKeyProperties config = Binder.get(context.getEnvironment()).bind("com.bld.crypto.public-key", PublicKeyProperties.class).orElse(null);
		return new ConditionOutcome(config!=null && MapUtils.isNotEmpty(config.getKeys()), "");
	}

}
