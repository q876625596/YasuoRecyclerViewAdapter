# YasuoRecyclerViewAdapter
一个能让你感受到快乐的RecyclerViewAdapter库

[![](https://jitpack.io/v/q876625596/YasuoRecyclerViewAdapter.svg)](https://jitpack.io/#q876625596/YasuoRecyclerViewAdapter)

**[Language]** [English](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/README.md) | 中文文档

![图片来自：https://www.zcool.com.cn/work/ZNDU0NzA2MTY=.html](https://upload-images.jianshu.io/upload_images/3106054-959a3c4c2c450a78.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
## 一、前言
**YasuoRecyclerViewAdapter！让你在Android中快乐的实现列表！**
[掘金](https://juejin.cn/post/6926784485623087117)
[简书](https://www.jianshu.com/p/7b062942ee26)

## 二、正片
简单介绍一下这个库：**[YasuoRecyclerViewAdapter](https://github.com/q876625596/YasuoRecyclerViewAdapter)** ，为什么要取名为Yasuo，因为亚索==快乐！这个库就是为了让大家在写代码的时候感受到快乐而存在的！

#### 1、功能特色
**①、List，Grid，StaggeredGrid类型的正常布局及多布局**

**②、空白页/头部/尾部**

**③、加载更多**

**④、折叠布局（支持多级折叠）**

**⑤、拖拽、横向滑动删除**

**⑥、附送两个ItemDecoration，可根据不同需求选择**

**⑦、采用ObservableList作为数据源，无需手动notify**

**⑧、支持findViewById，ViewBinding，DataBinding三种模式，可根据你现有项目模式或喜好随意更换！**

**⑨、动画的高可配置(综合考虑后采用recyclerView的itemAnimator方案，如有需要请自行依赖mikepenz大神的[ItemAnimators](https://github.com/mikepenz/ItemAnimators)库)**

**⑩、吸顶(采用[sticky-layoutmanager](https://github.com/qiujayen/sticky-layoutmanager)的方案，低耦合adapter和item，由于原库的position获取有一些bug，便将其集成到本项目中并修复了bug)**

#### 2、依赖，最新版本请看[github](https://github.com/q876625596/YasuoRecyclerViewAdapter)或者[jitpack](https://jitpack.io/#q876625596/YasuoRecyclerViewAdapter)

``` groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

``` groovy
dependencies {
    implementation 'com.github.q876625596:YasuoRecyclerViewAdapter:x.y.z'
}
```

#### 2、示例展示


**如果想直接Ctrl + CV代码，快速上手的同学，请直接移步[sample](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/app/src/main/java/com/fusion_nex_gen/yasuorecyclerviewadapter/MainActivity.kt)**

![吸顶.gif](https://user-images.githubusercontent.com/20555239/107305126-53123f00-6abd-11eb-9bbe-7ef0d85e8c7e.gif) ![加载更多.gif](https://user-images.githubusercontent.com/20555239/107305175-6d4c1d00-6abd-11eb-9ce8-c322f731a17b.gif) ![空布局，header，footer.gif](https://user-images.githubusercontent.com/20555239/107305208-7dfc9300-6abd-11eb-9f95-4c403cae05c4.gif) ![折叠.gif](https://user-images.githubusercontent.com/20555239/107305232-8a80eb80-6abd-11eb-8e98-0d379c8ee07d.gif) ![拖拽，侧滑删除.gif](https://user-images.githubusercontent.com/20555239/107305255-979dda80-6abd-11eb-961f-54c576ae56dc.gif)


#### 3、详细介绍

##### 1）数据源
必须使用[YasuoList](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/YasuoRVAdapter/src/main/java/com/fusion_nex_gen/yasuorvadapter/bean/YasuoList.kt)或其子类作为数据源，YasuoList继承自ObservableArrayList，新增了部分常用方法，并在adapter内部做了监听处理，因此使用该类型数据源可以不用手动notify

##### 2）简单写法（单布局/多布局/header/footer）

``` kotlin
    fun findViewByIdMode(){
        //数据源
        val list = YasuoList<Any>()
        val headerList = YasuoList<Any>()
        val footerList = YasuoList<Any>()
        binding.myRV.layoutManager = GridLayoutManager(this, 3)
        //findViewById模式
        binding.myRV.adapterBinding(this,list){
            //do something
            //绑定文本布局
            //只需要给对应的布局配置holderConfig，即可实现多布局，header，footer
            holderConfig(R.layout.item_layout_text, TextBean::class) {
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.itemText).apply {
                        text = item.text.value
                    }
                }
            }
        }
        //ViewBinding模式
        binding.myRV.adapterViewBinding(this,list){
            //do something
            //配置文本布局
            //只需要给对应的布局配置holderConfig，即可实现多布局，header，footer
            holderConfig(R.layout.item_layout_text, TextBean::class, { ItemLayoutTextBinding.bind(it) }) {
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                }
            }
        }
        //DataBinding模式
        binding.myRV.adapterDataBinding(this,list){
            //do something
            //配置文本布局
            //只需要给对应的布局配置holderConfig，即可实现多布局，header，footer
            holderConfig(R.layout.item_layout_text_data_binding, TextBean::class, ItemLayoutTextDataBindingBinding::class) {
                onHolderBind { holder ->
                    //dataBinding模式已在xml中绑定了数据，无需手动设置
                }
            }
        }
    }
```

以上三种模式的差异就只有这么一点，相互切换也相当的方便。

**额外的ViewPager2Adapter**
adapterDataBinding可以替换为adapterViewBinding，adapterBinding
**创建布局时需要注意：adapterDataBinding中默认为的BR.vpItem**

``` kotlin
    binding.viewPager.adapterDataBinding(this, list) {
        holderConfigVP(R.layout.empty_layout_one_data_binding, EmptyBeanOne::class, EmptyLayoutOneDataBindingBinding::class) {

        }
        holderConfigVP(R.layout.empty_layout_two_data_binding, EmptyBeanTwo::class, EmptyLayoutTwoDataBindingBinding::class) {

        }
     }
```

##### 3）空布局
空布局的使用也非常简单，先将空布局的holderConfig配置之后，再调用**adapter.showEmptyLayout**就行了。

``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //do something
            holderConfig(R.layout.item_layout_text, TextBean::class, { ItemLayoutTextBinding.bind(it) }) {
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                    itemText.setOnClickListener {
                        showEmptyLayout(/*空布局实体*/EmptyBeanTwo(), /*是否清空header*/true, /*是否清空footer*/true)
                    }
                }
            }
        }
```

##### 4）对布局设置占比
设置占比有两种方式，第一种，给一种类型的布局设置占比：
``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //do something
            holderConfig(R.layout.item_layout_text, TextBean::class, { ItemLayoutTextBinding.bind(it) }) {
                //do something
                //给某个itemViewType的布局统一设置
                //瀑布流占满一行
                staggeredGridFullSpan = true
                //网格布局占比
                gridSpan = 3
            }
        }
```
第二种，针对某个item单独设置占比：
``` kotlin
        list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))).apply {
                    //给某个item单独设置
                    //瀑布流占满一行
                    staggeredGridFullSpan = true
                    //网格布局占比
                    gridSpan = 3
        }
```
**判断优先级：单个item设置 > 类型设置**

##### 5）加载更多
加载更多和空布局类似，也是先将加载更多布局的holderConfig配置之后，再调用**adapter.showLoadMoreLayout**使空布局显示出来，最后添加**adapter.onLoadMoreListener**监听即可。

``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //展示加载更多
            showLoadMoreLayout(DefaultLoadMoreItem())
            //设置加载更多的监听
            onLoadMoreListener(binding.myRV) {
                //请求数据...
            }
            //do something
        }
```

##### 6）拖拽/侧滑删除
只需要使用**adapter.enableDragOrSwipe**即可启用拖拽，同时也可以设置监听，设置手势方向，以及对某些特定布局禁用等

``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //拖拽/侧滑删除
            enableDragOrSwipe(binding.myRV, isLongPressDragEnable = true, isItemViewSwipeEnable = true)
            //do something
        }
```

##### 7）吸顶
首先设置layoutManager：**StickyLinearLayoutManager**，**StickyGridLayoutManager**，**StickyStaggeredGridLayoutManager**
吸顶有两种方式，第一种，对某一个类型的布局设置吸顶
``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //do something
            holderConfig(R.layout.item_layout_text, TextBean::class, { ItemLayoutTextBinding.bind(it) }) {
                //给某个itemViewType的布局统一设置
                //吸顶，注意，吸顶会默认占满一行
                sticky = true
                //do something
            }
        }
```
第二种，针对某个item设置吸顶
``` kotlin
list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))).apply {
        //给某个item单独设置
        //吸顶，注意，吸顶会默认占满一行
        sticky = true
}
```

**判断优先级：单个item设置 > 类型设置**

##### 8）折叠布局
折叠布局需要数据类继承[YasuoFoldItem](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/YasuoRVAdapter/src/main/java/com/fusion_nex_gen/yasuorvadapter/bean/YasuoFoldItem.kt)，之后只需要使用**adapter.expandOrFoldItem**来展开/收起即可，支持多级折叠，如果需要删除或添加折叠布局中的某个item，建议使用**adapter.removeAndFoldListItem**和**adapter.addAndFoldListItem**方法

##### 9）动画配置
动画采用mikepenz大神的[ItemAnimators](https://github.com/mikepenz/ItemAnimators)库，如有需要，请先自行依赖该库。
``` kotlin
        binding.myRV.itemAnimator = SlideLeftAlphaAnimator()
```

##### 10）附送的itemDecoration
支持为每条边单独设置样式
``` kotlin
        binding.myRV.addYasuoDecoration {
            setDecoration(R.layout.item_layout_text, this@MainActivity, defaultRes)
            setDecoration(R.layout.item_layout_image, this@MainActivity, defaultRes)
        }
```
额外附一个span相等的网格布局专用空白分隔ItemDecoration
``` kotlin
        binding.myRV.addItemDecoration(GridSpacingItemDecoration(3, 20, true))
```

#### 4、api展示

##### 1）adapter可配置属性/方法一览
| 属性名/方法名 | 介绍 |默认值 |
| ------ | ------ | ------ |
| itemList | 主体列表 | YasuoList<T>() |
| headerList | 头部列表 | YasuoList<T>() |
| footerList | 尾部列表 | YasuoList<T>() |
| showLoadMoreLayout() | 配置并显示加载更多布局 | ------ |
| removeLoadMore() | 移除加载更多布局 | ------ |
| enableLoadMoreListener() | 启用列表滚动到底部时加载更多的监听 | ------ |
| disableLoadMoreListener() | 禁用列表滚动到底部时加载更多的监听 | ------ |
| isShowEmptyLayout() | 判断当前是否是显示空布局状态 | ------ |
| showEmptyLayout() | 配置并显示空布局 | ------ |
| expandOrFoldItem() | 展开/折叠某个item | ------ |
| removeAndFoldListItem() | 移除一个item的同时移除其折叠列表的相同item | ------ |
| getAllListSize() | 获取全部列表的长度 | ------ |
| getItemListTrueSize() | 获取[itemList]的实际长度 | ------ |
| getHeaderListTrueSize() | 获取[headerList]的实际长度 | ------ |
| getFooterListTrueSize() | 获取[footerList]的实际长度 | ------ |
| getHeaderTruePosition() | 获取[headerList]的真实position | ------ |
| getItemTruePosition() | 获取[itemList]的真实position | ------ |
| getFooterTruePosition() | 获取[footerList]的真实position | ------ |
| inHeaderList() | 判断position在[headerList]内 | ------ |
| inItemList() | 判断position在[itemList]内 | ------ |
| inFooterList() | 判断position在[footerList]内 | ------ |
| setAfterDataChangeListener() | 列表数据发生改变后的监听，在notify之后触发 | ------ |
| enableDragOrSwipe() | 设置item是否可拖拽、滑动删除 | ------ |
| onLoadMoreListener() | 设置加载更多的监听 | ------ |
| holderConfig() | 配置holder，建立数据类与布局文件之间的匹配关系 | ------ |


##### 2）holderConfig可配置属性/方法一览
| 属性名/方法名 | 介绍 |默认值|
| ------ | ------ | ------ |
| sticky | 该类型布局是否吸顶 | false |
| isFold | 该类型布局是否支持展开折叠 | false |
| gridSpan | 该类型布局在grid中的占比 | 0 |
| staggeredGridFullSpan | 该类型布局在staggeredGrid中是否占满 | false |
| holderCreateListener | 该类型Holder创建时的监听 | null |
| holderBindListener | 该类型的Holder绑定时的监听 | null |
| createBindingFun | ViewBinding的创建方法，仅ViewBinding模式 | null |
| variableId | xml中对应的数据id，仅DataBinding模式 | BR.item |

##### 3）YasuoNormalItem可配置属性/方法一览
| 属性名/方法名 | 介绍 |默认值|
| ------ | ------ | ------ |
| sticky | 该item是否吸顶 | false |
| gridSpan | 该item在grid中的占比 | 0 |
| staggeredGridFullSpan | 该item在staggeredGrid中是否占满 | false |

##### 4）YasuoFoldItem可配置属性/方法一览
| 属性名/方法名 | 介绍 |默认值|
| ------ | ------ | ------ |
| list | 下一级列表 | YasuoList<YasuoFoldItem>() |
| isExpand | 是否已展开 | false |
| parentHash | 父级hash，展开后才会赋值 | false |
| sticky | 该item是否吸顶 | false |
| gridSpan | 该item在grid中的占比 | 0 |
| staggeredGridFullSpan | 该item在staggeredGrid中是否占满 | false |

## 三、结语
首先感谢大家的阅读，马上就要过年了，在这里也祝大家**新年快乐**，希望这个库能够对你们有所帮助。如果你喜欢**[YasuoRecyclerViewAdapter](https://github.com/q876625596/YasuoRecyclerViewAdapter)**这个库，希望能在github上给一个star，作为我进步的动力！
如果他有不足或需要新增的功能，可以向我提issue或添加我的qq:876625596

# [License](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/LICENSE)
