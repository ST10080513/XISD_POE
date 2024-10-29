package com.example.xisd_poe

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.google.zxing.BarcodeFormat
import java.io.ByteArrayOutputStream
import java.util.ArrayList

class QRcode : AppCompatActivity() {

    private lateinit var rvQRCodes: RecyclerView
    private lateinit var tvNoQRCodes: TextView
    private val qrCodesList = ArrayList<QRCodeData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)

        rvQRCodes = findViewById(R.id.rvQRCodes)
        tvNoQRCodes = findViewById(R.id.tvNoQRCodes)

        rvQRCodes.layoutManager = LinearLayoutManager(this)

        getUserQRCodes()
    }

    private fun getUserQRCodes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val qrCodesRef = database.getReference("Users").child(userId).child("qrCodes")

        qrCodesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    qrCodesList.clear()
                    for (qrSnapshot in snapshot.children) {
                        val qrCodeData = qrSnapshot.getValue(QRCodeData::class.java)
                        if (qrCodeData != null) {
                            qrCodesList.add(qrCodeData)
                        }
                    }
                    if (qrCodesList.isEmpty()) {
                        tvNoQRCodes.visibility = View.VISIBLE
                    } else {
                        tvNoQRCodes.visibility = View.GONE
                    }
                    rvQRCodes.adapter = QRCodeAdapter(qrCodesList)
                } else {
                    tvNoQRCodes.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@QRcode, "Failed to retrieve QR codes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    data class QRCodeData(
        val products: List<Map<String, Any>>? = null,
        val totalAmount: Double? = null,
        val qrCode: String? = null
    )

    inner class QRCodeAdapter(private val qrCodes: List<QRCodeData>) : RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRCodeViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_qr_code, parent, false)
            return QRCodeViewHolder(view)
        }

        override fun onBindViewHolder(holder: QRCodeViewHolder, position: Int) {
            val qrCodeData = qrCodes[position]
            holder.bind(qrCodeData)
        }

        override fun getItemCount(): Int = qrCodes.size

        inner class QRCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val ivQRCodeImage: ImageView = itemView.findViewById(R.id.ivQRCodeImage)
            private val tvTotalAmount: TextView = itemView.findViewById(R.id.tvTotalAmount)
            private val productsContainer: LinearLayout = itemView.findViewById(R.id.productsContainer)

            fun bind(qrCodeData: QRCodeData) {
                // Display total amount
                tvTotalAmount.text = "Total Amount: R${qrCodeData.totalAmount ?: 0.0}"

                // Clear previous product views in the container
                productsContainer.removeAllViews()

                // Add each product as a separate TextView
                qrCodeData.products?.forEach { product ->
                    val productTextView = TextView(itemView.context)
                    productTextView.text = "${product["name"]}: R${product["price"]}"
                    productTextView.textSize = 14f
                    productTextView.setTextColor(itemView.resources.getColor(android.R.color.black))
                    productsContainer.addView(productTextView)
                }

                // Check if the QR code is available
                if (qrCodeData.qrCode != null) {
                    // Decode and display the existing QR code image
                    val decodedString = Base64.decode(qrCodeData.qrCode, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                    ivQRCodeImage.setImageBitmap(bitmap)
                } else {
                    // Generate a new QR code if missing
                    val newQRCodeData = buildQRCodeData(qrCodeData.products ?: emptyList(), qrCodeData.totalAmount ?: 0.0)
                    generateAndSaveQRCode(newQRCodeData, qrCodeData)
                }
            }

            // Helper function to build QR Code data
            private fun buildQRCodeData(products: List<Map<String, Any>>, totalAmount: Double): String {
                val itemDetails = products.joinToString(separator = ";") { "${it["name"]}:R${it["price"]}" }
                return "Items:[$itemDetails];Total:R$totalAmount"
            }

            // Generate and save a new QR code if it doesn't exist
            private fun generateAndSaveQRCode(data: String, qrCodeData: QRCodeData) {
                try {
                    val barcodeEncoder = BarcodeEncoder()
                    val bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400)

                    // Display the generated QR code
                    ivQRCodeImage.setImageBitmap(bitmap)

                    // Convert bitmap to Base64
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                    val qrCodeBase64 = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

                    // Save the generated QR code to Firebase
                    saveQRCodeToFirebase(qrCodeBase64, qrCodeData)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(itemView.context, "Failed to generate QR Code.", Toast.LENGTH_SHORT).show()
                }
            }

            // Save QR code to Firebase
            private fun saveQRCodeToFirebase(qrCodeBase64: String, qrCodeData: QRCodeData) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
                val database = FirebaseDatabase.getInstance()
                val qrCodesRef = database.getReference("Users").child(userId).child("qrCodes")

                // Find the specific QR code reference and update it
                qrCodesRef.child(qrCodeData.totalAmount.toString())  // Assuming totalAmount or other unique key identifies it
                    .child("qrCode")
                    .setValue(qrCodeBase64)
                    .addOnSuccessListener {
                        Toast.makeText(itemView.context, "QR Code saved to Firebase!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(itemView.context, "Failed to save QR Code to Firebase.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
