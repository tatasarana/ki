package com.docotel.ki.controller.onlineadmin;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.DataTablesSearchResult;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.EmailService;
import com.docotel.ki.service.master.LookupService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.RegistrasiOnlineService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.enumeration.RegistrasiStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Controller
public class RegistrasiOnlineAdminController extends BaseController {
    private static final String DIRECTORY_PAGE = "reg-online/";
    private static final String TEMPLATE_EMAIL_REGISTRASI_APPROVE = "eTemplateRegistrasiApprove";
    private static final String TEMPLATE_EMAIL_REGISTRASI_REJECT = "eTemplateRegistrasiReject";
    private static final String TEMPLATE_EMAIL_REGISTRASI_VERIFY = "eTemplateRegistrasiVerify";
    private static final String SUBJECT_EMAIL_REGISTRASI_APPROVE = "DJKI - Akun Aplikasi Berhasil Didaftarkan";
    private static final String SUBJECT_EMAIL_REGISTRASI_REJECT = "DJKI - Akun Aplikasi Gagal Didaftarkan";
    private static final String SUBJECT_EMAIL_REGISTRASI_VERIFY = "DJKI - Verifikasi Akun Aplikasi";
    private static final String PAGE_REGISTRASI_LIST = DIRECTORY_PAGE + "registrasi-online-list";
    private static final String PAGE_REGISTRASI_DETAIL = DIRECTORY_PAGE + "registrasi-online-detail";
    private static final String PATH_AJAX_SEARCH_LIST = "/cari-daftar-online";
    private static final String PATH_REGISTRASI_LIST = "/list-daftar-online";
    private static final String PATH_REGISTRASI_DETAIL = "/detail-daftar-online";
    private static final String PATH_REGISTRASI_APPROVE = "/approve-daftar-online";
    private static final String PATH_REGISTRASI_REJECT = "/reject-daftar-online";
    private static final String PATH_REGISTRASI_RESEND = "/resend-daftar-online";
    private static final String REDIRECT_REGISTRATION_LIST = "redirect:" + PATH_AFTER_LOGIN + PATH_REGISTRASI_LIST;
    private static final String REQUEST_MAPPING_AJAX_SEARCH_LIST = PATH_AFTER_LOGIN + PATH_AJAX_SEARCH_LIST + "*";
    private static final String REQUEST_MAPPING_REGISTRASI_LIST = PATH_AFTER_LOGIN + PATH_REGISTRASI_LIST + "*";
    private static final String REQUEST_MAPPING_REGISTRASI_DETAIL = PATH_AFTER_LOGIN + PATH_REGISTRASI_DETAIL + "*";
    private static final String REQUEST_MAPPING_REGISTRASI_APPROVE = PATH_AFTER_LOGIN + PATH_REGISTRASI_APPROVE + "*";
    private static final String REQUEST_MAPPING_REGISTRASI_REJECT = PATH_AFTER_LOGIN + PATH_REGISTRASI_REJECT + "*";
    private static final String REQUEST_MAPPING_REGISTRASI_RESEND = PATH_AFTER_LOGIN + PATH_REGISTRASI_RESEND + "*";
    @Autowired
    EmailService emailService;
    @Autowired
    LookupService lookupService;
    @Autowired
    RegistrasiOnlineService registrasiOnlineService;
    @Autowired
    UserService userService;
    @Value("${upload.file.doc.regaccount.path:}")
    private String uploadFileDocRegaccountPath;
    @Value("${upload.file.web.image:}")
    private String pathImage;
    @Value("${upload.file.logoemail.image:}")
    private String logoEmailImage;

    @ModelAttribute
    public void addModelAttribute(final Model model, final HttpServletRequest request) {
        model.addAttribute("menu", "usermgmnt");
        model.addAttribute("subMenu", "listRegOnline");
    }


    @GetMapping(path = REQUEST_MAPPING_REGISTRASI_LIST)
    public String showPageDaftarOnlineList(@RequestParam(value = "error", required = false) String error, final Model model) {
        //System.out.println("exclude");
        List<MLookup> mLookupJPList = lookupService.selectAllbyGroup("JenisPemohon");
        model.addAttribute("listJenisPemohon", mLookupJPList);

        if (StringUtils.isNotBlank(error)) {
            model.addAttribute("errorMessage", error);
        }

        return PAGE_REGISTRASI_LIST;
    }

