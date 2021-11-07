package dev.spikeysanju.expensetracker.view.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.spikeysanju.expensetracker.R
import dev.spikeysanju.expensetracker.databinding.ItemTransactionLayoutBinding
import dev.spikeysanju.expensetracker.repo.TransactionModel
import indianRupee

class TransactionAdapter : ListAdapter<TransactionModel, TransactionVH>(DifferCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TransactionVH(
        ItemTransactionLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: TransactionVH, position: Int) {

        val item: TransactionModel = getItem(position)
        holder.binding.apply {

            transactionName.text = item.title
            transactionCategory.text = item.tag
            transactionAmount.text = formatAmount(item)

            transactionAmount.setTextColor(
                ContextCompat.getColor(
                    transactionAmount.context,
                    if (item.transactionType == "Income") R.color.income else R.color.expense
                )
            )

            transactionIconView.setImageResource(
                when (item.tag) {
                    "Housing" -> R.drawable.ic_food
                    "Transportation" -> R.drawable.ic_transport
                    "Food" -> R.drawable.ic_food
                    "Utilities" -> R.drawable.ic_utilities
                    "Insurance" -> R.drawable.ic_insurance
                    "Healthcare" -> R.drawable.ic_medical
                    "Saving & Debts" -> R.drawable.ic_savings
                    "Personal Spending" -> R.drawable.ic_personal_spending
                    "Entertainment" -> R.drawable.ic_entertainment
                    "Miscellaneous" -> R.drawable.ic_others
                    else -> R.drawable.ic_others
                }
            )

            // on item click
            holder.itemView.setOnClickListener {
                onItemClickListener?.let { it(item) }
            }
        }
    }

    private fun formatAmount(item: TransactionModel): String {
        return when (item.transactionType) {
            "Income" -> "+ ".plus(indianRupee(item.amount))
            "Expense" -> "- ".plus(indianRupee(item.amount))
            else -> ""
        }
    }

    // on item click listener
    private var onItemClickListener: ((TransactionModel) -> Unit)? = null
    fun setOnItemClickListener(listener: (TransactionModel) -> Unit) {
        onItemClickListener = listener
    }
}

class TransactionVH(val binding: ItemTransactionLayoutBinding) : RecyclerView.ViewHolder(binding.root)

private object DifferCallback : DiffUtil.ItemCallback<TransactionModel>() {
    override fun areItemsTheSame(oldItem: TransactionModel, newItem: TransactionModel): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TransactionModel, newItem: TransactionModel): Boolean {
        return oldItem == newItem
    }
}
