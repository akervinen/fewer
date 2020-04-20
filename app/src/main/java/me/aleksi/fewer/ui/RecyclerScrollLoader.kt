package me.aleksi.fewer.ui

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// https://stackoverflow.com/a/26561717
class RecyclerScrollLoader(
    private val layoutManager: LinearLayoutManager,
    private val updateDataList: () -> Unit
) : RecyclerView.OnScrollListener() {
    private var previousTotal = 0
    var loading = true
    var moreToLoad = true
    private val visibleThreshold = 10
    private var firstVisibleItem = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        visibleItemCount = recyclerView.childCount
        totalItemCount = layoutManager.itemCount
        firstVisibleItem = layoutManager.findFirstVisibleItemPosition()

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false
                previousTotal = totalItemCount
            }
        }

        if (!loading && moreToLoad && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            updateDataList()
            loading = true
        }
    }
}
