package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class HttpErrorController extends BaseController {
	private static final String PAGE_ACCESS_DENIED = "access-denied";
	public static final String PATH_ACCESS_DENIED = "/access-denied";
	private static final String REQUEST_MAPPING_ACCESS_DENIED = PATH_ACCESS_DENIED + "*";

	@GetMapping(value = REQUEST_MAPPING_ACCESS_DENIED)
	public String doShowAccessDeniedPage(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		model.addAttribute("url", request.getAttribute("url"));
		return PAGE_ACCESS_DENIED;
	}
}
