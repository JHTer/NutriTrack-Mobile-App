package com.fit2081.ter_34857613.NutriTrack.model.repository

import androidx.annotation.WorkerThread
import com.fit2081.ter_34857613.NutriTrack.model.database.Entity.Patient
import com.fit2081.ter_34857613.NutriTrack.model.database.DAO.PatientDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository for handling all [Patient]-related database operations.
 * This class acts as an abstraction layer between the [PatientDao] (database access)
 * and ViewModels or other data consumers. It provides methods for common CRUD operations
 * (Create, Read, Update, Delete) on patient data, as well as more specific queries for
 * aggregations (counts, averages) and filtered lists of patients.
 *
 * Most data access methods are `suspend` functions, intended to be called from coroutines
 * to avoid blocking the main thread. Many are also annotated with `@WorkerThread` to indicate
 * they perform blocking I/O operations and should not be called on the main thread directly.
 *
 * @property patientDao The Data Access Object (DAO) for [Patient] entities, injected into the repository.
 */
class PatientRepository(private val patientDao: PatientDao) {
    
    /**
     * A [Flow] that emits a list of all patients whenever the patient data changes in the database.
     * This allows for reactive updates in the UI or other observers.
     */
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatients()
    
    /**
     * Retrieves a list of all patients from the database synchronously.
     * This is a suspend function and should be called from a coroutine.
     * Useful for operations like AI analysis where a snapshot of all data is needed.
     *
     * @return A [List] of all [Patient] entities.
     */
    suspend fun getAllPatientsSync(): List<Patient> {
        return patientDao.getAllPatientsSync()
    }
    
    /**
     * Retrieves a single patient from the database by their unique user ID.
     * This is a suspend function.
     *
     * @param userId The unique identifier of the patient to retrieve.
     * @return The [Patient] entity if found, or `null` if no patient with the given ID exists.
     */
    suspend fun getPatient(userId: String): Patient? {
        return patientDao.getPatientById(userId)
    }
    
    /**
     * Inserts a new patient into the database.
     * This operation should be performed on a worker thread.
     *
     * @param patient The [Patient] entity to insert.
     */
    @WorkerThread
    suspend fun insert(patient: Patient) {
        patientDao.insert(patient)
    }
    
    /**
     * Updates an existing patient's information in the database.
     * This operation should be performed on a worker thread.
     *
     * @param patient The [Patient] entity to update. The entity's primary key (`userId`) will be
     *                used to find the existing record.
     */
    @WorkerThread
    suspend fun update(patient: Patient) {
        patientDao.update(patient)
    }
    
    /**
     * Deletes a patient from the database.
     * This operation should be performed on a worker thread.
     *
     * @param patient The [Patient] entity to delete. The entity's primary key (`userId`) will be
     *                used to identify the record for deletion.
     */
    @WorkerThread
    suspend fun delete(patient: Patient) {
        patientDao.delete(patient)
    }
    
    /**
     * Gets the total number of patients currently stored in the database.
     * This operation should be performed on a worker thread.
     *
     * @return An [Int] representing the total count of patients.
     */
    @WorkerThread
    suspend fun getTotalPatientCount(): Int {
        return patientDao.getPatientCount()
    }
    
    /**
     * Gets the number of male patients in the database.
     * This operation should be performed on a worker thread.
     *
     * @return An [Int] representing the count of patients where sex is "Male".
     */
    @WorkerThread
    suspend fun getMalePatientCount(): Int {
        return patientDao.getMalePatientCount()
    }
    
    /**
     * Gets the number of female patients in the database.
     * This operation should be performed on a worker thread.
     *
     * @return An [Int] representing the count of patients where sex is "Female".
     */
    @WorkerThread
    suspend fun getFemalePatientCount(): Int {
        return patientDao.getFemalePatientCount()
    }
    
    /**
     * Calculates the average HEIFA (Healthy Eating Index for Australian Adults) total score for all male patients.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average male HEIFA score, or `null` if there are no male patients
     *         or if their scores are not set.
     */
    @WorkerThread
    suspend fun getAverageHeifaScoreMale(): Double? {
        return patientDao.getAverageHeifaScoreMale()
    }
    
