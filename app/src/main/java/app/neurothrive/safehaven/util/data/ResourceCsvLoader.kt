package app.neurothrive.safehaven.util.data

import android.content.Context
import app.neurothrive.safehaven.data.database.entities.LegalResource
import app.neurothrive.safehaven.data.repository.SafeHavenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CSV Loader for Legal Resources
 *
 * Loads the legal_resources.csv file from assets and populates the database.
 * Should be called once on first app launch.
 *
 * CSV Format:
 * - Header row with column names
 * - Data rows with comma-separated values
 * - JSON arrays in quotes for services, languages, culturally specific
 */
@Singleton
class ResourceCsvLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SafeHavenRepository
) {

    /**
     * Load all resources from CSV into database
     * Returns number of resources loaded
     */
    suspend fun loadResources(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting CSV resource load...")

            // Check if resources already loaded
            val existingCount = repository.getResourceCount()
            if (existingCount > 0) {
                Timber.d("Resources already loaded ($existingCount resources)")
                return@withContext Result.success(existingCount)
            }

            val resources = mutableListOf<LegalResource>()

            // Open CSV file from assets
            val csvFile = context.assets.open("legal_resources.csv")
            val reader = BufferedReader(InputStreamReader(csvFile))

            // Skip header row
            reader.readLine()

            // Parse each line
            var line: String?
            var lineNumber = 2 // Start at 2 (after header)

            while (reader.readLine().also { line = it } != null) {
                try {
                    val resource = parseCsvLine(line!!)
                    resources.add(resource)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse line $lineNumber: $line")
                }
                lineNumber++
            }

            reader.close()

            // Insert all resources into database
            repository.saveResources(resources)

            Timber.d("Successfully loaded ${resources.size} resources")

            Result.success(resources.size)

        } catch (e: Exception) {
            Timber.e(e, "Failed to load resources from CSV")
            Result.failure(e)
        }
    }

    /**
     * Parse a single CSV line into a LegalResource
     */
    private fun parseCsvLine(line: String): LegalResource {
        // Split by comma, but respect quotes
        val fields = parseCSVLine(line)

        return LegalResource(
            id = fields[0],
            resourceType = fields[1],
            organizationName = fields[2],
            phone = fields[3].takeIf { it != "N/A" },
            website = fields[4].takeIf { it != "N/A" },
            email = fields[5].takeIf { it != "N/A" },
            address = fields[6].takeIf { it != "N/A" },
            city = fields[7],
            state = fields[8],
            zipCode = fields[9].takeIf { it.isNotBlank() },
            latitude = fields[10].toDoubleOrNull(),
            longitude = fields[11].toDoubleOrNull(),
            servicesJson = fields[12],
            hours = fields[13].takeIf { it.isNotBlank() },
            is24_7 = fields[14] == "1",
            servesLGBTQIA = fields[15] == "1",
            lgbtqSpecialized = fields[16] == "1",
            transInclusive = fields[17] == "1",
            nonBinaryInclusive = fields[18] == "1",
            servesBIPOC = fields[19] == "1",
            bipocLed = fields[20] == "1",
            culturallySpecificJson = fields[21].takeIf { it != "[]" },
            servesMaleIdentifying = fields[22] == "1",
            servesUndocumented = fields[23] == "1",
            uVisaSupport = fields[24] == "1",
            vawaSupport = fields[25] == "1",
            noICEContact = fields[26] == "1",
            servesDisabled = fields[27] == "1",
            wheelchairAccessible = fields[28] == "1",
            servesDeaf = fields[29] == "1",
            aslInterpreter = fields[30] == "1",
            languagesJson = fields[31],
            isFree = fields[32] == "1",
            slidingScale = fields[33] == "1",
            // NEW FIELDS: Dependent Care (fields 34-38)
            acceptsChildren = fields[34] == "1",
            childAgeRestrictions = fields[35].takeIf { it.isNotBlank() && it != "N/A" },
            acceptsDependentAdults = fields[36] == "1",
            acceptsPets = fields[37] == "1",
            hasChildcare = fields[38] == "1",
            // NEW FIELDS: Vulnerable Populations (fields 39-45)
            servesPregnant = fields[39] == "1",
            servesSubstanceUse = fields[40] == "1",
            servesTeenDating = fields[41] == "1",
            servesElderAbuse = fields[42] == "1",
            servesTrafficking = fields[43] == "1",
            servesTBI = fields[44] == "1",
            acceptsCriminalRecord = fields[45] == "1",
            // NEW FIELDS: Medical/Mental Health (fields 46-48)
            hasMedicalSupport = fields[46] == "1",
            hasMentalHealthCounseling = fields[47] == "1",
            traumaInformedCare = fields[48] == "1",
            // NEW FIELDS: Transportation Support (fields 49-55) - CRITICAL FOR RURAL
            providesTransportation = fields[49] == "1",
            transportationRadius = fields[50].takeIf { it.isNotBlank() && it != "N/A" },
            transportationPartnerships = fields[51].takeIf { it.isNotBlank() && it != "[]" },
            offersVirtualServices = fields[52] == "1",
            gasVoucherProgram = fields[53] == "1",
            relocationAssistance = fields[54] == "1",
            greyhoundHomeFreePartner = fields[55] == "1",
            // Verification fields (shifted from 49-52 to 56-59)
            lastVerified = fields[56].toLong(),
            verifiedBy = fields[57].takeIf { it.isNotBlank() },
            rating = fields[58].toFloatOrNull(),
            reviewCount = fields[59].toIntOrNull() ?: 0
        )
    }

    /**
     * Parse CSV line respecting quoted fields
     */
    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
        }

        // Add last field
        result.add(current.toString())

        return result
    }
}
