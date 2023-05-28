package com.example.whattodo

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whattodo.databinding.SimpleViewHolderBinding
import java.util.*
import android.graphics.drawable.GradientDrawable as GradientDrawable1


// 우선 제가 간단히 만들어 놓은 예시용 디자인을 binding 했습니다.
// binding을 바꾸고 싶으시다면 1,2,3,4,5 binding 부분을 바꿔주세요

class MyAdapter(val items:ArrayList<ToDo>)
    : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {



    interface OnItemClickListener{
        fun OnItemClick(position : Int)
        fun OnItemLongClick(position : Int) : Boolean // 꾹 눌렀을 때 반응할 콜백 함수는 Boolean 값을 반환해 줘야 합니다
    }

    interface OnCalculatePriorityListener{ // 우선도 정해주는 함수를 외부로 부터 받습니다
        fun calculatePriority( _importance : Int, _timeLeft : Long, _time_taken : Float) : Float
        // importance : 중요도
        // timeLeft : 현재 시간에서 마감일을 뺀 기간. 초 단위로 인수가 넘어갑니다
        // time_taken : 일을 하는 데 걸리는 시간
        
        // 최종 우선도를 float으로 반환합니다
    }

    var itemClickListener: OnItemClickListener? = null // 그냥 눌렀을 때 반응할 Listener
    var itemLongClickListener : OnItemClickListener? = null // 꾹 눌렀을 때 반응할 Listener

    var calculatePriorityListener : OnCalculatePriorityListener? = null // 우선도 정해주는 함수를 외부로 부터 받습니다

    inner class MyViewHolder(val binding: SimpleViewHolderBinding) : RecyclerView.ViewHolder(binding.root) //////////////////// 1 /////////////////////////
    {  
        init{
            binding.touchableLayout.setOnClickListener{//////////////////// 2 /////////////////////////
                itemClickListener?.OnItemClick(adapterPosition)
            }

            binding.touchableLayout.setOnLongClickListener{ //////////////////// 3 /////////////////////////
                itemLongClickListener?.OnItemLongClick(adapterPosition)!!
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view : SimpleViewHolderBinding = SimpleViewHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false) //////////////////// 4 /////////////////////////
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) { //////////////////// 5 /////////////////////////
        holder.binding.explanation.text = items[position].explanation
        holder.binding.importance.text = "중요도: " + items[position].importance.toString()

        // Date.getTime() 은 해당날짜를 기준으로1970년 00:00:00 부터 몇 초가 흘렀는지를 반환해준다.
        val calDate: Long = items[position].deadLine.getTime() - Date(System.currentTimeMillis()).getTime()
        // 이제 24*60*60*1000(각 시간값에 따른 차이점) 을 나눠주면 일수가 나온다.
        var calDateDays = calDate / (24 * 60 * 60 * 1000)


        holder.binding.deadLine.text = "마감 " + calDateDays.toString() + "일 전"
        // calDateDay가 음수 -> 마감기한이 지났다 -> 제한 시간 내에 완수 불가능
        if(calDateDays < 0) {
            holder.binding.deadLine.text = "마감 기한 초과"
        }


        val priority : Float =  calculatePriorityListener?.calculatePriority(items[position].importance , calDate, items[position].time_taken)!!
        if(priority > 50)
        {
            var priorityGap = 100 - priority
            var hex =""

            //만약 우선도가 100이상이면 최대치로, 그 이하면 50과의 간격 차이에 비례 해서 명도가 결정이 된다
            if(priorityGap < 0)
            {
                hex = "FF"
            }else
            {
                hex = Integer.toHexString(((priorityGap / (100 - 50)) * 200 + 56).toInt())
            }

            if(hex.length == 1)
            {
                hex = "0$hex"
            }

            holder.binding.priorityImageView.setColorFilter(Color.parseColor("#${hex}e03b22"))
        }else if(priority > 20)
        {
            val priorityGap = priority - 20
            var hex = Integer.toHexString(((priorityGap / (50 - 20)) * 200 + 56).toInt())

            if(hex.length == 1)
            {
                hex = "0$hex"
            }

            holder.binding.priorityImageView.setColorFilter(Color.parseColor("#${hex}eff238"))
        }else if(priority > 0)
        {
            val priorityGap = priority
            var hex = Integer.toHexString(((priorityGap / (20 - 0)) * 200 + 56).toInt())

            if(hex.length == 1)
            {
                hex = "0$hex"
            }

            holder.binding.priorityImageView.setColorFilter(Color.parseColor("#${hex}24f064"))

        }else // 우선도가 음수 -> 마감기한 놓침
        {
            holder.binding.priorityImageView.setColorFilter(Color.TRANSPARENT)
        }


        //////////////////// 5 /////////////////////////

    }

}