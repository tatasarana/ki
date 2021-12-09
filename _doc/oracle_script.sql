
--index utk multisearch inggris
--CREATE INDEX INDEX_CLASS_DETAIL_EN
--ON M_CLASS_DETAIL(CLASS_DESC_EN)
--INDEXTYPE IS ctxsys.CONTEXT;
--index utk multisearch indonesia
--CREATE INDEX INDEX_CLASS_DETAIL
--ON M_CLASS_DETAIL(CLASS_DESC)
--INDEXTYPE IS ctxsys.CONTEXT;

--*sync index kelas detail baru*/
--begin
--ctx_ddl.sync_index('INDEX_CLASS_DETAIL', '2M');
--end;
--begin
--ctx_ddl.sync_index('INDEX_CLASS_DETAIL_EN', '2M');
--end;

--GRANT EXECUTE ON CTXSYS.CTX_DLL TO MEREK;

--DROP INDEX INDEX_CLASS_DETAIL_EN;

--SELECT *  FROM M_CLASS_DETAIL WHERE catsearch (CLASS_DESC, 'alat pengukur akustik', NULL) > 0 ;

SELECT CLASS_DESC, SCORE(11) from M_CLASS_DETAIL WHERE CONTAINS(CLASS_DESC, 'near((peralatan,radiologis,industri),2)',11) > 0 ORDER BY SCORE(11) ASC 

SELECT * from M_CLASS_DETAIL WHERE CLASS_DESC_EN = 'downloadable mobile applications for browsing videos with the built-in browser'
--CREATE INDEX index_class_detail3
--ON M_CLASS_DETAIL(CLASS_DESC)
--INDEXTYPE IS ctxsys.ctxrule;


--SELECT * from M_CLASS_DETAIL where MATCHES (CLASS_DESC, 'air putih amonia')>0 




