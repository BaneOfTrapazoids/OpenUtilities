package com.baneoftrapazoids.OpenUtilities.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Internet {
    public static void executePost(String destURL, byte[] payload) throws IOException {
        URL url = new URL(destURL);
        System.out.println("PAYLOAD LENGTH " + payload.length);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        conn.setRequestProperty("Content-Length", Integer.toString(payload.length));
        conn.setUseCaches(false);
        conn.setDoOutput(true);

        DataOutputStream stream = new DataOutputStream(conn.getOutputStream());
        stream.write(payload);
        stream.close();
        conn.disconnect();
    }
}
