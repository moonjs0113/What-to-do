package com.example.whattodo

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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whattodo.databinding.FragmentDeadlineBinding
import com.example.whattodo.manager.Persistence.PersistenceService
import com.example.whattodo.manager.Persistence.toLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DeadlineFragment : Fragment() {
    var binding:FragmentDeadlineBinding?=null
    var adapter: MyAdapter = MyAdapter(ArrayList<ToDo>())

    //선택된 날짜 데이터 초기값은 오늘날짜
    var seletecdDate: LocalDateTime = LocalDateTime.now()

    lateinit var mainActivity: MainActivity

    lateinit var broadcastReceiver: BroadcastReceiver

    //설정된 날짜로 필터링된 리스트
    val fliteredList = arrayListOf<ToDo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("DeadLine", "DeadLine onCreate")
        PersistenceService.share.registerContext(mainActivity)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity

        broadcastReceiver = object : BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent != null)
                {
                    if(intent.hasExtra("message")){
                        CoroutineScope(Dispatchers.IO).launch{
                            filterListByDate()
                            withContext(Dispatchers.Main)
                            {
                                adapter.CalcItemsPrority()
                                adapter.sortItemwithDescendingPriority()
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }else if(intent.hasExtra("colorChanged1"))
                    {
                        val color1 = intent.getStringExtra("colorChanged1")!!
                        val color2 = intent.getStringExtra("colorChanged2")!!
                        val color3 = intent.getStringExtra("colorChanged3")!!

                        adapter.setPriorityColor(color1 , color2, color3)
                        adapter.notifyDataSetChanged()
                        Log.d("DeadLine", "color received")
                    }else if(intent.hasExtra("spareTimeScalar"))
                    {
                        val spareTimeScalar = intent.getIntExtra("spareTimeScalar", 100)!!
                        val priorityScalar = intent.getIntExtra("priorityScalar", 5)!!
                        val ifLeftTimeChecked = intent.getBooleanExtra("ifLeftTimeChecked", true)!!

                        if(ifLeftTimeChecked)
                        {
                            adapter.calculatePriorityListener = object : MyAdapter.OnCalculatePriorityListener{
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
                            adapter.calculatePriorityListener = object : MyAdapter.OnCalculatePriorityListener{
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

                        adapter.CalcItemsPrority()
                        adapter.sortItemwithDescendingPriority()
                        adapter.notifyDataSetChanged()
                    }
                }
            }

        }

        val intentFilter = IntentFilter("Todo added")
        intentFilter.addAction("color changed")
        intentFilter.addAction("calc changed")
        intentFilter.addAction("todo Checked")
        requireActivity().registerReceiver(broadcastReceiver, intentFilter)

    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().unregisterReceiver(broadcastReceiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeadlineBinding.inflate(layoutInflater, container, false)

        //달력 날짜 변경 이벤트 처리
        binding!!.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            seletecdDate = LocalDateTime.of(year, month+1, dayOfMonth, 0,0)

            filterListByDate()

        }

        //리스트 내의 아이템 클릭시 이벤트 처리
        //아직 미구현(뭘 하기로 했는지 기억이...)
        adapter.itemClickListener = object :MyAdapter.OnItemClickListener{
            override fun OnItemClick(position: Int) {
                val newToDo = adapter.items[position]
                newToDo.isComplete = !(newToDo.isComplete)

                CoroutineScope(Dispatchers.IO).launch {
                    PersistenceService.share.registerContext(mainActivity)
                    // 수정 모드인 경우
                    PersistenceService.share.updateTodo(newToDo)
                    withContext(Dispatchers.Main)
                    {
                        val intent = Intent("Todo added");
                        intent.putExtra("message","dataChanged");
                        mainActivity.sendBroadCastInMainActivity(intent)
                    }
                }
            }
        }

        adapter.itemLongClickListener = object :
            MyAdapter.OnLongItemClickListener {

            override fun OnItemLongClick(position: Int): Boolean {
                // Todo 객체 삭제
                val builder = AlertDialog.Builder(mainActivity)
                builder.setMessage("수정 또는 삭제하시겠습니까?")
                    .setPositiveButton("삭제") { dialog, which ->
                        // 삭제 작업 수행
                        CoroutineScope(Dispatchers.IO).launch{
                            PersistenceService.share.registerContext(mainActivity)
                            var list2 = PersistenceService.share.getAllTodo()
                            PersistenceService.share.deleteTodo(list2[position])
                        }
                        adapter.items.removeAt(position)
                        adapter.notifyItemChanged(position)
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
                        // 저장될 실제 값 바꾸기
                        mainActivity.deadline = LocalDateTime.now()
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
                        mainActivity.idToAmend = adapter.items[position].id
                        if(!mainActivity.isInputFormOpen) {
                            mainActivity.animator.start()
                        }
                        dialog.dismiss()
                    }
                    .show()
                return true
            }
        }

        //리사이클러뷰 초기설정
        binding!!.prioirtyRecyclerView.layoutManager = LinearLayoutManager(context)
        binding!!.prioirtyRecyclerView.adapter = this@DeadlineFragment.adapter

        //시작하면서 현재 날짜로 필터링
        filterListByDate()


        return binding!!.root
    }

    private fun filterListByDate() {
        fliteredList.clear()
        CoroutineScope(Dispatchers.IO).launch{
            val list = PersistenceService.share.getAllTodo()
            withContext(Dispatchers.Main)
            {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                for(item in list){
                    if(LocalDate.parse(item.deadLine.substring(0,10), formatter).isEqual(seletecdDate.toLocalDate())){
                        fliteredList.add(item)
                    }
                }
                adapter.items = fliteredList
                adapter.CalcItemsPrority()
                adapter.sortItemwithDescendingPriority()
                adapter.notifyDataSetChanged()
            }
        }

    }
}