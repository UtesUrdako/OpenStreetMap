package com.broadcast.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.graphics.toRect
import androidx.core.view.setPadding
import com.utesurdako.openstreetmap.Data.MapField
import com.utesurdako.openstreetmap.Data.onFieldChangedListener
import java.lang.Float.min
import kotlin.math.max

typealias OnTileActionsListener = () -> Unit

class CustomViewModel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var dx: Float = 0f
    var dy: Float = 0f

    private val listener: onFieldChangedListener = {
        invalidate()
    }
    var actionsListener: OnTileActionsListener? = null

    var mapField: MapField? = null
        set(value) {
            field?.listeners?.remove(listener)
            field = value
            value?.listeners?.add(listener)
            updateViewSizes()
            requestLayout()
            invalidate()
        }

    private val fieldRect = RectF()
    private val tileRect = RectF()
    private var tileSize: Float = 0f

    private lateinit var tilePaint: Paint

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mapField?.listeners?.add (listener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mapField?.listeners?.remove (listener)
    }

    init {
        if (attrs != null)
            initAttributes(attrs, defStyleAttr)
        initPaint()
    }

    private fun initAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, drawableState, defStyleAttr, defStyleAttr)
        setPadding(0)
        typedArray.recycle()
    }

    private fun initPaint() {
        tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val minHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val desiredTileSizeInPixel = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DESIRED_TILE_SIZE,
            resources.displayMetrics
        ).toInt()

        val rows = mapField?.rows ?: 0
        val columns = mapField?.columns ?: 0

        val desiredWith = max(minWidth, columns * desiredTileSizeInPixel + paddingLeft + paddingRight)
        val desiredHeight = max(minHeight, rows * desiredTileSizeInPixel + paddingTop + paddingBottom)

        setMeasuredDimension(
            resolveSize(desiredWith, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec),
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSizes()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mapField == null) return
        if (tileSize == 0f) return
        if (fieldRect.width() <= 0) return
        if (fieldRect.height() <= 0) return

        canvas?.translate(dx, dy)
//        dx = 0f
//        dy = 0f

        val field = this.mapField ?: return
        for (row in 0 until field.rows) {
            for (column in 0 until field.columns) {
                val tile = field.getTile(row, column)
                if (tile.tile == null) continue
                canvas?.drawBitmap(tile.tile, fieldRect.toRect(), getTileRect(row, column), tilePaint)
            }
        }
        //canvas.drawBitmap()
    }

    private fun getTileRect(row: Int, column: Int): RectF {
        tileRect.left = fieldRect.left + column * tileSize
        tileRect.top = fieldRect.top + row * tileSize
        tileRect.right = tileRect.left + tileSize
        tileRect.bottom = tileRect.top + tileSize
        return  tileRect
    }

    private fun updateViewSizes() {
        val field = this.mapField ?: return

        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingTop - paddingBottom

        val tileWidth = safeWidth / field.columns.toFloat()
        val tileHeight = safeHeight / field.rows.toFloat()

        tileSize = min(tileWidth, tileHeight)

        val fieldWidth = tileSize * field.columns
        val fieldHeight = tileSize * field.rows

        fieldRect.left = paddingLeft + (safeWidth - fieldWidth) / 2
        fieldRect.top = paddingTop + (safeHeight - fieldHeight) / 2
        fieldRect.right = fieldRect.left + fieldWidth
        fieldRect.bottom = fieldRect.top + fieldHeight
    }

    companion object {
        const val DESIRED_TILE_SIZE = 100f
    }
}