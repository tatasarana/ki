package com.docotel.ki.controller.online;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.service.master.*;
import com.docotel.ki.service.transaction.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class PembayaranOnlineController extends BaseController{

    @Autowired
    TrademarkService trademarkService;

    private static final String DIRECTORY_PAGE_ONLINE = "permohonan-online/";

    private static final String PAGE_TAMBAH_PERMOHONAN_ONLINE = DIRECTORY_PAGE_ONLINE + "tambah-permohonan-online";
    private static final String PAGE_PERMOHONAN_ONLINE = DIRECTORY_PAGE_ONLINE + "permohonan-online";
}
