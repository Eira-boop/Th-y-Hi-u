package vn.edu.mku.foodsuggest.utils;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class GeminiChatHelper {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";

    // DÁN TRỰC TIẾP API KEY CỦA BẠN VÀO ĐÂY (Giữ nguyên dấu ngoặc kép)
    private static final String API_KEY = "REDACTED";

    private final GeminiApiService apiService;

    public GeminiChatHelper() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(GeminiApiService.class);
    }

    public CompletableFuture<String> getRecipeSuggestion(String userInput) {
        String prompt = "Bạn là một đầu bếp chuyên nghiệp tại Việt Nam. " +
                "Người dùng có các nguyên liệu: " + userInput + ". " +
                "Hãy gợi ý 2-3 món ăn ngon, dễ làm. Trả lời ngắn gọn, có gạch đầu dòng.";

        GeminiRequest request = new GeminiRequest(
                Collections.singletonList(
                        new GeminiRequest.Content(
                                Collections.singletonList(new GeminiRequest.Part(prompt))
                        )
                )
        );

        CompletableFuture<String> future = new CompletableFuture<>();

        // Gọi API
        apiService.generateContent("gemini-3.1-flash-lite", API_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String text = response.body().candidates.get(0).content.parts.get(0).text;
                                future.complete(text);
                            } catch (Exception e) {
                                future.completeExceptionally(new Exception("Lỗi đọc dữ liệu"));
                            }
                        } else {
                            future.completeExceptionally(new Exception("Lỗi API: " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        future.completeExceptionally(t);
                    }
                });

        return future;
    }

    private interface GeminiApiService {
        @POST("v1beta/models/{model}:generateContent")
        Call<GeminiResponse> generateContent(
                @Path("model") String modelName,
                @Query("key") String apiKey,
                @Body GeminiRequest request
        );
    }
}