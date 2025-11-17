package app.neurothrive.safehaven.data.repository

import app.neurothrive.safehaven.data.database.dao.*
import app.neurothrive.safehaven.data.database.entities.*
import app.neurothrive.safehaven.util.crypto.SafeHavenCrypto
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SafeHaven Repository
 * Central data layer for the app
 *
 * CRITICAL: Handles encryption/decryption transparently
 */
@Singleton
class SafeHavenRepository @Inject constructor(
    private val profileDao: SafeHavenProfileDao,
    private val incidentReportDao: IncidentReportDao,
    private val verifiedDocumentDao: VerifiedDocumentDao,
    private val evidenceItemDao: EvidenceItemDao,
    private val legalResourceDao: LegalResourceDao,
    private val survivorProfileDao: SurvivorProfileDao
) {

    // ==================== SafeHaven Profile ====================

    suspend fun getProfile(userId: String): SafeHavenProfile? {
        return profileDao.getProfile(userId)
    }

    fun getProfileFlow(userId: String): Flow<SafeHavenProfile?> {
        return profileDao.getProfileFlow(userId)
    }

    suspend fun saveProfile(profile: SafeHavenProfile) {
        profileDao.insert(profile)
    }

    suspend fun updateProfile(profile: SafeHavenProfile) {
        profileDao.update(profile)
    }

    suspend fun deleteProfile(userId: String) {
        profileDao.deleteByUserId(userId)
    }

    // ==================== Incident Reports ====================

    suspend fun getAllIncidents(userId: String): List<IncidentReport> {
        return incidentReportDao.getAll(userId)
    }

    fun getAllIncidentsFlow(userId: String): Flow<List<IncidentReport>> {
        return incidentReportDao.getAllFlow(userId)
    }

    suspend fun saveIncident(report: IncidentReport) {
        // Encrypt sensitive fields before saving
        val encrypted = report.copy(
            descriptionEncrypted = if (report.descriptionEncrypted.isNotBlank()) {
                SafeHavenCrypto.encrypt(report.descriptionEncrypted)
            } else {
                ""
            },
            witnessesEncrypted = report.witnessesEncrypted?.let {
                if (it.isNotBlank()) SafeHavenCrypto.encrypt(it) else null
            },
            injuriesEncrypted = report.injuriesEncrypted?.let {
                if (it.isNotBlank()) SafeHavenCrypto.encrypt(it) else null
            }
        )
        incidentReportDao.insert(encrypted)
    }

    suspend fun deleteIncident(report: IncidentReport) {
        incidentReportDao.delete(report)
    }

    suspend fun deleteAllIncidents(userId: String) {
        incidentReportDao.deleteAll(userId)
    }

    // ==================== Evidence Items ====================

    suspend fun getAllEvidence(userId: String): List<EvidenceItem> {
        return evidenceItemDao.getAll(userId)
    }

    fun getAllEvidenceFlow(userId: String): Flow<List<EvidenceItem>> {
        return evidenceItemDao.getAllFlow(userId)
    }

    suspend fun saveEvidence(item: EvidenceItem) {
        evidenceItemDao.insert(item)
    }

    suspend fun deleteEvidence(item: EvidenceItem) {
        evidenceItemDao.delete(item)
    }

    suspend fun deleteAllEvidence(userId: String) {
        evidenceItemDao.deleteAll(userId)
    }

    // ==================== Verified Documents ====================

    suspend fun getAllDocuments(userId: String): List<VerifiedDocument> {
        return verifiedDocumentDao.getAll(userId)
    }

    fun getAllDocumentsFlow(userId: String): Flow<List<VerifiedDocument>> {
        return verifiedDocumentDao.getAllFlow(userId)
    }

    suspend fun saveDocument(document: VerifiedDocument) {
        verifiedDocumentDao.insert(document)
    }

    suspend fun deleteDocument(document: VerifiedDocument) {
        verifiedDocumentDao.delete(document)
    }

    suspend fun deleteAllDocuments(userId: String) {
        verifiedDocumentDao.deleteAll(userId)
    }

    // ==================== Legal Resources ====================

    suspend fun getResourcesByType(type: String): List<LegalResource> {
        return legalResourceDao.getByType(type)
    }

    suspend fun getFilteredResources(
        type: String,
        lgbtq: Boolean,
        trans: Boolean,
        bipoc: Boolean,
        male: Boolean,
        undoc: Boolean,
        disabled: Boolean,
        deaf: Boolean
    ): List<LegalResource> {
        return legalResourceDao.getFiltered(type, lgbtq, trans, bipoc, male, undoc, disabled, deaf)
    }

    suspend fun saveResources(resources: List<LegalResource>) {
        legalResourceDao.insertAll(resources)
    }

    suspend fun getResourceCount(): Int {
        return legalResourceDao.getCount()
    }

    // ==================== Survivor Profile ====================

    suspend fun getSurvivorProfile(userId: String): SurvivorProfile? {
        return survivorProfileDao.getByUserId(userId)
    }

    suspend fun saveSurvivorProfile(profile: SurvivorProfile) {
        survivorProfileDao.insert(profile)
    }

    suspend fun updateSurvivorProfile(profile: SurvivorProfile) {
        survivorProfileDao.update(profile)
    }
}
