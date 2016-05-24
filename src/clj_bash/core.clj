(ns clj-bash.core)

(use '[clojure.string :only [join]])
(use '[clojure.core.match :only [match]])

(defn- parse-command [command rest]
  (concat (list :bash command) (map name rest)))

(defn parse-main [line]
  (if (keyword? (first line))
    (parse-command (name (first line)) (rest line))
    (eval line)))

(defmacro do-dsl [& body]
  `(map parse-main '~body))

(defn pprint-tree [tree]
  (doseq [line tree]
    (match [line]
           [([:bash & rest] :seq)] (println (join " " rest)))))

(defn -main [& args]
  (pprint-tree
   (do-dsl
    (:cd test)
    (:ls .))))
