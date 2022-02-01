package test;

import okhttp3.*;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SaveTest {

    @Test
    public void a_test(){
        final OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("title", "Pushups")
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:3000/todos/save")
                .post(formBody)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            String body = response.body().string();
            System.out.println(body);
            assertTrue(body.contains("<span id=\"count\">1</span>"));
        }catch (IOException ioex){}
    }
}
