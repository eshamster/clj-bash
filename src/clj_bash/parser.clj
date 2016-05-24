(ns clj-bash.parser)

(use '[clojure.string :only [join]])

(defn- parse-command [command rest]
  (concat (list :bash command) (map name rest)))

(defn parse-main [line]
  (if (keyword? (first line))
    (parse-command (name (first line)) (rest line))
    (eval line)))

(defmacro do-dsl [& body]
  `(map parse-main '~body))
