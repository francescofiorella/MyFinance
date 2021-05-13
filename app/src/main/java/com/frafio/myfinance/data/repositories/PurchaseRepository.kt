package com.frafio.myfinance.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.frafio.myfinance.data.storage.PurchaseStorage
import com.frafio.myfinance.data.storage.UserStorage
import com.frafio.myfinance.data.models.Purchase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

class PurchaseRepository {

    companion object {
        private val TAG = PurchaseRepository::class.java.simpleName
    }

    fun updatePurchaseList(): LiveData<Any> {
        val response = MutableLiveData<Any>()

        FirebaseFirestore.getInstance().also { fStore ->
            fStore.collection("purchases")
                .whereEqualTo("email", UserStorage.getUser()!!.email)
                .orderBy("year", Query.Direction.DESCENDING)
                .orderBy("month", Query.Direction.DESCENDING)
                .orderBy("day", Query.Direction.DESCENDING).orderBy("type")
                .orderBy("price", Query.Direction.DESCENDING)
                .get().addOnSuccessListener { queryDocumentSnapshots ->
                    PurchaseStorage.resetPurchaseList()

                    queryDocumentSnapshots.forEach { document ->
                        val purchase = document.toObject(Purchase::class.java)

                        // set id
                        purchase.id = document.id

                        purchase.updateFormattedDate()
                        purchase.updateFormattedPrice()

                        PurchaseStorage.addPurchase(purchase)
                    }

                    response.value = "List updated"
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")

                    response.value = "Error! ${e.localizedMessage}"
                }
        }

        return response
    }

    fun purchaseListSize(): Int {
        return PurchaseStorage.getPurchaseList().size
    }

    fun getPurchaseList(): List<Purchase> {
        return PurchaseStorage.getPurchaseList()
    }

    fun calculateStats(): List<String> {
        val dayAvg: Double
        val monthAvg: Double
        var todayTot = 0.0
        var tot = 0.0
        var ticketTot = 0.0
        var numTot = 0
        var trenTot = 0
        var amTot = 0

        var nDays = 0
        var nMonth = 0
        var lastMonth = 0
        var lastYear = 0

        PurchaseStorage.getPurchaseList().forEach { purchase ->
            // totale biglietti Amtab
            if (purchase.name == "Biglietto Amtab") {
                amTot++
            }
            if (purchase.type == 0) {
                // totale di oggi
                val year = LocalDate.now().year
                val month = LocalDate.now().monthValue
                val day = LocalDate.now().dayOfMonth
                if (purchase.year == year && purchase.month == month && purchase.day == day) {
                    todayTot = purchase.price ?: 0.0
                }

                // incrementa il totale
                tot += purchase.price ?: 0.0

                // conta i giorni
                nDays++

                // conta i mesi
                if (purchase.year != lastYear) {
                    lastYear = purchase.year ?: 0
                    lastMonth = purchase.month ?: 0
                    nMonth++
                } else if (purchase.month != lastMonth) {
                    lastMonth = purchase.month ?: 0
                    nMonth++
                }
            } else if (purchase.type != 3) {
                // totale acquisti (senza biglietti)
                numTot++
            } else {
                // totale biglietti
                ticketTot += purchase.price ?: 0.0

                // totale biglietti TrenItalia
                if (purchase.name == "Biglietto TrenItalia") {
                    trenTot++
                }
            }
        }

        dayAvg = tot / nDays
        monthAvg = tot / nMonth

        val locale = Locale("en", "UK")
        val nf = NumberFormat.getInstance(locale)
        val formatter = nf as DecimalFormat
        formatter.applyPattern("###,###,##0.00")

        val stats = mutableListOf<String>()
        stats.add("€ ${formatter.format(dayAvg)}")
        stats.add("€ ${formatter.format(monthAvg)}")
        stats.add("€ ${formatter.format(todayTot)}")
        stats.add("€ ${formatter.format(tot)}")
        stats.add(numTot.toString())
        stats.add("€ ${formatter.format(ticketTot)}")
        stats.add(trenTot.toString())
        stats.add(amTot.toString())

        return stats
    }

