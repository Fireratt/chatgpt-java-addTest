package com.plexpt.chatgpt.listener;

import com.plexpt.chatgpt.entity.billing.Usage;
import com.plexpt.chatgpt.entity.chat.ChatChoice;
import com.plexpt.chatgpt.entity.chat.ChatCompletionResponse;
import com.plexpt.chatgpt.listener.AbstractStreamListener;
import com.plexpt.chatgpt.util.fastjson.JSON;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;


import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class AbstractStreamListenerTest {

    @Mock
    private Consumer<String> mockOnComplete;
    @Mock
    private EventSource eventSource;  // 模拟 EventSource

    @Mock
    private Response response;        // 模拟 Response

    @Mock
    private ChatCompletionResponse chatCompletionResponse=new ChatCompletionResponse(); // 模拟 ChatCompletionResponse

    @Spy
    private AbstractStreamListener listener; // 使用 Spy 来部分 mock 方法

    @Captor
    private ArgumentCaptor<String> argumentCaptor;

    private final String mockMessage = "This is a mock message";

    @Before
    public void setUp() {
        chatCompletionResponse.setId("test");
        List<ChatChoice> list=new ArrayList<>();
        chatCompletionResponse.setChoices(list);
        chatCompletionResponse.setCreated(1L);
        chatCompletionResponse.setUsage(new Usage());
        chatCompletionResponse.setSystemFingerprint("test");
        chatCompletionResponse.setObject("1");
        chatCompletionResponse.setModel("model");

        MockitoAnnotations.openMocks(this);


        // 创建 AbstractStreamListener 的实例并替换 onComplete 为 mock 对象



        // 设置 listener 的 onComplete 和 onMsg 方法
        listener.setOnComplete(s -> {
            // 在测试完成时打印消息
            System.out.println("Complete message: " + s);
        });
        doNothing().when(listener).onMsg(anyString());
        when(chatCompletionResponse.toPlainStringStream()).thenReturn(mockMessage);

    }

    @Test
    public void testOnEventWithValidData() {
        // 模拟正常的 event 数据
        String eventData = "{ \"text\": \"Hello, world!\" }";  // 假设这里是从服务器返回的 JSON 数据

        // mock JSON.parseObject 静态方法
        try (MockedStatic<JSON> mockedStatic = mockStatic(JSON.class)) {
            mockedStatic.when(() -> JSON.parseObject(eventData, ChatCompletionResponse.class))
                    .thenReturn(chatCompletionResponse);

            // 调用被测试方法
            listener.onEvent(eventSource, "123", "message", eventData);

            // 验证 onMsg 被调用
            verify(listener, times(1)).onMsg(anyString());
    }
    }

    @Test
    public void testOnEventWithNoData() {
        // 模拟正常的 event 数据
        String eventData = "{ \"text\": \"Hello, world!\" }";  // 假设这里是从服务器返回的 JSON 数据

        // mock JSON.parseObject 静态方法
        try (MockedStatic<JSON> mockedStatic = mockStatic(JSON.class)) {
            mockedStatic.when(() -> JSON.parseObject(eventData, ChatCompletionResponse.class))
                    .thenReturn(chatCompletionResponse);

            when(chatCompletionResponse.toPlainStringStream()).thenReturn("");
            // 调用被测试方法
            listener.onEvent(eventSource, "123", "message", eventData);

            // 验证 onMsg 被调用
            verify(listener, times(0)).onMsg(anyString());
        }
    }


    @Test
    public void testOnEventWithDoneData() {
        // 模拟 '[DONE]' 数据，表示流结束
        String eventData = "[DONE]";

        // 模拟 onComplete 被触发
        listener.onEvent(eventSource, "123", "message", eventData);


    }

    @Test
    public void testLeftData() {
        // 模拟 '[DONE]' 数据，表示流结束
        String eventData = "[DONE]";

        // 模拟 onComplete 被触发
        listener.onOpen(eventSource, null);
        listener.onClosed(eventSource);


    }

    @Test
    public void testOnFailureWithError() throws Exception {
        // 模拟一个错误
        Throwable throwable = new Throwable("Test error");

        // 模拟响应体内容
        String errorResponse = "Error response body";
        MediaType mediaType1 = MediaType.get("text/plain");
        when(response.body()).thenReturn(okhttp3.ResponseBody.create(mediaType1,"Error response body"));



        // 调用 onFailure 方法
        listener.onFailure(eventSource, throwable, response);

        // 验证日志是否输出错误
        Logger logger = LoggerFactory.getLogger(AbstractStreamListener.class);
        // 验证 onError 被调用
        verify(listener).onError(throwable, errorResponse);
    }

    @Test
    public void failure(){
        // Mock Response
        when(response.body()).thenThrow(new RuntimeException("Mocked Exception"));

        // Mock EventSource
        doNothing().when(eventSource).cancel();

        // Call the onFailure method
        listener.onFailure(eventSource, new Throwable("Test Throwable"), response);

        // Verify log behavior (you might need a logging framework like SLF4J with a custom appender to capture logs)
        // Verify eventSource.cancel() is called
        verify(eventSource, times(1)).cancel();
    }
}
