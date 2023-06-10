package com.example.whattodo.search

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whattodo.MainActivity
import com.example.whattodo.MyAdapter
import com.example.whattodo.R
import com.example.whattodo.ToDo
import com.example.whattodo.databinding.FragmentPriorityBinding
import com.example.whattodo.databinding.FragmentSearchBinding
import com.example.whattodo.manager.Persistence.PersistenceService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var viewBinding: FragmentSearchBinding
    lateinit var adapter: MyAdapter

    lateinit var mainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentSearchBinding.inflate(inflater, container, false)
        setupRecyclerView()
        return viewBinding.root
    }

    private fun setupRecyclerView() {
        viewBinding.searchRecyclerView.layoutManager = LinearLayoutManager(context)

        CoroutineScope(Dispatchers.IO).launch{
            var list = PersistenceService.share.getAllTodo(mainActivity)

            withContext(Dispatchers.Main)
            {
                adapter  = MyAdapter(list)
                adapter.sortItemwithAscendingPriority()

                adapter.itemLongClickListener = object :MyAdapter.OnItemClickListener,
                    MyAdapter.OnLongItemClickListener {
                    override fun OnItemClick(position: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun OnItemLongClick(position: Int): Boolean {
                        // Todo 객체 삭제
                        val builder = AlertDialog.Builder(mainActivity)
                        builder.setMessage("수정 또는 삭제하시겠습니까?")
                            .setPositiveButton("삭제") { dialog, which ->
                                // 삭제 작업 수행
                                CoroutineScope(Dispatchers.IO).launch {
                                    PersistenceService.share.registerContext(mainActivity)
                                    var list2 = PersistenceService.share.getAllTodo(mainActivity)
                                    PersistenceService.share.deleteTodo(list2[position])
                                }
                                adapter.items.removeAt(position)
                                adapter.notifyDataSetChanged()
                                dialog.dismiss()
                            }
                            .setNegativeButton("수정") { dialog, which ->
                                // 수정
                                mainActivity.binding.todoInput.setText(adapter.items[position].explanation)
                                mainActivity.binding.timeToSpend.setText("${adapter.items[position].time_taken.toInt()}시간")
                                val time = adapter.items[position].deadLine.split("T")
                                mainActivity.binding.datePickedText.setText("${time[0].split("-")[0]}년 ${time[0].split("-")[1].toInt()}월 " +
                                        "${time[0].split("-")[2].toInt()}일 ${time[1].split(":")[0].toInt()}시 ${time[1].split(":")[1].toInt()}분")
                                mainActivity.binding.importance.setText("${adapter.items[position].importance}/10")

                                mainActivity.deadline = mainActivity.deadline.withYear(time[0].split("-")[0].toInt())
                                mainActivity.deadline = mainActivity.deadline.withMonth(time[0].split("-")[1].toInt())
                                mainActivity.deadline = mainActivity.deadline.withDayOfMonth(time[0].split("-")[2].toInt())
                                mainActivity.deadline = mainActivity.deadline.withHour(time[1].split(":")[0].toInt())
                                mainActivity.deadline = mainActivity.deadline.withMinute(time[1].split(":")[1].toInt())

                                mainActivity.importanceVal = adapter.items[position].importance
                                mainActivity.timeToSpendVal = adapter.items[position].time_taken.toInt()


                                // 수정 모드로 변경 - 등록 버튼이 수정 버튼으로 변경. 수정 버튼을 누르기 전까지는 모드 해제 불가
                                mainActivity.isAmend = true
                                mainActivity.binding.registerBtn.text = "수정"
                                if(mainActivity.animator != null && !mainActivity.isInputFormOpen) {
                                    mainActivity.animator.start()
                                    mainActivity.isInputFormOpen = true
                                }
                                mainActivity.idToAmend = adapter.items[position].id

                                dialog.dismiss()
                            }
                            .show()
                        return true
                    }
                }

                adapter.CalcItemsPrority()
                adapter.sortItemwithDescendingPriority()
                viewBinding.searchRecyclerView.adapter = adapter
            }
        }


    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}