    fun deletePurchaseAt(position: Int): LiveData<Any> {
        val response = MutableLiveData<Any>()
        var totPosition: Int

        val purchaseList = PurchaseStorage.getPurchaseList()

        val fStore = FirebaseFirestore.getInstance()
        fStore.collection("purchases").document(purchaseList[position].id!!).delete()
            .addOnSuccessListener {
                if (purchaseList[position].type == 0) {
                    purchaseList.removeAt(position)
                    PurchaseStorage.setPurchaseList(purchaseList)

                    response.value = Triple("Totale eliminato!", position, null)
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
                                    PurchaseStorage.setPurchaseList(purchaseList)

                                    response.value =
                                        Triple("Acquisto eliminato!", position, totPosition)
                                    fStore.terminate()
                                }.addOnFailureListener { e ->
                                    Log.e(TAG, "Error! ${e.localizedMessage}")

                                    response.value = "Acquisto non eliminato correttamente!"
                                    fStore.terminate()
                                }
                            break
                        }
                    }
                } else {
                    purchaseList.removeAt(position)
                    PurchaseStorage.setPurchaseList(purchaseList)

                    response.value = Triple("Acquisto eliminato!", position, null)
                    fStore.terminate()
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")

                response.value = "Acquisto non eliminato correttamente!"
                fStore.terminate()
            }

        return response
    }

    fun addTotale(purchase: Purchase): LiveData<Any> {
        val response = MutableLiveData<Any>()

        purchase.id = "${purchase.year}${purchase.month}${purchase.day}"
        FirebaseFirestore.getInstance().also { fStore ->
            fStore.collection("purchases").document(purchase.id!!).set(purchase)
                .addOnSuccessListener {
                    response.value = 1
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    response.value = "Totale non aggiunto!"
                }
        }

        return response
    }

    fun addPurchase(purchase: Purchase): LiveData<Any> {
        val response = MutableLiveData<Any>()

        val userEmail = UserStorage.getUser()!!.email

        FirebaseFirestore.getInstance().also { fStore ->
            fStore.collection("purchases").add(purchase)
                .addOnSuccessListener {
                    var sum = if (purchase.type != 3) {
                        purchase.price ?: 0.0
                    } else {
                        0.0
                    }
                    for (item in PurchaseStorage.getPurchaseList()) {
                        if (item.email == userEmail && item.type != 0
                            && item.type != 3 && item.year == purchase.year
                            && item.month == purchase.month && item.day == purchase.day
                        ) {
                            sum += item.price ?: 0.0
                        }
                    }
                    val totalP = Purchase(userEmail, "Totale", sum, purchase.year, purchase.month, purchase.day, 0)
                    FirebaseFirestore.getInstance().also { fStore1 ->
                        totalP.id = "${purchase.year}${purchase.month}${purchase.day}"
                        fStore1.collection("purchases").document(totalP.id!!).set(totalP)
                            .addOnSuccessListener {
                                response.value = 1
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error! ${e.localizedMessage}")
                                response.value = "Acquisto non aggiunto correttamente!"
                            }
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    response.value = "Acquisto non aggiunto!"
                }
        }

        return response
    }

    fun editPurchase(purchase: Purchase, position: Int, purchasePrice: Double): LiveData<Any> {
        val response = MutableLiveData<Any>()

        FirebaseFirestore.getInstance().also { fStore ->
            fStore.collection("purchases").document(purchase.id!!).set(purchase)
                .addOnSuccessListener {

                    purchase.updateFormattedPrice()
                    purchase.updateFormattedDate()

                    PurchaseStorage.updatePurchaseAt(position, purchase)
                    if (purchase.price != purchasePrice) {
                        var sum = 0.0
                        for (item in PurchaseStorage.getPurchaseList()) {
                            if (item.email == purchase.email && item.type != 0
                                && item.type != 3 && item.year == purchase.year
                                && item.month == purchase.month && item.day == purchase.day
                            ) {
                                sum += item.price ?: 0.0
                            }
                        }
                        val totID = "${purchase.year}${purchase.month}${purchase.day}"
                        val totalP = Purchase(purchase.email, "Totale", sum, purchase.year, purchase.month, purchase.day, 0, totID)
                        FirebaseFirestore.getInstance().also { fStore1 ->
                            fStore1.collection("purchases").document(totID).set(totalP)
                                .addOnSuccessListener {
                                    response.value = 1
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error! ${e.localizedMessage}")
                                    response.value = "Acquisto non aggiunto correttamente!"
                                }
                        }

                    } else {
                        response.value = "List updated"
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error! ${e.localizedMessage}")
                    response.value = "Acquisto non modificato!"
                }
        }

        return response
    }
}