    @GetMapping(path = REQUEST_MAPPING_REGISTRASI_DETAIL)
    public String showPageDaftarOnlineDetail(final Model model, final HttpServletRequest request) throws IOException {
        TxOnlineReg txOnlineReg = registrasiOnlineService.findOne(request.getParameter("id"));

        if (txOnlineReg != null) {
            if (txOnlineReg.getFileNameCard() != null) {
                String pathFolder = DateUtil.formatDate(txOnlineReg.getCreatedDate(), "yyyy/MM/dd/");
                File file = new File(uploadFileDocRegaccountPath + pathFolder + txOnlineReg.getId() + "_" + txOnlineReg.getFileNameCard());
                String mimetype = Files.probeContentType(file.toPath());

                if (mimetype != null) {
                    String fileCard = "data:" + mimetype + ";base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));

                    if (mimetype.split("/")[0].equals("image"))
                        model.addAttribute("imgReprsCard", fileCard);
                    else
                        model.addAttribute("fileReprsCard", fileCard);
                }
            }

            model.addAttribute("showButton", txOnlineReg.getApprovalStatus().equals(RegistrasiStatusEnum.PREPARE.name()));
            model.addAttribute("form", txOnlineReg);
            return PAGE_REGISTRASI_DETAIL;
        }

        return REDIRECT_REGISTRATION_LIST + "?error=Akun tidak ditemukan";
    }

