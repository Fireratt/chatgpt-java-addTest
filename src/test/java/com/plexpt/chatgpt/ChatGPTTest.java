package com.plexpt.chatgpt;
import  com.plexpt.chatgpt.ChatGPT;
import com.plexpt.chatgpt.entity.chat.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import com.plexpt.chatgpt.api.Api;
import com.plexpt.chatgpt.entity.BaseResponse;
import com.plexpt.chatgpt.entity.DeleteResponse;
import com.plexpt.chatgpt.entity.FileResponse;
import com.plexpt.chatgpt.entity.audio.AudioResponse;
import com.plexpt.chatgpt.entity.audio.SpeechRequest;
import com.plexpt.chatgpt.entity.audio.Transcriptions;
import com.plexpt.chatgpt.entity.billing.CreditGrantsResponse;
import com.plexpt.chatgpt.entity.billing.SubscriptionData;
import com.plexpt.chatgpt.entity.billing.UseageResponse;
import com.plexpt.chatgpt.entity.embedding.EmbeddingRequest;
import com.plexpt.chatgpt.entity.embedding.EmbeddingResult;
import com.plexpt.chatgpt.entity.images.Edits;
import com.plexpt.chatgpt.entity.images.Generations;
import com.plexpt.chatgpt.entity.images.ImagesRensponse;
import com.plexpt.chatgpt.entity.images.Variations;
import com.plexpt.chatgpt.exception.ChatException;
import com.plexpt.chatgpt.util.fastjson.JSON;
import io.reactivex.Single;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.plexpt.chatgpt.util.FormatDateUtil.formatDate;
class ChatGPTTest {

    private ChatGPT chatGPT;

    @BeforeEach
    void setUp() {

    }
    ///初始化函数
    @Test
    void testInit_case1_successPath() throws IOException {
        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey("test-key")
                .apiKeyList(Arrays.asList("keyA", "keyB"))
                .proxy(Proxy.NO_PROXY) // not null
                .apiHost("http://localhost/")
                .build();

        chatGPT.init();
        OkHttpClient client = chatGPT.getOkHttpClient();
        List<Interceptor> interceptors = client.interceptors();

        Interceptor authInterceptor = interceptors.get(0);
        Interceptor errorInterceptor = interceptors.get(1);

        Request request = new Request.Builder()
                .url("http://localhost/test")
                .get()
                .build();

        Interceptor.Chain mockChain1 = mock(Interceptor.Chain.class);
        when(mockChain1.request()).thenReturn(request);
        when(mockChain1.proceed(any())).thenAnswer(invocation -> {
            Request intercepted = invocation.getArgument(0);
            assertTrue(intercepted.header("Authorization").startsWith("Bearer "));
            assertEquals("application/json", intercepted.header("Content-Type"));
            return new Response.Builder()
                    .request(intercepted)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(MediaType.get("application/json"), "{}"))
                    .build();
        });

        authInterceptor.intercept(mockChain1);

