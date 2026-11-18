# Third-Party Notices

本项目包含 Android 客户端应用、构建脚本与本地化基础数据层面的第三方开源组件及数据。实际依赖集合以仓库源码、`gradle/libs.versions.toml`、`app/build.gradle.kts` 以及最终发布产物为准。

## 关键组件与数据归属

- **AndroidX / Jetpack Compose / Material 3 / Navigation / Lifecycle / DataStore / Security Crypto**：Apache License 2.0
- **Kotlin / Kotlinx Coroutines / Kotlinx Collections Immutable**：Apache License 2.0
- **Dagger Hilt**：Apache License 2.0
- **OkHttp / Retrofit / Coil / Gson / Apache Commons Codec**：Apache License 2.0
- **Auth0 JWTDecode（`com.auth0.android:jwtdecode`）**：MIT License
- **GeoNames 地名快照数据**：[Creative Commons Attribution 4.0](https://creativecommons.org/licenses/by/4.0/)，署名 GeoNames（`https://www.geonames.org/`），详情见 [`docs/location-names.md`](docs/location-names.md)

## 依赖核对命令

正式构建或发行衍生版本前，可通过以下命令导出完整依赖树，核对具体版本与许可证义务：

```bash
./gradlew :app:dependencies
```
