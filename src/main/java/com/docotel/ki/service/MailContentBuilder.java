package com.docotel.ki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailContentBuilder {
    private TemplateEngine templateEngine;

    @Autowired
    public MailContentBuilder(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String build(String name, String username, String url, String mailTemplate) {

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("username", username);
        context.setVariable("url", url);
        //context.setVariable("logo", "logo");
        return templateEngine.process(mailTemplate, context);
    }
    
    public String build(String name, String username, String password, String url, String mailTemplate) {

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("username", username);
        context.setVariable("password", password);
        context.setVariable("url", url);
        context.setVariable("logo", "logo");
        return templateEngine.process(mailTemplate, context);
    }

    public String build(String nm, String appId, String filenm, String doctype, String brand, String tglupload, String mailTemplate) {
        Context context = new Context();
        context.setVariable("filenm", filenm);
        context.setVariable("nm", nm);
        context.setVariable("appId", appId);
        context.setVariable("doctype", doctype);
        context.setVariable("brand", brand);
        context.setVariable("tglupload", tglupload);
        context.setVariable("logo", "logo");
        return templateEngine.process(mailTemplate, context);
    }
    
    public String buildWithAttacment(String nm, String appId, String brand, String mailTemplate) {
        Context context = new Context();
        context.setVariable("nm", nm);
        context.setVariable("appId", appId);
        context.setVariable("brand", brand);
        context.setVariable("note", "Catatan : Email ini tidak dapat dibalas / no reply.");
        context.setVariable("logo", "logo");
        return templateEngine.process(mailTemplate, context);
    }
}
