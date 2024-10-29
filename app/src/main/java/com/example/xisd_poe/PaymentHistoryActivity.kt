package com.example.xisd_poe

import PaymentHistoryAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xisd_poe.models.PaymentHistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class PaymentHistoryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var paymentHistoryRecyclerView: RecyclerView
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter
    private lateinit var paymentHistoryList: ArrayList<PaymentHistoryItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize RecyclerView
        paymentHistoryRecyclerView = findViewById(R.id.paymentHistoryRecyclerView)
        paymentHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        paymentHistoryList = ArrayList()
        paymentHistoryAdapter = PaymentHistoryAdapter(paymentHistoryList)
        paymentHistoryRecyclerView.adapter = paymentHistoryAdapter

        // Load Payment History from Firebase
        loadPaymentHistory()
    }

    private fun loadPaymentHistory() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val paymentHistoryRef = database.getReference("Users").child(userId).child("payment-history")

            paymentHistoryRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val paymentHistoryList: ArrayList<PaymentHistoryItem> = ArrayList()

                        for (dataSnapshot in snapshot.children) {
                            val amount = dataSnapshot.child("amount").getValue(Double::class.java) ?: 0.0
                            val cardNumber = dataSnapshot.child("cardNumber").getValue(String::class.java) ?: "N/A"
                            val transactionDate = dataSnapshot.child("transactionDate").getValue(String::class.java) ?: "No Date"
                            val cardholderName = dataSnapshot.child("cardholderName").getValue(String::class.java) ?: "N/A"
                            val cvv = dataSnapshot.child("cvv").getValue(String::class.java) ?: "N/A"
                            val expirationDate = dataSnapshot.child("expirationDate").getValue(String::class.java) ?: "N/A"
                            val paymentMethod = dataSnapshot.child("paymentMethod").getValue(String::class.java) ?: "N/A"
                            val purchaseInfo = dataSnapshot.child("purchaseInfo").getValue(object : GenericTypeIndicator<Map<String, Any>>() {}) ?: mapOf()

                            // Get the unique ID (key) of the payment history record
                            val id = dataSnapshot.key ?: "Unknown ID"

                            val paymentHistoryItem = PaymentHistoryItem(
                                id = id, // Store the ID
                                amount = amount,
                                cardNumber = cardNumber,
                                transactionDate = transactionDate,
                                purchaseInfo = purchaseInfo,
                                cardholderName = cardholderName, // Add this
                                cvv = cvv, // Add this
                                expirationDate = expirationDate, // Add this
                                paymentMethod = paymentMethod, // Add this
                            )

                            paymentHistoryList.add(paymentHistoryItem)
                        }

                        paymentHistoryAdapter.updateData(paymentHistoryList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PaymentHistory", "Error loading payment history", error.toException())
                    Toast.makeText(this@PaymentHistoryActivity, "Error loading payment history", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }



}
