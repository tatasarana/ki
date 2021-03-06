package com.docotel.ki.enumeration;

import com.docotel.ki.model.master.MStatus;

public enum StatusEnum {
	TM_PERMOHONAN_BARU,
	TM_DATA_CAPTURE_SELESAI,
	TM_PUBLIKASI,
	M_PUBLICATION,
	M_DOC_DIST,
	TM_DISTRIBUSI_DOKUMEN,
	SYSTEM_STORED_IN_EDMS,
	IPT_DRAFT,
	IPT_PENGAJUAN_PERMOHONAN,
	IPT_PENGAJUAN_MADRID,
	TM_PASCA_PERMOHONAN,
	M_NEW_APPLICATION,
	EXPIRED,
	TM_PELAYANAN_TEKNIS,
	NOTYET,
	DONE,
	SELESAI_MASA_PENGUMUMAN,
	IPT_PENGAJUAN_MADRID_TR,
	IPT_PENGAJUAN_MADRID_RP,

	;

	public MStatus value() {
		return new MStatus(name());
	}
}
