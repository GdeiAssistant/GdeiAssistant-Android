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

## 日志

- V3.0.4：调整首页权限和模块加载逻辑
- V3.0.3：修正今日课表模块信息显示不完整的错误
- V3.0.2：调整版本号显示格式	
- V3.0.1：用户昵称过长时使用省略号显示，修正客户端生成数字签名的错误
- V3.0.0：新版广东二师助手安卓客户端发布

## 链接
- [广东二师助手官网](https://gdeiassistant.cn)
- [酷安APP平台下载](https://www.coolapk.com/apk/edu.gdei.gdeiassistant)
- [Google Play下载](https://play.google.com/store/apps/details?id=edu.gdei.gdeiassistant)
- [Amazone Store下载](https://www.amazon.cn/dp/B07932T9V8)

## 相关

广东二师助手安卓客户端的后端API数据接口由[广东第二师范学院校园助手系统](https://github.com/SweetRadish/GdeiAssistant)提供

## 初始化

### 克隆仓库

```bash
$ git clone https://github.com/SweetRadish/GdeiAssistant-Android.git
```

### Android EMAS统一接入

阿里云平台为了方便EMAS各个产品的接入，提供了基于gradle的emas-services插件。详情请查阅 [Android EMAS统一接入](https://help.aliyun.com/knowledge_detail/68655.html)

### 字符串资源

项目路径下的res/values/strings.xml是字符串资源文件。在项目中，该文件还保存了一些重要的配置参数信息。

1. **防重放攻击**：request_validate_token是移动端请求服务端的拥有防重放攻击保护的数据接口时，需要携带的令牌信息。该令牌信息应该与服务端中配置的防重放攻击令牌值相同，否则校验无法通过。详情请参考 [广东第二师范学院校园助手系统初始化说明](https://github.com/SweetRadish/GdeiAssistant/blob/master/README.md#%E5%88%9D%E5%A7%8B%E5%8C%96)

2. **HotFix热修复**：项目使用了阿里云平台提供的移动热修复（Mobile Hotfix）。HotFix是阿里云提供的全平台App热修复服务方案。产品基于阿里巴巴首创hotpatch技术，提供最细粒度热修复能力，能让用户无需等待实时修复应用线上问题。字符串资源文件中的hotfix_app_key、hotfix_app_secret和hotfix_rsa分别代表通过平台HotFix服务申请得到的AppID、AppSecret和RSA密钥。

3. **HTTPDNS**：项目使用了阿里云平台提供的HTTPDNS组件，能有效防止DNS劫持。HTTPDNS是面向移动开发者推出的一款域名解析产品，具有域名防劫持、精准调度的特性。字符串资源文件中的httpdns_account_id和httpdns_secret_key分别表示通过平台HTTPDNS服务申请的AccountID和SecretKey。

## 协议

[MIT License](http://opensource.org/licenses/MIT)

[Anti 996 License](https://github.com/996icu/996.ICU/blob/master/LICENSE)

Copyright (c) 2016 - 2019 GdeiAssistant

## 贡献

- 若你喜欢本项目，欢迎Star本项目

- 要贡献代码，欢迎Fork之后再提交[Pull Request](https://github.com/SweetRadish/GdeiAssistant-Android/pulls)

- 如果你有好的意见或建议，欢迎给我们提交[Issue](https://github.com/SweetRadish/GdeiAssistant-Android/issues)

## 联系

- 邮箱：[gdeiassistant@gmail.com](mailto:gdeiassistant@gmail.com)

## 声明

本项目只用作个人学习研究，如因使用本项目导致任何损失，本人概不负责。
