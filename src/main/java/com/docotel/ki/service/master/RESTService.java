package com.docotel.ki.service.master;

import com.docotel.ki.component.LireIndexing;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.model.transaction.TxTmBrand;
import com.docotel.ki.model.transaction.TxTmBrandDetail;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.repository.custom.master.MBrandTypeCustomRepository;
import com.docotel.ki.repository.custom.transaction.MrepresentativeCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmBrandCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmBrandDetailCustomRepository;
import com.docotel.ki.repository.master.MBrandTypeRepository;
import com.docotel.ki.repository.master.MRepresentativeRepository;
import com.docotel.ki.repository.master.MUserRepository;
import com.docotel.ki.repository.transaction.TxTmBrandDetailRepository;
import com.docotel.ki.repository.transaction.TxTmBrandRepository;
import com.docotel.ki.util.DateUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import java.util.regex.*;

//import com.docotel.ki.model.transaction.TxLogMenu;



@Service
@Transactional
public class RESTService {
    @Autowired
    private  MrepresentativeCustomRepository mrepresentativeCustomRepository;
    @Autowired
    private  MRepresentativeRepository mRepresentativeRepository ;
    @Autowired
    private MUserRepository mUserRepository;

    @Transactional
    public MRepresentative synPDKKIbyNo(String noReps, String type){
        String url = "" ;
        String result = "";

        MRepresentative mRepresentative = new MRepresentative();
        url = "http://pdkki.dgip.go.id/api/konsultan/merek.dgip.go.id/fcb0c9a28982415d54043f867be7dbd1/"+noReps;
        result = getRESTinJSON(url);

        if (type == "pdkki") {
           mRepresentative = getRESTpdkki(result) ;
        } else if (type == "registerNewReps") {
            mRepresentative = getRESTpdkki(result) ;

            // Tidak boleh ada yg NULL
            mRepresentative.setNo(noReps);
            mRepresentative.setId(noReps);

            mRepresentative.setCreatedDate(mRepresentative.getCreatedDate());
            mRepresentative.setStatusFlag(false);
            mRepresentative.setCreatedBy(mRepresentative.getCreatedBy());

            try{
                mrepresentativeCustomRepository.saveNewReps(mRepresentative);
            }
            catch(Exception e){
//                System.out.println(e.getMessage());
                e.printStackTrace();
                //System.out.println("Gak bisa disimpan!!!");
            }
        }
        return  mRepresentative;
    }

    public MRepresentative syncPDKKI(MRepresentative mRepresentative) {
        String url = "http://pdkki.dgip.go.id/api/konsultan/merek.dgip.go.id/fcb0c9a28982415d54043f867be7dbd1/"+ mRepresentative.getNo();
        MRepresentative restPDKKI = getRESTpdkki(getRESTinJSON(url));

        int isUpdate = mrepresentativeCustomRepository.syncronizePDKKI(mRepresentative.getId(), restPDKKI);

        if (isUpdate > 0) {
            mRepresentative = mRepresentativeRepository.findOne(mRepresentative.getId());
        }

        return mRepresentative;
    }

    public MRepresentative getRESTpdkki(String result){
        MRepresentative mRepresentative = new MRepresentative();

        Gson g = new Gson();
        JsonObject item = g.fromJson(toPrettyFormat(result),JsonObject.class);

        String response = item.get("response").toString().replace("\"","");

        if (response.equalsIgnoreCase("success")){

            String status = (item.get("STATUS") == null ? "0" : item.get("STATUS").toString().replace("\"","") ) ;
            String no_reps = (item.get("AGENT_CODE") == null ? "" : item.get("AGENT_CODE").toString().replace("\"","") ) ;
            String agent_name = (item.get("AGENT_NAME") == null ? "" : item.get("AGENT_NAME").toString().replace("\"","") ) ;
            String office_name = (item.get("OFFICE_NAME") == null ? "" : item.get("OFFICE_NAME").toString().replace("\"","") ) ;
            String office_telp = (item.get("OFFICE_TELP") == null ? "" : item.get("OFFICE_TELP").toString().replace("\"","") ) ;
            String office_email = (item.get("OFFICE_EMAIL") == null ? "" : item.get("OFFICE_EMAIL").toString().replace("\"","") ) ;
            String office_fax = (item.get("OFFICE_FAX") == null ? "" : item.get("OFFICE_FAX").toString().replace("\"","") ) ;
            String add_street = (item.get("ADDR_STREET") == null ? "" : item.get("ADDR_STREET").toString().replace("\"","") ) ;
            String city_name = (item.get("CITY_NAME") == null ? "" : item.get("CITY_NAME").toString().replace("\"","") ) ;
            String state_name = (item.get("STATE_NAME") == null ? "" : item.get("STATE_NAME").toString().replace("\"","") ) ;
            String zipcode = (item.get("ZIPCODE") == null ? "" : item.get("ZIPCODE").toString().replace("\"","") ) ;

            String front_title = "";
            String back_title ="";
            if (item.get("FRONT_TITLE") != null )
            {
                front_title = item.get("FRONT_TITLE").toString().replace("\"","");
                front_title = front_title.replace("\\t","");
            }
            if (item.get("BACK_TITLE") != null ){
                back_title = item.get("BACK_TITLE").toString().replace("\"","");
                back_title = back_title.replace("\\t","");
            }
            String sname = front_title+" "+agent_name+" "+back_title;

            mRepresentative.setNo(no_reps);
            mRepresentative.setName(sname.trim());
            mRepresentative.setAddress(add_street);
            mRepresentative.setPhone(office_telp);
            mRepresentative.setEmail(office_email);
            mRepresentative.setOffice(office_name);
            mRepresentative.setZipCode(zipcode);

            if (status.equalsIgnoreCase("1"))
                mRepresentative.setStatusFlag(true);
            else
                mRepresentative.setStatusFlag(false);

            MProvince mprovince = new MProvince() ;
            mprovince.setName(state_name);
            mRepresentative.setmProvince(mprovince);

            MCity city = new MCity();
            city.setName(city_name.contains("Kota Jakarta") ? "Kota Administrasi " + city_name.substring(5) : city_name);
            mRepresentative.setmCity(city);

            return mRepresentative;
        }

        return mRepresentative;
    }

    public String getRESTinJSON(String resourceURL){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.parseMediaType("text/html;charset=UTF-8"));
        HttpEntity <String> entity = new HttpEntity<>(headers);
        ResponseEntity <String> response = restTemplate.exchange(resourceURL, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {

            Gson g = new Gson();
            JsonObject item = g.fromJson(toPrettyFormat(response.getBody()),JsonObject.class);
            String result = item.get("AGENT_NAME").toString() ;

            return response.getBody() ;
        }
        else
        {
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

}