        // 成功响应应该直接返回
        Interceptor.Chain mockChain2 = mock(Interceptor.Chain.class);
        when(mockChain2.request()).thenReturn(request);
        when(mockChain2.proceed(request)).thenReturn(new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.get("application/json"), "{}"))
                .build());

        Response res = errorInterceptor.intercept(mockChain2);
        assertEquals(200, res.code());
    }




    @Test
    void testInit_case2_errorWithErrorMessage() throws IOException {
        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey("test-key")
                .apiKeyList(null)
                .proxy(null)
                .apiHost("http://localhost/")
                .build();

        chatGPT.init();
        List<Interceptor> interceptors = chatGPT.getOkHttpClient().interceptors();
        Interceptor authInterceptor = interceptors.get(0);
        Interceptor errorInterceptor = interceptors.get(1);

        Request request = new Request.Builder()
                .url("http://localhost/test")
                .get()
                .build();

        // 补充对第一个拦截器（authInterceptor）的测试，apiKeyList == null
        Interceptor.Chain mockChain1 = mock(Interceptor.Chain.class);
        when(mockChain1.request()).thenReturn(request);
        when(mockChain1.proceed(any())).thenAnswer(invocation -> {
            Request intercepted = invocation.getArgument(0);
            // 应该使用 apiKey 字段，非随机选择
            assertEquals("Bearer test-key", intercepted.header("Authorization"));
            assertEquals("application/json", intercepted.header("Content-Type"));
            return new Response.Builder()
                    .request(intercepted)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(ResponseBody.create(MediaType.get("application/json"), "{}"))
                    .build();
        });

        authInterceptor.intercept(mockChain1);

        // errorInterceptor（模拟失败 + error.message != null）
        Interceptor.Chain mockChain2 = mock(Interceptor.Chain.class);
        when(mockChain2.request()).thenReturn(request);
        when(mockChain2.proceed(request)).thenReturn(new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(400)
                .message("Bad Request")
                .body(ResponseBody.create(MediaType.get("application/json"), "{\"error\":{\"message\":\"Some API Error\"}}"))
                .build());

        ChatException ex = assertThrows(ChatException.class, () -> {
            errorInterceptor.intercept(mockChain2);
        });
        assertEquals("Some API Error", ex.getMessage());
    }



    @Test
    void testInit_case3_errorWithoutErrorMessage() throws IOException {
        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey("test-key")
                .apiKeyList(null)
                .proxy(null)
                .apiHost("http://localhost/")
                .build();

        chatGPT.init();
        List<Interceptor> interceptors = chatGPT.getOkHttpClient().interceptors();
        Interceptor errorInterceptor = interceptors.get(1);

        Request request = new Request.Builder()
                .url("http://localhost/test")
                .get()
                .build();

        Interceptor.Chain mockChain = mock(Interceptor.Chain.class);
        when(mockChain.request()).thenReturn(request);
        when(mockChain.proceed(request)).thenReturn(new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(401)
                .message("Unauthorized")
                .body(ResponseBody.create(MediaType.get("application/json"), "{}")) // 空 error 字段
                .build());

        ChatException ex = assertThrows(ChatException.class, () -> {
            errorInterceptor.intercept(mockChain);
        });
        assertEquals("ChatGPT init error!", ex.getMessage());
    }

    ///其他函数

    @Test
    void others_1() {
        // Mock Api 和返回值
        Api mockApi = mock(Api.class);

        // 构造 ChatCompletion 用于传参
        ChatCompletion mockChatCompletion = ChatCompletion.builder()
                .messages(Arrays.asList(Message.of("Hello")))
                .build();

        // 构造 ChatCompletionResponse 返回值
        ChatCompletionResponse mockChatResponse = new ChatCompletionResponse();

        ChatChoice chatChoice = new ChatChoice();
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setContent("Mocked response"); // 设定返回内容
        chatChoice.setMessage(messageResponse);

        mockChatResponse.setChoices(Collections.singletonList(chatChoice));

        when(mockApi.chatCompletion(any(ChatCompletion.class)))
                .thenReturn(Single.just(mockChatResponse));

        // 构造 EmbeddingResult
        EmbeddingResult mockEmbeddingResult = new EmbeddingResult();
        when(mockApi.createEmbeddings(any(EmbeddingRequest.class)))
                .thenReturn(Single.just(mockEmbeddingResult));

        // 构建 ChatGPT 并注入 mockApi
        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey("test-key")
                .apiKeyList(null)
                .proxy(null)
                .apiHost("http://localhost/")
                .build();
        chatGPT.setApiClient(mockApi); 

        // 测试 chatCompletion(ChatCompletion)
        ChatCompletionResponse result1 = chatGPT.chatCompletion(mockChatCompletion);
        assertNotNull(result1);
        assertEquals("Mocked response", result1.getChoices().get(0).getMessage().getContent());

        // 测试 chatCompletion(List<Message>)
        ChatCompletionResponse result2 = chatGPT.chatCompletion(Arrays.asList(Message.of("Hello")));
        assertNotNull(result2);

        // 测试 chat(String)
        String result3 = chatGPT.chat("Hello");
        assertEquals("Mocked response", result3);

        // 测试 createEmbeddings(EmbeddingRequest)
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder().input(Collections.singletonList("text")).build();
        EmbeddingResult result4 = chatGPT.createEmbeddings(embeddingRequest);
        assertNotNull(result4);

        // 测试 createEmbeddings(String, String)
        EmbeddingResult result5 = chatGPT.createEmbeddings("text", "user");
        assertNotNull(result5);
    }

    @Test
    void others_2() {
        // Mock API client 和返回值
        Api mockApi = mock(Api.class);

        // imageGeneration
        ImagesRensponse mockImageResponse = new ImagesRensponse();
        when(mockApi.imageGenerations(any(Generations.class)))
                .thenReturn(Single.just(mockImageResponse));

        // imageEdit
        when(mockApi.imageEdits(any(MultipartBody.Part.class), any(MultipartBody.Part.class), any(Edits.class)))
                .thenReturn(Single.just(mockImageResponse));

        // imageVariation
        when(mockApi.imageVariations(any(MultipartBody.Part.class), any(Variations.class)))
                .thenReturn(Single.just(mockImageResponse));

        // audioTranscription
        AudioResponse mockAudioResponse = new AudioResponse();
        when(mockApi.audioTranscriptions(any(MultipartBody.Part.class), any(Transcriptions.class)))
                .thenReturn(Single.just(mockAudioResponse));

        // audioSpeech
        byte[] fakeAudio = new byte[]{1, 2, 3};
        ResponseBody mockResponseBody = mock(ResponseBody.class);
        when(mockResponseBody.byteStream()).thenReturn(new ByteArrayInputStream(fakeAudio));
        when(mockApi.audioSpeech(any(SpeechRequest.class)))
                .thenReturn(Single.just(mockResponseBody));

        // 构建 ChatGPT 实例
        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey("test-key")
                .apiHost("http://localhost/")
                .build();
        chatGPT.setApiClient(mockApi); 

        // 测试 imageGeneration
        ImagesRensponse res1 = chatGPT.imageGeneration(mock(Generations.class));
        assertNotNull(res1);

        // 测试 imageEdit
        File fakeImage = new File("test.jpg");
        File fakeMask = new File("mask.jpg");
        ImagesRensponse res2 = chatGPT.imageEdit(fakeImage, fakeMask, mock(Edits.class));
        assertNotNull(res2);

        // 测试 imageVariation
        ImagesRensponse res3 = chatGPT.imageVariation(fakeImage, mock(Variations.class));
        assertNotNull(res3);

        // 测试 audioTranscription
        File fakeAudioFile = new File("audio.wav");
        AudioResponse res4 = chatGPT.audioTranscription(fakeAudioFile, mock(Transcriptions.class));
        assertNotNull(res4);

        // 测试 audioSpeech
        InputStream res5 = chatGPT.audioSpeech(mock(SpeechRequest.class));
        assertNotNull(res5);
    }

    @Test
    void others_3() {
        // Mock API client 和返回值
        Api mockApi = mock(Api.class);

        // audioTranslation
        AudioResponse mockAudioResponse = new AudioResponse();
        when(mockApi.audioTranslations(any(MultipartBody.Part.class), any(Transcriptions.class)))
                .thenReturn(Single.just(mockAudioResponse));

        // balance
        SubscriptionData mockSubscriptionData = new SubscriptionData();
        mockSubscriptionData.setHardLimitUsd(BigDecimal.valueOf(1000));
        when(mockApi.subscription()).thenReturn(Single.just(mockSubscriptionData));

        UseageResponse mockUseageResponse = new UseageResponse();
        mockUseageResponse.setTotalUsage(BigDecimal.valueOf(500));
        when(mockApi.usage(anyString(), anyString())).thenReturn(Single.just(mockUseageResponse));

        // creditGrants
        CreditGrantsResponse mockCreditGrantsResponse = new CreditGrantsResponse();
        when(mockApi.creditGrants()).thenReturn(Single.just(mockCreditGrantsResponse));

        // listFiles
        BaseResponse<FileResponse> mockFileResponse = new BaseResponse<>();
        when(mockApi.listFiles()).thenReturn(Single.just(mockFileResponse));

        // 构建 ChatGPT 实例
        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey("test-key")
                .apiHost("http://localhost/")
                .build();
        chatGPT.setApiClient(mockApi); 

        // 测试 audioTranslation
        File fakeAudioFile = new File("audio.wav");
        AudioResponse res1 = chatGPT.audioTranslation(fakeAudioFile, mock(Transcriptions.class));
        assertNotNull(res1);

        // 测试 balance
        BigDecimal balance = chatGPT.balance();
        assertNotNull(balance);
        assertEquals(BigDecimal.valueOf(995), balance);  //

        // 测试 creditGrants
        CreditGrantsResponse creditGrants = chatGPT.creditGrants();
        assertNotNull(creditGrants);

        // 测试 listFiles
        BaseResponse<FileResponse> fileResponse = chatGPT.listFiles();
        assertNotNull(fileResponse);
    }

    @Test
    void others_4() {
        // Mock API client 和返回值
        Api mockApi = mock(Api.class);

        // uploadFile
        FileResponse mockFileResponse = new FileResponse();
        when(mockApi.uploadFile(any(RequestBody.class), any(MultipartBody.Part.class)))
                .thenReturn(Single.just(mockFileResponse));

        // deleteFile
        DeleteResponse mockDeleteResponse = new DeleteResponse();
        when(mockApi.deleteFile(anyString()))
                .thenReturn(Single.just(mockDeleteResponse));

        // retrieveFile
        when(mockApi.retrieveFile(anyString()))
                .thenReturn(Single.just(mockFileResponse));

        // retrieveFileContent
        ResponseBody mockFileContentResponse = mock(ResponseBody.class);
        when(mockApi.retrieveFileContent(anyString()))
                .thenReturn(Single.just(mockFileContentResponse));

        // 构建 ChatGPT 实例
        ChatGPT chatGPT = ChatGPT.builder()
                .apiKey("test-key")
                .apiHost("http://localhost/")
                .build();
        chatGPT.setApiClient(mockApi); 

        // 测试 uploadFile
        MultipartBody.Part fakeFilePart = mock(MultipartBody.Part.class);
        FileResponse uploadResponse = chatGPT.uploadFile("test-purpose", fakeFilePart);
        assertNotNull(uploadResponse);
        verify(mockApi).uploadFile(any(RequestBody.class), any(MultipartBody.Part.class));

        // 测试 deleteFile
        DeleteResponse deleteResponse = chatGPT.deleteFile("file-id");
        assertNotNull(deleteResponse);
        verify(mockApi).deleteFile(anyString());

        // 测试 retrieveFile
        FileResponse retrieveFileResponse = chatGPT.retrieveFile("file-id");
        assertNotNull(retrieveFileResponse);
        verify(mockApi).retrieveFile(anyString());

        // 测试 retrieveFileContent
        ResponseBody fileContentResponse = chatGPT.retrieveFileContent("file-id");
        assertNotNull(fileContentResponse);
        verify(mockApi).retrieveFileContent(anyString());
    }

}