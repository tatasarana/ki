package com.docotel.ki.model.master;

import com.docotel.ki.model.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.docotel.ki.audittrail.EntityAuditTrailListener;
import com.docotel.ki.enumeration.OperationEnum;
import com.docotel.ki.enumeration.UserEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;

import java.sql.Timestamp;
import java.util.*;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "m_user")
@EntityListeners(EntityAuditTrailListener.class)
public class MUser extends BaseModel implements UserDetails {
    public static boolean flagLoop = false;

    /***************************** - FIELD SECTION - ****************************/
    private String id;
    private String username;
    private String password;
    private String email;
    private boolean enabled = false;
    private boolean accountNonExpired = false;
    private boolean accountNonLocked = false;
    private boolean credentialsNonExpired = false;
    private MOperation operation;
    private MSection mSection;
    private MUser createdBy;
    private Timestamp createdDate;
    private MUser updatedBy;
    private MFileSequence mFileSequence;
    private Timestamp updatedDate;
    private boolean employee = false;
    private boolean reprs = false;
    private String userType;
//    private Timestamp lastLogin ;


    private MEmployee mEmployee;
    private MRepresentative mRepresentative;

    /***************************** - TRANSIENT FIELD SECTION - ****************************/
    @Transient
    private List<MRoleDetail> mRoleDetailList;
    @Transient
    private List<MMenu> mMenuList;
    @Transient
    private Map<String, List<MMenuDetail>> mMenuDetailMap;

    /***************************** - CONSTRUCTOR SECTION - ****************************/
    public MUser() {
    }

    public MUser(String id) {
        this.id = id;
    }

    public MUser(MUser mUser) {
        if (mUser != null) {
            this.id = mUser.id;
            this.username = mUser.username;
            this.password = mUser.password;
            this.email = mUser.email;
            this.enabled = mUser.enabled;
            this.accountNonExpired = mUser.accountNonExpired;
            this.accountNonLocked = mUser.accountNonLocked;
            this.credentialsNonExpired = mUser.credentialsNonExpired;
            if (mUser.operation != null) {
                this.operation = new MOperation(mUser.operation.getAuthority());
            }
            if (mUser.mSection != null) {
                mSection = new MSection(mUser.mSection);
            }
            if (mUser.createdBy != null) {
                this.createdBy = new MUser(mUser.createdBy);
            }
            this.createdDate = mUser.createdDate;
//            this.lastLogin = mUser.lastLogin;
            if (mUser.updatedBy != null) {
                this.updatedBy = new MUser(mUser.updatedBy);
            }
            updatedDate = mUser.updatedDate;
            if (mUser.mFileSequence != null) {
                this.mFileSequence = new MFileSequence(mUser.mFileSequence);
            }
            this.employee = mUser.employee;
            this.reprs = mUser.reprs;
            this.userType = mUser.userType;
            if (mUser.mEmployee != null) {
                this.mEmployee = new MEmployee(mUser.mEmployee);
            }
            if (mUser.mRepresentative != null) {
                this.mRepresentative = new MRepresentative(mUser.mRepresentative);
            }
        }
    }

    public MUser(String id, String username, String password, String email, boolean enabled, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, MFileSequence mFileSequence, boolean employee, boolean reprs, String userType) {
        setId(id);
        setUsername(username);
        setPassword(password);
        setEmail(email);
        setEnabled(enabled);
        setAccountNonExpired(accountNonExpired);
        setAccountNonLocked(accountNonLocked);
        setCredentialsNonExpired(credentialsNonExpired);
        setmFileSequence(mFileSequence);
        setEmployee(employee);
        setReprs(reprs);
        setUserType(userType);
        setOperation(OperationEnum.USER.value());
    }

    /***************************** - GETTER SETTER METHOD SECTION - ****************************/
    @Id
    @Column(name = "user_id", length = 36)
    public String getId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    @Column(name = "username", length = 255, nullable = false, unique=true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        try {
            this.username = username.trim().toLowerCase();
        } catch (NullPointerException e) {
            this.username = null;
        }
    }

