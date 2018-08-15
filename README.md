# kanban
`http://suikwasha.com:18080`にサンプルがあります

## 要件

* アカウント管理機能がある
    * 認証は必須だが、認可はオプション
* アカウントを用いてログインできる
* タスクには件名と状態、そして作成したアカウントを持つ
    * 件名は必須
    * 状態は未完了と完了
* タスクを一覧できる
    * 未完了、完了での絞り込みができる
* タスクを作成、編集できる
    * 作成、編集の対象は件名を入力する
* タスクを削除できる
* タスクを未完了から完了に変更できる

### オプション課題

オプション課題として、基本課題で開発した web サービスに対して変更を加えてください。 この課題は任意です。 実装しなくても構いませんし、実装して自身の強みなどのアピールのために使用することもできます。

オプション課題に取り組む場合は、取り組んだ内容をドキュメントに明記してください。 変更内容は自由です。 下記の例を参考にしてください。

### 基礎発展系

* 状態を未着手、着手中、完了の3状態を扱えるようにする
* 認証機能を追加する

### インフラ系

* 作成したサービスを Docker で起動できるようにする

### フロントエンド系

* サービスの見た目を整える
* Ajax を用いてタスクの登録、状態の変更をできるようにする

### サーバーサイド系

* タスクを自然言語で検索できるようにする
* タスクに締め切りを設定できるようにする (未完了の状態で締め切りに近づいた時、ユーザーにメール通知が飛ぶように)


## configuration
`config/application.conf`を編集してください。

### データベースの設定
そのままではH2DBを使用するので各環境に用意されたMySQLなどの接続情報に書き換えてください
```$xslt
# slick settings
slick.dbs.default.profile = "slick.jdbc.H2Profile$"
slick.dbs.default.db.driver = "
slick.dbs.default.db.url = "jdbc:mysql://<.....>"
slick.dbs.default.db.user = "your username"
slick.dbs.default.db.password = "your password"
```

### EMail通知設定
通知には`play-mailer`を利用しています。
Senderには通知を送るユーザーを指定してください。
```$xslt
# email notification settings
kanban.notification.email {
  sender = "hogehoge@example.com"
}
play.mailer {
  mock = yes
}
```
`play.mailer`の設定は以下を参考にしてください(デフォルトではモックの実装になります)
```$xslt
play.mailer {
  host = "example.com" // (mandatory)
  port = 25 // (defaults to 25)
  ssl = no // (defaults to no)
  tls = no // (defaults to no)
  tlsRequired = no // (defaults to no)
  user = null // (optional)
  password = null // (optional)
  debug = no // (defaults to no, to take effect you also need to set the log level to "DEBUG" for the application logger)
  timeout = null // (defaults to 60s in milliseconds)
  connectiontimeout = null // (defaults to 60s in milliseconds)
  mock = no // (defaults to no, will only log all the email properties instead of sending an email)
}
```

## for developer

### ビルド方法
`sbt compile`

### 起動方法
`sbt run`または`sbt start`

### テスト方法
`sbt test:compile test`

### docker
ビルド済みのイメージが`https://hub.docker.com/r/abrengw/kanban/`にあります

#### イメージの作成
`sbt docker:publishLocal`
#### コンテナの起動
`docker run -d -v <path_to_your_config>:/opt/docker/conf/application.conf -p <port>:9000 kanban:1.0.1`

