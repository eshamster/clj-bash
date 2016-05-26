(ns clj-bash.pprint)

(def ^:dynamic num-indent 2)

(defn- indent-line [depth str-line]
  (str (apply str (repeat (* depth num-indent) " "))
       str-line))

(defn- inc-hash-value [hash key add-value]
  (let [base-value (hash key)]
    (assoc hash key (+ base-value add-value))))

(defn pprint-tree [tree & {:keys [env] :or {env {:depth 0}}}]
  (doseq [line tree]
    (if (list? line)
      (pprint-tree line :env (inc-hash-value env :depth 1))
      (println (indent-line (env :depth) line)))))
