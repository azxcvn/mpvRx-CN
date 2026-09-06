# mpvRx Lua 与 JavaScript 自定义命令开发指南

本文档记录了 mpvRx 向 mpv Lua 及 JavaScript 脚本暴露的公开脚本扩展接口（API）。

脚本通过向以下属性写入字符串值来与 mpvRx 进行通信：

```text
user-data/mpvrx/*
```

mpvRx 会监听这些属性的变动，执行原生播放器对应的操作，并在处理完毕后清空该命令属性。唯一的例外是 curl 网络桥接接口：`curl_request` 与 `curl_response` 会保留足够长的时间，以便异步处理 HTTP 请求与响应。

文中的示例使用了以下无需身份验证的公开测试 API 端点：

- JSONPlaceholder: https://jsonplaceholder.typicode.com/
- httpbin: https://httpbin.org/

## 快速上手

1. 在 mpvRx 的“高级设置”中启用 Lua/JS 脚本功能。
2. 将脚本文件放入所选的 mpv 配置文件夹中。
3. 推荐存放在 `scripts/` 子文件夹中；mpvRx 也会自动检索配置文件夹根目录。
4. Lua 脚本使用 `.lua` 后缀，JavaScript 脚本使用 `.js` 后缀。
5. 在 mpvRx 脚本控制面板中勾选启用对应的脚本。
6. 如果在视频播放开始前添加了新脚本，请重新打开视频以生效。

mpvRx 还会自动同步所选 mpv 配置文件夹中的 `script-opts/` 配置目录。

## 重要规则与注意事项

- 所有 mpvRx 命令属性均作为**字符串**处理。
- 跳转（Seek）时间值必须为**整数秒**，请勿传递浮点小数。
- `seek_to_with_text` 与 `seek_by_with_text` 的传参格式为 `秒数|提示文本`。
- `curl_request` 为异步操作，请始终监听 `curl_response`。
- 始终为 curl 请求分配唯一的 `id`，并在回调中忽略 `id` 不匹配的响应。
- JavaScript 通过 mpv 内置的 JavaScript 引擎运行。建议使用与 ES5 兼容的语法：尽量使用 `var` 与 `function`。

## 支持的命令列表

| 属性 | 取值 | 功能说明 |
| --- | --- | --- |
| `user-data/mpvrx/show_text` | 任意字符串 | 显示 mpvRx 原生文本悬浮提示（Toast/Overlay）。 |
| `user-data/mpvrx/toggle_ui` | `show`、`hide`、`toggle` | 显示、隐藏或切换播放器控制界面的显示状态。 |
| `user-data/mpvrx/show_panel` | 面板 ID | 打开 mpvRx 的原生底栏面板或弹出菜单。 |
| `user-data/mpvrx/seek_to` | 整数秒 | 绝对时间跳转到指定秒数。 |
| `user-data/mpvrx/seek_by` | 整数秒 | 相对当前播放进度快进或快退指定秒数。 |
| `user-data/mpvrx/seek_to_with_text` | `秒数|提示文本` | 绝对时间跳转并显示自定义悬浮提示文本。 |
| `user-data/mpvrx/seek_by_with_text` | `秒数|提示文本` | 相对时间跳转并显示自定义悬浮提示文本。 |
| `user-data/mpvrx/software_keyboard` | `show`、`hide`、`toggle` | 控制 Android 软键盘的弹出、收起或切换。 |
| `user-data/mpvrx/curl_request` | JSON 字符串 | 通过原生 libcurl 发送异步 HTTP 网络请求。 |
| `user-data/mpvrx/curl_response` | JSON 字符串 | 由 mpvRx 写入的响应数据。脚本应监听此属性。 |

支持的面板 ID（Panel ID）：

| 面板 ID | 效果 |
| --- | --- |
| `frame_navigation` | 打开逐帧导航底栏面板。 |
| `subtitle_settings` | 打开字幕样式与偏好设置。 |
| `subtitle_delay` | 打开字幕延迟调节控制。 |
| `audio_delay` | 打开音频延迟调节控制。 |
| `video_filters` | 打开视频滤镜调节面板。 |
| `lua_scripts` | 打开 Lua/JS 脚本管理面板。 |
| `hdr_screen_output` | 打开 HDR 屏幕输出控制面板。 |

