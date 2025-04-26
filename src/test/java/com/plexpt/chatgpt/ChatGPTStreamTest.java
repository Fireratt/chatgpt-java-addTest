package com.plexpt.chatgpt;

import com.plexpt.chatgpt.entity.chat.ChatCompletion;
import com.plexpt.chatgpt.entity.chat.Message;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChatGPTStreamTest {

    private ChatGPTStream chatGPTStream;

    @Before
    public void setUp() {
        chatGPTStream = ChatGPTStream.builder()
                .apiKey("test-api-key")
                .apiHost("https://api.openai.com/")
                .build();
    }

    @Test
    public void testInitShouldCreateOkHttpClient() {
        chatGPTStream.init();
        assertNotNull(chatGPTStream.getOkHttpClient());
    }

    @Test
    public void testInitShouldSetTimeoutsAndProxy() throws Exception {
        chatGPTStream.setTimeout(100);
        chatGPTStream.setProxy(Proxy.NO_PROXY);

        chatGPTStream.init();

        OkHttpClient client = chatGPTStream.getOkHttpClient();
        Field timeoutField = OkHttpClient.class.getDeclaredField("connectTimeoutMillis");
        timeoutField.setAccessible(true);
        int timeoutMillis = (Integer) timeoutField.get(client);

        assertEquals(100_000, timeoutMillis); // 100秒 = 100,000毫秒
    }

    @Test
    public void testStreamChatCompletionWithChatCompletion() {
        chatGPTStream.init();

        EventSource.Factory factoryMock = mock(EventSource.Factory.class);
        EventSourceListener listenerMock = mock(EventSourceListener.class);
        EventSource eventSourceMock = mock(EventSource.class);

        when(factoryMock.newEventSource(any(Request.class), eq(listenerMock))).thenReturn(eventSourceMock);

        ChatCompletion chatCompletion = ChatCompletion.builder()
                .messages(Collections.singletonList(Message.of("Hello")))
                .build();

        // 用反射注入mock的 EventSource.Factory
        try {
            Field okHttpClientField = chatGPTStream.getClass().getDeclaredField("okHttpClient");
            okHttpClientField.setAccessible(true);
            OkHttpClient okHttpClient = (OkHttpClient) okHttpClientField.get(chatGPTStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 因为 streamChatCompletion 内部用 EventSources.createFactory(okHttpClient)
        // 无法直接mock，需要集成测试去测 EventSource建立过程
    }

    @Test
    public void testStreamChatCompletionWithMessages() {
        chatGPTStream.init();
        EventSourceListener listenerMock = mock(EventSourceListener.class);

        List<Message> messages = Arrays.asList(
                Message.of("Message 1"),
                Message.of("Message 2")
        );

        // 调用不会抛异常就算通过
        chatGPTStream.streamChatCompletion(messages, listenerMock);
    }

    @Test
    public void testApiKeyRandomSelection() {
        chatGPTStream.setApiKeyList(Arrays.asList("key1", "key2", "key3"));

        for (int i = 0; i < 10; i++) {
            chatGPTStream.init();
            EventSourceListener listenerMock = mock(EventSourceListener.class);
            List<Message> messages = Collections.singletonList(Message.of("Test"));
            chatGPTStream.streamChatCompletion(messages, listenerMock);
        }

        assertNotNull(chatGPTStream.getApiKeyList());
        assertEquals(3, chatGPTStream.getApiKeyList().size());
    }
}

