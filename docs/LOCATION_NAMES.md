# 地区目录译名来源

地区代码、层级、原始中文名称及目录顺序继续使用 `GdeiAssistant/src/main/resources/location.xml`，用户保存的所在地和家乡代码保持不变。

部分行政区和城市的英文、日文、韩文名称来自 [GeoNames 每日数据导出](https://download.geonames.org/export/dump/)，数据快照日期为 **2026-10-06**。GeoNames 数据采用 [Creative Commons Attribution 4.0](https://creativecommons.org/licenses/by/4.0/) 许可，版权归 GeoNames 及其数据贡献者所有。项目按国家和完整代码路径关联名称，并保留原有显式译名；目录内的名称关联、筛选和语言回退属于本项目对数据的修改。

本次同步 **2144 个**有来源且可明确关联的地区条目，另纠正一个国家的外部元数据。GeoNames 缺少日文或韩文名称时，使用同一地理实体的标准英文名称或当地原名。仍有 **1899 个**存在歧义或无法匹配的条目保留原有回退，不猜测翻译。因此本目录不代表全部全球条目均已人工核验或具有日文、韩文专属译名。除下列明确更正外，繁体中文名称保持不变。

数据随应用打包，不增加运行时地名服务、网络请求或生产依赖。

日本 `JPN/JPN/9` 的旧中文名称“枥木”存在笔误。根据 [栃木县官网](https://www.pref.tochigi.lg.jp/index.html)及 GeoNames 行政区记录 `1850310`，该节点的中文显示名称更正为“栃木”，英文为 `Tochigi`；数据库目录原始名称与代码继续保留。

根据 [GeoNames 国家元数据](https://download.geonames.org/export/dump/countryInfo.txt)，`GUF` 是法属圭亚那，外部 ISO 代码应为 `GF`；`GUY` 是圭亚那，外部 ISO 代码为 `GY`。本次修正 `GUF` 误用的 `GY` 及六语言显示名称，业务代码 `GUF` 和原始名称不变。只包含旧名称“圭亚那”的系统地区字符串保持原样，不猜测国家；明确的“法属圭亚那”名称可正确切换语言。

纽约州保留英文名称 `New York`，纽约市使用 `New York City`，与 [纽约市政府网页内容规范](https://designsystem.nyc.gov/standards/nyc-web-content-style-guide.html)中的名称一致，避免市与州显示重名。

共享离线来源快照位于 `GdeiAssistant/frontend/scripts/location-names-geonames.json`，记录采用条目的代码路径、GeoNames ID、匹配依据和标签。同步更新后应核对四端对应路径的标签与国家 ISO，不能只按数组位置复制。
