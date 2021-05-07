package com.frafio.myfinance.data.manager

import android.util.Log
import android.util.Pair
import com.frafio.myfinance.data.models.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object PurchaseManager {
    var fetchListener: FetchListener? = null

    private var purchaseList: MutableList<Pair<String, Purchase>> = mutableListOf()

    private val TAG: String = PurchaseManager::class.java.simpleName

    fun updatePurchaseList() {
        purchaseList = mutableListOf()

        val fAuth = FirebaseAuth.getInstance()
        val fStore = FirebaseFirestore.getInstance()

        fStore.collection("purchases").whereEqualTo("email", fAuth.currentUser!!.email)
            .orderBy("year", Query.Direction.DESCENDING)
            .orderBy("month", Query.Direction.DESCENDING)
            .orderBy("day", Query.Direction.DESCENDING).orderBy("type")
            .orderBy("price", Query.Direction.DESCENDING)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                for ((position, document) in queryDocumentSnapshots.withIndex()) {
                    val purchase = document.toObject(Purchase::class.java)
                    purchaseList.add(position, Pair(document.id, purchase))
                }
                fetchListener?.onFetchSuccess()
                fStore.terminate()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                fetchListener?.onFetchFailure("Error! ${e.localizedMessage}")
                fStore.terminate()
            }
    }

    fun updatePurchaseAt(index: Int, purchase: Purchase) {
        val purchaseID = purchaseList.get(index).first
        purchaseList[index] = Pair(purchaseID, purchase)
    }

    fun getPurchaseAt(index: Int) : Pair<String, Purchase>? {
        return if (!purchaseList.isNullOrEmpty()) {
            purchaseList[index]
        } else {
            null
        }
    }

    fun getPurchaseList() : List<Pair<String, Purchase>>{
        return purchaseList
    }

    fun removePurchaseAt(index: Int) {
        purchaseList.removeAt(index)
    }

    fun resetPurchaseList() {
        purchaseList = mutableListOf()
    }
}