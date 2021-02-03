package com.fusion_nex_gen.yasuorvadapter.listener

import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion_nex_gen.yasuorvadapter.YasuoBaseRVAdapter
import com.fusion_nex_gen.yasuorvadapter.sticky.StickyGridLayoutManager
import com.fusion_nex_gen.yasuorvadapter.sticky.StickyLinearLayoutManager
import com.fusion_nex_gen.yasuorvadapter.sticky.StickyStaggeredGridLayoutManager

fun <Adapter : YasuoBaseRVAdapter<*, *, *>> RecyclerView.onLoadMoreListener(
    adapter: Adapter,
    onLoadMore: YasuoLoadMoreListener.(lastVisiblePosition: Int) -> Unit
) {
    addOnScrollListener(YasuoLoadMoreListener(adapter, onLoadMore))
}

class YasuoLoadMoreListener(
    private val adapter: YasuoBaseRVAdapter<*, *, *>,
    private val onLoadMore: YasuoLoadMoreListener.(lastVisiblePosition: Int) -> Unit
) :
    RecyclerView.OnScrollListener() {

    var lastPosition = -1
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
            is StickyGridLayoutManager<*> -> {
                //通过LayoutManager找到当前显示的最后的item的position
                lastPosition = layoutManager.findLastVisibleItemPosition()
            }
            is StickyLinearLayoutManager<*> -> {
                lastPosition = layoutManager.findLastVisibleItemPosition()
            }
            is StickyStaggeredGridLayoutManager<*> -> {
                //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
                //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
                val lastPositionArray =
                    layoutManager.findLastVisibleItemPositions(null)
                lastPosition = lastPositionArray[lastPositionArray.size - 1]
            }
            is GridLayoutManager -> {
                //通过LayoutManager找到当前显示的最后的item的position
                lastPosition = layoutManager.findLastVisibleItemPosition()
                Log.e("lastPosition", lastPosition.toString())
            }
            is LinearLayoutManager -> {
                lastPosition = layoutManager.findLastVisibleItemPosition()
            }
            is StaggeredGridLayoutManager -> {
                //因为StaggeredGridLayoutManager的特殊性可能导致最后显示的item存在多个，所以这里取到的是一个数组
                //得到这个数组后再取到数组中position值最大的那个就是最后显示的position值了
                val lastPositionArray =
                    layoutManager.findLastVisibleItemPositions(null)
                lastPosition = lastPositionArray[lastPositionArray.size - 1]
            }
        }
        //时判断界面显示的最后item的position是否等于所有列表item数之和，也就是最后一个loadMore的position
        //如果相等则说明已经滑动到最后了
        if (adapter.inLoadMoreList(lastPosition)) {
            Log.e("loadMoreListener", "开始加载更多，最底部的item：position=${lastPosition}，已显示")
            adapter.disableLoadMoreListener()
            this.onLoadMore(lastPosition)
        }
    }
}
