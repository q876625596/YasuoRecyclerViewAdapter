package com.fusion_nex_gen.yasuorecyclerviewadapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.ActivityMainBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.ItemLayoutImageBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.ItemLayoutTextBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.ImageBean
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.TextBean
import com.fusion_nex_gen.yasuorvadapter.DefaultLoadMoreItem
import com.fusion_nex_gen.yasuorvadapter.ObList
import com.fusion_nex_gen.yasuorvadapter.onHolderDataBinding
import com.fusion_nex_gen.yasuorvadapter.rvDataBindingAdapter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val list = ObList<Any>().apply {
            add(TextBean(MutableLiveData("我是第一个text")))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))))
            add(TextBean(MutableLiveData("我是第三个text")))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.bbb))))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ccc))))
            add(TextBean(MutableLiveData("我是第四个text")))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ddd))))
            add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
            add(TextBean(MutableLiveData("我是第五个text")))
            add(TextBean(MutableLiveData("我是第六个text")))
        }

        fun getType(raw: Class<*>, vararg args: Type) = object : ParameterizedType {
            override fun getRawType(): Type = raw
            override fun getActualTypeArguments(): Array<out Type> = args
            override fun getOwnerType(): Type? = null
        }

        val loadMoreItem = DefaultLoadMoreItem()
        binding.myRV.layoutManager = StaggeredGridLayoutManager( 3,GridLayoutManager.VERTICAL)
        binding.myRV.rvDataBindingAdapter(this, this, list) {
            loadMoreLayoutId = R.layout.default_load_more_layout
            loadMoreLayoutItem = loadMoreItem
            onHolderDataBinding(R.layout.item_layout_text, TextBean::class, ItemLayoutTextBinding::class) {
                root.setOnClickListener {
                    loadMoreItem.bgColor.value = Color.BLACK
                }
            }

            onHolderDataBinding(R.layout.item_layout_image, ImageBean::class, ItemLayoutImageBinding::class) {

            }
        }
    }
}