    @Override
    @Column(name = "password", length = 100)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "email", length = 255, nullable = true)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    @Column(name = "enabled", nullable = false)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    @Column(name = "acc_non_exp", nullable = false)
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    @Override
    @Column(name = "acc_non_lck", nullable = false)
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    @Column(name = "cred_non_exp", nullable = false)
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation", nullable = false)
    public MOperation getOperation() {
        return operation;
    }

    public void setOperation(MOperation operation) {
        this.operation = operation;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_seq_id", nullable = true)
    public MFileSequence getmFileSequence() {
        return mFileSequence;
    }

    public void setmFileSequence(MFileSequence mFileSequence) {
        this.mFileSequence = mFileSequence;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    @JsonIgnore
    public MUser getCreatedBy() {
        if (createdBy == null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(principal instanceof  UserDetails) {
                createdBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                createdBy = (MUser) UserEnum.SYSTEM.value();
            }
        }
        return createdBy;
    }

    public void setCreatedBy(MUser createdBy) {
        this.createdBy = createdBy;
    }

    @Column(name = "created_date", nullable = false, updatable = false)
    public Timestamp getCreatedDate() {
        if (createdDate == null) {
            createdDate = new Timestamp(System.currentTimeMillis());
        }
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @JsonIgnore
    public MUser getUpdatedBy() {
        if (updatedBy == null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(principal instanceof  UserDetails) {
                updatedBy = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            } else {
                updatedBy = (MUser) UserEnum.SYSTEM.value();
            }
        }
        return updatedBy;
    }

    public void setUpdatedBy(MUser updatedBy) {
        this.updatedBy = updatedBy;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = true)
    public MSection getmSection() {
        return mSection;
    }

    public void setmSection(MSection mSection) {
        this.mSection = mSection;
    }

    @Column(name = "updated_date")
    public Timestamp getUpdatedDate() {
        if (updatedDate == null) {
            updatedDate = new Timestamp(System.currentTimeMillis());
        }
        return updatedDate;
    }

//    @Column(name = "last_login")
//    public Timestamp getLastLogin() {
//        if (lastLogin == null) {
//            lastLogin = new Timestamp(System.currentTimeMillis());
//        }
//        return lastLogin;
//    }
//
//
//    public void setLastLogin(Timestamp lastLogin) {
//        this.lastLogin = lastLogin;
//    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Column(name = "is_employee", nullable = false)
    public boolean isEmployee() {
        return employee;
    }

    public void setEmployee(boolean isEmployee) {
        this.employee = isEmployee;
    }

    @Column(name = "is_reprs", nullable = false)
    public boolean isReprs() {
        return reprs;
    }

    public void setReprs(boolean isReprs) {
        this.reprs = isReprs;
    }


    /***************************** - GETTER SETTER TRANSIENT METHOD SECTION - *****************************/

    @OneToOne(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    // @Transient
    public MEmployee getmEmployee() {
        return mEmployee;
    }

    public void setmEmployee(MEmployee mEmployee) {
        this.mEmployee = mEmployee;
    }

    @OneToOne(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    // @Transient
    public MRepresentative getmRepresentative() {
        return mRepresentative;
    }

    public void setmRepresentative(MRepresentative mRepresentative) {
        this.mRepresentative = mRepresentative;
    }

    /***************************** - OTHER METHOD SECTION - ****************************/
    @Override
    @Transient
    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new LinkedHashSet<GrantedAuthority>();
        authorities.add(operation);
        return authorities;
    }

    @Transient
    public String getCurrentId() {
        return id;
    }

    public boolean hasAccessMenu(String... codeArr) {
        // todo uncomment and remove return true to activate user access protection
        try {
            for (String code : codeArr) {
                for (MMenu mMenu : this.mMenuList) {
                    if (StringUtils.equals(code, mMenu.getCode())) {
                        return true;
                    }
                }
            }
        } catch (NullPointerException e) {
        }
        return false;
//		return true;
    }

    public boolean hasAccessMenuDetail(String menuCode, String menuDetailDesc) {
        // todo uncomment and remove return true to activate user access protection
        try {
            List<MMenuDetail> mMenuDetailList = this.mMenuDetailMap.get(menuCode);
            for (MMenuDetail mMenuDetail : mMenuDetailList) {
                if (StringUtils.equals(menuDetailDesc, mMenuDetail.getDesc())) {
                    return true;
                }
            }
        } catch (NullPointerException e) {
        }
        return false;
//		return true;
    }

    public boolean hasAccessUrl(String... urlArr) {
        // todo uncomment and remove return true to activate user access protection
        try {
            for (String url : urlArr) {
                for (List<MMenuDetail> mMenuDetailList : this.mMenuDetailMap.values()) {
                    for (MMenuDetail mMenuDetail : mMenuDetailList) {
                        if (StringUtils.equals(url, mMenuDetail.getUrl())) {
                            return true;
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
        }
        return false;
//		return true;
    }

    public void putMRoleDetailList(List<MRoleDetail> mRoleDetailList) {
        this.mRoleDetailList = mRoleDetailList;
        this.mMenuList = null;
        this.mMenuDetailMap = null;
        if (this.mRoleDetailList != null) {
            this.mMenuList = new ArrayList<>();
            this.mMenuDetailMap = new HashMap<>();

            for (MRoleDetail mRoleDetail : mRoleDetailList) {
                List<MMenuDetail> mMenuDetailList = null;
                if (this.mMenuDetailMap.containsKey(mRoleDetail.getmMenuDetail().getmMenu().getCode())) {
                    mMenuDetailList = this.mMenuDetailMap.get(mRoleDetail.getmMenuDetail().getmMenu().getCode());
                } else {
                    mMenuDetailList = new ArrayList<>();
                    this.mMenuDetailMap.put(mRoleDetail.getmMenuDetail().getmMenu().getCode(), mMenuDetailList);

                    this.mMenuList.add(mRoleDetail.getmMenuDetail().getmMenu());
                }
                mMenuDetailList.add(mRoleDetail.getmMenuDetail());
            }
        }
    }



    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public String logAuditTrail() {
        StringBuilder sb = new StringBuilder();
        sb.append("UUID: " + getCurrentId());
        sb.append("^ Username: " + getUsername());
        sb.append("^ Status: " + (isEnabled() ? "Aktif" : "Tidak Aktif"));
        if(getEmail()!=null) {
            sb.append("^ Email: " +  getEmail() );
        }
        if(getUserType()!=null ) {
            sb.append("^ Tipe User: " + getUserType() );
        }
        if(getmFileSequence()!=null ) {
            sb.append("^ Asal Permohonan: " + getmFileSequence().getDesc()  ) ;
        }
        if(getmSection()!=null ) {
            sb.append("^ Seksi: " +  getmSection().getName() );
        }

        return sb.toString();
    }
}
