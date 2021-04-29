package com.frafio.myfinance.ui.home

import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.frafio.myfinance.R
import com.frafio.myfinance.data.models.ReceiptItem
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class ReceiptActivity : AppCompatActivity() {

    lateinit var layout: RelativeLayout
    var nunito: Typeface? = null

    lateinit var mToolbar: MaterialToolbar
    lateinit var mTitleTV: TextView
    lateinit var mPriceTV:TextView
    lateinit var mRecyclerView: RecyclerView
    private lateinit var adapter: FirestoreRecyclerAdapter<ReceiptItem, ReceiptItemViewHolder>
    lateinit var mNameET: EditText
    lateinit var mPriceET: EditText
    lateinit var mAddBtn: ExtendedFloatingActionButton

    private var purchaseID: String? = null
    private var purchaseName: String? = null
    private var purchasePrice: String? = null

    companion object {
        private val TAG = ReceiptActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt)

        nunito = ResourcesCompat.getFont(applicationContext, R.font.nunito)

        // toolbar
        mToolbar = findViewById(R.id.receipt_toolbar)
        setSupportActionBar(mToolbar)

        // back arrow
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        layout = findViewById(R.id.receipt_layout)
        mTitleTV = findViewById(R.id.receipt_purchaseTitle)
        mPriceTV = findViewById(R.id.receipt_purchasePrice)
        mRecyclerView = findViewById(R.id.receipt_recView)
        mNameET = findViewById(R.id.receipt_name_EditText)
        mPriceET = findViewById(R.id.receipt_price_EditText)
        mAddBtn = findViewById(R.id.receipt_addBtn)

        // retrieve purchase data from intent
        purchaseID = intent.getStringExtra("com.frafio.myfinance.purchaseID")
        purchaseName = intent.getStringExtra("com.frafio.myfinance.purchaseName")
        purchasePrice = intent.getStringExtra("com.frafio.myfinance.purchasePrice")

        mTitleTV.text = purchaseName
        mPriceTV.text = purchasePrice

        loadReceiptList()

        mAddBtn.setOnClickListener {
            addItem()
        }
    }

    private fun loadReceiptList() {
        val fStore = FirebaseFirestore.getInstance()
        val query = fStore.collection("purchases").document(purchaseID!!)
            .collection("receipt").orderBy("name")

        // recyclerOptions
        val options: FirestoreRecyclerOptions<ReceiptItem> = FirestoreRecyclerOptions.Builder<ReceiptItem>().setQuery(
            query,
            ReceiptItem::class.java
        ).build()
        adapter = object :
            FirestoreRecyclerAdapter<ReceiptItem, ReceiptItemViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptItemViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_recycler_view_receipt_item,
                    parent,
                    false
                )
                return ReceiptItemViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: ReceiptItemViewHolder,
                position: Int,
                model: ReceiptItem
            ) {
                holder.rNomeTV?.text = model.name
                val locale = Locale("en", "UK")
                val nf = NumberFormat.getInstance(locale)
                val formatter = nf as DecimalFormat
                formatter.applyPattern("###,###,##0.00")
                holder.rPriceTV?.text = "€ ${formatter.format(model.price)}"
                holder.rItemLayout?.setOnLongClickListener {
                    val voceID = snapshots.getSnapshot(position).id
                    val builder = MaterialAlertDialogBuilder(
                        this@ReceiptActivity,
                        R.style.ThemeOverlay_MyFinance_AlertDialog
                    )
                    builder.setTitle(model.name)
                    builder.setMessage("Vuoi eliminare la voce selezionata?")
                    builder.setNegativeButton("Annulla", null)
                    builder.setPositiveButton("Elimina") { dialog, which ->
                        val fStore1 = FirebaseFirestore.getInstance()
                        fStore1.collection("purchases").document(purchaseID!!)
                            .collection("receipt").document(voceID).delete()
                            .addOnSuccessListener {
                                showSnackbar("Voce eliminata!")
                            }.addOnFailureListener { e ->
                                Log.e(TAG, "Error! ${e.localizedMessage}")
                                showSnackbar("Voce non eliminata!")
                            }
                    }
                    builder.show()
                    true
                }
            }
        }

        // View Holder
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView.adapter = adapter
    }

    private class ReceiptItemViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var rItemLayout: ConstraintLayout? = null
        var rNomeTV: TextView? = null
        var rPriceTV: TextView? = null

        init {
            rItemLayout = itemView.findViewById(R.id.recView_receiptItem_constraintLayout)
            rNomeTV = itemView.findViewById(R.id.recView_receiptItem_nomeTextView)
            rPriceTV = itemView.findViewById(R.id.recView_receiptItem_priceTextView)
        }
    }

    private fun addItem() {
        val name = mNameET.text.toString().trim()
        val priceString = mPriceET.text.toString().trim()

        // controlla le info aggiunte
        if (TextUtils.isEmpty(name)) {
            mNameET.error = "Inserisci il nome dell'acquisto."
            return
        }
        if (TextUtils.isEmpty(priceString)) {
            mPriceET.error = "Inserisci il costo dell'acquisto."
            return
        }

        val price = priceString.toDouble()
        val item = ReceiptItem(name, price)
        val fStore = FirebaseFirestore.getInstance()
        fStore.collection("purchases").document(purchaseID!!).collection("receipt").add(item)
            .addOnSuccessListener {
                showSnackbar("Voce aggiunta!")
                mNameET.setText("")
                mPriceET.setText("")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error! ${e.localizedMessage}")
                showSnackbar("Voce non aggiunta!")
            }
    }

    //start&stop listening
    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    // ends this activity (back arrow)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSnackbar(string: String) {
        val snackbar = Snackbar.make(layout, string, BaseTransientBottomBar.LENGTH_SHORT)
            .setAnchorView(mNameET)
            .setBackgroundTint(ContextCompat.getColor(applicationContext, R.color.snackbar))
            .setTextColor(ContextCompat.getColor(applicationContext, R.color.inverted_primary_text))
        val tv = snackbar.view.findViewById<TextView>(R.id.snackbar_text)
        tv.typeface = nunito
        snackbar.show()
    }
}