    /**
     * Calculates the average HEIFA total score for all female patients.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average female HEIFA score, or `null` if there are no female patients
     *         or if their scores are not set.
     */
    @WorkerThread
    suspend fun getAverageHeifaScoreFemale(): Double? {
        return patientDao.getAverageHeifaScoreFemale()
    }
    
    /**
     * Calculates the average score for a specific HEIFA component for male patients.
     * This operation should be performed on a worker thread.
     *
     * @param component The name of the HEIFA component (e.g., "vegetables", "fruit").
     * @return A [Double] representing the average score for that component among male patients,
     *         or `null` if data is insufficient.
     */
    @WorkerThread
    suspend fun getAverageComponentScoreMale(component: String): Double? {
        return patientDao.getAverageComponentScoreMale(component)
    }
    
    /**
     * Calculates the average score for a specific HEIFA component for female patients.
     * This operation should be performed on a worker thread.
     *
     * @param component The name of the HEIFA component (e.g., "vegetables", "fruit").
     * @return A [Double] representing the average score for that component among female patients,
     *         or `null` if data is insufficient.
     */
    @WorkerThread
    suspend fun getAverageComponentScoreFemale(component: String): Double? {
        return patientDao.getAverageComponentScoreFemale(component)
    }
    
    /**
     * Gets the count of patients who have a high water intake.
     * The specific criteria for "high water intake" are defined in the underlying [PatientDao] query.
     * This operation should be performed on a worker thread.
     *
     * @return An [Int] count of patients meeting the high water intake criteria.
     */
    @WorkerThread
    suspend fun getPatientCountWithHighWaterIntake(): Int {
        return patientDao.getPatientCountWithHighWaterIntake()
    }
    
    /**
     * Gets the count of male patients who have low vegetable variety.
     * The criteria for "low vegetable variety" are defined in the [PatientDao] query.
     * Get count of male patients with low vegetable variety
     */
    @WorkerThread
    suspend fun getMalePatientCountWithLowVegetableVariety(): Int {
        return patientDao.getMalePatientCountWithLowVegetableVariety()
    }
    
    /**
     * Get count of patients with high sodium intake
     */
    @WorkerThread
    suspend fun getPatientCountWithHighSodiumIntake(): Int {
        return patientDao.getPatientCountWithHighSodiumIntake()
    }
    
    /**
     * Get count of male patients with low unsaturated fat
     */
    @WorkerThread
    suspend fun getMalePatientCountWithLowUnsaturatedFat(): Int {
        return patientDao.getMalePatientCountWithLowUnsaturatedFat()
    }
    
    /**
     * Get patients by gender
     */
    @WorkerThread
    suspend fun getPatientsByGender(gender: String): List<Patient> {
        return patientDao.getPatientsByGender(gender)
    }
    
    /**
     * Get patients with HEIFA score above threshold
     */
    @WorkerThread
    suspend fun getPatientsWithScoreAbove(threshold: Double): List<Patient> {
        return patientDao.getPatientsWithScoreAbove(threshold)
    }
    
    /**
     * Get patients with HEIFA score below threshold
     */
    @WorkerThread
    suspend fun getPatientsWithScoreBelow(threshold: Double): List<Patient> {
        return patientDao.getPatientsWithScoreBelow(threshold)
    }
    
    /**
     * Get patients with component score above threshold
     */
    @WorkerThread
    suspend fun getPatientsWithComponentScoreAbove(component: String, threshold: Double): List<Patient> {
        return patientDao.getPatientsWithComponentScoreAbove(component, threshold)
    }
    
    /**
     * Get patients with component score below threshold
     */
    @WorkerThread
    suspend fun getPatientsWithComponentScoreBelow(component: String, threshold: Double): List<Patient> {
        return patientDao.getPatientsWithComponentScoreBelow(component, threshold)
    }
    
    /**
     * Get a patient by their ID.
     */
    suspend fun getPatientById(userId: String): Patient? {
        return patientDao.getPatientById(userId)
    }
    
    /**
     * Validate patient credentials for account claiming.
     * Returns the patient if userId and phoneNumber match, null otherwise.
     */
    suspend fun validatePatientCredentials(userId: String, phoneNumber: String): Patient? {
        return patientDao.getPatientByIdAndPhone(userId, phoneNumber)
    }
    
