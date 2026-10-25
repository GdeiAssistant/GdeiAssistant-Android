# 安全说明

这是 GdeiAssistant 的 Android 客户端。服务端漏洞、账号体系问题请报到 [主仓](https://github.com/GdeiAssistant/GdeiAssistant)；这里只处理客户端本身的问题。

## Reporting a vulnerability

Report security vulnerabilities through [GitHub private vulnerability reporting](https://github.com/GdeiAssistant/GdeiAssistant-Android/security/advisories/new). Do not post exploit details, credentials, private sessions or personal data in public issues or PRs. Include the affected version, impact and minimal redacted reproduction. The maintainer handles reports on a best-effort basis; no bounty or response-time guarantee is implied.

## 客户端特有的风险面

| 位置 | 风险 |
| --- | --- |
| `EncryptedSharedPreferences`（`TokenUtils`） | 设备被 root 或备份被导出时，Token 可能被读取 |
| `SessionManager` | 换号登录时旧请求的失败响应不应清除新会话 |
| mock 数据源 | 只在 Debug 生效；Release 构建强制 `remote` + `prod` |
| 构建产物 | 签名 keystore 泄露等于任何人都能发「官方」更新 |
| 日志与崩溃上报 | 可能带出校园账号、Cookie、请求体 |

签名 keystore、`local.properties` 和任何 `google-services.json` 都不该进仓库。如果你在提交历史里看到了，请告诉我们，别在 Issue 里复述具体内容。

## 测试边界

请只在你自己的账号和你自己的设备上测试。

不要做这些事：

- 攻击学校相关系统、第三方系统或与本项目对接的外部服务
- 获取或尝试获取他人数据
- 撞库、爆破、批量请求、绕过验证码、批量爬取、拒绝服务
- 对生产环境 `https://gdeiassistant.cn/` 做影响可用性的测试

发现自己的凭据泄露，先改密码并退出所有设备，再联系维护者。

## 支持范围

安全修复面向当前 `master` 分支和仍在维护的正式版本。旧的衍生版本不保证回溯修复。

## 响应

维护者会在合理时间内评估报告。本项目没有赏金计划，也没有固定 SLA。
