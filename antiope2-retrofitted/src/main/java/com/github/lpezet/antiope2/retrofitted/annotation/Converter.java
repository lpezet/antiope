/**
 * 
 */
package com.github.lpezet.antiope2.retrofitted.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author Luc Pezet
 *
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Converter {
  Class<? extends com.github.lpezet.antiope2.retrofitted.converter.Converter> value();
}