    /**
     * Authenticate a patient login.
     * Returns the patient if userId and password match, null otherwise.
     */
    suspend fun authenticatePatient(userId: String, password: String): Patient? {
        return patientDao.getPatientByIdAndPassword(userId, password)
    }
    
    /**
     * Check if a patient exists by userId.
     */
    suspend fun patientExists(userId: String): Boolean {
        return patientDao.patientExists(userId)
    }
    
    /**
     * Check if a patient has set a password (completed account claim).
     */
    suspend fun hasSetPassword(userId: String): Boolean {
        return patientDao.hasSetPassword(userId)
    }
    
    /**
     * Set or update a patient's name and password after account claiming.
     */
    suspend fun setPatientNameAndPassword(userId: String, name: String, password: String) {
        val patient = patientDao.getPatientById(userId)
        patient?.let {
            val updatedPatient = it.copy(name = name, password = password)
            patientDao.update(updatedPatient)
        }
    }

    /**
     * Updates a patient's phone number.
     *
     * @param userId The ID of the patient to update.
     * @param newPhoneNumber The new phone number.
     * @return True if the update was successful, false otherwise.
     */
    suspend fun updatePhoneNumber(userId: String, newPhoneNumber: String): Boolean {
        val patient = patientDao.getPatientById(userId)
        return if (patient != null) {
            val updatedPatient = patient.copy(phoneNumber = newPhoneNumber)
            patientDao.update(updatedPatient)
            true
        } else {
            false
        }
    }

    // --- START OF NEW METHODS FOR AVERAGE SERVE SIZES/INTAKE ---

    /**
     * Calculates the average daily vegetable serves for male patients.
     * This value is typically derived from patient-reported data or calculated HEIFA components.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average vegetable serves for male patients, or `null` if
     *         data is insufficient or not available.
     */
    @WorkerThread
    suspend fun getAverageVegetableServesMale(): Double? {
        return patientDao.getAverageVegetableServesMale()
    }

    /**
     * Calculates the average daily vegetable serves for female patients.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average vegetable serves for female patients, or `null` if
     *         data is insufficient or not available.
     */
    @WorkerThread
    suspend fun getAverageVegetableServesFemale(): Double? {
        return patientDao.getAverageVegetableServesFemale()
    }

    /**
     * Calculates the average daily fruit serves for male patients.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average fruit serves for male patients, or `null` if
     *         data is insufficient or not available.
     */
    @WorkerThread
    suspend fun getAverageFruitServesMale(): Double? {
        return patientDao.getAverageFruitServesMale()
    }

    /**
     * Calculates the average daily fruit serves for female patients.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average fruit serves for female patients, or `null` if
     *         data is insufficient or not available.
     */
    @WorkerThread
    suspend fun getAverageFruitServesFemale(): Double? {
        return patientDao.getAverageFruitServesFemale()
    }

    /**
     * Calculates the average daily protein serves for male patients.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average protein serves for male patients, or `null` if
     *         data is insufficient or not available.
     */
    @WorkerThread
    suspend fun getAverageProteinServesMale(): Double? {
        return patientDao.getAverageProteinServesMale()
    }

    /**
     * Calculates the average daily protein serves for female patients.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average protein serves for female patients, or `null` if
     *         data is insufficient or not available.
     */
    @WorkerThread
    suspend fun getAverageProteinServesFemale(): Double? {
        return patientDao.getAverageProteinServesFemale()
    }

    /**
     * Calculates the average daily water intake in milliliters (mL) for male patients.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average water intake for male patients in mL, or `null` if
     *         data is insufficient or not available.
     */
    @WorkerThread
    suspend fun getAverageWaterIntakeMLMale(): Double? {
        return patientDao.getAverageWaterIntakeMLMale()
    }

    /**
     * Calculates the average daily water intake in milliliters (mL) for female patients.
     * This operation should be performed on a worker thread.
     *
     * @return A [Double] representing the average water intake for female patients in mL, or `null` if
     *         data is insufficient or not available.
     */
    @WorkerThread
    suspend fun getAverageWaterIntakeMLFemale(): Double? {
        return patientDao.getAverageWaterIntakeMLFemale()
    }

} 