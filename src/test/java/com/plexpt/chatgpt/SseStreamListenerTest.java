package com.plexpt.chatgpt;

import com.plexpt.chatgpt.listener.SseStreamListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.*;

class SseStreamListenerTest {

    private SseEmitter sseEmitter;
    private SseStreamListener listener;

    @BeforeEach
    void setUp() {
        sseEmitter = mock(SseEmitter.class);
        listener = new SseStreamListener(sseEmitter);
    }

    @Test
    void testOnMsg() throws Exception {  // <- 注意这里加了 throws Exception
        String message = "Hello, SSE!";

        listener.onMsg(message);

        // 验证 send 被调用
        verify(sseEmitter, times(1)).send(message);
    }

    @Test
    void testOnError() {
        Throwable throwable = new RuntimeException("Test Error");

        listener.onError(throwable, "Some response");

        // 验证 complete 被调用
        verify(sseEmitter, times(1)).complete();
    }
}



