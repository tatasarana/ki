package com.docotel.ki.service.master;

import com.docotel.ki.enumeration.OperationEnum;
import com.docotel.ki.model.master.*;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.pojo.RegUserReprs;
import com.docotel.ki.repository.custom.master.*;
import com.docotel.ki.repository.custom.transaction.MrepresentativeCustomRepository;
import com.docotel.ki.repository.master.MEmployeeRepository;
import com.docotel.ki.repository.master.MRepresentativeRepository;
import com.docotel.ki.repository.master.MUserRepository;
import com.docotel.ki.service.transaction.RegistrasiOnlineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
	/***************************** - AUTO INJECT SECTION - ****************************/
	@Autowired
	private Environment env;

	@Autowired
    MOperationCustomRepository mOperationCustomRepository;
	@Autowired
	MSectionCustomRepository mSectionCustomRepository;
	@Autowired
	MProvinceCustomRepository mProvinceCustomRepository;
	@Autowired
	MCityCustomRepository mCityCustomRepository;
	@Autowired
	MCountryCostumRepository mCountryCostumRepository;
	@Autowired
	MLookupCostumRepository mLookupCostumRepository;
	@Autowired
    MFileSeqCustomRepository mFileSeqCustomRepository;
	@Autowired
	MEmployeeRepository mEmployeeRepository;
	@Autowired
    MEmployeeCustomRepository mEmployeeCustomRepository;
	@Autowired
	PasswordEncoder passwordEncoder;
	@Autowired
	private MUserRepository mUserRepository;
	@Autowired
	private MUserCustomRepository mUserCustomRepository;
	@Autowired
	private MUserRoleCustomRepository mUserRoleCustomRepository;
	@Autowired
	MrepresentativeCustomRepository mrepresentativeCustomRepository;
	@Autowired
	private MRepresentativeRepository mRepresentativeRepository;
	@Autowired
	private RegistrasiOnlineService registrasiOnlineService;

	/***************************** - METHOD SECTION - ****************************/
	public MUser getUserByName(String name) {
		return mUserCustomRepository.selectOne("username", name, true);
	}

	public List<MUser> getAllUsers() {
		return mUserRepository.findAll();
	}
	
	public List<UserDetails> selectListUserByUserType(String userType) throws UsernameNotFoundException {
		return mUserCustomRepository.selectListUserByUserType(userType);
	}

	public MUser getRoleByUserId(String id) {
		return mUserRepository.findOne(id);
	}

	public MUser getUserById(String id) {
		//v1; return mUserRepository.findOne(id);

		return mUserCustomRepository.selectOne("LEFT JOIN FETCH c.operation o "
						+ "LEFT JOIN FETCH c.mSection ms "
						+ "LEFT JOIN FETCH c.createdBy cb "
						+ "LEFT JOIN FETCH c.updatedBy ub "
						+ "LEFT JOIN FETCH c.mFileSequence mf "
				, "id", id, true);
	}

	public MUser getUserByEmail(String email) {
		return mUserCustomRepository.selectOne("email", email, true);
	}
	
	public MUser getUserByUsername(String username) {
		return mUserCustomRepository.selectOne("username", username, true);
	}
	
	public MUser selectOneById(String id) {
		return mUserCustomRepository.selectOne("id", id);
	}
	
	public Long count(List<KeyValue> searchCriteria) {
        return mUserCustomRepository.count(searchCriteria);
    }

	public void insert(MUser data) {
		mUserRepository.save(data);
	}

	@Transactional
	public void insertUserEmployee(RegUserReprs form) {
		MSection mSection = mSectionCustomRepository.selectOne("id", form.getBagianId(), true);

		MFileSequence mFileSequence = mFileSeqCustomRepository.selectOne("id", form.getAsalPermohonanId(), true);

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		MUser mUser = new MUser();
		mUser.setUsername(form.getUsername());
		mUser.setPassword(passwordEncoder.encode("djkipassword123"));
		mUser.setEmail(form.getEmail());
		mUser.setEnabled(true);
		mUser.setAccountNonExpired(true);
		mUser.setAccountNonLocked(true);
		mUser.setCredentialsNonExpired(true);
		mUser.setOperation(OperationEnum.USER.value());
		mUser.setmSection(mSection);
		mUser.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		mUser.setCreatedDate(timestamp);
		mUser.setUpdatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		mUser.setmFileSequence(mFileSequence);
		mUser.setUpdatedDate(timestamp);
		if ("Karyawan".equalsIgnoreCase(form.getUserType()) || "Kanwil".equalsIgnoreCase(form.getUserType())) {
			mUser.setEmployee(true);
			mUser.setReprs(false);
		} else if ("Konsultan".equalsIgnoreCase(form.getUserType())) {
			mUser.setEmployee(false);
			mUser.setReprs(true);
		} else if ("Others".equalsIgnoreCase(form.getUserType())) {
			mUser.setEmployee(false);
			mUser.setReprs(false);
		}
		mUser.setUserType(form.getUserType());
		mUserRepository.save(mUser);

		if ("Karyawan".equalsIgnoreCase(form.getUserType()) || "Kanwil".equalsIgnoreCase(form.getUserType())) {
			MEmployee mEmployee = new MEmployee();
			mEmployee.setNik(form.getNik());
			mEmployee.setEmployeeName(form.getNamaKaryawan());
			mEmployee.setTelp(form.getTelp());
			mEmployee.setEmail(form.getEmail());
			mEmployee.setmSection(mSection);
			mEmployee.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			mEmployee.setCreatedDate(timestamp);
			mEmployee.setUserId(mUser);
			mEmployee.setStatusFlag(true);
			mEmployeeRepository.save(mEmployee);
		} else if ("Konsultan".equalsIgnoreCase(form.getUserType())) {
			MRepresentative mRepresentative = new MRepresentative();
			mRepresentative.setId(form.getKonsultan());
			mRepresentative.setName(form.getNamaKaryawan());
			mRepresentative.setUserId(mUser);
			mRepresentative.setPhone(form.getTelp());
			mRepresentative.setEmail(form.getEmail());
			mRepresentative.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
			mRepresentative.setCreatedDate(timestamp);
			mRepresentative.setStatusFlag(true);
			mrepresentativeCustomRepository.saveOrUpdate(mRepresentative);

		}


	}

	@Transactional
	public void updateUserEmployee(MUser mUser, RegUserReprs form) {
		MSection mSection = mSectionCustomRepository.selectOne("id", form.getBagianId(), true);
		MProvince mProvince = mProvinceCustomRepository.selectOne("id", form.getProvinceId(),true);
		MCity mCity = mCityCustomRepository.selectOne("id", form.getCityId(), true);
		MCountry mCountry = mCountryCostumRepository.selectOne("id", form.getNationalityId(), true);
		MLookup mLookup = mLookupCostumRepository.selectOne("id",form.getApplicantType(), true);
		MFileSequence mFileSequence = mFileSeqCustomRepository.selectOne("id", form.getAsalPermohonanId(), true);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		if ("Registrasi Akun".equalsIgnoreCase(form.getUserType())) {
			TxOnlineReg txOnlineReg = registrasiOnlineService.selectByValue("email", mUser.getUsername());

			txOnlineReg.setName( form.getNamaKaryawan() );
			txOnlineReg.setBirthDateTemp( form.getBirthDate() );
			txOnlineReg.setNo(form.getNik());
			txOnlineReg.setAddress( form.getAddress() );
			txOnlineReg.setmProvince( mProvince );
			txOnlineReg.setmCity( mCity );
			txOnlineReg.setNationality( mCountry );
			txOnlineReg.setApplicantType( mLookup.getName());
			txOnlineReg.setZipCode( form.getZipCode() );
			txOnlineReg.setPhone( form.getTelp() );
			registrasiOnlineService.saveOrUpdate( txOnlineReg );
		}

		MEmployee mEmployee = null;
		MRepresentative mRepresentative = null;
		if (mUser.isEmployee()) {
			mEmployee = mEmployeeCustomRepository.selectOne("userId.id", mUser.getCurrentId(), true); //mEmployeeRepository.findOne(form.getId());									
			//mEmployeeCustomRepository.delete("userId.id", mUser.getCurrentId()); 
			if(mEmployee!=null) { 
				mEmployee.setNik(form.getNik());
				mEmployee.setEmployeeName(form.getNamaKaryawan());
				mEmployee.setTelp(form.getTelp());
				mEmployee.setEmail(form.getEmail());
				mEmployee.setmSection(mSection);
				mEmployee.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				mEmployee.setCreatedDate(timestamp);
				mEmployee.setStatusFlag(form.getStatus());
				mEmployee.setUserId(mUser);				
				mEmployeeCustomRepository.saveOrUpdate(mEmployee);
			}
			
			
		}
		if (mUser.isReprs()) {
			String temp_konsultanId = form.getKonsultan();
			String temp_nik = form.getNik();
			String tmp_alamat= form.getAddress();

			String konsultanId = temp_konsultanId.contains(",") ? temp_konsultanId.substring(temp_konsultanId.lastIndexOf(",") + 1) : temp_konsultanId;
			String nik = temp_nik.contains(",") ? temp_nik.substring(temp_nik.lastIndexOf(",") + 1) : temp_nik;
			String alamat = "";
			if (tmp_alamat != null) {
				alamat = tmp_alamat.contains(",") ? tmp_alamat.substring(tmp_alamat.lastIndexOf(",") + 1) : tmp_alamat;
			}

			//mRepresentative = mRepresentativeRepository.findOne(form.getKonsultan());

			mRepresentative = mrepresentativeCustomRepository.selectOne("userId.id", mUser.getId(), true);
			if (mRepresentative != null) {
				
				if (mProvince != null) {
					mRepresentative.setmProvince(mProvince);
				}
				if (mCity != null) {
					mRepresentative.setmCity(mCity);
				}
				if (mCountry != null) {
					mRepresentative.setmCountry(mCountry);
				}
				if (tmp_alamat != null) {
					mRepresentative.setAddress(alamat);
				}
				if (form.getZipCode() != null) {
					mRepresentative.setZipCode(form.getZipCode());
				}
				if (form.getTelp() != null) {
					mRepresentative.setPhone(form.getTelp());
				}
				if (form.getEmail() != null) {
					mRepresentative.setEmail(form.getEmail());
				}				
				
				mRepresentative.setStatusFlag(form.getStatus());
				mRepresentative.setUserId(mUser);
				mrepresentativeCustomRepository.saveOrUpdate(mRepresentative);

//				if (mEmployee != null) { //jika sebelumnya adalah karyawan, maka kolom user_id di mEmployee dikosongkan kembali.
//					mEmployee.setUserId(null);
//					mEmployeeCustomRepository.saveOrUpdate(mEmployee);
//				}

				TxOnlineReg txOnlineReg = registrasiOnlineService.selectByValue("email", mUser.getUsername());
				if(txOnlineReg != null){
					txOnlineReg.setName( form.getNamaKaryawan() );
					txOnlineReg.setNo(nik);
					txOnlineReg.setAddress( alamat );
					txOnlineReg.setmProvince( mProvince );
					txOnlineReg.setmCity( mCity );
					txOnlineReg.setZipCode( form.getZipCode() );
					txOnlineReg.setPhone( form.getTelp() );
					txOnlineReg.setEmail(form.getEmail());
					registrasiOnlineService.saveOrUpdate( txOnlineReg );
				}
			}
		} else {
			if (form.getNik() != null && mEmployee == null) { //konsultan pindah/balik lagi jadi karyawan //user id masih belum ditempelkan NIK
				mEmployee = new MEmployee();
				mEmployee.setNik(form.getNik());
				mEmployee.setEmployeeName(form.getNamaKaryawan());
				mEmployee.setTelp(form.getTelp());
				mEmployee.setEmail(form.getEmail());
				mEmployee.setmSection(mSection);
				mEmployee.setCreatedBy((MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
				mEmployee.setCreatedDate(timestamp);
				mEmployee.setStatusFlag(form.getStatus());
				mEmployee.setUserId(mUser);
				mEmployeeCustomRepository.saveOrUpdate(mEmployee);
			}
		}
		//mUserRepository.findOne(form.getId()); //
		//mUser.setUsername(form.getUsername());
		mUser.setEmail(form.getEmail());
		mUser.setOperation(OperationEnum.USER.value());
		mUser.setmSection(mSection);
		mUser.setmFileSequence(mFileSequence);
		mUser.setUpdatedDate(timestamp);
		mUser.setEnabled(form.getStatus());
		mUser.setUserType(form.getUserType());
		mUser.setmEmployee(mEmployee);
		if ("Karyawan".equalsIgnoreCase(form.getUserType()) || "Kanwil".equalsIgnoreCase(form.getUserType())) {
			mUser.setEmployee(true);
			mUser.setReprs(false);
		} else if ("Konsultan".equalsIgnoreCase(form.getUserType())) {
			mUser.setEmployee(false);
			mUser.setReprs(true);
		} else if ("Others".equalsIgnoreCase(form.getUserType())) {
			mUser.setEmployee(false);
			mUser.setReprs(false);
		}
		//v1-> mEmployeeCustomRepository.saveOrUpdate(mEmployee);
		mUserCustomRepository.saveOrUpdate(mUser);

	}

	@Override
	public UserDetails loadUserByUsername(String username) throws AuthenticationException {
//		if(env.getProperty("app.mode").equals("general")) {
//			Calendar calendar = Calendar.getInstance();
//			int day = calendar.get(Calendar.DAY_OF_WEEK);
//			if(day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
//				throw new CantAccessException("Aplikasi hanya dapat diakses di hari kerja");
//			}
//		}
		return mUserCustomRepository.selectOneUser(username);
	}
	
	public MUser selectUserByUsername(String username) throws AuthenticationException {
		return mUserCustomRepository.selectOneUserByUsername(username);
	}
	
	public MUser selectOneUserByUserId(String userId) throws AuthenticationException {
		return mUserCustomRepository.selectOneUserByUserId(userId);
	}

	public GenericSearchWrapper<MUser> searchGeneral(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MUser> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(mUserCustomRepository.count(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(mUserCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	public GenericSearchWrapper<MUser> searchGeneral2(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		GenericSearchWrapper<MUser> searchResult = new GenericSearchWrapper<>();
		searchResult.setCount(mUserCustomRepository.countAllUserEmployeeReprs(searchCriteria));
		if (searchResult.getCount() > 0) {
			searchResult.setList(mUserCustomRepository.selectAllUserEmployeeReprs(searchCriteria, orderBy, orderType, offset, limit));
		}
		return searchResult;
	}

	@Transactional
	public void saveOrUpdate(MUser mUser) {
		mUserCustomRepository.saveOrUpdate(mUser);
	}

	public List<MUserRole> getUserRolebyUser(MUser mUser) {
		return mUserRoleCustomRepository.selectAll("mUser.id", mUser.getId(), true, 0, 4000);
	}
}
