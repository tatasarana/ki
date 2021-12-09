package com.docotel.ki.audittrail;

public class EntityAuditTrailListener {
//	@PostPersist
//	public void postPersist(BaseModel target) {
//		if (target instanceof MUser && target.getCurrentId().equalsIgnoreCase(UserEnum.SYSTEM.name())) {
//			return;
//		}
//
//		if(target.isSkipLogAuditTrail() != null && target.isSkipLogAuditTrail()) {
//			return;
//		}
//
//		TxLogHistory txLogHistory = createLogHistory();
//		txLogHistory.setActivity("INSERT");
//		txLogHistory.setObjectId(target.getCurrentId());
//		txLogHistory.setObjectClassName(target.getClass().getName());
//		txLogHistory.setTableName(target.getClass().getAnnotation(Table.class).name());
//		txLogHistory.setOldData(null);
//		txLogHistory.setNewData(target.logAuditTrail());
//
//		persistLog(txLogHistory);
//	}
//
//	@PreUpdate
//	public void preUpdate(BaseModel target) {
////		if (target instanceof MUser && target.getCurrentId().equalsIgnoreCase(UserEnum.SYSTEM.name())) {
////			return;
////		}
//
//
//		if (target instanceof MUser) {
//			if(MUser.flagLoop) {
//				return;
//			} else {
//				MUser.flagLoop = true;
//			}
//		} else if (target instanceof MEmployee) {
//			if(MEmployee.flagLoop) {
//				return;
//			} else {
//				MEmployee.flagLoop = true;
//			}
//		} else {
//			if(MUser.flagLoop && MEmployee.flagLoop) {
//				MUser.flagLoop = MEmployee.flagLoop = false;
//				return;
//			}
//		}
//
//		if(target.isSkipLogAuditTrail() != null && target.isSkipLogAuditTrail()) {
//			return;
//		}
//		EntityManagerFactory entityManagerFactory = KiApplication.getBean(EntityManagerFactory.class);
//		BaseModel baseModel = entityManagerFactory.createEntityManager().find(target.getClass(), target.getCurrentId());
//		if (baseModel == null) {
//			return;
//		}
//
//
//		TxLogHistory txLogHistory = createLogHistory();
//		txLogHistory.setActivity("UPDATE");
//		txLogHistory.setObjectId(target.getCurrentId());
//		txLogHistory.setObjectClassName(target.getClass().getName());
//		txLogHistory.setTableName(target.getClass().getAnnotation(Table.class).name());
//		txLogHistory.setOldData(baseModel.logAuditTrail());
//		txLogHistory.setNewData(null);
//
//		persistLog(txLogHistory);
//	}
//
//	@PostUpdate
//	public void postUpdate(BaseModel target) {
//		if (target instanceof MUser && target.getCurrentId().equalsIgnoreCase(UserEnum.SYSTEM.name())) {
//			return;
//		}
//
//		if(target.isSkipLogAuditTrail() != null && target.isSkipLogAuditTrail()) {
//			return;
//		}
//
//		if (target instanceof MUser) {
//			if(MUser.flagLoop) {
//				return;
//			} else {
//				MUser.flagLoop = true;
//			}
//		} else if (target instanceof MEmployee) {
//			if(MEmployee.flagLoop) {
//				return;
//			} else {
//				MEmployee.flagLoop = true;
//			}
//		} else {
//			if(MUser.flagLoop && MEmployee.flagLoop) {
//				MUser.flagLoop = MEmployee.flagLoop = false;
//				return;
//			}
//		}
//
//		AuditTrailService auditTrailService = KiApplication.getBean(AuditTrailService.class);
//		TxLogHistory txLogHistory = auditTrailService.findOneLogHistory(target.getClass().getName(), target.getCurrentId());
//		if (txLogHistory != null) {
//			txLogHistory.setNewData(target.logAuditTrail());
//			auditTrailService.saveLogHistory(txLogHistory);
//		}
//	}
//
//	@PostRemove
//	public void postRemove(BaseModel target) {
//		if (target instanceof MUser && target.getCurrentId().equalsIgnoreCase(UserEnum.SYSTEM.name())) {
//			return;
//		}
//
//		if(target.isSkipLogAuditTrail() != null && target.isSkipLogAuditTrail()) {
//			return;
//		}
//
//		TxLogHistory txLogHistory = createLogHistory();
//		txLogHistory.setActivity("DELETE");
//		txLogHistory.setObjectId(target.getCurrentId());
//		txLogHistory.setObjectClassName(target.getClass().getName());
//		txLogHistory.setTableName(target.getClass().getAnnotation(Table.class).name());
//		txLogHistory.setOldData(target.logAuditTrail());
//		txLogHistory.setNewData(null);
//
//		persistLog(txLogHistory);
//	}
//
//	private TxLogHistory createLogHistory() {
//		MUser mUser = null;
//		try {
//			mUser = (MUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//		} catch (ClassCastException e)  {
//
//		} catch (NullPointerException nullE) {
//
//		}
//
//		TxLogHistory txLogHistory = new TxLogHistory();
//		if (mUser == null) {
//			txLogHistory.setUserName("SYSTEM");
//			txLogHistory.setName("SYSTEM");
//		} else {
//			txLogHistory.setUserName(mUser.getUsername());
//			if (mUser.isEmployee()) {
//				if (mUser.getmEmployee() != null) {
//					txLogHistory.setName(mUser.getmEmployee().getEmployeeName());
//					//txLogHistory.setSection(mUser.getmEmployee().getmSection().getName());
//				}
//			} else if (mUser.isReprs()) {
//				if (mUser.getmRepresentative() != null) {
//					txLogHistory.setName(mUser.getmRepresentative().getName());
//				}
//			}
//		}
//		return txLogHistory;
//	}
//
//	private void persistLog(TxLogHistory txLogHistory) {
//		AuditTrailService auditTrailService = KiApplication.getBean(AuditTrailService.class);
//		auditTrailService.saveLogHistory(txLogHistory);
//	}
}