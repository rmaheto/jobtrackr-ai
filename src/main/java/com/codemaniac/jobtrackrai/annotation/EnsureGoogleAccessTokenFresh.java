package com.codemaniac.jobtrackrai.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ensures the current user's Google access token is valid before proceeding.
 * If expired, automatically refreshes using the stored refresh token.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnsureGoogleAccessTokenFresh {}

