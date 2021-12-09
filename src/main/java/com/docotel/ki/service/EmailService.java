package com.docotel.ki.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import java.io.File;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String systemMail;

    private JavaMailSender javaMailSender;
    private MailContentBuilder mailContentBuilder;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, MailContentBuilder mailContentBuilder) {
        this.javaMailSender = javaMailSender;
        this.mailContentBuilder = mailContentBuilder;
    }

    public void prepareAndSend(String recipient, String subject, String name, String username, String url, String mailTemplate) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);

            //messageHelper.addAttachment("logo.png", new ClassPathResource("static/img/logo.png"));

            messageHelper.setFrom(new InternetAddress("permohonan.online@dgip.go.id", "DJKI - KEMENKUMHAM"));
            messageHelper.setTo(recipient);
            messageHelper.setReplyTo("permohonan.online@dgip.go.id", "DJKI - KEMENKUMHAM");
            messageHelper.setSubject(subject);
            String content = mailContentBuilder.build(name, username, url, mailTemplate);
            messageHelper.setText(content, true);

            //messageHelper.addInline("logo", new ClassPathResource(""), );
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
        }
    }

    public void prepareAndSend(String recipient, String subject, String name, String username, String password, String url, String logo, String mailTemplate) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

            messageHelper.setFrom(new InternetAddress("permohonan.online@dgip.go.id", "DJKI - KEMENKUMHAM"));
            messageHelper.setTo(recipient);
            messageHelper.setReplyTo("permohonan.online@dgip.go.id", "DJKI - KEMENKUMHAM");
            messageHelper.setSubject(subject);
            String content = mailContentBuilder.build(name, username, password, url, mailTemplate);
            messageHelper.setText(content, true);
            //messageHelper.addInline("logo", new ClassPathResource(logo), "image/png");
            FileSystemResource res = new FileSystemResource(new File(logo));
            messageHelper.addInline("logo", res);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
        }
    }

    public void prepareAndSendForTambahLampiran(String recipient, String subject, String nm, String filenm, String appId, String doctype, String brand, String tglupload, String logo, String mailTemplate) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

            messageHelper.setFrom(new InternetAddress("permohonan.online@dgip.go.id", "DJKI - KEMENKUMHAM"));
            messageHelper.setTo(recipient);
            messageHelper.setReplyTo("permohonan.online@dgip.go.id", "DJKI - KEMENKUMHAM");
            messageHelper.setSubject(subject);
            String content = mailContentBuilder.build(nm, appId, filenm, doctype, brand, tglupload, mailTemplate);
            messageHelper.setText(content, true);
            //messageHelper.addInline("logo", new ClassPathResource(logo), "image/png");
            FileSystemResource res = new FileSystemResource(new File(logo));
            messageHelper.addInline("logo", res);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
        }
    }
    
    public void kirimEmailWithAttacment(String recipient, String subject, String nm, String appId, String brand, String logo, File filePdf, String pdfName, String mailTemplate) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

            messageHelper.setFrom(new InternetAddress("permohonan.online@dgip.go.id", "DJKI - KEMENKUMHAM"));
            messageHelper.setTo(recipient);
            messageHelper.setReplyTo("permohonan.online@dgip.go.id", "DJKI - KEMENKUMHAM");
            messageHelper.setSubject(subject);
            String content = mailContentBuilder.buildWithAttacment(nm, appId, brand, mailTemplate);
            messageHelper.setText(content, true);
            FileSystemResource res = new FileSystemResource(new File(logo));
            messageHelper.addInline("logo", res);
            messageHelper.addAttachment(pdfName, filePdf);
        };
        try {
            javaMailSender.send(messagePreparator);
        } catch (MailException e) {
            // runtime exception; compiler will not force you to handle it
        }
    }
}
