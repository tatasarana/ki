package com.docotel.ki.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

/**
 * @author mohd rully k
 */

@Service
public class GoogleTranslationService {

    private Logger logger = LoggerFactory.getLogger(GoogleTranslationService.class);
    
    public GoogleTranslationService() {
    }

    public String getTranslation(String keyword, boolean isEn) {
    	
    	CloseableHttpClient httpClient = HttpClients.createDefault();
        
        String key = "AIzaSyDt0WaXr60obWUbXXFDc-o0iiEYT1yHbwQ";
        String url = "https://www.googleapis.com/language/translate/v2";

        if (keyword.length() > 5000) {
            keyword = keyword.substring(0, 4999);
        }

        try {

            HttpPost post = new HttpPost(url);
            post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("q", keyword));
            urlParameters.add(new BasicNameValuePair("key", key));
            
            if (isEn) {
            	urlParameters.add(new BasicNameValuePair("source", "en"));
                urlParameters.add(new BasicNameValuePair("target", "id"));
            } else {
            	urlParameters.add(new BasicNameValuePair("source", "id"));
                urlParameters.add(new BasicNameValuePair("target", "en"));
            }            
            
            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            CloseableHttpResponse response = httpClient.execute(post);

            JsonNode responseNode = new ObjectMapper().readTree(response.getEntity().getContent());
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                //System.out.println("dump translate " +  response.getEntity().getContent());
                return responseNode.get("data").get("translations").get(0).get("translatedText").asText();
            } else {
                //System.out.println("Gagal translate GoogleTranslationService " +keyword);
                return null;
            }
        } catch (Exception e) {
            //logger.error("Error translate description class keyword: " + keyword, e);
            return "";
        }
    }

}
