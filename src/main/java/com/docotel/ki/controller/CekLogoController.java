package com.docotel.ki.controller;

import com.docotel.ki.base.BaseController;
import com.docotel.ki.component.LireIndexing;
import com.docotel.ki.model.transaction.TxTmGeneral;
import com.docotel.ki.service.master.BrandService;
import com.docotel.ki.service.transaction.TrademarkService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.util.NumberUtil;
import lire.ImageSearch;
import lire.ImageSearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

@Controller
@RequestMapping(BaseController.PATH_AFTER_LOGIN)
public class CekLogoController extends BaseController {
	private static final String DIRECTORY_PAGE = "pemeriksaan-substantif/";

	private static final String PAGE_LIST = DIRECTORY_PAGE + "list-cek-logo";

	private static final String PATH_LIST = "/list-cek-logo";

	@Value("${index.similarity.file.path:}")
	private String indexSimilarityFilePath;

	@Autowired
	private BrandService brandService;

	@Autowired
	private LireIndexing lireIndexing;

	@Autowired
	private TrademarkService trademarkService;

	@Value("${upload.file.brand.path:}")
	private String uploadFileBrandPath;

	@Value("${upload.file.ipasimage.path:}")
	private String uploadFileIpasPath;

	@ModelAttribute
	public void addModelAttribute(final Model model, final HttpServletRequest request) {
		model.addAttribute("isIndexingOnProgress", lireIndexing.isIndexingOnProgress());
		model.addAttribute("indexingProgress", lireIndexing.getIndexingProgress());
		model.addAttribute("menu", "listChk");
	}

	@GetMapping(path = PATH_LIST)
	public String showPageList(final Model model, final HttpServletRequest request, final HttpServletResponse response) {
		String noCekKesamaanLogo = request.getParameter( "no" );
		if (noCekKesamaanLogo != null) {
			doCekLogoKesamaan(model, request);
		}
		return PAGE_LIST;
	}

