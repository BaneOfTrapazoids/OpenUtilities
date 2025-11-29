package com.baneoftrapazoids.OpenUtilities.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.HashMap;

public class Internet {
    public static final String PLAIN_TEXT = "text/plain; charset=UTF-8";
    public static final String JSON = "application/json";
    public static void executePost(String destURL, byte[] payload, HashMap<String, String> headers, String contentType) throws IOException, InterruptedException {
        URL url = new URL(destURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        //conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        conn.setRequestProperty("Content-Type", contentType);
        for(String key: headers.keySet()) {
            conn.setRequestProperty(key, headers.get(key));
        }
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(1000);

        conn.connect();
        OutputStream stream = conn.getOutputStream();
        stream.write(payload);
        stream.flush();
        stream.close();

        // For some reason, the request isn't actually sent out unless we try to get a response
        //byte[] response = conn.getInputStream().readAllBytes();
        int code = conn.getResponseCode();
        //System.out.println(code);
        //System.out.println(Arrays.toString(a));
    }
}
