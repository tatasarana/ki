package com.docotel.ki.pojo;

import java.io.Serializable;
import java.util.List;

public class DataMenuDetail implements Serializable {
    private String roleId; //id role detail
    private String[] desc; //nama role menu detail

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String[] getDesc() {
        return desc;
    }

    public void setDesc(String[] desc) {
        this.desc = desc;
    }
}
