package com.GroceerCart.sa.db.db

import androidx.room.*

@Dao
interface UserDAO {
    @Insert
     suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM user")
    suspend fun deleteAll()

    @Query("SELECT * FROM user")
    suspend fun getAllUsers():List<User>
}