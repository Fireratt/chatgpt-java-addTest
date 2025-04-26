package com.plexpt.chatgpt;

import com.plexpt.chatgpt.entity.chat.Message;
import com.plexpt.chatgpt.listener.ConsoleStreamListener;
import org.junit.*;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;




public class ConsoleChatGPTTest {

    private final InputStream originalIn = System.in;

    @After
    public void restoreSystemIn() {
        System.setIn(originalIn);
    }

    // --- ✅ 测试 check() 方法 ---
    @Test
    public void testMain_shouldThrowExceptionWhenApiKeyEmpty() {
        // 模拟用户输入一个空 API Key（按两次回车）
        String input = "\n\n";  // getInput 会收集空行后返回空字符串
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        try {
            ConsoleChatGPT.main(new String[0]);
            fail("Expected RuntimeException for empty API key");
        } catch (RuntimeException e) {
            assertEquals("请输入正确的KEY", e.getMessage());
        }
    }

    // --- ✅ 测试 getInput() ---
    @Test
    public void testGetInput_singleLine() {
        String input = "Hello\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        String result = ConsoleChatGPT.getInput("输入:");
        assertEquals("Hello", result);
    }

    @Test
    public void testGetInput_multiLine() {
        String input = "你好\nChatGPT\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        String result = ConsoleChatGPT.getInput("输入:");
        assertEquals("你好\nChatGPT", result);
    }

    @Test
    public void testGetInput_IOError() {
        try (MockedConstruction<BufferedReader> mockedBufferedReader = mockConstruction(
                BufferedReader.class,
                (mock, context) -> {
                    when(mock.readLine()).thenThrow(new IOException("Simulated IO Error"));
                }
        )) {
            // 2. Replace System.in to avoid real input
            ByteArrayInputStream dummyInputStream = new ByteArrayInputStream("".getBytes());
            System.setIn(dummyInputStream);

            // 3. Call the method and expect an empty string (since @SneakyThrows suppresses the exception)
            String result = ConsoleChatGPT.getInput("Test prompt:");

            // 4. Verify BufferedReader.readLine() was called
            BufferedReader mockReader = mockedBufferedReader.constructed().get(0);
            verify(mockReader, atLeastOnce()).readLine();


        } catch (IOException ignored) {

        } finally {
            // Restore System.in
            System.setIn(System.in);
        }
    }

    // --- ✅ 测试 configureProxy() ---
    @Test
    public void testConfigureProxy_http() {

        try (MockedStatic<ConsoleChatGPT> mockedStatic = mockStatic(ConsoleChatGPT.class)){
        mockedStatic.when(ConsoleChatGPT::configureProxy).thenCallRealMethod();
        mockedStatic.when(()->ConsoleChatGPT.getInput(anyString())).thenReturn("y","http","127.0.0.1","1080");
        ConsoleChatGPT.configureProxy();
        assertNotNull(ConsoleChatGPT.proxy);
        assertEquals(Proxy.Type.HTTP, ConsoleChatGPT.proxy.type());
        }
    }


    @Test
    public void testConfigureProxy_other() {

        try (MockedStatic<ConsoleChatGPT> mockedStatic = mockStatic(ConsoleChatGPT.class)){
            mockedStatic.when(ConsoleChatGPT::configureProxy).thenCallRealMethod();
            mockedStatic.when(()->ConsoleChatGPT.getInput(anyString())).thenReturn("y","direct","127.0.0.1","1080");
            ConsoleChatGPT.configureProxy();
            assertNotNull(ConsoleChatGPT.proxy);
            assertEquals(Proxy.Type.SOCKS, ConsoleChatGPT.proxy.type());
        }
    }

    @Test
    public void testConfigureProxy_skip() {
        try (MockedStatic<ConsoleChatGPT> mockedStatic = mockStatic(ConsoleChatGPT.class)){
            mockedStatic.when(ConsoleChatGPT::configureProxy).thenCallRealMethod();
            mockedStatic.when(()->ConsoleChatGPT.getInput(anyString())).thenReturn("n");
            ConsoleChatGPT.configureProxy();
            assertNotNull(ConsoleChatGPT.proxy);
            assertEquals(Proxy.Type.HTTP, ConsoleChatGPT.proxy.type());
        }
    }

