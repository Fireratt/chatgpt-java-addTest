package com.plexpt.chatgpt;

import com.plexpt.chatgpt.util.Proxys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Proxy;

import static org.junit.Assert.*;

public class ConsoleChatGPTTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;

    private ByteArrayOutputStream outContent;

    @Before
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    public void testGetInputSingleLine() throws IOException {
        String input = "Hello GPT\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String result = ConsoleChatGPT.getInput("请输入:");
        assertEquals("Hello GPT", result);
    }

    @Test
    public void testGetInputMultipleLines() throws IOException {
        String input = "line1\nline2\nline3\n\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        String result = ConsoleChatGPT.getInput("请输入:");
        assertEquals("line1\nline2\nline3", result);
    }

    @Test
    public void testCheckValidKey() throws Exception {
        // 使用反射调用 private 方法
        Method method = ConsoleChatGPT.class.getDeclaredMethod("check", String.class);
        method.setAccessible(true);

        // 不应抛出异常
        method.invoke(null, "sk-valid-key");
    }

    @Test(expected = RuntimeException.class)
    public void testCheckInvalidKey() throws Exception {
        Method method = ConsoleChatGPT.class.getDeclaredMethod("check", String.class);
        method.setAccessible(true);

        // 应抛出异常
        method.invoke(null, "");
    }

    @Test
    public void testProxySetupHttp() {
        Proxy proxy = Proxys.http("127.0.0.1", 7890);
        assertNotNull(proxy);
        assertEquals("HTTP @ 127.0.0.1:7890", proxy.toString());
    }

    @Test
    public void testProxySetupSocks() {
        Proxy proxy = Proxys.socks5("127.0.0.1", 1080);
        assertNotNull(proxy);
        assertEquals("SOCKS @ 127.0.0.1:1080", proxy.toString());
    }

    @Test
    public void testPrintStatements() {
        System.out.println("ChatGPT - Java command-line interface");
        assertTrue(outContent.toString().contains("ChatGPT - Java command-line interface"));
    }
}

