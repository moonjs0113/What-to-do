package com.example.whattodo.search

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whattodo.MainActivity
import com.example.whattodo.MyAdapter
import com.example.whattodo.R
import com.example.whattodo.ToDo
import com.example.whattodo.databinding.DialogSortBinding
import com.example.whattodo.databinding.FragmentPriorityBinding
import com.example.whattodo.databinding.FragmentSearchBinding
import com.example.whattodo.manager.Persistence.PersistenceService
import com.example.whattodo.manager.Persistence.toLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale.filter
import java.util.Objects

class SearchFragment : Fragment() {
    lateinit var viewBinding: FragmentSearchBinding
    lateinit var searchRecyclerAdapter: MyAdapter
    lateinit var mainActivity: MainActivity

    lateinit var broadcastReceiver: BroadcastReceiver

    lateinit var currentList : java.util.ArrayList<ToDo>

    enum class SortValue(val title: String) {
        PRIORITY("중요도순"),
        DEADLINE("마감일순"),
        TIMECOST("소요시간순")
    }

    var sortedValue = SortValue.PRIORITY

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent != null)
                {
                    if(intent.hasExtra("message")){
                        CoroutineScope(Dispatchers.IO).launch{
                            currentList = PersistenceService.share.getAllTodo()
                            searchRecyclerAdapter.items = currentList
                            withContext(Dispatchers.Main)
                            {
                                searchRecyclerAdapter.notifyDataSetChanged()
                                searchRecyclerAdapter.CalcItemsPrority()
                                searchRecyclerAdapter.sortItemwithDescendingPriority()
                            }
                        }
                    }else if(intent.hasExtra("colorChanged1"))
                    {
                        val color1 = intent.getStringExtra("colorChanged1")!!
                        val color2 = intent.getStringExtra("colorChanged2")!!
                        val color3 = intent.getStringExtra("colorChanged3")!!

                        searchRecyclerAdapter.setPriorityColor( color1 , color2, color3)
                        searchRecyclerAdapter.notifyDataSetChanged()
                        Log.d("Prioirty", "color received")
                    }else if(intent.hasExtra("spareTimeScalar"))
                    {
                        val spareTimeScalar = intent.getIntExtra("spareTimeScalar", 100)!!
                        val priorityScalar = intent.getIntExtra("priorityScalar", 5)!!
                        val ifLeftTimeChecked = intent.getBooleanExtra("ifLeftTimeChecked", true)!!

                        if(ifLeftTimeChecked)
                        {
                            searchRecyclerAdapter.calculatePriorityListener = object : MyAdapter.OnCalculatePriorityListener{
                                override fun calculatePriority(
                                    item : ToDo
                                ): Float {
                                    val currentTime = LocalDateTime.now()
                                    val remainingTime = if (item.deadLine.toLocalDateTime() > currentTime) {
                                        val duration = Duration.between(currentTime, item.deadLine.toLocalDateTime())
//                val diffHour = (item.deadLine.toLocalDateTime().atZone(ZoneId.systemDefault()).toEpochSecond()/360
//                        - currentTime.atZone(ZoneId.systemDefault()).toEpochSecond()/360)
                                        duration.toHours().toFloat()
                                    } else {                -0.00001f

                                    }

                                    val urgencyFactor = item.time_taken / remainingTime * (spareTimeScalar * 10)
                                    val priorityScore = urgencyFactor + item.importance * priorityScalar

                                    return priorityScore
                                }
                            }
                        }else
                        {
                            searchRecyclerAdapter.calculatePriorityListener = object : MyAdapter.OnCalculatePriorityListener{
                                override fun calculatePriority(
                                    item : ToDo
                                ): Float {
                                    val currentTime = LocalDateTime.now()
                                    val remainingTime = if (item.deadLine.toLocalDateTime() > currentTime) {
                                        val duration = Duration.between(currentTime, item.deadLine.toLocalDateTime())
//                val diffHour = (item.deadLine.toLocalDateTime().atZone(ZoneId.systemDefault()).toEpochSecond()/360
//                        - currentTime.atZone(ZoneId.systemDefault()).toEpochSecond()/360)
                                        duration.toHours().toFloat()
                                    } else {                -0.00001f

                                    }

                                    val urgencyFactor = item.time_taken / remainingTime * spareTimeScalar
                                    val priorityScore = urgencyFactor + item.importance * priorityScalar * 3

                                    return priorityScore
                                }
                            }
                        }

                        searchRecyclerAdapter.CalcItemsPrority()
                        searchRecyclerAdapter.sortItemwithDescendingPriority()
                        searchRecyclerAdapter.notifyDataSetChanged()
                    }
                }
            }

        }

        val intentFilter = IntentFilter("Todo added")
        intentFilter.addAction("color changed")
        intentFilter.addAction("calc changed")
        requireActivity().registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().unregisterReceiver(broadcastReceiver)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PersistenceService.share.registerContext(mainActivity)
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

        CoroutineScope(Dispatchers.IO).launch{
            currentList = PersistenceService.share.getAllTodo()
            withContext(Dispatchers.Main)
            {
                searchRecyclerAdapter  = MyAdapter(currentList)
                searchRecyclerAdapter.sortItemwithAscendingPriority()

                searchRecyclerAdapter.itemLongClickListener = object :
                    MyAdapter.OnLongItemClickListener {

                    override fun OnItemLongClick(position: Int): Boolean {
                        // Todo 객체 삭제
                        val builder = AlertDialog.Builder(mainActivity)
                        builder.setMessage("수정 또는 삭제하시겠습니까?")
                            .setPositiveButton("삭제") { dialog, which ->
                                // 삭제 작업 수행
                                CoroutineScope(Dispatchers.IO).launch{
                                    var list2 = PersistenceService.share.getAllTodo()
                                    PersistenceService.share.deleteTodo(list2[position])
                                }
                                searchRecyclerAdapter.items.removeAt(position)
                                searchRecyclerAdapter.notifyItemChanged(position)
                                dialog.dismiss()
                            }
                            .setNegativeButton("수정") { dialog, which ->
                                // 수정
                                mainActivity.binding.todoInput.setText(searchRecyclerAdapter.items[position].explanation)
                                mainActivity.binding.timeToSpend.setText("${searchRecyclerAdapter.items[position].time_taken.toInt()}시간")
                                val time = searchRecyclerAdapter.items[position].deadLine.split("T")
                                mainActivity.binding.datePickedText.setText("${time[0].split("-")[0]}년 ${time[0].split("-")[1].toInt()}월 " +
                                        "${time[0].split("-")[2].toInt()}일 ${time[1].split(":")[0].toInt()}시 ${time[1].split(":")[1].toInt()}분")
                                mainActivity.binding.importance.setText("${searchRecyclerAdapter.items[position].importance}/10")
                                // 저장될 실제 값 바꾸기
                                mainActivity.deadline = LocalDateTime.now()
                                mainActivity.deadline = mainActivity.deadline.withYear(time[0].split("-")[0].toInt())
                                mainActivity.deadline = mainActivity.deadline.withMonth(time[0].split("-")[1].toInt())
                                mainActivity.deadline = mainActivity.deadline.withDayOfMonth(time[0].split("-")[2].toInt())
                                mainActivity.deadline = mainActivity.deadline.withHour(time[1].split(":")[0].toInt())
                                mainActivity.deadline = mainActivity.deadline.withMinute(time[1].split(":")[1].toInt())
                                mainActivity.importanceVal = searchRecyclerAdapter.items[position].importance
                                mainActivity.timeToSpendVal = searchRecyclerAdapter.items[position].time_taken.toInt()

                                // 수정 모드로 변경 - 등록 버튼이 수정 버튼으로 변경. 수정 버튼을 누르기 전까지는 모드 해제 불가
                                mainActivity.isAmend = true
                                mainActivity.binding.registerBtn.text = "수정"
                                mainActivity.idToAmend = searchRecyclerAdapter.items[position].id
                                if(!mainActivity.isInputFormOpen) {
                                    mainActivity.animator.start()
                                }
                                dialog.dismiss()
                            }
                            .show()
                        return true
                    }
                }

                searchRecyclerAdapter.CalcItemsPrority()
                searchRecyclerAdapter.sortItemwithDescendingPriority()
                viewBinding.searchRecyclerView.adapter = searchRecyclerAdapter

            }
        }
    }

    private fun setLayoutListener() {
        viewBinding.searchEditText.addTextChangedListener {
            searchRecyclerAdapter.items =
                if
                        (it.toString().isBlank()) currentList
                else
                    currentList.filter { todo ->
                        todo.explanation.contains(it.toString())
                    } as ArrayList
            searchRecyclerAdapter.notifyDataSetChanged()
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

                var flag = 0
                sortedValue = SortValue.values()[when(dialogViewBinding.radioGroup.checkedRadioButtonId) {
                    dialogViewBinding.prioritySortButton.id ->{
                        flag = 0
                        0
                    }
                    dialogViewBinding.deadLineSortButton.id ->{
                        flag = 1
                        1
                    }
                    dialogViewBinding.timeCostSortButton.id -> {
                        flag = 2
                        2
                    }
                    else -> {
                        flag = 0
                        0
                    }
                }]
                viewBinding.sortButton.text = sortedValue.title

                when(flag) {
                    0 -> searchRecyclerAdapter.sortItemwithDescendingPriority()
                    1 -> searchRecyclerAdapter.sortItemwithDescendingDeadLine()
                    2 -> searchRecyclerAdapter.sortItemwithDescendingTimeTaken()
                }

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