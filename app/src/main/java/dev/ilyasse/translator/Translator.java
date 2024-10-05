package dev.ilyasse.translator;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Translator {
    private static final String BASE_URL = "https://translate.googleapis.com";

    private final OkHttpClient client = new OkHttpClient();

    public interface TranslationCallback {
        void onSuccess(String translatedText);

        void onError(String errorMessage, int errorCode);
    }

    public void translate(Language sourceLanguage, Language targetLanguage, String text, TranslationCallback callback) {
        String sourceLangCode = sourceLanguage.getCode();
        String targetLangCode = targetLanguage.getCode();

        try {
            String encodedText = URLEncoder.encode(text.trim(), StandardCharsets.UTF_8.name());
            String urlString = String.format("%s/translate_a/single?client=gtx&sl=%s&tl=%s&dt=t&q=%s",
                    BASE_URL, sourceLangCode, targetLangCode, encodedText);

            Request request = new Request.Builder()
                    .url(urlString)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.onError(e.getMessage(), Error.NETWORK_ERROR.getCode());
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = Objects.requireNonNull(response.body()).string();
                        try {
                            String translatedText = parseTranslatedText(responseBody);
                            callback.onSuccess(translatedText);
                        } catch (Exception e) {
                            callback.onError("Parsing error: " + e.getMessage(), Error.PARSING_ERROR.getCode());
                        }
                    } else if (response.code() == 429) {
                        callback.onError("Too many requests", Error.TOO_MANY_REQUESTS.getCode());
                    } else {
                        callback.onError("Failed to translate text " + response.code(), Error.SERVER_ERROR.getCode());
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage(), -1);
        }
    }

    private String parseTranslatedText(String response) throws Exception {
        String[] parts = response.split("\"");
        if (parts.length >= 2) {
            return parts[1];
        } else {
            throw new JSONException("Translation not available");
        }
    }
}
