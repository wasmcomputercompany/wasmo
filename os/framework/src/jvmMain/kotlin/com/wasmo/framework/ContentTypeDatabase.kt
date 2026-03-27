package com.wasmo.framework

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

interface ContentTypeDatabase {
  operator fun get(fileName: String): MediaType?

  companion object
}

/**
 * Use Mozilla's nicely curated set of content types.
 *
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types/Common_types
 */
private object MdnContentTypeDatabase : ContentTypeDatabase {
  private val map = mapOf(
    "3g2" to "video/3gpp2".toMediaType(),
    "3gp" to "video/3gpp".toMediaType(),
    "7z" to "application/x-7z-compressed".toMediaType(),
    "aac" to "audio/aac".toMediaType(),
    "abw" to "application/x-abiword".toMediaType(),
    "apng" to "image/apng".toMediaType(),
    "arc" to "application/x-freearc".toMediaType(),
    "avi" to "video/x-msvideo".toMediaType(),
    "avif" to "image/avif".toMediaType(),
    "azw" to "application/vnd.amazon.ebook".toMediaType(),
    "bin" to "application/octet-stream".toMediaType(),
    "bmp" to "image/bmp".toMediaType(),
    "bz" to "application/x-bzip".toMediaType(),
    "bz2" to "application/x-bzip2".toMediaType(),
    "cda" to "application/x-cdf".toMediaType(),
    "csh" to "application/x-csh".toMediaType(),
    "css" to "text/css".toMediaType(),
    "csv" to "text/csv".toMediaType(),
    "doc" to "application/msword".toMediaType(),
    "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaType(),
    "eot" to "application/vnd.ms-fontobject".toMediaType(),
    "epub" to "application/epub+zip".toMediaType(),
    "gif" to "image/gif".toMediaType(),
    "gz" to "application/gzip".toMediaType(),
    "htm" to "text/html".toMediaType(),
    "html" to "text/html".toMediaType(),
    "ico" to "image/vnd.microsoft.icon".toMediaType(),
    "ics" to "text/calendar".toMediaType(),
    "jar" to "application/java-archive".toMediaType(),
    "jpeg" to "image/jpeg".toMediaType(),
    "jpg" to "image/jpeg".toMediaType(),
    "js" to "text/javascript".toMediaType(),
    "json" to "application/json".toMediaType(),
    "jsonld" to "application/ld+json".toMediaType(),
    "md" to "text/markdown".toMediaType(),
    "mid" to "audio/midi".toMediaType(),
    "midi" to "audio/midi".toMediaType(),
    "mjs" to "text/javascript".toMediaType(),
    "mp3" to "audio/mpeg".toMediaType(),
    "mp4" to "video/mp4".toMediaType(),
    "mpeg" to "video/mpeg".toMediaType(),
    "mpkg" to "application/vnd.apple.installer+xml".toMediaType(),
    "odp" to "application/vnd.oasis.opendocument.presentation".toMediaType(),
    "ods" to "application/vnd.oasis.opendocument.spreadsheet".toMediaType(),
    "odt" to "application/vnd.oasis.opendocument.text".toMediaType(),
    "oga" to "audio/ogg".toMediaType(),
    "ogv" to "video/ogg".toMediaType(),
    "ogx" to "application/ogg".toMediaType(),
    "opus" to "audio/ogg".toMediaType(),
    "otf" to "font/otf".toMediaType(),
    "pdf" to "application/pdf".toMediaType(),
    "php" to "application/x-httpd-php".toMediaType(),
    "png" to "image/png".toMediaType(),
    "ppt" to "application/vnd.ms-powerpoint".toMediaType(),
    "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation".toMediaType(),
    "rar" to "application/vnd.rar".toMediaType(),
    "rtf" to "application/rtf".toMediaType(),
    "sh" to "application/x-sh".toMediaType(),
    "svg" to "image/svg+xml".toMediaType(),
    "tar" to "application/x-tar".toMediaType(),
    "tif" to "image/tiff".toMediaType(),
    "tiff" to "image/tiff".toMediaType(),
    "ts" to "video/mp2t".toMediaType(),
    "ttf" to "font/ttf".toMediaType(),
    "txt" to "text/plain".toMediaType(),
    "vsd" to "application/vnd.visio".toMediaType(),
    "wav" to "audio/wav".toMediaType(),
    "weba" to "audio/webm".toMediaType(),
    "webm" to "video/webm".toMediaType(),
    "webmanifest" to "application/manifest+json".toMediaType(),
    "webp" to "image/webp".toMediaType(),
    "woff" to "font/woff".toMediaType(),
    "woff2" to "font/woff2".toMediaType(),
    "xhtml" to "application/xhtml+xml".toMediaType(),
    "xls" to "application/vnd.ms-excel".toMediaType(),
    "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaType(),
    "xml" to "application/xml".toMediaType(),
    "xul" to "application/vnd.mozilla.xul+xml".toMediaType(),
    "zip" to "application/zip".toMediaType(),
  )

  override fun get(fileName: String): MediaType? {
    val lastDot = fileName.lastIndexOf('.')
    if (lastDot == -1) return null
    val extension = fileName.substring(lastDot + 1).lowercase()
    return map[extension]
  }
}

val ContentTypeDatabase.Companion.MDN: ContentTypeDatabase
  get() = MdnContentTypeDatabase
