package com.docotel.ki.service.transaction;

import com.docotel.ki.model.master.MFileSequence;
import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.master.MUserRole;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.GenericSearchWrapper;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.RESTService;
import com.docotel.ki.service.master.RepresentativeService;
import com.docotel.ki.service.master.UserService;
import com.docotel.ki.util.DateUtil;
import com.docotel.ki.enumeration.OperationEnum;
import com.docotel.ki.enumeration.RegistrasiStatusEnum;
import com.docotel.ki.enumeration.UserEnum;
import com.docotel.ki.repository.custom.master.MOperationCustomRepository;
import com.docotel.ki.repository.custom.master.MUserRoleCustomRepository;
import com.docotel.ki.repository.custom.transaction.MrepresentativeCustomRepository;
import com.docotel.ki.repository.master.MFileSeqRepository;
import com.docotel.ki.repository.master.MRepresentativeRepository;
import com.docotel.ki.repository.master.MRoleRepository;
import com.docotel.ki.repository.online.custom.RegistrasiOnlineCustomRepository;
import com.docotel.ki.repository.online.jpa.RegistrasiOnlineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RegistrasiOnlineService {
    private static Logger logger = LoggerFactory.getLogger(RegistrasiOnlineService.class);
    @Autowired
    RegistrasiOnlineRepository registrasiOnlineRepository;
    @Autowired
    RegistrasiOnlineCustomRepository registrasiOnlineCustomRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    MOperationCustomRepository mOperationCustomRepository;
    @Autowired
    RepresentativeService representativeService;
    @Autowired
    RESTService restService;

    @Autowired
    UserService userService;
    @Autowired
    MRepresentativeRepository mRepresentativeRepository;
    @Autowired
    MrepresentativeCustomRepository mrepresentativeCustomRepository;
    @Autowired
    MRoleRepository mRoleRepository;
    @Autowired
    MUserRoleCustomRepository mUserRoleCustomRepository;
    @Autowired
    MFileSeqRepository mFileSeqRepository;
    @Value("${upload.file.doc.regaccount.path:}")
    private String uploadFileDocRegaccountPath;



    public void injectNewPDKKI(TxOnlineReg txOnlineReg, boolean NEW_REPS) throws IOException {
        mrepresentativeCustomRepository.injectNewRegister(txOnlineReg,NEW_REPS);

    }


    @Transactional(readOnly = false, rollbackFor = Exception.class)
    public void insert(TxOnlineReg txOnlineReg) throws IOException {
        try{
            registrasiOnlineRepository.save(txOnlineReg);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        /*if (txOnlineReg.getFileKtp() != null) {
            File file = new File(uploadFilePath + txOnlineReg.getId() + "_" + txOnlineReg.getFileNameNik());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            byte[] buff = new byte[5120];
            BufferedInputStream bis = new BufferedInputStream(txOnlineReg.getFileKtp().getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            while (bis.available() > 0) {
                bis.read(buff);
                bos.write(buff);
            }
            bos.close();
            bis.close();
        }*/
        if (txOnlineReg.getFileCard() != null) {
            String pathAdd = DateUtil.formatDate(txOnlineReg.getCreatedDate(), "yyyy/MM/dd/");
            File file = new File(uploadFileDocRegaccountPath + pathAdd + txOnlineReg.getId() + "_" + txOnlineReg.getFileNameCard());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            byte[] buff = new byte[5120];
            BufferedInputStream bis = new BufferedInputStream(txOnlineReg.getFileCard().getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            while (bis.available() > 0) {
                bis.read(buff);
                bos.write(buff);
            }
            bos.close();
            bis.close();
        }
    }

    public TxOnlineReg findOne(String s) {
        return registrasiOnlineCustomRepository.selectOne("LEFT JOIN FETCH c.nationality n LEFT JOIN FETCH c.gender g " +
                "LEFT JOIN FETCH c.mProvince mp LEFT JOIN FETCH c.mCity mc LEFT JOIN FETCH c.mReprs mr ", "id", s, true);
    }

    public TxOnlineReg selectByValue(String by, String value) {
        return registrasiOnlineCustomRepository.selectOne("LEFT JOIN FETCH c.nationality n LEFT JOIN FETCH c.gender g " +
                "LEFT JOIN FETCH c.mProvince mp LEFT JOIN FETCH c.mCity mc LEFT JOIN FETCH c.mReprs mr ", by, value, true);
    }
    
    public TxOnlineReg selectOne(List<KeyValue> searchCriteria) {
        return registrasiOnlineCustomRepository.selectOne(searchCriteria, null, null);
    }

    @Transactional
    public void insertForLogin(TxOnlineReg txOnlineReg) throws IOException {
        registrasiOnlineCustomRepository.saveOrUpdate(txOnlineReg);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        MUser mUser = new MUser();
        mUser.setUsername(txOnlineReg.getEmail());
        mUser.setPassword(passwordEncoder.encode(txOnlineReg.getPassword()));
        mUser.setEmail(txOnlineReg.getEmail());
        mUser.setEnabled(true);
        mUser.setAccountNonExpired(true);
        mUser.setAccountNonLocked(true);
        mUser.setCredentialsNonExpired(true);
        mUser.setOperation(OperationEnum.USER.value());
        mUser.setCreatedBy(UserEnum.SYSTEM.value());
        mUser.setCreatedDate(timestamp);
        mUser.setUpdatedBy(null);
        mUser.setUpdatedDate(timestamp);
        mUser.setEmployee(false);
        mUser.setmSection(null);
        if(txOnlineReg.getApplicantType().equals("Kanwil")) {
        	mUser.setmFileSequence(mFileSeqRepository.findOne(txOnlineReg.getFileSeqId()));
        	mUser.setUserType("Kanwil");
        }
        else if (txOnlineReg.getApplicantType().equals("Konsultan KI")){
            mUser.setReprs(true);
            mUser.setUserType("Konsultan");
            MFileSequence mSec = new MFileSequence() ;
            mSec.setId("ONLINE");
            mUser.setmFileSequence(mSec);
        }
        else {
        	mUser.setmFileSequence(mFileSeqRepository.findOne("ONLINE"));// file_seq=ONLINE
        	mUser.setUserType("Registrasi Akun");
        }

        userService.insert(mUser);

        if (txOnlineReg.getApplicantType().equals("Konsultan KI")) {

            String noReps = "" ;
            try{
                noReps = txOnlineReg.getmReprs().getCurrentId();
            }
            catch (NullPointerException nl){
                noReps  = txOnlineReg.getId();
            }

            try {
                // Konsultan boleh memiliki lebih dari satu I.D.
                MRepresentative mRepresentative = restService.synPDKKIbyNo(noReps,"pdkki");
                if (mRepresentative == null){
                    //System.out.println("Tidak Terdaftar di PDKKI!");
                } else {
                    mRepresentative.setUserId(mUser);
                    try {
                        mrepresentativeCustomRepository.saveNewReps(mRepresentative);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();

            }
        }

        MUserRole mUserRole = new MUserRole();
        mUserRole.setmUser(mUser);
        mUserRole.setmRole(mRoleRepository.findOne("PUBLIC")); //Role=Permohonan Online
        mUserRoleCustomRepository.saveOrUpdate(mUserRole);
    }

    public GenericSearchWrapper<TxOnlineReg> searchOnlineReg(List<KeyValue> searchCriteria, String orderBy, String orderType, int offset, int limit) {
        GenericSearchWrapper<TxOnlineReg> searchResult = new GenericSearchWrapper<TxOnlineReg>();
        searchResult.setCount(registrasiOnlineCustomRepository.countData(searchCriteria));
        if (searchResult.getCount() > 0) {
            searchResult.setList(registrasiOnlineCustomRepository.selectAll(searchCriteria, orderBy, orderType, offset, limit));
        }
        return searchResult;
    }

    @Transactional
    public void doApproveReject(TxOnlineReg txOnlineReg) {
        registrasiOnlineCustomRepository.saveOrUpdate(txOnlineReg);
    }

    @Transactional
    public void saveOrUpdate(TxOnlineReg txOnlineReg) {
        registrasiOnlineCustomRepository.saveOrUpdate( txOnlineReg );
    }
    public Long count(List<KeyValue> searchCriteria) {
        return registrasiOnlineCustomRepository.count(searchCriteria);
    }

    public Long count(String by, Object value, boolean exactMatch) {
        return registrasiOnlineCustomRepository.count(by, value, exactMatch);
    }

    @Transactional
    public void batchJob() {
        List<TxOnlineReg> listResult = registrasiOnlineCustomRepository.selectAll("approvalStatus", RegistrasiStatusEnum.PREPARE.name(), true, null, null);
        for (TxOnlineReg txOnlineReg : listResult) {
            String createdDate = DateUtil.formatDate(txOnlineReg.getCreatedDate(), "yyyy-MM-dd HH:mm:ss");
            String currentDate = DateUtil.formatDate(new Timestamp(System.currentTimeMillis()), "yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime locCreatedDate = LocalDateTime.parse(createdDate, format);
            LocalDateTime locCurrentDate = LocalDateTime.parse(currentDate, format);
            Duration duration = Duration.between(locCreatedDate, locCurrentDate);

            /*if (duration.toHours() > 24) {
                logger.info("- Set Expire for Reg with email: " + txOnlineReg.getEmail());
                txOnlineReg.setApprovalStatus(RegistrasiStatusEnum.EXPIRE.name());
                doApproveReject(txOnlineReg);
            }*/
        }
    }

    public TxOnlineReg findByUserEfiling(String user_id) {
        return registrasiOnlineRepository.findByUserEfiling(user_id);
    }

    @Transactional
    public void insertForLoginEfiling(TxOnlineReg txOnlineReg) throws IOException {
        registrasiOnlineCustomRepository.saveOrUpdate(txOnlineReg);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        MUser mUser = new MUser();
        mUser.setId( txOnlineReg.getId() );
        mUser.setUsername(txOnlineReg.getEmail());
        mUser.setPassword(passwordEncoder.encode(txOnlineReg.getPassword()));
        mUser.setEmail(txOnlineReg.getEmail());
        mUser.setEnabled(true);
        mUser.setAccountNonExpired(true);
        mUser.setAccountNonLocked(true);
        mUser.setCredentialsNonExpired(true);
        mUser.setOperation(OperationEnum.USER.value());
        mUser.setCreatedBy(UserEnum.SYSTEM.value());
        mUser.setCreatedDate(timestamp);
        mUser.setUpdatedBy(null);
        mUser.setUpdatedDate(timestamp);
        mUser.setEmployee(false);
        mUser.setmSection(null);
        if(txOnlineReg.getApplicantType().equals("Kanwil")) {
        	mUser.setmFileSequence(mFileSeqRepository.findOne(txOnlineReg.getFileSeqId()));
        } else {
        	mUser.setmFileSequence(mFileSeqRepository.findOne("ONLINE"));// file_seq=ONLINE
        }
        mUser.setUserType("Registrasi Akun");
        userService.insert(mUser);

        if (txOnlineReg.getmReprs() != null) {
            mUser.setReprs(true);
            mUser.setUserType("Konsultan");
            userService.saveOrUpdate(mUser);

            String noReps = "" ;
            try {
                noReps = txOnlineReg.getmReprs().getCurrentId();
            } catch (NullPointerException nl) {
                noReps  = txOnlineReg.getId();
            }

            MRepresentative mRepresentative = restService.synPDKKIbyNo(noReps,"pdkki");
            if (mRepresentative == null){
                //System.out.println("Tidak Terdaftar di PDKKI!");
            } else {
                mRepresentative.setUserId(mUser);
                try {
                    mrepresentativeCustomRepository.saveNewReps(mRepresentative);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        MUserRole mUserRole = new MUserRole();
        mUserRole.setmUser(mUser);
        mUserRole.setmRole(mRoleRepository.findOne("PUBLIC")); //Role=Permohonan Online
        mUserRoleCustomRepository.saveOrUpdate(mUserRole);
    }
}
