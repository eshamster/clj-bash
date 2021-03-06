(ns clj-bash.parser)

(use '[clojure.string :only [join]]
     '[clj-bash.utils :only [match-seq check-return ensure-no-unrecognized-keys]]
     '[clj-bash.cb-macro :only [cb-macro? cb-macroexpand]])

(declare parse-line)
(declare parse-main)
(declare parse-set-value)

(defn- add-prefix [prefix rest]
  (concat (list prefix) rest))

(defn- cover-by-eval [seq]
  (add-prefix :eval seq))

(defn- parse-string [string]
  (add-prefix :string (list string)))

;; TODO: process each element of array for such a case as [0 (:expr 1 + 2) 1]
(defn- parse-arg [arg]
  (cond
    (seq? arg) (if-not (= (name (first arg)) "var")
                  (cover-by-eval (parse-line arg))
                  (parse-line arg))
    (vector? arg) (add-prefix :array arg)
    (number? arg) (str arg)
    (string? arg) (parse-string arg)
    (keyword? arg) (name arg)
    (symbol? arg) (name arg)
    :else (throw (Exception.
                  (format "not recognized arg type: %s (%s)"
                          arg (type arg))))))

(defn- parse-and-or [and-or exprs]
  (add-prefix and-or (parse-main exprs)))

(defn- parse-and [exprs]
  (parse-and-or :and exprs))
(defn- parse-or [exprs]
  (parse-and-or :or exprs))

(defn- parse-command [command rest]
  (add-prefix :command (concat (list command)
                               (map parse-arg rest))))

;; --- cond --- ;;

(defn- check-cond-body [body]
  (if-not (even? (count body))
    (throw (IllegalArgumentException. "cond requires an even number of forms"))))

(defn- process-condition [condition]
  (if (vector? condition)
    (add-prefix :test (map parse-arg condition))
    (cover-by-eval (parse-line condition))))

(defn- process-cond-line [now-result condition expr]
  (when-not (seq? expr)
    (throw (IllegalArgumentException.
            (str "The process of cond should be a list: " expr))))
  (let [parsed-expr (list (parse-line expr))]
    (if (= condition :else)
      (add-prefix :else parsed-expr)
      (let [prefix (if (nil? now-result) :if :elif)
            test-clause (process-condition condition)]
        (add-prefix prefix (cons test-clause parsed-expr))))))

(defn- parse-cond [body]
  (check-cond-body body)
  (let [grouped-body (partition 2 body)]
    (loop [result nil
           line (first grouped-body)
           rest-body (rest grouped-body)]
      (if-not (nil? line)
        (recur (cons (process-cond-line result (first line) (second line))
                     result)
               (first rest-body)
               (rest rest-body))
        (add-prefix :cond (reverse result))))))

;; --- defn --- ;;

(defn- parse-defn [name args body]
  (letfn [(check-name [name]
            (check-return [#(or (string? %) (symbol? %))
                           "A function name should be a string or a symbol"]
                          name))
          (check-args [args]
            (check-return [vector? "A argument list should be a vector"]
                          args))]
    (add-prefix :function
                (list :fn-name (check-name name)
                      :args (check-args args)
                      :body (parse-main body)))))

;; --- --- ;;

(defn- parse-do [exprs]
  (add-prefix :do (parse-main exprs)))

(defn- parse-for [var array rest]
  (add-prefix
   :for (list :var var
              :range (parse-arg array)
              :body (parse-main rest))))

(defn- parse-set-value [name value]
  (add-prefix :set (list name (parse-arg value))))

(defn- parse-pipe [exprs]
  (add-prefix :pipe (parse-main exprs)))

(defn- parse-var [var-name]
  (add-prefix :var (list (name var-name))))

(defn- parse-with-heredoc [body heredoc]
  (let [[command & {:keys [out append] :as options}] body]
    (ensure-no-unrecognized-keys options '(:out :append))
    (add-prefix :with-heredoc
                (list :body (parse-line command)
                      :out (cond (some? append) (list :append (parse-arg append))
                                 (some? out) (list :out (parse-arg out))
                                 :else  nil)
                      :heredoc heredoc))))

;; --- while --- ;;

(defn- parse-while [condition body]
  (add-prefix :while
              (list :condition (process-condition condition)
                    :body (parse-main body))))

;; --- basic functions --- ;;

(defn- try-cb-macro [line]
  (if (cb-macro? (first line))
    (cb-macroexpand line)
    (throw (Exception. (str (first line) " is not a cb-macro")))))

(defn- parse-line [line]
  (let [kind (first line)
        kind-name (name kind)
        args (rest line)]
    (if (keyword? kind)
      (parse-command kind-name args)
      (match-seq
       (cons kind-name args)
       ["and" & body]  (parse-and body)
       ["cond" & body] (parse-cond body)
       ["defn" name fn-args & body] (parse-defn name fn-args body)
       ["do" & body]   (parse-do body)
       ["for" var array & body]  (parse-for var array body)
       ["or" & body]   (parse-or body)
       ["set" name value]        (parse-set-value name value)
       ["var" name]    (parse-var name)
       ["with-heredoc" [& body] & heredoc] (parse-with-heredoc body heredoc)
       ["while" condition & body] (parse-while condition body)
       ["->" & body]   (parse-pipe body)
       :else (parse-line (try-cb-macro line))))))

(defn parse-main [body]
  (map parse-line body))
