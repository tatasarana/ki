package com.docotel.ki.pojo;


import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemREST {

    @JsonProperty("AGENT_NAME")
    public String agentName ;

    @JsonProperty("AGENT_CODE")
    public String agentCode ;


    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode;
    }
}
