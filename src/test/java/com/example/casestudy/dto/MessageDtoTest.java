package com.example.casestudy.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageDtoTest {

    @Test
    void shouldCreateObjectUsingNoArgsConstructor() {
        MessageDto dto = new MessageDto();
        assertNotNull(dto);
        assertNull(dto.getMessage());
    }

    @Test
    void shouldCreateObjectUsingAllArgsConstructor() {
        MessageDto dto = new MessageDto("Test");
        assertEquals("Test", dto.getMessage());
    }

    @Test
    void shouldSetAndGetMessage() {
        MessageDto dto = new MessageDto();
        dto.setMessage("Test");
        assertEquals("Test", dto.getMessage());
    }

    @Test
    void shouldBeEqualWhenMessagesAreSame() {
        MessageDto dto1 = new MessageDto("Test");
        MessageDto dto2 = new MessageDto("Test");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenMessagesDiffer() {
        MessageDto dto1 = new MessageDto("Text1");
        MessageDto dto2 = new MessageDto("Text2");

        assertNotEquals(dto1, dto2);
    }

    @Test
    void toStringShouldContainMessage() {
        MessageDto dto = new MessageDto("Test message");
        assertTrue(dto.toString().contains("Test message"));
    }
}
