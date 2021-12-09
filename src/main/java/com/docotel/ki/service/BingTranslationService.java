package com.docotel.ki.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BingTranslationService {
    private Logger logger = LoggerFactory.getLogger(BingTranslationService.class);

    /*
        Service untuk melakukan translasi dari bahasa inggris ke bahasa indonesia.
        Bing secara periodik merubah nilai nilai key dibawah ini termasuk struktur responsenya (dalam json)
        Step yang harus dilakukan jika ada perubahan:
        1. Buka halaman bing translation menggunakan web browser.
        2. Buka devtool browser dan record network yang terjadi.
        3. Lakukan translasi dari bahasa inggris ke bahasa indonesia.
        4. lihat hasil request dan response yang terekam di devtools tadi.
        5. ubah parameter dibawah ini menyesuakan request dan response tadi.
     */
    private final String url = "https://www.bing.com/ttranslatev3?isVertical=1&&IG=0738102653F54C4BB7BBA19C8CCA6E31&IID=translator.5026.1";
    private final String sourceLangKey = "fromLang";
    private final String targetLangKey = "to";
    private final String queryKey = "text";
    private final String sourceLang = "en";
    private final String targetLang = "id";

    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    public BingTranslationService() {
    }

    public String getTranslation(String keyword) {
        // Bing, max 5000 character (16-11-2019)
        if (keyword.length() > 5000) {
            keyword = keyword.substring(0, 4999);
        }

        try {
            HttpPost post = new HttpPost(this.url);

            post.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0");
            post.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair(this.sourceLangKey, this.sourceLang));
            urlParameters.add(new BasicNameValuePair(this.targetLangKey, this.targetLang));
            urlParameters.add(new BasicNameValuePair(this.queryKey, keyword));

            try {
                post.setEntity(new UrlEncodedFormEntity(urlParameters));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            CloseableHttpResponse response = this.httpClient.execute(post);
            /*try{
                HttpEntity entity = response.getEntity();
                String res = null;
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    byte[] bytes = IOUtils.toByteArray(instream);
                    res = new String(bytes, "UTF-8");
                    instream.close();
                }
                System.out.println("dump " + res);
            } catch (IOException e) {

            }*/

            //return null;
            JsonNode responseNode = new ObjectMapper().readTree(response.getEntity().getContent());
            return responseNode.get(0).get("translations").get(0).get("text").textValue();
        } catch (Exception e) {
            //logger.error("Error translate description class keyword: " + keyword, e);
            return "";
        }
    }
}
