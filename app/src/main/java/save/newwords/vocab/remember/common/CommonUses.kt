package save.newwords.vocab.remember.common

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout


/**
 * helper function to display toast in a fragment
 * @param value: the string to display
 */
internal fun Fragment.makeToast(value: String) {
    Toast.makeText(activity, value, Toast.LENGTH_SHORT).show()
}

internal fun Fragment.showSnackbar(message: String){
    Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
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
/**
 * ext func to show a view
 */
fun View.show() {
    visibility = View.VISIBLE
}

/**
* ext func to not show a view
*/
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

/**
 * ext function to get instance of shared prefs
 */
fun Activity.getSharedPrefsFor(key: Int) : SharedPreferences{
    return getSharedPreferences(getString(key), Context.MODE_PRIVATE)
}

/**
 *  ext function to get instance of shared prefs
 */
fun Context.getSharedPrefsFor(key: Int) : SharedPreferences{
    return getSharedPreferences(getString(key), Context.MODE_PRIVATE)
}

/**
 * ext function to change shared prefs
 */
fun Activity.changeSharedPrefTo(prefLayoutManager: SharedPreferences, key: Int, whatToPut: Any?, type: Int ){
    //0 means shared pref is of int type, so perform int operation
    if (type == 0){
        with(prefLayoutManager.edit()){
            putInt(getString(key), whatToPut as Int)
            commit()
        }
    }else if (type == 1){
        //1 means shared pref is of string type
        with(prefLayoutManager.edit()) {
            putString(getString(key), whatToPut as String?)
            commit()
        }
    }else if (type == 2){
        with(prefLayoutManager.edit()) {
            putBoolean(getString(key), whatToPut as Boolean)
            commit()
        }
    }
}

internal fun TextInputLayout.isValid() : Boolean{
    val string = editText!!.text.toString().trim()

    if (!TextUtils.isEmpty(editText!!.text) && string != ""){
        return true
    }
    return false
}

internal fun TextInputLayout.getString() : String{
    return editText!!.text.toString().trim()
}

/**
 * to return formatted time for UI updation
 * @param hours from time picker
 * @param minutes from time picker
 */
fun getFormattedTimeForUI(hours: Int, minutes: Int): String{
    var isAM = true
    var hrs: Int = hours

    if (hours > 12){
        isAM = false
        hrs -= 12
    }

    val time : String
    time = if (isAM) {
        if (minutes < 10){
            "$hrs:0$minutes AM"
        }else{
            "$hrs:$minutes AM"
        }
    }else{
        if (minutes < 10){
            "$hrs:0$minutes PM"
        }else{
            "$hrs:$minutes PM"
        }
    }
    return time
}




