package com.GroceerCart.sa.adapter

import android.util.SparseArray
import androidx.recyclerview.widget.RecyclerView
import com.GroceerCart.sa.listener.Section

abstract class SectionAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var sections : SparseArray<Section> = SparseArray()

    override fun getItemCount() = sections.size()

    fun configSections(sections: List<Section>) {
        this.sections.clear()
        sections.sortedWith(Comparator { lhs, rhs ->
            if (lhs.firstPosition == rhs.firstPosition) 0
            else if (lhs.firstPosition < rhs.firstPosition) -1
            else 1
        })
        var offset = 0
        sections.forEach {
            it.sectionedPosition = it.firstPosition + offset
            this.sections.append(it.sectionedPosition, it)
            ++offset
        }
    }

}