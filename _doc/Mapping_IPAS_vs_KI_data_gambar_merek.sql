SELECT     
IM.APPLICATION_ID,     
BR.TM_BRAND_COLOR,


IM.UPDATED_DATE

FROM 
TX_TM_GENERAL IM , TX_TM_BRAND BR
WHERE
IM.APPLICATION_ID = BR.APPLICATION_ID

;