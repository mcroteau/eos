package test;

import eos.Eos;
import okhttp3.*;

import java.io.IOException;


public class Main {
    public static void main(String[] args) throws Exception {
        Eos eos = new Eos.Builder().withPort(3000).spawn(2000).make();
        eos.run();

        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("title", "Pushups")
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:3000/todos/save")
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            System.out.println(response.body().string());
        }

    }
}
