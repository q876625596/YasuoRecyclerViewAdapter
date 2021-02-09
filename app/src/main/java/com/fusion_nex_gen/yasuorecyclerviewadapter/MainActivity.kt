package com.fusion_nex_gen.yasuorecyclerviewadapter

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.fusion_nex_gen.yasuorvadapter.databinding.DefaultLoadMoreLayoutDataBindingBinding
import com.fusion_nex_gen.yasuorvadapter.decoration.addYasuoDecoration
import com.fusion_nex_gen.yasuorvadapter.listener.enableDragOrSwipe
import com.fusion_nex_gen.yasuorvadapter.listener.onLoadMoreListener
import com.fusion_nex_gen.yasuorvadapter.sticky.StickyGridLayoutManager
import com.mikepenz.itemanimators.SlideLeftAlphaAnimator

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
        //设置列表为吸顶模式
        //Set the list to sticky header mode
        binding.myRV.layoutManager = StickyGridLayoutManager<YasuoNormalRVAdapter>(this, 3)
        //更换动画
        //Change animation
        binding.myRV.itemAnimator = SlideLeftAlphaAnimator()
        //配置itemDecoration
        //configuration itemDecoration
        binding.myRV.addYasuoDecoration {
//            setDecoration(R.layout.item_layout_text,DrawableBean(this@MainActivity, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp, R.drawable.divider_gray_10dp))
//            setDecoration(R.layout.item_layout_image,DrawableBean(this@MainActivity, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp, R.drawable.divider_black_10dp))
            setDecoration(R.layout.item_layout_text, this@MainActivity, defaultRes)
            setDecoration(R.layout.item_layout_image, this@MainActivity, defaultRes)
        }
        //only All span equal
        //binding.myRV.addItemDecoration(GridSpacingItemDecoration(3, 20, true))
        //findViewById用法
        //findViewById mode
        /*binding.myRV.adapterBinding(this, list, headerList, footerList) {
            //设置可以拖拽和侧滑删除
            //Settings can be deleted by dragging and sliding
            enableDragOrSwipe(binding.myRV, isLongPressDragEnable = true, isItemViewSwipeEnable = true)
            //显示加载更多布局
            //Show load more layouts
            showLoadMoreLayout(DefaultLoadMoreItem())
            //设置加载更多的监听
            //Set to load more listeners
            onLoadMoreListener(binding.myRV) {
                //当itemList的真实数量小于50的时候，模拟加载更多数据
                //When the real number of itemList is less than 50, the simulation loads more data
                if (getItemListTrueSize() < 50) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.bbb))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ccc))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ddd))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
                        //解开监听锁
                        //Unlock the monitor lock
                        enableLoadMoreListener()
                    }, 1000L)
                } else {
                    showLoadMoreLayout(DefaultLoadMoreItem().apply { text.value = "Complete!" })
                }
            }
            //配置空布局1
            //Configure empty Layout 1
            holderConfig(R.layout.empty_layout_one, EmptyBeanOne::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setOnClickListener {
                        showEmptyLayout(EmptyBeanTwo(), true, true)
                    }
                }
            }
            //配置空布局2
            //Configure empty Layout 2
            holderConfig(R.layout.empty_layout_two, EmptyBeanTwo::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setOnClickListener {
                        enableLoadMoreListener()
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))))
                    }
                }
            }
            //配置加载更多布局
            //Configure loadMore Layout 2
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
            //配置header1
            //Configure header1
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
            //配置header2
            //Configure header2
            holderConfig(R.layout.header_layout_two, HeaderTwoBean::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setBackgroundColor(item.headerOneBgColor.value!!)
                }
            }
            //配置footer1
            //Configure footer1
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
            //配置footer2
            //Configure footer2
            holderConfig(R.layout.footer_layout_two, FooterTwoBean::class) {
                onHolderBind { holder, item ->
                    holder.itemView.setBackgroundColor(item.footerTwoBgColor.value!!)
                }
            }
            //配置文本布局
            //Configure text layout
            holderConfig(R.layout.item_layout_text, TextBean::class) {
                //给某个itemViewType的布局统一设置
                //Set the layout of an itemViewType uniformly
                //瀑布流占满一行
                //The staggeredGrid filled the line
                staggeredGridFullSpan = true
                //网格布局占比
                //Proportion of grid layout
                gridSpan = 3
                //吸顶，注意，吸顶会默认占满一行
                //Sticky header. Note that sticky header will fill one line by default
                sticky = true
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.itemText).apply {
                        //如果未使用了LiveData，这样写
                        //If LiveData is not used, write
                        text = item.text.value
                        setOnClickListener {
                            if (item.parentHash != null) {
                                removeFoldChildListItem(item)
                            }
                            itemList.remove(item)
                        }
                        //如果使用了liveData，那么这样写
                        //If livedata is used, write this
                        //必须在设置observe监听之前将所有监听移除
                        //You must remove all listening before setting observe listening
//                        item.text.removeObservers(this@MainActivity)
//                        item.text.observe(this@MainActivity) {
//                            text = it
//                        }
                    }
                }
            }
            //配置展开第一级文本布局
            //Configure expands the first level text layout
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
            //配置展开第二级文本布局
            //Configure expands the second level text layout
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
            //配置展开第三级文本布局
            //Configure expands the third level text layout
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
            //配置图片布局
            //Configure picture layout
            holderConfig(R.layout.item_layout_image, ImageBean::class) {
                //配置该布局可折叠展开（需要实体类继承YasuoFoldItem）
                //Configure the layout to fold and expand (the entity class needs to inherit YasuoFoldItem)
                isFold = true
                onHolderBind { holder, item ->
                    holder.getView<ImageView>(R.id.itemImage).apply {
                        setImageDrawable(item.image.value)
                        setOnClickListener {
                            //点击该图片时展开折叠的子列表
                            //When you click on the image, expand the folded sub list
                            expandOrFoldItem(item)
                        }
                    }
                }
            }
        }*/
        //viewBinding的用法
        //viewBinding mode
        binding.myRV.adapterViewBinding(this, list, headerList, footerList) {
            //设置拖拽及侧滑删除
            //Settings can be deleted by dragging and sliding
            enableDragOrSwipe(binding.myRV, isLongPressDragEnable = true, isItemViewSwipeEnable = true)
            //展示加载更多
            //Show load more layouts
            showLoadMoreLayout(DefaultLoadMoreItem())
            //设置加载更多的监听
            //Set to load more listeners
            onLoadMoreListener(binding.myRV) {
                //当itemList的真实数量小于50的时候，模拟加载更多数据
                //When the real number of itemList is less than 50, the simulation loads more data
                if (getItemListTrueSize() < 50) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.bbb))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ccc))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ddd))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
                        //解开监听锁
                        //Unlock the monitor lock
                        enableLoadMoreListener()
                    }, 1000L)
                } else {
                    showLoadMoreLayout(DefaultLoadMoreItem().apply { text.value = "Click me, keep header and footer and jump to empty Layout 1\n点击我，保留header和footer并跳转空布局1" })
                }
            }
            //配置空布局1
            //Configure empty Layout 1
            holderConfig(R.layout.empty_layout_one, EmptyBeanOne::class, { EmptyLayoutOneBinding.bind(it) }) {
                onHolderBind { holder, item ->
                    holder.itemView.setOnClickListener {
                        showEmptyLayout(EmptyBeanTwo(), true, true)
                    }
                }
            }
            //配置空布局2
            //Configure empty Layout 2
            holderConfig(R.layout.empty_layout_two, EmptyBeanTwo::class, { EmptyLayoutTwoBinding.bind(it) }) {
                onHolderBind { holder, item ->
                    holder.itemView.setOnClickListener {
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))))
                    }
                }
            }
            //配置加载更多布局
            //Configure loadMore Layout 2
            holderConfig(R.layout.default_load_more_layout, DefaultLoadMoreItem::class, {
                DefaultLoadMoreLayoutBinding.bind(it)
            }) {
                onHolderBind { holder, item ->
                    loadMoreText.text = item.text.value
                    holder.itemView.setOnClickListener {
                        showEmptyLayout(EmptyBeanOne())
                    }
                }
            }
            //配置header1
            //Configure header1
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
            //Configure header2
            holderConfig(R.layout.header_layout_two, HeaderTwoBean::class, {
                HeaderLayoutTwoBinding.bind(it)
            }) {
                onHolderBind { holder, item ->
                    root.setBackgroundColor(item.headerOneBgColor.value!!)
                }
            }
            //配置footer1
            //Configure footer1
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
            //Configure footer2
            holderConfig(R.layout.footer_layout_two, FooterTwoBean::class, {
                FooterLayoutTwoBinding.bind(it)
            }) {
                onHolderBind { holder, item ->
                    root.setBackgroundColor(item.footerTwoBgColor.value!!)
                }
            }
            //配置文本布局
            //Configure text layout
            holderConfig(R.layout.item_layout_text, TextBean::class, {
                ItemLayoutTextBinding.bind(it)
            }) {
                //给某个itemViewType的布局统一设置
                //Set the layout of an itemViewType uniformly
                //瀑布流占满一行
                //The staggeredGrid filled the line
                staggeredGridFullSpan = true
                //网格布局占比
                //Proportion of grid layout
                gridSpan = 3
                //吸顶，注意，吸顶会默认占满一行
                //Sticky header. Note that sticky header will fill one line by default
                sticky = true
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                    itemText.setOnClickListener {
                        itemList.remove(item)
                    }
                }
            }
            //配置展开第一级文本布局
            //Configure expands the first level text layout
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
            //配置展开第二级文本布局
            //Configure expands the second level text layout
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
            //配置展开第三级文本布局
            //Configure expands the third level text layout
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
            //Configure picture layout
            holderConfig(R.layout.item_layout_image, ImageBean::class, {
                ItemLayoutImageBinding.bind(it)
            }) {
                //配置该布局可折叠展开（需要实体类继承YasuoFoldItem）
                //Configure the layout to fold and expand (the entity class needs to inherit YasuoFoldItem)
                isFold = true
                onHolderBind { holder, item ->
                    itemImage.setImageDrawable(item.image.value)
                    itemImage.setOnClickListener {
                        //点击该图片时展开折叠的子列表
                        //When you click on the image, expand the folded sub list
                        expandOrFoldItem(item)
                    }
                }
            }
        }
        //dataBinding的用法
        //dataBinding mode
        /*binding.myRV.adapterDataBinding(this, list, headerList, footerList) {
            //设置可以拖拽和侧滑删除
            //Settings can be deleted by dragging and sliding
            enableDragOrSwipe(binding.myRV, isLongPressDragEnable = true, isItemViewSwipeEnable = true)
            //显示加载更多布局
            //Show load more layouts
            showLoadMoreLayout(DefaultLoadMoreItem())
            //设置加载更多的监听
            //Set to load more listeners
            onLoadMoreListener(binding.myRV) {
                //当itemList的真实数量小于50的时候，模拟加载更多数据
                //When the real number of itemList is less than 50, the simulation loads more data
                if (getItemListTrueSize() < 50) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.bbb))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ccc))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ddd))))
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))))
                        //解开监听锁
                        //Unlock the monitor lock
                        enableLoadMoreListener()
                    }, 1000L)
                } else {
                    showLoadMoreLayout(DefaultLoadMoreItem().apply { text.value = "Click me, keep header and footer and jump to empty Layout 1\n点击我，保留header和footer并跳转空布局1" })
                }
            }
            //配置空布局1
            //Configure empty Layout 1
            holderConfig(R.layout.empty_layout_one_data_binding, EmptyBeanOne::class,EmptyLayoutOneDataBindingBinding::class) {
                onHolderBind { holder ->
                    holder.itemView.setOnClickListener {
                        showEmptyLayout(EmptyBeanTwo(), true, true)
                    }
                }
            }
            //配置空布局2
            //Configure empty Layout 2
            holderConfig(R.layout.empty_layout_two_data_binding, EmptyBeanTwo::class,EmptyLayoutTwoDataBindingBinding::class) {
                onHolderBind { holder ->
                    holder.itemView.setOnClickListener {
                        enableLoadMoreListener()
                        itemList.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.aaa))))
                    }
                }
            }
            //配置加载更多布局
            //Configure loadMore Layout 2
            holderConfig(R.layout.default_load_more_layout_data_binding, DefaultLoadMoreItem::class, DefaultLoadMoreLayoutDataBindingBinding::class) {
                onHolderBind { holder ->

                }
            }
            //配置header1
            //Configure header1
            holderConfig(R.layout.header_layout_one_data_binding, HeaderOneBean::class, HeaderLayoutOneDataBindingBinding::class) {
                onHolderBind { holder ->
                    headerText.setOnClickListener {
                        headerList.add(HeaderOneBean(MutableLiveData("我是header${headerList.size}，点击我新增header")))
                    }
                }
            }
            //配置header2
            //Configure header2
            holderConfig(R.layout.header_layout_two_data_binding, HeaderTwoBean::class, HeaderLayoutTwoDataBindingBinding::class) {
                onHolderBind { holder ->

                }
            }
            //配置footer1
            //Configure footer1
            holderConfig(R.layout.footer_layout_one_data_binding, FooterOneBean::class, FooterLayoutOneDataBindingBinding::class) {
                onHolderBind { holder ->
                    footerText.setOnClickListener {
                        footerList.add(FooterOneBean(MutableLiveData("我是footer${footerList.size}，点击我新增footer")))
                    }
                }
            }
            //配置footer2
            //Configure footer2
            holderConfig(R.layout.footer_layout_two_data_binding, FooterTwoBean::class, FooterLayoutTwoDataBindingBinding::class) {
                onHolderBind { holder ->
                }
            }
            //配置文本布局
            //Configure text layout
            holderConfig(R.layout.item_layout_text_data_binding, TextBean::class, ItemLayoutTextDataBindingBinding::class) {
                //给某个itemViewType的布局统一设置
                //Set the layout of an itemViewType uniformly
                //瀑布流占满一行
                //The staggeredGrid filled the line
                staggeredGridFullSpan = true
                //网格布局占比
                //Proportion of grid layout
                gridSpan = 3
                //吸顶，注意，吸顶会默认占满一行
                //Sticky header. Note that sticky header will fill one line by default
                sticky = true
                onHolderBind { holder ->
                    itemText.setOnClickListener {
                        itemList.remove(item!!)
                    }
                }
            }
            //配置展开第一级文本布局
            //Configure expands the first level text layout
            holderConfig(R.layout.item_layout_text_inner_1_data_binding, TextBeanInnerOne::class, ItemLayoutTextInner1DataBindingBinding::class) {
                isFold = true
                gridSpan = 3
                staggeredGridFullSpan = true
                onHolderBind { holder ->
                    itemText.text = item!!.text.value
                    itemText.setOnClickListener {
                        expandOrFoldItem(item!!)
                    }
                }
            }
            //配置展开第二级文本布局
            //Configure expands the second level text layout
            holderConfig(R.layout.item_layout_text_inner_2_data_binding, TextBeanInnerTwo::class, ItemLayoutTextInner2DataBindingBinding::class) {
                isFold = true
                gridSpan = 3
                staggeredGridFullSpan = true
                onHolderBind { holder ->
                    itemText.text = item!!.text.value
                    itemText.setOnClickListener {
                        expandOrFoldItem(item!!)
                    }
                }
            }
            //配置展开第三级文本布局
            //Configure expands the third level text layout
            holderConfig(R.layout.item_layout_text_inner_3_data_binding, TextBeanInnerThree::class, ItemLayoutTextInner3DataBindingBinding::class) {
                isFold = true
                gridSpan = 3
                staggeredGridFullSpan = true
                onHolderBind { holder ->
                    itemText.text = item!!.text.value
                    itemText.setOnClickListener {
                        expandOrFoldItem(item!!)
                    }
                }
            }
            //配置图片布局
            //Configure picture layout
            holderConfig(R.layout.item_layout_image_data_binding, ImageBean::class, ItemLayoutImageDataBindingBinding::class) {
                //配置该布局可折叠展开（需要实体类继承YasuoFoldItem）
                //Configure the layout to fold and expand (the entity class needs to inherit YasuoFoldItem)
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
                4 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.mipmap.ic_launcher))))
                5 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))).apply {
                    //给某个item单独设置
                    //To set an item individually
                    //瀑布流占满一行
                    //The staggeredGrid filled the line
                    staggeredGridFullSpan = true
                    //网格布局占比
                    //Proportion of grid layout
                    gridSpan = 3
                    //吸顶，注意，吸顶会默认占满一行
                    //Sticky header. Note that sticky header will fill one line by default
                    sticky = true
                })
                6 -> list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.ccc))))
            }
        }
        return list
    }
}