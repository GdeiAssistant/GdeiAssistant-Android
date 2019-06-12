<p align="center">
  <img width="300" src="./github/logo.png">
</p>

# 广东二师助手安卓客户端

**广东第二师范学院校园助手系统安卓原生客户端应用**，兼容Android 9.0，已正式投入校园生产环境供在校师生使用。应用使用了Sophix、JWT、HttpDNS、OKHttp等框架组件，应用主要涉及JSON序列化、数据存储和网络访问等技术。应用的后端API数据接口由广东第二师范学院校园助手系统提供。

## 功能

- 成绩查询
- 课表查询
- 四六级查询
- 一键评教
- 消费查询
- 校园卡充值

## 预览

<p>
  <img width="250" src="./github/screenshot_01.jpg">
  <img width="250" src="./github/screenshot_02.jpg">
  <img width="250" src="./github/screenshot_03.jpg">
</p>

## 体验

为便于非在校师生用户体验和测试应用，应用提供了体验用户账号。详情请查阅 [广东二师助手体验用户账号说明](https://github.com/GdeiAssistant/GdeiAssistant#%E4%BD%93%E9%AA%8C)

## 日志

- V3.1.0：添加图书借阅查询功能
- V3.0.0：新版广东二师助手安卓客户端发布

## 链接
- [广东二师助手官网](https://gdeiassistant.cn)
- [酷安APP平台下载](https://www.coolapk.com/apk/edu.gdei.gdeiassistant)
- [Google Play下载](https://play.google.com/store/apps/details?id=edu.gdei.gdeiassistant)
- [Amazone Store下载](https://www.amazon.cn/dp/B07932T9V8)

## 数据接口

广东二师助手安卓客户端的后端API数据接口由[广东第二师范学院校园助手系统](https://github.com/GdeiAssistant/GdeiAssistant)提供

数据接口API文档说明请查阅[广东第二师范学院校园助手系统数据接口API文档](https://github.com/GdeiAssistant/GdeiAssistant/wiki)

## 初始化

### 克隆仓库

```bash
$ git clone https://github.com/GdeiAssistant/GdeiAssistant-Android.git
```

### Android EMAS统一接入

阿里云平台为了方便EMAS各个产品的接入，提供了基于gradle的emas-services插件。详情请查阅[Android EMAS统一接入](https://help.aliyun.com/knowledge_detail/68655.html)

该插件需要开发者在项目主模块路径下提供文件名为“aliyun-emas-services.json”的配置文件，该配置文件用于emas-services插件读取、解析阿里云移动研发平台SDK初始化必要的字段并保存到工程XML中。各阿里云移动研发平台产品SDK默认初始化时自动读取该配置文件中的对应字段。

项目主模块路径下提供了文件名为aliyun-emas-services-template.json的EMAS模板配置文件，开发者需要将文件去除后缀重命名为“aliyun-emas-services.json”，并填入配置参数。此外，开发者也可以通过阿里云移动研发平台控制台下载该配置文件，放置到对应的位置中。

### 字符串资源

项目主模块路径下的src/main/res/values/strings-template.xml是字符串资源模板配置文件，该文件用于保存字符串资源和重要的配置参数信息。

开发者需要将该模板配置文件去除其后缀“_template”，根据字符串资源模板配置文件修改和生成字符串资源配置文件，即将文件重命名为strings.xml。

同时，开发者还需要将模板配置文件内的配置参数名称的后缀“_template”去除，如参数名称app_name_template修改为app_name。

最后，填入相应的配置参数。配置参数的说明如下：

1. **防重放攻击**：request_validate_token是移动端请求服务端的拥有防重放攻击保护的数据接口时，需要携带的令牌信息。该令牌信息应该与服务端中配置的防重放攻击令牌值相同，否则校验无法通过。详情请参考 [广东第二师范学院校园助手系统初始化配置文件说明](https://github.com/GdeiAssistant/GdeiAssistant/blob/master/README.md#%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6)

2. **HotFix热修复**：项目使用了阿里云平台提供的移动热修复（Mobile Hotfix）。HotFix是阿里云提供的全平台App热修复服务方案。产品基于阿里巴巴首创hotpatch技术，提供最细粒度热修复能力，能让用户无需等待实时修复应用线上问题。字符串资源文件中的hotfix_app_key、hotfix_app_secret和hotfix_rsa分别代表通过平台HotFix服务申请得到的AppID、AppSecret和RSA密钥。

3. **HTTPDNS**：项目使用了阿里云平台提供的HTTPDNS组件，能有效防止DNS劫持。HTTPDNS是面向移动开发者推出的一款域名解析产品，具有域名防劫持、精准调度的特性。字符串资源文件中的httpdns_account_id和httpdns_secret_key分别表示通过平台HTTPDNS服务申请的AccountID和SecretKey。

## 协议

[MIT License](http://opensource.org/licenses/MIT)

[Anti 996 License](https://github.com/996icu/996.ICU/blob/master/LICENSE)

Copyright (c) 2016 - 2019 GdeiAssistant

## 贡献

- 若你喜欢本项目，欢迎Star本项目

- 要贡献代码，欢迎Fork之后再提交[Pull Request](https://github.com/GdeiAssistant/GdeiAssistant-Android/pulls)

- 如果你有好的意见或建议，欢迎给我们提交[Issue](https://github.com/GdeiAssistant/GdeiAssistant-Android/issues)

## 联系

- 技术支持和意见建议反馈：[gdeiassistant@gmail.com](mailto:gdeiassistant@gmail.com)

- 用户客服和系统故障工单提交：[support@gdeiassistant.cn](mailto:support@gdeiassistant.cn)

- 社区违法和不良信息举报邮箱：[report@gdeiassistant.cn](mailto:report@gdeiassistant.cn)

## 声明

本项目只用作个人学习研究，如因使用本项目导致任何损失，本人概不负责。
