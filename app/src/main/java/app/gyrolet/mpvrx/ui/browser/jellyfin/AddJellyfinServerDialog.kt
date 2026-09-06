/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package app.gyrolet.mpvrx.ui.browser.jellyfin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.gyrolet.mpvrx.domain.jellyfin.JellyfinAuthMode
import app.gyrolet.mpvrx.domain.jellyfin.JellyfinServer
import app.gyrolet.mpvrx.ui.components.themedSegmentedButtonColors
import app.gyrolet.mpvrx.ui.icons.Icon
import app.gyrolet.mpvrx.ui.icons.Icons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJellyfinServerDialog(
  isOpen: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
  initialServer: JellyfinServer? = null,
  onDismiss: () -> Unit,
  onConnect: (serverUrl: String, serverName: String, authMode: JellyfinAuthMode, username: String, password: String, token: String) -> Unit,
) {
  if (!isOpen) return

  val sheetState =
    rememberBottomSheetState(
      initialValue = SheetValue.Hidden,
      enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

  var serverUrl by remember(initialServer) { mutableStateOf(initialServer?.serverUrl ?: "") }
  var serverName by remember(initialServer) { mutableStateOf(initialServer?.name ?: "") }
  var authMode by remember(initialServer) { mutableStateOf(JellyfinAuthMode.CREDENTIALS) }
  var username by remember(initialServer) { mutableStateOf(initialServer?.username ?: "") }
  var password by remember(initialServer) { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var token by remember(initialServer) { mutableStateOf("") }

  val canConnect =
    serverUrl.isNotBlank() &&
      when (authMode) {
        JellyfinAuthMode.CREDENTIALS -> username.isNotBlank()
        JellyfinAuthMode.TOKEN -> token.isNotBlank()
      }

  val submitAction = {
    if (canConnect && !isLoading) {
      onConnect(serverUrl, serverName, authMode, username, password, token)
    }
  }

  ModalBottomSheet(
    onDismissRequest = { if (!isLoading) onDismiss() },
    sheetState = sheetState,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    dragHandle = { BottomSheetDefaults.DragHandle() },
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp)
          .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "添加 Jellyfin 服务器",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "输入您的服务器地址和账户信息",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = { if (!isLoading) onDismiss() },
          enabled = !isLoading,
        ) {
          Icon(
            imageVector = Icons.RoundedFilled.Close,
            contentDescription = "关闭",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Server URL Input
      OutlinedTextField(
        value = serverUrl,
        onValueChange = { serverUrl = it },
        label = { Text("服务器地址") },
        placeholder = { Text("jellyfin.example.com or 192.168.1.100:8096") },
        leadingIcon = {
          Icon(
            imageVector = Icons.RoundedFilled.BringYourOwnIp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
        },
        trailingIcon = {
          if (serverUrl.isNotEmpty()) {
            IconButton(onClick = { serverUrl = "" }) {
              Icon(
                imageVector = Icons.RoundedFilled.Close,
                contentDescription = "清除",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        },
        supportingText = { Text("将自动优先尝试 HTTPS") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions =
          KeyboardOptions(
            keyboardType = KeyboardType.Uri,
            imeAction = ImeAction.Next,
          ),
      )

      // Display Name Input
      OutlinedTextField(
        value = serverName,
        onValueChange = { serverName = it },
        label = { Text("显示名称（可选）") },
        placeholder = { Text("家庭 Jellyfin") },
        leadingIcon = {
          Icon(
            imageVector = Icons.RoundedFilled.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions =
          KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
          ),
      )

      // Authentication Method Selector (M3 SingleChoiceSegmentedButtonRow)
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "认证方式",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SingleChoiceSegmentedButtonRow(
          modifier = Modifier.fillMaxWidth(),
        ) {
          SegmentedButton(
            selected = authMode == JellyfinAuthMode.CREDENTIALS,
            onClick = { authMode = JellyfinAuthMode.CREDENTIALS },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            colors = themedSegmentedButtonColors(),
            icon = {
              Icon(
                imageVector = Icons.RoundedFilled.Person,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
            },
          ) {
            Text("凭据")
          }
          SegmentedButton(
            selected = authMode == JellyfinAuthMode.TOKEN,
            onClick = { authMode = JellyfinAuthMode.TOKEN },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = themedSegmentedButtonColors(),
            icon = {
              Icon(
                imageVector = Icons.RoundedFilled.Security,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
            },
          ) {
            Text("API 令牌")
          }
        }
      }

      // Conditional Auth Fields
      if (authMode == JellyfinAuthMode.CREDENTIALS) {
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("用户名") },
          leadingIcon = {
            Icon(
              imageVector = Icons.RoundedFilled.Person,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions =
            KeyboardOptions(
              keyboardType = KeyboardType.Text,
              imeAction = ImeAction.Next,
            ),
        )

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("密码") },
          leadingIcon = {
            Icon(
              imageVector = Icons.RoundedFilled.Lock,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions =
            KeyboardOptions(
              keyboardType = if (isPasswordVisible) KeyboardType.Text else KeyboardType.Password,
              imeAction = ImeAction.Done,
            ),
          keyboardActions = KeyboardActions(onDone = { submitAction() }),
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                imageVector = if (isPasswordVisible) Icons.RoundedFilled.VisibilityOff else Icons.RoundedFilled.Visibility,
                contentDescription = if (isPasswordVisible) "隐藏密码" else "显示密码",
              )
            }
          },
          modifier = Modifier.fillMaxWidth(),
        )
      } else {
        OutlinedTextField(
          value = token,
          onValueChange = { token = it },
          label = { Text("访问令牌 / API 密钥") },
          placeholder = { Text("粘贴在 Jellyfin 控制面板中生成的令牌") },
          leadingIcon = {
            Icon(
              imageVector = Icons.RoundedFilled.Security,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          supportingText = { Text("控制面板 -> API 密钥") },
          singleLine = true,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions =
            KeyboardOptions(
              keyboardType = KeyboardType.Password,
              imeAction = ImeAction.Done,
            ),
          keyboardActions = KeyboardActions(onDone = { submitAction() }),
        )
      }

      // Animated Error Card
      AnimatedVisibility(
        visible = !errorMessage.isNullOrBlank(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
      ) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Icon(
              imageVector = Icons.RoundedFilled.Warning,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(22.dp),
            )
            Text(
              text = errorMessage ?: "",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }

      // Action Buttons
      Button(
        onClick = submitAction,
        enabled = canConnect && !isLoading,
        shape = RoundedCornerShape(16.dp),
        modifier =
          Modifier
            .fillMaxWidth()
            .height(52.dp),
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.5.dp,
            color = MaterialTheme.colorScheme.onPrimary,
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "正在连接...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
        } else {
          Icon(
            imageVector = Icons.RoundedFilled.Link,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "连接服务器",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageJellyfinServersDialog(
  isOpen: Boolean,
  servers: List<JellyfinServer>,
  activeServer: JellyfinServer?,
  onDismiss: () -> Unit,
  onSelectServer: (JellyfinServer) -> Unit,
  onDeleteServer: (JellyfinServer) -> Unit,
  onAddServerClick: () -> Unit,
) {
  if (!isOpen) return

  val sheetState =
    rememberBottomSheetState(
      initialValue = SheetValue.Hidden,
      enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    dragHandle = { BottomSheetDefaults.DragHandle() },
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp)
          .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Jellyfin 服务器",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "已配置 ${servers.size} 台服务器",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        TextButton(onClick = onDismiss) {
          Text("完成")
        }
      }

      if (servers.isEmpty()) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer,
            ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.RoundedFilled.BringYourOwnIp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
              )
              Text(
                text = "尚未连接任何服务器",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          servers.forEach { server ->
            val isSelected = server.id == activeServer?.id
            Surface(
              shape = RoundedCornerShape(16.dp),
              color =
                if (isSelected) {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceContainer
                },
              modifier =
                Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(16.dp))
                  .clickable {
                    onSelectServer(server)
                    onDismiss()
                  },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
              ) {
                Surface(
                  shape = CircleShape,
                  color =
                    if (isSelected) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.surfaceContainerHighest
                    },
                  modifier = Modifier.size(40.dp),
                ) {
                  Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = if (isSelected) Icons.RoundedFilled.Check else Icons.RoundedFilled.BringYourOwnIp,
                      contentDescription = null,
                      tint =
                        if (isSelected) {
                          MaterialTheme.colorScheme.onPrimary
                        } else {
                          MaterialTheme.colorScheme.onSurfaceVariant
                        },
                      modifier = Modifier.size(20.dp),
                    )
                  }
                }

                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    Text(
                      text = server.name,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                      color =
                        if (isSelected) {
                          MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                          MaterialTheme.colorScheme.onSurface
                        },
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                    )
                    if (isSelected) {
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(2.dp),
                      ) {
                        Text(
                          text = "Active",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onPrimary,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                      }
                    }
                  }
                  Text(
                    text = server.serverUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                      if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                      } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }

                IconButton(
                  onClick = { onDeleteServer(server) },
                  modifier = Modifier.size(36.dp),
                ) {
                  Icon(
                    imageVector = Icons.RoundedFilled.Delete,
                    contentDescription = "移除服务器",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp),
                  )
                }
              }
            }
          }
        }
      }

      FilledTonalButton(
        onClick = {
          onDismiss()
          onAddServerClick()
        },
        shape = RoundedCornerShape(16.dp),
        modifier =
          Modifier
            .fillMaxWidth()
            .height(48.dp),
      ) {
        Icon(
          imageVector = Icons.RoundedFilled.Add,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "添加另一台服务器",
          style = MaterialTheme.typography.labelLarge,
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}
