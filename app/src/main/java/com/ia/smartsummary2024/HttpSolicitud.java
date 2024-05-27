package com.ia.smartsummary2024;

import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpSolicitud {
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS) // 15 segundos de tiempo de espera para la conexión
            .readTimeout(120, TimeUnit.SECONDS) // 90 segundos de tiempo de espera para la lectura
            .build();
    private static final Gson gson = new Gson();

    public static <T> void post(String url, Mensaje data, Callback callback) {
        String json = gson.toJson(data);
        Mensaje ms = new Mensaje();
        ms = data;

        RequestBody requestBody = new FormBody.Builder()
                .add(ms.getNombreMensaje(), ms.getMessage())
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(callback);
    }
}