被监听但非公开的命令：

`set_button_title`、`reset_button_title` 和 `toggle_button` 目前虽然会被 mpv 属性监听器捕获，但播放器命令调度器尚未为其实现公共操作，请将其视为系统保留属性。

## 完整可运行的 Lua 示例

将以下代码保存为 `mpvrx_demo.lua`，放入 mpv 的 `scripts/` 文件夹中。

此脚本会在视频加载时向 JSONPlaceholder 获取一条测试帖子，显示其标题，并绑定常用 mpvRx 命令的快捷键。

```lua
-- mpvrx_demo.lua

local utils = require("mp.utils")

local REQUEST_ID = "mpvrx-demo-lua-post"

local function mpvrx(command, value)
    mp.set_property("user-data/mpvrx/" .. command, tostring(value))
end

local function show(message)
    mpvrx("show_text", message)
end

local function fetch_post()
    show("正在获取 JSONPlaceholder 帖子...")

    mpvrx("curl_request", utils.format_json({
        id = REQUEST_ID,
        url = "https://jsonplaceholder.typicode.com/posts/1",
        method = "GET",
        headers = {
            Accept = "application/json",
        },
        timeout = 15,
    }))
end

mp.observe_property("user-data/mpvrx/curl_response", "string", function(_, value)
    if value == nil or value == "" then return end

    local res = utils.parse_json(value)
    if res == nil or res.id ~= REQUEST_ID then return end

    if res.error ~= nil then
        show("Curl 请求失败: " .. tostring(res.error))
        return
    end

    if tonumber(res.status) ~= 200 then
        show("HTTP 状态码: " .. tostring(res.status))
        return
    end

    local body = utils.parse_json(res.body)
    if body == nil then
        show("无法解析响应内容")
        return
    end

    show("帖子 #" .. tostring(body.id) .. "\n" .. tostring(body.title))
end)

mp.register_event("file-loaded", fetch_post)

mp.add_key_binding("J", "mpvrx-fetch-jsonplaceholder-post", fetch_post)
mp.add_key_binding("U", "mpvrx-toggle-ui", function()
    mpvrx("toggle_ui", "toggle")
end)
mp.add_key_binding("V", "mpvrx-open-video-filters", function()
    mpvrx("show_panel", "video_filters")
end)
mp.add_key_binding("RIGHT", "mpvrx-seek-forward", function()
    mpvrx("seek_by_with_text", "30|快进 30 秒")
end)
mp.add_key_binding("LEFT", "mpvrx-seek-back", function()
    mpvrx("seek_by_with_text", "-10|后退 10 秒")
end)
```

## 完整可运行的 JavaScript 示例

将以下代码保存为 `mpvrx_demo.js`，放入 mpv 的 `scripts/` 文件夹中。

此示例采用 ES5 风格语法以兼容 mpv 的 JS 运行环境。它向 JSONPlaceholder 发送一个 POST 请求，并显示测试 API 返回的模拟新建帖子 ID。

```javascript
// mpvrx_demo.js

var REQUEST_ID = "mpvrx-demo-js-create-post";

function mpvrx(command, value) {
    mp.set_property("user-data/mpvrx/" + command, String(value));
}

function show(message) {
    mpvrx("show_text", message);
}

function createPost() {
    var payload = {
        title: "mpvRx JavaScript curl 测试",
        body: "从 mpv JavaScript 脚本通过 mpvRx curl 发送的内容。",
        userId: 1
    };

    show("正在向 JSONPlaceholder 发送请求...");

    mpvrx("curl_request", JSON.stringify({
        id: REQUEST_ID,
        url: "https://jsonplaceholder.typicode.com/posts",
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload),
        content_type: "application/json",
        timeout: 15
    }));
}

mp.observe_property("user-data/mpvrx/curl_response", "string", function(name, value) {
    if (!value) return;

    var res;
    try {
        res = JSON.parse(value);
    } catch (e) {
        return;
    }

    if (!res || res.id !== REQUEST_ID) return;

    if (res.error) {
        show("Curl 请求失败: " + res.error);
        return;
    }

    if (res.status < 200 || res.status >= 300) {
        show("HTTP 状态码: " + res.status);
        return;
    }

    var body;
    try {
        body = JSON.parse(res.body);
    } catch (e2) {
        show("无法解析响应内容");
        return;
    }

    show("已创建模拟帖子 #" + body.id + "\nHTTP " + res.status);
});

mp.add_key_binding("P", "mpvrx-create-jsonplaceholder-post", createPost);
mp.add_key_binding("U", "mpvrx-toggle-ui-js", function() {
    mpvrx("toggle_ui", "toggle");
});
mp.add_key_binding("S", "mpvrx-open-subtitle-settings-js", function() {
    mpvrx("show_panel", "subtitle_settings");
});
mp.add_key_binding("K", "mpvrx-show-keyboard-js", function() {
    mpvrx("software_keyboard", "show");
});
```

