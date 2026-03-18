<p align="center">
  <img width="300" src="./github/logo.png" alt="GdeiAssistant Android logo">
</p>

# 广东二师助手安卓客户端

广东二师助手安卓客户端当前正在进行 2.0 Jetpack Compose 原生重构。项目以广东第二师范学院校园助手系统后端接口为基础，围绕教务查询、校园服务、社区互动和个人中心提供统一的安卓端体验。

## 当前状态

- Jetpack Compose 全面重构中，当前主流程已经迁移到原生 Compose UI
- 最低支持 Android 8.0 (`minSdk 26`)，目标版本 Android 15 (`targetSdk 35`)
- 使用 Kotlin、Material 3、Hilt、Retrofit、OkHttp、DataStore、Coil 等现代 Android 技术栈
- 支持 Mock 数据模式，方便在后端联调前进行前端开发与页面回归

## 已覆盖模块

- 教务查询：成绩、课表、四六级、教学评价、考研查询、自习室
- 校园服务：校园卡、充值、图书借阅、数据中心、黄页、电费
- 社区功能：二手、失物招领、树洞、表白墙、话题、全民快递、拍好校园、卖室友
- 资讯与账号：新闻通知、系统通知、互动消息、个人资料、头像管理、隐私与账号设置

## 近期更新

- 完成 Home、Profile、Messages 等核心页面的 Compose 化重构
- 对齐并重做了社区模块的大量列表页、详情页、发布页与个人页
- 资料页补齐了头像管理、上传、恢复默认头像、地区与院系等字段链路
- 资讯模块改为原生详情流，系统通知和新闻支持应用内阅读与跳转
- 根据后端调整，安卓端已删除阅读模块及相关接口、路由、Mock 数据

## 技术栈

- Kotlin 2.1
- Jetpack Compose + Material 3
- Hilt
- Retrofit + OkHttp
- Jackson / Gson / Fastjson
- DataStore
- Coil

## 快速开始

### 克隆仓库

```bash
git clone https://github.com/GdeiAssistant/GdeiAssistant-Android.git
cd GdeiAssistant-Android
```

### 构建环境

- Android Studio 最新稳定版
- JDK 17
- Android SDK 35

### 本地编译

```bash
./gradlew compileDebugKotlin
```

## 数据接口

安卓端使用的后端接口由 [GdeiAssistant](https://github.com/GdeiAssistant/GdeiAssistant) 提供。

- 项目主页：[广东第二师范学院校园助手系统](https://github.com/GdeiAssistant/GdeiAssistant)
- 接口文档：[Wiki](https://github.com/GdeiAssistant/GdeiAssistant/wiki)

## 协议

- [MIT License](http://opensource.org/licenses/MIT)
- [Anti 996 License](https://github.com/996icu/996.ICU/blob/master/LICENSE)

Copyright (c) 2016 - 2026 GdeiAssistant

## 贡献

- 欢迎提交 Issue 反馈问题或建议
- 欢迎 Fork 后提交 Pull Request
- 如果项目对你有帮助，欢迎 Star

## 联系

- 技术支持和意见建议反馈：[gdeiassistant@gmail.com](mailto:gdeiassistant@gmail.com)
- 用户客服和系统故障工单提交：[support@gdeiassistant.cn](mailto:support@gdeiassistant.cn)
- 社区违法和不良信息举报邮箱：[report@gdeiassistant.cn](mailto:report@gdeiassistant.cn)

## 声明

本项目仅供学习与研究使用，请遵循学校与相关平台的使用规范。
