# 广东二师助手 · Android 客户端

广东第二师范学院校园助手的安卓客户端，Kotlin + Jetpack Compose 编写。当前版本 `2.0.0-PRO`。

这个仓库只有客户端。成绩、课表、校园卡这些数据来自 [GdeiAssistant 后端](https://github.com/GdeiAssistant/GdeiAssistant)，本仓库不包含服务端代码和部署配置。项目由学生维护，不是学校官方服务。

- [功能](#功能)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [接口环境](#接口环境)
- [工程结构](#工程结构)
- [架构说明](#架构说明)
- [参与贡献](#参与贡献)

## 功能

| 模块 | 内容 |
| --- | --- |
| 教务 | 成绩、课表、四六级、考研、教室、教学评价、图书馆 |
| 校园卡 | 余额查询、充值、挂失、电费 |
| 生活 | 二手交易、全民快递、失物招领、校园树洞、卖室友、表白墙、校园话题、拍好校园 |
| 资讯 | 新闻通知、系统公告、互动消息、私信会话 |
| 社交 | 用户搜索与公开主页、关注/粉丝/互关、拉黑、四档私信接收设置 |
| 个人中心 | 登录与登录态恢复、资料与头像、绑定手机/邮箱、隐私设置、登录记录、数据导出、主题与字体、意见反馈 |

匿名树洞不暴露作者身份；社区作者主页只在非匿名内容上显示入口。

## 技术栈

Kotlin · Jetpack Compose + Material 3 · Hilt · Retrofit + OkHttp + Gson · DataStore · EncryptedSharedPreferences · Coil · JWT

## 快速开始

### 环境要求

| 项 | 版本 |
| --- | --- |
| Android Studio | 最新稳定版 |
| JDK | 17 |
| Gradle | 9.6.1（使用仓库自带 wrapper） |
| Android Gradle Plugin | 9.3.1 |
| Kotlin | 2.3.21 |
| Android SDK | 35 |
| 设备 | Android 8.0 及以上 |

### 构建并安装 Debug 包

```bash
git clone https://github.com/GdeiAssistant/GdeiAssistant-Android.git
cd GdeiAssistant-Android
./gradlew :app:assembleDebug
./gradlew :app:installDebug
```

APK 输出在 `app/build/outputs/apk/debug/app-debug.apk`。

首次用命令行构建前，需要先生成 `local.properties` 并写入 `sdk.dir`。最简单的办法是用 Android Studio 打开一次项目完成 Gradle Sync，它会自动生成这个文件。

### 构建 Release 包

走 CI：在 Actions 页打开 [Android Release](https://github.com/GdeiAssistant/GdeiAssistant-Android/actions/workflows/android-release.yml)，选 **Run workflow**，填 `tag_name`、`version_name`、`version_code`，可勾选 prerelease。工作流先跑 lint 和单元测试，再执行 `assembleRelease` 与 `bundleRelease`，成功后把签名产物发到 [Releases](https://github.com/GdeiAssistant/GdeiAssistant-Android/releases)。

签名所需配置（仓库 secret 或 environment）：

| 名称 | 内容 |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | release keystore 的 base64 |
| `ANDROID_KEYSTORE_PASSWORD` | keystore 密码 |
| `ANDROID_KEY_ALIAS` | key alias |
| `ANDROID_KEY_PASSWORD` | key 密码 |

本地签名构建：

```bash
./gradlew :app:assembleRelease \
  -PGDEI_RELEASE_STORE_FILE=/path/to/release-keystore.jks \
  -PGDEI_RELEASE_STORE_PASSWORD="$ANDROID_KEYSTORE_PASSWORD" \
  -PGDEI_RELEASE_KEY_ALIAS="$ANDROID_KEY_ALIAS" \
  -PGDEI_RELEASE_KEY_PASSWORD="$ANDROID_KEY_PASSWORD" \
  -PGDEI_VERSION_NAME=2.0.0-PRO \
  -PGDEI_VERSION_CODE=2
```

## 接口环境

| 环境 | 地址 | 用途 |
| --- | --- | --- |
| `dev` | `http://10.0.2.2:8080/` | 模拟器连本机后端 |
| `staging` | `https://gdeiassistant.azurewebsites.net/` | 演示环境 |
| `prod` | `https://gdeiassistant.cn/` | 生产环境 |

Debug 构建可以在应用内切换环境和数据源；Release 构建固定 `remote` + `prod`，没有开关。命令行覆盖地址：

```bash
./gradlew :app:assembleDebug \
  -PGDEI_BASE_URL_DEV=http://10.0.2.2:8080/ \
  -PGDEI_BASE_URL_STAGING=https://gdeiassistant.azurewebsites.net/ \
  -PGDEI_BASE_URL_PROD=https://gdeiassistant.cn/
```

## 工程结构

```text
app/src/main/java/cn/gdeiassistant/
├── data/          本地存储与仓库层
├── di/            Hilt 模块
├── model/         领域模型
├── network/       Retrofit 接口与 mock 路由
├── service/       后台服务
├── ui/            按业务域拆分的 Compose 页面
│   ├── home/ messages/ social/ profile/
│   ├── grade/ schedule/ card/ book/
│   ├── marketplace/ lostfound/ secret/ dating/
│   ├── delivery/ express/ topic/ photograph/
│   └── navigation/
└── util/
```

## 架构说明

### 数据源

`remote` 请求真实接口，`mock` 使用本地模拟数据。默认 `DEFAULT_MOCK_MODE_ENABLED=false`，即默认连真实后端。只有 Debug 构建能在关于页切换 mock；Release 构建被 `SettingsRepository.canUseDemoMode()` 和 `canChangeNetworkEnvironment()` 锁死为 `remote` + `prod`。

mock 数据只用于 UI 联调和回归验证，不是真实校园业务数据。测试账号密码不要写进公开发行说明。

### 登录态

四个组件分工：`SessionManager` 管当前用户、Token 和 Cookie 会话；`TokenUtils` 用 `EncryptedSharedPreferences` 做本地加密存储；`AuthInterceptor` 注入鉴权头；`ResponseInterceptor` 统一处理错误。

### 网络层

Retrofit + OkHttp 负责 API Service 注入、Bearer Token 注入、Cookie 持久化与线程安全、mock 路由分发、Gson 序列化，以及错误归一化。

### UI 与导航

底部三个 Tab：首页、消息、我的。业务页面按域组织成 `service`、`community`、`information`、`account` 四张导航图，方便各域独立迭代。

### 主题

固定 Campus Green 品牌色，与 Web / iOS / 小程序四端一致，不使用 Material You 动态取色。用户可选浅色/深色/跟随系统，字体四档可调，偏好存在独立的 `user_preferences` DataStore 里。

## 社交与私信

客户端实现见 [`docs/social-messaging-implementation.zh-CN.md`](docs/social-messaging-implementation.zh-CN.md)。数据契约以主仓设计文档为准。

## 参与贡献

见 [CONTRIBUTING.md](CONTRIBUTING.md)。安全问题请按 [SECURITY.md](SECURITY.md) 处理，不要在公开 Issue 里贴真实账号、验证码或密钥。

## 许可

[Apache License 2.0](LICENSE)。

## 免责声明

本项目不代表学校官方服务。面向用户的协议、隐私政策和功能风险提示，以 App 内「关于」「隐私」「校园凭证」等页面展示的内容为准，本 README 不替代这些正文。发布衍生版本前，请自行确认隐私处理、校园凭证管理、Android 权限说明、日志脱敏、密钥管理、签名配置和商标使用边界。
