package com.docotel.ki.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Bean;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RESTQuote {

    private String type ;
    private RESTValue value ;

    // constructor
    public RESTQuote(){

    }


    public RESTQuote(String inp){
        this.type = inp ;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RESTValue getValue() {
        return value;
    }

    public void setValue(RESTValue value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
