package com.github.lppedd.idea.jenkins.pipeline.gdsl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a method exposed to GDSL scripts.
 *
 * @author Edoardo Luppi
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface GdslMethod { }
