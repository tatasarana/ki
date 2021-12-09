package com.docotel.ki.controller.ajax;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.transaction.TxPostReception;
import com.docotel.ki.model.transaction.TxPostReceptionDetail;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.model.transaction.TxTmOwner;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.KeyValueSelect;
import com.docotel.ki.pojo.NativeQueryModel;
import com.docotel.ki.repository.NativeQueryRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxPostReceptionDetailCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmOwnerCustomRepository;
import com.docotel.ki.repository.transaction.TxTmGeneralRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Column;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class AjaxCheckPermohonanOnlineController extends BaseController {

    @Autowired
    TxTmOwnerCustomRepository   txTmOwnerCustomRepository ;

    @Autowired
    TxTmGeneralRepository txTmGeneralRepository;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    NativeQueryRepository nativeQueryRepository ;

    @Autowired
    TxPostReceptionDetailCustomRepository txPostReceptionDetailCustomRepository ;

    @Autowired
    TxPostReceptionCustomRepository txPostReceptionCustomRepository ;

    public static final String REQUEST_MAPPING_AJAX = "ajax-check" ;
    public static final String REQUEST_MAPPING_AJAX_PASCA_PENGALIHAN =  REQUEST_MAPPING_AJAX + "/pasca-pengalihan" + "*" ;

    @RequestMapping(value = REQUEST_MAPPING_AJAX_PASCA_PENGALIHAN, method = {RequestMethod.POST})
    public void doRequestMappingAjaxPascaPengalihan(final HttpServletRequest request, final HttpServletResponse response) throws IOException
    {
        String application_no = request.getParameter("noIPT");
//        System.out.println(application_no);

        TxPostReceptionDetail txPostReceptionDetail = txPostReceptionDetailCustomRepository.selectOneByAppId(application_no);
        String apt = txPostReceptionDetail.getTxTmGeneral().getId();

        List<KeyValueSelect> searchCriteria = new ArrayList<>();

        searchCriteria.add(new KeyValueSelect("TM_OWNER_ID","KANDIDAT-"+apt,"=",true,null));
        String[] targetcolumn = {"tm_owner_name"} ;
        GenericSearchWrapper<Object[]>  Result = new GenericSearchWrapper<Object[]>();

        String tablename = "TX_TM_OWNER" ;
        try {
            Result = nativeQueryRepository.selectNative("GET",tablename,targetcolumn,searchCriteria,null,null,null,null);
        }
        catch (Exception e){
            e.printStackTrace();
        }

       long sum = Result.getList().size();

       if (sum == 0){
           response.getWriter().print("failed");
       }
       else
           response.getWriter().print("success");

    }



    @GetMapping(value = "/selectnative")
    @ResponseBody
    public GenericSearchWrapper<Object[]> doSelectNative2() {

        NativeQueryModel querymodel = new NativeQueryModel() ;

        // mencari nama table nya
        TxTmOwner txTmOwner = new TxTmOwner();
        String table_name = txTmOwner.getClass().getAnnotation(Table.class).name().toUpperCase();
        querymodel.setTable_name(table_name);

        // mencari Id (primary key) nya
        Object id = entityManagerFactory.getPersistenceUnitUtil().getIdentifier(txTmOwner);
        String Id = id.toString().toLowerCase();

        for (Field field : txTmOwner.getClass().getFields()) {
                // But the field doesn't have annotation @Column specified
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name();
        }


        // target column
        String[] resultcol = {Id,"tm_owner_name","tm_owner_email"}; ;
        querymodel.setResultcol(resultcol);

        // searchby
        ArrayList<KeyValueSelect> searchBy = new ArrayList <>() ;
        searchBy.add(new KeyValueSelect("APPLICATION_ID","IPT2019000356","=",true,null));
        querymodel.setSearchBy(searchBy);

        GenericSearchWrapper<Object[]>  Result = nativeQueryRepository.selectNative2(querymodel);



        return Result;
    }

    @GetMapping(value = "/updatenative2")
    @ResponseBody
    public int doUpdateNative() {

        NativeQueryModel querymodel = new NativeQueryModel() ;
        querymodel.setTable_name("TX_TM_OWNER");

        //updateQ
        ArrayList<KeyValue> updateq = new ArrayList <>();
                KeyValue updateq1 = new KeyValue();
                updateq1.setKey("TM_OWNER_ID");
                updateq1.setValue(UUID.randomUUID().toString());
            updateq.add(updateq1);


        querymodel.setUpdateQ(updateq);

        // searchby
        ArrayList<KeyValueSelect> searchBy = new ArrayList <>() ;
        searchBy.add(new KeyValueSelect("APPLICATION_ID","IPT2019000356","=", true,null));
        searchBy.add(new KeyValueSelect("TM_OWNER_ID","KANDIDAT-IPT2019000356","=",true,null));

        querymodel.setSearchBy(searchBy);

        int exe = nativeQueryRepository.updateNavite(querymodel);

        return exe;
    }

    @GetMapping("/testselect1")
    @ResponseBody
    private String doEditPengalihanHak(){
        String result ="" ;
        String application_id = "IPT2019000356" ;
        try{
            TxTmOwner txTmOwner = txTmOwnerCustomRepository.selectOne("txTmGeneral.id",application_id);
            result = txTmOwner.getAddress();
        }
        catch (NullPointerException n){
            result = "Null Pointer" ;
        }
        catch (Exception e){
            result = "Query Gagal" ;
        }


        return result ;
    }

    @GetMapping("/testquery2")
    @ResponseBody
    private int doEditPengalihanHak2(){
        int result ;
        // diambil dari E-Filling No alias /*[[${dataGeneral.id}]]*/'',
        String postId ="IPT2019000504" ;

        TxPostReception txP = txPostReceptionCustomRepository.selectOne("eFilingNo",postId);
        TxPostReceptionDetail txD = txPostReceptionDetailCustomRepository.selectOne("txPostReception.id",txP.getId());
        TxTmGeneral txG = txD.getTxTmGeneral();
        String  application_id =  txG.getId();

        try{
            NativeQueryModel queryModel = new NativeQueryModel() ;
            queryModel.setTable_name("TX_TM_OWNER");
            ArrayList<KeyValue> updateQ = new ArrayList <>();
            KeyValue updateq1 = new KeyValue() ;
            updateq1.setKey("TM_OWNER_STATUS"); updateq1.setValue("0");
            updateQ.add(updateq1);
            queryModel.setUpdateQ(updateQ);
            ArrayList<KeyValueSelect> searchBy = new ArrayList <>();
            searchBy.add(new KeyValueSelect("APPLICATION_ID",application_id,"=", true,null));
            queryModel.setSearchBy(searchBy);
            nativeQueryRepository.updateNavite(queryModel);

            NativeQueryModel queryModela = new NativeQueryModel() ;
            queryModela.setTable_name("TX_TM_OWNER");
            ArrayList<KeyValue> updateQa = new ArrayList <>();
            KeyValue updateq1a = new KeyValue() ;
            updateq1a.setKey("TM_OWNER_STATUS"); updateq1a.setValue("1");
            KeyValue updateq2a = new KeyValue() ;
            updateq2a.setKey("TM_OWNER_ID"); updateq2a.setValue(UUID.randomUUID().toString());
            updateQa.add(updateq1a);
            updateQa.add(updateq2a);
            queryModela.setUpdateQ(updateQa);
            ArrayList<KeyValueSelect> searchBya = new ArrayList <>();
            searchBya.add(new KeyValueSelect("TM_OWNER_ID","KANDIDAT-"+application_id,"=", true,null));
            queryModela.setSearchBy(searchBya);
            nativeQueryRepository.updateNavite(queryModela);

            result = 1;
        }
        catch (NullPointerException n){
            result = 0 ;
        }
        catch (Exception e){
            result = 0 ;
        }

        return result ;
    }


    @GetMapping("/testubah2")
    @ResponseBody
    private int dotestUbah() {
        int result ;
        // diambil dari E-Filling No alias /*[[${dataGeneral.id}]]*/'',
        String application_id = "IPT2019000356";

        try{
            NativeQueryModel queryModela = new NativeQueryModel() ;
            queryModela.setTable_name("TX_TM_OWNER");
            ArrayList<KeyValue> updateQa = new ArrayList <>();
            KeyValue updateq1a = new KeyValue() ;
            updateq1a.setKey("TM_OWNER_STATUS"); updateq1a.setValue("1");
            KeyValue updateq2a = new KeyValue() ;
            updateq2a.setKey("TM_OWNER_ID"); updateq2a.setValue(UUID.randomUUID().toString());
            updateQa.add(updateq1a);
            updateQa.add(updateq2a);
            queryModela.setUpdateQ(updateQa);
            ArrayList<KeyValueSelect> searchBya = new ArrayList <>();
            searchBya.add(new KeyValueSelect("TM_OWNER_ID","KANDIDAT-"+application_id,"=", true,null));
            queryModela.setSearchBy(searchBya);
            nativeQueryRepository.updateNavite(queryModela);

            result = 1;
        }
        catch (NullPointerException n){
            result = 0 ;
        }
        catch (Exception e){
            result = 0 ;
        }

        return result ;
    }


}