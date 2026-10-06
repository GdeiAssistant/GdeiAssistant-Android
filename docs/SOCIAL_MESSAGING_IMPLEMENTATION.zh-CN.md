# Android 社交与私信实现说明

日期：2026-10-06。图片实现原始基线：`c54a37f`；本轮模拟器测试补充基线：`98a9c2ceb48e03caedb95c752bfadd26bed1c3c2`。

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
- 图片实现阶段本机执行结果：`OK (52 tests)`；编译与测试记录分别为该临时目录下 `android-pure/compile.log` 和 `android-pure/test.log`。该检查不包含完整 Gradle/设备测试。
- 此独立检查使用 Java 17、Kotlin 2.3.21、JUnit 4.13.2、Retrofit 3.0.0，以及已缓存的 Gson 2.13.2 / OkHttp 4.12.0 / Okio 3.6.0 / coroutines 1.9.0；不能代替仓库声明版本的完整 Gradle 构建。测试涵盖 MIME/尺寸限制、真实 multipart 字节、同 ID 原图在隐私收紧后的重试、已提交优先、精确同源 Bearer、mock 图片 HTTP 响应头与字节、DTO 缺字段默认和 Retrofit 字段名。
- 六种语言 `strings.xml` 实际 XML 解析通过；新增图片文案均齐全。固定合成 JPEG 由真实 JVM ImageIO 解码确认，不将缺失文件伪装成成功。
- 2026-10-06 Dot 云端补充：在 Debian 13 Linux 隔离目录安装并校验 Gradle 9.6.1、Android CLI 22.0、官方 `platforms;android-37.0` revision 2 与 Build-Tools 36.0.0。用户确认的 2019-01-16 SDK 许可按准确正文接受，组件安装退出 0；未改变源码、依赖、compileSdk 或系统网络设置。
- 该环境首次 `lintDebug`、`testDebugUnitTest` 均在根项目 AGP 9.3.1 插件解析阶段退出 1，实际单测 0 项；Google 官方 AGP 与插件 marker POM 在 Dot 均可 HTTP 200 读取。探针确认 JVM 未使用现有代理后，仅对隔离进程接入同一代理，启动一次 `lintDebug testDebugUnitTest assembleDebug`。随后结果读取被工具自动审批取消，未获得明确拒绝理由，也未获得退出码；句柄 71771 的终态未知，可能仍运行，未重启或终止。因此完整 Gradle 构建、compileSdk 绑定与设备 UI 仍未验证通过。
- 主助手已读回第 3 版阶段报告：ZIP CRC 与 307 个文件 SHA-256 全部匹配，SDK 安装记录、原始失败日志和组合命令现有启动日志相符；原 Android 354 个源码文件校验无差异。恢复后应先只读核对原命令终态与保存日志，再决定后续验证。

## 2026-10-06 图片私信模拟器测试补充

- 新增 `MockSocialChatImageUiTest` 四项仪器测试，沿用现有 Compose/JUnit 框架。测试运行时生成 320×240 四色 PNG，通过 `MediaStore` 写入模拟器相册，操作实际系统 Photo Picker；只有远端服务使用既有 in-memory mock，不伪造 picker 回调。
- 覆盖系统选图取消、预览渲染及移除、真实规范化 JPEG 上传、鉴权图片气泡与完整看图；断言实际图片尺寸、存储字节和 SHA-256，并从 Compose 所在 Android 窗口采样已渲染的合成图片颜色。
- 一次性 mock HTTP 503 验证发送失败；收紧隐私后重试被拒，再恢复互关权限后使用原 `clientMessageId` 和原 bytes 重试成功，服务端仅一条消息。隐私收紧后历史已发送图片仍可查看。离开聊天页后，本页失败消息及临时文件清理，重新进入不显示残留。
- 该流程暴露刷新历史丢失失败消息的问题，已修正 `ChatMessageMerge.merge(replace=true)`：保留未经服务端确认的本地 pending/failed；同 client key 的服务端确认仍替换占位。新增两项消息合并回归及一项 mock 单次失败/重试回归。
- 本机实际运行 `python3 /tmp/gdei-chat-images-20261005/android-pure.py`，直接编译本轮实际纯 production 源码并执行 `OK (55 tests)`；沿用上方标记和依赖边界。`git diff --check`、`bash -n scripts/run-emulator-tests.sh` 及两个 workflow 的 YAML 解析通过。脚本控制流另用临时合成命令验证：即使诊断采集失败，Gradle 原退出码 0/37 均保留；这不代表 Gradle 或模拟器执行成功。
- 本轮本机再次确认无 SDK、`adb`、`emulator`、`sdkmanager` 或 `local.properties`，未执行 instrumentation/Compose 编译。新增四项测试的实际结果须以随后精确提交的 CI 为准。
- PR 的 Android CI 继续在 API 35 / Google APIs / x86_64 / Pixel 6 执行全部 `connectedDebugAndroidTest`（原四项 smoke 加新增四项）。入口为 `bash scripts/run-emulator-tests.sh`，退出前采集截图/可访问性树和 logcat，上传到 `android-instrumentation-reports` 的 `android-ui-evidence/`、`android-emulator-logcat.log`，同时保留 Gradle 报告。
- 签名发布 workflow 只补与已验证 CI 相同的 `platforms;android-37.0` 安装步骤，放在 release secrets 检查之后；未修改签名或执行发布。
- 仍需实际设备确认 OEM/旧版 picker、相机/HEIC 输入、键盘交互及真实服务环境；本轮相册仅使用合成 PNG，退出测试时删除该测试创建的 URI。

