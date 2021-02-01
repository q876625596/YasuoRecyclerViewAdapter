package com.fusion_nex_gen.yasuorecyclerviewadapter

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.*
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.*
import com.fusion_nex_gen.yasuorvadapter.*
import com.fusion_nex_gen.yasuorvadapter.bean.DefaultLoadMoreItem
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoFoldItem
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.databinding.DefaultLoadMoreLayoutBinding
import com.fusion_nex_gen.yasuorvadapter.listener.YasuoItemTouchHelperCallBack
import com.fusion_nex_gen.yasuorvadapter.listener.attach
import com.fusion_nex_gen.yasuorvadapter.sticky.StickyGridLayoutManager

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //itemList
        val list = YasuoList<Any>()
        for (i in 0 until 40) {
            when (i % 7) {
                0 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))).apply {
                    this.list = YasuoList<YasuoFoldItem>().apply {
                        add(TextBean(MutableLiveData("我是内部第1个text")).apply { span = 3 })
                        add(TextBean(MutableLiveData("我是内部第2个text")))
                        add(TextBean(MutableLiveData("我是内部第3个text")))
                        add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))).apply {
                            canClick.value = true
                        })
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

        val headerList = YasuoList<Any>().apply {
            add(HeaderOneBean(MutableLiveData("我是header1，点击我新增header")))
            add(HeaderTwoBean(MutableLiveData(Color.RED)))
        }
        val footerList = YasuoList<Any>().apply {
            add(FooterOneBean(MutableLiveData("我是footer1，点击我新增footer")))
            add(FooterTwoBean(MutableLiveData(Color.BLUE)))
        }
        val loadMoreItem = DefaultLoadMoreItem()
        binding.myRV.layoutManager = StickyGridLayoutManager<YasuoNormalRVAdapter>(this, 3)
        //普通findViewById用法
        binding.myRV.adapterViewBinding(this, this, list, headerList, footerList) {
            YasuoItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
  /*          setSticky {
                return@setSticky it % 4 == 0
            }*/
         /*   setGridSpan {
                return@setGridSpan if (it % 4 == 0) 3 else 1
            }*/
            holderBind(R.layout.default_load_more_layout, DefaultLoadMoreItem::class,
                DefaultLoadMoreLayoutBinding::class, {
                    DefaultLoadMoreLayoutBinding.bind(it)
                }) {
                onBind { holder, item ->
                    loadMoreText.text = item.text.value
                }
            }
            holderBind(R.layout.header_layout_one_ex, HeaderOneBean::class,
                HeaderLayoutOneExBinding::class, {
                    HeaderLayoutOneExBinding.bind(it)
                }) {
                onBind { holder, item ->
                    headerText.text = item.headerOneText.value
                    headerText.setOnClickListener {
                        headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                    }
                }
            }

            holderBind(R.layout.header_layout_two_ex, HeaderTwoBean::class, HeaderLayoutTwoExBinding::class, {
                HeaderLayoutTwoExBinding.bind(it)
            }) {
                onBind { holder, item ->
                    root.setBackgroundColor(item.headerOneBgColor.value!!)
                }
            }
            holderBind(R.layout.footer_layout_one_ex, FooterOneBean::class, FooterLayoutOneExBinding::class, {
                FooterLayoutOneExBinding.bind(it)
            }) {
                onBind { holder, item ->
                    footerText.text = item.footerOneText.value
                    footerText.setOnClickListener {
                        footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
                    }
                }
            }

            holderBind(R.layout.footer_layout_two_ex, FooterTwoBean::class, FooterLayoutTwoExBinding::class, {
                FooterLayoutTwoExBinding.bind(it)
            }) {
                onBind { holder, item ->
                    root.setBackgroundColor(item.footerTwoBgColor.value!!)
                }
            }
            holderBind(R.layout.item_layout_text_ex, TextBean::class, ItemLayoutTextExBinding::class, {
                ItemLayoutTextExBinding.bind(it)
            }) {
                onBind { holder, item ->
                    //如果未使用了LiveData，这样写
                    text.text = item.text.value
                    text.setOnClickListener {
                        Log.e("asas", "setOnClickListener")
                        if (item.parentHash != null) {
                            removeFoldListItem(item)
                        }
                        itemList.remove(item)
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
            }
            holderBind(R.layout.item_layout_image_ex, ImageBean::class, ItemLayoutImageExBinding::class, {
                ItemLayoutImageExBinding.bind(it)
            }) { isFold = true
                onBind { holder, item ->
                    image.setImageDrawable(item.image.value)
                    if (item.canClick.value == true) {
                        image.setOnClickListener {
                            item.image.value = ContextCompat.getDrawable(this@MainActivity, R.drawable.ccc)
                            image.setImageDrawable(item.image.value)
                        }
                    } else {
                        image.setOnClickListener {
                            expandOrFoldItem(item)
                        }
                    }
                }
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