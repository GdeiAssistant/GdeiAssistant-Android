# 参与贡献

这个仓库是 GdeiAssistant 的 Android 客户端。感谢你愿意花时间改进它。

提交之前先做一件事：确认你要改的是客户端，还是后端。成绩、课表、校园卡这类数据来自 [GdeiAssistant 主仓](https://github.com/GdeiAssistant/GdeiAssistant)，接口不对、数据不对，问题通常在后端。

## 流程

1. 从最新的 `master` 拉出功能分支。
2. 改代码，同步改受影响的文档和测试。
3. 跑下面的检查，全部通过再提交。
4. 开 PR，说明改了什么、影响哪些页面、怎么验证的。

分支名带上主题，例如 `feat/dark-theme-tokens`、`fix/card-balance-parse`。

## 提交前检查

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
python3 scripts/check_locale_resources.py
```

需要跑真机/模拟器用例时：

```bash
./scripts/run-emulator-tests.sh
```

改动了多语言文案，必须同时更新 `values-*/strings.xml` 和 `docs/location-names.md` 里对应的条目。文案对不上，`check_locale_resources.py` 会报错。

## 代码风格

- 跟着现有代码走：Kotlin 官方风格，Compose 页面放在 `ui/<域>/`，别把业务逻辑写进 Composable。
- 新页面挂到已有的四张导航图（`service` / `community` / `information` / `account`）里，不要另开顶层导航。
- 不引入新框架或重依赖，除非现有方案确实做不了。引入前在 PR 里说清楚理由和体积影响。
- 网络请求走 `network/` 的 Repository，不要在 ViewModel 里直接调 Retrofit。

## 关于 mock 数据

`mock` 只服务本地开发和 UI 联调。不要往 mock 里塞真实校园数据、真实用户信息或可用的测试账号。Release 构建会强制走 `remote`，mock 分支不应该被当作正式能力。

## 不要提交的内容

- 真实用户数据、真实校园账号、密码、验证码、Cookie、Token
- keystore 文件、`local.properties`、`google-services.json` 等本机或签名配置
- 未脱敏的日志、抓包、截图
- 第三方 API Key

## 提交信息

用能看懂的话写，不要只写「update」：

```text
feat: 成绩页支持按学期筛选
fix: 校园卡余额在弱网下显示为 0
docs: 补充 release 签名步骤
```

## 许可

向本仓库提交的代码和文档，默认按 [Apache License 2.0](LICENSE) 授权。提交前请确认你有权提交这些内容。

## AI 辅助维护

维护流程、文档更新和交付核对见 [AI 辅助维护约定](.github/engineering.md)，项目约束见 [AGENTS.md](AGENTS.md)。
