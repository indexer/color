package com.indexer.zeekwat

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import java.util.*

class ColorUtils(bitmap: Bitmap) {
    private val COLOR_THRESHOLD_MINIMUM_PERCENTAGE = 0.01
    private val EDGE_COLOR_DISCARD_THRESHOLD = 0.3
    private val MINIMUM_SATURATION_THRESHOLD = 0.15f

    private val mBitmap: Bitmap

    private var mImageColors: ColorCount<Int>? = null
    var backgroundColor: Int = 0
        private set
    private var mPrimaryColor: Int? = null
    private var mSecondaryColor: Int? = null
    private var mDetailColor: Int? = null


    init {
        mBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, false)
        analyzeImage()
    }

    private fun analyzeImage() {
        backgroundColor = findEdgeColor()
        findTextColors(mImageColors!!)
        val hasDarkBackground = isDarkColor(backgroundColor)

        if (mPrimaryColor == null) {
            Log.d(LOG_TAG, "Unable to detect primary color in image")
            if (hasDarkBackground) {
                mPrimaryColor = Color.WHITE
            } else {
                mPrimaryColor = Color.BLACK
            }
        }

        if (mSecondaryColor == null) {
            Log.d(LOG_TAG, "Unable to detect secondary in image")
            if (hasDarkBackground) {
                mSecondaryColor = Color.WHITE
            } else {
                mSecondaryColor = Color.BLACK
            }
        }

        if (mDetailColor == null) {
            Log.d(LOG_TAG, "Unable to detect detail color in image")
            if (hasDarkBackground) {
                mDetailColor = Color.WHITE
            } else {
                mDetailColor = Color.BLACK
            }
        }
    }

    private fun findEdgeColor(): Int {
        val height = mBitmap.getHeight()
        val width = mBitmap.getWidth()

        mImageColors = ColorCount<Int>()
        val leftImageColors = ColorCount<Int>()
        for (x in 0..width - 1) {
            for (y in 0..height - 1) {
                if (x == 0) {
                    leftImageColors.add(mBitmap.getPixel(x, y))
                }
                mImageColors!!.add(mBitmap.getPixel(x, y))
            }
        }

        val sortedColors = ArrayList<CountedColor>()

        val randomColorThreshold = (height * COLOR_THRESHOLD_MINIMUM_PERCENTAGE).toInt()
        val iterator = leftImageColors.iterator()
        while (iterator.hasNext()) {
            val color = iterator.next()
            val colorCount = leftImageColors.getCount(color)
            if (colorCount < randomColorThreshold) {
                continue
            }

            val container = CountedColor(color!!, colorCount)
            sortedColors.add(container)
        }

        Collections.sort<CountedColor>(sortedColors)

        val sortedColorIterator = sortedColors.iterator()
        if (!sortedColorIterator.hasNext()) {
            return Color.BLACK
        }

        var proposedEdgeColor = sortedColorIterator.next()
        if (!proposedEdgeColor.isBlackOrWhite) {
            return proposedEdgeColor.color
        }

        while (sortedColorIterator.hasNext()) {
            val nextProposedColor = sortedColorIterator.next()
            val edgeColorRatio = nextProposedColor.count.toDouble() / proposedEdgeColor.count
            if (edgeColorRatio <= EDGE_COLOR_DISCARD_THRESHOLD) {
                break
            }

            if (!nextProposedColor.isBlackOrWhite) {
                proposedEdgeColor = nextProposedColor
                break
            }
        }

        return proposedEdgeColor.color
    }

    private fun findTextColors(colors: ColorCount<Int>) {
        val iterator = colors.iterator()
        var currentColor: Int
        val sortedColors = ArrayList<CountedColor>()
        val findDarkTextColor = !isDarkColor(backgroundColor)

        while (iterator.hasNext()) {
            currentColor = iterator.next()
            currentColor = colorWithMinimumSaturation(currentColor, MINIMUM_SATURATION_THRESHOLD)
            if (isDarkColor(currentColor) == findDarkTextColor) {
                val colorCount = colors.getCount(currentColor)
                val container = CountedColor(currentColor, colorCount)
                sortedColors.add(container)
            }
        }

        Collections.sort<CountedColor>(sortedColors)

        for (currentContainer in sortedColors) {
            currentColor = currentContainer.color
            if (mPrimaryColor == null) {
                if (isContrastingColor(currentColor, backgroundColor)) {
                    mPrimaryColor = currentColor
                }
            } else if (mSecondaryColor == null) {
                if (!isDistinctColor(mPrimaryColor!!, currentColor) || !isContrastingColor(currentColor, backgroundColor)) {
                    continue
                }
                mSecondaryColor = currentColor
            } else if (mDetailColor == null) {
                if (!isDistinctColor(mSecondaryColor!!, currentColor) ||
                        !isDistinctColor(mPrimaryColor!!, currentColor) ||
                        !isContrastingColor(currentColor, backgroundColor)) {
                    continue
                }
                mDetailColor = currentColor
                break
            }
        }
    }

    val primaryColor: Int
        get() {
            return mPrimaryColor!!
        }

    val secondaryColor: Int
        get() {
            return mSecondaryColor!!
        }

    val detailColor: Int
        get() {
            return mDetailColor!!
        }

    //helpers
    private fun colorWithMinimumSaturation(color: Int, minSaturation: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        if (hsv[1] < minSaturation) {
            return Color.HSVToColor(floatArrayOf(hsv[0], minSaturation, hsv[2]))
        }

        return color
    }

    private fun isDarkColor(color: Int): Boolean {
        val r = Color.red(color).toDouble() / 255
        val g = Color.green(color).toDouble() / 255
        val b = Color.blue(color).toDouble() / 255

        val lum = 0.2126 * r + 0.7152 * g + 0.0722 * b

        return lum < 0.5
    }

    private fun isContrastingColor(backgroundColor: Int, foregroundColor: Int): Boolean {
        val br = Color.red(backgroundColor).toDouble() / 255
        val bg = Color.green(backgroundColor).toDouble() / 255
        val bb = Color.blue(backgroundColor).toDouble() / 255

        val fr = Color.red(foregroundColor).toDouble() / 255
        val fg = Color.green(foregroundColor).toDouble() / 255
        val fb = Color.blue(foregroundColor).toDouble() / 255


        val bLum = 0.2126 * br + 0.7152 * bg + 0.0722 * bb
        val fLum = 0.2126 * fr + 0.7152 * fg + 0.0722 * fb

        val contrast: Double

        if (bLum > fLum) {
            contrast = (bLum + 0.05) / (fLum + 0.05)
        } else {
            contrast = (fLum + 0.05) / (bLum + 0.05)
        }

        return contrast > 1.6
    }

    private fun isDistinctColor(colorA: Int, colorB: Int): Boolean {
        val r = Color.red(colorA).toDouble() / 255
        val g = Color.green(colorA).toDouble() / 255
        val b = Color.blue(colorA).toDouble() / 255
        val a = Color.alpha(colorA).toDouble() / 255

        val r1 = Color.red(colorB).toDouble() / 255
        val g1 = Color.green(colorB).toDouble() / 255
        val b1 = Color.blue(colorB).toDouble() / 255
        val a1 = Color.alpha(colorB).toDouble() / 255

        val threshold = .25 //.15

        if (Math.abs(r - r1) > threshold ||
                Math.abs(g - g1) > threshold ||
                Math.abs(b - b1) > threshold ||
                Math.abs(a - a1) > threshold) {
            // check for grays, prevent multiple gray colors

            if (Math.abs(r - g) < .03 && Math.abs(r - b) < .03 &&
                    (Math.abs(r1 - g1) < .03 && Math.abs(r1 - b1) < .03)) {
                return false
            }

            return true
        }

        return false
    }

    private inner class CountedColor(val color: Int, val count: Int) : Comparable<CountedColor> {

        public override fun compareTo(another: CountedColor): Int {
            return if (count < another.count) -1 else (if (count == another.count) 0 else 1)
        }

        // color is white or black
        val isBlackOrWhite: Boolean
            get() {
                val r = Color.red(color).toDouble() / 255
                val g = Color.green(color).toDouble() / 255
                val b = Color.blue(color).toDouble() / 255

                if ((r > .91 && g > .91 && b > .91) || (r < .09 && g < .09 && b < .09)) return true

                return false
            }

    }

    companion object {
        private val LOG_TAG = "Color"
    }
}
