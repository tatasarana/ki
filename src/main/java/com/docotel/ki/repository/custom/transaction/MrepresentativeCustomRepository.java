package com.docotel.ki.repository.custom.transaction;
import javax.persistence.*;

import com.docotel.ki.base.BaseRepository;
import com.docotel.ki.model.master.MRepresentative;
import com.docotel.ki.model.master.MUser;
import com.docotel.ki.model.transaction.TxOnlineReg;
import com.docotel.ki.pojo.KeyValue;
import com.docotel.ki.service.master.RepresentativeService;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Repository
public class MrepresentativeCustomRepository extends BaseRepository<MRepresentative> {


	@Autowired
	private EntityManagerFactory emf;

	@Autowired
	RepresentativeService representativeService;

    /*@Override
    public List<MRepresentative> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
        return super.selectAll(searchCriteria,orderBy,orderType,offset,limit);
    }*/
	@Override
    public List<MRepresentative> selectAll(List<KeyValue> searchCriteria, String orderBy, String orderType, Integer offset, Integer limit) {
		return selectAll("LEFT JOIN FETCH c.mCountry mc "
				+ "LEFT JOIN FETCH c.mProvince mp "
				+ "LEFT JOIN FETCH c.mCity mci " 
				+ "LEFT JOIN FETCH c.createdBy cb "
				+ "LEFT JOIN FETCH c.userId ui", searchCriteria, orderBy, orderType, offset, limit);
	}


