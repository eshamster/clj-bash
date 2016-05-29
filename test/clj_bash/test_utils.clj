(ns clj-bash.test-utils
  (:require [clj-bash.core :as core]
            [clj-bash.parser :as parser]
            [clj-bash.str :as to-str]
            [clojure.test :refer :all]
            [clojure.java.io :as io]
            [me.raynes.conch :as sh]))

(defmacro ^:private dirname []
  "This is implemented as a macro to be evaluated at compile time. In REPL *file* is not defined. ;; This else clause is probably a dirty hack:( In 'lein test', the *file* is 'clj_bash/test_utils.clj' and when getting its canonical path the directory 'test' is not included."
  (if (.exists (io/file *file*))
    `(.getParent (io/file ~*file*))
    `(str "test/" (.getParent (io/file ~*file*)))))

(defn- get-kind-path [kind test-name specifier]
  (.getCanonicalPath
   (io/file (dirname) kind (str test-name "." specifier))))

(defn- get-diff-path [test-name]
  (get-kind-path "output" test-name "txt.diff"))

(defn- get-detail-path [test-name]
  (get-kind-path "output" test-name "txt.detail"))

(defn- get-output-path [test-name]
  (get-kind-path "output" test-name "txt"))

(defn- get-shell-path [test-name]
  (get-kind-path "shell" test-name "sh"))

(defn- get-expected-path [test-name]
  (get-kind-path "expected" test-name "txt"))

(defn clean-old-results [name]
  (doseq [path (list (get-output-path name)
                     (get-diff-path name)
                     (get-detail-path name)
                     (get-shell-path name))]
    (if (.exists (io/file path))
      (io/delete-file path))))

(defmacro create-shell [path & body]
  `(with-open [fout# (io/writer ~path :append nil)]
     (.write fout# (core/compile-bash ~@body))))

(defn execute-shell [shell-path output-path]
  (with-open [fout (io/writer output-path :append nil)]
    (sh/with-programs [bash]
      (.write fout (bash "-f" shell-path)))))

(defn- diff-files [path1 path2]
  (sh/with-programs [diff]
    (try
      (diff path1 path2)
      (catch Exception e
        (let [data (ex-data e)]
          (if (= @(data :exit-code) 1)
            (data :stdout)
            (throw e)))))))

(defn compare-with-expected [output-path expected-path]
  (if (.exists (io/file expected-path))
    (diff-files output-path expected-path)
    (str "The expected file '" expected-path "' has not been created")))

(defn output-detail [test-name diff body-lst]
  "Output diff and pivot expressions"
  (let [diff-path (get-diff-path test-name)
        detail-path (get-detail-path test-name)]
    (with-open [fout (io/writer diff-path :append nil)]
      (.write fout diff))
    (with-open [fout (io/writer detail-path :append nil)]
      (let [parsed (parser/parse-main body-lst)
            str-list (to-str/str-main parsed)]
        (.write fout (pr-str parsed))
        (.write fout "\n---------\n")
        (.write fout (str str-list))))))

;; Main 
" 
pre:
  output, expectedの対象ファイルを削除
execute:
  パースした結果をshell/*.shに出力 + 実行属性付与
  実行して結果をoutput/*.txtに出力

<TODO:>
compare: expected & output
  expectedがない場合、diffがあった場合と同じ扱い＋メッセージ
  expectedがある場合、比較して ->
    diffがなければtrue
    diffがあればNG, .txt.diffと.txt.detailを出力, false
      detail -> parser, str-mainの結果
"
(defmacro test-bash [test-name & body]
  `(testing ~(str "test clj-bash: " test-name)
     (clean-old-results ~(str test-name))
     (let [shell-path# ~(get-shell-path test-name)
           output-path# ~(get-output-path test-name)
           expected-path# ~(get-expected-path test-name)]
       (create-shell shell-path# ~@body)
       (execute-shell shell-path# output-path#)
       (let [diff# (compare-with-expected output-path# expected-path#)]
         (when (not (= "" diff#))
           (output-detail ~(str test-name) diff# '~body))
         (is (=  "" diff#))))))
