package com.GroceerCart.sa.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.GroceerCart.sa.R
import com.GroceerCart.sa.listener.GroceerBaseActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException

class GroceerBarcodeScannerActivity : GroceerBaseActivity() {
    var surfaceView: SurfaceView? = null
    var txtBarcodeValue: TextView? = null
    private var barcodeDetector: BarcodeDetector? = null
    private var cameraSource: CameraSource? = null
    private val REQUEST_CAMERA_PERMISSION = 201
    var btnAction: Button? = null
    var intentData = ""
    var isEmail = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groceer_barcode_scanner)

        initViews()
    }

    private fun initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue)
        btnAction = findViewById(R.id.btnAction)
        surfaceView = findViewById(R.id.surfaceView)
    }

    override fun onResume() {
        super.onResume()
        initialiseDetectorsAndSources()
    }

    private fun initialiseDetectorsAndSources() {
        Toast.makeText(applicationContext, "Barcode scanner started", Toast.LENGTH_SHORT)
            .show()

        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        surfaceView!!.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                var mCameraSource: CameraSource? = cameraSource as CameraSource?
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this@GroceerBarcodeScannerActivity,
                            Manifest.permission.CAMERA
                        ) === PackageManager.PERMISSION_GRANTED
                    ) {
                        mCameraSource?.start(surfaceView!!.holder)
                    } else {
                        ActivityCompat.requestPermissions(
                            this@GroceerBarcodeScannerActivity,
                            arrayOf(Manifest.permission.CAMERA),
                            REQUEST_CAMERA_PERMISSION
                        )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                var mCameraSource: CameraSource? = cameraSource as CameraSource?
                mCameraSource?.stop()
            }
        })
         var mBarcodeDetector: BarcodeDetector? = barcodeDetector

        mBarcodeDetector?.setProcessor(object :
            Detector.Processor<Barcode> {
            override fun release() {
               /* Toast.makeText(
                    applicationContext,
                    "To prevent memory leaks barcode scanner has been stopped",
                    Toast.LENGTH_SHORT
                ).show()*/
            }

            override fun receiveDetections(detections: Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    txtBarcodeValue!!.post {
                        if (barcodes.valueAt(0).email != null) {
                            txtBarcodeValue!!.removeCallbacks(null)
                            intentData = barcodes.valueAt(0).email.address
                            txtBarcodeValue!!.text = intentData
                            isEmail = true
                            btnAction!!.text = "ADD CONTENT TO THE MAIL"
                        } else {
                            isEmail = false
                            btnAction!!.text = "LAUNCH URL"
                            intentData = barcodes.valueAt(0).displayValue
                            txtBarcodeValue!!.text = intentData
                        }
                        setResult(501, Intent().putExtra("barCode",intentData))
                        finish()
                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        cameraSource?.release()
    }
}