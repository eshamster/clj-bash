(ns clj-bash.pprint)

(use '[clojure.string :only [join]])
(use '[clojure.core.match :only [match]])

(defn pprint-tree [tree]
  (doseq [line tree]
    (match [line]
           [([:bash & rest] :seq)] (println (join " " rest)))))
