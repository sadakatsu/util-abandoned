package com.sadakatsu.util;

import java.lang.reflect.Constructor;

import org.apache.commons.text.StringEscapeUtils;

public class Exceptions {
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void throwException(
        Class<T> throwableClass,
        String messageFormat,
        Object...args
    ) throws T {
        try {
            Constructor<T> constructor = throwableClass.getConstructor(String.class);
            String message = buildMessage(messageFormat, args);
            throw constructor.newInstance(message);
        } catch (Throwable t) {
            if (throwableClass != null && throwableClass.equals(t.getClass())) {
                throw (T) t;
            } else {
                throw new IllegalStateException(
                    "Failed to create the requested Throwable.  This is probably a programming error.",
                    t
                );
            }
        }
    }
    
    private static String buildMessage( String messageFormat, Object...args ) {
        String message = String.format(messageFormat, args);
        return StringEscapeUtils.escapeJava(message);
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void throwException(
        Class<T> throwableClass,
        Throwable cause,
        String messageFormat,
        Object...args
    ) throws T {
        try {
            Constructor<T> constructor = throwableClass.getConstructor(String.class, Throwable.class);
            String message = buildMessage(messageFormat, args);
            throw constructor.newInstance(message, cause);
        } catch (Throwable t) {
            if (throwableClass != null && throwableClass.equals(t.getClass())) {
                throw (T) t;
            } else {
                throw new IllegalStateException(
                    "Failed to create the requested Throwable.  This is probably a programming error.",
                    t
                );
            }
        }
    }
    
    private Exceptions() {}
}
