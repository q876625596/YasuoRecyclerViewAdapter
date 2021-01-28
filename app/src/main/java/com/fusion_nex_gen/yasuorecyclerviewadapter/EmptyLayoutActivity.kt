package com.fusion_nex_gen.yasuorecyclerviewadapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.ActivityEmptyLayoutBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.ImageBean
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.TextBean
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.adapterBinding
import com.fusion_nex_gen.yasuorvadapter.holderBind

class EmptyLayoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEmptyLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val list = YasuoList<Any>().apply {
            add(TextBean(MutableLiveData("我是第一个text")))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@EmptyLayoutActivity, R.drawable.aaa))))
            add(TextBean(MutableLiveData("我是第三个text")))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@EmptyLayoutActivity, R.drawable.bbb))))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@EmptyLayoutActivity, R.drawable.ccc))))
            add(TextBean(MutableLiveData("我是第四个text")))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@EmptyLayoutActivity, R.drawable.ddd))))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@EmptyLayoutActivity, R.drawable.eee))))
            add(TextBean(MutableLiveData("我是第五个text")))
            add(TextBean(MutableLiveData("我是第六个text")))
        }
        binding.myRV.layoutManager = LinearLayoutManager(this)
        //普通findViewById用法
        binding.myRV.adapterBinding(this,this,list){
            holderBind(R.layout.item_layout_text, TextBean::class) {

            }

            holderBind(R.layout.item_layout_image, ImageBean::class) {

            }
        }
    }
}