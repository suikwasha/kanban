# kanban
かんばんのなりそこない

## features

- ユーザーの登録・作成
- タスクの作成・編集・削除
- タスクの期限の設定
- タスクの状態（未完了、作業中、完了）
- タスクの状態による絞り込み
- タスクタイトルによる部分一致検索
- 期限1日に前になったタスクの通知

## configuration
`config/application.conf`を編集してください。

### データベースの設定
そのままではH2DBを使用するので各環境に用意されたMySQLなどの接続情報に書き換えてください
```$xslt
# slick settings
slick.dbs.default.profile = "slick.jdbc.H2Profile$"
slick.dbs.default.db.driver = "org.h2.Driver"
slick.dbs.default.db.url = "jdbc:h2:file:./h2db"
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
`play.mailer`の設定は以下を参考にしてください
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

### テスト方法
`sbt test:compile test`

### dockerイメージの作成
`sbt docker:publishLocal`
