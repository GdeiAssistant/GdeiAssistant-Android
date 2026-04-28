# 广东二师助手安卓客户端

广东二师助手安卓客户端，基于 Kotlin 与 Jetpack Compose 构建，面向广东第二师范学院校园场景，围绕教务查询、校园服务、社区互动与账号管理提供统一的 Android 端体验。

当前版本：`2.0.0-PRO`

## 功能概览

### 校园服务

- 成绩查询
- 课表查询
- 四六级查询
- 考研查询
- 教室查询
- 教学评价
- 图书馆
- 校园卡
- 校园卡充值
- 校园卡挂失
- 电费查询
- 数据中心
- 校园黄页

### 校园生活

- 二手交易
- 全民快递
- 失物招领
- 校园树洞
- 卖室友
- 表白墙
- 校园话题
- 拍好校园

### 资讯信息

- 新闻通知
- 系统通知公告
- 互动消息

### 个人中心

- 登录与登录态恢复
- 资料展示与编辑
- 头像管理
- 绑定手机
- 绑定邮箱
- 隐私设置
- 登录记录
- 用户数据导出与下载
- 意见反馈
- 界面和外观（主题切换、字体大小、语言）
- 关于应用
- 退出登录

## 技术栈

- Kotlin
- Jetpack Compose + Material 3
- Hilt
- Retrofit + OkHttp + Gson
- DataStore
- EncryptedSharedPreferences
- Coil
- JWT 登录态管理

## 工程结构

```text
GdeiAssistant-Android/
├── app/src/main/java/cn/gdeiassistant/
│   ├── data/
│   ├── di/
│   ├── model/
│   ├── network/
│   │   ├── api/
│   │   └── mock/
│   ├── service/
│   ├── ui/
│   │   ├── home/
│   │   ├── messages/
│   │   ├── profile/
│   │   ├── grade/
│   │   ├── schedule/
│   │   ├── card/
│   │   ├── book/
│   │   ├── marketplace/
│   │   ├── lostfound/
│   │   ├── secret/
│   │   ├── dating/
│   │   ├── delivery/
│   │   ├── express/
│   │   ├── topic/
│   │   ├── photograph/
│   │   └── navigation/
│   └── util/
├── app/src/main/res/
└── github/
```

## 架构说明

### 1. 数据源模式

项目支持两种数据源：

- `remote`：请求真实后端接口
- `mock`：使用本地模拟数据

应用默认开启 `mock` 模式，便于本地联调和页面验证。切换入口位于应用内关于页，切换后建议重启应用以确保网络层与页面状态全部刷新。

本地示例登录能力仅用于开发调试、界面验证与联调演示，不应在公开发行说明中暴露测试账号或密码。

### 2. 登录态与安全存储

当前登录链路由以下组件协作完成：

- `SessionManager`：统一管理当前用户、Token 与 Cookie 会话
- `TokenUtils`：基于 `EncryptedSharedPreferences` 的本地安全存储
- `AuthInterceptor`：注入鉴权头并处理登录态请求
- `ResponseInterceptor`：统一响应拦截与错误处理

### 3. 网络层

网络层基于 Retrofit 与 OkHttp，统一负责：

- API Service 注入
- Bearer Token 注入
- Cookie 持久化与线程安全管理
- Mock 路由分发
- Gson JSON 序列化与反序列化
- 统一错误归一化处理

### 4. UI 与导航

应用界面基于 Jetpack Compose 构建，当前主导航已经拆分为：

- `service graph`
- `community graph`
- `information graph`
- `account graph`

业务页面按模块拆分在 `ui/` 目录下，便于逐步迭代和独立维护。

### 5. 主题系统

应用支持亮色/暗色主题切换，默认跟随系统：

- Material You 动态取色（Android 12+），低版本回退到 Campus Green 静态色板
- 用户可在"界面和外观"页面手动选择浅色/深色/跟随系统
- 字体大小四档可调（小/标准/大/超大）
- 主题与字体偏好通过独立 DataStore (`user_preferences`) 持久化

## 运行环境

- Android Studio 最新稳定版
- JDK 17
- Gradle 9.3.1
- Android Gradle Plugin 9.1.0
- Kotlin 2.3.20
- Android SDK 35
- Android 8.0 及以上设备或模拟器

## 快速部署

### 1. 克隆仓库

```bash
git clone https://github.com/GdeiAssistant/GdeiAssistant-Android.git
cd GdeiAssistant-Android
```

### 2. 准备本地环境

- 使用 Android Studio 打开项目并完成 Gradle Sync
- 确保本机已安装 Android SDK 35 与 Build Tools
- 若命令行环境尚未生成 `local.properties`，请先通过 Android Studio 打开一次项目，或手动补充本地 `sdk.dir`

### 3. 构建 Debug 安装包

```bash
./gradlew :app:assembleDebug
```

构建成功后，APK 默认输出到：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 4. 安装到设备

```bash
./gradlew :app:installDebug
```

### 5. 运行方式

- `mock` 模式：适合本地开发、UI 联调、回归验证
- `remote` 模式：适合连接真实后端接口进行联调

### 6. 远程接口环境

应用内远程接口默认按 `dev / staging / prod` 三套环境运行：

- `dev`：`http://10.0.2.2:8080/`（Android 模拟器本地联调）
- `staging`：`https://gdeiassistant.azurewebsites.net/`
- `prod`：`https://gdeiassistant.cn/`

也可以在 Gradle 命令行覆盖：

```bash
./gradlew :app:assembleDebug \
  -PGDEI_BASE_URL_DEV=http://10.0.2.2:8080/ \
  -PGDEI_BASE_URL_STAGING=https://gdeiassistant.azurewebsites.net/ \
  -PGDEI_BASE_URL_PROD=https://gdeiassistant.cn/
```

## 后端接口位置

本项目对应的后端仓库为：

- GitHub：`https://github.com/GdeiAssistant/GdeiAssistant`
- Wiki：`https://github.com/GdeiAssistant/GdeiAssistant/wiki`

## 开源协议

本项目采用 [Apache License 2.0](LICENSE) 开源协议。

你可以在遵守协议条款的前提下使用、修改和分发本项目代码。

## 法律、隐私与安全提示

1. 本项目为校园场景 Android 客户端，不代表学校官方服务。
2. 面向用户的正式协议、隐私政策和功能风险提示，以 App 内关于、隐私、校园凭证、二手、跑腿/快递、失物招领等页面展示内容为准。
3. 仓库中的 `mock` 数据仅用于本地开发、界面验证和联调演示，不代表真实校园业务数据。
4. 生产发布前，发布方应结合实际环境审查隐私处理、校园凭证管理、Android 权限说明、第三方服务、日志脱敏、密钥管理、签名配置以及商标/名称使用边界。
5. 本 README 只保留维护和部署层面的合规提示，不替代用户协议或隐私政策正文。
