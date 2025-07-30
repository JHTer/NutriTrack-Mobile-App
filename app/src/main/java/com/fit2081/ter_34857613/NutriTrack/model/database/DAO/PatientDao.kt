package com.fit2081.ter_34857613.NutriTrack.model.database.DAO

import androidx.room.*
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.Patient
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Patient entity.
 * Provides methods to query, insert, update and delete patients in the database.
 */
@Dao
interface PatientDao {
    /**
     * Insert a new patient into the database.
     * If there's a conflict (same userId), replace the existing record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)
    
    /**
     * Insert multiple patients at once.
     * Used when loading initial data from CSV.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(patients: List<Patient>)
    
    /**
     * Update an existing patient.
     */
    @Update
    suspend fun update(patient: Patient)
    
    /**
     * Delete a patient from the database.
     */
    @Delete
    suspend fun delete(patient: Patient)
    
    /**
     * Get a patient by their userId.
     */
    @Query("SELECT * FROM patients WHERE userId = :userId")
    suspend fun getPatientById(userId: String): Patient?
    
    /**
     * Get a patient by userId and phoneNumber for account claiming.
     */
    @Query("SELECT * FROM patients WHERE userId = :userId AND phoneNumber = :phoneNumber")
    suspend fun getPatientByIdAndPhone(userId: String, phoneNumber: String): Patient?

    /**
     * Get a patient by userId and password for login authentication.
     */
    @Query("SELECT * FROM patients WHERE userId = :userId AND password = :password")
    suspend fun getPatientByIdAndPassword(userId: String, password: String): Patient?
    
    /**
     * Get all patients in the database.
     * Uses LiveData for observing changes.
     */
    @Query("SELECT * FROM patients")
    fun getAllPatients(): Flow<List<Patient>>
    
    /**
     * Get all patients in the database synchronously.
     * Used for login screen to load user IDs.
     */
    @Query("SELECT * FROM patients")
    suspend fun getAllPatientsSync(): List<Patient>
    
    /**
     * Check if a patient exists by userId.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM patients WHERE userId = :userId)")
    suspend fun patientExists(userId: String): Boolean
    
    /**
     * Check if a patient has already set a password (completed account claim).
     */
    @Query("SELECT EXISTS(SELECT 1 FROM patients WHERE userId = :userId AND password != '')")
    suspend fun hasSetPassword(userId: String): Boolean
    
    /**
     * Get the average HEIFA score for male patients.
     */
    @Query("SELECT AVG(heifaTotalScoreMale) FROM patients WHERE sex = 'Male'")
    suspend fun getAverageHeifaScoreMale(): Double?
    
    /**
     * Get the average HEIFA score for female patients.
     */
    @Query("SELECT AVG(heifaTotalScoreFemale) FROM patients WHERE sex = 'Female'")
    suspend fun getAverageHeifaScoreFemale(): Double?
    
    /**
     * Delete all patients from the database.
     * Used when refreshing data from CSV.
     */
    @Query("DELETE FROM patients")
    suspend fun deleteAllPatients()
    
    /**
     * Get patient count
     */
    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int

    /**
     * Get male patient count
     */
    @Query("SELECT COUNT(*) FROM patients WHERE sex = 'Male'")
    suspend fun getMalePatientCount(): Int

    /**
     * Get female patient count
     */
    @Query("SELECT COUNT(*) FROM patients WHERE sex = 'Female'")
    suspend fun getFemalePatientCount(): Int
    
    /**
     * Get average component score for male patients based on the component name
     */
    @Query("""
        SELECT
            CASE :component
                WHEN 'vegetables' THEN AVG(vegetablesHeifaScoreMale)
                WHEN 'fruits' THEN AVG(fruitHeifaScoreMale)
                WHEN 'grains' THEN AVG(grainsAndCerealsHeifaScoreMale)
                WHEN 'protein' THEN AVG(meatAndAlternativesHeifaScoreMale)
                WHEN 'dairy' THEN AVG(dairyAndAlternativesHeifaScoreMale)
                WHEN 'water' THEN AVG(waterHeifaScoreMale)
                WHEN 'sodium' THEN AVG(sodiumHeifaScoreMale)
                WHEN 'unsaturated_fat' THEN AVG(unsaturatedFatHeifaScoreMale)
                ELSE 0
            END
        FROM patients WHERE sex = 'Male'
    """)
    suspend fun getAverageComponentScoreMale(component: String): Double?

    /**
     * Get average component score for female patients based on the component name
     */
    @Query("""
        SELECT
            CASE :component
                WHEN 'vegetables' THEN AVG(vegetablesHeifaScoreFemale)
                WHEN 'fruits' THEN AVG(fruitHeifaScoreFemale)
                WHEN 'grains' THEN AVG(grainsAndCerealsHeifaScoreFemale)
                WHEN 'protein' THEN AVG(meatAndAlternativesHeifaScoreFemale)
                WHEN 'dairy' THEN AVG(dairyAndAlternativesHeifaScoreFemale)
                WHEN 'water' THEN AVG(waterHeifaScoreFemale)
                WHEN 'sodium' THEN AVG(sodiumHeifaScoreFemale)
                WHEN 'unsaturated_fat' THEN AVG(unsaturatedFatHeifaScoreFemale)
                ELSE 0
            END
        FROM patients WHERE sex = 'Female'
    """)
    suspend fun getAverageComponentScoreFemale(component: String): Double?

