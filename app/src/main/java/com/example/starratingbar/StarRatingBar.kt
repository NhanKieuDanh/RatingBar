package com.example.starratingbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class StarRatingView : View {

    ///region VARIABLES

    private var mNumberOfStars = 0
    private var mCurrentStar = 0f
    private var mStarMargin = 4f
    private var mStarRadius = 40f
    private var mNormalColor = Color.YELLOW
    private var mFillColor = Color.RED

    private var mStarHeight: Float = 0.toFloat()
    private var mStarWidth: Float = 0.toFloat()

    private var mPaint: Paint? = null
    private var mPath: Path? = null
    private var mStarPath: Path? = null
    private var mClipPath: Path? = null
    private var mRectPath: Path? = null

    ///endregion

    ///region CONSTRUCTORS

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    ///endregion

    ///region OVERRIDDEN FUNCTIONS

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // allocations per draw cycle.

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        val centerY = (contentHeight - mStarHeight) / 2 + mStarRadius
        val startX = (contentWidth.toFloat() - mNumberOfStars * mStarWidth - mStarMargin * (mNumberOfStars - 1)) / 2

        for (i in 0 until mNumberOfStars) {
            val centerX = startX + mStarWidth * (2 * i + 1) / 2 + mStarMargin * i
            drawStar(mCurrentStar - i, centerX + paddingLeft, centerY + paddingTop, canvas)
        }
    }

    ///endregion

    ///region PRIVATE FUNCTIONS

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.StarRatingView, defStyle, 0)

        mNumberOfStars = a.getInt(R.styleable.StarRatingView_star_count, mNumberOfStars)
        mCurrentStar = a.getFloat(R.styleable.StarRatingView_star_value, mCurrentStar)

        mStarMargin = a.getDimension(R.styleable.StarRatingView_star_margin, mStarMargin)
        mStarRadius = a.getDimension(R.styleable.StarRatingView_star_radius, mStarRadius)

        mNormalColor = a.getColor(R.styleable.StarRatingView_star_normal_color, mNormalColor)
        mFillColor = a.getColor(R.styleable.StarRatingView_star_fill_color, mFillColor)

        a.recycle()
        initData(mStarRadius, mCurrentStar)
    }

    private fun drawStar(percent: Float, x: Float, y: Float, canvas: Canvas) {
        canvas.save()
        mPath?.let {
            canvas.translate(x, y)
            it.reset()
            it.addPath(mStarPath)
            mPaint?.color = mNormalColor
            mPaint?.style = Paint.Style.FILL
            canvas.drawPath(it, mPaint!!)
            canvas.restore()

            if (percent < 0) return

            canvas.save()
            canvas.translate(x, y)
            it.rewind()
            it.addPath(if (percent < 1) mClipPath else mRectPath)
            mPaint?.color = Color.GRAY
            mPaint?.style = Paint.Style.STROKE
            canvas.clipPath(it)

            it.rewind()
            it.addPath(mStarPath)
            mPaint?.color = mFillColor
            mPaint?.style = Paint.Style.FILL
            canvas.drawPath(it, mPaint!!)
        }
        canvas.restore()
    }

    private fun initData(starRadius: Float, currentStar: Float) {
        // Set up a default Paint object
        mPaint = Paint(ANTI_ALIAS_FLAG)
        mPath = Path()

        mStarWidth = (2.0 * starRadius.toDouble() * Math.sin(2 * Math.PI / STAR_SECTION)).toFloat()
        mStarHeight =
                (2.0 * starRadius.toDouble() * Math.sin(2 * Math.PI / STAR_SECTION) * Math.sin(2 * Math.PI / STAR_SECTION)).toFloat()

        val innerStartRadius =
            (starRadius * Math.sin(Math.PI / (2 * STAR_SECTION)) / Math.cos(Math.PI / STAR_SECTION)).toFloat()

        val section = Math.PI / STAR_SECTION

        mStarPath = Path()
        mStarPath?.moveTo((starRadius * Math.sin(0.0)).toFloat(), (-starRadius * Math.cos(0.0)).toFloat())
        mStarPath?.lineTo(
            (innerStartRadius * Math.sin(section)).toFloat(),
            (-innerStartRadius * Math.cos(section)).toFloat()
        )

        for (i in 1 until STAR_SECTION) {
            mStarPath?.lineTo(
                (starRadius * Math.sin(section * i.toDouble() * 2.0)).toFloat(),
                (-starRadius * Math.cos(section * i.toDouble() * 2.0)).toFloat()
            )
            mStarPath?.lineTo(
                (innerStartRadius * Math.sin(section * (2 * i + 1))).toFloat(),
                (-innerStartRadius * Math.cos(section * (2 * i + 1))).toFloat()
            )
        }

        mStarPath?.close()
        mRectPath = initRectPath(0.5f, mStarRadius, mStarWidth, mStarHeight)
        mClipPath = initRectPath(
            (currentStar.toDouble() - Math.floor(currentStar.toDouble()) - 0.5).toFloat(),
            starRadius,
            mStarWidth,
            mStarHeight
        )
    }

    private fun initRectPath(percent: Float, starRadius: Float, starWidth: Float, starHeight: Float): Path {
        val path = Path()
        path.moveTo(starWidth * percent, -starRadius)                    // right top
        path.lineTo(starWidth * percent, starHeight - starRadius)     // right bottom
        path.lineTo(-starWidth / 2, starHeight - starRadius)          // left bottom
        path.lineTo(-starWidth / 2, -starRadius)                         // left top
        path.close()
        return path
    }

    ///endregion

    ///region PUBLIC FUNCTIONS

    fun getNumberOfStars(): Int {
        return mNumberOfStars
    }

    fun setNumberOfStars(numberOfStars: Int) {
        this.mNumberOfStars = numberOfStars
        invalidate()
    }

    fun getCurrentStar(): Float {
        return mCurrentStar
    }

    fun setCurrentStar(currentStar: Float) {
        this.mCurrentStar = currentStar
        initData(mStarRadius, mCurrentStar)
        invalidate()
    }

    fun getStarMargin(): Float {
        return mStarMargin
    }

    fun setStarMargin(starMargin: Float) {
        this.mStarMargin = starMargin
        invalidate()
    }

    fun getStarRadius(): Float {
        return mStarRadius
    }

    fun setStarRadius(starRadius: Float) {
        this.mStarRadius = starRadius
        initData(mStarRadius, mCurrentStar)
        invalidate()
    }

    fun getNormalColor(): Int {
        return mNormalColor
    }

    fun setNormalColor(mNormalColor: Int) {
        this.mNormalColor = mNormalColor
        invalidate()
    }

    fun getFillColor(): Int {
        return mFillColor
    }

    fun setFillColor(mFillColor: Int) {
        this.mFillColor = mFillColor
        invalidate()
    }

    companion object {
        private const val STAR_SECTION = 5
    }

    ///endregion

}