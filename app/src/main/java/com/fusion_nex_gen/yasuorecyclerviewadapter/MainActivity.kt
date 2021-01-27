package com.fusion_nex_gen.yasuorecyclerviewadapter

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.ActivityMainBinding
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.HeaderOneBean
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.HeaderTwoBean
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.ImageBean
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.TextBean
import com.fusion_nex_gen.yasuorvadapter.*

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

        val headerList = ObList<Any>().apply {
            add(HeaderOneBean(MutableLiveData("我是header1")))
            add(HeaderTwoBean(MutableLiveData(Color.RED)))
        }
        val loadMoreItem = DefaultLoadMoreItem()
        binding.myRV.layoutManager = GridLayoutManager(this,3)
        //普通findViewById用法
        binding.myRV.adapterBinding(this, this, list, headerList) {
            loadMoreLayoutId = R.layout.default_load_more_layout
            loadMoreLayoutItem = loadMoreItem
            ItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
            holderBind(R.layout.header_layout_one, HeaderOneBean::class) { holder ->
                this as HeaderOneBean
                val textView = holder.getView<TextView>(R.id.headerText)
                textView.text = this.headerOneText.value
            }
            holderBind(R.layout.header_layout_two, HeaderTwoBean::class) { holder ->
                this as HeaderTwoBean
                holder.itemView.setBackgroundColor(headerOneBgColor.value!!)
            }
            holderBind(R.layout.item_layout_text, TextBean::class) { holder ->
                //这里我将holder.bindingAdapterPosition对应的item返回给了this。只需要做一下转换即可
                this as TextBean

                //如果未使用了LiveData，这样写
                val textView = holder.getView<TextView>(R.id.text)
                textView.text = text.value
                textView.setOnClickListener {
                    itemList.remove(this)
                }

//                //如果使用了liveData，那么这样写
//                textView.setOnClickListener {
//                    text.value = "134565"
//                }
//                //如果使用了liveData，必须在设置observe监听之前将所有监听移除
//                text.removeObservers(this@MainActivity)
//                text.observe(this@MainActivity) {
//                    textView.text = it
//                }
            }

            holderBind(R.layout.item_layout_image, ImageBean::class) { holder ->
                this as ImageBean
                val imageView = holder.getView<ImageView>(R.id.image)
                imageView.setImageDrawable(image.value)
            }
        }
        //viewBinding的用法
        //dataBinding的用法
        /*     binding.myRV.adapterDataBinding(this, this, list) {
                 loadMoreLayoutId = R.layout.default_load_more_layout
                 loadMoreLayoutItem = loadMoreItem
                 ItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
                 holderBind(R.layout.item_layout_text, TextBean::class, ItemLayoutTextBinding::class) {
                     root.setOnClickListener {
                         //loadMoreItem.bgColor.value = Color.BLACK
                         item!!.text.value = "123456"
                     }
                 }

                 holderBind(R.layout.item_layout_image, ImageBean::class, ItemLayoutImageBinding::class) {

                 }
             }*/
    }
}