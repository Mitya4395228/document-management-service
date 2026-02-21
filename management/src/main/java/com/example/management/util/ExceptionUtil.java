package com.example.management.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.MessageSourceResolvable;

/**
 * Used to handle exception in messages for
 * {@link com.example.management.exception.handler.ErrorInfo}
 */
public final class ExceptionUtil {

    private ExceptionUtil() {
    }

    /**
     * In case of validation errors, get jointed messages from list of {@code MessageSourceResolvable} 
     * @param messages list of {@code MessageSourceResolvable}
     * @return {@code String} jointed messages
     */

    public static String getJointedMessages(List<? extends MessageSourceResolvable> messages) {
        return messages.stream().map(m -> m.getDefaultMessage()).collect(Collectors.joining("; "));
    }
}
