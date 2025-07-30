package com.fit2081.ter_34857613.NutriTrack.model.database.Entity

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.fit2081.ter_34857613.NutriTrack.model.database.DAO.FoodIntakeDao
import com.fit2081.ter_34857613.NutriTrack.model.database.DAO.FoodPreferencesDao
import com.fit2081.ter_34857613.NutriTrack.model.database.DAO.NutriCoachTipDao
import com.fit2081.ter_34857613.NutriTrack.model.database.DAO.PatientDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Room Database class for NutriTrack application.
 * Serves as the main access point to the SQLite database.
 */
@Database(
    entities = [Patient::class, FoodIntake::class, NutriCoachTip::class, PatientFoodPreferences::class],
    version = 2,
    exportSchema = false
)
abstract class NutriTrackDatabase : RoomDatabase() {
    
    // DAOs for database access
    abstract fun patientDao(): PatientDao
    abstract fun foodIntakeDao(): FoodIntakeDao
    abstract fun nutriCoachTipDao(): NutriCoachTipDao
    abstract fun foodPreferencesDao(): FoodPreferencesDao
    
    companion object {
        // Singleton instance
        @Volatile
        private var INSTANCE: NutriTrackDatabase? = null
        
        private val _isDatabaseReady = MutableStateFlow(false)
        val isDatabaseReady: StateFlow<Boolean> = _isDatabaseReady.asStateFlow()
        
        // Migration from version 1 to 2 - add food_preferences table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `food_preferences` (" +
                        "`userId` TEXT NOT NULL, " +
                        "`fruits` INTEGER NOT NULL DEFAULT 0, " +
                        "`vegetables` INTEGER NOT NULL DEFAULT 0, " +
                        "`grains` INTEGER NOT NULL DEFAULT 0, " +
                        "`redMeat` INTEGER NOT NULL DEFAULT 0, " +
                        "`seafood` INTEGER NOT NULL DEFAULT 0, " +
                        "`poultry` INTEGER NOT NULL DEFAULT 0, " +
                        "`fish` INTEGER NOT NULL DEFAULT 0, " +
                        "`eggs` INTEGER NOT NULL DEFAULT 0, " +
                        "`nutsSeeds` INTEGER NOT NULL DEFAULT 0, " +
                        "`personaId` INTEGER NOT NULL DEFAULT 0, " +
                        "`personaName` TEXT NOT NULL DEFAULT '', " +
                        "`biggestMealTime` TEXT NOT NULL DEFAULT '', " +
                        "`sleepTime` TEXT NOT NULL DEFAULT '', " +
                        "`wakeUpTime` TEXT NOT NULL DEFAULT '', " +
                        "PRIMARY KEY(`userId`)" +
                    ")"
                )
            }
        }
        
        // New flag for basic data ready state
        private val _isBasicDataReady = MutableStateFlow(false)
        val isBasicDataReady: StateFlow<Boolean> = _isBasicDataReady.asStateFlow()
        
        // Constants for database and preferences
        private const val DATABASE_NAME = "nutritrack_database"
        private const val CSV_FILENAME = "user.csv"
        private const val LOG_TAG = "NutriTrackDatabase"
        
        // Constants for CSV column names
        private object CsvColumns {
            const val PHONE_NUMBER = "PhoneNumber"
            const val USER_ID = "User_ID"
            const val SEX = "Sex"
            const val HEIFA_TOTAL_SCORE_MALE = "HEIFAtotalscoreMale"
            const val HEIFA_TOTAL_SCORE_FEMALE = "HEIFAtotalscoreFemale"
            const val DISCRETIONARY_HEIFA_SCORE_MALE = "DiscretionaryHEIFAscoreMale"
            const val DISCRETIONARY_HEIFA_SCORE_FEMALE = "DiscretionaryHEIFAscoreFemale"
            const val DISCRETIONARY_SERVE_SIZE = "Discretionaryservesize"
            const val VEGETABLES_HEIFA_SCORE_MALE = "VegetablesHEIFAscoreMale"
            const val VEGETABLES_HEIFA_SCORE_FEMALE = "VegetablesHEIFAscoreFemale"
            const val VEGETABLES_WITH_LEGUMES_ALLOCATED_SERVE_SIZE = "Vegetableswithlegumesallocatedservesize"
            const val LEGUMES_ALLOCATED_VEGETABLES = "LegumesallocatedVegetables"
            const val VEGETABLES_VARIATIONS_SCORE = "Vegetablesvariationsscore"
            const val VEGETABLES_CRUCIFEROUS = "VegetablesCruciferous"
            const val VEGETABLES_TUBE_AND_BULB = "VegetablesTuberandbulb"
            const val VEGETABLES_OTHER = "VegetablesOther"
            const val LEGUMES = "Legumes"
            const val VEGETABLES_GREEN = "VegetablesGreen"
            const val VEGETABLES_RED_AND_ORANGE = "VegetablesRedandorange"
            const val FRUIT_HEIFA_SCORE_MALE = "FruitHEIFAscoreMale"
            const val FRUIT_HEIFA_SCORE_FEMALE = "FruitHEIFAscoreFemale"
            const val FRUIT_SERVE_SIZE = "Fruitservesize"
            const val FRUIT_VARIATIONS_SCORE = "Fruitvariationsscore"
            const val FRUIT_POME = "FruitPome"
            const val FRUIT_TROPICAL_AND_SUBTROPICAL = "FruitTropicalandsubtropical"
            const val FRUIT_BERRY = "FruitBerry"
            const val FRUIT_STONE = "FruitStone"
            const val FRUIT_CITRUS = "FruitCitrus"
            const val FRUIT_OTHER = "FruitOther"
            const val GRAINS_AND_CEREALS_HEIFA_SCORE_MALE = "GrainsandcerealsHEIFAscoreMale"
            const val GRAINS_AND_CEREALS_HEIFA_SCORE_FEMALE = "GrainsandcerealsHEIFAscoreFemale"
            const val GRAINS_AND_CEREALS_SERVE_SIZE = "Grainsandcerealsservesize"
            const val GRAINS_AND_CEREALS_NON_WHOLE_GRAINS = "GrainsandcerealsNonwholegrains"
            const val WHOLE_GRAINS_HEIFA_SCORE_MALE = "WholegrainsHEIFAscoreMale"
            const val WHOLE_GRAINS_HEIFA_SCORE_FEMALE = "WholegrainsHEIFAscoreFemale"
            const val WHOLE_GRAINS_SERVE_SIZE = "Wholegrainsservesize"
            const val MEAT_AND_ALTERNATIVES_HEIFA_SCORE_MALE = "MeatandalternativesHEIFAscoreMale"
            const val MEAT_AND_ALTERNATIVES_HEIFA_SCORE_FEMALE = "MeatandalternativesHEIFAscoreFemale"
            const val MEAT_AND_ALTERNATIVES_WITH_LEGUMES_ALLOCATED_SERVE_SIZE = "Meatandalternativeswithlegumesallocatedservesize"
            const val LEGUMES_ALLOCATED_MEAT_AND_ALTERNATIVES = "LegumesallocatedMeatandalternatives"
            const val DAIRY_AND_ALTERNATIVES_HEIFA_SCORE_MALE = "DairyandalternativesHEIFAscoreMale"
            const val DAIRY_AND_ALTERNATIVES_HEIFA_SCORE_FEMALE = "DairyandalternativesHEIFAscoreFemale"
            const val DAIRY_AND_ALTERNATIVES_SERVE_SIZE = "Dairyandalternativesservesize"
            const val SODIUM_HEIFA_SCORE_MALE = "SodiumHEIFAscoreMale"
            const val SODIUM_HEIFA_SCORE_FEMALE = "SodiumHEIFAscoreFemale"
            const val SODIUM_MG_MILLIGRAMS = "Sodiummgmilligrams"
            const val ALCOHOL_HEIFA_SCORE_MALE = "AlcoholHEIFAscoreMale"
            const val ALCOHOL_HEIFA_SCORE_FEMALE = "AlcoholHEIFAscoreFemale"
            const val ALCOHOL_STANDARD_DRINKS = "Alcoholstandarddrinks"
            const val WATER_HEIFA_SCORE_MALE = "WaterHEIFAscoreMale"
            const val WATER_HEIFA_SCORE_FEMALE = "WaterHEIFAscoreFemale"
            const val WATER = "Water"
            const val WATER_TOTAL_ML = "WaterTotalmL"
            const val BEVERAGE_TOTAL_ML = "BeverageTotalmL"
            const val SUGAR_HEIFA_SCORE_MALE = "SugarHEIFAscoreMale"
            const val SUGAR_HEIFA_SCORE_FEMALE = "SugarHEIFAscoreFemale"
            const val SUGAR = "Sugar"
            const val SATURATED_FAT_HEIFA_SCORE_MALE = "SaturatedFatHEIFAscoreMale"
            const val SATURATED_FAT_HEIFA_SCORE_FEMALE = "SaturatedFatHEIFAscoreFemale"
            const val SATURATED_FAT = "SaturatedFat"
            const val UNSATURATED_FAT_HEIFA_SCORE_MALE = "UnsaturatedFatHEIFAscoreMale"
            const val UNSATURATED_FAT_HEIFA_SCORE_FEMALE = "UnsaturatedFatHEIFAscoreFemale"
            const val UNSATURATED_FAT_SERVE_SIZE = "UnsaturatedFatservesize"
        }
        
        // Minimum expected columns in CSV
        private const val MIN_COLUMNS = 63
        
        /**
         * Get the singleton database instance.
         * Creates the database the first time it's accessed.
         */
        fun getDatabase(context: Context): NutriTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutriTrackDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(DatabaseCallback(context.applicationContext))
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        private class DatabaseCallback(private val context: Context) : Callback() {
            private val initManager = DatabaseInitManager.getInstance(context.applicationContext)
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.d(LOG_TAG, "DATABASE: Created new database")
                
                // Always mark as uninitialized on fresh creation to ensure data is loaded
                initManager.resetInitializationStatus()
                
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        Log.d(LOG_TAG, "CSV: Loading initial data on new database creation")
                        loadInitialData(context, database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(LOG_TAG, "DATABASE: Opened existing database")
                
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (!initManager.isInitialized()) {
                            // Use Log.w for more visibility
                            Log.w("NutriTrackCSV", "⚠️ FIRST RUN: LOADING CSV DATA (first run or after reset)")
                            Log.d(LOG_TAG, "CSV: LOADING DATA FROM CSV (first run or reset)")
                        val patientCount = database.patientDao().getPatientCount()
                            Log.d(LOG_TAG, "DATABASE: Patient count before CSV load: $patientCount")
                            
                            // Load data from CSV
                            loadInitialData(context, database)
                            
                            // Mark as initialized to avoid loading on subsequent runs
                            initManager.markAsInitialized()
                            Log.d(LOG_TAG, "DATABASE: Marked as initialized for future runs")
                        } else {
                            // Use Log.w for more visibility
                            Log.w("NutriTrackCSV", "CACHED DATA: Using existing database (CSV import SKIPPED)")
                            Log.d(LOG_TAG, "CSV: SKIPPING CSV IMPORT (using existing database)")
                            
                            // Simply set flags to ready since database is already initialized
                            val patientCount = database.patientDao().getPatientCount()
                            Log.d(LOG_TAG, "DATABASE: Current patient count: $patientCount")
                            
                            _isBasicDataReady.value = true
                            _isDatabaseReady.value = true
                            Log.d(LOG_TAG, "DATABASE: Readiness flags set (no CSV loading needed)")
                        }
                    }
                }
            }
        }
        
        /**
         * Load data from CSV into the database using a two-phase approach.
         * Phase 1: Load essential data (userId, phoneNumber, sex) for quick UI display
         * Phase 2: Load all remaining data
         */
        private suspend fun loadInitialData(context: Context, database: NutriTrackDatabase) {
            try {
                Log.d(LOG_TAG, "CSV: Starting two-phase data loading")
                _isBasicDataReady.value = false
                _isDatabaseReady.value = false
                
                // PHASE 1: Load basic user data (userId, phoneNumber, sex)
                val basicPatients = mutableListOf<Patient>()
                
                try {
                    Log.d(LOG_TAG, "CSV PHASE 1: Loading essential patient data")
                    context.assets.open(CSV_FILENAME).use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                
                        // Read header
                val headerLine = reader.readLine()
                if (headerLine == null) {
                    Log.e(LOG_TAG, "Failed to read header line from CSV")
                    return
                }
                
                val headers = headerLine.split(",")
                Log.d(LOG_TAG, "CSV Header read: ${headers.size} columns")
                
                // Verify key columns exist
                if (!headers.contains(CsvColumns.USER_ID) || !headers.contains(CsvColumns.PHONE_NUMBER)) {
                    Log.e(LOG_TAG, "CSV is missing critical columns: USER_ID or PHONE_NUMBER")
                            return
                        }
                        
                        val headerMap = headers.withIndex().associate { (index, header) -> header to index }
                        
                        // Process rows for basic data
                        processCSVRows(reader, headerMap, CsvColumns, basicPatients, isBasicDataOnly = true)
                    }
                    
                    // Insert basic data
                    if (basicPatients.isNotEmpty()) {
                        database.runInTransaction {
                            runBlocking {
                                database.patientDao().deleteAllPatients() // Clear existing data
                                database.patientDao().insertAll(basicPatients)
                                Log.d(LOG_TAG, "CSV PHASE 1: Inserted ${basicPatients.size} basic patient records")
                            }
                        }
                        
                        // Signal that basic data is ready
                        _isBasicDataReady.value = true
                        Log.d(LOG_TAG, "CSV PHASE 1: Basic data ready, UI can now show essential information")
                    } else {
                        Log.e(LOG_TAG, "CSV PHASE 1: No basic patient data extracted from CSV")
                    }
                    
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "CSV PHASE 1: Error loading basic data: ${e.message}", e)
                    // Don't set _isBasicDataReady to true if there was an error
                }
                
                // PHASE 2: Load complete data for all patients
                val fullPatients = mutableListOf<Patient>()
                
                try {
                    Log.d(LOG_TAG, "CSV PHASE 2: Loading complete patient data")
                    context.assets.open(CSV_FILENAME).use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        
                        // Read header again
                        val headerLine = reader.readLine()
                        if (headerLine == null) {
                            Log.e(LOG_TAG, "PHASE 2: Failed to read header line from CSV")
                            return
                        }
                        
                        val headers = headerLine.split(",")
                        val headerMap = headers.withIndex().associate { (index, header) -> header to index }
                        
                        // Process rows for full data
                        processCSVRows(reader, headerMap, CsvColumns, fullPatients, isBasicDataOnly = false)
                    }
                    
                    // Insert/update full data
                    if (fullPatients.isNotEmpty()) {
                        database.runInTransaction {
                            runBlocking {
                                database.patientDao().insertAll(fullPatients) // This will replace the basic data
                                Log.d(LOG_TAG, "CSV PHASE 2: Updated ${fullPatients.size} full patient records")
                            }
                        }
                        
                        // Signal that all data is ready
                    _isDatabaseReady.value = true
                        Log.d(LOG_TAG, "CSV PHASE 2: Full data ready, UI can now show all information")
                } else {
                        Log.e(LOG_TAG, "CSV PHASE 2: No full patient data extracted from CSV")
                    }
                    
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "PHASE 2: Error loading full data: ${e.message}", e)
                    // Don't set _isDatabaseReady to true if there was an error
                }
                
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error in two-phase data loading: ${e.message}", e)
                e.printStackTrace()
            }
        }
        
        /**
         * Process CSV rows to extract patient data
         * @param isBasicDataOnly If true, only process userId, phoneNumber and sex
         */
        private fun processCSVRows(
            reader: BufferedReader,
            headerMap: Map<String, Int>,
            columnNames: Any,
            patients: MutableList<Patient>,
            isBasicDataOnly: Boolean = false
        ) {
            var line: String?
            var rowCount = 0
            var successCount = 0
            var failCount = 0
            
            // Use reflection to get the column name values
            val userIdColumn = columnNames::class.java.getDeclaredField("USER_ID").get(columnNames) as String
            val phoneNumberColumn = columnNames::class.java.getDeclaredField("PHONE_NUMBER").get(columnNames) as String
            
            while (reader.readLine().also { line = it } != null) {
                try {
                    // Split the CSV line into columns - using safe call operator
                    val columns = line?.split(",") ?: emptyList()
                    rowCount++
                    
                    // Create a Patient object from CSV data
                    if (columns.size >= MIN_COLUMNS) { // Ensure we have enough columns
                        val userId = getColumnValue(columns, headerMap, userIdColumn)
                        val phoneNumber = getColumnValue(columns, headerMap, phoneNumberColumn)
                        
                        if (userId.isNotEmpty() && phoneNumber.isNotEmpty()) {
                            if (isBasicDataOnly) {
                                // For basic data, only create patient with essential fields
                                val sex = getColumnValue(columns, headerMap, CsvColumns.SEX)
                                val patient = Patient(
                                    userId = userId,
                                    phoneNumber = phoneNumber,
                                    sex = sex
                                    // All other fields will use default null values
                                )
                                patients.add(patient)
                                successCount++
                            } else {
                                // For full data, create patient with all fields
                            val patient = Patient(
                                userId = userId,
                                phoneNumber = phoneNumber,
                                sex = getColumnValue(columns, headerMap, CsvColumns.SEX),
                                
                                // Parse all HEIFA scores using the header map
                                heifaTotalScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.HEIFA_TOTAL_SCORE_MALE),
                                heifaTotalScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.HEIFA_TOTAL_SCORE_FEMALE),
                                discretionaryHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.DISCRETIONARY_HEIFA_SCORE_MALE),
                                discretionaryHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.DISCRETIONARY_HEIFA_SCORE_FEMALE),
                                discretionaryServeSize = getColumnValueAsDouble(columns, headerMap, CsvColumns.DISCRETIONARY_SERVE_SIZE),
                                
                                // Vegetables scores
                                vegetablesHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.VEGETABLES_HEIFA_SCORE_MALE),
                                vegetablesHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.VEGETABLES_HEIFA_SCORE_FEMALE),
                                vegetablesWithLegumesAllocatedServeSize = getColumnValueAsDouble(columns, headerMap, CsvColumns.VEGETABLES_WITH_LEGUMES_ALLOCATED_SERVE_SIZE),
                                legumesAllocatedVegetables = getColumnValueAsDouble(columns, headerMap, CsvColumns.LEGUMES_ALLOCATED_VEGETABLES),
                                vegetablesVariationsScore = getColumnValueAsDouble(columns, headerMap, CsvColumns.VEGETABLES_VARIATIONS_SCORE),
                                vegetablesCruciferous = getColumnValueAsDouble(columns, headerMap, CsvColumns.VEGETABLES_CRUCIFEROUS),
                                vegetablesTubeAndBulb = getColumnValueAsDouble(columns, headerMap, CsvColumns.VEGETABLES_TUBE_AND_BULB),
                                vegetablesOther = getColumnValueAsDouble(columns, headerMap, CsvColumns.VEGETABLES_OTHER),
                                legumes = getColumnValueAsDouble(columns, headerMap, CsvColumns.LEGUMES),
                                vegetablesGreen = getColumnValueAsDouble(columns, headerMap, CsvColumns.VEGETABLES_GREEN),
                                vegetablesRedAndOrange = getColumnValueAsDouble(columns, headerMap, CsvColumns.VEGETABLES_RED_AND_ORANGE),
                                
                                // Fruit scores
                                fruitHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_HEIFA_SCORE_MALE),
                                fruitHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_HEIFA_SCORE_FEMALE),
                                fruitServeSize = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_SERVE_SIZE),
                                fruitVariationsScore = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_VARIATIONS_SCORE),
                                fruitPome = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_POME),
                                fruitTropicalAndSubtropical = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_TROPICAL_AND_SUBTROPICAL),
                                fruitBerry = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_BERRY),
                                fruitStone = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_STONE),
                                fruitCitrus = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_CITRUS),
                                fruitOther = getColumnValueAsDouble(columns, headerMap, CsvColumns.FRUIT_OTHER),
                                
                                // Grains and cereals scores
                                grainsAndCerealsHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.GRAINS_AND_CEREALS_HEIFA_SCORE_MALE),
                                grainsAndCerealsHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.GRAINS_AND_CEREALS_HEIFA_SCORE_FEMALE),
                                grainsAndCerealsServeSize = getColumnValueAsDouble(columns, headerMap, CsvColumns.GRAINS_AND_CEREALS_SERVE_SIZE),
                                grainsAndCerealsNonWholeGrains = getColumnValueAsDouble(columns, headerMap, CsvColumns.GRAINS_AND_CEREALS_NON_WHOLE_GRAINS),
                                wholeGrainsHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.WHOLE_GRAINS_HEIFA_SCORE_MALE),
                                wholeGrainsHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.WHOLE_GRAINS_HEIFA_SCORE_FEMALE),
                                wholeGrainsServeSize = getColumnValueAsDouble(columns, headerMap, CsvColumns.WHOLE_GRAINS_SERVE_SIZE),
                                
                                // Meat and alternatives scores
                                meatAndAlternativesHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.MEAT_AND_ALTERNATIVES_HEIFA_SCORE_MALE),
                                meatAndAlternativesHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.MEAT_AND_ALTERNATIVES_HEIFA_SCORE_FEMALE),
                                meatAndAlternativesWithLegumesAllocatedServeSize = getColumnValueAsDouble(columns, headerMap, CsvColumns.MEAT_AND_ALTERNATIVES_WITH_LEGUMES_ALLOCATED_SERVE_SIZE),
                                legumesAllocatedMeatAndAlternatives = getColumnValueAsDouble(columns, headerMap, CsvColumns.LEGUMES_ALLOCATED_MEAT_AND_ALTERNATIVES),
                                
                                // Dairy and alternatives scores
                                dairyAndAlternativesHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.DAIRY_AND_ALTERNATIVES_HEIFA_SCORE_MALE),
                                dairyAndAlternativesHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.DAIRY_AND_ALTERNATIVES_HEIFA_SCORE_FEMALE),
                                dairyAndAlternativesServeSize = getColumnValueAsDouble(columns, headerMap, CsvColumns.DAIRY_AND_ALTERNATIVES_SERVE_SIZE),
                                
                                // Sodium scores
                                sodiumHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.SODIUM_HEIFA_SCORE_MALE),
                                sodiumHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.SODIUM_HEIFA_SCORE_FEMALE),
                                sodiumMgMilligrams = getColumnValueAsDouble(columns, headerMap, CsvColumns.SODIUM_MG_MILLIGRAMS),
                                
                                // Alcohol scores
                                alcoholHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.ALCOHOL_HEIFA_SCORE_MALE),
                                alcoholHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.ALCOHOL_HEIFA_SCORE_FEMALE),
                                alcoholStandardDrinks = getColumnValueAsDouble(columns, headerMap, CsvColumns.ALCOHOL_STANDARD_DRINKS),
                                
                                // Water scores
                                waterHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.WATER_HEIFA_SCORE_MALE),
                                waterHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.WATER_HEIFA_SCORE_FEMALE),
                                water = getColumnValueAsDouble(columns, headerMap, CsvColumns.WATER),
                                waterTotalML = getColumnValueAsDouble(columns, headerMap, CsvColumns.WATER_TOTAL_ML),
                                beverageTotalML = getColumnValueAsDouble(columns, headerMap, CsvColumns.BEVERAGE_TOTAL_ML),
                                
                                // Sugar scores
                                sugarHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.SUGAR_HEIFA_SCORE_MALE),
                                sugarHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.SUGAR_HEIFA_SCORE_FEMALE),
                                sugar = getColumnValueAsDouble(columns, headerMap, CsvColumns.SUGAR),
                                
                                // Fat scores
                                saturatedFatHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.SATURATED_FAT_HEIFA_SCORE_MALE),
                                saturatedFatHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.SATURATED_FAT_HEIFA_SCORE_FEMALE),
                                saturatedFat = getColumnValueAsDouble(columns, headerMap, CsvColumns.SATURATED_FAT),
                                unsaturatedFatHeifaScoreMale = getColumnValueAsDouble(columns, headerMap, CsvColumns.UNSATURATED_FAT_HEIFA_SCORE_MALE),
                                unsaturatedFatHeifaScoreFemale = getColumnValueAsDouble(columns, headerMap, CsvColumns.UNSATURATED_FAT_HEIFA_SCORE_FEMALE),
                                unsaturatedFatServeSize = getColumnValueAsDouble(columns, headerMap, CsvColumns.UNSATURATED_FAT_SERVE_SIZE)
                            )
                            patients.add(patient)
                            successCount++
                            }
                        } else {
                            Log.w(LOG_TAG, "Row $rowCount: Missing userId or phoneNumber")
                            failCount++
                        }
                    } else {
                        Log.w(LOG_TAG, "Row $rowCount: Insufficient columns. Found ${columns.size}, need $MIN_COLUMNS")
                        failCount++
                    }
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Error processing row $rowCount: ${e.message}")
                    failCount++
                }
            }
            
            val phase = if (isBasicDataOnly) "basic" else "full"
            Log.d(LOG_TAG, "Processed $rowCount rows for $phase data (Success: $successCount, Failed: $failCount)")
        }
        
        /**
         * Helper method to get a column value as String safely
         */
        private fun getColumnValue(columns: List<String>, headerMap: Map<String, Int>, columnName: String): String {
            val index = headerMap[columnName] ?: return ""
            return if (index < columns.size) columns[index] else ""
        }
        
        /**
         * Helper method to get a column value as Double safely
         */
        private fun getColumnValueAsDouble(columns: List<String>, headerMap: Map<String, Int>, columnName: String): Double? {
            val index = headerMap[columnName] ?: return null
            return if (index < columns.size) columns[index].toDoubleOrNull() else null
        }
    }
} 