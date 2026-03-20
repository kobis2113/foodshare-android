package com.foodshare.data.local

import androidx.room.*
import com.foodshare.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT id FROM users LIMIT 1")
    fun getCurrentUserIdSync(): String?

    @Query("DELETE FROM users")
    suspend fun clearAllUsers()
}
