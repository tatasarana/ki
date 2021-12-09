SELECT   
(IPMNC.FILE_SEQ || IPMNC.FILE_TYP || IPMNC.FILE_SER || IPMNC.FILE_NBR) AS APPLICATION_NO,   
IPMNC.NICE_CLASS_CODE AS NICE_CLASS_TXT,   
IPMNC.NICE_CLASS_DESCRIPTION 

FROM PRODDGIPR.IP_MARK_NICE_CLASSES IPMNC 