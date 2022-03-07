package ani.saikou.manga.source.parsers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Point
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * Fixes the MangaReader images by cropping and moving around chunks of the image
 * Made by LagradOst
 * */
class MangaReaderToTransformation : Transformation<File> {
    private val id = this.javaClass.name
    private val idBytes = id.toByteArray(Charset.defaultCharset())

//    fun seedRand(start: Int, stop: Int){
//        return floor(stop - start + 1) + start
//    }

    override fun transform(
        context: Context,
        resource: Resource<File>,
        outWidth: Int,
        outHeight: Int
    ): Resource<File> {
        val width = 200
        val height = 200

        val file = resource.get()
        val bitmap = BitmapFactory.decodeFile(file.path)
        val fullImage = Bitmap.createScaledBitmap(bitmap, totalWidth, totalHeight, true)

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
        val newFile = File(context.cacheDir, file.name)
        newFile.createNewFile()
        val bos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 0, bos)
        val bitmapData = bos.toByteArray()
        newFile.writeBytes(bitmapData)
        return object : Resource<File> {
            override fun getResourceClass(): Class<File> {
                return newFile.javaClass
            }

            override fun get(): File {
                return newFile
            }

            override fun getSize(): Int {
                return newFile.length().toInt()
            }

            override fun recycle() {
                newFile.delete()
            }
        }
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(idBytes)
    }

    override fun equals(other: Any?): Boolean {
        return other is MangaReaderToTransformation
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object {
        const val totalHeight = 1145
        const val totalWidth = 784
        private val transformation = MangaReaderToTransformation()

        fun <T> RequestBuilder<T>.transformMangaReader(): RequestBuilder<T> {
            return this.override(totalWidth, totalHeight)
                .transform(File("").javaClass, transformation)
        }
    }
}
