(ns clj-bash.cb-macro)

(def cb-macro-table (atom {}))
(def default-cb-macro-table (atom {}))

(defn init-cb-macro-table []
  (reset! cb-macro-table @default-cb-macro-table))

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

(defn- register-cb-macro-to [table name args body]
  `(reset! ~table
           (assoc @~table
                  ~(str name)
                  (fn [args-list#]
                    (let [~args args-list#]
                      ~body)))))

(defn- register-cb-macro [name args body]
  (register-cb-macro-to 'cb-macro-table name args body))

(defn- register-cb-macro-as-default [name args body]
  (register-cb-macro-to 'cb-macro-table name args body)
  (register-cb-macro-to 'default-cb-macro-table name args body))

;; TODO: return the name of the defined macro
(defmacro def-cb-macro [name args body]
  (register-cb-macro name args body))

(defmacro def-cb-macro-as-default [name args body]
  `(do ~(register-cb-macro name args body)
       ~(register-cb-macro-as-default name args body)))
