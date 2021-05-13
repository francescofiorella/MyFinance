package com.frafio.myfinance.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.frafio.myfinance.data.models.ReceiptItem
import com.google.firebase.firestore.FirebaseFirestore

class ReceiptRepository {

    companion object {
        private val TAG = ReceiptRepository::class.java.simpleName
    }

    fun setOptions(purchaseID: String): FirestoreRecyclerOptions<ReceiptItem> {
        val fStore = FirebaseFirestore.getInstance()
        val query = fStore.collection("purchases").document(purchaseID)
            .collection("receipt").orderBy("name")

        return FirestoreRecyclerOptions.Builder<ReceiptItem>().setQuery(
            query,
            ReceiptItem::class.java
        ).build()
    }

    fun addReceiptItem(receiptItem: ReceiptItem, purchaseID: String): LiveData<Any> {
        val response = MutableLiveData<Any>()
        FirebaseFirestore.getInstance().also { fStore ->
            fStore.collection("purchases").document(purchaseID)
                .collection("receipt").add(receiptItem)
                .addOnSuccessListener {
                    response.value = "Voce aggiunta!"
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    response.value = "Voce non aggiunta!"
                }
        }
        return response
    }

    fun deleteReceiptItem(receiptItem: ReceiptItem, purchaseID: String): LiveData<Any> {
        val response = MutableLiveData<Any>()
        FirebaseFirestore.getInstance().also { fStore ->
            fStore.collection("purchases").document(purchaseID)
                .collection("receipt").document(receiptItem.id!!).delete()
                .addOnSuccessListener {
                    response.value = "Voce eliminata!"
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    response.value = "Voce non eliminata!"
                }
        }

        return response
    }
}