package com.docotel.ki.service.transaction;

import com.docotel.ki.model.master.MCountry;
import com.docotel.ki.model.transaction.*;
import com.docotel.ki.repository.custom.transaction.TxTmBrandCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmClassCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmGeneralCustomRepository;
import com.docotel.ki.repository.custom.transaction.TxTmReprsCustomRepository;
import com.docotel.ki.repository.master.MCountryRepository;
import com.docotel.ki.repository.transaction.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;


import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MadridOoService {

    @Autowired
    private TxTmGeneralCustomRepository txTmGeneralCustomRepository;
    @Autowired
    private TxTmMadridFeeRepository txTmMadridFeeRepository;
    @Autowired
    private TxTmMadridFeeDetailRepository txTmMadridFeeDetailRepository;
    @Autowired
    private MCountryRepository mCountryRepository;
    @Autowired
    private TxTmBrandRepository txTmBrandRepository;
    @Autowired
    private TxTmClassRepository txTmClassRepository;
    @Autowired
    private TrademarkService trademarkService;
	@Autowired
	private TxTmClassCustomRepository txTmClassCustomRepository;
	@Autowired
	private TxTmBrandCustomRepository txTmBrandCustomRepository; 
	@Autowired 
	private TxTmReferenceRepository txTmReferenceRepository;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TxTmReprsCustomRepository txTmReprsCustomRepository ;
    
	public TxTmGeneral selectOneDataReferensi(String by, String value) {
        return txTmGeneralCustomRepository.selectOne(
                "LEFT JOIN FETCH c.txReception " +
                        "LEFT JOIN FETCH c.txRegistration r" +
                        "LEFT JOIN FETCH c.txTmBrand b" +
                        "LEFT JOIN FETCH c.txTmClassList cl " +
                        "LEFT JOIN FETCH cl.mClass cls " +
                        "LEFT JOIN FETCH cl.mClassDetail cdl ", by, value, true);
    }
	
	public TxTmMadridFee saveMadridFee(TxTmGeneral txTmGeneral, BigDecimal basicFee, Integer compVol, 
			BigDecimal compTotal, Integer supVol, BigDecimal supTotal, BigDecimal feeTotal) {
		TxTmMadridFee madridFee = txTmMadridFeeRepository.findByTxTmGeneral(txTmGeneral);
		if(madridFee == null) {
			madridFee = new TxTmMadridFee(txTmGeneral, basicFee, compVol, compTotal, supVol, supTotal, feeTotal);
		} else {
			madridFee.setBasicFee(basicFee);
			madridFee.setComplentaryFeeVolume(compVol);
			madridFee.setComplentaryFeeTotal(compTotal);
			madridFee.setSuplementaryFeeVolume(supVol);
			madridFee.setSuplementaryFeeTotal(supTotal);
			madridFee.setGrandFeeTotal(feeTotal);
		}
		txTmMadridFeeRepository.save(madridFee);
		
		return madridFee;
	}

	@Transactional
	public void saveTxTmRepresentative(TxTmRepresentative txTmRepresentative){
		txTmReprsCustomRepository.saveOrUpdate(txTmRepresentative);
	}
	
	public void saveMadridFeeDetail(TxTmMadridFee madridFee, String[] listBiayaNegara) {
		List<TxTmMadridFeeDetail> madridFeeDetails = new ArrayList<>();
		
		for(String biayaNegara : listBiayaNegara) {
			String[] biayaNegaraTemp = biayaNegara.split(";");
			MCountry country = mCountryRepository.findOne(biayaNegaraTemp[0]);
			TxTmMadridFeeDetail madridFeeDetail = txTmMadridFeeDetailRepository.findByTxTmMadridFeeAndMCountry(madridFee, country);
			if(madridFeeDetail == null) {
				madridFeeDetail = new TxTmMadridFeeDetail(madridFee, country, new BigDecimal(biayaNegaraTemp[1]));
				madridFeeDetails.add(madridFeeDetail);
			}
		}
		txTmMadridFeeDetailRepository.save(madridFeeDetails);
	}
	
	@Transactional
	public void saveBrandAndClassFromReference(String appNo, String referenceId) {
		TxTmGeneral txTmGeneral = trademarkService.selectOneRegistration("applicationNo", appNo);
		TxTmGeneral generalReference = trademarkService.selectOneRegistration("applicationNo", referenceId);
		//cek new brand is equal current brand
		if(txTmGeneral.getTxTmBrand()==null){
			TxTmBrand txTmBrand = new TxTmBrand(generalReference.getTxTmBrand(), txTmGeneral);
			txTmBrandCustomRepository.saveOrUpdate(txTmBrand);
		}
		/*List<TxTmClass> txTmClasses = generalReference.getTxTmClassList().stream().map(txTmClass -> {
			return new TxTmClass(txTmClass, txTmGeneral);
		}).collect(Collectors.toList());*/
		txTmClassRepository.save(generalReference.getTxTmClassList());
	}

	@Transactional
	public void deleteBrandAndClassFromReference(String appNo, String referenceId) {
		TxTmGeneral txTmGeneral = trademarkService.selectOneRegistration("applicationNo", appNo);
		TxTmGeneral generalReference = trademarkService.selectOneRegistration("applicationNo", referenceId);
		//cek new brand is equal current brand
		if(txTmGeneral.getTxTmBrand()==null){
			TxTmBrand txTmBrand = new TxTmBrand(generalReference.getTxTmBrand(), txTmGeneral);
			txTmBrandCustomRepository.delete(txTmBrand);
		}
		List<TxTmClass> txTmClasses = generalReference.getTxTmClassList().stream().map(txTmClass -> {
			return new TxTmClass(txTmClass, txTmGeneral);
		}).collect(Collectors.toList());
		txTmClassRepository.delete(txTmClasses); 
		
		TxTmReference txTmReference2 = txTmReferenceRepository.getReffByTxTmGeneralIdAndEFilingNo(txTmGeneral.getId(), generalReference.getId());
		
		if(txTmReference2 != null){
			txTmReferenceRepository.delete(txTmReference2);
		}
		
	}

	public Connection getConnection(){

		Connection connection = null;
		try {
			connection =  dataSource.getConnection();
		} catch (SQLException e) {

		}
		return connection;
	}
	

}
