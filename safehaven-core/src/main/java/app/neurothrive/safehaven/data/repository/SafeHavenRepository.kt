package app.neurothrive.safehaven.data.repository

import app.neurothrive.safehaven.data.database.dao.*
import app.neurothrive.safehaven.data.database.entities.*
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafeHavenRepository @Inject constructor(
    private val profileDao: SafeHavenProfileDao,
    private val incidentReportDao: IncidentReportDao,
    private val evidenceItemDao: EvidenceItemDao,
    private val verifiedDocumentDao: VerifiedDocumentDao,
    private val legalResourceDao: LegalResourceDao,
    private val survivorProfileDao: SurvivorProfileDao
) {
    
    // ========== SafeHavenProfile ==========
    
    fun getProfileFlow(userId: String): Flow<SafeHavenProfile?> {
        return profileDao.getProfileFlow(userId)
    }
    
    suspend fun getProfile(userId: String): SafeHavenProfile? {
        return profileDao.getProfile(userId)
    }
    
    suspend fun saveProfile(profile: SafeHavenProfile) {
        profileDao.insert(profile)
    }
    
    suspend fun updateProfile(profile: SafeHavenProfile) {
        profileDao.update(profile)
    }
    
    suspend fun deleteProfile(userId: String) {
        profileDao.deleteProfile(userId)
    }
    
    // ========== IncidentReport ==========
    
    fun getAllIncidentsFlow(userId: String): Flow<List<IncidentReport>> {
        return incidentReportDao.getAllFlow(userId)
    }
    
    suspend fun getAllIncidents(userId: String): List<IncidentReport> {
        return incidentReportDao.getAll(userId)
    }
    
    suspend fun saveIncident(report: IncidentReport) {
        // Encrypt sensitive fields before saving
        val encrypted = report.copy(
            descriptionEncrypted = if (report.descriptionEncrypted.isNotEmpty()) 
                SafeHavenCrypto.encrypt(report.descriptionEncrypted) 
            else "",
            witnessesEncrypted = report.witnessesEncrypted?.let { 
                if (it.isNotEmpty()) SafeHavenCrypto.encrypt(it) else null 
            },
            injuriesEncrypted = report.injuriesEncrypted?.let { 
                if (it.isNotEmpty()) SafeHavenCrypto.encrypt(it) else null 
            }
        )
        incidentReportDao.insert(encrypted)
    }
    
    suspend fun updateIncident(report: IncidentReport) {
        incidentReportDao.update(report)
    }
    
    suspend fun deleteIncident(report: IncidentReport) {
        incidentReportDao.delete(report)
    }
    
    suspend fun deleteAllIncidents(userId: String) {
        incidentReportDao.deleteAllForUser(userId)
    }
    
    // ========== EvidenceItem ==========
    
    fun getAllEvidenceFlow(userId: String): Flow<List<EvidenceItem>> {
        return evidenceItemDao.getAllFlow(userId)
    }
    
    suspend fun getAllEvidence(userId: String): List<EvidenceItem> {
        return evidenceItemDao.getAll(userId)
    }
    
    suspend fun getEvidenceByIncident(reportId: String): List<EvidenceItem> {
        return evidenceItemDao.getByIncident(reportId)
    }
    
    suspend fun saveEvidence(item: EvidenceItem) {
        evidenceItemDao.insert(item)
    }
    
    suspend fun updateEvidence(item: EvidenceItem) {
        evidenceItemDao.update(item)
    }
    
    suspend fun deleteEvidence(item: EvidenceItem) {
        evidenceItemDao.delete(item)
    }
    
    suspend fun deleteAllEvidence(userId: String) {
        evidenceItemDao.deleteAllForUser(userId)
    }
    
    // ========== VerifiedDocument ==========
    
    fun getAllDocumentsFlow(userId: String): Flow<List<VerifiedDocument>> {
        return verifiedDocumentDao.getAllFlow(userId)
    }
    
    suspend fun getAllDocuments(userId: String): List<VerifiedDocument> {
        return verifiedDocumentDao.getAll(userId)
    }
    
    suspend fun getDocumentByHash(hash: String): VerifiedDocument? {
        return verifiedDocumentDao.getByHash(hash)
    }
    
    suspend fun saveDocument(document: VerifiedDocument) {
        verifiedDocumentDao.insert(document)
    }
    
    suspend fun updateDocument(document: VerifiedDocument) {
        verifiedDocumentDao.update(document)
    }
    
    suspend fun deleteDocument(document: VerifiedDocument) {
        verifiedDocumentDao.delete(document)
    }
    
    suspend fun deleteAllDocuments(userId: String) {
        verifiedDocumentDao.deleteAllForUser(userId)
    }
    
    // ========== LegalResource ==========
    
    suspend fun getResourcesByType(type: String): List<LegalResource> {
        return legalResourceDao.getByType(type)
    }
    
    suspend fun getResourcesFiltered(
        type: String,
        lgbtq: Boolean,
        bipoc: Boolean,
        male: Boolean,
        undoc: Boolean
    ): List<LegalResource> {
        return legalResourceDao.getFiltered(type, lgbtq, bipoc, male, undoc)
    }
    
    suspend fun saveResource(resource: LegalResource) {
        legalResourceDao.insert(resource)
    }
    
    suspend fun saveAllResources(resources: List<LegalResource>) {
        legalResourceDao.insertAll(resources)
    }
    
    // ========== SurvivorProfile ==========
    
    fun getSurvivorProfileFlow(userId: String): Flow<SurvivorProfile?> {
        return survivorProfileDao.getProfileFlow(userId)
    }
    
    suspend fun getSurvivorProfile(userId: String): SurvivorProfile? {
        return survivorProfileDao.getProfile(userId)
    }
    
    suspend fun saveSurvivorProfile(profile: SurvivorProfile) {
        survivorProfileDao.insert(profile)
    }
    
    suspend fun updateSurvivorProfile(profile: SurvivorProfile) {
        survivorProfileDao.update(profile)
    }
    
    suspend fun deleteSurvivorProfile(userId: String) {
        survivorProfileDao.deleteProfile(userId)
    }
}
