package com.fusion_nex_gen.yasuorecyclerviewadapter

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
                0 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))).apply {
                    this.list = ObList<FoldItem>().apply {
                        add(TextBean(MutableLiveData("我是内部第1个text")).apply { fullSpan = true })
                        add(TextBean(MutableLiveData("我是内部第2个text")))
                        add(TextBean(MutableLiveData("我是内部第3个text")))
                    }
                })
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
        binding.myRV.adapterViewBinding(this, this, list, headerList, footerList, true) {
            ItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
            holderBindLoadMore(R.layout.default_load_more_layout, loadMoreItem, DefaultLoadMoreLayoutBinding::class, {
                DefaultLoadMoreLayoutBinding.bind(it)
            }) { holder, item ->
                loadMoreText.text = item.text.value
//                val textView = holder.getView<TextView>(R.id.loadMoreText)
//                textView.text = text.value
            }
            holderBind(R.layout.header_layout_one_ex, HeaderOneBean::class, HeaderLayoutOneExBinding::class, {
                HeaderLayoutOneExBinding.bind(it)
            }) { holder, item ->
                headerText.text = item.headerOneText.value
                headerText.setOnClickListener {
                    headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                }
//                val textView = holder.getView<TextView>(R.id.headerText)
//                textView.text = this.headerOneText.value
//                textView.setOnClickListener {
//                    headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
//                }
            }
            holderBind(R.layout.header_layout_two_ex, HeaderTwoBean::class, HeaderLayoutTwoExBinding::class, {
                HeaderLayoutTwoExBinding.bind(it)
            }) { holder, item ->
                root.setBackgroundColor(item.headerOneBgColor.value!!)
            }
            holderBind(R.layout.footer_layout_one_ex, FooterOneBean::class, FooterLayoutOneExBinding::class, {
                FooterLayoutOneExBinding.bind(it)
            }) { holder, item ->
                footerText.text = item.footerOneText.value
                footerText.setOnClickListener {
                    footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
                }
//                val textView = holder.getView<TextView>(R.id.footerText)
//                textView.text = this.footerOneText.value
//                textView.setOnClickListener {
//                    footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
//                }
            }
            holderBind(R.layout.footer_layout_two_ex, FooterTwoBean::class, FooterLayoutTwoExBinding::class, {
                FooterLayoutTwoExBinding.bind(it)
            }) { holder, item ->
                root.setBackgroundColor(item.footerTwoBgColor.value!!)
            }
            holderBind(R.layout.item_layout_text_ex, TextBean::class, ItemLayoutTextExBinding::class, {
                ItemLayoutTextExBinding.bind(it)
            }) { holder, item ->
                //如果未使用了LiveData，这样写
                text.text = item.text.value
                text.setOnClickListener {
                    Log.e("asas", "setOnClickListener")
                    removeFoldListItem(item)
                    itemList.remove(item)
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
            holderBind(R.layout.item_layout_image_ex, ImageBean::class, ItemLayoutImageExBinding::class, {
                ItemLayoutImageExBinding.bind(it)
            }) { holder, item ->
                image.setImageDrawable(item.image.value)
//                val imageView = holder.getView<ImageView>(R.id.image)
//                imageView.setImageDrawable(image.value)
            }
        }
        //viewBinding的用法
        //dataBinding的用法
        /* binding.myRV.adapterDataBinding(this, this, list) {
             ItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
             holderBind(R.layout.item_layout_text, TextBean::class, ItemLayoutTextBinding::class) {
                 text.setOnClickListener {
                     itemList.remove(item)
                 }
                 main.setOnLongClickListener {
                     Log.e("asas", "Asasas")
                     true
                 }
             }

             holderBind(R.layout.item_layout_image, ImageBean::class, ItemLayoutImageBinding::class) {

             }
         }
 */
        /*binding.myRV.adapterBinding(this, this, list) {
            ItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
            holderBind(R.layout.item_layout_text_ex, TextBean::class){
                holder ->
                val textView = holder.getView<TextView>(R.id.text)
                textView.text  = text.value
                textView.setOnClickListener {
                    itemList.remove(this)
                }
            }
            holderBind(R.layout.item_layout_image, ImageBean::class){
                    holder ->
                val imageView = holder.getView<ImageView>(R.id.image)
                imageView.setImageDrawable(image.value)
            }
        }*/
    }
}