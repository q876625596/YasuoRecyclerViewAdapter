# YasuoRecyclerViewAdapter
一个能让你感受到快乐的RecyclerViewAdapter库

![图片来自：https://www.zcool.com.cn/work/ZNDU0NzA2MTY=.html](https://upload-images.jianshu.io/upload_images/3106054-959a3c4c2c450a78.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
## 一、前言
自从我开始做安卓开发以来，我就得了一种病，**Adapter编写焦虑症**。在现如今的安卓App开发中，大家用得最多的ViewGroup，那一定是身为老大哥的RecyclerView，单布局列表，多布局列表，网格列表，瀑布流列表，折叠列表，吸顶列表，甚至一个无需滚动的页面，出于屏幕高度适配考虑，都有可能做成列表形式。这足以证明RecyclerView在实际开发中的重要性。

那么这样一个如此重要的组件，通常情况下我们需要怎么实现呢？

**第一步**：在布局中添加RecyclerVIew
**第二步**：创建item布局及其实体类
**第三步**：继承**RecyclerView.Adapter**类，编写自定义Adapter
**第四步**：绑定Adapter到RecyclerView

以上便是实现一个完整的列表所需的步骤，其中第三步可谓是整个流程中最为复杂的，目前的一些三方库，在一定程度上简化了第三步，让开发者在继承了他们自定义的Adapter之后，可以少些很大一部分的代码，从而提升开发效率，我以前也是用过这些优秀的库，比如安卓界使用最广泛[BRVAH](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)，为开发者们大大减少了开发时间，让大家有时间回家陪陪家人，首先在这里感谢各位大佬的贡献。

但是，由于我接触kotlin比较早，2016年7月开始从事Android开发，2017年初开始接触kotlin，然后在17年5月google开始正式钦定kotlin之后便开始正式使用kotlin做项目，从那时起，我就被kotlin的dsl所吸引，便开始尝试将这些优秀的第三方库进行dsl的改造，奈何当时的能力不足，这种几乎重写的改造宣告失败。既然大型改造不行，那我能不能去找一些别人写好的adapter dsl呢？于是经过我的不断搜索，终于在github上找到了一些库，比如[kotlin-adapter](https://github.com/wuhenzhizao/kotlin-adapter)和[Yasha](https://github.com/ssseasonnn/Yasha)。

这两个都是优秀的库，曾经我也在项目中使用过他们，但是渐渐地我发现他们已经无法满足我的一些需求了，我便开始尝试写一个自己用的库。当时这个库还是存在于我的私有项目中，我在项目中遇到的问题就可以及时改，直到19年的上半年为止，这个库基本可以用于大多数日常开发，当时我就考虑将它进行一些完善工作后开源，但是世事难料，由于工作原因，19年的7月，我去了一趟柬埔寨，没错，就是程序员们口诛笔伐的东南亚国家之一。

去那边之后，因为人手问题，我不得不学习了vue开发，flutter开发，以及后端开发，几乎没有时间来搞这个库的开源工作。时间到了20年的9月，由于工作安排，又回到了重庆，又经过几个月的忙碌后，终于在最近抽出了一些时间，来继续进行作业。

当我重新审视这个库的时候，我发现了很多可以优化的点，这代表我这一年多是在进步，我很高兴，然后花了两三周的空余时间将其优化完善，新增一些功能后，我决定在今天将它发布出来，希望大家能够喜欢。

不好意思，前言有点啰嗦，但依旧不妨碍后续的精彩。

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


**如果想直接Ctrl CV代码，快速上手的同学，请直接移步[sample](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/app/src/main/java/com/fusion_nex_gen/yasuorecyclerviewadapter/MainActivity.kt)**

![吸顶.gif](https://upload-images.jianshu.io/upload_images/3106054-7d8cd2f661c479e2.gif?imageMogr2/auto-orient/strip) ![加载更多.gif](https://upload-images.jianshu.io/upload_images/3106054-8288fd89ce3c35ed.gif?imageMogr2/auto-orient/strip) ![空布局，header，footer.gif](https://upload-images.jianshu.io/upload_images/3106054-154d54fa4cb550c3.gif?imageMogr2/auto-orient/strip) ![折叠.gif](https://upload-images.jianshu.io/upload_images/3106054-d6cac74b56459e4a.gif?imageMogr2/auto-orient/strip) ![拖拽，侧滑删除.gif](https://upload-images.jianshu.io/upload_images/3106054-1a1bad944b878496.gif?imageMogr2/auto-orient/strip)


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
                    //dataBInding模式已在xml中绑定了数据，无需手动设置
                }
            }
        }
    }
