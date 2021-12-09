package com.docotel.ki.service;

import com.docotel.ki.model.master.MPnbpFeeCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.docotel.ki.repository.custom.master.MPnbpFeeCodeCustomRepository;

@Service
public class SimpakiService {
    @Value("${simpaki.api.query.billing}") private String simpakiQueryBilling;
    @Value("${simpaki.api.use.billing}") private String simpakiUseBilling;
    @Value("${simpaki.api.client.id}") private String simpakiClientId;
    @Value("${simpaki.api.secret.id}") private String simpakiSecretId;
    
    @Autowired
	private MPnbpFeeCodeCustomRepository mPnbpFeeCodeCustomRepository;

    public String getQueryBilling(String billingCode) {
    	RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHttpHeaders(billingCode);            
    	String requestJson = "{\"id_client\" : \"" + simpakiClientId + "\",\"kode_pembayaran\": \"" + billingCode + "\"}";
    	HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
    	ResponseEntity<String> response = restTemplate.exchange(simpakiQueryBilling, HttpMethod.POST, entity, String.class);
    	return response.getBody();
    }
    
    public String setUseBilling(String billingCode, String appNo) {
    	RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = createHttpHeaders(billingCode);            
    	String requestJson = "{\"id_client\" : \"" + simpakiClientId + "\",\"kode_pembayaran\": \"" + billingCode + "\",\"no_transaksi\" : \"" + appNo + "\"}";
    	HttpEntity<String> entity = new HttpEntity<String>(requestJson, headers);
    	ResponseEntity<String> response = restTemplate.exchange(simpakiUseBilling, HttpMethod.POST, entity, String.class);
    	return response.getBody();
    }
    
    private HttpHeaders createHttpHeaders(String billingCode) {
        String notEncoded = simpakiClientId + billingCode + simpakiSecretId;
        String encodedAuth = DigestUtils.sha256Hex(notEncoded.getBytes());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("AuthPNBP", "Bearer " + encodedAuth);
        return headers;
    }

    public MPnbpFeeCode getPnbpFeeCodeByCode(String code) {
		return mPnbpFeeCodeCustomRepository.selectOne("LEFT JOIN FETCH c.mFileSequence mfs LEFT JOIN FETCH c.mFileType mft  "
				+ "LEFT JOIN FETCH c.mFileTypeDetail mftd " ,"code", code, true);
	}
}