## 命令参考手册

### `show_text`

显示一条短暂的原生 mpvRx 悬浮文本提示。

Lua:

```lua
mp.set_property("user-data/mpvrx/show_text", "着色器已启用")
```

JavaScript:

```javascript
mp.set_property("user-data/mpvrx/show_text", "着色器已启用");
```

### `toggle_ui`

控制播放器界面控制栏的显示/隐藏。

支持的取值：

- `show`
- `hide`
- `toggle`

Lua:

```lua
mp.set_property("user-data/mpvrx/toggle_ui", "hide")
```

JavaScript:

```javascript
mp.set_property("user-data/mpvrx/toggle_ui", "toggle");
```

### `show_panel`

打开 mpvRx 的原生底栏面板或弹出设置。

Lua:

```lua
mp.set_property("user-data/mpvrx/show_panel", "frame_navigation")
mp.set_property("user-data/mpvrx/show_panel", "video_filters")
```

JavaScript:

```javascript
mp.set_property("user-data/mpvrx/show_panel", "lua_scripts");
mp.set_property("user-data/mpvrx/show_panel", "hdr_screen_output");
```

### `seek_to`

绝对跳转到指定秒数（必须为整数秒）。

```lua
mp.set_property("user-data/mpvrx/seek_to", "600")
```

### `seek_by`

相对当前进度快进或快退指定秒数（必须为整数秒）。

```lua
mp.set_property("user-data/mpvrx/seek_by", "30")
mp.set_property("user-data/mpvrx/seek_by", "-10")
```

### `seek_to_with_text`

绝对跳转并同时在屏幕上显示自定义悬浮提示文本。

参数格式：

```text
秒数|提示文本
```

Lua:

```lua
mp.set_property("user-data/mpvrx/seek_to_with_text", "90|跳转到片头")
```

JavaScript:

```javascript
mp.set_property("user-data/mpvrx/seek_to_with_text", "3600|跳转到终局之战");
```

### `seek_by_with_text`

相对当前进度跳转并同时在屏幕上显示自定义悬浮提示文本。

Lua:

```lua
mp.set_property("user-data/mpvrx/seek_by_with_text", "85|跳过片头")
mp.set_property("user-data/mpvrx/seek_by_with_text", "-15|后退 15 秒")
```

JavaScript:

```javascript
mp.set_property("user-data/mpvrx/seek_by_with_text", "30|快进 30 秒");
```

### `software_keyboard`

控制 Android 系统软键盘的弹出或收起。

支持的取值：

- `show`
- `hide`
- `toggle`

Lua:

```lua
mp.set_property("user-data/mpvrx/software_keyboard", "show")
```

JavaScript:

```javascript
mp.set_property("user-data/mpvrx/software_keyboard", "hide");
```

## Curl 网络请求桥接（Curl Bridge）

Curl 桥接接口允许 Lua 和 JavaScript 脚本通过 Android 原生 libcurl 库发送 HTTP 网络请求。脚本通过将 JSON 请求字符串写入以下属性发起：

```text
user-data/mpvrx/curl_request
```

mpvRx 会在执行完请求后将响应写入：

```text
user-data/mpvrx/curl_response
```

所有请求均为**异步执行**，不会阻塞视频播放。

### Curl Request 请求 JSON 字段规范

