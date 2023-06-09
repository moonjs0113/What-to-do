package com.example.whattodo

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.appcompat.app.AppCompatActivity
import com.example.whattodo.databinding.ActivityMainBinding
import com.example.whattodo.manager.Persistence.PersistenceService
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    var isForeGroundServiceValid = false
    lateinit var foregroundService: Intent

    val textarr = arrayListOf<String>("우선도", "마감일", "검색", "환경설정")
    var deadline = LocalDateTime.now()
    var timeToSpendVal = 1
    var importanceVal = 5

    var isInputFormOpen = false;


    // 입력폼 움직임을 위한 애니매이터
    lateinit var animator: ObjectAnimator

    // 수정인지 등록인지 표시하는 플래그
    var isAmend = false
    // 수정할 객체 id
    var idToAmend = 0

    //    val imgarr = arrayListOf<Int>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        PersistenceService.share.registerContext(this)
        layoutInit()
    }

    fun startForegroundService() {
        foregroundService = Intent(this, ForegroundService::class.java)

        isForeGroundServiceValid = true

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(foregroundService)
        } else {
            startService(foregroundService)
        }
    }

    fun stopForegroundService() {
        if(isForeGroundServiceValid)
        {
            stopService(foregroundService)
        }
        isForeGroundServiceValid = false
    }

    @SuppressLint("ObjectAnimatorBinding", "MissingInflatedId", "ResourceType")
    fun layoutInit() {
        binding.viewPager.adapter = MainViewPagerAdapter(this)
        binding.viewPager.offscreenPageLimit = 3
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = textarr[pos]
//            tab.setIcon(imgarr[pos])
        }.attach()

        binding.apply {
            datePickedText.setText("${deadline.year}년 ${deadline.monthValue}월 ${deadline.dayOfMonth}일 ${deadline.hour}시 ${deadline.minute}분")
            var bottomBarHeight = 0
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                bottomBarHeight = resources.getDimensionPixelSize(resourceId)
            }
            println(bottomBarHeight)
            animator = ObjectAnimator.ofInt(todoInputForm, "layoutParams", 52+ bottomBarHeight, 700)
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener { animation ->
                val layoutParams = todoInputForm.layoutParams
                layoutParams.height = animation.animatedValue as Int
                todoInputForm.layoutParams = layoutParams
            }

            todoInputForm.setOnClickListener {
                if(isInputFormOpen) {
                    animator.reverse()
                    isInputFormOpen = false
                } else {
                    animator.start()
                    isInputFormOpen = true
                }
            }
            todoInput.setOnClickListener {
                if(isInputFormOpen) {
                    animator.reverse()
                    isInputFormOpen = false
                } else {
                    animator.start()
                    isInputFormOpen = true
                }
            }

            todoInput.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    animator.start()
                    isInputFormOpen = true
                }
            }

            datePickedText.setOnClickListener {
                setDeadline()
            }

            datePickedButton.setOnClickListener {
                setDeadline()
            }

            timeToSpendButton.setOnClickListener {
                setTimeToSpend()
            }
            timeToSpend.setOnClickListener {
                setTimeToSpend()
            }
            seekBar.progress = importanceVal
            seekBar.max = 10
            seekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        importanceVal = progress
                        importance.setText("$importanceVal/10")
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    }
                }
            )

            registerBtn.setOnClickListener {
                if(todoInput.text.isEmpty() && !isAmend) {
                    var snackbar = Snackbar.make(binding.root, "일정 이름을 입력해주세요.", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    return@setOnClickListener
                }
                if(deadline.isBefore(LocalDateTime.now()) && !isAmend) {
                    var snackbar = Snackbar.make(binding.root, "마감일이 현재 시간 이전입니다.", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    return@setOnClickListener
                }
                var newToDo = ToDo(todoInput.text.toString(), deadline.toString(), timeToSpendVal.toFloat(), importanceVal, 5f)
                newToDo.id = idToAmend

                // Room에 ToDo객체 저장
                CoroutineScope(Dispatchers.IO).launch {
                    PersistenceService.share.registerContext(this@MainActivity)
                    // 수정 모드인 경우
                    if(isAmend) {
                        PersistenceService.share.updateTodo(newToDo)
                        // 수정 끝
                        isAmend = false
                        registerBtn.text = "등록"
                    } else {
                        newToDo.id = 0
                        PersistenceService.share.insertTodo(newToDo)
                    }
                }


                val intent = Intent("Todo added");
                intent.putExtra("message","dataChanged");
                sendBroadcast(intent);

                // 저장 성공 후 초기화
                todoInput.text.clear()
                deadline = LocalDateTime.now()
                datePickedText.setText("${deadline.year}년 ${deadline.monthValue}월 ${deadline.dayOfMonth}일 ${deadline.hour}시 ${deadline.minute}분")
                timeToSpendVal = 1
                timeToSpend.setText("${timeToSpendVal}시간")
                importanceVal = 5
                importance.setText("$importanceVal/10")
                seekBar.progress = 5

                //등록시 토스트 메시지 출력
                Toast.makeText(this@MainActivity, "일정 등록 완료", Toast.LENGTH_SHORT).show()
                if(isInputFormOpen) {
                    animator.reverse()
                    isInputFormOpen = false
                }
            }
        }
    }

    fun sendBroadCastInMainActivity(intent : Intent)
    {
        sendBroadcast(intent)
    }

    fun setTimeToSpend() {
        val layout = layoutInflater.inflate(R.layout.dialog_num_select, null)
        val build = AlertDialog.Builder(this).apply {
            setView(layout)
        }
        val dialog = build.create()
        dialog.show()

        var numberPicker: NumberPicker = layout.findViewById(R.id.number_picker)
        numberPicker.minValue = 1
        numberPicker.maxValue = 10000

        var btnCancel: Button = layout.findViewById(R.id.btn_cancel)
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        var btnOK: Button = layout.findViewById(R.id.btn_ok)
        btnOK.setOnClickListener {
            timeToSpendVal = numberPicker.value
            binding.timeToSpend.setText("${timeToSpendVal}시간")
            dialog.dismiss()
        }
    }

    fun setDeadline() {
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            deadline =  deadline.withYear(year)
            deadline =  deadline.withMonth(month + 1)
            deadline =  deadline.withDayOfMonth(dayOfMonth)
            binding.datePickedText.setText("${deadline.year}년 ${deadline.monthValue}월 ${deadline.dayOfMonth}일 ${deadline.hour}시 ${deadline.minute}분")
            val timeSetListener = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                deadline =  deadline.withHour(hourOfDay)
                deadline =  deadline.withMinute(minute)
                binding.datePickedText.setText("${deadline.year}년 ${deadline.monthValue}월 ${deadline.dayOfMonth}일 ${deadline.hour}시 ${deadline.minute}분")
            }
            TimePickerDialog(this@MainActivity, R.style.MyDatePickerStyle, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),true).show()
        }
        DatePickerDialog(this@MainActivity, R.style.MyTimePickerStyle, dateSetListener, cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
    }
}