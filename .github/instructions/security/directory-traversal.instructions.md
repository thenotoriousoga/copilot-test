---
applyTo: "src/main/java/**"
---

# ディレクトリ・トラバーサル レビュー指示

- レビューコメントはすべて日本語で記述してください

## 概要

外部からのパラメータでサーバ内のファイル名やパスを指定している箇所がないか確認し、ディレクトリ・トラバーサルの脆弱性を防止する。

## 判定フロー

以下の順序で判定すること：

1. ファイル操作に外部入力が関与しているか → 関与していなければ指摘不要
2. 根本的解決が実施されているか → 実施されていれば対策済みコメント
3. 保険的対策のみか → 根本的解決の検討を指摘
4. いずれも未実施か → 脆弱性リスクを明示し、根本的解決を強く求める

## レビュー観点

### 根本的解決（以下が実施されている場合、指摘不要）

- 外部からのパラメータでサーバ内のファイル名を直接指定する実装を避けている
  - ファイル名をパラメータで受け取る代わりに、識別子（ID等）で間接的に参照する方式になっている
  - 仕様や設計レベルで、外部パラメータによるファイル指定が不要な構成になっている
- ファイルを開く際に、固定のディレクトリを指定し、かつファイル名にディレクトリ名が含まれないようにしている
  - `Paths.get(fixedDir).resolve(filename).normalize()` 等で固定ディレクトリを基点にしている
  - `Path.getFileName()` 等を使用して、パス名からファイル名のみを取り出している
  - 正規化後のパスが固定ディレクトリ配下であることを `Path.startsWith()` で検証している

### 保険的対策（以下のみで対処している場合、根本的解決の実施を検討するよう指摘する）

- ファイルへのアクセス権限の設定のみで対処している
- ファイル名のチェック（`/`、`../`、`..\` 等の検出）のみで対処している
  - URLデコード後の `%2F`、`..%2F`、`..%5C` や二重エンコードの `%252F`、`..%252F`、`..%255C` も考慮が必要

## 検出すべきパターン

以下のパターンに該当する箇所を検出すること。

### 危険なコード例（指摘対象）

```java
// パターン1: 外部入力を直接ファイルパスに使用
Path path = Paths.get("/uploads/" + filename); // 危険
return Files.readAllBytes(path);

// パターン2: 外部入力をFileコンストラクタに使用
return Files.readAllBytes(new File("/data", name).toPath()); // 危険

// パターン3: 外部入力でInputStream を取得
return new FileInputStream("/templates/" + template); // 危険
```

### 安全なコード例（指摘不要）

```java
// 根本的解決1: IDで間接参照（外部入力をファイルパスに使用しない）
String storedName = fileRepository.findStoredNameById(id);
Path path = Paths.get(UPLOAD_DIR).resolve(storedName);
return Files.readAllBytes(path);

// 根本的解決2: 固定ディレクトリ + ファイル名のみ抽出 + パス検証
Path basePath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
Path filePath = basePath.resolve(Paths.get(filename).getFileName()).normalize();
if (!filePath.startsWith(basePath)) {
    throw new SecurityException("不正なパスです");
}
return Files.readAllBytes(filePath);
```

### 検出対象のAPI

- `java.io.File`、`java.nio.file.Path`、`java.nio.file.Paths` を使用し、外部入力をファイルパスに組み込んでいる箇所
- `new File(userInput)` や `Paths.get(userInput)` のように、ユーザー入力を直接ファイルパスとして使用している箇所
- `Resource`、`ResourceLoader`、`ClassPathResource` 等で外部入力をリソースパスに使用している箇所
- サードパーティ製ライブラリを通じたファイル操作で、外部入力がパス構築に関与している箇所
- ファイルダウンロード、ファイルアップロード、テンプレート読み込み等のファイル操作全般

## 指摘対象外

以下のケースはディレクトリ・トラバーサルの指摘対象外とする：

- ファイル操作に外部入力が一切関与していない場合（固定パスのみの読み書き）
- テストコード内のファイル操作
- `application.yml` 等の設定ファイルで定義されたパスの読み込み

## レビューコメントの記載ルール

- レビューコメントの先頭に「【ディレクトリ・トラバーサル】」を付与する
- レビューコメントの末尾に、以下の参照リンクを記載する：
  「詳細はIPAの[『安全なウェブサイトの作り方』](https://www.ipa.go.jp/security/vuln/websecurity/parameter.html)を参照してください。」
- 根本的解決が実施されている場合は、対策済みである旨を伝えつつ、念のためIPAの参照リンクを確認するよう促す
- 保険的対策のみで対処している場合は、根本的解決の実施を検討するよう指摘する
- 根本的解決・保険的対策のいずれも未実施の場合は、脆弱性のリスクを明示し、根本的解決を強く求める

### コメント例

```
【ディレクトリ・トラバーサル】
{コメント内容}
詳細はIPAの[『安全なウェブサイトの作り方』](https://www.ipa.go.jp/security/vuln/websecurity/parameter.html)を参照してください。
```
