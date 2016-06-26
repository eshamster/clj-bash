(ns clj-bash.pprint)

(use '[clojure.string :as str])
(use '[clj-bash.str :only [heredoc-identifier]])

(def ^:dynamic num-indent 2)

(defn- indent-line [depth str-line]
  (if (str/starts-with? str-line heredoc-identifier)
    (str/replace-first str-line heredoc-identifier "")
    (str (apply str (repeat (* depth num-indent) " "))
         str-line)))

(defn- inc-hash-value [hash key add-value]
  (let [base-value (hash key)]
    (assoc hash key (+ base-value add-value))))

(defn pprint-tree [tree & {:keys [env] :or {env {:depth 0}}}]
  (doseq [line tree]
    (if (seq? line)
      (pprint-tree line :env (inc-hash-value env :depth 1))
      (println (indent-line (env :depth) line)))))
