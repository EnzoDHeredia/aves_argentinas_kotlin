package com.example.avesargentinas.ui.bird

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.avesargentinas.R

class BirdInfoPagerAdapter(
    private val bindDistribution: (View) -> Unit,
    private val clearDistribution: (View) -> Unit,
    private val bindInformation: (View) -> Unit,
    private val clearInformation: (View) -> Unit
) : RecyclerView.Adapter<BirdInfoPagerAdapter.Page>() {
    class Page(view: View) : RecyclerView.ViewHolder(view)

    private val pages = mutableSetOf<Page>()
    var selectedPage = 0
        set(value) {
            field = value
            pages.forEach(::updateAccessibility)
        }

    override fun getItemCount() = 2
    override fun getItemViewType(position: Int) = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Page {
        val layout = if (viewType == 0) R.layout.view_bird_information else R.layout.view_bird_distribution
        return Page(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: Page, position: Int) {
        pages.add(holder)
        updateAccessibility(holder)
        if (position == 1) bindDistribution(holder.itemView) else bindInformation(holder.itemView)
    }

    private fun updateAccessibility(page: Page) {
        val active = page.itemViewType == selectedPage
        page.itemView.importantForAccessibility = if (active) View.IMPORTANT_FOR_ACCESSIBILITY_AUTO
            else View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        if (!active) page.itemView.clearFocus()
    }

    override fun onViewRecycled(holder: Page) {
        if (holder.itemViewType == 1) clearDistribution(holder.itemView)
        else clearInformation(holder.itemView)
        pages.remove(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        pages.filter { it.itemViewType == 1 }.forEach { clearDistribution(it.itemView) }
        pages.filter { it.itemViewType == 0 }.forEach { clearInformation(it.itemView) }
        pages.clear()
    }
}
