package ani.saikou.manga.source

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * Fixes the MangaReader images by cropping and moving around chunks of the image
 * Made by LagradOst
 * */
class MangaReaderTransformation : BitmapTransformation() {
    private val id = this.javaClass.name
    private val idBytes = id.toByteArray(Charset.defaultCharset())

    override fun transform(
        pool: BitmapPool, toTransform: Bitmap,
        outWidth: Int, outHeight: Int
    ): Bitmap {
        val width = 200
        val height = 200
        val fullImage = Bitmap.createScaledBitmap(toTransform, totalWidth, totalHeight, true)

        val diffList = listOf(
            Point(2, 1) to Point(0, 0),
            Point(0, 0) to Point(1, 0),
            Point(1, 1) to Point(2, 0),
            Point(3, 0) to Point(3, 0),

            Point(2, 4) to Point(0, 1),
            Point(2, 3) to Point(1, 1),
            Point(1, 3) to Point(2, 1),
            Point(3, 4) to Point(3, 1),

            Point(0, 4) to Point(0, 2),
            Point(1, 4) to Point(1, 2),
            Point(0, 1) to Point(2, 2),
            Point(3, 3) to Point(3, 2),

            Point(2, 2) to Point(0, 3),
            Point(1, 0) to Point(1, 3),
            Point(0, 3) to Point(2, 3),
            Point(3, 1) to Point(3, 3),

            Point(0, 2) to Point(0, 4),
            Point(2, 0) to Point(1, 4),
            Point(1, 2) to Point(2, 4),
            Point(3, 2) to Point(3, 4),

            Point(0, 5) to Point(0, 5),
            Point(2, 5) to Point(1, 5),
            Point(1, 5) to Point(2, 5),
            Point(3, 5) to Point(3, 5),
        )

        val image = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        diffList.forEach {
            val diffWidth =
                if (it.first.x >= 3) (totalWidth % width) else width
            val diffHeight =
                if (it.first.y >= 5) (totalHeight % height) else height

            val part = Bitmap.createBitmap(
                fullImage,
                it.first.x * width,
                it.first.y * height,
                diffWidth,
                diffHeight
            )
            canvas.drawBitmap(
                part,
                (it.second.x * width).toFloat(),
                (it.second.y * height).toFloat(),
                null
            )
        }

        return image
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(idBytes)
    }

    override fun equals(other: Any?): Boolean {
        return other is MangaReaderTransformation
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        const val totalHeight = 1145
        const val totalWidth = 784
        private val transformation = MangaReaderTransformation()
        fun <T> RequestBuilder<T>.transformMangaReader(): RequestBuilder<T> {
            return this.override(totalWidth, totalHeight)
                .transform(transformation)
        }
    }
}
