package com.frafio.myfinance.data.managers

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.enums.db.DbPurchases
import com.frafio.myfinance.data.enums.db.PurchaseCode
import com.frafio.myfinance.data.models.Purchase
import com.frafio.myfinance.data.models.PurchaseResult
import com.frafio.myfinance.data.storages.PurchaseStorage
import com.frafio.myfinance.data.storages.UserStorage
import com.frafio.myfinance.utils.getSharedCategory
import com.frafio.myfinance.utils.getSharedDynamicColor
import com.frafio.myfinance.utils.setSharedCategory
import com.frafio.myfinance.utils.setSharedDynamicColor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class PurchaseManager(private val sharedPreferences: SharedPreferences) {

    companion object {
        private val TAG = PurchaseManager::class.java.simpleName
    }

    private val fStore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun getCategories(): LiveData<Pair<PurchaseResult, List<String>>> {
        val response = MutableLiveData<Pair<PurchaseResult, List<String>>>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .get().addOnSuccessListener { docSnap ->
                val categories =
                    (docSnap.data?.get(DbPurchases.FIELDS.CATEGORIES.value) as List<*>).map { value ->
                        value.toString()
                    }
                response.value = Pair(
                    PurchaseResult(PurchaseCode.PURCHASE_GET_CATEGORIES_SUCCESS),
                    categories
                )
            }.addOnFailureListener {
                response.value = Pair(
                    PurchaseResult(PurchaseCode.PURCHASE_GET_CATEGORIES_FAILURE),
                    listOf()
                )
            }

        return response
    }

    fun createCategory(name: String): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .get().addOnSuccessListener { docSnap ->
                val categories =
                    (docSnap.data?.get(DbPurchases.FIELDS.CATEGORIES.value) as List<*>).map { value ->
                        value.toString()
                    }
                val mutCat = categories.toMutableList()
                mutCat.add(name)
                fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
                    .document(UserStorage.user!!.email!!)
                    .update(DbPurchases.FIELDS.CATEGORIES.value, mutCat).addOnSuccessListener {
                        response.value =
                            PurchaseResult(PurchaseCode.PURCHASE_CREATE_CATEGORY_SUCCESS)
                    }.addOnFailureListener {
                        response.value =
                            PurchaseResult(PurchaseCode.PURCHASE_CREATE_CATEGORY_FAILURE)
                    }
            }.addOnFailureListener {
                response.value = PurchaseResult(PurchaseCode.PURCHASE_CREATE_CATEGORY_FAILURE)
            }
        return response
    }

    fun updateList(): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .whereEqualTo(
                DbPurchases.FIELDS.CATEGORY.value,
                getSharedCategory(sharedPreferences)
            )
            .orderBy(DbPurchases.FIELDS.YEAR.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.MONTH.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.DAY.value, Query.Direction.DESCENDING)
            .orderBy(DbPurchases.FIELDS.PRICE.value, Query.Direction.DESCENDING)
            .get().addOnSuccessListener { queryDocumentSnapshots ->
                PurchaseStorage.resetPurchaseList()
                // Create total for the local list
                var total: Purchase? = null
                // Used to keep the order
                var currentPurchases = mutableListOf<Purchase>()

                queryDocumentSnapshots.forEach { document ->
                    val purchase = document.toObject(Purchase::class.java)
                    if (purchase.type != DbPurchases.TYPES.TOTAL.value) {
                        // set id
                        purchase.updateID(document.id)

                        var todayDate = LocalDate.now()
                        val purchaseDate =
                            LocalDate.of(purchase.year!!, purchase.month!!, purchase.day!!)
                        var prevDate: LocalDate? = if (total == null)
                            null
                        else
                            LocalDate.of(total!!.year!!, total!!.month!!, total!!.day!!)

                        // se è < today and non hai fatto today
                        // quindi se purchase < today and (totale == null or
                        // totale > today)
                        if (prevDate == null &&
                            ChronoUnit.DAYS.between(purchaseDate, todayDate) > 0
                        ) {
                            // Aggiungi totali a 0 per ogni giorno tra oggi e purchase
                            val totToAdd = ChronoUnit.DAYS.between(purchaseDate, todayDate)
                            for (i in 0..<totToAdd) {
                                val totId =
                                    "${todayDate.dayOfMonth}_${todayDate.monthValue}_${todayDate.year}"
                                total = Purchase(
                                    email = UserStorage.user!!.email,
                                    name = DbPurchases.NAMES.TOTAL.value,
                                    price = 0.0,
                                    year = todayDate.year,
                                    month = todayDate.monthValue,
                                    day = todayDate.dayOfMonth,
                                    type = 0,
                                    id = totId,
                                    category = purchase.category
                                )
                                PurchaseStorage.purchaseList.add(total!!)
                                prevDate =
                                    LocalDate.of(total!!.year!!, total!!.month!!, total!!.day!!)
                                todayDate = todayDate.minusDays(1)
                            }
                            todayDate = LocalDate.now()
                        }

                        var totId = "${purchase.day}_${purchase.month}_${purchase.year}"
                        if (prevDate == null) { // If is the first total
                            currentPurchases.add(purchase)
                            total = Purchase(
                                email = UserStorage.user!!.email,
                                name = DbPurchases.NAMES.TOTAL.value,
                                price = if (purchase.type != DbPurchases.TYPES.RENT.value)
                                    purchase.price else 0.0,
                                year = purchase.year,
                                month = purchase.month,
                                day = purchase.day,
                                type = 0,
                                id = totId,
                                category = purchase.category
                            )
                        } else if (total!!.id == totId) { // If the total should be updated
                            currentPurchases.add(purchase)
                            if (purchase.type != DbPurchases.TYPES.RENT.value) {
                                total!!.price = total!!.price!!.plus(purchase.price ?: 0.0)
                            }
                        } else { // If we need a new total
                            // Update the local list with previous day purchases
                            if (currentPurchases.isNotEmpty()) {
                                PurchaseStorage.purchaseList.add(total!!)
                                currentPurchases.forEach { cPurchase ->
                                    PurchaseStorage.purchaseList.add(cPurchase)
                                }
                            }
                            // aggiungi 0 anche se totale - purchase > 1,
                            // aggiungi uno 0 per ogni differenza tra totale e purchase
                            val startFromToday =
                                ChronoUnit.DAYS.between(purchaseDate, todayDate) > 0 &&
                                        ChronoUnit.DAYS.between(todayDate, prevDate) > 0
                            val totToAdd = if (startFromToday)
                                ChronoUnit.DAYS.between(purchaseDate, todayDate) + 1
                            else
                                ChronoUnit.DAYS.between(purchaseDate, prevDate)
                            if (ChronoUnit.DAYS.between(purchaseDate, todayDate) > 0 &&
                                totToAdd > 1
                            ) {
                                if (startFromToday) {
                                    prevDate = LocalDate.now().plusDays(1)
                                }
                                for (i in 1..<totToAdd) {
                                    prevDate = prevDate!!.minusDays(1)
                                    totId =
                                        "${prevDate.dayOfMonth}_${prevDate.monthValue}_${prevDate.year}"
                                    total = Purchase(
                                        email = UserStorage.user!!.email,
                                        name = DbPurchases.NAMES.TOTAL.value,
                                        price = 0.0,
                                        year = prevDate.year,
                                        month = prevDate.monthValue,
                                        day = prevDate.dayOfMonth,
                                        type = 0,
                                        id = totId,
                                        category = purchase.category
                                    )
                                    PurchaseStorage.purchaseList.add(total!!)
                                }
                            }

                            // Create new total
                            currentPurchases = mutableListOf()
                            currentPurchases.add(purchase)
                            totId = "${purchase.day}_${purchase.month}_${purchase.year}"
                            total = Purchase(
                                email = UserStorage.user!!.email,
                                name = DbPurchases.NAMES.TOTAL.value,
                                price = if (purchase.type != DbPurchases.TYPES.RENT.value)
                                    purchase.price else 0.0,
                                year = purchase.year,
                                month = purchase.month,
                                day = purchase.day,
                                type = 0,
                                id = totId,
                                category = purchase.category
                            )
                        }
                    }
                }

                response.value = PurchaseResult(PurchaseCode.PURCHASE_LIST_UPDATE_SUCCESS)
            }.addOnFailureListener { e ->
                val error = "Error! ${e.localizedMessage}"
                Log.e(TAG, error)

                response.value = PurchaseResult(PurchaseCode.PURCHASE_LIST_UPDATE_FAILURE)
            }

        return response
    }

    fun deleteAt(position: Int): LiveData<Triple<PurchaseResult, List<Purchase>, Int?>> {
        val response = MutableLiveData<Triple<PurchaseResult, List<Purchase>, Int?>>()
        var totPosition: Int

        val purchaseList = PurchaseStorage.purchaseList.toMutableList()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchaseList[position].id!!).delete()
            .addOnSuccessListener {
                if (purchaseList[position].type != DbPurchases.TYPES.TRANSPORT.value
                    && purchaseList[position].type != DbPurchases.TYPES.RENT.value
                ) {
                    for (i in position - 1 downTo 0) {
                        if (purchaseList[i].type == DbPurchases.TYPES.TOTAL.value) {
                            totPosition = i

                            val newPurchase = purchaseList[totPosition]
                            newPurchase.price = newPurchase.price?.minus(
                                purchaseList[position].price!!
                            )

                            purchaseList.removeAt(position)
                            if (newPurchase.price != 0.0) {
                                purchaseList[totPosition] = newPurchase
                            } else {
                                purchaseList.removeAt(totPosition)
                            }

                            PurchaseStorage.purchaseList = purchaseList

                            response.value = Triple(
                                PurchaseResult(PurchaseCode.PURCHASE_DELETE_SUCCESS),
                                purchaseList,
                                totPosition
                            )
                            break
                        }
                    }
                } else {
                    purchaseList.removeAt(position)
                    PurchaseStorage.purchaseList = purchaseList

                    response.value = Triple(
                        PurchaseResult(PurchaseCode.PURCHASE_DELETE_SUCCESS),
                        purchaseList,
                        null
                    )
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")

                response.value =
                    Triple(PurchaseResult(PurchaseCode.PURCHASE_DELETE_FAILURE), purchaseList, null)
            }

        return response
    }

    fun addPurchase(purchase: Purchase): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .add(purchase).addOnSuccessListener {
                response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.PURCHASE_ADD_FAILURE)
            }

        return response
    }

    fun editPurchase(
        purchase: Purchase,
        position: Int
    ): LiveData<PurchaseResult> {
        val response = MutableLiveData<PurchaseResult>()

        fStore.collection(DbPurchases.FIELDS.PURCHASES.value)
            .document(UserStorage.user!!.email!!)
            .collection(DbPurchases.FIELDS.PAYMENTS.value)
            .document(purchase.id!!).set(purchase).addOnSuccessListener {
                PurchaseStorage.purchaseList[position] = purchase
                response.value = PurchaseResult(PurchaseCode.PURCHASE_EDIT_SUCCESS)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                response.value = PurchaseResult(PurchaseCode.PURCHASE_EDIT_FAILURE)
            }


        return response
    }

    fun updateListByCollection(collection: String): LiveData<PurchaseResult> {
        setSharedCategory(sharedPreferences, collection)
        return updateList()
    }

    fun getSelectedCategory(): String {
        return getSharedCategory(sharedPreferences)
    }

    fun setDynamicColorActive(active: Boolean) {
        setSharedDynamicColor(sharedPreferences, active)
    }

    fun getDynamicColorActive(): Boolean {
        return getSharedDynamicColor(sharedPreferences)
    }
}