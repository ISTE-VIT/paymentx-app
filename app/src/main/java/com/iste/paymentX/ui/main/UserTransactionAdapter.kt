package com.iste.paymentX.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iste.paymentX.R
import com.iste.paymentX.data.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class UserTransactionAdapter(
    private val transactionList: List<Transaction>
) : RecyclerView.Adapter<UserTransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.transaction_name)
        val date: TextView = itemView.findViewById(R.id.transaction_date)
        val time: TextView = itemView.findViewById(R.id.transaction_time)
        val amount: TextView = itemView.findViewById(R.id.transaction_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]

        // Set name (using merchant name or user name based on your model)
        holder.name.text = transaction.merchantName ?: "Unknown"

        // For debugging - print the timestamp to logcat
        android.util.Log.d("TransactionAdapter", "Original timestamp: ${transaction.timestamp}")

        // Format and set date and time
        val dateTimeFormatted = formatTimestamp(transaction.timestamp)
        holder.date.text = dateTimeFormatted.first
        holder.time.text = dateTimeFormatted.second

        // Set amount with currency symbol
        holder.amount.text = "₹${transaction.amount}"
    }

    private fun formatTimestamp(timestamp: String): Pair<String, String> {
        // First, try multiple timestamp formats to handle different possibilities
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss'Z'",  // ISO format with Z
            "yyyy-MM-dd'T'HH:mm:ssX",    // ISO format with timezone offset
            "yyyy-MM-dd'T'HH:mm:ss",     // ISO format without timezone
            "yyyy-MM-dd HH:mm:ss",       // Simple datetime format
            "yyyy-MM-dd"                 // Just date format
        )

        for (formatPattern in possibleFormats) {
            try {
                val inputFormat = SimpleDateFormat(formatPattern, Locale.getDefault())
                val date = inputFormat.parse(timestamp) ?: continue

                // Format date like "February 16, 2024"
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(date)

                // Format time like "10:23 AM"
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val formattedTime = timeFormat.format(date)

                return Pair(formattedDate, formattedTime)
            } catch (e: Exception) {
                // Try next format
                continue
            }
        }

        // If we reach here, try to parse the string manually
        try {
            // For debugging - log the actual timestamp format
            android.util.Log.d("TransactionAdapter", "Failed to parse timestamp: $timestamp")

            // Example: if timestamp is simply a long value (milliseconds since epoch)
            if (timestamp.matches(Regex("\\d+"))) {
                val timeMillis = timestamp.toLong()
                val date = java.util.Date(timeMillis)

                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                return Pair(dateFormat.format(date), timeFormat.format(date))
            }

            // Fall back to manual string parsing
            // Example: if format is "2024-02-16 10:23:45"
            val parts = timestamp.split(" ", "T")
            if (parts.size >= 1) {
                val datePart = parts[0].split("-")
                if (datePart.size == 3) {
                    val year = datePart[0].toInt()
                    val month = datePart[1].toInt() - 1 // 0-based months in Calendar
                    val day = datePart[2].toInt()

                    val calendar = java.util.Calendar.getInstance()
                    calendar.set(year, month, day)

                    // Set time if available
                    if (parts.size >= 2) {
                        val timePart = parts[1].split(":")
                        if (timePart.size >= 2) {
                            val hour = timePart[0].toInt()
                            val minute = timePart[1].toInt()
                            val second = if (timePart.size >= 3) timePart[2].replace("Z", "").toInt() else 0

                            calendar.set(java.util.Calendar.HOUR_OF_DAY, hour)
                            calendar.set(java.util.Calendar.MINUTE, minute)
                            calendar.set(java.util.Calendar.SECOND, second)
                        }
                    }

                    val date = calendar.time
                    val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                    return Pair(dateFormat.format(date), timeFormat.format(date))
                }
            }

            // If all else fails, show at least part of the original timestamp
            return Pair(timestamp.split("T", " ")[0], "")
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair("February 16, 2024", "10:23 AM") // Default fallback values
        }
    }

    override fun getItemCount(): Int = transactionList.size
}