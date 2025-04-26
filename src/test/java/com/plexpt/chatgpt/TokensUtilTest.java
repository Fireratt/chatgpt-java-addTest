package com.plexpt.chatgpt;

import com.plexpt.chatgpt.entity.chat.Message;
import com.plexpt.chatgpt.util.TokensUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokensUtilTest {

    @Test
    void testCountTextTokens_Default() {
        String text = "Hello world!";
        int tokens = TokensUtil.countTextTokens(text);
        assertTrue(tokens > 0, "Tokens should be greater than 0");
    }

    @Test
    void testCountTextTokens_WithModel_GPT35() {
        String text = "Hello GPT-3.5!";
        String model = "gpt-3.5-turbo";
        int tokens = TokensUtil.countTextTokens(text, model);
        assertTrue(tokens > 0, "Tokens should be greater than 0");
    }

    @Test
    void testCountTextTokens_WithModel_GPT4() {
        String text = "Hello GPT-4!";
        String model = "gpt-4";
        int tokens = TokensUtil.countTextTokens(text, model);
        assertTrue(tokens > 0, "Tokens should be greater than 0");
    }

    @Test
    void testGetModelTypeByName_Valid() {
        String modelName = "gpt-4";
        assertEquals("gpt-4", TokensUtil.getModelTypeByName(modelName).getName(), "ModelType name should match gpt-4");
    }

    @Test
    void testGetModelTypeByName_Invalid() {
        String modelName = "invalid-model";
        assertEquals("gpt-3.5-turbo", TokensUtil.getModelTypeByName(modelName).getName(), "Should fallback to gpt-3.5-turbo");
    }

    @Test
    void testTokens_EmptyMessages() {
        List<Message> emptyList = new ArrayList<>();
        int tokens = TokensUtil.tokens(emptyList, "gpt-3.5-turbo");
        assertEquals(0, tokens, "Tokens should be 0 for empty list");
    }

    @Test
    void testTokens_SingleMessage_GPT35() {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder()
                .role("user")
                .content("Hello")
                .name("testName")
                .build());

        int tokens = TokensUtil.tokens(messages, "gpt-3.5-turbo");
        assertTrue(tokens > 0, "Tokens should be greater than 0 for single message");
    }

    @Test
    void testTokens_SingleMessage_GPT4() {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder()
                .role("user")
                .content("Hello GPT-4")
                .name("testName")
                .build());

        int tokens = TokensUtil.tokens(messages, "gpt-4");
        assertTrue(tokens > 0, "Tokens should be greater than 0 for GPT-4 message");
    }

    @Test
    void testTokens_SingleMessage_GPT4O() {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder()
                .role("assistant")
                .content("Response from GPT-4o")
                .name("assistantName")
                .build());

        int tokens = TokensUtil.tokens(messages, "gpt-4o");
        assertTrue(tokens > 0, "Tokens should be greater than 0 for GPT-4o message");
    }

    @Test
    void testTokens_OverloadedMethod() {
        List<Message> messages = new ArrayList<>();
        messages.add(Message.builder()
                .role("system")
                .content("System message")
                .build());

        int tokens = TokensUtil.tokens("gpt-3.5-turbo", messages);
        assertTrue(tokens > 0, "Tokens should be greater than 0 using overloaded method");
    }
}

