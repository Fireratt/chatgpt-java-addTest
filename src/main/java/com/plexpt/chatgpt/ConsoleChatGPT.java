package com.plexpt.chatgpt;

import com.plexpt.chatgpt.entity.chat.Message;
import com.plexpt.chatgpt.listener.ConsoleStreamListener;
import com.plexpt.chatgpt.util.Proxys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import cn.hutool.core.util.NumberUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * open ai 客户端
 *
 * @author plexpt
 */

@Slf4j
public class ConsoleChatGPT {

    public static Proxy proxy = Proxy.NO_PROXY;

    public static void main(String[] args) {
        System.out.println("ChatGPT - Java command-line interface");
        System.out.println("Press enter twice to submit your question.");
        System.out.println();
        System.out.println("按两次回车以提交您的问题！！！");

        String key = getInput("请输入APIKEY，按两次回车以提交:\n");
        check(key);

        configureProxy();

        ChatGPTStream chatGPT = ChatGPTStream.builder()
                .apiKey(key)
                .proxy(proxy)
                .build()
                .init();

        runWith(key, chatGPT);
    }

    // ✅ 新增：抽出代理配置
    public static void configureProxy() {
        System.out.println("是否使用代理？(y/n): ");
        String useProxy = getInput("按两次回车以提交:\n");

        if (useProxy.equalsIgnoreCase("y")) {
            System.out.println("请输入代理类型(http/socks): ");
            String type = getInput("按两次回车以提交:\n");

            System.out.println("请输入代理IP: ");
            String proxyHost = getInput("按两次回车以提交:\n");

            System.out.println("请输入代理端口: ");
            String portStr = getInput("按两次回车以提交:\n");
            int proxyPort = Integer.parseInt(portStr);

            proxy = "http".equalsIgnoreCase(type)
                    ? Proxys.http(proxyHost, proxyPort)
                    : Proxys.socks5(proxyHost, proxyPort);
        }
    }

    // ✅ 拆出核心逻辑
    public static void runWith(String key, ChatGPTStream chatGPT) {
        while (true) {
            String prompt = getInput("\nYou:\n");

            System.out.println("AI: ");
            CountDownLatch countDownLatch = new CountDownLatch(1);

            Message message = Message.of(prompt);
            ConsoleStreamListener listener = new ConsoleStreamListener() {
                @Override
                public void onError(Throwable throwable, String response) {
                    throwable.printStackTrace();
                    countDownLatch.countDown();
                }
            };

            listener.setOnComplete(msg -> countDownLatch.countDown());

            chatGPT.streamChatCompletion(Arrays.asList(message), listener);

            try {
                countDownLatch.await();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private static void check(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new RuntimeException("请输入正确的KEY");
        }
    }

    @SneakyThrows
    public static String getInput(String prompt) {
        System.out.print(prompt);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;
        try {
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines.stream().collect(Collectors.joining("\n"));
    }
}