    @RequestMapping(value = REQUEST_MAPPING_AJAX_SEARCH_LIST, method = {RequestMethod.POST})
    public void doSearchDataTablesList(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            setResponseAsJson(response);

            DataTablesSearchResult dataTablesSearchResult = new DataTablesSearchResult();
            try {
                dataTablesSearchResult.setDraw(Integer.parseInt(request.getParameter("draw")));
            } catch (NumberFormatException e) {
                dataTablesSearchResult.setDraw(0);
            }

            int offset = 0;
            int limit = 50;
            try {
                offset = Math.abs(Integer.parseInt(request.getParameter("start")));
            } catch (NumberFormatException e) {
            }
            try {
                limit = Math.abs(Integer.parseInt(request.getParameter("length")));
            } catch (NumberFormatException e) {
            }

            String[] searchByArr = request.getParameterValues("searchByArr[]");
            String[] keywordArr = request.getParameterValues("keywordArr[]");
            List<KeyValue> searchCriteria = null;

            if (searchByArr != null) {
                searchCriteria = new ArrayList<>();
                for (int i = 0; i < searchByArr.length; i++) {
                    String searchBy = searchByArr[i];
                    String value = null;
                    try {
                        value = keywordArr[i];
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    if (searchBy != null && !searchBy.equalsIgnoreCase("")) {
                        if (StringUtils.isNotBlank(value)) {
                            searchCriteria.add(new KeyValue(searchBy, value, false));
                        }
                    }
                }
            }

            String orderBy = request.getParameter("order[0][column]");
            if (orderBy != null) {
                orderBy = orderBy.trim();
                if (orderBy.equalsIgnoreCase("")) {
                    orderBy = null;
                } else {
                    switch (orderBy) {
                        case "1":
                            orderBy = "name";
                            break;
                        case "2":
                            orderBy = "email";
                            break;
//                        case "3":
//                            orderBy = "no";
//                            break;
                        case "3":
                            orderBy = "gender.name";
                            break;
                        case "4":
                            orderBy = "applicantType";
                            break;
                        case "5":
                            orderBy = "mReprs.no";
                            break;
                        case "6":
                            orderBy = "approvalStatus";
                            break;
                        case "7":
                            orderBy = "createdDate";
                            break;
                        case "8":
                            orderBy = "id";
                            break;
                        default:
                            orderBy = "createdDate";
                            break;
                    }
                }
            }

            String orderType = request.getParameter("order[0][dir]");
            if (orderType == null) {
                orderType = "ASC";
            } else {
                orderType = orderType.trim();
                if (!orderType.equalsIgnoreCase("DESC")) {
                    orderType = "ASC";
                }
            }


            List<String[]> data = new ArrayList<>();

            GenericSearchWrapper<TxOnlineReg> searchResult = registrasiOnlineService.searchOnlineReg(searchCriteria, orderBy, orderType, offset, limit);
            if (searchResult.getCount() > 0) {
                dataTablesSearchResult.setRecordsFiltered(searchResult.getCount());
                dataTablesSearchResult.setRecordsTotal(searchResult.getCount());

                int no = offset;
                for (TxOnlineReg result : searchResult.getList()) {
                    no++;
                    Date date = new Date(result.getCreatedDate().getTime());
                    String pDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date);
                    data.add(new String[]{
                            "" + no,
                            "<a href=" + getPathURLAfterLogin(PATH_REGISTRASI_DETAIL + "?id=" + result.getId()) + ">" + result.getName() + "</a>",
                            result.getEmail() == null ? "-" : result.getEmail(),
//                            result.getNoTemp(),
                            result.getGender() == null ? "-" : result.getGender().getName(),
                            result.getApplicantType() == null ? "-" : result.getApplicantType(),
                            result.getmReprs() == null ? "-" : result.getmReprs().getNo(),
                            result.getApprovalStatusLabel(),
                            pDate,
//                            result.getId() == null ? "-" : result.getId()
                    });
                }
            }

            dataTablesSearchResult.setData(data);

            writeJsonResponse(response, dataTablesSearchResult);
        } else {
            response.setStatus(HttpServletResponse.SC_FOUND);
        }
    }


    @RequestMapping(path = REQUEST_MAPPING_REGISTRASI_APPROVE, method = {RequestMethod.POST})
    public void doApproveDaftarOnline(@RequestParam(value = "id", required = false) String id,
                                      final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) throws IOException {
//            ahlicoding.com BERHENTI DI SINI !!!
        TxOnlineReg txOnlineReg = registrasiOnlineService.findOne(id);

        MUser UserExisting = userService.getUserByEmail(txOnlineReg.getEmail());
        if (UserExisting == null) {
            txOnlineReg.setApprovalStatus(RegistrasiStatusEnum.APPROVE.name());

            registrasiOnlineService.insertForLogin(txOnlineReg);

            this.doSendEmail(txOnlineReg, request, SUBJECT_EMAIL_REGISTRASI_APPROVE, TEMPLATE_EMAIL_REGISTRASI_APPROVE, null);
            writeJsonResponse(response, "SUCCESS");
        } else {
            writeJsonResponse(response, "Email sudah terdaftar sebagai User");
        }
    }

    @RequestMapping(path = REQUEST_MAPPING_REGISTRASI_REJECT, method = {RequestMethod.POST})
    public void doRejectDaftarOnline(@RequestParam(value = "id", required = false) String id,
                                     final HttpServletRequest request, final HttpServletResponse response, final HttpSession session) throws IOException {

        TxOnlineReg txOnlineReg = registrasiOnlineService.findOne(id);
        txOnlineReg.setApprovalStatus(RegistrasiStatusEnum.REJECT.name());
        registrasiOnlineService.doApproveReject(txOnlineReg);

        this.doSendEmail(txOnlineReg, request, SUBJECT_EMAIL_REGISTRASI_REJECT, TEMPLATE_EMAIL_REGISTRASI_REJECT, null);
    }

    @RequestMapping(path = REQUEST_MAPPING_REGISTRASI_RESEND, method = {RequestMethod.POST})
    public void doResendEMailVerification(@RequestParam(value = "id", required = false) String id,
    								final HttpServletRequest request, final HttpServletResponse response) throws IOException {
    	
    	TxOnlineReg txOnlineReg = registrasiOnlineService.findOne(id);
    	doSendEmailVerification(txOnlineReg, request, SUBJECT_EMAIL_REGISTRASI_VERIFY, TEMPLATE_EMAIL_REGISTRASI_VERIFY);
	}
    
    public void doSendEmailVerification(TxOnlineReg form, HttpServletRequest request, String subject, String template) throws IOException {
        byte[] encodedBytes = Base64.getEncoder().encode(form.getId().getBytes());
        String param = new String(encodedBytes);
        String baseUrl = String.format("%s://%s:%d%s/aktivasi-user?no=%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath(), param);
        if (baseUrl.contains(":80/")) {
        	baseUrl.replace(":80/", "/");
        }
        doSendEmail(form, request, subject, template, baseUrl);
    }
    
    public void doSendEmail(TxOnlineReg form, HttpServletRequest request, String subject, String template, String url) throws IOException {
    	String logo = "static/img/" + logoEmailImage;
        File file = new File(pathImage + logoEmailImage);    	
        if (file.exists()) {
            logo = pathImage + logoEmailImage;
        }
        emailService.prepareAndSend(form.getEmail(), subject, form.getName(), form.getEmail(), form.getPassword(), url, logo, template);
    }
}