### 首次真实 CI 结果与定向返修

- 提交 `97172915d05d237836f6cd740b60dcfa036ab030` 的 CI run `37415567394`：原四项 smoke 通过，新增四项均在系统窗口检测报 `Photo picker did not open`。实际 logcat 四次记录 `PICK_IMAGES` 启动及 `com.google.android.providers.media.module/...PhotoPickerActivity` 显示，用时 829–1207ms；因此应用已经启动系统 picker，未增加超时或替换/跳过系统选图。
- 窗口检测由仅读取 `rootInActiveWindow` 改为启用 `FLAG_RETRIEVE_INTERACTIVE_WINDOWS` 并遍历交互窗口、检查实际节点包名。返回应用还必须确认 picker 窗口已消失，防止把透明窗口后面的应用误判为已返回；原 20 秒限制保留。首次未取得控件树，具体活动根节点状态仍待新 CI 的窗口记录确认。
- 首次 `adb pull` 对应用私有外部目录返回不存在；真实 applicationId 和日志均为 `cn.gdeiassistant`，不是包名猜错。本轮改用 `MediaStore.Downloads` 保存到 `Download/GdeiSocialUiEvidence`，避免应用清理丢掉文件。截图装入 ZIP，防止诊断 PNG 混入后续系统相册；CI 拉取后解压，窗口树单独保存，诊断写入错误及实际窗口包名写入 logcat。
- 本机通过 `git diff --check`、脚本语法检查；临时合成命令实际验证新采集路径、ZIP 解压和保留原退出码 0/37。本轮不改生产业务，未重复纯 JVM 测试；修正后的实际窗口检测、选图与截图保存须由新精确提交的 API 35 CI 验证。

### 第二次 CI 结果与断言修正

- 提交 `dead5b4fadc4002ab3fe3fee5592340bb13e71da` 的 run `37416746107`：八项仪器测试中五项通过，包含实际系统选图、真实图片 bytes 上传、气泡渲染和完整查看器；截图/窗口树已成功采集。已通过的窗口遍历、上传与查看实现保持不变。
- 两项失败在 `waitForRetry`：XML 与窗口树显示实际文案为「发送失败，可重试」，而 `assertTextContains("发送失败")` 缺少 `substring=true`，按完整文本匹配失败。修正为明确的子串断言，继续检查原消息失败状态及重试控件。
- 取消项失败在 `pressSystemBack` 的 DOWN 事件；同期 logcat 明确 `Dropped event because it is stale`，截图仍是系统 picker。返回键改用当前 `SystemClock.uptimeMillis()`，DOWN/UP 共用同一 `downTime`，保留真实系统事件和取消/清理断言。未改生产业务或增加等待；本机差异检查通过，修正后的三项须由下一精确提交的模拟器 CI 验证。
- 实际发送截图还显示原始 ISO 纳秒时间。显示层增加局部 `formatSocialTime`，沿用项目已有 `java.time`，将会话列表、聊天头部及已发送消息时间按设备时区转成 `yyyy-MM-dd HH:mm`；原始字段、消息排序与 API 不变，不新增依赖。无效或空字符串保留原值，便于沿用已发送占位。实际 helper 的 JVM 示例验证中国/纽约时区、跨日、带偏移时间和空/无效输入；UI 渲染仍由下一提交 CI 验证。
