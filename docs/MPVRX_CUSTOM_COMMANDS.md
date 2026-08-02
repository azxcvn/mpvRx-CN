# MpvRx Lua 与 JavaScript 命令指南

本文档记录了 MpvRx 向 mpv Lua 和 JavaScript 脚本公开的所有脚本接口。

脚本通过向以下属性写入字符串值与 MpvRx 通信：

```text
user-data/mpvrx/*
```

MpvRx 会监听这些属性，执行对应的原生播放器操作，并在处理后清除命令属性。唯一的例外是 curl 桥接：`curl_request` 和 `curl_response` 会保留足够长的时间以完成异步 HTTP 处理。

以下示例使用了公开的无认证 API 端点：

- JSONPlaceholder: https://jsonplaceholder.typicode.com/
- httpbin: https://httpbin.org/

## 快速开始

1. 在 MpvRx 高级设置中启用 Lua/JS 脚本功能。
2. 将脚本文件放入已选择的 mpv 配置文件夹中。
3. 建议使用 `scripts/` 子文件夹。MpvRx 也会回退到配置根目录。
4. Lua 脚本使用 `.lua` 扩展名，JavaScript 脚本使用 `.js` 扩展名。
5. 在 MpvRx 脚本面板中选择要启用的脚本。
6. 如果在播放开始前添加了脚本，需要重新打开视频。

MpvRx 也会同步已选 mpv 配置文件夹中的 `script-opts/` 目录。

## 重要规则

- 所有 MpvRx 命令属性均以字符串形式处理。
- 跳转值必须为整数秒，不要发送小数。
- `seek_to_with_text` 和 `seek_by_with_text` 使用 `秒数|消息` 格式。
- `curl_request` 是异步的，务必监听 `curl_response`。
- 始终为 curl 请求指定唯一的 `id`，并忽略具有不同 `id` 的响应。
- JavaScript 通过 mpv 的 JavaScript 运行执行，使用 ES5 兼容语法最为安全：推荐使用 `var` 和 `function`。

## 支持的命令

| 属性 | 值 | 功能说明 |
| --- | --- | --- |
| `user-data/mpvrx/show_text` | 任意字符串 | 显示原生 MpvRx 文本叠加层。 |
| `user-data/mpvrx/toggle_ui` | `show`、`hide`、`toggle` | 显示、隐藏或切换播放器控件。 |
| `user-data/mpvrx/show_panel` | 面板 ID | 打开原生 MpvRx 面板或工作表。 |
| `user-data/mpvrx/seek_to` | 整数秒 | 跳转到绝对时间戳。 |
| `user-data/mpvrx/seek_by` | 整数秒 | 相对于当前时间戳跳转。 |
| `user-data/mpvrx/seek_to_with_text` | `秒数|消息` | 绝对跳转并显示叠加文字。 |
| `user-data/mpvrx/seek_by_with_text` | `秒数|消息` | 相对跳转并显示叠加文字。 |
| `user-data/mpvrx/software_keyboard` | `show`、`hide`、`toggle` | 控制 Android 软键盘。 |
| `user-data/mpvrx/curl_request` | JSON 字符串 | 通过原生 curl 运行异步 HTTP 请求。 |
| `user-data/mpvrx/curl_response` | JSON 字符串 | MpvRx 写入的响应，脚本应监听此属性。 |

支持的面板 ID：

| 面板 ID | 效果 |
| --- | --- |
| `frame_navigation` | 打开逐帧导航面板。 |
| `subtitle_settings` | 打开字幕样式设置。 |
| `subtitle_delay` | 打开字幕延迟控制。 |
| `audio_delay` | 打开音频延迟控制。 |
| `video_filters` | 打开视频滤镜控制。 |
| `lua_scripts` | 打开脚本面板。 |
| `hdr_screen_output` | 打开 HDR 屏幕输出控制。 |

已监听但未公开的命令：

`set_button_title`、`reset_button_title` 和 `toggle_button` 目前已被 mpv 属性观察器监听，但播放器命令分派器尚未为其实现公开行为，请将其视为保留命令。

## 完整 Lua 示例

将此文件保存为 mpv `scripts/` 文件夹中的 `mpvrx_demo.lua`。

该脚本会从 JSONPlaceholder 获取一篇公开帖子，显示帖子标题，并包含常用 MpvRx 命令的按键绑定。

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
    show("正在从 JSONPlaceholder 获取帖子...")

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
        show("Curl 失败: " .. tostring(res.error))
        return
    end

    if tonumber(res.status) ~= 200 then
        show("HTTP " .. tostring(res.status))
        return
    end

    local body = utils.parse_json(res.body)
    if body == nil then
        show("无法解析响应正文")
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
    mpvrx("seek_by_with_text", "30|前进 30 秒")
