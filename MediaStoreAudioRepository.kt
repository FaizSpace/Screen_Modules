package screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.model.MediaStoreAudio
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class MediaStoreAudioRepository @Inject constructor(
    private val context: Context, private val ioDispatcher: CoroutineDispatcher
) {
    @SuppressLint("Range")
    suspend fun getAllAudio(): Flow<List<screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.model.MediaStoreAudio>> = flow {
        val audio = getAudioFromMediaStore()

        val mediaStoreAudios = mutableListOf<screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.model.MediaStoreAudio>()
        audio.use { cursor ->
            while (cursor?.moveToNext() == true) {

                val mediaStoreAudio =
                    screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.model.MediaStoreAudio(
                        id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                        artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                        album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)),
                        contentUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                        ),
                        filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        getGenreNameForAudio(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))),
                        duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)),
                        albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                        dateAdded = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)),
                        dateModified = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)),
                        trackNumber = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)),
                        year = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)),
                        mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)),
                        size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE))
                    )
                mediaStoreAudios.add(mediaStoreAudio)
            }
        }

        emit(mediaStoreAudios)
    }.flowOn(ioDispatcher)

    private fun getAudioFromMediaStore(): Cursor? {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.MIME_TYPE} IN (?, ?, ?)"
        val selectionArgs = arrayOf("audio/mpeg", "audio/ogg", "audio/wav")

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        return context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }

    @SuppressLint("Range")
    private fun getGenreNameForAudio(audioId: Long): String? {
        val projection = arrayOf(
            MediaStore.Audio.Genres.NAME
        )
        val uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", audioId.toInt())
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor.use {
            if (it != null && it.moveToFirst()) {
                return it.getString(it.getColumnIndex(MediaStore.Audio.Genres.NAME))
            }
        }
        return null
    }

}