    /**
     * Get count of patients with high water intake
     */
    @Query("SELECT COUNT(*) FROM patients WHERE waterTotalML > 2500")
    suspend fun getPatientCountWithHighWaterIntake(): Int

    /**
     * Get count of male patients with low vegetable variety
     */
    @Query("SELECT COUNT(*) FROM patients WHERE sex = 'Male' AND vegetablesVariationsScore < 3")
    suspend fun getMalePatientCountWithLowVegetableVariety(): Int

    /**
     * Get count of patients with high sodium intake
     */
    @Query("SELECT COUNT(*) FROM patients WHERE sodiumMgMilligrams > 2000")
    suspend fun getPatientCountWithHighSodiumIntake(): Int

    /**
     * Get count of male patients with low unsaturated fat
     */
    @Query("SELECT COUNT(*) FROM patients WHERE sex = 'Male' AND unsaturatedFatHeifaScoreMale < 1.0")
    suspend fun getMalePatientCountWithLowUnsaturatedFat(): Int

    /**
     * Get patients by gender
     */
    @Query("SELECT * FROM patients WHERE sex = :gender")
    suspend fun getPatientsByGender(gender: String): List<Patient>

    /**
     * Get patients with HEIFA score above threshold
     * Uses the appropriate score based on sex
     */
    @Query("""
        SELECT * FROM patients 
        WHERE (sex = 'Male' AND heifaTotalScoreMale > :threshold) 
        OR (sex = 'Female' AND heifaTotalScoreFemale > :threshold)
       
    """)
    suspend fun getPatientsWithScoreAbove(threshold: Double): List<Patient>

    /**
     * Get patients with HEIFA score below threshold
     * Uses the appropriate score based on sex
     */
    @Query("""
        SELECT * FROM patients 
        WHERE (sex = 'Male' AND heifaTotalScoreMale < :threshold) 
        OR (sex = 'Female' AND heifaTotalScoreFemale < :threshold)
       
    """)
    suspend fun getPatientsWithScoreBelow(threshold: Double): List<Patient>

    /**
     * Get patients with component score above threshold for males
     */
    @Query("""
        SELECT * FROM patients WHERE sex = 'Male' AND
        CASE :component
            WHEN 'vegetables' THEN vegetablesHeifaScoreMale
            WHEN 'fruits' THEN fruitHeifaScoreMale
            WHEN 'grains' THEN grainsAndCerealsHeifaScoreMale
            WHEN 'protein' THEN meatAndAlternativesHeifaScoreMale
            WHEN 'dairy' THEN dairyAndAlternativesHeifaScoreMale
            WHEN 'water' THEN waterHeifaScoreMale
            WHEN 'sodium' THEN sodiumHeifaScoreMale
            WHEN 'unsaturated_fat' THEN unsaturatedFatHeifaScoreMale
            ELSE 0
        END > :threshold
       
    """)
    suspend fun getPatientsWithComponentScoreAbove(component: String, threshold: Double): List<Patient>

    /**
     * Get patients with component score below threshold for males
     */
    @Query("""
        SELECT * FROM patients WHERE sex = 'Male' AND
        CASE :component
            WHEN 'vegetables' THEN vegetablesHeifaScoreMale
            WHEN 'fruits' THEN fruitHeifaScoreMale
            WHEN 'grains' THEN grainsAndCerealsHeifaScoreMale
            WHEN 'protein' THEN meatAndAlternativesHeifaScoreMale
            WHEN 'dairy' THEN dairyAndAlternativesHeifaScoreMale
            WHEN 'water' THEN waterHeifaScoreMale
            WHEN 'sodium' THEN sodiumHeifaScoreMale
            WHEN 'unsaturated_fat' THEN unsaturatedFatHeifaScoreMale
            ELSE 0
        END < :threshold
       
    """)
    suspend fun getPatientsWithComponentScoreBelow(component: String, threshold: Double): List<Patient>

    @Query("SELECT AVG(vegetablesWithLegumesAllocatedServeSize) FROM patients WHERE sex = 'Male'")
    suspend fun getAverageVegetableServesMale(): Double?

    @Query("SELECT AVG(vegetablesWithLegumesAllocatedServeSize) FROM patients WHERE sex = 'Female'")
    suspend fun getAverageVegetableServesFemale(): Double?

    @Query("SELECT AVG(fruitServeSize) FROM patients WHERE sex = 'Male'")
    suspend fun getAverageFruitServesMale(): Double?

    @Query("SELECT AVG(fruitServeSize) FROM patients WHERE sex = 'Female'")
    suspend fun getAverageFruitServesFemale(): Double?

    @Query("SELECT AVG(meatAndAlternativesWithLegumesAllocatedServeSize) FROM patients WHERE sex = 'Male'")
    suspend fun getAverageProteinServesMale(): Double?

    @Query("SELECT AVG(meatAndAlternativesWithLegumesAllocatedServeSize) FROM patients WHERE sex = 'Female'")
    suspend fun getAverageProteinServesFemale(): Double?

    @Query("SELECT AVG(waterTotalML) FROM patients WHERE sex = 'Male'")
    suspend fun getAverageWaterIntakeMLMale(): Double?

    @Query("SELECT AVG(waterTotalML) FROM patients WHERE sex = 'Female'")
    suspend fun getAverageWaterIntakeMLFemale(): Double?
} 