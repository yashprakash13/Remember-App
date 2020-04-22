package save.newwords.vocab.remember.ui

import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import save.newwords.vocab.remember.R
import save.newwords.vocab.remember.common.recordPermission
import save.newwords.vocab.remember.common.writePermission

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this,
                recordPermission
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                writePermission
            ) == PackageManager.PERMISSION_GRANTED) {
            //Permission is granted, do nothing.
        }else{
            //Permission not granted, show dialog
            showPermissionsDialog()
        }
    }

    private fun showPermissionsDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.permissions_req))
        builder.setMessage(getString(R.string.permissions_req_desc))
        builder.setIcon(R.drawable.ic_verified_user_black_24dp)
        builder.background = getDrawable(R.drawable.round_corner_white_back)
        builder.setPositiveButton(getString(R.string.okay_label)) { _, i ->
            // ask for permission
            ActivityCompat.requestPermissions(this,
                arrayOf(recordPermission, writePermission),
                save.newwords.vocab.remember.common.PERMISSION_CODE)
        }
        builder.setCancelable(false)
        builder.show()
    }
}