| 字段名 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | 字符串 | 否 | 由 mpvRx 自动生成 UUID | 建议自行指定唯一 ID，以便脚本正确匹配对应的响应。 |
| `url` | 字符串 | 是 | 无 | 不能为空。必须以 `http://` 或 `https://` 开头。 |
| `method` | 字符串 | 否 | `GET` | 支持：`GET`、`HEAD`、`POST`、`PUT`、`PATCH`、`DELETE`。 |
| `headers` | 对象 | 否 | `{}` | 请求头键值对（字符串映射）。最多允许 64 个 Header。 |
| `body` | 字符串 | 否 | null | 请求体数据，适用于 `POST`、`PUT` 和 `PATCH` 请求。 |
| `content_type` | 字符串 | 否 | `text/plain; charset=utf-8` | 非空时作为 `Content-Type` 发送。 |
| `timeout` | 整数 | 否 | `30` | 超时时间（秒），限制在 1 到 120 秒之间。 |

Lua 发起请求示例：

```lua
local utils = require("mp.utils")

mp.set_property("user-data/mpvrx/curl_request", utils.format_json({
    id = "lua-httpbin-get",
    url = "https://httpbin.org/get",
    method = "GET",
    headers = {
        Accept = "application/json",
    },
    timeout = 10,
}))
```

JavaScript 发起请求示例：

```javascript
mp.set_property("user-data/mpvrx/curl_request", JSON.stringify({
    id: "js-httpbin-get",
    url: "https://httpbin.org/get",
    method: "GET",
    headers = {
        "Accept": "application/json"
    },
    timeout: 10
}));
```

### Curl Response 响应 JSON 字段规范

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | 字符串 | 回显请求中的 `id`；若请求未指定则为自动生成的 ID。 |
| `status` | 整数 | HTTP 状态码。`0` 表示桥接/网络/底层 curl 错误。 |
| `body` | 字符串 | UTF-8 编码的响应体，最大限制为 2 MB。 |
| `headers` | 对象 | 响应头键值对（字符串映射）。 |
| `error` | 字符串或 null | 请求成功时为空/null；请求失败时返回错误描述字符串。 |

Lua 监听响应示例：

```lua
local utils = require("mp.utils")

mp.observe_property("user-data/mpvrx/curl_response", "string", function(_, value)
    if value == nil or value == "" then return end

    local res = utils.parse_json(value)
    if res == nil or res.id ~= "lua-httpbin-get" then return end

    if res.error ~= nil then
        mp.set_property("user-data/mpvrx/show_text", "Curl 出错: " .. res.error)
        return
    end

    mp.set_property("user-data/mpvrx/show_text", "HTTP 状态码: " .. tostring(res.status))
end)
```

JavaScript 监听响应示例：

```javascript
mp.observe_property("user-data/mpvrx/curl_response", "string", function(name, value) {
    if (!value) return;

    var res;
    try {
        res = JSON.parse(value);
    } catch (e) {
        return;
    }

    if (!res || res.id !== "js-httpbin-get") return;

    if (res.error) {
        mp.set_property("user-data/mpvrx/show_text", "Curl 出错: " + res.error);
        return;
    }

    mp.set_property("user-data/mpvrx/show_text", "HTTP 状态码: " + res.status);
});
```

### Curl 限制与运行机制

- 响应体抓取最大限制为 2 MB。
- 响应头抓取最大限制为 256 KB。
- 请求头最多允许 64 项。
- HTTP 与 HTTPS 均会自动跟随重定向。
- 仅允许 HTTP 与 HTTPS 协议的 URL。
- 超时时间同时作用于连接超时与请求总耗时。
- 在底层桥接实现中，`DELETE` 请求不会附带请求体。
- `curl_response` 不会自动清空，因此脚本中务必根据 `id` 校验是否属于本请求的响应。

## 自定义按钮（Custom Buttons）

mpvRx 的自定义按钮支持编写 Lua 和 JavaScript 执行逻辑。

在自定义按钮编辑器中：

- `按钮标题`（Button title）：在播放器 UI 中显示的文本。
- `点击操作`（Tap action）：必填项。
- `长按操作`（Long press action）：选填项。
- `启动时执行`（On startup）：选填项。
- `脚本语言`（Script language）：可选择 Lua 或 JavaScript。

