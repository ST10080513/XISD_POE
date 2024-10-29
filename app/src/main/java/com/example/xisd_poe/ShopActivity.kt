package com.example.xisd_poe

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import androidx.appcompat.app.ActionBarDrawerToggle
import java.util.Locale
import androidx.appcompat.widget.SearchView


class ShopActivity : AppCompatActivity() {

    // Firebase database reference
    private lateinit var database: FirebaseDatabase
    private lateinit var productRef: DatabaseReference

    // Cart to store selected products
    private val cartItems = mutableListOf<Pair<String, Double>>()
    private val productList = mutableListOf<Product>() // List to store products from Firebase
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartPreferences: CartPreferences
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var searchView: SearchView
    private val filteredProductList = mutableListOf<Product>() // List for filtered products


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        // Initialize the toolbar
        toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)

        // Initialize the DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout)

        // Set up ActionBarDrawerToggle for the hamburger menu
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle item clicks in the navigation drawer
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_history -> {
                    val intent = Intent(this, PaymentHistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_cart -> {
                    val intent = Intent(this, QRcode::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Close the drawer after item click
            true
        }

        // Initialize CartPreferences
        cartPreferences = CartPreferences(this)

        // Load existing cart items from SharedPreferences
        cartItems.addAll(cartPreferences.getCartItems())

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        productRef = database.getReference("products")

        // Setup ListView and adapter
        val productListView = findViewById<ListView>(R.id.productListView)
        productAdapter = ProductAdapter(this, filteredProductList) // Use filteredProductList
        productListView.adapter = productAdapter

        searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterProducts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText)
                return true
            }
        })

        // Fetch products from Firebase
        fetchProductsFromFirebase()

        // View Cart button
        val btnViewCart = findViewById<Button>(R.id.btnViewCart)
        btnViewCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
    }

    private fun filterProducts(query: String?) {
        val lowerCaseQuery = query?.toLowerCase(Locale.getDefault()) ?: ""

        filteredProductList.clear()
        if (lowerCaseQuery.isEmpty()) {
            filteredProductList.addAll(productList)
        } else {
            for (product in productList) {
                if (product.name.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    product.shortDesc.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)
                ) {
                    filteredProductList.add(product)
                }
            }
        }
        productAdapter.notifyDataSetChanged() // Update the adapter with filtered products
    }

    private fun fetchProductsFromFirebase() {
        productRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear() // Clear the list before adding new data
                for (productSnapshot in snapshot.children) {
                    val product = productSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        productList.add(product)
                    }
                }
                filterProducts(searchView.query.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ShopActivity, "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Custom Adapter to display the products
    inner class ProductAdapter(private val context: ShopActivity, private val dataSource: List<Product>) :
        ArrayAdapter<Product>(context, R.layout.product_item, dataSource) {

        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
            val inflater = context.layoutInflater
            val rowView = inflater.inflate(R.layout.product_item, parent, false)

            val product = dataSource[position]
// In ProductAdapter's getView method

             // Set the short description text

            // Populate views with product data
            val productName = rowView.findViewById<TextView>(R.id.productName)
            val productPrice = rowView.findViewById<TextView>(R.id.productPrice)
            val productImage = rowView.findViewById<ImageView>(R.id.productImage) // ImageView for product image
            val btnAddToCart = rowView.findViewById<Button>(R.id.btnAddToCart)
            val productShortDesc = rowView.findViewById<TextView>(R.id.productShortDesc)
            productName.text = product.name
            productPrice.text = "Price: R${product.sellPrice}"
            productShortDesc.text = product.shortDesc
            // Load the first image from the images list using Glide
            if (product.images.isNotEmpty()) {
                Glide.with(context)
                    .load(product.images[0]) // Load the first image
                    .into(productImage)
            }

            btnAddToCart.setOnClickListener {
                cartItems.add(Pair(product.name, product.sellPrice.toDouble()))
                cartPreferences.saveCartItems(cartItems) // Save updated cart to SharedPreferences
                Toast.makeText(context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
            }

            return rowView
        }
    }
}