	public String injectNewRegister(TxOnlineReg form, boolean NEW_REPS){
		String message = "";
		try
		{

			EntityManager em = emf.createEntityManager();
			EntityTransaction tx = em.getTransaction();
			tx.begin();

			String action_id =  form.getId();
			Date created_date, updated_date;
			created_date = form.getCreatedDate();
			updated_date = created_date ;



		 String sqlstring = "INSERT INTO TX_ONLINE_REG ( " +
							"  action_id " +
							", skip_audit_trail " +
							", online_address " +
							", online_applicant_type " +
							", approval_status " +
							", online_birth_date " +
							", created_date " +
							", online_email " +
							", online_name " +
							", online_password " +
							", online_phone " +
							", online_zip_code " +
							", online_gender " +
							", online_city " +
							", online_province " +
							", file_seq_id " +
							", online_nationality )" +

							" VALUES ( " +
							"  :action_id " +
							", :skip_audit_trail " +
							", :online_address " +
							", :online_applicant_type " +
							", :approval_status " +
							", :online_birth_date " +
							", :created_date " +
							", :online_email " +
							", :online_name " +
							", :online_password " +
							", :online_phone " +
							", :online_zip_code " +
							", :online_gender " +
							", :online_city " +
							", :online_province " +
							", :file_seq_id " +
							", :online_nationality " +
							")";

		 em.createNativeQuery(sqlstring);


					em.createNativeQuery(sqlstring).setParameter("action_id",form.getmReprs().getCurrentId());

			em.createNativeQuery(sqlstring).setParameter("skip_audit_trail", 0);
			em.createNativeQuery(sqlstring).setParameter("online_address", form.getAddress());
			em.createNativeQuery(sqlstring).setParameter("online_applicant_type", "Konsultan KI");
			em.createNativeQuery(sqlstring).setParameter("approval_status", "PREPARE");
			em.createNativeQuery(sqlstring).setParameter("online_birth_date", created_date);
			em.createNativeQuery(sqlstring).setParameter("created_date", updated_date);
			em.createNativeQuery(sqlstring).setParameter("online_email", form.getEmail());
			em.createNativeQuery(sqlstring).setParameter("online_name", form.getName());
			em.createNativeQuery(sqlstring).setParameter("online_password", form.getPassword());
			em.createNativeQuery(sqlstring).setParameter("online_phone", form.getPhone());
			em.createNativeQuery(sqlstring).setParameter("online_zip_code", form.getZipCode());
			em.createNativeQuery(sqlstring).setParameter("online_gender", form.getGender());
			em.createNativeQuery(sqlstring).setParameter("online_city", form.getmCity());
			em.createNativeQuery(sqlstring).setParameter("online_province", form.getmProvince());
			em.createNativeQuery(sqlstring).setParameter("file_seq_id", "ONLINE");
			em.createNativeQuery(sqlstring).setParameter("online_nationality", form.getNationality());
			em.createNativeQuery(sqlstring).executeUpdate();
			tx.commit();
			em.close();
			message = "Success";



		}
		catch (HibernateException ex){
			return ex.getMessage();
		}

		return null;
	}



	
	public MRepresentative selectOneByUserId(String userId) {
		try {
			return (MRepresentative) entityManager.createQuery(
					//"SELECT new MRepresentative(c.id, c.no, c.name, c.office, c.mCountry, c.mProvince, c.mCity, c.address, c.zipCode, c.phone, c.email, c.statusFlag) FROM " + getClassName() + " c WHERE c.userId.id = :p0")
					"SELECT c FROM MRepresentative c WHERE c.userId.id= :p0")
					.setParameter("p0", userId)
					.getSingleResult();
		} catch (NoResultException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public String saveNewReps(MRepresentative mRep){
		String message = "";
		try
		{

			if (mRep.getId() != null)
			{
				String reprs_id, reprs_email,reprs_name,reprs_no,reprs_office,reprs_phone,reprs_addr;
				String status_flag,reprs_zipcode,created_by,updated_by,city_id,country_id,prov_id,user_id;

				Date created_date, updated_date;

				reprs_id = (mRep.getId() == null ? "reprs_id:NULL": mRep.getId());
				reprs_addr = (mRep.getAddress() == null ? "reprs_address:NULL": mRep.getAddress());
				created_date = mRep.getCreatedDate();
				reprs_email = (mRep.getId() == null ? "reprs_email:NULL": mRep.getEmail());
				reprs_name = (mRep.getName() == null ? "reprs_name:NULL": mRep.getName());
				reprs_no = (mRep.getNo() == null ? "reprs_no:NULL": mRep.getNo());
				reprs_office = (mRep.getOffice() == null ? "reprs_office:NULL": mRep.getOffice());
				reprs_phone = (mRep.getPhone() == null ? "reprs_phone:NULL": mRep.getPhone());
				status_flag = "1" ;
				reprs_zipcode = (mRep.getZipCode() == null ? "reprs_zipcode:NULL": mRep.getZipCode());
				created_by =  "SYSTEM";

				city_id = searchOnebyLike("M_CITY","CITY_ID","CITY_NAME",mRep.getmCity().getName());
				if (city_id == null)
					city_id = "" ;

				country_id = "ID";

				prov_id = searchOnebyLike("M_PROVINCE","PROV_ID","PROV_NAME",mRep.getmProvince().getName());
				if (prov_id == null)
					prov_id = "" ;

				MUser user_lid = mRep.getUserId();
				String user_repId = user_lid.getId();


				updated_date = created_date;
				updated_by = created_by;


//				jika belum ada, maka Reprs_id = reprs_no (mesti unique)
				MRepresentative mtest  = new MRepresentative();
				mtest = representativeService.findOneByNoReps(reprs_no);
				if (mtest == null){
					reprs_id = reprs_no ;
				}



				int resultList = entityManager.createNativeQuery
				("INSERT INTO M_REPRS ( " +
						"  reprs_id " +
						", skip_audit_trail " +
						", reprs_address " +
						", created_date " +
						", reprs_email " +
						", reprs_name " +
						", reprs_no " +
						", reprs_office " +
						", reprs_phone " +
						", status_flag " +
						", reprs_zipcode " +
						", created_by " +
						", city_id " +
						", country_id " +
						", prov_id " +
						", user_id " +
						", updated_by " +
						", updated_date )" +

						" VALUES ( " +
						"  :reprs_id " +
						", :skip_audit_trail " +
						", :reprs_address " +
						", :created_date " +
						", :reprs_email " +
						", :reprs_name " +
						", :reprs_no " +
						", :reprs_office " +
						", :reprs_phone " +
						", :status_flag " +
						", :reprs_zipcode " +
						", :created_by " +
						", :city_id " +
						", :country_id " +
						", :prov_id " +
						", :user_id " +
						", :updated_by " +
						", :updated_date " +
						")")

						.setParameter("reprs_id",reprs_id)
						.setParameter("skip_audit_trail", 0)
						.setParameter("reprs_address", reprs_addr)
						.setParameter("created_date", created_date)
						.setParameter("reprs_email", reprs_email)
						.setParameter("reprs_name", reprs_name)
						.setParameter("reprs_no", reprs_no)
						.setParameter("reprs_office", reprs_office)
						.setParameter("reprs_phone", reprs_phone)
						.setParameter("status_flag", status_flag)
						.setParameter("reprs_zipcode", reprs_zipcode)
						.setParameter("created_by", created_by)
						.setParameter("city_id", city_id)
						.setParameter("country_id", country_id)
						.setParameter("prov_id", prov_id)
						.setParameter("user_id", user_repId)
						.setParameter("updated_by", updated_by)
						.setParameter("updated_date", updated_date)
						.executeUpdate();
				entityManager.getTransaction().commit();
				entityManager.close();
				message = "Success";
			}
		} catch (HibernateException ex)
		{
			message = ex.getMessage();
		}
		return null;
	}

	public String searchOnebyLike(String table, String searchBy, String WhereCond , String WhereValue){

		String target = "";
		target = WhereValue.trim().toUpperCase();

		if (target != null){
			target = "%"+target+"%" ;
//			System.out.println(target);
		}

		try{
			String nativeQuery = "SELECT "+searchBy+" FROM "+table+" WHERE UPPER("+WhereCond+") LIKE '"+target+"' ";
//			System.out.println(nativeQuery);
			Query query = entityManager.createNativeQuery(nativeQuery);
//					.setParameter("p1", WhereCond)
//					.setParameter("p2",target );

			List<Object> result = query.getResultList();

			if (result != null)
				return result.get(0).toString() ;
		}
		catch (Exception e){

		}

		return null;
	}

	public int registerNewReps(String noReps,MRepresentative mRepresentative){
	    mRepresentative.setId(mRepresentative.getNo());
		try {
			saveOrUpdate(mRepresentative);
			return 1 ;
		}
		catch (Exception e){
			return 0;
		}

	}


	public int syncronizePDKKI(String idMreps, MRepresentative mRepresentative){
        try {

            String agent_name =  mRepresentative.getName();
            String add_street =  mRepresentative.getAddress();
            String office_telp =  mRepresentative.getPhone();
            String office_email = mRepresentative.getEmail();
            String office_name = mRepresentative.getOffice();
            String zipcode = mRepresentative.getZipCode();
            Timestamp updated_date = new Timestamp(System.currentTimeMillis());

            String city = mRepresentative.getmCity().getName();
			String id_city = "" ;
            if (city == null){
				id_city = "";
				} else {
                if (city.equalsIgnoreCase("")) {
                    id_city = "";
                } else {
                    try {
                        id_city = searchOnebyLike("M_CITY", "CITY_ID", "CITY_NAME", city);
                    }
                    catch (Exception e){
                        id_city = "";
                    }
                }
            }

			String province = mRepresentative.getmProvince().getName();
            String id_province = "" ;
            if (province == null) {
                id_province = "";
            } else {
                if (province.equalsIgnoreCase("")) {
                    id_province = "";
                } else{
                    try {
                        id_province = searchOnebyLike("M_PROVINCE", "PROV_ID", "PROV_NAME", province);
                    }
                    catch (Exception e){
                        id_province = "";
                    }
                }
            }

            // LEBIH BAIK STRING KOSONG DARIPADA NULL
			if (id_city == null)
				id_city ="" ;
			if (id_province == null)
				id_province = "";

            String nativeQuery = "UPDATE M_REPRS SET REPRS_NAME = :p1 " +
                    ", REPRS_EMAIL = :p2" +
                    ", REPRS_ADDRESS = :p3" +
                    ", REPRS_PHONE = :p4" +
                    ", REPRS_OFFICE = :p5" +
                    ", REPRS_ZIPCODE = :p6" +
					", PROV_ID = :p7" +
					", CITY_ID = :p8" +
					", COUNTRY_ID = :p9" +
					", UPDATED_DATE = :p10" +
					" WHERE REPRS_ID = :px";


			Query query = entityManager.createNativeQuery(nativeQuery)
                    .setParameter("p1", agent_name)
                    .setParameter("p2", office_email)
                    .setParameter("p3", add_street)
                    .setParameter("p4", office_telp)
                    .setParameter("p5", office_name)
                    .setParameter("p6", zipcode)
					.setParameter("p7", id_province)
					.setParameter("p8", id_city)
					.setParameter("p9", "ID")
					.setParameter("p10", updated_date)
                    .setParameter("px", idMreps) ;

            query.executeUpdate();
            return 1 ;

        } catch (NoResultException e) {
            logger.error(e.getMessage(), e);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);

        }
        return 0;
	}
}
