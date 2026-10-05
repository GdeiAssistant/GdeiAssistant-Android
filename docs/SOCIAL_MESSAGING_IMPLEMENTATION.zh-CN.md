# Android 社交与私信实现说明

日期：2026-10-05。基线 HEAD：`c54a37f`（本说明随图片私信追加更新，未 commit）。

## 范围

本仓实现共享契约（`GdeiAssistant/docs/SOCIAL_MESSAGING_DESIGN.zh-CN.md`，含「图片私信追加契约」）的 Android 客户端接入：

- Retrofit `SocialApi` + `SocialRepository` + Debug-only `MockSocialProvider`
- 全局单连接 `SocialRealtimeManager`（OkHttp WebSocket；mock 走本地事件流）
- WS URL 纯函数 `SocialRealtimeUrls`：由 http(s) base 构造 ws/wss 字符串，不经 `HttpUrl.scheme(ws)`，路径不重复 `/api`，不携带 token
- 连接世代/`token` 校验：connecting/authenticating/ready 期间不重复重开；旧 socket 回调全部无效
- 前台恢复 REST/重连补拉：`SocialSessionCoordinator` + `MainActivity` STARTED；未读回写校验当前账号+token
- 登录建立连接、退出/`Unauthorized` 清理；后台隐藏页停轮询；logout/换 token 清理私信临时图目录；聊天页离场清理本页图片、使未完成的选图/REST 回调失效
- 消息 merge 双索引（serverId + conversation/sender/clientId）；已提交优先；HTTP 超时不降级已 SENT；跨发送者同 UUID 不覆盖；merge 保留 `type`/`image`/`localImagePath`
- Mock 社交路由 HTTP 状态与 payload `code` 对齐并保留 `errorCode`；已提交同 `clientMessageId` 在隐私收紧后仍返回原消息
- 图片鉴权：公开外链直达；仅同源 `/api/social/users/{uuid}/avatar` 与 `/api/social/conversations/{id}/messages/{messageId}/image` 加 Bearer；`avatarUrl=null` 占位不假造 URL；鉴权图片禁用 disk/memory 缓存
- 图片私信：`POST …/messages/image` multipart(`clientMessageId`+`image`)；`Conversation.imageMessagingEnabled` 缺省 false；选图用 `PickVisualMedia`；读取系统 EXIF Orientation 1..8 旋正像素后重新编码 JPEG，规范化 ≤5MiB/4096；发送前预览/取消；IMAGE 气泡与 Dialog 完整看图；pending/failed/sent；同 ID 原 bytes 重试不因新 canSend 误挡确认
- Mock 二进制解析真实 multipart（包括 Retrofit 的 text/plain `clientMessageId` Part），按 SHA-256 幂等；检查 JPEG/PNG 头并返回真实宽高和 MIME。GET 要求非空演示 Bearer、校验会话/消息归属，返回存储的图片字节及 `Content-Type`、`Cache-Control: private, no-store`、`X-Content-Type-Options: nosniff`；不以合成图片替代缺失文件
- 页面：用户搜索、公开主页、关注/粉丝/好友、黑名单、会话列表（头像/时间/未读/图片摘要）、聊天（头像头栏、气泡、选图、固定输入/IME）
- 公开社区作者 `authorId` 主页链接：话题、二手、卖室友（发布者非被介绍人物）、失物、拍好校园；匿名树洞/匿名表白不下发/不接作者入口
- 六种语言文案；Release 强制 remote（沿用 `SettingsRepository.canUseDemoMode()`）

## 关键路径

| 层级 | 路径 |
|---|---|
| 契约模型 | `app/src/main/java/cn/gdeiassistant/model/SocialGraphModels.kt` |
| API | `app/src/main/java/cn/gdeiassistant/network/api/SocialApi.kt` |
| 仓储 | `app/src/main/java/cn/gdeiassistant/data/SocialRepository.kt` |
| 图片规范化/临时缓存 | `SocialChatImageSupport.kt` / `SocialChatImageCache` |
| 可独立 JVM 验证的图片头/摘要 | `app/src/main/java/cn/gdeiassistant/data/SocialChatImageMetadata.kt` |
| 文本校验 | `app/src/main/java/cn/gdeiassistant/data/SocialTextSupport.kt` |
| 消息去重 | `app/src/main/java/cn/gdeiassistant/data/ChatMessageMerge.kt` |
| Mock | `app/src/main/java/cn/gdeiassistant/network/mock/MockSocialProvider.kt` |
| 实时 | `SocialRealtimeManager.kt` / `SocialRealtimeUrls.kt` |
| 头像/私信图 URL+鉴权 | `SocialAvatarUrls` / `SocialChatImageUrls` / `SocialImageAuthSupport` |
| UI | `app/src/main/java/cn/gdeiassistant/ui/social/` |
| 测试 | 见下方「验证」 |

## 行为要点

- 关系：单向关注、粉丝反向、好友=互关；无好友申请；PUT/DELETE follow 幂等。
- 私信权限按接收方 `dmPolicy`；FOLLOWING 检查 B→A；未知策略默认 MUTUAL。
- 发送：`clientMessageId` 本地幂等；pending/sent/failed；失败可同 ID 重试；文字 Unicode 1–1000；图片比 SHA-256。
- 消息分页：`beforeSeq`/`afterSeq` 互斥；已读仅前进。
- realtime：首帧 auth JWT → ready 后才收业务事件；logout/换 token 使旧回调无效。
- 图片：不请求整库相册权限；不落公开外链泄漏；不把 Bearer 放进 URL/缓存 key。

