---
applyTo: "**/*.yml,**/*.yaml"
---

# YMLファイルのコードレビュー指示

- レビューコメントはすべて日本語で記述してください
- プロパティ名（キー）はすべてスネークケース（snake_case）で記述すること
  - 良い例: `server_port`, `database_url`, `max_connections`
  - 悪い例: `serverPort`, `databaseUrl`, `maxConnections`
- Spring Bootのデフォルトプロパティ（`server.port` 等）はそのまま許容する
- カスタムプロパティを定義する場合は必ずスネークケースを使用する
- インデントはスペース2文字で統一する
- 不要な空行や末尾スペースがないか確認する
- 機密情報（パスワード、APIキー等）がハードコードされていないか確認する
