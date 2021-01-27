package com.fusion_nex_gen.yasuorecyclerviewadapter

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.*
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.*
import com.fusion_nex_gen.yasuorvadapter.*
import com.fusion_nex_gen.yasuorvadapter.databinding.DefaultLoadMoreLayoutBinding

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val list = ObList<Any>()
        for (i in 0 until 40) {
            when (i % 7) {
                0 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))))
                1 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.bbb))))
                2 -> list.add(TextBean(MutableLiveData("我是第${i + 1}个text")))
                3 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ddd))))
                4 -> list.add(TextBean(MutableLiveData("我是第${i + 1}个text")))
                5 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
                6 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
            }
        }

        val headerList = ObList<Any>().apply {
            add(HeaderOneBean(MutableLiveData("我是header1，点击我新增header")))
            add(HeaderTwoBean(MutableLiveData(Color.RED)))
        }
        val footerList = ObList<Any>().apply {
            add(FooterOneBean(MutableLiveData("我是footer1，点击我新增footer")))
            add(FooterTwoBean(MutableLiveData(Color.BLUE)))
        }
        val loadMoreItem = DefaultLoadMoreItem()
        binding.myRV.layoutManager = GridLayoutManager(this, 3)
        //普通findViewById用法
        binding.myRV.adapterViewBinding(this, this, list, headerList, footerList) {
            ItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
            holderBindLoadMore(R.layout.default_load_more_layout_data_binding, loadMoreItem) { holder ->
                this as DefaultLoadMoreItem
                val bin = holder.createBinding {
                    DefaultLoadMoreLayoutBinding.bind(it)
                }

                bin.loadMoreText.text = text.value
//                val textView = holder.getView<TextView>(R.id.loadMoreText)
//                textView.text = text.value
            }
            holderBind(R.layout.header_layout_one, HeaderOneBean::class) { holder ->
                this as HeaderOneBean
                val bin = holder.createBinding {
                    HeaderLayoutOneExBinding.bind(it)
                }
                bin.headerText.text = headerOneText.value
                bin.headerText.setOnClickListener {
                    headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                }
//                val textView = holder.getView<TextView>(R.id.headerText)
//                textView.text = this.headerOneText.value
//                textView.setOnClickListener {
//                    headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
//                }
            }
            holderBind(R.layout.header_layout_two, HeaderTwoBean::class) { holder ->
                this as HeaderTwoBean
                holder.itemView.setBackgroundColor(headerOneBgColor.value!!)
            }
            holderBind(R.layout.footer_layout_one, FooterOneBean::class) { holder ->
                this as FooterOneBean
                val bin = holder.createBinding {
                    FooterLayoutOneExBinding.bind(it)
                }
                bin.footerText.text = footerOneText.value
                bin.footerText.setOnClickListener {
                    footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
                }
//                val textView = holder.getView<TextView>(R.id.footerText)
//                textView.text = this.footerOneText.value
//                textView.setOnClickListener {
//                    footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
//                }
            }
            holderBind(R.layout.footer_layout_two, FooterTwoBean::class) { holder ->
                this as FooterTwoBean
                holder.itemView.setBackgroundColor(footerTwoBgColor.value!!)
            }
            holderBind(R.layout.item_layout_text, TextBean::class) { holder ->
                //这里我将holder.bindingAdapterPosition对应的item返回给了this。只需要做一下转换即可
                this as TextBean
                val bin = holder.createBinding {
                    ItemLayoutTextExBinding.bind(it)
                }
                //如果未使用了LiveData，这样写
                bin.text.text = text.value
                bin.text.setOnClickListener {
                    itemList.remove(this)
                }
//                val textView = holder.getView<TextView>(R.id.text)
//                textView.text = text.value
//                textView.setOnClickListener {
//                    itemList.remove(this)
//                }

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
                val bin = holder.createBinding {
                    ItemLayoutImageExBinding.bind(it)
                }
                bin.image.setImageDrawable(image.value)
//                val imageView = holder.getView<ImageView>(R.id.image)
//                imageView.setImageDrawable(image.value)
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