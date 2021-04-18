package com.GroceerCart.sa.db.taxitem

import com.GroceerCart.sa.db.tax.Tax

class TaxItemRepository(private val dao:TaxItemDAO){

    suspend fun getAllTaxItem():List<TaxItem>{
        return dao.getAllTaxItem()
    }

    suspend fun insertTaxItem(tax: TaxItem){
        dao.insertTax(tax)
    }

    suspend fun updateTaxItem(tax: TaxItem){
        dao.updateTax(tax)
    }

    suspend fun updateTaxWithId(
        taxPrice: Double,
        id: Int
    ){
        dao.updateTaxWithId(taxPrice,id)
    }

    suspend fun deleteTaxItem(tax: TaxItem){
        dao.deleteTax(tax)
    }

    suspend  fun deleteAllTaxItem(){
        dao.deleteAllTax()
    }

    suspend fun deleteTaxById(id: Int){
        dao.deleteTaxById(id)
    }

    suspend fun getTaxItemWithProductId(id: Int):List<TaxItem>{
        return dao.getTaxItemWithProductId(id)
    }

    suspend fun getTaxItemWithVendorId(id: Int):List<TaxItem>{
        return dao.getTaxItemWithVendorId(id)
    }
}