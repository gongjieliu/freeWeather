package com.weather.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val isCurrentLocation: Boolean = false,
    val displayOrder: Int = 0
)

@Dao
interface CityDao {
    @Query("SELECT * FROM cities ORDER BY displayOrder ASC")
    fun getAllCities(): Flow<List<CityEntity>>
    
    @Query("SELECT * FROM cities WHERE id = :id")
    suspend fun getCityById(id: String): CityEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)
    
    @Update
    suspend fun updateCity(city: CityEntity)
    
    @Delete
    suspend fun deleteCity(city: CityEntity)
    
    @Query("DELETE FROM cities WHERE id = :id")
    suspend fun deleteCityById(id: String)
    
    @Query("SELECT COUNT(*) FROM cities")
    suspend fun getCityCount(): Int
}
