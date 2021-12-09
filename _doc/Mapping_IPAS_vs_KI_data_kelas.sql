SELECT     
IM.APPLICATION_ID,   

--IPMNC.NICE_CLASS_CODE AS NICE_CLASS_TXT,   
CS.CLASS_NO,


TC.CLASS_ID,

--IPMNC.NICE_CLASS_DESCRIPTION 
MD.CLASS_DESC,


IM.UPDATED_DATE

FROM 
TX_TM_GENERAL IM ,   TX_TM_CLASS TC , M_CLASS_DETAIL MD , M_CLASS CS
WHERE
IM.APPLICATION_ID = TC.APPLICATION_ID
AND
TC.CLASS_DETAIL_ID = MD.CLASS_DETAIL_ID
AND
CS.CLASS_ID = MD.CLASS_ID


;