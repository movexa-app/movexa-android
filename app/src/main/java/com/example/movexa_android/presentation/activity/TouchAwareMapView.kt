package com.example.movexa_android.presentation.activity

import android.content.Context
import android.view.MotionEvent
import org.osmdroid.views.MapView
import kotlin.math.abs

/**
 * A MapView that allows vertical swipes to pass through to the parent.
 * Returning 'false' from dispatchTouchEvent on vertical move allows the 
 * Compose AndroidView to let other components handle the gesture.
 */
class TouchAwareMapView(context: Context) : MapView(context) {

    private var startY = 0f
    private var startX = 0f

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startY = ev.y
                startX = ev.x
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = abs(ev.y - startY)
                val dx = abs(ev.x - startX)

                // If moving vertically, yield to the BottomSheetScaffold
                if (dy > dx && dy > 15f) {
                    parent?.requestDisallowInterceptTouchEvent(false)
                    return false // Important: Stop consuming so parent can start a drag
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
