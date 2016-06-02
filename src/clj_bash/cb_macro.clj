(ns clj-bash.cb-macro)

(def cb-macro-table (atom {}))

(defn init-cb-macro-table []
  (reset! cb-macro-table {}))

(defn- get-cb-macro-func [symbol]
  (get @cb-macro-table (name symbol)))

(defn cb-macro? [name]
  (if (get-cb-macro-func name)
    true
    false))

(defn cb-macroexpand [form]
  (if (seq? form)
    (let [head (first form)]
      (if (symbol? head)
        (let [func (get-cb-macro-func head)]
          (if func
            (cb-macroexpand (func (rest form)))
            form))
        form))
    form))

(defmacro def-cb-macro [name args body]
  `(reset! cb-macro-table
           (assoc @cb-macro-table
                  ~(str name)
                  (fn [args-list#]
                    (let [~args args-list#]
                      ~body)))))
