package com.fusion_nex_gen.yasuorecyclerviewadapter

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.fusion_nex_gen.yasuorecyclerviewadapter.databinding.*
import com.fusion_nex_gen.yasuorecyclerviewadapter.model.*
import com.fusion_nex_gen.yasuorvadapter.*
import com.fusion_nex_gen.yasuorvadapter.bean.DefaultLoadMoreItem
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoFoldItem
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.listener.YasuoItemTouchHelperCallBack
import com.fusion_nex_gen.yasuorvadapter.listener.attach
import com.fusion_nex_gen.yasuorvadapter.listener.onLoadMoreListener
import com.fusion_nex_gen.yasuorvadapter.sticky.StickyGridLayoutManager
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator
import com.mikepenz.itemanimators.ScaleUpAnimator
import com.mikepenz.itemanimators.SlideDownAlphaAnimator
import com.mikepenz.itemanimators.SlideLeftAlphaAnimator

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //itemList
        val list = initList()
        //headerList
        val headerList = YasuoList<Any>().apply {
            add(HeaderOneBean(MutableLiveData("我是header1，点击我新增header")))
            add(HeaderTwoBean(MutableLiveData(Color.RED)))
        }
        //footerList
        val footerList = YasuoList<Any>().apply {
            add(FooterOneBean(MutableLiveData("我是footer1，点击我新增footer")))
            add(FooterTwoBean(MutableLiveData(Color.BLUE)))
        }
        //binding.myRV.layoutManager = GridLayoutManager(this, 3)
        binding.myRV.layoutManager = StickyGridLayoutManager<YasuoNormalRVAdapter>(this, 3)
        binding.myRV.itemAnimator = SlideLeftAlphaAnimator()
        //普通findViewById用法
        binding.myRV.adapterBinding(this, list, headerList, footerList) {
            //设置可以拖拽和侧滑删除
            YasuoItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
            //显示加载更多布局
            showLoadMoreLayout(DefaultLoadMoreItem())
            //设置加载更多的监听
            binding.myRV.onLoadMoreListener(this) {
                //当itemList的真实数量小于50的时候，模拟加载更多数据
                if (getItemListTrueSize() < 50) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.bbb))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ccc))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ddd))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
                        enableLoadMoreListener()
                    }, 1000L)
                }
            }
            //绑定空布局1
            holderBindEmpty(R.layout.empty_layout_one, EmptyBeanOne::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setOnClickListener {
                        showEmptyLayout(EmptyBeanTwo(), true, true)
                    }
                }
            }
            //绑定空布局2
            holderBindEmpty(R.layout.empty_layout_two, EmptyBeanTwo::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setOnClickListener {
                        enableLoadMoreListener()
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))))
                    }
                }
            }
            //绑定加载更多布局
            holderBindLoadMore(R.layout.default_load_more_layout, DefaultLoadMoreItem::class) {
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.loadMoreText).apply {
                        text = item.text.value
                    }
                    holder.itemView.setOnClickListener {
                        showEmptyLayout(EmptyBeanOne())
                    }
                }
            }
            //绑定header1
            holderBindHeader(R.layout.header_layout_one, HeaderOneBean::class) {
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.headerText).apply {
                        text = item.headerOneText.value
                        setOnClickListener {
                            headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                        }
                    }
                }
            }
            //绑定header2
            holderBindHeader(R.layout.header_layout_two, HeaderTwoBean::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setBackgroundColor(item.headerOneBgColor.value!!)
                }
            }
            //绑定footer1
            holderBindFooter(R.layout.footer_layout_one, FooterOneBean::class) {
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.footerText).apply {
                        text = item.footerOneText.value
                        setOnClickListener {
                            footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
                        }
                    }
                }
            }
            //绑定footer2
            holderBindFooter(R.layout.footer_layout_two, FooterTwoBean::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setBackgroundColor(item.footerTwoBgColor.value!!)
                }
            }
            //绑定文本布局
            holderBind(R.layout.item_layout_text, TextBean::class) {
                isFold = true
                sticky = true
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.itemText).apply {
                        //如果未使用了LiveData，这样写
                        text = item.text.value
                        setOnClickListener {
                            Log.e("asas", "setOnClickListener")
                            if (item.parentHash != null) {
                                removeFoldChildListItem(item)
                            }
                            itemList.remove(item)
                        }
                        //如果使用了liveData，那么这样写
                        //如果使用了liveData，必须在设置observe监听之前将所有监听移除
//                        item.text.removeObservers(this@MainActivity)
//                        item.text.observe(this@MainActivity) {
//                            text = it
//                        }
                    }
                }
            }
            //绑定展开第一级文本布局
            holderBind(R.layout.item_layout_text_inner_1, TextBeanInnerOne::class) {
                isFold = true
                gridSpan = 3
                staggeredGridFullSpan = true
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.itemText).apply {
                        text = item.text.value
                        setOnClickListener {
                            expandOrFoldItem(item)
                        }
                    }
                }
            }
            //绑定展开第二级文本布局
            holderBind(R.layout.item_layout_text_inner_2, TextBeanInnerTwo::class) {
                isFold = true
                gridSpan = 3
                staggeredGridFullSpan = true
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.itemText).apply {
                        text = item.text.value
                        setOnClickListener {
                            expandOrFoldItem(item)
                        }
                    }
                }
            }
            //绑定展开第三级文本布局
            holderBind(R.layout.item_layout_text_inner_3, TextBeanInnerThree::class) {
                isFold = true
                gridSpan = 3
                staggeredGridFullSpan = true
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.itemText).apply {
                        text = item.text.value
                        setOnClickListener {
                            expandOrFoldItem(item)
                        }
                    }
                }
            }
            //绑定图片布局
            holderBind(R.layout.item_layout_image, ImageBean::class) {
                //设置可展开
                isFold = true
                onHolderBind { holder, item ->
                    holder.getView<ImageView>(R.id.itemImage).apply {
                        setImageDrawable(item.image.value)
                        setOnClickListener {
                            expandOrFoldItem(item)
                        }
                    }
                }
            }
        }
        //viewBinding的用法
        /*binding.myRV.adapterViewBinding(this, this, list, headerList, footerList) {
            YasuoItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
//            setSticky {
//                return@setSticky it % 4 == 0
//            }
//            setGridSpan {
//                return@setGridSpan if (it % 4 == 0) 3 else 1
//            }
            holderBindLoadMore(R.layout.default_load_more_layout, DefaultLoadMoreItem::class,{
                    DefaultLoadMoreLayoutBinding.bind(it)
                }) {
                onBind { holder, item ->
                    loadMoreText.text = item.text.value
                }
            }
            holderBindHeader(R.layout.header_layout_one_ex, HeaderOneBean::class,{
                    HeaderLayoutOneExBinding.bind(it)
                }) {
                onBind { holder, item ->
                    headerText.text = item.headerOneText.value
                    headerText.setOnClickListener {
                        headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                    }
                }
            }

            holderBindHeader(R.layout.header_layout_two_ex, HeaderTwoBean::class, {
                HeaderLayoutTwoExBinding.bind(it)
            }) {
                onBind { holder, item ->
                    root.setBackgroundColor(item.headerOneBgColor.value!!)
                }
            }
            holderBindFooter(R.layout.footer_layout_one_ex, FooterOneBean::class,  {
                FooterLayoutOneExBinding.bind(it)
            }) {
                onBind { holder, item ->
                    footerText.text = item.footerOneText.value
                    footerText.setOnClickListener {
                        footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
                    }
                }
            }

            holderBindFooter(R.layout.footer_layout_two_ex, FooterTwoBean::class,  {
                FooterLayoutTwoExBinding.bind(it)
            }) {
                onBind { holder, item ->
                    root.setBackgroundColor(item.footerTwoBgColor.value!!)
                }
            }
            holderBind(R.layout.item_layout_text_ex, TextBean::class, {
                ItemLayoutTextExBinding.bind(it)
            }) {
                isFold = true
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
            holderBind(R.layout.item_layout_image_ex, ImageBean::class, {
                ItemLayoutImageExBinding.bind(it)
            }) {
                isFold = true
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
        }*/
        //dataBinding的用法
        /*binding.myRV.adapterDataBinding(this, this, list, headerList, footerList) {
            YasuoItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
            *//*          setSticky {
                          return@setSticky it % 4 == 0
                      }*//*
            *//*   setGridSpan {
                   return@setGridSpan if (it % 4 == 0) 3 else 1
               }*//*
            holderBindLoadMore(R.layout.default_load_more_layout_data_binding, loadMoreItem, DefaultLoadMoreLayoutDataBindingBinding::class) {
                onBind { holder ->

                }
            }
            holderBindHeader(R.layout.header_layout_one_data_binding, HeaderOneBean::class, HeaderLayoutOneDataBindingBinding::class) {
                onBind { holder ->
                    headerText.setOnClickListener {
                        headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                    }
                }
            }

            holderBindHeader(R.layout.header_layout_two_data_binding, HeaderTwoBean::class, HeaderLayoutTwoDataBindingBinding::class) {
                onBind { holder ->

                }
            }
            holderBindFooter(R.layout.footer_layout_one_data_binding, FooterOneBean::class, FooterLayoutOneDataBindingBinding::class) {
                onBind { holder ->
                    footerText.setOnClickListener {
                        footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
                    }
                }
            }

            holderBindFooter(R.layout.footer_layout_two_data_binding, FooterTwoBean::class, FooterLayoutTwoDataBindingBinding::class) {
                onBind { holder ->
                }
            }
            holderBind(R.layout.item_layout_text_data_binding, TextBean::class, ItemLayoutTextDataBindingBinding::class) {
                isFold = true
                onBind { holder ->
                    itemText.setOnClickListener {
                        Log.e("asas", "setOnClickListener")
                        if (item!!.parentHash != null) {
                            removeFoldListItem(item!!)
                        }
                        itemList.remove(item!!)
                    }
                }
            }
            holderBind(R.layout.item_layout_image_data_binding, ImageBean::class, ItemLayoutImageDataBindingBinding::class) {
                isFold = true
                onBind { holder ->
                    if (item!!.canClick.value == true) {
                        itemImage.setOnClickListener {
                            item!!.image.value = ContextCompat.getDrawable(this@MainActivity, R.drawable.ccc)
                            itemImage.setImageDrawable(item!!.image.value)
                        }
                    } else {
                        itemImage.setOnClickListener {
                            expandOrFoldItem(item!!)
                        }
                    }
                }
            }
        }*/

    }

    fun initList(): YasuoList<Any> {
        val list = YasuoList<Any>()
        for (i in 0 until 40) {
            when (i % 7) {
                0 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))).apply {
                    this.list = YasuoList<YasuoFoldItem>().apply {
                        add(TextBeanInnerOne(MutableLiveData("我是一级内部第1个text")))
                        add(TextBeanInnerOne(MutableLiveData("我是一级内部第2个text")))
                        add(TextBeanInnerOne(MutableLiveData("我是一级内部第3个text")).apply {
                            this.list = YasuoList<YasuoFoldItem>().apply {
                                add(TextBeanInnerTwo(MutableLiveData("我是二级内部第1个text")))
                                add(TextBeanInnerTwo(MutableLiveData("我是二级内部第2个text")))
                                add(TextBeanInnerTwo(MutableLiveData("我是二级内部第3个text")).apply {
                                    this.list = YasuoList<YasuoFoldItem>().apply {
                                        add(TextBeanInnerThree(MutableLiveData("我是三级内部第1个text")))
                                        add(TextBeanInnerThree(MutableLiveData("我是三级内部第2个text")))
                                        add(TextBeanInnerThree(MutableLiveData("我是三级内部第3个text")))
                                    }
                                })
                            }
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
        return list
    }
}