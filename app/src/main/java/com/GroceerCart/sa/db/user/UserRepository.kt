package com.GroceerCart.sa.db.db

class UserRepository (private val dao: UserDAO) {

  //  val users = dao.getAllUsers()

    suspend fun getAllUsers():List<User>{
        return dao.getAllUsers()
    }

     suspend fun insertUser(user: User){
        dao.insertUser(user)
    }

    suspend fun updateUser(user: User){
        dao.updateUser(user)
    }

    suspend fun deleteUser(user: User){
        dao.deleteUser(user)
    }

    suspend  fun deleteAllUsers(){
        dao.deleteAll()
    }
}