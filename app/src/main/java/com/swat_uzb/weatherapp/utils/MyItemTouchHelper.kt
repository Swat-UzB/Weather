package com.swat_uzb.weatherapp.utils

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.swat_uzb.weatherapp.R
import javax.inject.Inject


class MyItemTouchHelper @Inject constructor() : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags: Int = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags: Int = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.itemView?.setBackgroundColor(
                ContextCompat.getColor(
                    viewHolder.itemView.context,
                    R.color.cardview_shadow_end_color
                )
            )
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        viewHolder.itemView.setBackgroundColor(
            Color.TRANSPARENT
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        onItemMoveListener?.let { onItemMove ->
            onItemMove(viewHolder.adapterPosition, target.adapterPosition)
        }
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onItemSwipedListener?.let { onItemSwiped ->
            onItemSwiped(viewHolder.adapterPosition)
        }
    }

    private var onItemMoveListener: ((fromPosition: Int, toPosition: Int) -> Unit)? = null

    private var onItemSwipedListener: ((Position: Int) -> Unit)? = null

}