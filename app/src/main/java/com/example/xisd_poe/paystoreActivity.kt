package com.example.xisd_poe

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class paystoreActivity : AppCompatActivity() {

    private lateinit var btnPayNow: Button
    private lateinit var cardNumberInput: EditText
    private lateinit var expiryDateInput: EditText
    private lateinit var paymentMethodInput: EditText
    private lateinit var cvvInput: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var cardholderNameInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paystore)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize input fields
        cardholderNameInput = findViewById(R.id.cardholderNameInput)
        cardNumberInput = findViewById(R.id.cardNumberInput)
        expiryDateInput = findViewById(R.id.expiryDateInput)
        paymentMethodInput = findViewById(R.id.paymentMethodInput)
        cvvInput = findViewById(R.id.cvvInput)
        btnPayNow = findViewById(R.id.btnPayNow)

        // Get cart items and total price passed from PaymentActivity
        val cartItems = intent.getSerializableExtra("cartItems") as? ArrayList<Pair<String, Double>>
        val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)

        if (cartItems == null) {
            Toast.makeText(this, "Cart items not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Auto-fill form if user has previous payment history
        autofillPaymentDetails()

        // Pay Now button action
        btnPayNow.setOnClickListener {
            val cardholderName = cardholderNameInput.text.toString()
            val cardNumber = cardNumberInput.text.toString()
            val expiryDate = expiryDateInput.text.toString()
            val paymentMethod = paymentMethodInput.text.toString()
            val cvv = cvvInput.text.toString()

            // Validate input
            if (validateInput(cardNumber,cardholderName, expiryDate, paymentMethod, cvv)) {
                processPayment(cartItems, totalPrice, cardNumber,cardholderName, expiryDate, paymentMethod, cvv)
            }
        }
    }

    private fun autofillPaymentDetails() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("Users").child(userId)

            // Fetch payment history
            userRef.child("payment-history").orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (paymentSnapshot in snapshot.children) {
                            val cardholderName = paymentSnapshot.child("cardholderName").value.toString()
                            val cardNumber = paymentSnapshot.child("cardNumber").value.toString()
                            val expirationDate = paymentSnapshot.child("expirationDate").value.toString()
                            val paymentMethod = paymentSnapshot.child("paymentMethod").value.toString()
                            val cvv = paymentSnapshot.child("cvv").value.toString()

                            // Auto-fill form with previous payment details
                            cardholderNameInput.setText(cardholderName)
                            cardNumberInput.setText(cardNumber)
                            expiryDateInput.setText(expirationDate)
                            paymentMethodInput.setText(paymentMethod)
                            cvvInput.setText(cvv)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@paystoreActivity, "Error loading payment history", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun validateInput(cardNumber: String,cardholderName: String, expiryDate: String, paymentMethod: String, cvv: String): Boolean {
        if (cardNumber.isEmpty() || cardNumber.length != 16) {
            Toast.makeText(this, "Invalid card number", Toast.LENGTH_SHORT).show()
            return false
        }
        if (expiryDate.isEmpty() || !expiryDate.matches(Regex("\\d{2}/\\d{2}"))) {
            Toast.makeText(this, "Invalid expiry date format. Use MM/YY", Toast.LENGTH_SHORT).show()
            return false
        }
        if (paymentMethod.isEmpty()) {
            Toast.makeText(this, "Please enter payment method", Toast.LENGTH_SHORT).show()
            return false
        }
        if (cvv.isEmpty() || cvv.length != 3) {
            Toast.makeText(this, "Invalid CVV", Toast.LENGTH_SHORT).show()
            return false
        }
        if (cardholderName.isEmpty()) {
            Toast.makeText(this, "Please enter cardholder name", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun processPayment(
        cartItems: ArrayList<Pair<String, Double>>,
        totalPrice: Double,
        cardNumber: String,
        cardholderName: String,
        expiryDate: String,
        paymentMethod: String,
        cvv: String

    ) {
        val purchaseInfo = HashMap<String, Any>()

        // Populate purchaseInfo with each product's details
        for (item in cartItems) {
            val productName = item.first
            val productDetails = hashMapOf(
                "actualPrice" to "36",  // Dummy data, replace with actual logic
                "cate" to "Fruit",
                "description" to "Peaches picked from fresh blah blah",
                "discount" to "19",
                "exDate" to "Thursday 10 January 2025",
                "manuDate" to "Friday 20 December 2024",
                "name" to productName,
                "prescription" to "yes",
                "quantity" to 1,
                "sellPrice" to item.second,
                "shortDesc" to "Sweet Peach selection",
                "stock" to "21",
                "use" to "Health"
            )
            purchaseInfo[productName] = productDetails
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userRef = database.getReference("Users").child(userId)
            val paymentHistoryRef = userRef.child("payment-history").push()

            // Save payment details to Firebase
            paymentHistoryRef.setValue(hashMapOf(
                "amount" to totalPrice,
                "cardNumber" to cardNumber,
                "cardholderName" to cardholderName,
                "cvv" to cvv,
                "expirationDate" to expiryDate,
                "paymentMethod" to paymentMethod,
                "purchaseInfo" to purchaseInfo,
                "transactionDate" to "2024-10-29"  // Replace with actual current date logic
            )).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Clear the cart after successful payment
                    Toast.makeText(this, "purchase successfully made", Toast.LENGTH_LONG).show()
                    clearCart()

                    // Redirect to ShopActivity
                    redirectToShop()
                } else {
                    Toast.makeText(this, "Payment Failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearCart() {
        // Assuming CartPreferences is similar to what you used in CartActivity
        val cartPreferences = CartPreferences(this)
        cartPreferences.saveCartItems(arrayListOf()) // Save an empty cart to SharedPreferences

    }

    private fun redirectToShop() {
        val intent = Intent(this, ShopActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clears the activity stack
        startActivity(intent)
        finish()
    }

}
