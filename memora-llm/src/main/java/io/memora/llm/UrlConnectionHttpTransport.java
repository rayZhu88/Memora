package io.memora.llm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class UrlConnectionHttpTransport implements HttpTransport {
    public HttpResponse post(
            String endpoint,
            Map<String, String> headers,
            String payload,
            int connectTimeoutMs,
            int readTimeoutMs) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setConnectTimeout(connectTimeoutMs);
            connection.setReadTimeout(readTimeoutMs);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            byte[] body = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(body.length);
            OutputStream outputStream = connection.getOutputStream();
            try {
                outputStream.write(body);
                outputStream.flush();
            } finally {
                outputStream.close();
            }

            int status = connection.getResponseCode();
            String responseBody = status >= 400
                    ? readBody(connection.getErrorStream())
                    : readBody(connection.getInputStream());
            return new HttpResponse(status, responseBody);
        } catch (IOException exception) {
            throw new LlmException("Failed to call provider endpoint " + endpoint, exception);
        }
    }

    private static String readBody(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        try {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } finally {
            reader.close();
        }
    }
}
