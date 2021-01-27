package com.fusion_nex_gen.yasuorvadapter.extension

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion_nex_gen.yasuorvadapter.YasuoBaseRVAdapter

class LoadMoreListener(
    private val adapter: YasuoBaseRVAdapter<*, *>,
    private val onLoadMore: LoadMoreListener.(lastVisiblePosition: Int) -> Unit
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
            is GridLayoutManager -> {
                //通过LayoutManager找到当前显示的最后的item的position
                lastPosition = layoutManager.findLastVisibleItemPosition()
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
        //时判断界面显示的最后item的position是否等于itemCount总数-1也就是最后一个item的position
        //如果相等则说明已经滑动到最后了
        if (lastPosition == recyclerView.layoutManager!!.itemCount - 1) {
            print("开始加载更多，最底部的item：position=${lastPosition}，已显示")
            adapter.lockedLoadMoreListener = true
            this.onLoadMore(lastPosition)
        }
    }
}

fun <Adapter : YasuoBaseRVAdapter<*, *>> RecyclerView.setOnLoadMoreListener(
    adapter: Adapter,
    onLoadMore: LoadMoreListener.(lastVisiblePosition: Int) -> Unit
) {
    addOnScrollListener(LoadMoreListener(adapter, onLoadMore))
}
