# HTTP 请求示例

该目录用于本地联调与测试：

- `idol.http`：偶像端接口请求示例
- `fan.http`：粉丝端接口请求示例

可在支持 `.http` 文件的客户端中直接运行（如 VS Code REST Client、JetBrains HTTP Client）。

默认变量：

- `baseUrl = http://localhost:8080/api`
- `idolKey = idol-dev-key`

粉丝端请求需要先在 `fan.http` 中执行注册/登录并替换 `fanBasicAuth` 与 `fanId`。
