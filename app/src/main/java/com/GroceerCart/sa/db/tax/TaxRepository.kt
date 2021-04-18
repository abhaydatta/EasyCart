package com.GroceerCart.sa.db.tax


class TaxRepository(private val dao: TaxDAO) {

    suspend fun getAllTax():List<Tax>{
        return dao.getAllTax()
    }

    suspend fun insertTax(tax: Tax){
        dao.insertTax(tax)
    }

    suspend fun updateTax(tax: Tax){
        dao.updateTax(tax)
    }

    suspend fun deleteTax(tax: Tax){
        dao.deleteTax(tax)
    }

    suspend  fun deleteAllTax(){
        dao.deleteAllTax()
    }

    suspend fun getTaxWithProductParentId(id: Int):List<Tax>{
        return dao.getTaxWithProductParentId(id)
    }
}