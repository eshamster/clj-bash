# functions

- 制御構造
  - if
  - while
  - case
  - EOF
- Syntax Sugar
  - trap
  - exportへの追記 (Ex. `export PATH=/usr/local/bin:${PATH}`)
  - to-stdout
  - to-stderr
  - relative path
- 未分類
  - function
  - set

# fixes

- `echo "in "` のようなケースで末尾のスペースが除かれてしまう
  - 原因: 