end)
mp.add_key_binding("LEFT", "mpvrx-seek-back", function()
    mpvrx("seek_by_with_text", "-10|后退 10 秒")
end)
```

## 完整 JavaScript 示例

将此文件保存为 mpv `scripts/` 文件夹中的 `mpvrx_demo.js`。

此示例使用 ES5 风格的 JavaScript 以确保 mpv 兼容性。它向 JSONPlaceholder 发送 POST 请求，并显示公开测试 API 返回的假创建帖子 ID。

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
        title: "MpvRx JavaScript curl 测试",
        body: "通过 MpvRx curl 从 mpv JavaScript 脚本发送。",
        userId: 1
    };

    show("正在向 JSONPlaceholder 发送...");

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
        show("Curl 失败: " + res.error);
        return;
    }

    if (res.status < 200 || res.status >= 300) {
        show("HTTP " + res.status);
        return;
    }

    var body;
    try {
        body = JSON.parse(res.body);
    } catch (e2) {
        show("无法解析响应正文");
        return;
    }

    show("已创建假帖子 #" + body.id + "\nHTTP " + res.status);
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

## 命令参考

### `show_text`

显示一个简短的原生 MpvRx 叠加层。

Lua：

```lua
mp.set_property("user-data/mpvrx/show_text", "已启用着色器")
```

JavaScript：

```javascript
mp.set_property("user-data/mpvrx/show_text", "已启用着色器");
```

### `toggle_ui`

控制播放器控件叠加层的显示。

可接受的值：

- `show`
- `hide`
- `toggle`

Lua：

```lua
mp.set_property("user-data/mpvrx/toggle_ui", "hide")
```

JavaScript：

```javascript
mp.set_property("user-data/mpvrx/toggle_ui", "toggle");
```

### `show_panel`

打开原生 MpvRx 面板或工作表。

Lua：

```lua
mp.set_property("user-data/mpvrx/show_panel", "frame_navigation")
mp.set_property("user-data/mpvrx/show_panel", "video_filters")
```

JavaScript：

```javascript
mp.set_property("user-data/mpvrx/show_panel", "lua_scripts");
mp.set_property("user-data/mpvrx/show_panel", "hdr_screen_output");
```

### `seek_to`

跳转到以整数秒计的绝对时间戳。

```lua
mp.set_property("user-data/mpvrx/seek_to", "600")
```

### `seek_by`

相对于当前时间戳按整数秒跳转。

```lua
mp.set_property("user-data/mpvrx/seek_by", "30")
mp.set_property("user-data/mpvrx/seek_by", "-10")
```

### `seek_to_with_text`

跳转到绝对时间戳并显示自定义叠加文字。

值格式：

```text
秒数|消息
```

Lua：

```lua
mp.set_property("user-data/mpvrx/seek_to_with_text", "90|跳转到片头")
```

JavaScript：

```javascript
mp.set_property("user-data/mpvrx/seek_to_with_text", "3600|跳转到终幕");
```

### `seek_by_with_text`

相对于当前时间戳跳转并显示自定义叠加文字。

Lua：

```lua
mp.set_property("user-data/mpvrx/seek_by_with_text", "85|跳过片头")
mp.set_property("user-data/mpvrx/seek_by_with_text", "-15|后退 15 秒")
```

JavaScript：

```javascript
mp.set_property("user-data/mpvrx/seek_by_with_text", "30|前进 30 秒");
```

### `software_keyboard`

控制 Android 软键盘。

可接受的值：

- `show`
- `hide`
- `toggle`

Lua：

```lua
mp.set_property("user-data/mpvrx/software_keyboard", "show")
```

JavaScript：

```javascript
mp.set_property("user-data/mpvrx/software_keyboard", "hide");
```

## Curl 桥接

Curl 桥接让 Lua 和 JavaScript 脚本能够通过原生 libcurl 桥接发起 HTTP 请求。脚本将 JSON 请求写入：

```text
user-data/mpvrx/curl_request
```

MpvRx 将响应写入：

```text
user-data/mpvrx/curl_response
```

请求是异步的。请求运行期间播放继续。

### Curl 请求 JSON

| 字段 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `id` | string | 否 | 由 MpvRx 生成 UUID | 建议使用自己的 id，以便脚本匹配响应。 |
| `url` | string | 是 | 无 | 不能为空。使用 `http://` 或 `https://`。 |
| `method` | string | 否 | `GET` | 支持：`GET`、`HEAD`、`POST`、`PUT`、`PATCH`、`DELETE`。 |
| `headers` | object | 否 | `{}` | 字符串键值对的请求头，最多 64 个。 |
| `body` | string | 否 | null | 用于 `POST`、`PUT` 和 `PATCH`。 |
| `content_type` | string | 否 | `text/plain; charset=utf-8` | 非空时作为 `Content-Type` 发送。 |
| `timeout` | integer | 否 | `30` | 限制在 1 到 120 秒之间。 |

Lua 请求：

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

JavaScript 请求：

```javascript
mp.set_property("user-data/mpvrx/curl_request", JSON.stringify({
    id: "js-httpbin-get",
    url: "https://httpbin.org/get",
    method: "GET",
    headers: {
        "Accept": "application/json"
    },
    timeout: 10
}));
```

### Curl 响应 JSON

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | string | 回显请求的 id；如果请求未提供则返回生成的 id。 |
| `status` | integer | HTTP 状态码。`0` 表示桥接/网络/原生错误。 |
| `body` | string | UTF-8 响应正文，上限 2 MB。 |
| `headers` | object | 字符串键值对的响应头。 |
| `error` | string 或 null | 成功时为 null/缺失；失败时为字符串错误消息。 |