在编辑器中**只需粘贴操作逻辑代码体**即可。mpvRx 会自动将其包装到生成的脚本中，并在内部注册对应的脚本消息通道。

Lua 点击操作示例：

```lua
mp.set_property("deband", "yes")
mp.set_property("user-data/mpvrx/show_text", "已启用去色带")
```

Lua 长按操作示例：

```lua
mp.set_property("deband", "no")
mp.set_property("user-data/mpvrx/show_text", "已关闭去色带")
```

JavaScript 点击操作示例：

```javascript
mp.set_property("video-zoom", "0.25");
mp.set_property("user-data/mpvrx/show_text", "画面放大 25%");
```

JavaScript 长按操作示例：

```javascript
mp.set_property("video-zoom", "0");
mp.set_property("user-data/mpvrx/show_text", "画面缩放已重置");
```

自定义按钮内部实现机制：

- mpvRx 会将生成的脚本写入应用内部的 `scripts/` 目录中。
- Lua 按钮会生成到 `custombuttons.lua` 文件中。
- JavaScript 按钮会生成到 `custombuttons.js` 文件中。
- 点击按钮会发送 `script-message call_button_<safe_id>`。
- 长按按钮会发送 `script-message call_button_long_<safe_id>`。
- `<safe_id>` 为内部按钮 ID（其中 `-` 会被替换为 `_`）。
- 生成的 Lua 操作由 `is_active_instance()` 进行实例校验保护。
- 生成的 JavaScript 操作由 `isActiveInstance()` 进行实例校验保护。

请勿在常规外部脚本文件中调用 `is_active_instance()` 或 `isActiveInstance()`，它们仅存在于自动生成的自定义按钮脚本环境中。

## Android 设备遥测属性（Telemetry Properties）

mpvRx 会将 Android 设备状态写入 mpv 的 `user-data/android/*` 属性中。脚本可以读取或监听这些状态值。

| 属性 | 类型 | 含义 |
| --- | --- | --- |
| `user-data/android/battery-level` | 整数 | 电池剩余百分比（0 至 100）。 |
| `user-data/android/battery-charging` | 布尔值 | 是否正在充电（`true` 表示正在充电）。 |
| `user-data/android/battery-plugged` | 布尔值 | 是否接通电源（`true` 表示已连接电源）。 |

Lua 遥测示例（电量低于 15% 且未充电时自动关闭去色带）：

```lua
mp.observe_property("user-data/android/battery-level", "native", function(_, level)
    if level == nil then return end

    local charging = mp.get_property_native("user-data/android/battery-charging")
    if tonumber(level) < 15 and not charging then
        mp.set_property("deband", "no")
        mp.set_property("user-data/mpvrx/show_text", "电量较低：已自动关闭去色带")
    end
end)
```

JavaScript 遥测示例：

```javascript
mp.observe_property("user-data/android/battery-level", "native", function(name, level) {
    if (level === null || level === undefined) return;

    var charging = mp.get_property_native("user-data/android/battery-charging");
    if (Number(level) < 15 && !charging) {
        mp.set_property("deband", "no");
        mp.set_property("user-data/mpvrx/show_text", "电量较低：已自动关闭去色带");
    }
});
```

## 常见问题与排错指南

如果执行命令后没有任何反应，请按以下步骤排查：

- 确认已在 mpvRx 高级设置中启用了脚本支持。
- 确认已在脚本控制面板中勾选并启用了该脚本。
- 确认脚本文件的扩展名为 `.lua` 或 `.js`。
- 跳转（Seek）类命令请务必使用整数秒。
- 检查命令属性路径是否准确无误（大小写敏感）。
- 对于 curl 请求，处理响应前请务必核对 `res.id`。
- 对于 curl 请求，请检查 `res.error` 和 `res.status`。
- 添加新脚本后，请重新打开视频播放以使新脚本生效。

极简冒烟测试（Smoke Test）：

Lua:

```lua
mp.register_event("file-loaded", function()
    mp.set_property("user-data/mpvrx/show_text", "Lua 脚本加载成功")
end)
```

JavaScript:

```javascript
mp.register_event("file-loaded", function() {
    mp.set_property("user-data/mpvrx/show_text", "JavaScript 脚本加载成功");
});
```
