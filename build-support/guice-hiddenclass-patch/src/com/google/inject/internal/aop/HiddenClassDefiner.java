package com.google.inject.internal.aop;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Drop-in replacement for Guice's HiddenClassDefiner that avoids sun.misc.Unsafe usage.
 *
 * <p>Maven bundles Guice and loads it on the core classpath. Newer JDKs emit runtime warnings
 * whenever {@code sun.misc.Unsafe::staticFieldBase} is called, which Guice uses in its original
 * HiddenClassDefiner implementation. This replacement relies solely on public
 * {@link MethodHandles} APIs to define hidden classes for AOP weaving, sidestepping Unsafe and
 * silencing those warnings.</p>
 */
final class HiddenClassDefiner implements ClassDefiner {
    private static final Object HIDDEN_CLASS_OPTIONS;
    private static final Method DEFINE_HIDDEN_CLASS;

    static {
        try {
            HIDDEN_CLASS_OPTIONS = classOptions("NESTMATE");
            DEFINE_HIDDEN_CLASS =
                    MethodHandles.Lookup.class.getMethod(
                            "defineHiddenClass",
                            byte[].class,
                            boolean.class,
                            HIDDEN_CLASS_OPTIONS.getClass());
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public Class<?> define(Class<?> targetClass, byte[] bytecode) throws Exception {
        // Create a lookup with access to the target class without touching Unsafe.
        MethodHandles.Lookup lookup =
                MethodHandles.privateLookupIn(Objects.requireNonNull(targetClass), MethodHandles.lookup());
        MethodHandles.Lookup hiddenLookup =
                (MethodHandles.Lookup) DEFINE_HIDDEN_CLASS.invoke(lookup, bytecode, false, HIDDEN_CLASS_OPTIONS);
        return hiddenLookup.lookupClass();
    }

    private static Object classOptions(String... options)
            throws ReflectiveOperationException {
        Class<?> optionClass = Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
        Method valueOf = optionClass.getMethod("valueOf", String.class);
        Object optionArray = Array.newInstance(optionClass, options.length);
        for (int i = 0; i < options.length; i++) {
            Array.set(optionArray, i, valueOf.invoke(null, options[i]));
        }
        return optionArray;
    }
}

