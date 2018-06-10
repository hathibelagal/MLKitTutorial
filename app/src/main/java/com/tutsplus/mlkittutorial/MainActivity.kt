package com.tutsplus.mlkittutorial

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.EditorInfo
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.ctx

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image_url_field.setText("https://upload.wikimedia.org/wikipedia/commons/thumb/8/84/Expedition_50_crew_portrait_%282%29.jpg/640px-Expedition_50_crew_portrait_%282%29.jpg")

        image_url_field.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                Picasso.with(ctx).load(image_url_field.text.toString())
                        .into(image_holder)
                true
            }
            false
        }
    }

    fun recognizeText(v: View) {
        val textImage = FirebaseVisionImage.fromBitmap(
                (image_holder.drawable as BitmapDrawable).bitmap
        )
        val detector = FirebaseVision.getInstance().visionTextDetector
        detector.detectInImage(textImage)
                .addOnCompleteListener {
                    var detectedText = ""
                    it.result.blocks.forEach {
                        detectedText += it.text + "\n"
                    }
                    runOnUiThread {
                        alert(detectedText, "Text").show()
                    }
                    detector.close()
                }
    }

    fun detectFaces(v: View) {
        val detector = FirebaseVision.getInstance().visionFaceDetector

        detector.detectInImage(FirebaseVisionImage.fromBitmap(
                    (image_holder.drawable as BitmapDrawable).bitmap
                )).addOnCompleteListener {
                    var markedBitmap =
                            (image_holder.drawable as BitmapDrawable)
                                    .bitmap
                                    .copy(Bitmap.Config.ARGB_8888, true)

                    val canvas = Canvas(markedBitmap)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

                    // Set paint color to something slightly transparent
                    paint.color = Color.parseColor("#99003399")
                    it.result.forEach {
                        canvas.drawRect(it.boundingBox, paint)
                    }
                    runOnUiThread {
                        image_holder.setImageBitmap(markedBitmap)
                    }
                    detector.close()
                }
    }

    fun generateLabels(v: View) {
        val detector = FirebaseVision.getInstance().visionCloudLabelDetector
        detector.detectInImage(FirebaseVisionImage.fromBitmap(
                    (image_holder.drawable as BitmapDrawable).bitmap
                )).addOnCompleteListener {
                    var output = ""
                    it.result.forEach {
                        if(it.confidence > 0.7)
                            output += it.label + "\n"
                    }
                    runOnUiThread {
                        alert(output, "Labels").show()
                    }
                    detector.close()
                }
    }
}
