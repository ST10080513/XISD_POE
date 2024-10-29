import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xisd_poe.R
import com.example.xisd_poe.models.PaymentHistoryItem

class PaymentHistoryAdapter(private var paymentHistoryList: ArrayList<PaymentHistoryItem>) :
    RecyclerView.Adapter<PaymentHistoryAdapter.PaymentHistoryViewHolder>() {

    // This method will update the data in the adapter
    fun updateData(newPaymentHistoryList: ArrayList<PaymentHistoryItem>) {
        paymentHistoryList.clear() // Clear the old list
        paymentHistoryList.addAll(newPaymentHistoryList) // Add all new data
        notifyDataSetChanged() // Notify the adapter about the dataset changes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentHistoryViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_history, parent, false)
        return PaymentHistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PaymentHistoryViewHolder, position: Int) {
        val currentItem = paymentHistoryList[position]

        holder.paymentIdTextView.text = "Transaction ID: ${currentItem.id}"
        holder.paymentMethodTextView.text = "Payment Method: ${currentItem.paymentMethod}"
        holder.amountTextView.text = "Amount: R${currentItem.amount}"
        holder.transactionDateTextView.text = "Transaction Date: ${currentItem.transactionDate}"

        // Clear previous product views
        holder.productDetailsContainer.removeAllViews()

        // Loop through purchaseInfo map and display each product's details
        currentItem.purchaseInfo.forEach { (productName, details) ->
            val productQuantity = (details as? Map<*, *>)?.get("quantity") ?: 1
            val productPrice = (details as? Map<*, *>)?.get("price") ?: 0.0
            val productTotal = productQuantity.toString().toInt() * productPrice.toString().toDouble()

            // Product Name
            val productNameTextView = TextView(holder.itemView.context).apply {
                text = "• $productName"
                textSize = 14f
                setTextColor(holder.itemView.context.getColor(android.R.color.black))
            }

            // Quantity
            val productQuantityTextView = TextView(holder.itemView.context).apply {
                text = "Quantity: $productQuantity"
                textSize = 12f
                setTextColor(holder.itemView.context.getColor(android.R.color.black))
            }

            // Price per unit
            val productPriceTextView = TextView(holder.itemView.context).apply {
                text = "Price per unit: R$productPrice"
                textSize = 12f
                setTextColor(holder.itemView.context.getColor(android.R.color.black))
            }

            // Total price
            val productTotalTextView = TextView(holder.itemView.context).apply {
                text = "Total: R$productTotal"
                textSize = 12f
                setTextColor(holder.itemView.context.getColor(android.R.color.black))
            }

            // Add views to the container
            holder.productDetailsContainer.addView(productNameTextView)
            holder.productDetailsContainer.addView(productQuantityTextView)
            holder.productDetailsContainer.addView(productPriceTextView)
            holder.productDetailsContainer.addView(productTotalTextView)
        }
    }


    override fun getItemCount(): Int = paymentHistoryList.size

    class PaymentHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentIdTextView: TextView = itemView.findViewById(R.id.paymentIdTextView)
        val paymentMethodTextView: TextView = itemView.findViewById(R.id.paymentMethodTextView)
        val amountTextView: TextView = itemView.findViewById(R.id.amountTextView)
        val transactionDateTextView: TextView = itemView.findViewById(R.id.transactionDateTextView)
        val productDetailsContainer: LinearLayout = itemView.findViewById(R.id.productDetailsContainer)
    }


}
