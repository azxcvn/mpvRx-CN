<p align="center">
  <img src="fastlane/metadata/android/en-US/images/icon.png" width="160" height="160" />
</p>

<h1 align="center">mpvRx-CN</h1>

<p align="center">
  <b>基于 libmpv 的高性能 Android 视频播放器中文汉化维护归档</b>
</p>

---

> [!IMPORTANT]
> ### 📢 项目停更与维护声明
> 
> 1. **停止更新**：本仓库自 **2026 年 9 月 6 日** 起正式停止维护更新，后续将不再同步上游代码或发布新版本。
> 2. **上游已自带中文支持**：上游原作者项目 [Riteshp2001/mpvRx](https://github.com/Riteshp2001/mpvRx) 现已在官方主线中加入了大量的中文界面支持，用户无需再依赖专门的第三方汉化版。目前官方版本仍有少部分界面文本尚未完全汉化，预计后续原作者会随着版本更新逐步补充覆盖。
> 3. **原作者更新迭代极快**：由于原作者更新节奏非常快，第三方分支持续跟进的意义有限，因此本仓库已无继续跟随更新的必要，建议大家直接使用官方原版。
> 4. **汉化代码同步基准**：截至 2026 年 9 月 6 日，本仓库所包含的最新完整中文汉化代码已同步至原作者的提交哈希：
>    - **Commit Hash:** [`ab45857dda1f117cbe0cc23c167b6c65baddcbc8`](https://github.com/Riteshp2001/mpvRx/commit/ab45857dda1f117cbe0cc23c167b6c65baddcbc8)

---

## 📚 中文文档查阅与导航

为了方便大家查阅项目原始的功能细节与版本变更历程，`docs/` 目录下已准备了完整的中文对照文档：

* 📘 **[原版 README 完整中文文档](docs/README-已翻译.md)**
  * 原作者 README 的全量中文翻译。包含播放器全套功能特性（主题系统、手势操作、HDR 渲染管线、温控与电池优化、Anime4K 画面增强、环境光氛围灯、双字幕系统、Google Cast 投屏、Jellyfin 客户端、种子边下边播、AI 工具集成等）、Mpv 续航配置建议、项目编译构建指南及致谢信息。
* 📜 **[完整版本更新历史记录 (CHANGELOG)](docs/CHANGELOG-已翻译.md)**
  * 自 1.0.0 起至 2.5.0 最新版本的完整更新日志中文翻译。包含每个版本的详细特性迭代、功能改动以及问题修复记录。
* 🛠️ **[Lua 与 JS 自定义脚本命令开发指南 (MPVRX_CUSTOM_COMMANDS)](docs/MPVRX_CUSTOM_COMMANDS-已翻译.md)**
  * 原版脚本扩展接口文档的完整中文翻译。供进阶用户和脚本开发者查阅 mpvRx 暴露的命令属性（`user-data/mpvrx/*`）、curl 异步网络请求桥接、原生控制面板 ID、自定义按钮交互逻辑及完整示例代码。
* 📱 **[安装包版本与架构选择指南 (APK_SELECTION_GUIDE)](docs/APK_SELECTION_GUIDE-版本选择指南.md)**
  * 针对 64位 (arm64-v8a)、32位 (armeabi-v7a)、universal (通用不分包)、x86_64、x86 等不同架构安装包的选包建议与详细区别解析，方便用户按设备快速选对版本。

---

## 🔗 相关项目链接

* **上游官方仓库**：[Riteshp2001/mpvRx](https://github.com/Riteshp2001/mpvRx)
* **官方最新发布**：[mpvRx Releases](https://github.com/Riteshp2001/mpvRx/releases)
* **开源许可证**：[GNU Affero General Public License v3.0 (AGPL-3.0)](LICENSE)
