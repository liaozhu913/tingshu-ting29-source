# 《我的听书》订阅源

导入地址：

```text
https://raw.githubusercontent.com/liaozhu913/tingshu-ting29-source/main/external_sources.json
```

文件说明：

- `external_sources.json`：《我的听书》订阅入口。
- `sources_by_ting29.jar`：自定义听书源解析包。

## 当前已注册源

| 站点 | 状态 | 验证说明 |
| --- | --- | --- |
| 29听书网 | 已注册 | 搜索、分类、详情、章节、音频解析通过 live test |
| 恋听网 | 已注册 | 搜索、详情、章节、`/glink` 音频解析通过 live test |
| 乐听网 | 已注册 | 搜索、详情、章节、iframe 播放器音频解析通过 live test |
| 麒麟听书 | 已注册 | 搜索、详情、章节、移动 iframe 播放器音频解析通过 live test |
| 275听书 | 已注册 | 搜索、详情、章节、带会话 cookie 播放页音频解析通过 live test |
| 有听网 | 已注册 | 搜索、详情、章节、`api-getneoplay` 音频解析通过 live test |
| 听13网 | 已注册 | 搜索、详情、章节通过 live test；浏览器现场嗅探到 mp3 音频请求，使用 WebView 嗅探解析 |
| 爱听书 | 已注册 | 搜索、详情、章节通过 live test；浏览器现场嗅探到 mp3 音频请求，使用 WebView 嗅探解析 |

## 未注册站点

| 站点 | 原因 |
| --- | --- |
| 听书网 `m.tingshuwang.cc` | 搜索可用，但详情页章节容器为空，桌面/移动页均未找到稳定目录接口 |
| hsh `m.hsh2011.com` | Cloudflare 403 验证页阻断普通源请求 |
| 天方听书 `tingbook.com` | SPA/静态配置未验证到完整听书搜索、章节和音频接口 |
| 百听听书 `bookting.cn` | Angular 前端可见播放路由，但未验证到可直接导入使用的完整搜索/详情/音频链路 |
| 听友FM `tingyou.fm` | Nuxt 页面可见章节数据和 JSON API 配置，但接口现场返回空体，未验证音频链路 |

## 本地验证

```powershell
.\gradlew.bat test --tests ExpandedSourcesLiveTest -x dexTask
.\gradlew.bat test --tests Ting29Test -x dexTask
```

## 听13网解析修复说明

听13网分类列表不能复用通用 PTCMS 列表字段下标。分类页中书名链接、分类文本、收听数等统计信息在同一行内相邻出现，直接读取整行文本或按 `.list-book-cs span` 固定位置映射，会导致书名、分类、收听数错位。

本仓库新增 `Ting13Parsing` 辅助代码，要求听13网源按站点结构独立解析：

1. 书名只从标题链接自身文本读取。
2. 详情页地址只从标题链接 `href` 读取并规范化为绝对地址。
3. 收听数、播放数、人气等统计文本仅进入元信息，不允许写入书名或分类。
4. 分类优先使用明确标注“分类/类型”的字段，否则回退到当前分类入口名称。
5. 搜索页和分类页应共用同一套 `parseBookItem` 字段映射，避免两个入口展示含义不一致。
