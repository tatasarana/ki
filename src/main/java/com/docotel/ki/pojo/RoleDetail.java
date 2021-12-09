package com.docotel.ki.pojo;

import java.util.List;

public class RoleDetail {
    private String idRole;
    private String name;
    private String desc;
    private String[] idRoleDetail;

    private List<TempMenuDetaill> menuDetaillList;

    public String getIdRole() {
        return idRole;
    }

    public void setIdRole(String idRole) {
        this.idRole = idRole;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String[] getIdRoleDetail() {
        return idRoleDetail;
    }

    public void setIdRoleDetail(String[] idRoleDetail) {
        this.idRoleDetail = idRoleDetail;
    }

    public List<TempMenuDetaill> getRoleDetailItems() {
        return menuDetaillList;
    }

    public void setRoleDetailItems(List<TempMenuDetaill> menuDetaillList) {
        this.menuDetaillList = menuDetaillList;
    }
}
