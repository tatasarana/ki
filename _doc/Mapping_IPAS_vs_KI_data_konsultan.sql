SELECT    
(IPMR.FILE_SEQ || IPMR.FILE_TYP || IPMR.FILE_SER || IPMR.FILE_NBR) AS APPLICATION_NO,   
IPP.PERSON_NAME REPRESENTATIVE_NAME,   
IPADD.ADDR_STREET REPRESENTATIVE_ADDRESS,   
IPP.NATIONALITY_COUNTRY_CODE REPRESENTATIVE_NATIONALITY 

FROM PRODDGIPR.IP_MARK_REPRS IPMR 
LEFT JOIN PRODDGIPR.IP_PERSON IPP ON IPMR.PERSON_NBR = IPP.PERSON_NBR 
LEFT JOIN PRODDGIPR.IP_PERSON_ADDRESSES IPADD ON IPMR.PERSON_NBR = IPADD.PERSON_NBR AND IPMR.ADDR_NBR = IPADD.ADDR_NBR; 
 