package com.frafio.myfinance.data.manager

import android.util.Log
import android.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.models.Purchase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

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

                    // formatta data
                    purchase.day?.let { day ->
                        purchase.month?.let { month ->
                            purchase.year?.let { year ->
                                val dayString: String = if (day < 10) {
                                    "0$day"
                                } else {
                                    day.toString()
                                }
                                val monthString: String = if (month < 10) {
                                    "0$month"
                                } else {
                                    month.toString()
                                }
                                purchase.formattedDate = "$dayString/$monthString/$year"
                            }
                        }
                    }

                    // formatta prezzo
                    purchase.price?.let { price ->
                        val locale = Locale("en", "UK")
                        val nf = NumberFormat.getInstance(locale)
                        val formatter = nf as DecimalFormat
                        formatter.applyPattern("###,###,##0.00")
                        purchase.formattedPrice = "€ " + formatter.format(price)
                    }

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

    fun resetPurchaseList() {
        purchaseList = mutableListOf()
    }

    fun deleteAndUpdatePurchaseAt(position: Int) {
        var totPosition: Int

        FirebaseFirestore.getInstance().also { fStore ->
            fStore.collection("purchases").document(purchaseList[position].id!!).delete().addOnSuccessListener {
                if (purchaseList[position].type == 0) {
                    purchaseList.removeAt(position)
                    fetchListener?.onFetchSuccess("Totale eliminato!")
                } else if (purchaseList[position].type != 3) {
                    for (i in position - 1 downTo 0) {
                        if (purchaseList[i].type == 0) {
                            totPosition = i
                            val newPurchase = purchaseList[totPosition]
                            newPurchase.price = newPurchase.price?.minus(
                                purchaseList[position].price!!
                            )
                            purchaseList[totPosition] = newPurchase
                            purchaseList.removeAt(position)
                            FirebaseFirestore.getInstance().also { fStore ->
                                fStore.collection("purchases").document(purchaseList[i].id!!)
                                    .set(purchaseList[i]).addOnSuccessListener {
                                        fetchListener?.onFetchSuccess("Acquisto eliminato!")
                                    }.addOnFailureListener { e ->
                                        Log.e(TAG, "Error! ${e.localizedMessage}")
                                        fetchListener?.onFetchFailure("Acquisto non eliminato correttamente!")
                                    }
                                fStore.terminate()
                            }
                            break
                        }
                    }
                } else {
                    purchaseList.removeAt(position)
                    fetchListener?.onFetchSuccess("Acquisto eliminato!")
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                fetchListener?.onFetchFailure("Acquisto non eliminato correttamente!")
            }
            fStore.terminate()
        }
    }
}