package com.kotor.build;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.security.ProtectionDomain;

/**
 * Java agent that redefines Guice's HiddenClassDefiner at JVM startup using the patched
 * implementation bundled alongside the agent.
 */
public final class HiddenClassDefinerAgent {

    private HiddenClassDefinerAgent() {
        // no-op
    }

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            byte[] patchedBytes =
                    readBytes("/com/google/inject/internal/aop/HiddenClassDefiner.class");

            ClassFileTransformer transformer =
                    new ClassFileTransformer() {
                        @Override
                        public byte[] transform(
                                Module module,
                                ClassLoader loader,
                                String className,
                                Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain,
                                byte[] classfileBuffer) {
                            if ("com/google/inject/internal/aop/HiddenClassDefiner".equals(className)) {
                                return patchedBytes;
                            }
                            return null;
                        }
                    };

            instrumentation.addTransformer(transformer);
        } catch (Throwable t) {
            System.err.println("[guice-hiddenclass-patch] Failed to patch HiddenClassDefiner: " + t);
        }
    }

    private static byte[] readBytes(String resourcePath) throws Exception {
        try (InputStream in = HiddenClassDefinerAgent.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource: " + resourcePath);
            }
            return in.readAllBytes();
        }
    }
}

