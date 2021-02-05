package com.fusion_nex_gen.yasuorecyclerviewadapter

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.fusion_nex_gen.yasuorvadapter.databinding.DefaultLoadMoreLayoutBinding
import com.fusion_nex_gen.yasuorvadapter.decoration.addYasuoDecoration
import com.fusion_nex_gen.yasuorvadapter.listener.enableDragOrSwipe
import com.fusion_nex_gen.yasuorvadapter.listener.onLoadMoreListener
import com.fusion_nex_gen.yasuorvadapter.sticky.StickyGridLayoutManager
import com.mikepenz.itemanimators.SlideLeftAlphaAnimator

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    fun findViewByIdMode() {
        //数据源
/*        val list = YasuoList<Any>().apply {
            for (i in 0 until 20) {
                if (i % 4 == 1) {
                    add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
                } else {
                    add(TextBean(MutableLiveData("I am text item!")))
                }
            }
        }*/
        val list = YasuoList<Any>().apply {
            for (i in 0 until 20) {
                add(TextBean(MutableLiveData("I am text item ${i}！")))
            }
        }

        //配置itemDecoration
        binding.myRV.addYasuoDecoration {
//            setDecoration(R.layout.item_layout_text,DrawableBean(this@MainActivity, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp))
//            setDecoration(R.layout.item_layout_image,DrawableBean(this@MainActivity, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp))
            setDecoration(R.layout.item_layout_text, this@MainActivity, defaultRes)
            setDecoration(R.layout.item_layout_image, this@MainActivity, defaultRes)
        }
        //binding.myRV.addItemDecoration(GridSpacingItemDecoration(3,20,false))
        binding.myRV.layoutManager = GridLayoutManager(this, 3)
        //binding.myRV.layoutManager = LinearLayoutManager(this)
        binding.myRV.adapterBinding(this, list) {
            //配置holder
            holderConfig(R.layout.item_layout_text, TextBean::class) {
                //holder绑定的触发事件
                onHolderBind { holder, item ->
                    staggeredGridFullSpan = true
                    holder.getView<TextView>(R.id.itemText).apply {
                        text = item.text.value
                    }
                }
            }
            holderConfig(R.layout.item_layout_image, ImageBean::class) {
                gridSpan = 2
                onHolderBind { holder, item ->
                    holder.getView<ImageView>(R.id.itemImage).apply {
                        setImageDrawable(item.image.value)
                    }
                }
            }
        }
    }

    fun viewBindingMode() {
        //数据源
/*        val list = YasuoList<Any>().apply {
            for (i in 0 until 20) {
                if (i % 4 == 1) {
                    add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
                } else {
                    add(TextBean(MutableLiveData("I am text item!")))
                }
            }
        }*/
        val list = YasuoList<Any>().apply {
            for (i in 0 until 20) {
                add(TextBean(MutableLiveData("I am text item ${i}！")))
            }
        }

        //配置itemDecoration
        binding.myRV.addYasuoDecoration {
//            setDecoration(R.layout.item_layout_text,DrawableBean(this@MainActivity, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp))
//            setDecoration(R.layout.item_layout_image,DrawableBean(this@MainActivity, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp))
            setDecoration(R.layout.item_layout_text, this@MainActivity, defaultRes)
            setDecoration(R.layout.item_layout_image, this@MainActivity, defaultRes)
        }
        //binding.myRV.addItemDecoration(GridSpacingItemDecoration(3,20,false))
        binding.myRV.layoutManager = GridLayoutManager(this, 3)
        //binding.myRV.layoutManager = LinearLayoutManager(this)
        binding.myRV.adapterViewBinding(this, list) {
            //配置holder
            holderConfig(R.layout.item_layout_text, TextBean::class, { ItemLayoutTextBinding.bind(it) }) {
                //holder绑定的触发事件
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                }
            }
            holderConfig(R.layout.item_layout_image, ImageBean::class, { ItemLayoutImageBinding.bind(it) }) {
                gridSpan = 2
                onHolderBind { holder, item ->
                    itemImage.setImageDrawable(item.image.value)
                }
            }
        }
    }

    fun dataBindingMode() {
        //数据源
/*        val list = YasuoList<Any>().apply {
            for (i in 0 until 20) {
                if (i % 4 == 1) {
                    add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
                } else {
                    add(TextBean(MutableLiveData("I am text item!")))
                }
            }
        }*/
        val list = YasuoList<Any>().apply {
            for (i in 0 until 20) {
                add(TextBean(MutableLiveData("I am text item ${i}！")))
            }
        }

        //配置itemDecoration
        binding.myRV.addYasuoDecoration {
//            setDecoration(R.layout.item_layout_text,DrawableBean(this@MainActivity, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp))
//            setDecoration(R.layout.item_layout_image,DrawableBean(this@MainActivity, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp))
            setDecoration(R.layout.item_layout_text_data_binding, this@MainActivity, defaultRes)
            setDecoration(R.layout.item_layout_image_data_binding, this@MainActivity, defaultRes)
        }
        //binding.myRV.addItemDecoration(GridSpacingItemDecoration(3,20,false))
        binding.myRV.layoutManager = GridLayoutManager(this, 3)
        //binding.myRV.layoutManager = LinearLayoutManager(this)
        binding.myRV.adapterDataBinding(this, list) {
            //配置holder
            holderConfig(R.layout.item_layout_text_data_binding, TextBean::class, ItemLayoutTextDataBindingBinding::class) {
                //holder绑定的触发事件
                onHolderBind { holder ->
                    //dataBinding直接在xml中绑定数据，无需手动配置
                    //itemText.text = item.text.value
                }
            }
            holderConfig(R.layout.item_layout_image_data_binding, ImageBean::class, ItemLayoutImageDataBindingBinding::class) {
                gridSpan = 2
                onHolderBind { holder ->
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //itemList
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
                4 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.mipmap.ic_launcher))))
                5 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))).apply {
                    //给某个item单独设置
                    //瀑布流占满一行
                    staggeredGridFullSpan = true
                    //网格布局占比
                    gridSpan = 3
                    //吸顶，注意，吸顶会默认占满一行
                    sticky = true
                })
                6 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ccc))))
            }
        }
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
        //设置列表为吸顶模式
        binding.myRV.layoutManager = StickyGridLayoutManager<YasuoNormalRVAdapter>(this, 3)
        //更换动画
        binding.myRV.itemAnimator = SlideLeftAlphaAnimator()
        //普通findViewById用法
        /*binding.myRV.adapterBinding(this, list, headerList, footerList) {
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
                } else {
                    showLoadMoreLayout(DefaultLoadMoreItem().apply { text.value = "Complete!" })
                }
            }
            //绑定空布局1
            holderConfig(R.layout.empty_layout_one, EmptyBeanOne::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setOnClickListener {
                        showEmptyLayout(EmptyBeanTwo(), true, true)
                    }
                }
            }
            //绑定空布局2
            holderConfig(R.layout.empty_layout_two, EmptyBeanTwo::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setOnClickListener {
                        enableLoadMoreListener()
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))))
                    }
                }
            }
            //绑定加载更多布局
            holderConfig(R.layout.default_load_more_layout, DefaultLoadMoreItem::class) {
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
            holderConfig(R.layout.header_layout_one, HeaderOneBean::class) {
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.headerText).apply {
                        item.headerOneText.removeObservers(this@MainActivity)
                        item.headerOneText.observe(this@MainActivity) {
                            text = it ?: ""
                        }
                        setOnClickListener {
                            headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                        }
                    }
                }
            }
            //绑定header2
            holderConfig(R.layout.header_layout_two, HeaderTwoBean::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setBackgroundColor(item.headerOneBgColor.value!!)
                }
            }
            //绑定footer1
            holderConfig(R.layout.footer_layout_one, FooterOneBean::class) {
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
            holderConfig(R.layout.footer_layout_two, FooterTwoBean::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setBackgroundColor(item.footerTwoBgColor.value!!)
                }
            }
            //绑定文本布局
            holderConfig(R.layout.item_layout_text, TextBean::class) {
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
            holderConfig(R.layout.item_layout_text_inner_1, TextBeanInnerOne::class) {
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
            holderConfig(R.layout.item_layout_text_inner_2, TextBeanInnerTwo::class) {
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
            holderConfig(R.layout.item_layout_text_inner_3, TextBeanInnerThree::class) {
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
            holderConfig(R.layout.item_layout_image, ImageBean::class) {
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
        }*/
        //viewBinding的用法
        binding.myRV.adapterViewBinding(this, list, headerList, footerList) {
            //设置拖拽及侧滑删除
            enableDragOrSwipe(binding.myRV, isLongPressDragEnable = true, isItemViewSwipeEnable = true)
            //展示加载更多
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
                        //解开监听锁
                        enableLoadMoreListener()
                    }, 1000L)
                } else {//
                    showLoadMoreLayout(DefaultLoadMoreItem().apply { text.value = "Complete!" })
                }
            }
            //配置加载更多的holder
            holderConfig(R.layout.default_load_more_layout, DefaultLoadMoreItem::class, {
                DefaultLoadMoreLayoutBinding.bind(it)
            }) {
                onHolderBind { holder, item ->
                    loadMoreText.text = item.text.value
                }
            }
            //配置header1
            holderConfig(R.layout.header_layout_one, HeaderOneBean::class, {
                HeaderLayoutOneBinding.bind(it)
            }) {
                onHolderBind { holder, item ->
                    headerText.text = item.headerOneText.value
                    headerText.setOnClickListener {
                        //点击此item时新增一个header
                        headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                    }
                }
            }
            //配置header2
            holderConfig(R.layout.header_layout_two, HeaderTwoBean::class, {
                HeaderLayoutTwoBinding.bind(it)
            }) {
                onHolderBind { holder, item ->
                    root.setBackgroundColor(item.headerOneBgColor.value!!)
                }
            }
            //配置footer1
            holderConfig(R.layout.footer_layout_one, FooterOneBean::class, {
                FooterLayoutOneBinding.bind(it)
            }) {
                onHolderBind { holder, item ->
                    footerText.text = item.footerOneText.value
                    footerText.setOnClickListener {
                        //点击此item时新增一个footer
                        footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
                    }
                }
            }
            //配置footer2
            holderConfig(R.layout.footer_layout_two, FooterTwoBean::class, {
                FooterLayoutTwoBinding.bind(it)
            }) {
                onHolderBind { holder, item ->
                    root.setBackgroundColor(item.footerTwoBgColor.value!!)
                }
            }
            //配置文本布局
            holderConfig(R.layout.item_layout_text, TextBean::class, {
                ItemLayoutTextBinding.bind(it)
            }) {
                //给某个itemViewType的布局统一设置
                //瀑布流占满一行
                staggeredGridFullSpan = true
                //网格布局占比
                gridSpan = 3
                //吸顶，注意，吸顶会默认占满一行
                sticky = true
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                    //点击移除该item
                    itemText.setOnClickListener {
                        itemList.remove(item)
                    }
                }
            }
            //绑定展开第一级文本布局
            holderConfig(R.layout.item_layout_text_inner_1, TextBeanInnerOne::class, { ItemLayoutTextInner1Binding.bind(it) }) {
                isFold = true
                gridSpan = 3
                staggeredGridFullSpan = true
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                    itemText.setOnClickListener {
                        expandOrFoldItem(item)
                    }
                }
            }
            //绑定展开第二级文本布局
            holderConfig(R.layout.item_layout_text_inner_2, TextBeanInnerTwo::class, { ItemLayoutTextInner2Binding.bind(it) }) {
                isFold = true
                gridSpan = 3
                staggeredGridFullSpan = true
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                    itemText.setOnClickListener {
                        expandOrFoldItem(item)
                    }
                }
            }
            //绑定展开第三级文本布局
            holderConfig(R.layout.item_layout_text_inner_3, TextBeanInnerThree::class, { ItemLayoutTextInner3Binding.bind(it) }) {
                isFold = true
                gridSpan = 3
                staggeredGridFullSpan = true
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                    itemText.setOnClickListener {
                        expandOrFoldItem(item)
                    }
                }
            }
            //配置图片布局
            holderConfig(R.layout.item_layout_image, ImageBean::class, {
                ItemLayoutImageBinding.bind(it)
            }) {
                //配置该布局可折叠展开（需要实体类继承YasuoFoldItem）
                isFold = true
                onHolderBind { holder, item ->
                    itemImage.setImageDrawable(item.image.value)
                    itemImage.setOnClickListener {
                        //点击该图片时展开折叠的子列表
                        expandOrFoldItem(item)
                    }
                }
            }
        }
        //dataBinding的用法
        /*binding.myRV.adapterDataBinding(this, list, headerList, footerList) {
            YasuoItemTouchHelperCallBack(this, isItemViewSwipeEnable = true).attach(binding.myRV)
            holderConfig(R.layout.default_load_more_layout_data_binding, DefaultLoadMoreItem::class, DefaultLoadMoreLayoutDataBindingBinding::class) {
                onHolderBind { holder ->

                }
            }
            holderConfig(R.layout.header_layout_one_data_binding, HeaderOneBean::class, HeaderLayoutOneDataBindingBinding::class) {
                onHolderBind { holder ->
                    headerText.setOnClickListener {
                        headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                    }
                }
            }

            holderConfig(R.layout.header_layout_two_data_binding, HeaderTwoBean::class, HeaderLayoutTwoDataBindingBinding::class) {
                onHolderBind { holder ->

                }
            }
            holderConfig(R.layout.footer_layout_one_data_binding, FooterOneBean::class, FooterLayoutOneDataBindingBinding::class) {
                onHolderBind { holder ->
                    footerText.setOnClickListener {
                        footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
                    }
                }
            }

            holderConfig(R.layout.footer_layout_two_data_binding, FooterTwoBean::class, FooterLayoutTwoDataBindingBinding::class) {
                onHolderBind { holder ->
                }
            }
            holderConfig(R.layout.item_layout_text_data_binding, TextBean::class, ItemLayoutTextDataBindingBinding::class) {
                isFold = true
                onHolderBind { holder ->
                    itemText.setOnClickListener {
                        Log.e("asas", "setOnClickListener")
                        if (item!!.parentHash != null) {
                            removeFoldChildListItem(item!!)
                        }
                        itemList.remove(item!!)
                    }
                }
            }
            holderConfig(R.layout.item_layout_image_data_binding, ImageBean::class, ItemLayoutImageDataBindingBinding::class) {
                isFold = true
                onHolderBind { holder ->
                    itemImage.setOnClickListener {
                        expandOrFoldItem(item!!)
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