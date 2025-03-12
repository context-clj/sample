(ns build
  (:require [clojure.tools.build.api :as b]))

(def app 'sample)
(def version (format "0.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def jar-file (format "target/%s.jar" (name app)))

;; delay to defer side effects (artifact downloads)
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis @basis
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file jar-file
           :basis @basis
           :main 'mysystem.main}))
