package com.GroceerCart.sa.db.vendor

import com.GroceerCart.sa.db.db.User

class VendorRepository(val dao: VendorDAO) {
    //  val users = dao.getAllUsers()

    suspend fun getAllVendor():List<Vendor>{
        return dao.getAllVendor()
    }

    suspend fun insertVendor(vendor: Vendor){
        dao.insertVendor(vendor)
    }

    suspend fun updateVendor(vendor: Vendor){
        dao.updateVendor(vendor)
    }

    suspend fun deleteVendor(vendor: Vendor){
        dao.deleteVendor(vendor)
    }

    suspend  fun deleteAllVendor(){
        dao.deleteAll()
    }
}