package com.fusion_nex_gen.yasuorvadapter.listener

import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion_nex_gen.yasuorvadapter.YasuoBaseRVAdapter

fun <Adapter : YasuoBaseRVAdapter<*, *,*>> RecyclerView.onStickyListener(
    adapter: Adapter,
    onSticky: YasuoStickyListener.(firstVisibleItemPosition: Int,firstCompletelyVisibleItemPosition: Int) -> Unit
) {
    addOnScrollListener(YasuoStickyListener(adapter, onSticky))
}

class YasuoStickyListener(
    private val adapter: YasuoBaseRVAdapter<*, *,*>,
    private val onLoadMore: YasuoStickyListener.(firstVisibleItemPosition: Int,firstCompletelyVisibleItemPosition: Int) -> Unit
) :
    RecyclerView.OnScrollListener() {

    var firstVisibleItemPosition = -1
    var firstCompletelyVisibleItemPosition = -1
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        RecyclerView.SCROLL_STATE_DRAGGING
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (adapter.lockedLoadMoreListener) {
            return
        }
        //当前RecyclerView显示出来的最后一个的item的position
        when (val layoutManager = recyclerView.layoutManager) {
            is GridLayoutManager -> {
                //通过LayoutManager找到当前显示的最后的item的position
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            }
            is LinearLayoutManager -> {
                firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                firstCompletelyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            }
            is StaggeredGridLayoutManager -> {
                //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
                //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
                val firstPositionArray =
                    layoutManager.findFirstVisibleItemPositions(null)
                val firstCompletelyPositionArray =
                    layoutManager.findFirstCompletelyVisibleItemPositions(null)
                firstVisibleItemPosition = firstPositionArray[0]
                firstCompletelyVisibleItemPosition = firstCompletelyPositionArray[0]
            }
        }
        Log.e("first",firstVisibleItemPosition.toString())
        Log.e("firstC",firstCompletelyVisibleItemPosition.toString())
        //时判断界面显示的最后item的position是否等于itemCount总数-1也就是最后一个item的position
        //如果相等则说明已经滑动到最后了
        /*if (firstVisibleItemPosition == recyclerView.layoutManager!!.itemCount - 1) {
            print("开始加载更多，最底部的item：position=${firstVisibleItemPosition}，已显示")
            adapter.lockedLoadMoreListener = true
            this.onLoadMore(firstVisibleItemPosition)
        }*/
    }
}
