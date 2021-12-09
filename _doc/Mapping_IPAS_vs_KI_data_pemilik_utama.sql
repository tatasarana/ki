SELECT     

OW.TM_OWNER_NO,
OW.TM_OWNER_NAME,
OW.TM_OWNER_COUNTRY,
MC.COUNTRY_NAME

FROM 
TX_TM_OWNER OW JOIN M_COUNTRY MC
ON
OW.TM_OWNER_COUNTRY = MC.COUNTRY_ID

;