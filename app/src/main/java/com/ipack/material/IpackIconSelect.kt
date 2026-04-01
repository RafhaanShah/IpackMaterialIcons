package com.ipack.material

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Typeface
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Collections

class IpackIconSelect : Activity() {
    /* access modifiers changed from: private */
    var cellSize: Int = DEFAULT_CELL_WIDTH
    private var grid: GridView? = null
    private var gridBackColour = 863467383

    /* access modifiers changed from: private */
    var iconSize: Int = DEFAULT_ICON_SIZE

    /* access modifiers changed from: private */
    var icons: Bundle = Bundle()

    /* access modifiers changed from: private */
    var sortTask: IconSortTask? = null

    /* access modifiers changed from: private */
    var sortedIDs: MutableList<Int?> = mutableListOf()

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setResult(0)
        IpackContent.fillBundle(getResources(), this.icons)
        parseCallIntent()
        setupGrid()
        requestWindowFeature(INFO_PADDING)
        setTitle(IpackContent.LABEL)
        val layout = LinearLayout(this)
        layout.setOrientation(LinearLayout.VERTICAL)
        layout.addView(this.infoTextView)
        layout.addView(this.grid)
        setContentView(layout)
        setProgressBarIndeterminateVisibility(true)
        this.sortTask = IconSortTask()
        this.sortTask!!.execute(*arrayOf<Boolean>(true))
    }

    private val infoTextView: TextView
        get() {
            val info = TextView(this)
            val b = StringBuilder()
            b.append("#").append(this.icons.size())
            val p = this.oneIconSize
            if (p != null) {
                b.append(" ").append(p.x).append("x").append(p.y)
            }
            if (!TextUtils.isEmpty(IpackContent.ATTRIBUTION)) {
                b.append(" [").append(IpackContent.ATTRIBUTION).append("]")
            }
            info.setText(b.toString())
            info.setTextSize(16.0f)
            info.setPadding(
                INFO_PADDING,
                INFO_PADDING,
                INFO_PADDING,
                INFO_PADDING
            )
            info.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC), Typeface.ITALIC)
            return info
        }

    private fun parseCallIntent() {
        val extras = getIntent().getExtras()
        if (extras != null) {
            try {
                if (extras.containsKey(IpackKeys.Extras.GRID_BACK_COLOUR)) {
                    this.gridBackColour = extras.getInt(IpackKeys.Extras.GRID_BACK_COLOUR)
                }
                if (extras.containsKey(IpackKeys.Extras.CELL_SIZE)) {
                    this.cellSize = extras.getInt(IpackKeys.Extras.CELL_SIZE)
                }
                if (extras.containsKey(IpackKeys.Extras.ICON_DISPLAY_SIZE)) {
                    this.cellSize = extras.getInt(IpackKeys.Extras.ICON_DISPLAY_SIZE)
                }
            } catch (e: Exception) {
                Log.d(IpackContent.LABEL, "exception parsing intent: " + e.toString())
            }
        }
    }

    /* access modifiers changed from: private */
    fun setGridAdapter() {
        this.grid!!.setAdapter(object : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val i: ImageView
                if (convertView == null) {
                    i = ImageView(this@IpackIconSelect)
                    i.setScaleType(ImageView.ScaleType.FIT_CENTER)
                    i.setLayoutParams(
                        AbsListView.LayoutParams(
                            this@IpackIconSelect.cellSize,
                            this@IpackIconSelect.cellSize
                        )
                    )
                    val padding =
                        (this@IpackIconSelect.cellSize - this@IpackIconSelect.iconSize) / 2
                    i.setPadding(padding, padding, padding, padding)
                } else {
                    i = convertView as ImageView
                }
                i.setImageResource((this@IpackIconSelect.sortedIDs.get(position) as Int))
                return i
            }

            override fun getCount(): Int {
                return this@IpackIconSelect.sortedIDs.size
            }

            override fun getItem(position: Int): Any? {
                return this@IpackIconSelect.sortedIDs.get(position)
            }

            override fun getItemId(position: Int): Long {
                return position.toLong()
            }
        })
    }

    val oneIconSize: Point?
        get() {
            val d = getResources().getDrawable(
                this.icons.getInt(
                    this.icons.keySet().iterator().next()
                )
            )
            if (d == null) {
                return null
            }
            val density = getResources().getDisplayMetrics().density
            return Point(
                ((d.getIntrinsicWidth().toFloat()) / density).toInt(),
                ((d.getIntrinsicHeight().toFloat()) / density).toInt()
            )
        }

    fun sortIcons(allSameSize: Boolean) {
        val res = getResources()
        val sizes: MutableMap<Int?, Int?> = HashMap<Int?, Int?>()
        val names: MutableMap<Int?, String?> = HashMap<Int?, String?>()
        for (name in this.icons.keySet()) {
            val id = this.icons.getInt(name)
            if (!allSameSize) {
                try {
                    val d = res.getDrawable(id)
                    sizes.put(id, d.getIntrinsicHeight() * d.getIntrinsicWidth())
                } catch (e: Resources.NotFoundException) {
                    Log.e(IpackContent.LABEL, "sort icons: resource not found: " + name)
                }
            }
            this.sortedIDs.add(id)
            names.put(id, name)
        }
        Collections.sort<Int?>(this.sortedIDs, object : Comparator<Int?> {
            override fun compare(x: Int?, y: Int?): Int {
                if (allSameSize) {
                    return (names.get(y) as String).compareTo(names.get(x)!!, ignoreCase = true)
                }
                val xsize = sizes.get(x)
                val ysize = sizes.get(y)
                if (xsize == ysize) {
                    return (names.get(y) as String).compareTo(names.get(x)!!, ignoreCase = true)
                }
                return ysize!!.compareTo(xsize!!)
            }
        })
    }

    private fun setupGrid() {
        this.grid = GridView(this)
        this.grid!!.setBackgroundColor(this.gridBackColour)
        this.grid!!.setGravity(17)
        this.grid!!.setPadding(GRID_PADDING, GRID_PADDING, GRID_PADDING, GRID_PADDING)
        this.grid!!.setNumColumns(getResources().getDisplayMetrics().widthPixels / this.cellSize)
        this.grid!!.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                val selID = (this@IpackIconSelect.sortedIDs.get(pos) as Int)
                var selName: String? = null
                val resourceName = this@IpackIconSelect.getResources().getResourceEntryName(selID)
                val `i$`: MutableIterator<*> = this@IpackIconSelect.icons.keySet().iterator()
                while (true) {
                    if (!`i$`.hasNext()) {
                        break
                    }
                    val name = `i$`.next() as String?
                    if (this@IpackIconSelect.icons.getInt(name) == selID) {
                        selName = name
                        break
                    }
                }
                val result = Intent()
                result.setData(Uri.parse(IpackKeys.ANDROID_RESOURCE_PREFIX + this@IpackIconSelect.getPackageName() + "/" + resourceName))
                if (selName != null) {
                    result.putExtra(IpackKeys.Extras.ICON_LABEL, selName)
                }
                result.putExtra(IpackKeys.Extras.ICON_NAME, resourceName)
                result.putExtra(IpackKeys.Extras.ICON_ID, selID)
                this@IpackIconSelect.setResult(-1, result)
                this@IpackIconSelect.finish()
            }
        })
    }

    inner class IconSortTask : AsyncTask<Boolean?, Int?, Boolean?>() {
        public override fun doInBackground(vararg args: Boolean?): Boolean {
            this@IpackIconSelect.sortIcons(args[0]!!)
            return true
        }

        /* access modifiers changed from: protected */
        public override fun onPostExecute(result: Boolean?) {
            try {
                this@IpackIconSelect.sortTask = null
                val unused = this@IpackIconSelect.sortTask
                this@IpackIconSelect.setGridAdapter()
                this@IpackIconSelect.setProgressBarIndeterminateVisibility(false)
            } catch (e: Exception) {
            }
        }
    }

    companion object {
        private const val DEFAULT_CELL_WIDTH = 90
        private const val DEFAULT_ICON_SIZE = 60
        private const val GRID_PADDING = 10
        private const val INFO_PADDING = 5
    }
}
