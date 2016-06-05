# functions

- 制御構造
  - while
  - case
  - do
    - condにもdoを利用したテストを追加
  - EOF
- Syntax Sugar
  - trap
  - exportへの追記 (Ex. `export PATH=/usr/local/bin:${PATH}`)
  - to-stdout
  - to-stderr
  - relative path ($(dirname ${0}))
  - when-err
- 未分類
  - subprocess
  - Ex. "a$(echo b)c" -> "abc"

# fixes

# improve

- cb-macro
  - with-initial-cb-macro-table
- cond
  - 条件式が決め打ちtestコマンドになっているが、
    任意のコマンドを受け取れるようにする
