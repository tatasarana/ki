package com.docotel.ki;

import com.docotel.ki.component.LireIndexing;
import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.service.master.CountryService;
import com.docotel.ki.service.master.MenuService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.UriUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
@EnableAutoConfiguration
@EnableScheduling
@EnableCaching
public class KiApplication extends SpringBootServletInitializer implements WebApplicationInitializer, ServletContextListener {
    public static MCountry INDONESIA;
    private static ApplicationContext applicationContext;
    @Autowired
    private CountryService countryService;
    @Autowired
    private MenuService menuService;
    @Value("${upload.file.brand.path:}")
    private String uploadFileBrandPath;
    @Value("${upload.file.path:}")
    private String uploadFilePath;
    @Value("${download.file.path:}")
    private String downloadFilePath;
    @Value("${download.output.certificate.file.path:}")
    private String downloadOutputCertificatePath;
    @Value("${upload.file.doc.application.path:}")
    private String uploadFileDocApplicationPath;
    @Value("${upload.file.doc.pasca.path:}")
    private String uploadFileDocPascaPath;
    @Value("${upload.file.doc.regaccount.path:}")
    private String uploadFileDocRegaccountPath;
    @Value("${upload.file.doc.multiupload.path:}")
    private String uploadFileDocMultiuploadPath;
    @Value("${upload.file.letters.masterdoc.path}")
    private String uploadFileLettersMasterDocPath;
    @Value("${upload.file.letters.uploaddoc.path}")
    private String uploadFileLettersUploaddocPath;

    @Value("${upload.file.path.signature}")
    private String uploadFileSignature;
    @Value("${upload.file.web.image}")
    private String uploadFileWebImage;
    @Value("${upload.file.web.doc}")
    private String uploadFileWebDoc;
    @Value("${certificate.file}")
    private String certificateP12;

    @Value("${user.session.timeout:-1}")
    private String userSessionTimeout;
    @Autowired
    private LireIndexing lireIndexing;
//    @Autowired
//    private SchedulerFactoryBean schedulerFactoryBean;

    public static void main(String[] args) {
        SpringApplication.run(KiApplication.class, args);
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(KiApplication.class);
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        try {
            servletContext.setAttribute("userSessionTimeout", Integer.parseInt(userSessionTimeout) * 1000);
        } catch (NumberFormatException e) {
            servletContext.setAttribute("userSessionTimeout", -1);
        }

        this.applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);

        INDONESIA = countryService.findByCode("ID");

        List<String> protectedUrlList = menuService.findAllMenuDetailUrl();

        servletContext.setAttribute("protectedUrlList", protectedUrlList);

        try {
            if (Files.notExists(Paths.get(uploadFileDocApplicationPath))) {
                Files.createDirectories(Paths.get(uploadFileDocApplicationPath));
            }
            if (Files.notExists(Paths.get(uploadFileDocPascaPath))) {
                Files.createDirectories(Paths.get(uploadFileDocPascaPath));
            }
            if (Files.notExists(Paths.get(uploadFileDocRegaccountPath))) {
                Files.createDirectories(Paths.get(uploadFileDocRegaccountPath));
            }
            if (Files.notExists(Paths.get(uploadFileDocMultiuploadPath))) {
                Files.createDirectories(Paths.get(uploadFileDocMultiuploadPath));
            }
            if (Files.notExists(Paths.get(uploadFilePath))) {
                Files.createDirectories(Paths.get(uploadFilePath));
            }
            if (Files.notExists(Paths.get(downloadFilePath))) {
                Files.createDirectories(Paths.get(downloadFilePath));
            }
            if (Files.notExists(Paths.get(downloadOutputCertificatePath))) {
                Files.createDirectories(Paths.get(downloadOutputCertificatePath));
            }
            if (Files.notExists(Paths.get(uploadFileLettersMasterDocPath))) {
                Files.createDirectories(Paths.get(uploadFileLettersMasterDocPath));
            }
            if (Files.notExists(Paths.get(uploadFileLettersUploaddocPath))) {
                Files.createDirectories(Paths.get(uploadFileLettersUploaddocPath));
            }
            if (Files.notExists(Paths.get(uploadFileSignature))) {
                Files.createDirectories(Paths.get(uploadFileSignature));
            }
            if (Files.notExists(Paths.get(certificateP12))) {
                Files.createDirectories(Paths.get(certificateP12));
                String source = getClass().getClassLoader().getResource("static/certificate-p12").getPath();
//                source = UriUtils.encodePath(source, "UTF-8");
                source = UriUtils.decode(source, "UTF-8");
                File srcDir = new File(source);

                String destination = certificateP12;
//                destination = UriUtils.encodePath(destination, "UTF-8");
                destination = UriUtils.decode(destination, "UTF-8");
                File destDir = new File(destination);

                try {
                    //copy source to target using Files Class
                    FileUtils.copyDirectory(srcDir, destDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (Files.notExists(Paths.get(uploadFileWebImage))) {
                Files.createDirectories(Paths.get(uploadFileWebImage));
                String source = getClass().getClassLoader().getResource("static/img").getPath();
//                source = UriUtils.encodePath(source, "UTF-8");
                source = UriUtils.decode(source, "UTF-8");

                File srcDir = new File(source);

                String destination = uploadFileWebImage;
//                destination = UriUtils.encodePath(destination, "UTF-8");
                destination = UriUtils.decode(destination,"UTF-8");
                File destDir = new File(destination);

                try {
                    //copy source to target using Files Class
                    FileUtils.copyDirectory(srcDir, destDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (Files.notExists(Paths.get(uploadFileWebDoc))) {
                Files.createDirectories(Paths.get(uploadFileWebDoc));
                String source = getClass().getClassLoader().getResource("static/doc").getPath();
//                source = UriUtils.encodePath(source, "UTF-8");
                source = UriUtils.decode(source, "UTF-8");
                File srcDir = new File(source);

                String destination = uploadFileWebDoc;
//                destination = UriUtils.encodePath(destination, "UTF-8");
                destination = UriUtils.decode(destination, "UTF-8");
                File destDir = new File(destination);

                try {
                //copy source to target using Files Class
                    FileUtils.copyDirectory(srcDir, destDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//         lireIndexing.startIndexing(uploadFileBrandPath);

//        try {
//            schedulerFactoryBean.getScheduler().start();
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
//        try {
//            schedulerFactoryBean.getScheduler().shutdown();
//        } catch (SchedulerException e) {
//            e.printStackTrace();
//        }
    }

   /* @Bean
    public ErrorPageFilter errorPageFilter() {
        return new ErrorPageFilter();
    }

    @Bean
    public FilterRegistrationBean disableSpringBootErrorFilter(ErrorPageFilter filter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setEnabled(false);
        return filterRegistrationBean;
    }*/
}
