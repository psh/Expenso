package dev.spikeysanju.expensetracker.dashboard

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dev.spikeysanju.expensetracker.R
import dev.spikeysanju.expensetracker.databinding.FragmentDashboardBinding
import dev.spikeysanju.expensetracker.repo.TransactionModel
import dev.spikeysanju.expensetracker.main.ExportState
import dev.spikeysanju.expensetracker.main.ViewState
import dev.spikeysanju.expensetracker.utils.BaseFragment
import dev.spikeysanju.expensetracker.main.TransactionViewModel
import dev.spikeysanju.expensetracker.utils.hide
import dev.spikeysanju.expensetracker.utils.indianRupee
import dev.spikeysanju.expensetracker.utils.show
import dev.spikeysanju.expensetracker.utils.snack
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.math.abs

class DashboardFragment :
    BaseFragment<FragmentDashboardBinding, TransactionViewModel>() {
    private lateinit var transactionAdapter: TransactionAdapter
    override val viewModel: TransactionViewModel by sharedViewModel()

    private val csvCreateRequestLauncher =
        registerForActivityResult(CreateCsvContract()) { uri: Uri? ->
            if (uri != null) {
                exportCSV(uri)
            } else {
                binding.root.snack(
                    string = R.string.failed_transaction_export
                )
            }
        }

    private val previewCsvRequestLauncher = registerForActivityResult(OpenCsvContract()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRV()
        initViews()
        observeFilter()
        observeTransaction()
        swipeToDelete()
    }

    private fun observeFilter() = with(binding) {
        lifecycleScope.launchWhenCreated {
            viewModel.transactionFilter.collect { filter ->
                when (filter) {
                    "Overall" -> {
                        totalBalanceView.totalBalanceTitle.text =
                            getString(R.string.text_total_balance)
                        totalIncomeExpenseView.show()
                        incomeCardView.totalTitle.text = getString(R.string.text_total_income)
                        expenseCardView.totalTitle.text = getString(R.string.text_total_expense)
                        expenseCardView.totalIcon.setImageResource(R.drawable.ic_expense)
                    }
                    "Income" -> {
                        totalBalanceView.totalBalanceTitle.text =
                            getString(R.string.text_total_income)
                        totalIncomeExpenseView.hide()
                    }
                    "Expense" -> {
                        totalBalanceView.totalBalanceTitle.text =
                            getString(R.string.text_total_expense)
                        totalIncomeExpenseView.hide()
                    }
                }
                viewModel.getAllTransaction(filter)
            }
        }
    }

    private fun setupRV() = with(binding) {
        transactionAdapter = TransactionAdapter()
        transactionRv.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun swipeToDelete() {
        // init item touch callback for swipe action
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // get item position & delete notes
                val position = viewHolder.absoluteAdapterPosition
                val transactionItem = transactionAdapter.currentList[position]
                viewModel.deleteTransaction(transactionItem)
                Snackbar.make(
                    binding.root,
                    getString(R.string.success_transaction_delete),
                    Snackbar.LENGTH_LONG
                ).apply {
                        setAction(getString(R.string.text_undo)) {
                            viewModel.insertTransaction(
                                transactionItem
                            )
                        }
                        show()
                    }
            }
        }

        // attach swipe callback to rv
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.transactionRv)
        }
    }

    private fun onTotalTransactionLoaded(transaction: List<TransactionModel>) = with(binding) {
        val (totalIncome, totalExpense) = transaction.partition { it.transactionType == "Income" }
        val income = totalIncome.sumOf { it.amount }
        val expense = totalExpense.sumOf { it.amount }
        incomeCardView.total.text = "+ ".plus(indianRupee(income))
        expenseCardView.total.text = "- ".plus(indianRupee(expense))
        totalBalanceView.totalBalance.text = indianRupee(income - expense)
    }

    private fun observeTransaction() = lifecycleScope.launchWhenStarted {
        viewModel.uiState.collect { uiState ->
            when (uiState) {
                is ViewState.Loading -> {
                }
                is ViewState.Success -> {
                    showAllViews()
                    onTransactionLoaded(uiState.transaction)
                    onTotalTransactionLoaded(uiState.transaction)
                }
                is ViewState.Error -> {
                    binding.root.snack(
                        string = R.string.text_error
                    )
                }
                is ViewState.Empty -> {
                    hideAllViews()
                }
            }
        }
    }

    private fun showAllViews() = with(binding) {
        dashboardGroup.show()
        emptyStateLayout.hide()
        transactionRv.show()
    }

    private fun hideAllViews() = with(binding) {
        dashboardGroup.hide()
        emptyStateLayout.show()
    }

    private fun onTransactionLoaded(list: List<TransactionModel>) =
        transactionAdapter.submitList(list)

    private fun initViews() = with(binding) {
        btnAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addTransactionFragment)
        }

        mainDashboardScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, sY, _, oY ->
                if (abs(sY - oY) > 10) {
                    when {
                        sY > oY -> btnAddTransaction.hide()
                        oY > sY -> btnAddTransaction.show()
                    }
                }
            }
        )

        transactionAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("transaction", it)
            }
            findNavController().navigate(
                R.id.action_dashboardFragment_to_transactionDetailsFragment,
                bundle
            )
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentDashboardBinding.inflate(inflater, container, false)

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_ui, menu)

        val item = menu.findItem(R.id.spinner)
        val spinner = item.actionView as Spinner

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.allFilters,
            R.layout.item_filter_dropdown
        )
        adapter.setDropDownViewResource(R.layout.item_filter_dropdown)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                lifecycleScope.launchWhenStarted {
                    when (position) {
                        0 -> {
                            viewModel.overall()
                            (view as TextView).setTextColor(blackColor())
                        }
                        1 -> {
                            viewModel.allIncome()
                            (view as TextView).setTextColor(blackColor())
                        }
                        2 -> {
                            viewModel.allExpense()
                            (view as TextView).setTextColor(blackColor())
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                lifecycleScope.launchWhenStarted {
                    viewModel.overall()
                }
            }
        }

        // Set the item state
        lifecycleScope.launchWhenStarted {
            val isChecked = viewModel.getUIMode.first()
            val uiMode = menu.findItem(R.id.action_night_mode)
            uiMode.isChecked = isChecked
            setUIMode(uiMode, isChecked)
        }
    }

    private fun blackColor() = ResourcesCompat.getColor(resources, R.color.black, null)

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        return when (item.itemId) {
            R.id.action_night_mode -> {
                item.isChecked = !item.isChecked
                setUIMode(item, item.isChecked)
                true
            }

            R.id.action_about -> {
                findNavController().navigate(R.id.action_dashboardFragment_to_aboutFragment)
                true
            }

            R.id.action_export -> {
                val csvFileName = "expenso_${System.currentTimeMillis()}"
                csvCreateRequestLauncher.launch(csvFileName)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportCSV(csvFileUri: Uri) {
        viewModel.exportTransactionsToCsv(csvFileUri)
        lifecycleScope.launchWhenCreated {
            viewModel.exportCsvState.collect { state ->
                when (state) {
                    ExportState.Empty -> {
                        /*do nothing*/
                    }
                    is ExportState.Error -> {
                        binding.root.snack(
                            string = R.string.failed_transaction_export
                        )
                    }
                    ExportState.Loading -> {
                        /*do nothing*/
                    }
                    is ExportState.Success -> {
                        binding.root.snack(string = R.string.success_transaction_export) {
                            action(text = R.string.text_open) {
                                previewCsvRequestLauncher.launch(state.fileUri)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setUIMode(item: MenuItem, isChecked: Boolean) {
        if (isChecked) {
            viewModel.setDarkMode(true)
            item.setIcon(R.drawable.ic_night)
        } else {
            viewModel.setDarkMode(false)
            item.setIcon(R.drawable.ic_day)
        }
    }

    private fun Snackbar.action(
        @StringRes text: Int,
        color: Int? = null,
        listener: (View) -> Unit
    ) {
        setAction(text, listener)
        color?.let { setActionTextColor(ContextCompat.getColor(context, color)) }
    }
}