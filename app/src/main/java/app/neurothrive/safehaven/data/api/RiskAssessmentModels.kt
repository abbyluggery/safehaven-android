package app.neurothrive.safehaven.data.api

import com.google.gson.annotations.SerializedName

/**
 * API Models for Risk Assessment Salesforce Integration
 *
 * Endpoints:
 * - POST /services/apexrest/safehaven/v1/risk-assessment/create
 * - GET  /services/apexrest/safehaven/v1/risk-assessment/{userId}
 */

/**
 * Request to create risk assessment
 */
data class CreateRiskAssessmentRequest(
    @SerializedName("userId")
    val userId: String
)

/**
 * Response from risk assessment API
 */
data class RiskAssessmentResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("errorMessage")
    val errorMessage: String? = null,

    @SerializedName("assessmentId")
    val assessmentId: String? = null,

    @SerializedName("riskLevel")
    val riskLevel: String? = null,

    @SerializedName("overallScore")
    val overallScore: Int? = null,

    @SerializedName("lethalityScore")
    val lethalityScore: Int? = null,

    @SerializedName("escalationScore")
    val escalationScore: Int? = null,

    @SerializedName("frequencyScore")
    val frequencyScore: Int? = null,

    @SerializedName("summary")
    val summary: String? = null,

    @SerializedName("recommendations")
    val recommendations: String? = null,

    @SerializedName("crisisResources")
    val crisisResources: String? = null,

    @SerializedName("detectedPatterns")
    val detectedPatterns: List<String>? = null,

    @SerializedName("assessmentDate")
    val assessmentDate: String? = null
)