Lua 监听器：

```lua
local utils = require("mp.utils")

mp.observe_property("user-data/mpvrx/curl_response", "string", function(_, value)
    if value == nil or value == "" then return end

    local res = utils.parse_json(value)
    if res == nil or res.id ~= "lua-httpbin-get" then return end

    if res.error ~= nil then
        mp.set_property("user-data/mpvrx/show_text", "Curl 错误: " .. res.error)
        return
    end

    mp.set_property("user-data/mpvrx/show_text", "HTTP " .. tostring(res.status))
end)
```

JavaScript 监听器：

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
        mp.set_property("user-data/mpvrx/show_text", "Curl 错误: " + res.error);
        return;
    }

    mp.set_property("user-data/mpvrx/show_text", "HTTP " + res.status);
});
```

### Curl 限制与行为

- 正文捕获上限为 2 MB。
- 响应头捕获上限为 256 KB。
- 请求头上限为 64 条。
- HTTP 和 HTTPS 均跟随重定向。
- 仅允许 HTTP 和 HTTPS URL。
- 超时同时适用于连接和请求总时长。
- 原生桥接中 `DELETE` 请求不发送正文。
- `curl_response` 不会自动清除，务必检查 `id`。

## 自定义按钮

MpvRx 自定义按钮支持 Lua 和 JavaScript 动作。

在自定义按钮编辑器中：

- `按钮标题` 是播放器界面中显示的文本。
- `点击动作` 为必填。
- `长按动作` 为可选。
- `启动时` 为可选。
- `脚本语言` 可以是 Lua 或 JavaScript。

只将动作正文粘贴到编辑器中。MpvRx 会将其包装进生成的脚本，并在内部注册相应的脚本消息。

Lua 点击动作示例：

```lua
mp.set_property("deband", "yes")
mp.set_property("user-data/mpvrx/show_text", "已启用去色带")
```

Lua 长按动作示例：

```lua
mp.set_property("deband", "no")
mp.set_property("user-data/mpvrx/show_text", "已禁用去色带")
```

JavaScript 点击动作示例：

```javascript
mp.set_property("video-zoom", "0.25");
mp.set_property("user-data/mpvrx/show_text", "缩放 25%");
```

JavaScript 长按动作示例：

```javascript
mp.set_property("video-zoom", "0");
mp.set_property("user-data/mpvrx/show_text", "已重置缩放");
```

生成的自定义按钮内部机制：

- MpvRx 将生成的脚本写入应用内部 `scripts/` 目录。
- Lua 按钮生成到 `custombuttons.lua`。
- JavaScript 按钮生成到 `custombuttons.js`。
- 点击按钮发送 `script-message call_button_<safe_id>`。
- 长按按钮发送 `script-message call_button_long_<safe_id>`。
- `<safe_id>` 是内部按钮 id，其中的 `-` 替换为 `_`。
- 生成的 Lua 动作由 `is_active_instance()` 保护。
- 生成的 JavaScript 动作由 `isActiveInstance()` 保护。

不要在普通脚本文件中调用 `is_active_instance()` 或 `isActiveInstance()`。它们只存在于生成的自定义按钮脚本内部。

## Android 遥测属性

MpvRx 将 Android 设备状态写入 mpv `user-data/android/*` 属性。脚本可以读取或监听这些值。

| 属性 | 类型 | 含义 |
| --- | --- | --- |
| `user-data/android/battery-level` | integer | 电池电量，0 到 100。 |
| `user-data/android/battery-charging` | boolean | 充电中为 `true`。 |
| `user-data/android/battery-plugged` | boolean | 插入电源时为 `true`。 |

Lua 遥测示例：

```lua
mp.observe_property("user-data/android/battery-level", "native", function(_, level)
    if level == nil then return end

    local charging = mp.get_property_native("user-data/android/battery-charging")
    if tonumber(level) < 15 and not charging then
        mp.set_property("deband", "no")
        mp.set_property("user-data/mpvrx/show_text", "电量低：已禁用去色带")
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
        mp.set_property("user-data/mpvrx/show_text", "电量低：已禁用去色带");
    }
});
```

## 故障排查

如果某个命令没有反应：

- 确认已在 MpvRx 设置中启用脚本功能。
- 确认已在脚本面板中选中该脚本。
- 确认脚本文件扩展名是 `.lua` 或 `.js`。
- 跳转命令使用整数秒。
- 使用准确的命令属性路径。
- 对于 curl，处理响应前先检查 `res.id`。
- 对于 curl，检查 `res.error` 和 `res.status`。
- 添加新脚本后重新打开视频。

最小冒烟测试：

Lua：

```lua
mp.register_event("file-loaded", function()
    mp.set_property("user-data/mpvrx/show_text", "Lua 脚本已加载")
end)
```

JavaScript：

```javascript
mp.register_event("file-loaded", function() {
    mp.set_property("user-data/mpvrx/show_text", "JavaScript 脚本已加载");
});
```
