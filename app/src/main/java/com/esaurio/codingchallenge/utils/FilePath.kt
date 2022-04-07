package com.esaurio.codingchallenge.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.*

object FilePath {

    val imageExts: ArrayList<String>
        get() {
            val imageTypes = arrayOf("png", "jpg", "jpeg", "bmp", "gif")
            val imageExts = imageTypes.indices.mapTo(ArrayList()) { imageTypes[it] }
            return imageExts
        }
    val videoExts: ArrayList<String>
        get() {
            val videoTypes = arrayOf("mpeg", "mp4", "gif", "wmv", "mov", "mpg", "3gp", "flv")
            val videoExts = videoTypes.indices.mapTo(ArrayList()) { videoTypes[it] }
            return videoExts
        }
    val docExts: ArrayList<String>
        get() {
            val docTypes = arrayOf("doc", "docx", "pdf", "txt")
            val docExts = docTypes.indices.mapTo(ArrayList()) { docTypes[it] }
            return docExts
        }

    fun getPath(context: Context, uri: Uri): String? {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                val externalFilesDir = context.getExternalFilesDir(null)
                return if ("primary".equals(type, ignoreCase = true) && externalFilesDir != null && File(externalFilesDir.path + "/" + split[1]).exists()) {
                    externalFilesDir.path + "/" + split[1]
                }else{
                    downloadUriToCache(context,uri)
                }
            } else if (isDownloadsDocument(uri)) {
                // DownloadsProvider
                val decodedURI = Uri.decode(uri.toString())
                if (decodedURI.contains("raw:")) {
                    return decodedURI.substring(decodedURI.indexOf("raw:") + 4)
                }
                val idStr = DocumentsContract.getDocumentId(Uri.parse(decodedURI))
                val contentUriPrefixesToTry = arrayOf(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads",
                    "content://downloads/all_downloads"
                )
                val id = idStr.toLongOrNull()
                if (id!=null) {
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        try {
                            val contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), id)
                            val path = getDataColumn(context, contentUri, null, null)
                            if (path != null) {
                                return path
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
                // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                return downloadUriToCache(context,uri)
/*
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!)
                return getDataColumn(context, contentUri, null, null)
*/
            } else if (isMediaDocument(uri)) {
                // MediaProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }else{
                return downloadUriToCache(context, uri)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            var path = getDataColumn(context, uri, null, null)
            if (path==null)
                path = downloadUriToCache(context,uri)
            return path
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)
        return null
    }

    fun downloadUriToCache(context: Context, uri: Uri) : String?{
        val fileName: String? = getFileName(context,uri)
        val cacheDir: File = getDocumentCacheDir(context)
        val file: File? = generateFileName(fileName, cacheDir)
        var destinationPath: String? = null
        if (file != null) {
            destinationPath = file.absolutePath
            saveFileFromUri(context, uri, destinationPath)
        }
        return destinationPath
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                      selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getFileName(context: Context?, uri: Uri): String? {
        val mimeType = context?.contentResolver?.getType(uri)
        var filename: String? = null
        if (mimeType == null && context != null) {
            val path = getPath(context, uri)
            filename = if (path == null) {
                getName(uri.toString())
            } else {
                val file = File(path)
                file.name
            }
        } else {
            val returnCursor = context?.contentResolver?.query(uri, null,
                null, null, null)
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                filename = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
        }
        return filename
    }

    fun getName(filename: String?): String? {
        if (filename == null) {
            return null
        }
        val index = filename.lastIndexOf('/')
        return filename.substring(index + 1)
    }

    fun getDocumentCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "documents")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun generateFileName(name: String?, directory: File?): File? {
        var mName = name ?: return null
        var file = File(directory, mName)
        if (file.exists()) {
            var fileName = mName
            var extension = ""
            val dotIndex = mName.lastIndexOf('.')
            if (dotIndex > 0) {
                fileName = mName.substring(0, dotIndex)
                extension = mName.substring(dotIndex)
            }
            var index = 0
            while (file.exists()) {
                index++
                mName = "$fileName($index)$extension"
                file = File(directory, mName)
            }
        }
        try {
            if (!file.createNewFile()) {
                return null
            }
        } catch (e: IOException) {
            return null
        }
        return file
    }

    private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
        var `is`: InputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
            if (`is` != null){
                bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
                val buf = ByteArray(1024)
                `is`.read(buf)
                do {
                    bos.write(buf)
                } while (`is`.read(buf) != -1)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}