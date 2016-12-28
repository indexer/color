package com.indexer.zeekwat

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity.START
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import org.jetbrains.anko.*


class MainActivity : AppCompatActivity() {
    var mBackGroundColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            val mCoverImage =
                    imageView(android.R.drawable.sym_def_app_icon) {
                        setImageResource(R.mipmap.ic_launcher)
                        scaleType = ImageView.ScaleType.FIT_XY
                        padding = 16
                    }.lparams {
                        width = matchParent
                        height = 600
                        gravity = START
                    }


            val mTitleText = textView {
                padding = 16
                text = "Cannon"
                textSize = 30F
                typeface = Typeface.DEFAULT_BOLD
            }.lparams {
                margin = 16
            }

            val mOverView = textView {
                text = "Overview"
                padding = 16
                textSize = 18F
                typeface = Typeface.DEFAULT_BOLD
            }.lparams {
                margin = 16
            }


            val mDetailText = textView {
                padding = 16
                text = "One of the old-line global leaders in the photo industry," +
                        " Canon cameras cover the range from entry-level point & shoot models to" +
                        " high-end professional SLRs at the very top of the market." +
                        "Canon cameras are divided into two broad product lines, " +
                        "Canon EOS for their SLR models, and " +
                        "Canon PowerShot for their point & shoot designs. The links below take you to " +
                        "dedicated pages for each category," +
                        " with more information on the models that make up each Canon camera product line."
                textSize = 14F
                typeface = Typeface.SANS_SERIF
            }.lparams {
                leftMargin = 16
                rightMargin = 16
            }


            Glide.with(context)
                    .load("https://s-media-cache-ak0.pinimg.com/originals/51/f3/aa/51f3aad3e922e8d5fdab209c778672c8.jpg")
                    .asBitmap()
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, glideAnimation: GlideAnimation<in Bitmap>) {
                            val mColor = ColorUtils(resource)
                            mBackGroundColor = mColor.backgroundColor
                            mCoverImage.setImageBitmap(resource)
                            mTitleText.textColor = mColor.primaryColor
                            mOverView.textColor = mColor.secondaryColor
                            mDetailText.textColor = Color.parseColor("#FFFFFF")
                            supportActionBar?.setBackgroundDrawable(ColorDrawable(mBackGroundColor))
                            setBackgroundColor(mBackGroundColor)
                        }
                    })

        }

    }
}
