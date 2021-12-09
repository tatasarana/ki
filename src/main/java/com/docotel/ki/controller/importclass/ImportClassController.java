package com.docotel.ki.controller.importclass;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.model.master.MClass;
import com.docotel.ki.model.master.MClassDetail;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxTmClass;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.service.master.ClassService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.enumeration.ClassStatusEnum;
import com.docotel.ki.enumeration.LanguageTranslatorEnum;
import com.docotel.ki.repository.custom.master.MClassDetailCustomRepository;
import com.docotel.ki.repository.custom.pojo.MigrasiClassRepository;
import com.docotel.ki.repository.custom.transaction.TxTmClassCustomRepository;
import com.docotel.ki.repository.master.MClassHeaderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class ImportClassController extends BaseController {
	@Autowired private ClassService classService;
	@Autowired private TrademarkService trademarkService;
	@Autowired private UserService userService;
	@Autowired MClassHeaderRepository mClassHeaderRepository;
	@Autowired MClassDetailCustomRepository mClassDetailCustomRepository;
	@Autowired MigrasiClassRepository migrasiClassRepository;
	@Autowired TxTmClassCustomRepository txTmClassCustomRepository;
	
    private static final String DIRECTORY_PAGE = "import-class/";    
    private static final String PAGE_IMPORTCLASS = DIRECTORY_PAGE + "import-class";    
    private static final String PATH_IMPORTCLASS = "/import-class";	 
    
    
    @RequestMapping(path = PATH_IMPORTCLASS)
    public String showPage(Model model) {
        return PAGE_IMPORTCLASS;
    }
    
    @PostMapping(path = PATH_IMPORTCLASS)
    @Transactional
    public void doProsesMappingClass(final HttpServletRequest request,  final HttpServletResponse response) throws IOException, ParseException {    	
    	String message = "";
    	try {
    		
    		message = mappingClass();
    	
    	} catch (Exception ex) {
    		logger.error(ex);
    		writeJsonResponse(response, ex);
    	}
    	    	
		writeJsonResponse(response, message);
	} 
    
    @Transactional
    public String mappingClass() throws IOException, ParseException {
    	String message = "Success";    	
    	//String createdDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd/MM/yyyy");    	
    	
    	String strclass_ = "";
    	String strUraian = "";
    	//String strAppNo = "";
    	String[] arrUraian = null;
    	int class_base_no = 0;
    	int max_char = 4000;
    	StringBuilder strb = null;
    	
    	LanguageTranslatorEnum languageTranslatorEnum = LanguageTranslatorEnum.BING;
    	
    	MUser mUser = userService.getUserByName("SYSTEM");
    	ArrayList<MClassDetail> listMClassDetail = new ArrayList<MClassDetail>();
    	ArrayList<TxTmClass> listTxTmClass = new ArrayList<TxTmClass>();
    	String tempApplicationNo ="";		
    	
    	try {
    		HashMap<String, TxTmGeneral> ListTxTmGeneral = migrasiClassRepository.selectGeneralByApplicationNo();
    		Iterator<Map.Entry<String, TxTmGeneral>> it = ListTxTmGeneral.entrySet().iterator();
    		
    		while(it.hasNext()) {
    			Map.Entry<String, TxTmGeneral> map = it.next();
    			
    			TxTmGeneral txTmGeneral = map.getValue();			    			
    			txTmClassCustomRepository.delete("txTmGeneral.id", txTmGeneral.getId());
    		}
    		
	    	List<Object[]> ObjMigrasiClass =  classService.selectObject();    		
	    	
	    	for (Object[] obj : ObjMigrasiClass) {
	    		
	    		strclass_ = obj[0].toString();	    		
	    		if(obj[1]!=null) {	    			
		    		if (obj[1] instanceof Clob) {
		    			Clob clob = (Clob) obj[1];
		    			strUraian = clob.getSubString(1, (int) clob.length());
		    		} else {	    			
		    				strUraian = obj[1].toString();	 
		    		}
	    		} else {
    				continue;
    			}
	    		
	    		tempApplicationNo = obj[2].toString();

	    		//proses filter  
	    		// pemisah / separator bisa berupa ';', '-' dan ','
	    		//ubah semua pemisah menjadi "|" 	    		
	    		//ada case dimana ada karakter ',' dan '-' dalam satu uraian.
	    		//ada case overload character (4777) class 29 :	    		
	    		List<String> classtList = Arrays.asList(strclass_.split("\\,"));	    			    		 	    		
    			strUraian = strUraian.replaceAll("\\*", "").replaceAll("\\d", "").
						replaceAll("\\.", "").replaceAll("\\)", "").
						replaceAll("\\#", "|");  		
				if(strUraian.charAt(0)=='|') {
					strUraian = strUraian.substring(1);
				}
				
				if(strUraian.charAt(strUraian.length()-1)=='|') {	    			
					strUraian = strUraian.substring(0, strUraian.length()-1);
				}
				
				if(strUraian.charAt(0)=='-') {
					strUraian = strUraian.replaceAll("\\-", "|");
				}	    		
    			
    			if(strUraian.contains("|")) {
	    			arrUraian = strUraian.split("\\|");	
	    		}else if (strUraian.contains(";")) {
	    			arrUraian = strUraian.split("\\;");	
	    		} else if (strUraian.contains(",")) {
	    			arrUraian = strUraian.split("\\,");			    		
	    		} else {
	    			arrUraian = new String[] { strUraian };
	    		}
    			
//    			String descEn = null; 
//		    	String sourceLang = "id";
//		    	String translateLang = "en";

    			class_base_no=1;		
    			for (int i = 0; i < arrUraian.length; i++) {
    				try {
	        			MClassDetail mClassDetail = new MClassDetail();
	        			mClassDetail.setClassBaseNo(classtList.get(0).concat(String.valueOf(class_base_no)));	        			
	        			
	        			if(arrUraian[i].length() > max_char) {
	        				strb = new StringBuilder(arrUraian[i].toString());
	        				strb.setLength(max_char); 
	        				mClassDetail.setDesc(strb.toString().replaceAll("[^\\x00-\\x7F]", "").trim());	  //^\\x00-\\x7F -> Contains ASCII only      			
	        			} else {
	        				strUraian = arrUraian[i].replaceAll("[^\\x00-\\x7F]", "").trim(); //^\\x00-\\x7F -> Contains ASCII only  
	        				mClassDetail.setDesc(strUraian); 
	        			}        			
        			
        				//descEn = languageTranslatorEnum.getTranslation(sourceLang, translateLang, strUraian);
        				//logger.info(strUraian + ";"+ descEn);
	        			//logger.info(strUraian);	        				        			
	        			
	        			mClassDetail.setCreatedBy(mUser);
	        			mClassDetail.setStatusFlag(false); 
	        			MClass mClass = mClassHeaderRepository.findFirstByNo(Integer.parseInt(classtList.get(0))); //classService.findFirstByNo( Integer.parseInt(classtList.get(0)));
	        			mClassDetail.setParentClass(mClass);
	        			mClassDetail.setDescEn(null);
	        			
	        			if("M".equalsIgnoreCase(tempApplicationNo.substring(1, 1) )) {
	        				mClassDetail.setDescEn(strUraian);
	        				mClassDetail.setDesc(null);	        				
	        			}
	        			
	        			//List<MClassDetail> mcdexistList = mClassDetailCustomRepository.selectAll("LEFT JOIN FETCH c.parentClass ci ", "desc",arrUraian[i], true, null, null);         												
	        			//if(mcdexistList == null) {  
	        			listMClassDetail.add(mClassDetail);	        			
	        			//}	        			
        			
        			     				
    					TxTmClass txTmClass = new TxTmClass();
        				txTmClass.setTxTmGeneral(ListTxTmGeneral.get(tempApplicationNo));
        				txTmClass.setmClass(mClass);
        				txTmClass.setmClassDetail(mClassDetail);		        				
        				txTmClass.setCreatedBy(mUser);	   	        				
        				Timestamp createdDate2 = new Timestamp(System.currentTimeMillis());		        				
        				txTmClass.setCreatedDate(createdDate2);	        				     				
        				txTmClass.setTransactionStatus(ClassStatusEnum.ACCEPT.name());	
        				txTmClass.setCorrectionFlag(false);
        				txTmClass.setEdition(mClass.getEdition());
        				txTmClass.setVersion(mClass.getVersion());        				
        				listTxTmClass.add(txTmClass);
        				class_base_no++;
        				
        				
        			} catch (Exception e) {	        				
//        				System.out.println(e.getMessage());
        			}
        		}	    				    		
	    	}
	    	classService.processedMigrasiClass(listMClassDetail, listTxTmClass);	    	
    	
    	} catch (Exception ex) {
    		logger.error(ex);
    	}
    	
    	return message;
    }
    
    
    
    /* obsolete
    public void mappingClass() {
    	String message = "Success";    	
    	String createdDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "dd/MM/yyyy");
    	
    	
    	String strclass_ = "";
    	String strUraian = "";
    	String strAppNo = "";
    	String[] arrUraian = null;
    	int class_base_no = 0;
    	int max_char = 4000;
    	StringBuilder strb = null;
    	
    	LanguageTranslatorEnum languageTranslatorEnum = LanguageTranslatorEnum.BING;
    	
    	MUser mUser = userService.getUserByName("SYSTEM");
    	MigrasiClass migrasiClass = null;    
    	ArrayList<MClassDetail> listMClassDetail = new ArrayList<MClassDetail>();
    	ArrayList<TxTmClass> listTxTmClass = new ArrayList<TxTmClass>(); 
    	HashMap<String, String> hListModMigrasi = new HashMap<String, String>();
    			
    	try {    		    	
	    	List<Object[]> ObjMigrasiClass =  classService.selectObject();    		
	    	for (Object[] obj : ObjMigrasiClass) {
	    		
	    		strclass_ = obj[0].toString();
	    			    		
	    		
	    		if(obj[1]!=null) {	    			
		    		if (obj[1] instanceof Clob) {
		    			Clob clob = (Clob) obj[1];
		    			strUraian = clob.getSubString(1, (int) clob.length());
		    		} else {	    			
		    				strUraian = obj[1].toString();	 
		    		}
	    		} else {
    				continue;
    			}
	    		
	    		strAppNo = obj[2].toString();
	    		
	    		TxTmGeneral txTmGeneral = new TxTmGeneral();;
    			if(!strAppNo.isEmpty()) {
    				txTmGeneral = trademarkService.selectOneImportClass(strAppNo);
    			}
    			if(txTmGeneral!=null) {
    				classService.deleteTxTMClassByValueN("txTmGeneral.id", txTmGeneral.getId());
    			}
    			
	    		
	    		//proses filter  
	    		// pemisah / separator bisa berupa ';', '-' dan ','
	    		//ubah semua pemisah menjadi "|" 	    		
	    		//ada case dimana ada karakter ',' dan '-' dalam satu uraian.
	    		//ada case overload character (4777) class 29 :	    		
	    		List<String> classtList = Arrays.asList(strclass_.split("\\,"));	    			    		 
	    		if(classtList.size()==1 ) {
	    			strUraian = strUraian.replaceAll("\\*", "").replaceAll("\\d", "").
							replaceAll("\\.", "").replaceAll("\\)", "").
							replaceAll("\\#", "|");  		
					if(strUraian.charAt(0)=='|') {
						strUraian = strUraian.substring(1);
					}
					
					if(strUraian.charAt(strUraian.length()-1)=='|') {	    			
						strUraian = strUraian.substring(0, strUraian.length()-1);
					}
					
					if(strUraian.charAt(0)=='-') {
						strUraian = strUraian.replaceAll("\\-", "|");
					}	    		
	    			
	    			if(strUraian.contains("|")) {
		    			arrUraian = strUraian.split("\\|");	
		    		}else if (strUraian.contains(";")) {
		    			arrUraian = strUraian.split("\\;");	
		    		} else if (strUraian.contains(",")) {
		    			arrUraian = strUraian.split("\\,");			    		
		    		} else {
		    			arrUraian = new String[] { strUraian };
		    		}
	    			
	    			String descEn = null; 
			    	String sourceLang = "id";
			    	String translateLang = "en";

	    			class_base_no=1;		
	    			for (int i = 0; i < arrUraian.length; i++) {
	    				
	        			MClassDetail mClassDetail = new MClassDetail();
	        			mClassDetail.setClassBaseNo(classtList.get(0).concat(String.valueOf(class_base_no)));	        			
	        			
	        			if(arrUraian[i].length() > max_char) {
	        				strb = new StringBuilder(arrUraian[i].toString());
	        				strb.setLength(max_char); 
	        				mClassDetail.setDesc(strb.toString().replaceAll("[^\\x00-\\x7F]", "").trim());	        			
	        			} else {
	        				strUraian = arrUraian[i].replaceAll("[^\\x00-\\x7F]", "").trim();
	        				mClassDetail.setDesc(strUraian); 
	        			}
	        			
	        			try {
	        				descEn = languageTranslatorEnum.getTranslation(sourceLang, translateLang, strUraian);
	        				logger.info(descEn);
	        			} catch (NullPointerException e) {
	        				System.out.println(e.getMessage());
	        			}	        			
	        			
	        			mClassDetail.setCreatedBy(mUser);
	        			mClassDetail.setStatusFlag(false); 
	        			//mcd.setParentClass(classService.findFirstByNo( String.valueOf(classtList.get(0))) );
	        			MClass mClass = mClassHeaderRepository.findFirstByNo(Integer.parseInt(classtList.get(0))); //classService.findFirstByNo( Integer.parseInt(classtList.get(0)));
	        			mClassDetail.setParentClass(mClass);
	        			mClassDetail.setDescEn(descEn);
	        			List<MClassDetail> mcdexistList = classService.selectAllClassDetail("desc",arrUraian[i] ); //classService.findOneBydescMClassDetail(arrUraian[i]); 	        				        				        			         			
	        			
	        			if(mcdexistList == null) {  
	        				listMClassDetail.add(mClassDetail);
	        				//classService.saveClassChildN(mClassDetail);	
	        			} else {
//	        				for (MClassDetail xClassDetail : mcdexistList) {
//		        				if(xClassDetail.getDescEn() == null) {	        					
//		        					classService.deleteMClassDetail(xClassDetail);	  	        					
//		        				}  			        				
//		        			}
	        				//classService.saveOrUpdateN(mClassDetail); 
	        				listMClassDetail.add(mClassDetail);
	        			}	        			
	        			
	        			try {
	        				if(txTmGeneral!=null) {
	        					TxTmClass txTmClass = new TxTmClass();
		        				txTmClass.setTxTmGeneral(txTmGeneral);
		        				txTmClass.setmClass(mClass);
		        				txTmClass.setmClassDetail(mClassDetail);		        				
		        				txTmClass.setCreatedBy(mUser);	   	        				
		        				Timestamp createdDate2 = new Timestamp(System.currentTimeMillis());		        				
		        				txTmClass.setCreatedDate(createdDate2);	        				     				
		        				txTmClass.setTransactionStatus(ClassStatusEnum.ACCEPT.name());	
		        				txTmClass.setCorrectionFlag(false);
		        				txTmClass.setEdition(mClass.getEdition());
		        				txTmClass.setVersion(mClass.getVersion());
		        				//classService.saveOrUpdateTxTmClassN(txTmClass); 
		        				listTxTmClass.add(txTmClass);
		        				class_base_no++;	
	        				}	        				
	        			} catch (NullPointerException e) {	
	        				System.out.println(e.getMessage());
	        			}
	        		}
	    			if(txTmGeneral!=null) {
	    				//classService.updateMigrasiClassProcessedN(classtList.get(0), txTmGeneral.getApplicationNo());	
	    				hListModMigrasi.put(classtList.get(0), txTmGeneral.getApplicationNo());	    				
	    			}
	    			 	
	    			
	    			
	    		} else if(classtList.size()>1) {
	    			class_base_no=1;
	    			if (strUraian.contains("*")) {	    				
	    				arrUraian = strUraian.split("\\*");	
	    			}
	    			
	    			for(int i=0;i<arrUraian.length;i++){
		   				 if(arrUraian[i].isEmpty() || arrUraian[i].trim().isEmpty()) {
		   					arrUraian = (String[]) ArrayUtils.remove(arrUraian, i); 
		   				 }
		   			}		    			 
	    			
	    			for (int i = 0; i < classtList.size(); i++) {
	    				//label1:
	    				for (int j = 0; j < arrUraian.length; j++) {
	    					//separator atau pemisah uraian berdasarkan spasi lebih satu, misal : '  ' dan titik : '.' 	    					
	    					if(i== j) {
	    						MClassDetail mcd = new MClassDetail();
	    						mcd.setClassBaseNo(classtList.get(i).replaceAll("\\s+", "").concat(String.valueOf(class_base_no)));	
	    						
	    						strb = new StringBuilder(arrUraian[i].toString());
		        				strb.setLength(max_char); 
		        				mcd.setDesc(strb.toString().replaceAll("[^\\x00-\\x7F]", "").trim());		        					    						 
	    						mcd.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
	    	        			mcd.setStatusFlag(false);
	    	        			//mcd.setParentClass(classService.findFirstByNo( String.valueOf(classtList.get(i).replaceAll("\\s+", ""))) );
	    	        			mcd.setParentClass(classService.findFirstByNo( Integer.parseInt(classtList.get(i).replaceAll("\\s+", ""))) );
	    	        			
	    	        			MClassDetail mcdexist =  classService.findOneBydescMClassDetail(arrUraian[i]);
	    	        			if(mcdexist == null) {
	    	        				classService.saveClassChild(mcd);
	    	        			} else {
	    	        				class_base_no--;	    	        			
	    	        			}	     						
	    					} else {
	    						//jika tidak masuk ke class detail, maka akan masuk ke table tampungan temporary class
	    						logger.debug("masuk ke temporary class");
	    					}
	    					
	    				}
	        		}
	    		}
	    	}
	    		    	
    	} catch (Exception ex) {
    		logger.error(ex);
    	}
    	
    
    }*/


    /*public void cleanUp() {
        try {
            if (!this.getJdbcTemplate().getDataSource().getConnection().isClosed()) {
                this.getJdbcTemplate().getDataSource().getConnection().close();
            }
        } catch (Exception e) {
            Logger.getLogger(myDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
    }*/
}