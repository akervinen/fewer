package me.aleksi.fewer.ui

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Endless scrolling for a [RecyclerView].
 *
 * Uses [layoutManager] to count items and calls [loadMore] to load more.
 *
 * Based on: [StackOverflow Answer #26561717](https://stackoverflow.com/a/26561717)
 */
class RecyclerScrollLoader(
    private val layoutManager: LinearLayoutManager,
    private val loadMore: () -> Unit
) : RecyclerView.OnScrollListener() {
    private var previousTotal = 0
    var loading = true
    var moreToLoad = true
    private val visibleThreshold = 10
    private var firstVisibleItem = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0

    /**
     * Called when [RecyclerView] is scrolled.
     */
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
            loadMore()
            loading = true
        }
    }
}