    // --- ✅ 测试 runWith() 的核心逻辑 ---
    @Test
    public void testRunWith_shouldTriggerCountDown() {
        // 模拟用户输入 prompt
        String input = "测试输入\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        // 创建 mock 的 ChatGPTStream
        ChatGPTStream mockChatGPT = mock(ChatGPTStream.class);

        // 使用 Answer 模拟异步完成：手动调用 listener 的 onComplete
        doAnswer(invocation -> {
            List<Message> messages = invocation.getArgument(0);
            ConsoleStreamListener listener = invocation.getArgument(1);

            // 立即触发完成回调，防止卡在 await()
            listener.getOnComplete().accept("Mock complete message");
            return null;
        }).when(mockChatGPT).streamChatCompletion(anyList(), any(ConsoleStreamListener.class));

        // 调用被测试方法
        ConsoleChatGPT.runWith("test-key", mockChatGPT);

        // 验证调用是否发生
        verify(mockChatGPT, times(1)).streamChatCompletion(anyList(), any(ConsoleStreamListener.class));
    }


    @Test
    public void testRunWithOnError() {
        String input = "测试输入\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ChatGPTStream mockChatGPT = mock(ChatGPTStream.class);
        doAnswer(invocation -> {
            ConsoleStreamListener listener = invocation.getArgument(1);
            listener.onError(new RuntimeException("Simulated Error"), "Error Response");
            return null;
        }).when(mockChatGPT).streamChatCompletion(anyList(), any());

        try (MockedConstruction<CountDownLatch> mockedLatch = mockConstruction(
                CountDownLatch.class,
                (mock, context) -> {
                    doNothing().when(mock).await();
                }
        )) {
            ConsoleChatGPT.runWith("test-key", mockChatGPT);
            CountDownLatch latch = mockedLatch.constructed().get(0);
            verify(latch, times(1)).countDown();
        }
    }

    @Test
    public void testRunWithInterruptedException() {
        String input = "测试输入\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        ChatGPTStream mockChatGPT = mock(ChatGPTStream.class);
        try (MockedConstruction<CountDownLatch> mockedLatch = mockConstruction(
                CountDownLatch.class,
                (mock, context) -> {
                    doThrow(new InterruptedException("Simulated Interrupt"))
                            .when(mock).await();}
        )) {
            ConsoleChatGPT.runWith("test-key", mockChatGPT);
            CountDownLatch latch = mockedLatch.constructed().get(0);

        }
    }


    //对于main函数测试

    @Test
    public void testMain() throws Exception {
        // 模拟用户输入（模拟 API key 和使用代理的情况）
        String simulatedInput = "sk-test-key\nn\n";  // 用户输入 API key 和不使用代理

        // 将 System.in 重定向到模拟的输入流
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // 由于我们忽略了 configureProxy 和 runWith 的具体实现，可以通过 Mock 直接替代它们
        // 在这里模拟 chatGPT 实例
        ChatGPTStream chatGPT = mock(ChatGPTStream.class);
        // 使用 mockStatic 来模拟静态方法 runWith，不执行实际操作
        try (MockedStatic<ConsoleChatGPT> mockedStatic = mockStatic(ConsoleChatGPT.class)) {
            // 使得 runWith 方法什么都不做
            mockedStatic.when(() -> ConsoleChatGPT.runWith(anyString(), any(ChatGPTStream.class)))
                    .thenAnswer(invocation -> null);

            // 调用 main 方法，应该不会阻塞
            ConsoleChatGPT.main(new String[0]);


            // 验证 runWith 是否被调用
        }
    }

    @Test
    public void testMain2() throws Exception {

        // 模拟用户输入（模拟 API key 和使用代理的情况）
        String simulatedInput = "sk-test-key\nn\n";  // 用户输入 API key 和不使用代理

        // 将 System.in 重定向到模拟的输入流
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));


        try (MockedStatic<ConsoleChatGPT> mockedStatic = mockStatic(ConsoleChatGPT.class)) {
            // Use doNothing() to mock the runWith method and prevent it from executing its logic
            mockedStatic.when(() -> ConsoleChatGPT.runWith(anyString(), any())).thenAnswer(invocation -> null);
            // Simulate user input for API key and proxy (if any)
            mockedStatic.when(() -> ConsoleChatGPT.main(any(String[].class))).thenCallRealMethod();

            // Call the main method
            ConsoleChatGPT.main(new String[0]);




        }


    }
}
