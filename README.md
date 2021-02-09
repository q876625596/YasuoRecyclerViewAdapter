# YasuoRecyclerViewAdapter
A RecyclerViewAdapter library that can make you feel happy

[![](https://jitpack.io/v/q876625596/YasuoRecyclerViewAdapter.svg)](https://jitpack.io/#q876625596/YasuoRecyclerViewAdapter)

**[Language]** English | [中文文档](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/README_CN.md)

![Image from：https://www.zcool.com.cn/work/ZNDU0NzA2MTY=.html](https://upload-images.jianshu.io/upload_images/3106054-959a3c4c2c450a78.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
## Ⅰ、Preface
**YasuoRecyclerViewAdapter ! Let you happy realization list in Android !**
[掘金](https://juejin.cn/post/6926784485623087117)
[简书](https://www.jianshu.com/p/7b062942ee26)

## Ⅱ、Main
Here's a brief introduction to this library：**[YasuoRecyclerViewAdapter](https://github.com/q876625596/YasuoRecyclerViewAdapter)** ，Why Yasuo, because Yasuo = = happy! This library is for everyone to feel happy when writing code!

#### 1、Functional features
**①、Normal layout and multi layout of list, grid and stageredgrid**

**②、EmptyLayout/Header/Footer**

**③、LoadMore**

**④、 Folding layout  (support multi-level fold) **

**⑤、Drag, slide delete**

**⑥、Two ItemDecoration are attached, which can be selected according to different needs**

**⑦、The ObservableList is used as the data source without manual notify**

**⑧、Support findViewById, ViewBinding, DataBinding three modes, according to your existing project mode or preferences to change!**

**⑨、Highly configurable animation (after comprehensive consideration, the itemanimator scheme of recyclerview is adopted. If necessary, please rely on the [ItemAnimators](https://github.com/mikepenz/ItemAnimators) Library of mikepenz)**

**⑩、Stick header (using [sticky-layoutmanager](https://github.com/qiujayen/sticky-layoutmanager) Because there are some bugs in the position acquisition of the original library, we integrate them into this project and fix the bugs.)**

#### 2、For the latest version, please see [jitpack](https://jitpack.io/#q876625596/YasuoRecyclerViewAdapter)

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


**If you want to use Ctrl + CV code directly, please move [sample](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/app/src/main/java/com/fusion_nex_gen/yasuorecyclerviewadapter/MainActivity.kt) directly**

![sticky.gif](https://user-images.githubusercontent.com/20555239/107305126-53123f00-6abd-11eb-9bbe-7ef0d85e8c7e.gif) ![loadMore.gif](https://user-images.githubusercontent.com/20555239/107305175-6d4c1d00-6abd-11eb-9ce8-c322f731a17b.gif) ![emptyLayout，header，footer.gif](https://user-images.githubusercontent.com/20555239/107305208-7dfc9300-6abd-11eb-9f95-4c403cae05c4.gif) ![fold.gif](https://user-images.githubusercontent.com/20555239/107305232-8a80eb80-6abd-11eb-8e98-0d379c8ee07d.gif) ![drag,swipr delete.gif](https://user-images.githubusercontent.com/20555239/107305255-979dda80-6abd-11eb-961f-54c576ae56dc.gif)


#### 3、Detailed introduction

##### 1）Data source
You must use [YasuoList](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/YasuoRVAdapter/src/main/java/com/fusion_nex_gen/yasuorvadapter/bean/YasuoList.kt) or its subclass as the data source. Yasuolist inherits from observablearraylist, adds some common methods, and listens inside the adapter. Therefore, you can use this type of data source without manually notifying

##### 2）Simple writing（Single layout/Multi layout/Header/Footer）

``` kotlin
    fun findViewByIdMode(){
        //data source
        val list = YasuoList<Any>()
        val headerList = YasuoList<Any>()
        val footerList = YasuoList<Any>()
        binding.myRV.layoutManager = GridLayoutManager(this, 3)
        //findViewById mode
        binding.myRV.adapterBinding(this,list){
            //do something
            //Binding text layout
            //Just configure holderConfig for the corresponding layout to realize multi layout, header and footer
            holderConfig(R.layout.item_layout_text, TextBean::class) {
                onHolderBind { holder, item ->
                    holder.getView<TextView>(R.id.itemText).apply {
                        text = item.text.value
                    }
                }
            }
        }
        //ViewBinding mode
        binding.myRV.adapterViewBinding(this,list){
            //do something
            //Binding text layout
            //Just configure holderConfig for the corresponding layout to realize multi layout, header and footer
            holderConfig(R.layout.item_layout_text, TextBean::class, { ItemLayoutTextBinding.bind(it) }) {
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                }
            }
        }
        //DataBinding mode
        binding.myRV.adapterDataBinding(this,list){
            //do something
            //Binding text layout
            //Just configure holderConfig for the corresponding layout to realize multi layout, header and footer
            holderConfig(R.layout.item_layout_text_data_binding, TextBean::class, ItemLayoutTextDataBindingBinding::class) {
                onHolderBind { holder ->
                    //The databinding schema has already bound data in XML, so there is no need to set it manually
                }
            }
        }
    }
```

There is only one difference between the above three modes, and it is quite convenient to switch between them.

##### 3）Empty Layout
The use of empty layout is also very simple. First configure the holderConfig of empty layout, and then call** adapter.showEmptyLayout **That's it.

``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //do something
            holderConfig(R.layout.item_layout_text, TextBean::class, { ItemLayoutTextBinding.bind(it) }) {
                onHolderBind { holder, item ->
                    itemText.text = item.text.value
                    itemText.setOnClickListener {
                        showEmptyLayout(/*Empty layout entity*/EmptyBeanTwo(), /*Clear header*/true, /*Clear footer*/true)
                    }
                }
            }
        }
```

##### 4）Set span for layout
There are two ways to set the proportion. The first is to set the proportion for one type of layout：
``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //do something
            holderConfig(R.layout.item_layout_text, TextBean::class, { ItemLayoutTextBinding.bind(it) }) {
                //do something
                //Set the layout of an itemViewType uniformly
                //The staggeredGridLayouytManager filled the line
                staggeredGridFullSpan = true
                //gridLayoutManager Span
                gridSpan = 3
            }
        }
```
Second, set the proportion of an item separately：
``` kotlin
        list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))).apply {
                    //To set an item individually
                    //The staggeredGridLayouytManager filled the line
                    staggeredGridFullSpan = true
                    //gridLayoutManager Span
                    gridSpan = 3
        }
```
**Judging priority: single item setting > type setting**

##### 5）LoadMore
Loading more layouts is similar to loading empty layouts. After loading the holderConfig configuration of more layouts, call ** adapter.showLoadMoreLayout ** Make the empty layout display and add it at last ** adapter.onLoadMoreListener ** Just monitor.

``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //Show load more
            showLoadMoreLayout(DefaultLoadMoreItem())
            //Set to load more listeners
            onLoadMoreListener(binding.myRV) {
                //Request data...
            }
            //do something
        }
```

##### 6）Drag / swipe delete
Just use** adapter.enableDragOrSwipe **You can enable drag and drop, set monitor, set gesture direction, and disable certain layouts

``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //Drag / swipe delete
            enableDragOrSwipe(binding.myRV, isLongPressDragEnable = true, isItemViewSwipeEnable = true)
            //do something
        }
```

##### 7）Sticky header
First, set the layout manager: * * stickylinear layout manager * *, * * stickygridlayout manager * *, * * stickystaged GridLayout manager**
There are two ways of Sticky header. The first is to set Sticky header for a certain type of layout
``` kotlin
        binding.myRV.adapterViewBinding(this,list){
            //do something
            holderConfig(R.layout.item_layout_text, TextBean::class, { ItemLayoutTextBinding.bind(it) }) {
                //Set the layout of an itemViewType uniformly
                //Sticky header. Note that sticky header will fill one line by default
                sticky = true
                //do something
            }
        }
```
Second, set the Sticky header for an item
``` kotlin
list.add(ImageBean(MutableLiveData(ContextCompat.getDrawable(this@MainActivity, R.drawable.eee))).apply {
        //To set an item individually
        //Sticky header. Note that sticky header will fill one line by default
        sticky = true
}
```

**Judging priority: single item setting > type setting**

##### 8）Folding layout
Folding layout requires data class to inherit [YasuoFoldItem](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/YasuoRVAdapter/src/main/java/com/fusion_nex_gen/yasuorvadapter/bean/YasuoFoldItem.kt), and then only need to use** adapter.expandOrFoldItem **It supports multi-level folding. If you need to delete or add an item in the folding layout, it is recommended to use** adapter.removeAndFoldListItem **And** adapter.addAndFoldListItem **Methods

##### 9）Animation configuration
The animation uses mikepenz's [ItemAnimators](https://github.com/mikepenz/ItemAnimators) library. If necessary, please rely on this library.
``` kotlin
        binding.myRV.itemAnimator = SlideLeftAlphaAnimator()
```

##### 10）Attached itemDecoration
It supports setting styles for each edge individually
``` kotlin
        binding.myRV.addYasuoDecoration {
            setDecoration(R.layout.item_layout_text, this@MainActivity, defaultRes)
            setDecoration(R.layout.item_layout_image, this@MainActivity, defaultRes)
        }
```
An additional span equal grid layout space is attached to separate itemDecoration
``` kotlin
        binding.myRV.addItemDecoration(GridSpacingItemDecoration(3, 20, true))
```

#### 4、API display

##### 1）List of adapter configurable properties / methods
| Property name / method name | introduce |Default value |
| ------ | ------ | ------ |
| itemList | main list | YasuoList<T>() |
| headerList | header list | YasuoList<T>() |
| footerList | footer list | YasuoList<T>() |
| showLoadMoreLayout() | Configure and show loading more layouts | ------ |
| removeLoadMore() | Remove load more layouts | ------ |
| enableLoadMoreListener() | Enable list scrolling to the bottom to load more listeners | ------ |
| disableLoadMoreListener() | Disable list scrolling to the bottom when loading more listeners | ------ |
| isShowEmptyLayout() | Determine whether the current display is empty layout state | ------ |
| showEmptyLayout() | Configure and display empty layouts | ------ |
| expandOrFoldItem() | Expand / fold an item | ------ |
| removeAndFoldListItem() | Remove an item and remove the same item in its collapsed list at the same time | ------ |
| getAllListSize() | Gets the length of all lists | ------ |
| getItemListTrueSize() | Get the actual length of [itemList] | ------ |
| getHeaderListTrueSize() | Get the actual length of [headerList] | ------ |
| getFooterListTrueSize() | Get the actual length of [footerList] | ------ |
| getHeaderTruePosition() | Get the real position of [headerList] | ------ |
| getItemTruePosition() | Get the real position of [itemList] | ------ |
| getFooterTruePosition() | Get the real position of [footerList] | ------ |
| inHeaderList() | Judge that position is in [headerList] | ------ |
| inItemList() | Judge that position is in [itemList] | ------ |
| inFooterList() | Judge that position is in [footerList] | ------ |
| setAfterDataChangeListener() | After the list data changes, the monitor will be triggered after notify | ------ |
| enableDragOrSwipe() | Set whether the item can be dragged or deleted by sliding | ------ |
| onLoadMoreListener() | Set to load more listeners | ------ |
| holderConfig() | Configure holder to establish the matching relationship between data class and layout file | ------ |


##### 2）Overview of holderConfig configurable properties / methods
| Property name / method name | introduce |Default value |
| ------ | ------ | ------ |
| sticky | Is this type of layout a sticky header | false |
| isFold | Whether this type of layout supports expand/fold | false |
| gridSpan | The proportion of this type of layout in Grid | 0 |
| staggeredGridFullSpan | Is this type of layout full in the staggeedgrid | false |
| holderCreateListener | Listen when the type holder is created | null |
| holderBindListener | The type of holder is used to listen when binding | null |
| createBindingFun | The creation method of viewbinding is only viewbinding mode | null |
| variableId | The corresponding data ID in XML is only in the data binding mode | BR.item |

##### 3）Overview of YasuoNormalItem configurable properties / methods
| Property name / method name | introduce |Default value |
| ------ | ------ | ------ |
| sticky | Is this type of layout a sticky header | false |
| gridSpan | The proportion of this type of layout in Grid | 0 |
| staggeredGridFullSpan | Is this type of layout full in the staggeedgrid | false |

##### 4）Overview of YasuoFoldItem configurable properties / methods
| Property name / method name | introduce |Default value |
| ------ | ------ | ------ |
| list | Next level list | YasuoList<YasuoFoldItem>() |
| isExpand | Expanded | false |
| parentHash | Parent hash, which will be assigned after expansion | false |
| sticky | Is this type of layout a sticky header | false |
| gridSpan | The proportion of this type of layout in Grid | 0 |
| staggeredGridFullSpan | Is this type of layout full in the staggeedgrid | false |

## Ⅲ、Epilogue
First of all, thank you for your reading. The new year is coming soon. I also wish you a happy new year. I hope this library can help you. If you like * * [YasuoRecyclerViewAdapter](https://github.com/q876625596/YasuoRecyclerViewAdapter) * * this library, I hope to give you a star on GitHub as a driving force for my progress!
If he has deficiencies or needs new functions, he can ask me issue or add my QQ: 876625596

# [License](https://github.com/q876625596/YasuoRecyclerViewAdapter/blob/main/LICENSE)
