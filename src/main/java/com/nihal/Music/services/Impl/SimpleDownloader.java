package com.nihal.Music.services.Impl;

import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.downloader.Request;
import org.schabi.newpipe.extractor.downloader.Response;
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SimpleDownloader extends Downloader {


    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0";

    // ✅ Singleton client (reused for all requests)
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    @Override
    public Response execute(Request request) throws IOException, ReCaptchaException {

        okhttp3.Request httpRequest = new okhttp3.Request.Builder()
                .url(request.url())
                .method(request.httpMethod(),
                        request.dataToSend() != null ? RequestBody.create(request.dataToSend()) : null)
                .addHeader("User-Agent", USER_AGENT)
                .addHeader("Accept-Encoding", "identity")
                .build();

        try (okhttp3.Response response = client.newCall(httpRequest).execute()) {
            ResponseBody body = response.body();
            if (body == null) throw new IOException("Empty response body");

            return new Response(
                    response.code(),
                    response.message(),
                    response.headers().toMultimap(),
                    response.body().string(),
                    response.request().url().toString()
            );
        }
    }


}
