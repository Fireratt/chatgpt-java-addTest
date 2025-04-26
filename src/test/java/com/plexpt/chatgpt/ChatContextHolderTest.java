package com.plexpt.chatgpt;

import com.plexpt.chatgpt.entity.chat.Message;
import com.plexpt.chatgpt.util.ChatContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class ChatContextHolderTest {

    @BeforeEach
    void setUp() {
        // 清理context，以确保每个测试都是从一个干净的状态开始
        ChatContextHolder.remove("testId1");
        ChatContextHolder.remove("testId2");
    }

    @Test
    void testGetWhenIdDoesNotExist() {
        List<Message> messages = ChatContextHolder.get("testId1");

        // 验证返回的消息列表是空的
        assertNotNull(messages, "Messages should not be null");
        assertTrue(messages.isEmpty(), "Messages should be empty when the ID doesn't exist");
    }

    @Test
    void testGetWhenIdExists() {
        // 预设一个消息
        ChatContextHolder.add("testId2", "Hello, World!");
        List<Message> messages = ChatContextHolder.get("testId2");

        // 验证返回的消息列表包含该消息
        assertEquals(1, messages.size(), "Messages size should be 1");
        assertEquals("Hello, World!", messages.get(0).getContent(), "Message content should match");
    }

    @Test
    void testAddStringMessage() {
        // 添加一条消息
        ChatContextHolder.add("testId1", "Test Message");

        List<Message> messages = ChatContextHolder.get("testId1");

        // 验证消息是否被成功添加
        assertEquals(1, messages.size(), "Messages size should be 1");
        assertEquals("Test Message", messages.get(0).getContent(), "Message content should match");
    }

    @Test
    void testAddMessageObject() {
        // 创建Message对象
        Message message = Message.builder().content("Another test message").build();

        // 添加该消息
        ChatContextHolder.add("testId1", message);

        List<Message> messages = ChatContextHolder.get("testId1");

        // 验证消息是否被成功添加
        assertEquals(1, messages.size(), "Messages size should be 1");
        assertEquals("Another test message", messages.get(0).getContent(), "Message content should match");
    }

    @Test
    void testRemove() {
        // 添加一些消息
        ChatContextHolder.add("testId1", "Message 1");
        ChatContextHolder.add("testId1", "Message 2");

        // 移除对话
        ChatContextHolder.remove("testId1");

        // 验证是否成功移除
        List<Message> messages = ChatContextHolder.get("testId1");
        assertTrue(messages.isEmpty(), "Messages should be empty after removal");
    }
}

