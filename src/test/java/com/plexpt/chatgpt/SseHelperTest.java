package com.plexpt.chatgpt;

import com.plexpt.chatgpt.util.SseHelper;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.Mockito.*;

class SseHelperTest {

    @Test
    void testCompleteNormal() throws Exception {
        // 创建Mock对象
        SseEmitter emitter = mock(SseEmitter.class);

        // 调用SseHelper.complete
        SseHelper.complete(emitter);

        // 验证complete方法被调用
        verify(emitter, times(1)).complete();
    }

    @Test
    void testCompleteWithException() throws Exception {
        // 创建Mock对象
        SseEmitter emitter = mock(SseEmitter.class);

        // 设置当调用complete时抛出异常
        doThrow(new RuntimeException("Test Exception")).when(emitter).complete();

        // 调用SseHelper.complete（应该不会抛出异常）
        SseHelper.complete(emitter);

        // 验证complete仍然被调用了一次
        verify(emitter, times(1)).complete();
    }

    @Test
    void testSendNormal() throws Exception {
        // 创建Mock对象
        SseEmitter emitter = mock(SseEmitter.class);
        Object data = "Test Data";

        // 调用SseHelper.send
        SseHelper.send(emitter, data);

        // 验证send方法被调用
        verify(emitter, times(1)).send(data);
    }

    @Test
    void testSendWithException() throws Exception {
        // 创建Mock对象
        SseEmitter emitter = mock(SseEmitter.class);
        Object data = "Test Data";

        // 设置当调用send时抛出异常
        doThrow(new RuntimeException("Send Exception")).when(emitter).send(data);

        // 调用SseHelper.send（应该不会抛出异常）
        SseHelper.send(emitter, data);

        // 验证send仍然被调用了一次
        verify(emitter, times(1)).send(data);
    }
}

