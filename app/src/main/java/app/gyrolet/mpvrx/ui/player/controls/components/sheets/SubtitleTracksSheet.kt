/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.player.controls.components.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.gyrolet.mpvrx.R
import app.gyrolet.mpvrx.presentation.components.PlayerSheet
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons
import app.gyrolet.mpvrx.ui.player.TrackNode
import app.gyrolet.mpvrx.ui.theme.spacing
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

sealed class SubtitleItem {
  data class Track(
    val node: TrackNode,
  ) : SubtitleItem()

  data class Header(
    val title: String,
  ) : SubtitleItem()

  object Divider : SubtitleItem()

  object Off : SubtitleItem()
}

@Composable
fun SubtitlesSheet(
  tracks: ImmutableList<TrackNode>,
  onToggleSubtitle: (Int) -> Unit,
  isSubtitleSelected: (Int) -> Boolean,
  subtitleSelectionIndicator: (Int) -> String?,
  onAddSubtitle: () -> Unit,
  onOpenSubtitleSettings: () -> Unit,
  onOpenSubtitleDelay: () -> Unit,
  onRemoveSubtitle: (Int) -> Unit,
  onOpenOnlineSearch: () -> Unit,
  onDismissRequest: () -> Unit,
  onTranslateSubtitle: (TrackNode, String) -> Unit,
  onGenerateSubtitle: () -> Unit,
  onCancelTranslation: () -> Unit,
  isTranslating: Boolean,
  translationProgress: Float,
  translationStatus: String,
  translationEnabled: Boolean,
  isGeneratingSubtitles: Boolean,
  subtitleGenerationProgress: Float,
  subtitleGenerationStatus: String,
  translatingTrackId: Int? = null,
  translatingTrackName: String = "",
  autoTranslateLanguages: String = "",
  aiEnabled: Boolean = true,
  realtimeSubsEnabled: Boolean = true,
  subtitlesOff: Boolean = false,
  onDisableSubtitles: () -> Unit = {},
  delayControlEnabled: Boolean = true,
  modifier: Modifier = Modifier,
) {
  val items =
    remember(tracks, subtitlesOff) {
      val list = mutableListOf<SubtitleItem>()
      list.add(SubtitleItem.Off)
      val internal = tracks.filter { it.external != true }
      val external = tracks.filter { it.external == true }

      if (internal.isNotEmpty() || external.isNotEmpty()) {
        list.add(SubtitleItem.Header(if (internal.isNotEmpty()) "内嵌字幕" else "本地字幕"))
        list.addAll(internal.map { SubtitleItem.Track(it) })
        if (internal.isNotEmpty() && external.isNotEmpty()) {
          list.add(SubtitleItem.Header("外部字幕"))
        }
        list.addAll(external.map { SubtitleItem.Track(it) })
      }

      list.toImmutableList()
    }

  val configuredLanguages =
    remember(autoTranslateLanguages) {
      autoTranslateLanguages.split(",").filter { it.isNotBlank() }
    }

  val allLanguages =
    remember {
      listOf(
        "南非荷兰语",
        "阿拉伯语",
        "孟加拉语",
        "保加利亚语",
        "加泰罗尼亚语",
        "简体中文",
        "繁体中文",
        "克罗地亚语",
        "捷克语",
        "丹麦语",
        "荷兰语",
        "英语",
        "爱沙尼亚语",
        "芬兰语",
        "法语",
        "德语",
        "希腊语",
        "古吉拉特语",
        "希伯来语",
        "印地语",
        "匈牙利语",
        "印度尼西亚语",
        "意大利语",
        "日语",
        "卡纳达语",
        "韩语",
        "拉脱维亚语",
        "立陶宛语",
        "马来语",
        "马拉雅拉姆语",
        "马拉地语",
        "挪威语",
        "波斯语",
        "波兰语",
        "葡萄牙语",
        "旁遮普语",
        "罗马尼亚语",
        "俄语",
        "塞尔维亚语",
        "斯洛伐克语",
        "斯洛文尼亚语",
        "西班牙语",
        "斯瓦希里语",
        "瑞典语",
        "泰米尔语",
        "泰卢固语",
        "泰语",
        "土耳其语",
        "乌克兰语",
        "乌尔都语",
        "越南语",
      )
    }

  val codeToName =
    remember {
      mapOf(
        "en" to "英语",
        "es" to "西班牙语",
        "fr" to "法语",
        "de" to "德语",
        "it" to "意大利语",
        "pt" to "葡萄牙语",
        "ru" to "俄语",
        "zh" to "简体中文",
        "ja" to "日语",
        "ko" to "韩语",
        "ar" to "阿拉伯语",
        "hi" to "印地语",
        "bn" to "孟加拉语",
        "vi" to "越南语",
        "te" to "泰卢固语",
        "ta" to "泰米尔语",
        "ur" to "乌尔都语",
        "tr" to "土耳其语",
        "pl" to "波兰语",
        "uk" to "乌克兰语",
        "nl" to "荷兰语",
        "el" to "希腊语",
        "hu" to "匈牙利语",
        "sv" to "瑞典语",
        "cs" to "捷克语",
        "ro" to "罗马尼亚语",
        "da" to "丹麦语",
        "fi" to "芬兰语",
        "no" to "挪威语",
        "he" to "希伯来语",
        "id" to "印度尼西亚语",
        "th" to "泰语",
        "ms" to "马来语",
        "fa" to "波斯语",
        "sk" to "斯洛伐克语",
        "bg" to "保加利亚语",
        "hr" to "克罗地亚语",
        "sr" to "塞尔维亚语",
        "sl" to "斯洛文尼亚语",
        "et" to "爱沙尼亚语",
        "lv" to "拉脱维亚语",
        "lt" to "立陶宛语",
        "af" to "南非荷兰语",
        "sw" to "斯瓦希里语",
      )
    }

  var langSearch by remember { mutableStateOf("") }
  var showLanguagePicker by remember { androidx.compose.runtime.mutableStateOf<TrackNode?>(null) }

  if (showLanguagePicker != null) {
    val languagesToShow =
      remember(configuredLanguages, langSearch) {
        val source =
          if (configuredLanguages.size >= 2) {
            configuredLanguages.mapNotNull { codeToName[it] }
          } else {
            allLanguages
          }
        if (langSearch.isBlank()) {
          source
        } else {
          source.filter { it.contains(langSearch, ignoreCase = true) }
        }
      }
    androidx.compose.material3.AlertDialog(
      onDismissRequest = {
        showLanguagePicker = null
        langSearch = ""
      },
      title = {
        Text(
          androidx.compose.ui.res
            .stringResource(app.gyrolet.mpvrx.R.string.ui_translate_to),
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
          OutlinedTextField(
            value = langSearch,
            onValueChange = { langSearch = it },
            placeholder = {
              Text(
                androidx.compose.ui.res
                  .stringResource(app.gyrolet.mpvrx.R.string.ui_search_language),
              )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
          )
          LazyColumn(modifier = Modifier.height(280.dp)) {
            items(languagesToShow, key = { it }) { lang ->
              Text(
                text = lang,
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .clickable {
                      onTranslateSubtitle(showLanguagePicker!!, lang)
                      showLanguagePicker = null
                      langSearch = ""
                    }.padding(MaterialTheme.spacing.medium),
              )
            }
            if (languagesToShow.isEmpty()) {
              item {
                Text(
                  androidx.compose.ui.res
                    .stringResource(app.gyrolet.mpvrx.R.string.ui_no_languages_found),
                  color = MaterialTheme.colorScheme.outline,
                  modifier = Modifier.padding(MaterialTheme.spacing.medium),
                )
              }
            }
          }
        }
      },
      confirmButton = {
        androidx.compose.material3.TextButton(onClick = {
          showLanguagePicker = null
          langSearch = ""
        }) {
          Text(
            androidx.compose.ui.res
              .stringResource(app.gyrolet.mpvrx.R.string.generic_cancel),
          )
        }
      },
    )
  }

  PlayerSheet(onDismissRequest) {
    Column(modifier) {
      AddTrackRow(
        stringResource(R.string.player_sheets_add_ext_sub),
        onAddSubtitle,
        actions = {
          IconButton(onClick = onOpenOnlineSearch) {
            Icon(Icons.RoundedFilled.Search, null)
          }
          if (aiEnabled && realtimeSubsEnabled) {
            IconButton(onClick = onGenerateSubtitle) {
              Icon(Icons.RoundedFilled.Subtitles, "生成字幕")
            }
          }
          IconButton(onClick = onOpenSubtitleSettings) {
            Icon(Icons.RoundedFilled.Palette, null)
          }
          IconButton(onClick = onOpenSubtitleDelay, enabled = delayControlEnabled) {
            Icon(Icons.RoundedFilled.AvTimer, null)
          }
        },
      )

      if (aiEnabled && isTranslating) {
        Column(
          modifier =
            Modifier.padding(
              start = MaterialTheme.spacing.medium,
              end = MaterialTheme.spacing.medium,
              top = MaterialTheme.spacing.small,
            ),
          verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              "${translationStatus.ifBlank {
                "正在翻译"
              }} $translatingTrackName... ${(translationProgress * 100).toInt()}%",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.weight(1f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            FilledTonalIconButton(
              onClick = onCancelTranslation,
              modifier = Modifier.size(36.dp),
              colors =
                IconButtonDefaults.filledTonalIconButtonColors(
                  containerColor = MaterialTheme.colorScheme.errorContainer,
                  contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
              Icon(
                imageVector = Icons.RoundedFilled.Close,
                contentDescription =
                  androidx.compose.ui.res.stringResource(
                    app.gyrolet.mpvrx.R.string.ui_cancel_translation,
                  ),
                modifier = Modifier.size(20.dp),
              )
            }
          }
          LinearProgressIndicator(
            progress = { translationProgress },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      if (aiEnabled && isGeneratingSubtitles) {
        androidx.compose.foundation.layout.Column(
          modifier = Modifier.padding(MaterialTheme.spacing.medium),
          verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        ) {
          Text(
            "${subtitleGenerationStatus.ifBlank {
              "正在生成字幕"
            }}... ${(subtitleGenerationProgress * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
          )
          LinearProgressIndicator(
            progress = { subtitleGenerationProgress },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      LazyColumn {
        items(
          items,
          key = { item ->
            when (item) {
              is SubtitleItem.Track -> item.node.id
              is SubtitleItem.Header -> item.title
              is SubtitleItem.Off -> "off"
              is SubtitleItem.Divider -> "divider"
            }
          },
          contentType = { item -> item.javaClass.simpleName },
        ) { item ->
          when (item) {
            is SubtitleItem.Track -> {
              val track = item.node
              SubtitleTrackRow(
                title = getTrackTitle(track),
                isSelected = isSubtitleSelected(track.id),
                selectionIndicator = subtitleSelectionIndicator(track.id),
                isExternal = track.external == true,
                onToggle = { onToggleSubtitle(track.id) },
                onRemove = { onRemoveSubtitle(track.id) },
                onTranslate = {
                  if (translationEnabled) {
                    if (configuredLanguages.size == 1) {
                      val langName = codeToName[configuredLanguages.first()] ?: configuredLanguages.first()
                      onTranslateSubtitle(track, langName)
                    } else {
                      showLanguagePicker = track
                    }
                  }
                },
                translationEnabled = translationEnabled,
                isCurrentlyTranslating = track.id == translatingTrackId,
              )
            }
            is SubtitleItem.Header -> {
              Row(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = item.title,
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
            is SubtitleItem.Off -> {
              Row(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDisableSubtitles)
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
              ) {
                Checkbox(checked = subtitlesOff, onCheckedChange = { onDisableSubtitles() })
                Text(
                  stringResource(R.string.player_sheets_off),
                  fontWeight = if (subtitlesOff) FontWeight.Bold else FontWeight.Normal,
                  modifier = Modifier.weight(1f),
                )
              }
            }
            SubtitleItem.Divider -> {
              HorizontalDivider(
                modifier =
                  Modifier.padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small,
                  ),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
              )
            }
          }
        }
        item {
          Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }
      }
    }
  }
}

@Composable
fun SubtitleTrackRow(
  title: String,
  isSelected: Boolean,
  selectionIndicator: String?,
  isExternal: Boolean,
  onToggle: () -> Unit,
  onRemove: () -> Unit,
  onTranslate: () -> Unit,
  translationEnabled: Boolean,
  isCurrentlyTranslating: Boolean = false,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onToggle)
        .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.extraSmall),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
  ) {
    Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
    Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))

    if (selectionIndicator != null) {
      Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
      ) {
        Text(
          text = selectionIndicator,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }
    }

    if (isCurrentlyTranslating) {
      androidx.compose.material3.CircularProgressIndicator(
        modifier = Modifier.size(MaterialTheme.spacing.large),
        strokeWidth = MaterialTheme.spacing.smaller,
      )
    }

    if (isExternal) {
      if (translationEnabled) {
        IconButton(onClick = onTranslate) {
          Icon(
            Icons.RoundedFilled.Translate,
            contentDescription =
              androidx.compose.ui.res
                .stringResource(app.gyrolet.mpvrx.R.string.ui_translate),
          )
        }
      }
      IconButton(onClick = onRemove) { Icon(Icons.RoundedFilled.Delete, contentDescription = null) }
    }
  }
}
