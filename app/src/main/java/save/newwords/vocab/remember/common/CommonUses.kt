package save.newwords.vocab.remember.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment


/**
 * helper function to display toast in a fragment
 */
internal fun Fragment.makeToast(value: String) {
    Toast.makeText(activity, value, Toast.LENGTH_SHORT).show()
}

/**
 * extension function to
 * get number of columns for grid layout
 * @return Int = number of cols
 */
fun Context.getNumColumns() : Int{
    val displayMetrics = resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density

    return (screenWidthDp / 197).toInt()
}

/**
 * to toggle visibility of any view
 */
fun View.toggleVisibility() {
    visibility = if(visibility == View.VISIBLE) {
        View.GONE
    }else{
        View.VISIBLE
    }
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.dontShow() {
    visibility = View.GONE
}

fun ImageButton.changeIconTo(id: Int){
    setImageResource(id)
}

fun View.getViewVisibility() : Int {
    return visibility
}

/**
 * helper function to convert a drawable xml to bitmap image
 */
fun drawableToBitmap(drawable: Drawable): Bitmap? {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
    drawable.draw(canvas)
    return bitmap
}






