// Copyright 2013 Square, Inc.
package com.github.lpezet.antiope2.retrofitted;

import java.lang.reflect.Method;

public final class TestingUtils {
  public static Method onlyMethod(Class c) {
    Method[] declaredMethods = c.getDeclaredMethods();
    if (declaredMethods.length == 1) {
      return declaredMethods[0];
    }
    throw new IllegalArgumentException("More than one method declared.");
  }
}
