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
  - comment
  - subprocess
  - Ex. "a$(echo b)c" -> "abc"

# fixes

# improve

- cb-macro
  - with-initial-cb-macro-table
- cond
  - 条件式が決め打ちtestコマンドになっているが、
    任意のコマンドを受け取れるようにする
    - 案：仕様を変更し、listのときは一般的なコマンドとして扱う。
      一方で、arrayのときはtestコマンドの省略系として扱う
- str-bodyにネスト（forやcondの内部）を扱うインタフェースをつける
  - 現状はstr-mainの返り値をリスト操作で挟み込んでいる
  - str-mainの返り値はリスト型のstr-bodyであることが保証されている（はず）
    なので、当面はこの実装でも問題（面倒な分岐）になることはないのでは

# discussion

- str-bodyはstringも許容すべきかlistだけにすべきか
  - 現状のstringを許す設計が何かと悩みの種になっているのは確か
    - そのため、将来的にlistしか許さない形に戻す可能性あり
  - ただし、str-bodyのインタフェースが整っていれば、str側は
    そのことを（余り）意識しなくて良いはずだ、という考えを持っている
  - したがって、str-bodyのインタフェースの試金石として、
    当面はstringを許容する形で進めていく
    
