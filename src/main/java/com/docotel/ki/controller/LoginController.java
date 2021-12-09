package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.component.LireIndexing;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.pojo.ChangePw;
import com.docotel.ki.service.EmailService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.WebImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

@Controller
public class LoginController extends BaseController {
    public static final String PATH_LOGIN = "/login";
    public static final String PATH_LOGOUT = "/logout";
    public static final String PATH_LOGOUT_SUCCESS = PATH_LOGIN + "?logout=ok";
    public static final String PATH_SEND_EMAIL_SUCCESS = PATH_LOGIN + "?email=ok";
    public static final String PATH_CHANGE_PASS_SUCCESS = PATH_LOGIN + "?pass=ok";
    public static final String PATH_CHANGE_PASSWORD = "/change-password";
    public static final String PATH_RESET_PASSWORD = "/reset-password";
    public static final String PATH_VERIFY_EMAIL = "/verify-email";
    public static final String PATH_FORGOT_PASSWORD = "/lupa-password";
    public static final String PATH_NEW_PASSWORD = "/new-password";
    public static final String REDIRECT_LOGIN = "redirect:" + PATH_LOGIN;
    public static final String REDIRECT_LOGOUT_SUCCESS = "redirect:" + PATH_LOGOUT_SUCCESS;
    public static final String REDIRECT_SEND_EMAIL_SUCCESS = "redirect:" + PATH_SEND_EMAIL_SUCCESS;
    public static final String REDIRECT_CHANGE_PASSWORD_SUCCESS = "redirect:" + PATH_CHANGE_PASS_SUCCESS;
    public static final String REQUEST_MAPPING_LOGIN = PATH_LOGIN + "*";
    public static final String REQUEST_MAPPING_LOGOUT = PATH_LOGOUT + "*";
    public static final String REQUEST_MAPPING_NEW_PASSWORD = PATH_NEW_PASSWORD + "*";
    public static final String REQUEST_MAPPING_CHANGE_PASSWORD = BaseController.PATH_AFTER_LOGIN + PATH_CHANGE_PASSWORD + "*";
    public static final String REQUEST_MAPPING_RESET_PASSWORD = BaseController.PATH_AFTER_LOGIN + PATH_RESET_PASSWORD + "*";
    private static final String TEMPLATE_EMAIL_AKTIFASI = "eTemplateForgotPass";
    private static final String LOGIN_ERROR_ACCOUNT_DISABLED = "acc-dis";
    private static final String LOGIN_ERROR_ACCOUNT_EXPIRED = "acc-exp";
    private static final String LOGIN_ERROR_ACCOUNT_INVALID = "invalid";
    private static final String LOGIN_ERROR_ACCOUNT_LOCKED = "acc-locked";
    private static final String LOGIN_ERROR_CREDENTIAL_EXPIRED = "cred-exp";
    private static final String LOGIN_ERROR_NO_AUTHORITY = "no-auth";
    private static final String LOGIN_ERROR_REPS_DEAD = "reps-dead";
    private static final String LOGIN_ERROR_SESSION_EXPIRED = "ses-exp";
    private static final String LOGIN_ERROR_WEEKEND = "week-end";
    private static final String PAGE_LOGIN = "login";
    private static final String PATH_LOGIN_ERROR = PATH_LOGIN + "?error=";
    public static final String PATH_LOGIN_ERROR_ACCOUNT_DISABLED = PATH_LOGIN_ERROR + LOGIN_ERROR_ACCOUNT_DISABLED;
    public static final String PATH_LOGIN_ERROR_ACCOUNT_EXPIRED = PATH_LOGIN_ERROR + LOGIN_ERROR_ACCOUNT_EXPIRED;
    public static final String PATH_LOGIN_ERROR_ACCOUNT_INVALID = PATH_LOGIN_ERROR + LOGIN_ERROR_ACCOUNT_INVALID;
    public static final String PATH_LOGIN_ERROR_ACCOUNT_LOCKED = PATH_LOGIN_ERROR + LOGIN_ERROR_ACCOUNT_LOCKED;
    public static final String PATH_LOGIN_ERROR_CREDENTIAL_EXPIRED = PATH_LOGIN_ERROR + LOGIN_ERROR_CREDENTIAL_EXPIRED;
    public static final String PATH_LOGIN_ERROR_NO_AUTHORITY = PATH_LOGIN_ERROR + LOGIN_ERROR_NO_AUTHORITY;
    public static final String PATH_LOGIN_ERROR_REPS_DEAD = PATH_LOGIN_ERROR + LOGIN_ERROR_REPS_DEAD;
    public static final String PATH_LOGIN_ERROR_SESSION_EXPIRED = PATH_LOGIN_ERROR + LOGIN_ERROR_SESSION_EXPIRED;
    public static final String PATH_LOGIN_ERROR_WEEKEND = PATH_LOGIN_ERROR + LOGIN_ERROR_WEEKEND;
    public static final String REDIRECT_LOGIN_NO_AUTHORITY = "redirect:" + PATH_LOGIN_ERROR_NO_AUTHORITY;
    public static final String REDIRECT_LOGIN_REPS_DEAD = "redirect:" + PATH_LOGIN_ERROR_REPS_DEAD;
    private static final String PAGE_CHANGE_PASSWORD = "user-management/change-password";
    private static final String PAGE_RESET_PASSWORD = "user-management/reset-password";
    private static final String PAGE_FORGOT_PASSWORD = "user-management/forgot-password";
    private static final String PAGE_NEW_PASSWORD = "user-management/new-password";
    private static final String REDIRECT_LIST_USER = "redirect:" + PATH_AFTER_LOGIN + "/list-user";

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private LireIndexing lireIndexing;
    @Autowired
    private WebImageService webImageService;

