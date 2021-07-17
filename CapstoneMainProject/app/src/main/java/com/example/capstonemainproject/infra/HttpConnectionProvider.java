package com.example.capstonemainproject.infra;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpConnectionProvider {

    private static final String HOST_SERVER = "http://192.168.219.141:8080";

    public String getHostServer() {
        return HOST_SERVER;
    }

    public HttpURLConnection createGETConnection(String uri) {
        return createConnection(uri, "GET");
    }

    public HttpURLConnection createPOSTConnection(String uri, String data) {
        HttpURLConnection post = createConnection(uri, "POST");
        post.setDoOutput(true);
        post.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

        try {
            OutputStream outputStream = post.getOutputStream();
            outputStream.write(data.getBytes("UTF-8"));
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return post;
    }

    public HttpURLConnection createPUTConnection(String uri) {
        return createConnection(uri, "PUT");
    }

    public HttpURLConnection createDELETEConnection(String uri) {
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

    private HttpURLConnection createConnection(String uri, String method) {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(uri);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setDoInput(true);
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setRequestProperty("Accept", "application/json");

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return connection;
    }

    private String convertInputStreamToString(InputStream inputStream) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        try {
            br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line = null;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
