package com.docotel.ki.enumeration;

import com.docotel.ki.model.master.MWorkflowProcess;

public enum WfProcessEnum {
	TM_PERMOHONAN_BARU,
	TM_DATA_CAPTURE_SELESAI,
	TM_PUBLIKASI,
	M_PUBLICATION,
	M_DOC_DIST,
	TM_DISTRIBUSI_DOKUMEN,
	SYSTEM_STORED_IN_EDMS,
	IPT_DRAFT,
	IPT_PENGAJUAN_PERMOHONAN,
	TM_PASCA_PERMOHONAN,
	M_NEW_APPLICATION,
	EXPIRED,
	TM_PELAYANAN_TEKNIS,
	SELESAI_MASA_PENGUMUMAN
	;

	public MWorkflowProcess value() {
		return new MWorkflowProcess(name());
	}
}
