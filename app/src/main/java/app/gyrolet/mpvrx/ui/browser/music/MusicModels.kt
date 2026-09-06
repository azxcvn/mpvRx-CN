/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */

package app.gyrolet.mpvrx.ui.browser.music

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
enum class MusicTab(val title: String) {
  SONGS("歌曲"),
  ALBUMS("专辑"),
  ARTISTS("艺术家"),
  PLAYLISTS("播放列表"),
  FOLDERS("文件夹");

  companion object {
    val defaultTabs = entries.toList()
  }
}

@Immutable
data class MusicSong(
  val id: Long,
  val title: String,
  val artist: String,
  val album: String,
  val albumId: Long,
  val durationMs: Long,
  val path: String,
  val uri: Uri,
  val dateAdded: Long,
  val trackNumber: Int = 0,
  val year: Int = 0,
  val albumArtUri: Uri? = null,
  val size: Long = 0L
)

@Immutable
data class MusicAlbum(
  val id: Long,
  val title: String,
  val artist: String,
  val songCount: Int,
  val year: Int = 0,
  val albumArtUri: Uri? = null
)

@Immutable
data class MusicArtist(
  val id: Long,
  val name: String,
  val songCount: Int,
  val albumCount: Int = 0
)

@Immutable
enum class MusicSortField(val displayName: String) {
  TITLE("标题"),
  ARTIST("艺术家"),
  ALBUM("专辑"),
  DURATION("时长"),
  DATE_ADDED("添加日期"),
  TRACK_COUNT("曲目数"),
  YEAR("年份")
}

@Immutable
enum class MusicSortOrder {
  ASCENDING,
  DESCENDING
}

@Immutable
enum class MusicViewMode {
  LIST,
  GRID
}
