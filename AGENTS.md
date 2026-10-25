# AGENTS.md

## 项目是什么

广东第二师范学院校园助手的安卓客户端。Kotlin + Jetpack Compose。

## 技术栈

Kotlin，Jetpack Compose，Gradle（`build.gradle.kts` + `settings.gradle.kts`）。

## 目录

- `app/` — 应用模块。
- `design/` — 设计令牌来源。
- `docs/` — `location-names.md` 说明地点名称来源。
- `scripts/` — 构建辅助脚本。

## 本地验证

按改动选择相关离线检查，命令以仓库当前 CI 为准。先区分代码问题与环境条件，不为通过检查降低保护。

**单元测试**

```bash
./gradlew :app:testDebugUnitTest
```

**Lint**

```bash
./gradlew :app:lintDebug
```

**编译**

```bash
./gradlew :app:assembleDebug
```

## 命名与文档

- 新增文档文件名小写、`-` 连接，中文版后缀 `.zh-CN.md`；`README.md`、`AGENTS.md` 等工具约定文件保留固定名称。源码沿用现有语言命名。
- 不提交审计报告、复盘、交接单、进度快照、升级计划。要追溯看 commit 历史。
- 改文件名时同一次提交改完所有引用。
- 提交前跑 `python scripts/check_hygiene.py`。

## 不要做

- 不要手改生成的设计令牌产物，改 `design/` 源文件。
- 不要单方面改接口契约。服务端、iOS、微信小程序共用同一套。
- 不要在 `app/` 里新增只为一个页面服务的全局单例。

## 提交与 PR

Conventional Commits，标题中文，不超过 50 字。类型只用 `feat` `fix` `docs` `refactor` `test` `chore` `perf` `ci`。

分支名 `<type>/<主题>`，例如 `docs/normalize-filenames`。不直接推默认分支，不 force push 已 review 的分支。

提交、推送、合并及生产动作按用户已明确授权的范围执行；同范围已有授权不重复确认。

PR 描述写改了什么、为什么改、怎么验证。
