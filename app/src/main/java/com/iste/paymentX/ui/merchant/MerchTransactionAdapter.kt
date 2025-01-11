package com.iste.paymentX.ui.merchant


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iste.paymentX.R
import com.iste.paymentX.data.model.Transaction

class MerchTransactionAdapter(
    private val transactionList: List<Transaction>
) : RecyclerView.Adapter<MerchTransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.transaction_user_name)
        val amount: TextView = itemView.findViewById(R.id.transaction_amount)
        val status: TextView = itemView.findViewById(R.id.transaction_status)
        val timestamp: TextView = itemView.findViewById(R.id.transaction_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.merchant_transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.userName.text = "User: ${transaction.userName}"
        holder.amount.text = "Amount: ₹${transaction.amount}"
        holder.status.text = "Status: ${transaction.status}"
        holder.timestamp.text = "Timestamp: ${transaction.timestamp}"
    }

    override fun getItemCount(): Int = transactionList.size
}