## 未由本仓承担

- 后端实现、数据库迁移、Web/小程序/iOS
- 真实校园登录、充值、远端部署、commit/push/PR
- 本机 Android SDK 安装与设备验证

## 验证

优先（有 SDK 时）：

```
./gradlew :app:testDebugUnitTest \
  --tests cn.gdeiassistant.network.SocialRealtimeUrlsTest \
  --tests cn.gdeiassistant.network.SocialAvatarUrlsTest \
  --tests cn.gdeiassistant.network.SocialChatImageUrlsTest \
  --tests cn.gdeiassistant.network.SocialImageAuthSupportTest \
  --tests cn.gdeiassistant.network.SocialImageApiContractTest \
  --tests cn.gdeiassistant.data.ChatMessageMergeTest \
  --tests cn.gdeiassistant.data.SocialChatImageSupportTest \
  --tests cn.gdeiassistant.network.mock.MockSocialProviderTest \
  --tests cn.gdeiassistant.network.mock.MockSocialProviderHttpAndIdempotencyTest \
  --tests cn.gdeiassistant.data.SocialTextSupportTest \
  --tests cn.gdeiassistant.data.SocialRepositoryContractTest
```

缺 SDK/`local.properties` 时记录环境阻塞，不以设备/完整 build 通过宣称；测试源码已落地。

`.gitignore` 对 `docs/` 增加精确例外：`docs/SOCIAL_MESSAGING_IMPLEMENTATION.zh-CN.md`。

## 本轮交付边界

- 已实现：图片私信契约字段/端点、选图预览、气泡看图、merge 保留 image、mock 真实 multipart、六语言摘要文案、logout/换 token/页退场清临时图。补充修正 bounds-only decode 返回 null 被误判为失败，以及接近 4096px 的缩放边界。
- 未验证：完整 Android App build、Compose 类型检查、模拟器/真机键盘与图片预览（本机缺 Android SDK）。
- 2026-10-06 本机确认：`adb`、`emulator`、`sdkmanager` 缺失，两个 SDK 环境变量未设，默认 SDK 目录和 `local.properties` 均不存在。
- 纯 JVM 验证脚本：`/tmp/gdei-chat-images-20261005/android-pure.py`。直接编译实际生产模型、API DTO/注解、URL/鉴权判定、图片头/摘要、消息合并和 mock，再执行 JUnit；只沿用 Compose `Immutable` / `BuildConfig` 编译标记，不替代业务或 Bitmap 实现。其图片支持测试验证纯 `SocialChatImageMetadata`，不包含 Android 图片解码、EXIF/Matrix、Photo Picker、ViewModel 生命周期或 Compose。
- 最新实际执行结果：`OK (52 tests)`；编译与测试记录分别为该临时目录下 `android-pure/compile.log` 和 `android-pure/test.log`。本仓未执行完整 Gradle/设备测试。
- 此独立检查使用 Java 17、Kotlin 2.3.21、JUnit 4.13.2、Retrofit 3.0.0，以及已缓存的 Gson 2.13.2 / OkHttp 4.12.0 / Okio 3.6.0 / coroutines 1.9.0；不能代替仓库声明版本的完整 Gradle 构建。测试涵盖 MIME/尺寸限制、真实 multipart 字节、同 ID 原图在隐私收紧后的重试、已提交优先、精确同源 Bearer、mock 图片 HTTP 响应头与字节、DTO 缺字段默认和 Retrofit 字段名。
- 六种语言 `strings.xml` 实际 XML 解析通过；新增图片文案均齐全。固定合成 JPEG 由真实 JVM ImageIO 解码确认，不将缺失文件伪装成成功。
- 2026-10-06 Dot 云端补充：在 Debian 13 Linux 隔离目录安装并校验 Gradle 9.6.1、Android CLI 22.0、官方 `platforms;android-37.0` revision 2 与 Build-Tools 36.0.0。用户确认的 2019-01-16 SDK 许可按准确正文接受，组件安装退出 0；未改变源码、依赖、compileSdk 或系统网络设置。
- 该环境首次 `lintDebug`、`testDebugUnitTest` 均在根项目 AGP 9.3.1 插件解析阶段退出 1，实际单测 0 项；Google 官方 AGP 与插件 marker POM 在 Dot 均可 HTTP 200 读取。探针确认 JVM 未使用现有代理后，仅对隔离进程接入同一代理，启动一次 `lintDebug testDebugUnitTest assembleDebug`。随后结果读取被工具自动审批取消，未获得明确拒绝理由，也未获得退出码；句柄 71771 的终态未知，可能仍运行，未重启或终止。因此完整 Gradle 构建、compileSdk 绑定与设备 UI 仍未验证通过。
- 主助手已读回第 3 版阶段报告：ZIP CRC 与 307 个文件 SHA-256 全部匹配，SDK 安装记录、原始失败日志和组合命令现有启动日志相符；原 Android 354 个源码文件校验无差异。恢复后应先只读核对原命令终态与保存日志，再决定后续验证。
