package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@Controller
//@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class RESTController extends BaseController {

    public static final String REQUEST_MAPPING_AJAX_PDKKI = "/rest-pdkki" ;

    private static final Logger log = LoggerFactory.getLogger(RESTController.class);

    @RequestMapping("/getrest/{reprsId}")
    public Object GetRest(@PathVariable(value = "reprsId") String reprsId) {
        String resourceURL = "http://pdkki.dgip.go.id/api/konsultan/merek.dgip.go.id/fcb0c9a28982415d54043f867be7dbd1/"+reprsId;

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.parseMediaType("text/html;charset=UTF-8"));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(resourceURL, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response;
        }
        return null;
    }


    @RequestMapping("/getrest2")
    @ResponseBody
    public String GetRest2() {
        String resourceURL = "http://pdkki.dgip.go.id/api/konsultan/merek.dgip.go.id/fcb0c9a28982415d54043f867be7dbd1/0240-2006";

        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.parseMediaType("text/html;charset=UTF-8"));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(resourceURL, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {

                Gson g = new Gson();
                JsonObject item = g.fromJson(toPrettyFormat(response.getBody()),JsonObject.class);
                String result = item.get("AGENT_NAME").toString() ;

                return result ;
            }
            else
            {
                return "EMPTY";
            }
        }
        catch(Exception e){
            return "EMPTY";
        }




    }


    public static String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }


    @RequestMapping(value = REQUEST_MAPPING_AJAX_PDKKI, method = {RequestMethod.POST})
    public void doRESTpdkki(final HttpServletRequest request,final HttpServletResponse response) throws IOException {

//        if (isAjaxRequest(request)) {
//            setResponseAsJson(response);

        String noReps = request.getParameter("noReps");
        String resourceURL = "http://pdkki.dgip.go.id/api/konsultan/merek.dgip.go.id/fcb0c9a28982415d54043f867be7dbd1/"+noReps;
        String result ="" ;
        try{
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.parseMediaType("text/html;charset=UTF-8"));
            HttpEntity <String> entity = new HttpEntity <>(headers);
            ResponseEntity <String> getresponse = restTemplate.exchange(resourceURL, HttpMethod.GET, entity, String.class);
            if (getresponse.getStatusCode() == HttpStatus.OK) {

                Gson g = new Gson();
                JsonObject item = g.fromJson(toPrettyFormat(getresponse.getBody()), JsonObject.class);
                result = item.get("AGENT_NAME").toString();

//            return result ;
//                writeJsonResponse(response,result);
                response.getWriter().print(result);

            }
        }catch (NullPointerException nl){
            result = "EMPTY" ;
            response.getWriter().print(result);
        }
        catch (Exception e){
            result = "ERROR" ;
            response.getWriter().print(result);
        }



//        }

    } // end public void
}
