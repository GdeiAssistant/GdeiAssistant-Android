# 地区目录译名来源

地区代码、层级、原始中文名称及目录顺序继续沿用 `GdeiAssistant/src/main/resources/location.xml`，用户保存的所在地和家乡代码保持不变。

部分行政区和城市的英文、日文、韩文名称来自 [GeoNames 每日数据导出](https://download.geonames.org/export/dump/)（2026-10-06 快照）。GeoNames 数据采用 [Creative Commons Attribution 4.0](https://creativecommons.org/licenses/by/4.0/) 许可，版权归 GeoNames 及其数据贡献者所有。目录按国家和完整代码路径关联名称并保留原有显式译名。

目录包含 **2144 个**有明确来源关联的地区条目。GeoNames 缺少日文或韩文名称时，采用同一地理实体的标准英文名称或当地原名；其余 **1899 个**存在歧义或无法匹配的条目保留原有回退显示，不猜测翻译，不代表全球条目均已完成人工核译。

日本 `JPN/JPN/9` 的旧中文源名称“枥木”保留数据库原始名称与代码不变；依据 [栃木县官网](https://www.pref.tochigi.lg.jp/index.html)及 GeoNames 记录 `1850310`，其显示名称更正为“栃木”（英文 `Tochigi`）。

依据 [GeoNames 国家元数据](https://download.geonames.org/export/dump/countryInfo.txt)，`GUF` 为法属圭亚那（ISO `GF`），`GUY` 为圭亚那（ISO `GY`）。目录保留原业务码并正确区分两者的外部 ISO 与六语言显示名；含糊的自由文本“圭亚那”保持原样不猜测归属。

纽约州保留英文名称 `New York`，纽约市采用 `New York City`（与 [纽约市政府网页内容规范](https://designsystem.nyc.gov/standards/nyc-web-content-style-guide.html) 一致），避免市与州重名混淆。

数据随应用打包，不增加运行时地名服务、网络请求或生产依赖。共享离线来源快照位于主仓库 `frontend/scripts/location-names-geonames.json`，同步更新时须逐一核对四端代码路径与国家 ISO。
