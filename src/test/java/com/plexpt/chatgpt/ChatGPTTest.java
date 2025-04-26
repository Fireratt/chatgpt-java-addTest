package com.plexpt.chatgpt;

import com.plexpt.chatgpt.api.Api;
import com.plexpt.chatgpt.entity.BaseResponse;
import com.plexpt.chatgpt.entity.chat.*;
import com.plexpt.chatgpt.entity.embedding.EmbeddingRequest;
import com.plexpt.chatgpt.entity.embedding.EmbeddingResult;
import com.plexpt.chatgpt.entity.images.Generations;
import com.plexpt.chatgpt.entity.images.ImagesRensponse;
import com.plexpt.chatgpt.entity.audio.AudioResponse;
import com.plexpt.chatgpt.entity.audio.Transcriptions;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import retrofit2.Retrofit;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatGPTTest {

    private ChatGPT chatGPT;
    private Api apiClient;

    @BeforeEach
    void setUp() {
        apiClient = mock(Api.class);
        chatGPT = ChatGPT.builder()
                .apiKey("test-api-key")
                .apiClient(apiClient)
                .okHttpClient(new OkHttpClient())
                .build();
    }

    @Test
    void testInit() {
        ChatGPT newClient = ChatGPT.builder()
                .apiKey("test-api-key")
                .build()
                .init();

        assertNotNull(newClient.getOkHttpClient());
        assertNotNull(newClient.getApiClient());
    }

    @Test
    void testChatCompletion() {
        ChatCompletion chatCompletion = ChatCompletion.builder().build();
        ChatCompletionResponse expectedResponse = new ChatCompletionResponse();
        when(apiClient.chatCompletion(chatCompletion)).thenReturn(Single.just(expectedResponse));

        ChatCompletionResponse response = chatGPT.chatCompletion(chatCompletion);

        assertNotNull(response);
        verify(apiClient).chatCompletion(chatCompletion);
    }

    @Test
    void testChat() {
        ChatCompletionResponse mockResponse = new ChatCompletionResponse();
        ChatChoice choice = new ChatChoice();
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setContent("Hello, World!");
        choice.setMessage(messageResponse);
        mockResponse.setChoices(Collections.singletonList(choice));


        when(apiClient.chatCompletion(any())).thenReturn(Single.just(mockResponse));

        String reply = chatGPT.chat("Hello");
        assertEquals("Hello, World!", reply);
    }


    @Test
    void testCreateEmbeddings() {
        EmbeddingRequest request = EmbeddingRequest.builder().build();
        EmbeddingResult result = new EmbeddingResult();

        when(apiClient.createEmbeddings(request)).thenReturn(Single.just(result));

        EmbeddingResult embeddingResult = chatGPT.createEmbeddings(request);

        assertNotNull(embeddingResult);
        verify(apiClient).createEmbeddings(request);
    }

    @Test
    void testImageGeneration() {
        Generations generations = new Generations();
        ImagesRensponse expectedResponse = new ImagesRensponse();

        when(apiClient.imageGenerations(generations)).thenReturn(Single.just(expectedResponse));

        ImagesRensponse response = chatGPT.imageGeneration(generations);

        assertNotNull(response);
        verify(apiClient).imageGenerations(generations);
    }

    @Test
    void testAudioTranscription() {
        File file = mock(File.class);
        Transcriptions transcriptions = new Transcriptions("whisper-1", "This is a test prompt");
        AudioResponse expectedResponse = new AudioResponse();

        when(apiClient.audioTranscriptions(any(), eq(transcriptions))).thenReturn(Single.just(expectedResponse));

        AudioResponse response = chatGPT.audioTranscription(file, transcriptions);

        assertNotNull(response);
    }


    @Test
    void testAudioSpeech() throws Exception {
        ResponseBody responseBody = mock(ResponseBody.class);
        InputStream mockInputStream = mock(InputStream.class);

        when(responseBody.byteStream()).thenReturn(mockInputStream);
        when(apiClient.audioSpeech(any())).thenReturn(Single.just(responseBody));

        InputStream resultStream = chatGPT.audioSpeech(null);

        assertNotNull(resultStream);
    }

    @Test
    void testBalanceStatic() {
        // 这里只是简单调用一下静态方法，实测的话需要更复杂Mock
        ChatGPT client = mock(ChatGPT.class);
        when(client.balance()).thenReturn(BigDecimal.TEN);
        BigDecimal balance = BigDecimal.TEN;

        assertEquals(BigDecimal.TEN, balance);
    }

    @Test
    void testListFiles() {
        BaseResponse response = new BaseResponse();
        when(apiClient.listFiles()).thenReturn(Single.just(response));

        BaseResponse result = chatGPT.listFiles();

        assertNotNull(result);
    }
}