	private void doCekLogoKesamaan(Model model, final HttpServletRequest request) {
		String appNo = request.getParameter( "no" );
		TxTmGeneral txTmGeneral = trademarkService.selectOneGeneralByApplicationNo( appNo );
		if (txTmGeneral != null) {
			File file = null;
			FileInputStream imageCheck = null;
			byte[] byteImage = null;
			String img = null;
			try {
				String pathFolder = DateUtil.formatDate( txTmGeneral.getTxTmBrand().getCreatedDate(), "yyyy/MM/dd/" );
				file = new File( uploadFileBrandPath + pathFolder + txTmGeneral.getTxTmBrand().getId() + ".jpg" );
				if (file.isFile() && file.canRead()) {
					imageCheck = new FileInputStream( uploadFileBrandPath + pathFolder + txTmGeneral.getTxTmBrand().getId() + ".jpg" );
				} else {
					file = new File( uploadFileBrandPath + uploadFileIpasPath + txTmGeneral.getTxTmBrand().getId() + ".jpg" );
					if (file.isFile() && file.canRead()) {
						imageCheck = new FileInputStream( uploadFileBrandPath + uploadFileIpasPath + txTmGeneral.getTxTmBrand().getId() + ".jpg" );

						List<ImageSearchResult> similarImageFilePathList = new ImageSearch(indexSimilarityFilePath).searchForImage(imageCheck, 25);
						Map<String, ImageSearchResult> similarImageMap = new HashMap<>();
						if (similarImageFilePathList.size() > 0) {
							for (ImageSearchResult similarImageFilePath : similarImageFilePathList) {
								try {
									File fileimg = new File(similarImageFilePath.getImagePath());
									String filename = fileimg.getName();
									String brandId = null;
									if (filename.indexOf(BrandService.BRAND_ID_FILE_NAME_CONNECTOR) > 0) {
										brandId = filename.substring(filename.lastIndexOf("/") + 1, filename.indexOf(BrandService.BRAND_ID_FILE_NAME_CONNECTOR));
									} else if (filename.indexOf('.') > 0) {
										brandId = filename.substring(filename.lastIndexOf("/") + 1, filename.indexOf('.'));
									}else {
										brandId = filename.substring(filename.lastIndexOf("/") + 1);
									}
									if (!similarImageMap.keySet().contains(brandId)) {
										similarImageFilePath.setFile(fileimg);
										similarImageMap.put(brandId, similarImageFilePath);
									}
								} catch (Exception e) {
								}
							}
						}
						List<Object[]> brandList = brandService.selectAllBrandList(new ArrayList(similarImageMap.keySet()));

						List<Map<String, Object>> resultList = new ArrayList<>();
						Map<String, Object[]> brandMap = new HashMap<>();
						for (Object[] txTmBrand : brandList) {
							brandMap.put(txTmBrand[0].toString(), txTmBrand);
						}

						for (String similarBrandId : similarImageMap.keySet()) {
							if (brandMap.containsKey(similarBrandId)) {
								Object[] txTmBrand = brandMap.get(similarBrandId);

								Map<String, Object> brand = new HashMap<>();
								byte[] byteImg = null;
								try {
									FileInputStream fis = new FileInputStream(similarImageMap.get(txTmBrand[0].toString()).getFile());
									byteImg = new byte[fis.available()];
									fis.read(byteImg);
									fis.close();
								} catch (IOException e) {
								}
								if (byteImg != null) {
									brand.put("imageSrc", "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImg));
								} else {
									brand.put("imageSrc", "");
								}
								brand.put("score", NumberUtil.formatDecimal(similarImageMap.get(txTmBrand[0].toString()).getScore()));
								brand.put("class", txTmBrand[5] == null ? "" : txTmBrand[5].toString());
								brand.put("ownerName", txTmBrand[6] == null ? "" : txTmBrand[6].toString());

								brand.put("applicationNo", txTmBrand[3] == null ? "" : txTmBrand[3].toString());
								brand.put("ownerName", txTmBrand[6] == null ? "" : txTmBrand[6].toString());
								brand.put("brandName", txTmBrand[2] == null ? "" : txTmBrand[2].toString());
								brand.put("name", txTmBrand[1] == null ? "" : txTmBrand[1].toString());
								brand.put("status", txTmBrand[4] == null ? "" : txTmBrand[4].toString());

								resultList.add(brand);
							}
						}
						model.addAttribute("resultList", resultList);
					} else {
						imageCheck = null;
					}
				}


				byteImage = new byte[imageCheck.available()];
				imageCheck.read(byteImage);
				imageCheck.close();
			} catch (NullPointerException e) {

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			img = (byteImage != null ? "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage) + "" : "" );
			model.addAttribute( "imgCekMerek", img);
		}
	}

	@PostMapping(path = PATH_LIST)
	public String doShowSimilarImage(final Model model, final HttpServletRequest request, final HttpServletResponse response, @RequestParam(name = "searchImage", required = false) final MultipartFile searchImageFile) {
		if (lireIndexing.isIndexingOnProgress()) {
			model.addAttribute("imgCekMerekError", "Silakan upload gambar dalam format JPG / JPEG terlebih dahulu");
		} else {
			if (searchImageFile != null && !searchImageFile.isEmpty()) {
				if (searchImageFile.getOriginalFilename().toLowerCase().endsWith(".jpg") || searchImageFile.getOriginalFilename().toLowerCase().endsWith(".jpeg")) {
					try {
						model.addAttribute("imgCekMerek", "data:image/jpeg;base64, " + Base64Utils.encodeToString(searchImageFile.getBytes()));

						List<ImageSearchResult> similarImageFilePathList = new ImageSearch(indexSimilarityFilePath).searchForImage(searchImageFile.getInputStream(), 25);
						Map<String, ImageSearchResult> similarImageMap = new HashMap<>();
						if (similarImageFilePathList.size() > 0) {
							for (ImageSearchResult similarImageFilePath : similarImageFilePathList) {
								try {
									File file = new File(similarImageFilePath.getImagePath());
									String filename = file.getName();
									String brandId = null;
									if (filename.indexOf(BrandService.BRAND_ID_FILE_NAME_CONNECTOR) > 0) {
										brandId = filename.substring(filename.lastIndexOf("/") + 1, filename.indexOf(BrandService.BRAND_ID_FILE_NAME_CONNECTOR));
									} else if (filename.indexOf('.') > 0) {
										brandId = filename.substring(filename.lastIndexOf("/") + 1, filename.indexOf('.'));
									}else {
										brandId = filename.substring(filename.lastIndexOf("/") + 1);
									}
									if (!similarImageMap.keySet().contains(brandId)) {
										similarImageFilePath.setFile(file);
										similarImageMap.put(brandId, similarImageFilePath);
									}
								} catch (Exception e) {
								}
							}
						}
						List<Object[]> brandList = brandService.selectAllBrandList(new ArrayList(similarImageMap.keySet()));

						List<Map<String, Object>> resultList = new ArrayList<>();
						Map<String, Object[]> brandMap = new HashMap<>();
						for (Object[] txTmBrand : brandList) {
							brandMap.put(txTmBrand[0].toString(), txTmBrand);
						}

						for (String similarBrandId : similarImageMap.keySet()) {
							if (brandMap.containsKey(similarBrandId)) {
								Object[] txTmBrand = brandMap.get(similarBrandId);

								Map<String, Object> brand = new HashMap<>();
								byte[] byteImage = null;
								try {
									FileInputStream fis = new FileInputStream(similarImageMap.get(txTmBrand[0].toString()).getFile());
									byteImage = new byte[fis.available()];
									fis.read(byteImage);
									fis.close();
								} catch (IOException e) {
								}
								if (byteImage != null) {
									brand.put("imageSrc", "data:image/jpeg;base64, " + Base64Utils.encodeToString(byteImage));
								} else {
									brand.put("imageSrc", "");
								}
								brand.put("score", NumberUtil.formatDecimal(similarImageMap.get(txTmBrand[0].toString()).getScore()));
								brand.put("class", txTmBrand[5] == null ? "" : txTmBrand[5].toString());
								brand.put("ownerName", txTmBrand[6] == null ? "" : txTmBrand[6].toString());

								brand.put("applicationNo", txTmBrand[3] == null ? "" : txTmBrand[3].toString());
								brand.put("ownerName", txTmBrand[6] == null ? "" : txTmBrand[6].toString());
								brand.put("brandName", txTmBrand[2] == null ? "" : txTmBrand[2].toString());
								brand.put("name", txTmBrand[1] == null ? "" : txTmBrand[1].toString());
								brand.put("status", txTmBrand[4] == null ? "" : txTmBrand[4].toString());

								resultList.add(brand);
							}
						}
						model.addAttribute("resultList", resultList);
					} catch (IOException e) {
						model.addAttribute("imgCekMerekError", "Gagal membaca file gambar");
					}
				} else {
					model.addAttribute("imgCekMerekError", "Ekstensi file tidak valid");
				}
			} else {
				model.addAttribute("imgCekMerekError", "Silakan upload gambar dalam format JPG / JPEG terlebih dahulu");
			}
		}
		return PAGE_LIST;
	}
}