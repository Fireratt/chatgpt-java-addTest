package com.plexpt.chatgpt.listener;

import static org.mockito.Mockito.*;

import com.plexpt.chatgpt.util.SseHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class SseStreamListenerTest {

    @Mock
    private SseEmitter sseEmitter;

    private SseStreamListener sseStreamListener;

    private MockedStatic<SseHelper> sseHelperMockedStatic;

    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.initMocks(this);

        // Create the SseStreamListener with the mocked SseEmitter
        sseStreamListener = new SseStreamListener(sseEmitter);

        // Mock static methods of SseHelper
        sseHelperMockedStatic = mockStatic(SseHelper.class);
    }

    @Test
    public void testOnMsg() {
        // Prepare test data
        String message = "Test message";

        // Call the onMsg method
        sseStreamListener.onMsg(message);

        // Verify that SseHelper.send is called with the correct arguments
        sseHelperMockedStatic.verify(() -> SseHelper.send(sseEmitter, message), times(1));
    }

    @Test
    public void testOnError() {
        // Prepare test data
        Throwable throwable = new RuntimeException("Test error");
        String response = "Error response";

        // Call the onError method
        sseStreamListener.onError(throwable, response);

        // Verify that SseHelper.complete is called with the correct arguments
        sseHelperMockedStatic.verify(() -> SseHelper.complete(sseEmitter), times(1));
    }

    @After
    public void tearDown() {
        // Close the static mock to clean up after each test
        sseHelperMockedStatic.close();
    }
}
