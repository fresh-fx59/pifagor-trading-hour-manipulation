package org.example.service;

import ch.qos.logback.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HttpService {

    public static String executeMethod(String targetURL, Map<String, String> params, String requestMethod) throws UnsupportedEncodingException {
        String urlParameters = getParamsString(params);

        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL + "?" + urlParameters);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setRequestProperty("Content-Type",
                    "application/json");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            log.error("Failed to perform http request", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String getParamsString(Map<String, String> params) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString();
        return !StringUtil.isNullOrEmpty(resultString)
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    public static String getResponse(String baseUrl, Map<String, String> parameters, String requestType) {
        try {
//            // Define the base URL and parameters
//            String baseUrl = "https://api.example.com/data";
//            Map<String, String> parameters = new HashMap<>();
//            parameters.put("param1", "value1");
//            parameters.put("param2", "value2");

            // Build the query string
            StringBuilder queryString = new StringBuilder("?");
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                queryString.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
            }
            String urlWithParams = baseUrl + queryString.toString();

            // Create the URL object
            URL url = new URL(urlWithParams);

            // Open the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestType);

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Close the connection
            connection.disconnect();

            return response.toString();

//            // Convert the response to a JSON object
//            JSONObject jsonResponse = new JSONObject(response.toString());
//
//            // Now you can work with the JSON object
//            System.out.println(jsonResponse.toString(4)); // Print the JSON object with 4-space indentation
        } catch (Exception e) {
            log.error("failed performing connection", e);
        }

        return "";
    }

}
