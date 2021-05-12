package com.frafio.myfinance.data.manager

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.models.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object PurchaseManager {
    var fetchListener: FetchListener? = null

    private var purchaseList: MutableList<Purchase> = mutableListOf()

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

                    // set id
                    purchase.id = document.id

                    purchase.updateFormattedDate()
                    purchase.updateFormattedPrice()

                    purchaseList.add(position, purchase)
                }
                fetchListener?.onFetchSuccess(null)
                fStore.terminate()
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                fetchListener?.onFetchFailure("Error! ${e.localizedMessage}")
                fStore.terminate()
            }
    }

    fun updatePurchaseAt(index: Int, purchase: Purchase) {
        purchaseList[index] = purchase
    }

    fun getPurchaseAt(index: Int) : Purchase? {
        return if (!purchaseList.isNullOrEmpty()) {
            purchaseList[index]
        } else {
            null
        }
    }

    fun getPurchaseList() : List<Purchase>{
        return purchaseList
    }

    fun getPurchaseListSize() : Int{
        return  purchaseList.size
    }

    fun resetPurchaseList() {
        purchaseList = mutableListOf()
    }

    fun deleteAndUpdatePurchaseAt(position: Int) {
        val response = MutableLiveData<Any>()
        var totPosition: Int

        val fStore = FirebaseFirestore.getInstance()
        fStore.collection("purchases").document(purchaseList[position].id!!).delete().addOnSuccessListener {
            if (purchaseList[position].type == 0) {
                purchaseList.removeAt(position)
                response.value = Triple("Totale eliminato!", position, null)
                fetchListener?.onFetchSuccess(response)
                fStore.terminate()
            } else if (purchaseList[position].type != 3) {
                for (i in position - 1 downTo 0) {
                    if (purchaseList[i].type == 0) {
                        totPosition = i
                        val newPurchase = purchaseList[totPosition]
                        newPurchase.price = newPurchase.price?.minus(
                            purchaseList[position].price!!
                        )

                        newPurchase.updateFormattedPrice()

                        purchaseList[totPosition] = newPurchase
                        purchaseList.removeAt(position)

                        fStore.collection("purchases").document(purchaseList[i].id!!)
                            .set(purchaseList[i]).addOnSuccessListener {
                                response.value = Triple("Acquisto eliminato!", position, totPosition)
                                fetchListener?.onFetchSuccess(response)
                                fStore.terminate()
                            }.addOnFailureListener { e ->
                                Log.e(TAG, "Error! ${e.localizedMessage}")
                                fetchListener?.onFetchFailure("Acquisto non eliminato correttamente!")
                                fStore.terminate()
                            }
                        break
                    }
                }
            } else {
                purchaseList.removeAt(position)
                response.value = Triple("Acquisto eliminato!", position, null)
                fetchListener?.onFetchSuccess(response)
                fStore.terminate()
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Error! ${e.localizedMessage}")
            fetchListener?.onFetchFailure("Acquisto non eliminato correttamente!")
            fStore.terminate()
        }
    }
}