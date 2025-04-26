package com.plexpt.chatgpt;

import com.plexpt.chatgpt.listener.AbstractStreamListener;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractStreamListenerTest {

    private TestStreamListener listener;

    @BeforeEach
    void setUp() {
        listener = new TestStreamListener();
    }

    @Test
    void testOnEvent_NormalMessage() {
        EventSource eventSource = mock(EventSource.class);

        // 手写符合格式的 JSON
        String jsonData = "{\"choices\":[{\"delta\":{\"content\":\"Hello\"}}]}";

        listener.onEvent(eventSource, "id", "type", jsonData);

        assertEquals("Hello", listener.getLastMessage());
        assertEquals("Hello", listener.getReceivedMessage());
    }

    @Test
    void testOnEvent_DoneSignal() {
        EventSource eventSource = mock(EventSource.class);

        AtomicReference<String> completedMessage = new AtomicReference<>();
        listener.setOnComplete(completedMessage::set);

        // 先模拟收一条消息
        String jsonData = "{\"choices\":[{\"delta\":{\"content\":\"PartialMessage\"}}]}";
        listener.onEvent(eventSource, "id", "type", jsonData);

        // 再模拟收到 [DONE]
        listener.onEvent(eventSource, "id", "type", "[DONE]");

        assertEquals("PartialMessage", completedMessage.get());
    }

    @Test
    void testOnFailure_WithResponse() {
        EventSource eventSource = mock(EventSource.class);
        Throwable throwable = new RuntimeException("Connection Error");

        ResponseBody responseBody = ResponseBody.create(
                MediaType.get("application/json"), "error-response"
        );

        Response response = new Response.Builder()
                .code(500)
                .message("Server Error")
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost").build())
                .build()
                .newBuilder()
                .body(responseBody)
                .build();

        listener.onFailure(eventSource, throwable, response);

        assertEquals("error-response", listener.getErrorResponse());
        verify(eventSource, times(1)).cancel();
    }

    @Test
    void testOnFailure_NullResponse() {
        EventSource eventSource = mock(EventSource.class);
        Throwable throwable = new RuntimeException("Connection Error");

        listener.onFailure(eventSource, throwable, null);

        assertNull(listener.getErrorResponse());
        verify(eventSource, times(1)).cancel();
    }

    /**
     * 简单实现的测试子类
     */
    static class TestStreamListener extends AbstractStreamListener {

        private String receivedMessage;
        private String errorResponse;

        @Override
        public void onMsg(String message) {
            this.receivedMessage = message;
        }

        @Override
        public void onError(Throwable throwable, String response) {
            this.errorResponse = response;
        }

        public String getReceivedMessage() {
            return receivedMessage;
        }

        public String getErrorResponse() {
            return errorResponse;
        }

        public String getLastMessage() {
            return lastMessage;
        }
    }
}

