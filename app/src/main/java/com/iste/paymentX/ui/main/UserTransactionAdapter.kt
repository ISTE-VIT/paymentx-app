package com.iste.paymentX.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iste.paymentX.R
import com.iste.paymentX.data.model.Transaction

class UserTransactionAdapter(
    private val transactionList: List<Transaction>
) : RecyclerView.Adapter<UserTransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val merchantName: TextView = itemView.findViewById(R.id.transaction_merchant_name)
        val amount: TextView = itemView.findViewById(R.id.transaction_amount)
        val status: TextView = itemView.findViewById(R.id.transaction_status)
        val timestamp: TextView = itemView.findViewById(R.id.transaction_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.merchantName.text = "Merchant: ${transaction.merchantName ?: "Unknown"}"
        holder.amount.text = "Amount: ₹${transaction.amount}"
        holder.status.text = "Status: ${transaction.status}"
        holder.timestamp.text = "Date: ${formatTimestamp(transaction.timestamp)}"
    }

    private fun formatTimestamp(timestamp: String): String {
        // Simple formatting for readability - can be enhanced with proper date formatting
        return try {
            // Example input: "2025-01-07T12:34:56Z"
            val parts = timestamp.split("T")
            val date = parts[0]
            val time = parts[1].substringBefore("Z")
            "$date $time"
        } catch (e: Exception) {
            timestamp // Return original if parsing fails
        }
    }

    override fun getItemCount(): Int = transactionList.size
}