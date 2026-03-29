/*
 * @auth Francesco Baldi
 * @class com.bld.crypto.bean.CryptoUtils.java
 */
package com.bld.crypto.bean;

/**
 * Abstract marker base class for crypto utility types.
 *
 * <p>Concrete utility classes that are not key-based (i.e., they do not extend
 * {@link CryptoKeyUtils}) can extend this class to participate in the same
 * component-scan hierarchy used by the encryption module.
 */
public abstract class CryptoUtils {

}
