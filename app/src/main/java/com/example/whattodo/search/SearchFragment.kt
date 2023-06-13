package com.example.whattodo.search

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whattodo.MainActivity
import com.example.whattodo.MyAdapter
import com.example.whattodo.R
import com.example.whattodo.ToDo
import com.example.whattodo.databinding.DialogSortBinding
import com.example.whattodo.databinding.FragmentPriorityBinding
import com.example.whattodo.databinding.FragmentSearchBinding
import java.util.Objects

class SearchFragment : Fragment() {
    lateinit var viewBinding: FragmentSearchBinding
    lateinit var searchRecyclerAdapter: MyAdapter
    lateinit var mainActivity: MainActivity

    enum class SortValue(val title: String) {
        PRIORITY("중요도순"),
        DEADLINE("마감일순"),
        TIMECOST("소요시간순")
    }

    var sortedValue = SortValue.PRIORITY

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentSearchBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setLayoutListener()
        return viewBinding.root
    }

    private fun setupRecyclerView() {
        viewBinding.searchRecyclerView.layoutManager = LinearLayoutManager(context)
        searchRecyclerAdapter = MyAdapter(ToDo.previewData)
        searchRecyclerAdapter.CalcItemsPrority()
        searchRecyclerAdapter.sortItemwithDescendingPriority()
        viewBinding.searchRecyclerView.adapter = searchRecyclerAdapter
    }

    private fun setLayoutListener() {
        viewBinding.searchEditText.setOnEditorActionListener { textView, i, keyEvent ->

            true
        }
        viewBinding.sortButton.setOnClickListener {
            val dialogViewBinding = DialogSortBinding.inflate(layoutInflater)
            val build = AlertDialog.Builder(mainActivity).apply {
                setView(dialogViewBinding.root)
            }

            val dialog = build.create()

            dialogViewBinding.radioGroup.check(
                when(sortedValue) {
                    SortValue.PRIORITY -> dialogViewBinding.prioritySortButton.id
                    SortValue.DEADLINE -> dialogViewBinding.deadLineSortButton.id
                    SortValue.TIMECOST -> dialogViewBinding.timeCostSortButton.id
                }
            )
            dialogViewBinding.applyButton.setOnClickListener {
                sortedValue = SortValue.values()[when(dialogViewBinding.radioGroup.checkedRadioButtonId) {
                    dialogViewBinding.prioritySortButton.id -> 0
                    dialogViewBinding.deadLineSortButton.id -> 1
                    dialogViewBinding.timeCostSortButton.id -> 2
                    else -> 0
                }]
                viewBinding.sortButton.text = sortedValue.title
                dialog.dismiss()
            }
            dialogViewBinding.cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
        viewBinding.completeShowSwitch.setOnCheckedChangeListener { _, b ->

        }
    }

}