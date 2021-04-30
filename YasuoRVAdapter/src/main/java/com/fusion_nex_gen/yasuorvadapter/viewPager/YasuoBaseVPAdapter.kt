package com.fusion_nex_gen.yasuorvadapter.viewPager

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoBaseItemConfigForVP
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import kotlin.reflect.KClass

abstract class YasuoBaseVPAdapter<VH : RecyclerView.ViewHolder, Config : YasuoBaseItemConfigForVP<VH>>
    (
    /**
     * item列表
     * Item list
     */
    val itemList: YasuoList<Any>,

    ) :
    RecyclerView.Adapter<VH>() {

    internal val dataInvalidation = Any()

    /**
     * 列表布局配置的集合，实体类[KClass]作为key，类型[YasuoBaseItemConfigForVP]作为value
     * 通常是这样获取：itemClassTypes[getItem(position)::class]
     * Set of list layout configurations, with entity class [KClass] as key and type [YasuoBaseItemConfigForVP] as value
     * Usually, get: itemClassTypes[getItem (position)::class]
     */
    val itemClassTypes: MutableMap<KClass<*>, Config> = mutableMapOf()

    /**
     * 列表布局配置的集合，layoutId作为key，类型[YasuoBaseItemConfigForVP]作为value
     * 相当于[itemClassTypes]的复制品，为的是能通过layoutId来获取[YasuoBaseItemConfigForVP]
     * 这样做是为了更方便的获取到[YasuoBaseItemConfigForVP]
     * 通常是这样获取：itemIdTypes[getItemViewType]
     * A set of list layout configurations, with layoutId as key and type [YasuoBaseItemConfigForVP] as value
     * It is equivalent to a replica of [itemClassTypes] so that [YasuoBaseItemConfigForVP] can be obtained through layoutId
     * This is to obtain [YasuoBaseItemConfigForVP] more conveniently
     * Usually, get: itemClassTypes[getItem (position)::class]
     */
    val itemIdTypes: SparseArray<Config> = SparseArray()

    /**
     * ViewPager2
     */
    internal var viewPager: ViewPager2? = null
    fun getViewPager() = viewPager

    /**
     * 布局创建器
     * Layout Creator
     */
    internal var inflater: LayoutInflater? = null
    fun initInflater(context: Context) {
        if (inflater == null) {
            inflater = LayoutInflater.from(context)
        }
    }

    /******item相关 Item related******/

    /**
     * 获取可显示的所有的item数量
     * Get the number of all items that can be displayed
     */
    override fun getItemCount(): Int {
        //这里如果是显示空布局，那么就需要把loadMore隐藏
        //If the empty layout is displayed, you need to hide loadMore
        return itemList.size
    }

    /**
     * 通过[position]获取item
     * Get item through [position]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    open fun getItem(position: Int): Any {
        return itemList[position]
    }

    /**
     * 防止itemView闪烁
     * Prevent ItemView from flashing
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    override fun getItemId(position: Int): Long {
        return if (hasStableIds()) {
            (getItem(position).hashCode() + position).toLong()
        } else {
            super.getItemId(position)
        }
    }

    /**
     * 通过[position]获取item的类型（类型即为布局id）
     * Get the type of item through [position] (the type is layout ID)
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    override fun getItemViewType(position: Int): Int {
        return itemClassTypes[getItem(position)::class]?.itemLayoutId
            ?: throw RuntimeException(
                "找不到viewType，position = ${position}，\n" +
                        "viewType not found,position = $position"
            )
    }

    /**
     * [itemList]改变的监听
     * Monitoring of [itemList] changes
     */
    val itemListListener = object : ObservableList.OnListChangedCallback<ObservableList<Any>>() {
        override fun onChanged(contributorViewModels: ObservableList<Any>) {
            notifyDataSetChanged()
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeChanged(contributorViewModels: ObservableList<Any>, i: Int, i1: Int) {
            notifyItemRangeChanged(i, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeInserted(
            contributorViewModels: ObservableList<Any>,
            i: Int,
            i1: Int
        ) {
            notifyItemRangeInserted(i, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeMoved(
            contributorViewModels: ObservableList<Any>,
            i: Int,
            i1: Int,
            i2: Int
        ) {
            notifyItemMoved(i, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeRemoved(contributorViewModels: ObservableList<Any>, i: Int, i1: Int) {
            if (contributorViewModels.isEmpty()) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeRemoved(i, i1)
            }
            afterDataChangeListener?.invoke()
        }
    }

    /**
     * 列表数据发生改变后的监听，在notify之后触发
     * After the list data changes, the monitor will be triggered after notify
     */
    private var afterDataChangeListener: (() -> Unit)? = null

    fun setAfterDataChangeListener(listener: () -> Unit) {
        afterDataChangeListener = listener
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        if (isValidPayLoads(payloads)) {
            onBindViewHolder(holder, position)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    private fun isValidPayLoads(payloads: List<Any>): Boolean {
        if (payloads.isEmpty()) {
            return false
        }
        payloads.forEach {
            if (it != dataInvalidation) {
                return false
            }
        }
        return true
    }

}

/**
 * 绑定适配器
 * Bind Adapter
 */
fun <VH, Config : YasuoBaseItemConfigForVP<VH>, Adapter : YasuoBaseVPAdapter<VH, Config>> Adapter.attach(vp: ViewPager2): Adapter {
    vp.adapter = this
    return this
}
