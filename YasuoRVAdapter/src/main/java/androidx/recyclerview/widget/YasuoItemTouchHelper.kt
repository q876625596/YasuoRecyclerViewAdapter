package androidx.recyclerview.widget

open class YasuoItemTouchHelper(callback: Callback) : ItemTouchHelper(callback) {
    public override fun select(selected: RecyclerView.ViewHolder?, actionState: Int) {
        super.select(selected, actionState)
    }
}