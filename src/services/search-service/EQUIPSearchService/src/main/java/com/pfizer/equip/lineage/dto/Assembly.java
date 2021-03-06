package com.pfizer.equip.lineage.dto;

import java.time.Instant;

public class Assembly {
	private String id;
	protected String nodeType = "Assembly";
	private String assemblyType;
	private String name;
	private String[] studyIds;
	private String equipId;
	private String[] parentIds;
	private String[] assemblyIds;
	private String[] dataframeIds;
	private Comment[] comments;
	private Metadata[] metadata;
	private ReportingEventStatusChangeWorkflow[] reportingEventStatusChangeWorkflows;
	private boolean isLocked;
	private String lockedByUser;
	private boolean deleteFlag;
	private boolean versionSuperSeded;
	private String dataStatus;
	private String promotionStatus;
	private String qcStatus;
	private boolean published;
	private boolean released;
	private Long versionNumber;
	private Instant created;
	private String createdBy;
	private Instant modifiedDate;
	private String modifiedBy;
	private boolean isCommitted;	// *
	private boolean obsoleteFlag;
	private Boolean atrIsCurrent;
	private String itemType;
	private String subType;
	private String description;
	private String[] protocolIds;
	private String[] projectIds;
	private String[] programIds;
	private String[] publishedItemIds;
	private String loadStatus;
	private String[] reportingItemIds;
	private ReportingEventItem[] reportingItems;
	

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	public String getAssemblyType() {
		return assemblyType;
	}
	public void setAssemblyType(String assemblyType) {
		this.assemblyType = assemblyType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String[] getStudyIds() {
		return studyIds;
	}
	public void setStudyIds(String[] studyIds) {
		this.studyIds = studyIds;
	}
	public String getEquipId() {
		return equipId;
	}
	public void setEquipId(String equipId) {
		this.equipId = equipId;
	}
	public String[] getParentIds() {
		return parentIds;
	}
	public void setParentIds(String[] parentIds) {
		this.parentIds = parentIds;
	}
	public String[] getDataframeIds() {
		return dataframeIds;
	}
	public void setDataframeIds(String[] dataframeIds) {
		this.dataframeIds = dataframeIds;
	}
	public String[] getAssemblyIds() {
		return assemblyIds;
	}
	public void setAssemblyIds(String[] assemblyIds) {
		this.assemblyIds = assemblyIds;
	}
	public Comment[] getComments() {
		return comments;
	}
	public void setComments(Comment[] comments) {
		this.comments = comments;
	}
	public Metadata[] getMetadata() {
		return metadata;
	}
	public void setMetadata(Metadata[] metadata) {
		this.metadata = metadata;
	}
	public ReportingEventStatusChangeWorkflow[] getReportingEventStatusChangeWorkflows() {
		return reportingEventStatusChangeWorkflows;
	}
	public void setReportingEventStatusChangeWorkflows(
			ReportingEventStatusChangeWorkflow[] reportingEventStatusChangeWorkflows) {
		this.reportingEventStatusChangeWorkflows = reportingEventStatusChangeWorkflows;
	}
	public boolean isLocked() {
		return isLocked;
	}
	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}
	public String getLockedByUser() {
		return lockedByUser;
	}
	public void setLockedByUser(String lockedByUser) {
		this.lockedByUser = lockedByUser;
	}
	public boolean getDeleteFlag() {
		return deleteFlag;
	}
	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	public boolean isVersionSuperSeded() {
		return versionSuperSeded;
	}
	public void setVersionSuperSeded(boolean versionSuperSeded) {
		this.versionSuperSeded = versionSuperSeded;
	}
	public String getDataStatus() {
		return dataStatus;
	}
	public void setDataStatus(String dataStatus) {
		this.dataStatus = dataStatus;
	}
	public String getPromotionStatus() {
		return promotionStatus;
	}
	public void setPromotionStatus(String promotionStatus) {
		this.promotionStatus = promotionStatus;
	}
	public String getQcStatus() {
		return qcStatus;
	}
	public void setQcStatus(String qcStatus) {
		this.qcStatus = qcStatus;
	}
	public boolean getPublished() {
		return published;
	}
	public void setPublished(boolean published) {
		this.published = published;
	}
	public boolean getReleased() {
		return released;
	}
	public void setReleased(boolean released) {
		this.released = released;
	}
	public Long getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(Long versionNumber) {
		this.versionNumber = versionNumber;
	}
	public Instant getCreated() {
		return created;
	}
	public void setCreated(Instant created) {
		this.created = created;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Instant getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Instant modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public boolean isCommitted() {
		return isCommitted;
	}
	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}
	public boolean isObsoleteFlag() {
		return obsoleteFlag;
	}
	public void setObsoleteFlag(boolean obsoleteFlag) {
		this.obsoleteFlag = obsoleteFlag;
	}
	public Boolean getAtrIsCurrent() {
		return atrIsCurrent;
	}
	public void setAtrIsCurrent(Boolean atrIsCurrent) {
		this.atrIsCurrent = atrIsCurrent;
	}
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String[] getProtocolIds() {
		return protocolIds;
	}
	public void setProtocolIds(String[] protocolIds) {
		this.protocolIds = protocolIds;
	}
	public String[] getProjectIds() {
		return projectIds;
	}
	public void setProjectIds(String[] projectIds) {
		this.projectIds = projectIds;
	}
	public String[] getProgramIds() {
		return programIds;
	}
	public void setProgramIds(String[] programIds) {
		this.programIds = programIds;
	}
	public String getItemType() {
		return itemType;
	}
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}
	public String[] getPublishedItemIds() {
		return publishedItemIds;
	}
	public void setPublishedItemIds(String[] publishedItemIds) {
		this.publishedItemIds = publishedItemIds;
	}
	public String getLoadStatus() {
		return loadStatus;
	}
	public void setLoadStatus(String loadStatus) {
		this.loadStatus = loadStatus;
	}
	public String[] getReportingItemIds() {
		return reportingItemIds;
	}
	public void setReportingItemIds(String[] reportingItemIds) {
		this.reportingItemIds = reportingItemIds;
	}
	public ReportingEventItem[] getReportingItems() {
		return reportingItems;
	}
	public void setReportingItems(ReportingEventItem[] reportingItems) {
		this.reportingItems = reportingItems;
	}
}
