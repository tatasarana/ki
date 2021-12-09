package com.docotel.ki.pojo;

public class ChangePw {
    private String id;
    private String username; // user
    private String pswdLama;
    private String pswdBaru;

    public String getId() {
        return id;
    }

    public String getPswdLama() {
        return pswdLama;
    }

    public void setPswdLama(String pswdLama) {
        this.pswdLama = pswdLama;
    }

    public String getPswdBaru() {
        return pswdBaru;
    }

    public void setPswdBaru(String pswdBaru) {
        this.pswdBaru = pswdBaru;
    }

    public String getUsername() {
        return username;
    }
}
