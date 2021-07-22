package com.example.capstonemainproject.infra.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConnectionProvider {

    private static final String HOST_SERVER = "http://192.168.219.107:8080";

    public String getHostServer() {
        return HOST_SERVER;
    }

    public HttpURLConnection createGETConnection(String uri) throws IOException {
        return createConnection(uri, "GET");
    }

    public HttpURLConnection createPOSTConnection(String uri, String data) throws IOException {
        HttpURLConnection post = createConnection(uri, "POST");
        post.setDoOutput(true);
        post.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

        OutputStream outputStream = post.getOutputStream();
        outputStream.write(data.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

        return post;
    }

    public HttpURLConnection createPUTConnection(String uri) throws IOException {
        return createConnection(uri, "PUT");
    }

    public HttpURLConnection createDELETEConnection(String uri) throws IOException {
        return createConnection(uri, "DELETE");
    }

    public void addHeader(HttpURLConnection connection, String property, String value) {
        connection.setRequestProperty(property, value);
    }

    public String readData(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();

        return convertInputStreamToString(inputStream);
    }

    public String readError(HttpURLConnection connection) throws IOException {
        InputStream errorStream = connection.getErrorStream();

        return convertInputStreamToString(errorStream);
    }

    private HttpURLConnection createConnection(String uri, String method) throws IOException {
        HttpURLConnection connection = null;
        URL url = new URL(uri);

        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(10000);
        connection.setDoInput(true);
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Accept", "application/json");

        return connection;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        br.close();

        return sb.toString();
    }
}
