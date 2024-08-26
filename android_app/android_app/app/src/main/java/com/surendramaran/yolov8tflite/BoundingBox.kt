package com.surendramaran.yolov8tflite

import android.os.Bundle

data class BoundingBox(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val cnf: Float,
    val cls: Int,
    val clsName: String
)

public fun BoundingBox.toBundle(): Bundle {
    return Bundle().apply {
        putFloat("x1", x1)
        putFloat("y1", y1)
        putFloat("x2", x2)
        putFloat("y2", y2)
        putFloat("cx", cx)
        putFloat("cy", cy)
        putFloat("w", w)
        putFloat("h", h)
        putFloat("cnf", cnf)
        putInt("cls", cls)
        putString("clsName", clsName)
    }
}

public fun Bundle.toBoundingBox(): BoundingBox {
    return BoundingBox(
        getFloat("x1"),
        getFloat("y1"),
        getFloat("x2"),
        getFloat("y2"),
        getFloat("cx"),
        getFloat("cy"),
        getFloat("w"),
        getFloat("h"),
        getFloat("cnf"),
        getInt("cls"),
        getString("clsName") ?: ""
    )
}

fun List<BoundingBox>.toParcelableArrayList(): ArrayList<Bundle> {
    return ArrayList(map { it.toBundle() })
}

fun ArrayList<Bundle>.toBoundingBoxList(): List<BoundingBox> {
    return map { it.toBoundingBox() }
}