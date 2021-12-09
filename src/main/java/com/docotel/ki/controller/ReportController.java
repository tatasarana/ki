package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class ReportController {
    private Logger logger = LoggerFactory.getLogger(ReportController.class);
    private static final String REQUEST_MAPPING_CETAK_SEBELUM_SUBMIT = "/cetak-sebelum-submit" + "*";

    @Autowired
    private ReportService reportService;

    @GetMapping("/letter")
    public String homecetak() {
        return "homecetak";
    }

    @PostMapping("/print")
    public void print(HttpServletResponse response) {
        try {
            String filename="reportMandarin.pdf";
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=" + filename);
            response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
            this.reportService.generateReport(response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            logger.error("Error generating report",e);
        }
    }



    @RequestMapping(value = REQUEST_MAPPING_CETAK_SEBELUM_SUBMIT, method = {RequestMethod.GET})
    @ResponseBody
    public String doCetakMerekPratinjau(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        return "";
    }



}