    @Value("${version:1.0.0}")
    private String version;

    @GetMapping(value = {"/", REQUEST_MAPPING_LOGIN})
    public String doShowPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "pass", required = false) String pass,
            @RequestParam(value = "reg", required = false) String reg,
            final Model model,
            final HttpServletRequest request,
            final HttpServletResponse response) {

        if (request.getUserPrincipal() != null) {
            doInvalidateUser(request, response);

            StringBuffer sbResponse = new StringBuffer();
            try {
                if (!StringUtils.isEmpty(logout.trim())) {
                    sbResponse.append("&logout=");
                    sbResponse.append(logout);
                }
            } catch (NullPointerException e) {
            }
            if (sbResponse.length() > 0) {
                sbResponse.deleteCharAt(0);
                sbResponse.insert(0, "?");
            }
            if (error!=null && error.equalsIgnoreCase(LOGIN_ERROR_NO_AUTHORITY)) {
            	sbResponse.insert(0, REDIRECT_LOGIN_NO_AUTHORITY);
            } else if (error!=null && error.equalsIgnoreCase(LOGIN_ERROR_REPS_DEAD)){
                sbResponse.insert(0, REDIRECT_LOGIN_REPS_DEAD);
            }
            else {
            	sbResponse.insert(0, REDIRECT_LOGIN);
            }
            return sbResponse.toString();
        }

        try {
            if (!StringUtils.isEmpty(error.trim())) {
                if (error.equalsIgnoreCase(LOGIN_ERROR_ACCOUNT_DISABLED)) {
                    model.addAttribute("errorMessage", getMessage("form.error.log.in.account.disabled"));
                } else if (error.equalsIgnoreCase(LOGIN_ERROR_ACCOUNT_EXPIRED)) {
                    model.addAttribute("errorMessage", getMessage("form.error.log.in.account.expired"));
                } else if (error.equalsIgnoreCase(LOGIN_ERROR_ACCOUNT_LOCKED)) {
                    model.addAttribute("errorMessage", getMessage("form.error.log.in.account.locked"));
                } else if (error.equalsIgnoreCase(LOGIN_ERROR_CREDENTIAL_EXPIRED)) {
                    model.addAttribute("errorMessage", getMessage("form.error.log.in.credential.expired"));
                } else if (error.equalsIgnoreCase(LOGIN_ERROR_SESSION_EXPIRED)) {
                    model.addAttribute("errorMessage", getMessage("form.error.log.in.session.expired"));
                } else if (error.equalsIgnoreCase(LOGIN_ERROR_WEEKEND)) {
                    model.addAttribute("errorMessage", getMessage("form.error.log.in.weekend"));
                } else if (error.equalsIgnoreCase(LOGIN_ERROR_NO_AUTHORITY)) {
                    model.addAttribute("errorMessage", getMessage("form.error.log.in.no.authority"));
                } else if (error.equalsIgnoreCase(LOGIN_ERROR_REPS_DEAD)){
                    model.addAttribute("errorMessage", getMessage("form.error.log.in.repsdead"));

                }
                    else {
                    model.addAttribute("errorMessage", getMessage("form.error.log.in"));
                }
            }
        } catch (NullPointerException e) {
        }

        try {
            if (!StringUtils.isEmpty(logout.trim())) {
                model.addAttribute("okMessage", getMessage("form.ok.log.out"));
            }
        } catch (NullPointerException e) {
        }
        try {
            if (!StringUtils.isEmpty(email.trim())) {
                model.addAttribute("okMessage", getMessage("form.ok.send.email"));
            }
        } catch (NullPointerException e) {
        }
        try {
            if (!StringUtils.isEmpty(pass.trim())) {
                model.addAttribute("okMessage", getMessage("form.ok.change.password"));
            }
        } catch (NullPointerException e) {
        }
        try {
            if (!StringUtils.isEmpty(reg.trim())) {
                if (reg.equalsIgnoreCase("ok"))
                    model.addAttribute("okMessage", getMessage("form.ok.reg"));
                else if (reg.equalsIgnoreCase("expired"))
                    model.addAttribute("errorMessage", getMessage("form.error.reg.expired"));
                else if (reg.equalsIgnoreCase("rejected"))
                    model.addAttribute("errorMessage", getMessage("form.error.reg.rejected"));
            }
        } catch (NullPointerException e) {
        }

        model.addAttribute("version", version);
//        model.addAttribute("pathImage", webImageService.setImage("bg-login.jpg"));

        return PAGE_LOGIN;
    }

    /********************************************** - LOGOUT SECTION - **********************************************/
    @RequestMapping(value = {REQUEST_MAPPING_LOGOUT}, method = {RequestMethod.GET, RequestMethod.POST})
    public String doLogout(final HttpServletRequest request, final HttpServletResponse response) {
        doInvalidateUser(request, response);

        return REDIRECT_LOGOUT_SUCCESS;
    }

    /********************************************** - OTHER SECTION - **********************************************/
    private void doInvalidateUser(final HttpServletRequest request, final HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if ("JSESSIONID".equalsIgnoreCase(cookie.getName())) {
                cookie.setMaxAge(0);
                cookie.setValue(null);
                cookie.setDomain(request.getServerName());
                cookie.setPath(request.getServletContext().getContextPath() + "/");
                cookie.setSecure(request.isSecure());
                response.addCookie(cookie);
                break;
            }
        }

        request.getSession(true);
    }

    @RequestMapping(value = {REQUEST_MAPPING_CHANGE_PASSWORD}, method = {RequestMethod.GET})
    public String doShowPageChangePassword(Model model, final HttpServletRequest request, final HttpServletResponse response) {
        model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        model.addAttribute("pwd", new ChangePw());

        return PAGE_CHANGE_PASSWORD;
    }

    @PostMapping(value = {REQUEST_MAPPING_CHANGE_PASSWORD})
    public String doChangePassword(@ModelAttribute("pwd") ChangePw pwd, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        MUser mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean hasil = passwordEncoder.matches(pwd.getPswdLama(), mUser.getPassword());

        if (!hasil) {
            model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            model.addAttribute("pwd", new ChangePw());
            model.addAttribute("errorMessage", getMessage("form.error.change.password.not.match"));
            return PAGE_CHANGE_PASSWORD;
        } else if(pwd.getPswdBaru().length() == 0) {
            model.addAttribute("user", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            model.addAttribute("pwd", new ChangePw());
            model.addAttribute("errorMessage", getMessage("form.error.change.password.new.empty"));
            return PAGE_CHANGE_PASSWORD;
        } else {
            String passwordBaru = passwordEncoder.encode(pwd.getPswdBaru());
            mUser.setPassword(passwordBaru);
            userService.saveOrUpdate(mUser);
        }
        return REDIRECT_LOGIN;
    }

    String noo = null;
    @RequestMapping(value = {REQUEST_MAPPING_RESET_PASSWORD}, method = {RequestMethod.GET})
    public String doShowPageResetPassword(Model model, @RequestParam(value = "no", required = true) String no, final HttpServletRequest request, final HttpServletResponse response) {
        MUser mUser = userService.getUserById(no);

        model.addAttribute("user", mUser );
        model.addAttribute("pwd", new ChangePw());

        noo = no;
        return PAGE_RESET_PASSWORD;
    }

    @PostMapping(value = {REQUEST_MAPPING_RESET_PASSWORD})
    public String doResetPassword(@ModelAttribute("pwd") ChangePw pwd, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        MUser mUser = userService.getUserById(noo);

        if (pwd.getPswdBaru().length() == 0) {
            model.addAttribute("user", mUser);
            model.addAttribute("pwd", new ChangePw());
            model.addAttribute("errorMessage", getMessage("form.error.change.password.new.empty"));
            return PAGE_RESET_PASSWORD;
        } else {
            String passwordBaru = passwordEncoder.encode(pwd.getPswdBaru());
            mUser.setPassword(passwordBaru);
            userService.saveOrUpdate(mUser);
        }
        return REDIRECT_LIST_USER;
    }

    @RequestMapping(value = {PATH_FORGOT_PASSWORD}, method = {RequestMethod.GET})
    public String doShowPageForgotPassword() {
        return PAGE_FORGOT_PASSWORD;
    }

    @RequestMapping(value = PATH_VERIFY_EMAIL+ "*", method = {RequestMethod.POST})
    @ResponseBody
    public void doVerifyUserbyEmail(@RequestParam("email") String email, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        MUser mUser = userService.getUserByEmail(email);
        if (mUser != null) {
            writeJsonResponse(response, 200);
        }else{
            writeJsonResponse(response, 201);
        }
    }

    @PostMapping(value = {PATH_FORGOT_PASSWORD})
    public String doSendEMailForgotPassword(@RequestParam("email") String email, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        MUser mUser = userService.getUserByEmail(email);
        if (mUser != null) {
            doSendEmail(mUser, request);
            return REDIRECT_SEND_EMAIL_SUCCESS;
        } else {
            model.addAttribute("errorMessage", getMessage("form.error.get.email"));
            return PAGE_FORGOT_PASSWORD;
        }
    }

    public void doSendEmail(MUser user, HttpServletRequest request) {
        byte[] encodedBytes = Base64.getEncoder().encode(user.getEmail().getBytes());
        String param = new String(encodedBytes);
        String key = passwordEncoder.encode(user.getEmail());
        String baseUrl = null;

        if(request.getRequestURL().toString().endsWith(request.getServletPath())) {
            baseUrl = String.format("%s/new-password?no=%s&key=%s", request.getRequestURL().toString().substring(0, request.getRequestURL().length() - request.getServletPath().length()), param, key);
        } else {
            baseUrl = String.format("%s://%s:%d%snew-password?no=%s&key=%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath(), param, key);
        }

        if (!baseUrl.contains("/new-password")) {
            baseUrl = baseUrl.replace("new-password", "/new-password");
        }

        if (baseUrl.contains(":80/")) {
            baseUrl = baseUrl.replace(":80/", "/");
        }

        String to = user.getEmail();
        String subject = "DJKI-Lupa Password";

        emailService.prepareAndSend(to, subject, user.getUsername(), user.getUsername(), baseUrl, TEMPLATE_EMAIL_AKTIFASI);
    }

    @RequestMapping(value = {REQUEST_MAPPING_NEW_PASSWORD}, method = {RequestMethod.GET})
    public String doShowPageNewPassword(@RequestParam("no") String no,@RequestParam("key") String key, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        byte[] decodedBytes = Base64.getDecoder().decode(no);
        String email = new String(decodedBytes);
        if(passwordEncoder.matches(email, key)) {
            MUser mUser = userService.getUserByEmail(email);
            if (mUser != null) {
                model.addAttribute("user", mUser);
                model.addAttribute("pwd", new ChangePw());

                return PAGE_NEW_PASSWORD;
            }
        }

        return REDIRECT_LOGIN;
    }

    @PostMapping(value = {REQUEST_MAPPING_NEW_PASSWORD})
    public String doNewPassword(@RequestParam("confirmpassword") String newPass, @RequestParam("email") String email, Model model, final HttpServletRequest request, final HttpServletResponse response) {
        String passwordBaru = passwordEncoder.encode(newPass);
        MUser mUser = userService.getUserByEmail(email);
        mUser.setPassword(passwordBaru);
        userService.saveOrUpdate(mUser);

        return REDIRECT_CHANGE_PASSWORD_SUCCESS;
    }
}
