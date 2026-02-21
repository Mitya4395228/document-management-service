package com.example.management.util;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;

public class ExceptionUtilTest {
    @Test
    void testGetJointedMessages() {
        var messages = List.of(new FieldError("name1", "field1", "message1"),
                new FieldError("name2", "field2", "message2"));
        var result = ExceptionUtil.getJointedMessages(messages);
        Assertions.assertEquals("message1; message2", result);
    }
}
