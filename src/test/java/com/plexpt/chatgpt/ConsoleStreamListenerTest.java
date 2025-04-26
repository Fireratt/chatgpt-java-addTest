package com.plexpt.chatgpt;

import com.plexpt.chatgpt.listener.ConsoleStreamListener;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

class ConsoleStreamListenerTest {

    private ConsoleStreamListener listener;

    @BeforeEach
    void setUp() {
        listener = new ConsoleStreamListener();
    }

    @Test
    void testOnEvent_MessageLogging() {
        EventSource eventSource = mock(EventSource.class);

        String json = "{\"choices\":[{\"delta\":{\"content\":\"TestMessage\"}}]}";

        listener.onEvent(eventSource, "id", "type", json);

        // 因为 log 不方便直接断言，这里只是确保代码运行无异常
        // 若你使用 LogCaptor 或 SystemOutRule，可以进一步验证日志输出内容
    }

    @Test
    void testOnEvent_CompletionTriggersOnComplete() {
        EventSource eventSource = mock(EventSource.class);

        // 收到部分消息
        String json = "{\"choices\":[{\"delta\":{\"content\":\"FinalText\"}}]}";
        listener.onEvent(eventSource, "id", "type", json);

        // 设置一个 onComplete 回调并触发 [DONE]
        StringBuilder result = new StringBuilder();
        listener.setOnComplete(result::append);
        listener.onEvent(eventSource, "id", "type", "[DONE]");

        assert result.toString().equals("FinalText");
    }

    @Test
    void testOnFailure_WithResponse() {
        EventSource eventSource = mock(EventSource.class);
        Throwable throwable = new RuntimeException("test error");

        ResponseBody body = ResponseBody.create(MediaType.get("application/json"), "error-body");
        Response response = new Response.Builder()
                .request(new Request.Builder().url("http://localhost").build())
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(500)
                .message("Server error")
                .body(body)
                .build();

        listener.onFailure(eventSource, throwable, response);

        verify(eventSource, times(1)).cancel();
    }

    @Test
    void testOnFailure_NullResponse() {
        EventSource eventSource = mock(EventSource.class);
        Throwable throwable = new RuntimeException("null response");

        listener.onFailure(eventSource, throwable, null);

        verify(eventSource, times(1)).cancel();
    }
}

