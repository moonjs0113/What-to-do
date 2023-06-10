package com.example.whattodo

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whattodo.databinding.SimpleViewHolderBinding
import com.example.whattodo.manager.Persistence.toDate
import com.example.whattodo.manager.Persistence.toLocalDateTime
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.Comparator

// 확장성 기능 1 : recyclerView의 우선도 색깔을 바꾸고 싶은 경우에는 adapter.setPriorityColor()로 설정 해 주세요

// 확장성 기능 2 : recyclerView의 우선도 공식을 바꾸고 싶은 경우에는 adapter.calculatePriorityListener = object : 형식으로 OnCalculatePriorityListener 익명 객체를 넣어주세요
/////////////// 우선도를 바꾼 뒤에는 notifyDataSetChanged을 불러주세요!! /////////////////


// 확장성 기능 3 : recyclerView의 클락 시의 행동을 바꾸고 싶은 경우에는 adapter.itemClickListener = object : 형식으로 OnItemClickListener 익명 객체를 넣어주세요
// 확장성 기능 4 : recyclerView의 긴 클릭 시의 행동을 바꾸고 싶은 경우에는 adapter.itemLongClickListener = object : 형식으로 OnLongItemClickListener 익명 객체를 넣어주세요

class MyAdapter(var items:ArrayList<ToDo>)
    : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    //각각 빨간색, 노란색, 초록색을 담고 있습니다
    // 만약 색깔을 바꾸고 싶다면 이 arrayList 의 값을 바꿔주면 그 색깔이 반영이 됩니다
    var hexColors = arrayListOf<String>("e03b22", "eff238", "24f064")

    //////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////adapter.메소드이름() 로 접근 해 주세요///////////////////////////

    // 6자리 hex 색깔을 문자열로 보내주면 바뀝니다. 기본값은 각 각 빨강, 노랑, 초록이지만, 이 함수를 call하면 넘겨진 색깔에 맞게 다 변하게 됩니다
    fun setPriorityColor(veryImportantColor : String, ImportantColor : String, notImportantColor : String)
    {
        hexColors[0] = veryImportantColor
        hexColors[1] = ImportantColor
        hexColors[2] = notImportantColor
        notifyDataSetChanged()
    }

    // 이걸 호출하면 리사이클러 뷰 안의 아이템들이 우선도 오름차순으로 정렬이 됩니다. -> 초록색일 수록 위에, 빨간색 일 수록 아래에 깔리게 됩니다
    fun sortItemwithAscendingPriority()
    {
        val comparator: Comparator<ToDo> = object : Comparator<ToDo> {
            override fun compare(o1: ToDo?, o2: ToDo?): Int {

                return if(o1!!.priority - o2!!.priority < 0) {
                    -1
                }else if(o1!!.priority - o2!!.priority > 0) {
                    1
                }else {
                    0
                }
            }

        }
        items.sortWith(comparator)
        notifyDataSetChanged()
    }

    // 이걸 호출하면 리사이클러 뷰 안의 아이템들이 우선도 내림차순으로 정렬이 됩니다. -> 빨간색일 수록 위에, 초록색 일 수록 아래에 깔리게 됩니다
    fun sortItemwithDescendingPriority()
    {
        val comparator: Comparator<ToDo> = object : Comparator<ToDo> {
            override fun compare(o1: ToDo?, o2: ToDo?): Int {
                return if(o1!!.priority - o2!!.priority > 0) {
                    -1
                }else if(o1!!.priority - o2!!.priority < 0) {
                    1
                }else {
                    0
                }
            }

        }

        items.sortWith(comparator)

        notifyDataSetChanged()
    }

    fun CalcItemsPrority()
    {
        for(item in items)
        {
            item.priority = calculatePriorityListener!!.calculatePriority(item)
        }
    }


    /////////////////////adapter.메소드이름() 로 접근 해 주세요///////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    interface OnItemClickListener{
        fun OnItemClick(position : Int)
    }

    interface OnLongItemClickListener{
        fun OnItemLongClick(position : Int) : Boolean // 꾹 눌렀을 때 반응할 콜백 함수는 Boolean 값을 반환해 줘야 합니다
    }

    interface OnCalculatePriorityListener{ // 우선도 정해주는 함수를 외부로 부터 받습니다
        fun calculatePriority(item : ToDo) : Float
        // importance : 중요도
        // timeLeft : 현재 시간에서 마감일을 뺀 기간. 초 단위로 인수가 넘어갑니다
        // time_taken : 일을 하는 데 걸리는 시간
        
        // 최종 우선도를 float으로 반환합니다
    }

    var itemClickListener: OnItemClickListener? = object : MyAdapter.OnItemClickListener{
        override fun OnItemClick(position: Int) {
            /////////////// data class에 flag가 생기면 그걸 바꿔주는 역할을 한다 ///////////////////////
        }

    }

    var itemLongClickListener : OnLongItemClickListener? = null // 꾹 눌렀을 때 반응할 Listener

    var calculatePriorityListener : OnCalculatePriorityListener? = object : MyAdapter.OnCalculatePriorityListener{
        override fun calculatePriority(
            item : ToDo
        ): Float {

            val currentTime = LocalDateTime.now()
            val remainingTime = if (item.deadLine.toLocalDateTime() > currentTime) {
                val duration = Duration.between(currentTime, item.deadLine.toLocalDateTime())
                duration.toHours().toFloat()
            } else {                -0.00001f

            }
            
            val urgencyFactor = item.time_taken / remainingTime * 100
            val priorityScore = urgencyFactor + item.importance * 5

            return priorityScore
        }
    }

    inner class MyViewHolder(val binding: SimpleViewHolderBinding) : RecyclerView.ViewHolder(binding.root)
    {  
        init{
            binding.touchableLayout.setOnClickListener{
                itemClickListener?.OnItemClick(adapterPosition)
            }

            binding.touchableLayout.setOnLongClickListener{
                itemLongClickListener?.OnItemLongClick(adapterPosition)!!
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view : SimpleViewHolderBinding = SimpleViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.binding.explanation.text = items[position].explanation
        holder.binding.importance.text = "중요도: " + items[position].importance.toString()
        holder.binding.deadLineDate.text = items[position].deadLine.substring(5,10)
        holder.binding.timeTaken.text = "소요시간: " + items[position].time_taken.toString()

        /////////////// data class에 flag가 생기면 그걸 보고 완료 처리로 표기할지 아닌지를 확인한다 ///////////////////////

        // Date.getTime() 은 해당날짜를 기준으로1970년 00:00:00 부터 몇 초가 흘렀는지를 반환해준다.
        val calDate: Long = items[position].deadLine.toDate().getTime() - Date(System.currentTimeMillis()).getTime()
        // 이제 24*60*60*1000(각 시간값에 따른 차이점) 을 나눠주면 일수가 나온다.
        var calDateDays = calDate / (24 * 60 * 60 * 1000)

//        holder.binding.deadLine.text = "마감 " + calDateDays.toString() + "일 전"
        holder.binding.deadLine.text = calDateDays.toString() + "일 전"
        // calDateDay가 음수 -> 마감기한이 지났다 -> 제한 시간 내에 완수 불가능

        // 아래 구문도 차이나는 시간을 초가 아닌 일수로 전달하는 것으로 바꿨습니다.
        //따라서  PriorityFragment의 calculatePriority 오버라이드 구현된 부분도 초 대신 일수를 받는것으로 다시 구현하였습니다.
        val priority : Float = items[position].priority

        Log.d("우선도", " 설명 : " + items[position].explanation + "  우선도 : " + items[position].priority.toString())

        if(priority < 0) {
            holder.binding.deadLine.text = "마감 기한 초과"
        }

        if(priority > 50)
        {
            var priorityGap = priority - 50
            var hex =""

            //만약 우선도가 100이상이면 최대치로, 그 이하면 50과의 간격 차이에 비례 해서 명도가 결정이 된다
            if(priority >= 100)
            {
                hex = "FF"
            }else
            {
                hex = Integer.toHexString(((priorityGap / (100 - 50)) * 156 + 100).toInt())
            }

            if(hex.length == 1)
            {
                hex = "0$hex"
            }

            holder.binding.priorityImageView.setColorFilter(Color.parseColor("#${hex}${hexColors[0]}"))
        }else if(priority > 10)
        {
            val priorityGap = priority - 20
            var hex = Integer.toHexString(((priorityGap / (50 - 10)) * 156 + 100).toInt())

            if(hex.length == 1)
            {
                hex = "0$hex"
            }

            holder.binding.priorityImageView.setColorFilter(Color.parseColor("#${hex}${hexColors[1]}"))
        }else if(priority >= 0)
        {
            val priorityGap = priority
            var hex = Integer.toHexString(((priorityGap / (10 - 0)) * 156 + 100).toInt())

            if(hex.length == 1)
            {
                hex = "0$hex"
            }

            holder.binding.priorityImageView.setColorFilter(Color.parseColor("#${hex}${hexColors[2]}"))

        }else // 우선도가 음수 -> 마감기한 놓침
        {
            holder.binding.priorityImageView.setColorFilter(Color.BLACK)
        }
    }

}