```

以上三种模式的差异就只有这么一点，相互切换也相当的方便。

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
| 属性名/方法名 | 介绍 |默认值|
| ------ | ------ | ------ |
| itemList | 主体列表 | YasuoList<T>() |
| headerList | 头部列表 | YasuoList<T>() |
| footerList | 尾部列表 | YasuoList<T>() |
| showLoadMoreLayout(loadMoreItem: T) | 配置并显示加载更多布局 | ------ |
| removeLoadMore() | 移除加载更多布局 | ------ |
| enableLoadMoreListener() | 启用列表滚动到底部时加载更多的监听 | ------ |
| disableLoadMoreListener() | 禁用列表滚动到底部时加载更多的监听 | ------ |
| isShowEmptyLayout() | 判断当前是否是显示空布局状态 | ------ |
| showEmptyLayout(emptyItem: T, clearHeader: Boolean = false, clearFooter: Boolean = false) | 判断当前是否是显示空布局状态 | ------ |
| expandOrFoldItem(item: YasuoFoldItem) | 展开/折叠某个item | ------ |
| removeAndFoldListItem(childItem: Any, foldList: YasuoList<YasuoFoldItem>? = null) | 移除一个item的同时移除其折叠列表的相同item | ------ |
| getAllListSize() | 获取全部列表的长度 | ------ |
| getItemListTrueSize() | 获取[itemList]的实际长度 | ------ |
| getHeaderListTrueSize() | 获取[headerList]的实际长度 | ------ |
| getFooterListTrueSize() | 获取[footerList]的实际长度 | ------ |
| getHeaderTruePosition(position: Int) | 获取[headerList]的真实position | ------ |
| getItemTruePosition(position: Int) | 获取[itemList]的真实position | ------ |
| getFooterTruePosition(position: Int) | 获取[footerList]的真实position | ------ |
| inHeaderList(position: Int) | 判断position在[headerList]内 | ------ |
| inItemList(position: Int) | 判断position在[itemList]内 | ------ |
| inFooterList(position: Int) | 判断position在[footerList]内 | ------ |
| setAfterDataChangeListener(listener: () -> Unit) | 列表数据发生改变后的监听，在notify之后触发 | ------ |
| enableDragOrSwipe(...) | 设置item是否可拖拽、滑动删除 | ------ |
| onLoadMoreListener(...) | 设置加载更多的监听 | ------ |
| holderConfig(...) | 配置holder，建立数据类与布局文件之间的匹配关系 | ------ |


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

##### 4）YasuoNormalItem可配置属性/方法一览
| 属性名/方法名 | 介绍 |默认值|
| ------ | ------ | ------ |
| list | 下一级列表 | YasuoList<YasuoFoldItem>() |
| isExpand | 是否已展开 | false |
| parentHash | 父级hash，展开后才会赋值 | false |
| sticky | 该item是否吸顶 | false |
| gridSpan | 该item在grid中的占比 | 0 |
| staggeredGridFullSpan | 该item在staggeredGrid中是否占满 | false |

## 三、结语
首先感谢大家的阅读，马上就要过年了，在这里也祝大家**新年快乐**，希望这个库能够对你们有所帮助。如果你喜欢**[YasuoRecyclerViewAdapter](https://github.com/q876625596/YasuoRecyclerViewAdapter)**这个库，希望能在github上给一个start，作为我进步的动力！
如果他有不足或需要新增的功能，可以向我提issue或添加我的qq:876625596

# [License](https://github.com/q876625596/YasuoRecyclerViewAdapter/LICENSE)
