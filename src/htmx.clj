(ns htmx
  (:require [clojure.string :as str]))


(defn & [& exprs]
  (str/join ";" exprs))

(defn remove [sel]
  (str "htmx.remove('" (name sel) "')"))

(def trigger :hx-trigger)


(def get :hx-get)
(def push-url :hx-push-url)
(def target :hx-target)
(def swap :hx-swap)
(def replace-url :hx-replace-url)
(def after-swap "after-swap")
(def beforeend "beforend")

(defn on [k] (str "hx-on::" (name k)))
