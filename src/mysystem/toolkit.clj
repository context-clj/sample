(ns mysystem.toolkit
  (:require [mysystem.ui.helpers :as h]
            [clojure.string :as str]))

(def selected-item-class
  ["flex"
   "items-center"
   "justify-between"
   "cursor-pointer"
   "border"
   "group"
   "border-[#ccced3]"
   "rounded-[6px]"
   "min-h-[36px]"
   "py-[5px]"
   "pl-[12px]"
   "pr-[11px]"
   "bg-[#ffffff]"])

(def menu-class
  ["absolute"
   "w-full"
   "bg-[#ffffff]"
   "border"
   "border-separator"
   "p-[8px]"
   "pt-[7px]"
   "rounded-[6px]"
   "mt-[4px]"
   "z-10"
   "shadow-sm"])

(def search-input-root-class
  ["border"
   "border-[#ccced3]"
   "rounded-[6px]"
   "flex"
   "items-center"
   "h-[36px]"])

(def search-input-class
  ["outline-none"
   "text-[#1d2331]"
   "px-[11px]"
   "py-[6px]"
   "bg-transparent"
   "w-full"
   "placeholder:text-[#ccced3]"])

(def list-item-class
  ["flex"
   "items-center"
   "h-[32px]"
   "rounded-[4px]"
   "text-[#717684]"
   "pr-[12px]"
   "pl-[12px]"
   "[&_svg]:mr-[7px]"
   "hover:bg-[#f9f9f9]"
   "cursor-pointer"
   "data-[selected=true]:bg-[#ebecee]"
   "data-[selected=true]:text-[#1d2331]"])

(defn build-list-items-id [id] (str id "-list-items"))
(defn build-search-input-id [id] (str id "-search-input"))
(defn build-selected-item-id [id] (str id "-selected-item"))
(defn build-menu-id [id] (str id "-menu"))

(defn selected-item-view
  [props]
  [:div (merge {:id    (build-selected-item-id (:id props))
                :class selected-item-class
                :hx-trigger    "click"
                :hx-target     (str "#" (:id props))
                :hx-swap       "outerHTML"}
               (when (:on-open props)
                 {:hx-get (h/rpc (:on-open props) {:id (:id props) :selected-value (get-in props [:value :value])})})
               (when (:on-close props)
                 {:hx-get (h/rpc (:on-close props) {:id (:id props) :selected-value (get-in props [:value :value])})}))
   (get-in props [:value :title])])

(defn search-input
  [props]
  [:fieldset {:class search-input-root-class}
   [:input (merge {:class      search-input-class
                   :autofocus  true
                   :id         (build-search-input-id (:id props))
                   :hx-swap    "outerHTML"
                   :hx-get     (h/rpc (:on-search props) {:id             (:id props)
                                                          :selected-value (get-in props [:value :value])})
                   :hx-vals    (format "js:{search_string: document.getElementById('%s').value}"
                                       (build-search-input-id (:id props)))
                   :hx-target  (str "#" (build-list-items-id (:id props)))
                   :hx-trigger "keyup changed delay:300ms"})]])

(defn list-item [props item]
  [:div (merge {:class         list-item-class
                :data-selected (str (= (:value item) (get-in props [:value :value])))}
               (when (:value item)
                 {:hx-trigger    "click"
                  :hx-get        (h/rpc (:on-select props) {:id (:id props) :selected-value (:value item)})
                  :hx-target     (str "#" (:id props))
                  :hx-swap       "outerHTML"}))
   (:title item)])

(defn list-items [props items]
  [:div {:id    (build-list-items-id (:id props))
         :class "mt-[8px] space-y-[2px] overflow-y-auto pr-[1px] max-h-[338px]"}
   (or (seq items)
       (list-item props {:title "Not found"}))])

(defn dropdown-menu [props]
  [:div {:class menu-class
         :id    (build-menu-id (:id props))
         :style {:display (if (seq (:items props)) "block" "none")}}
   (search-input props)
   (list-items props
               (for [item (:items props)]
                 (list-item props item)))])

(defn dropdown
  [props]
  [:div {:class "relative" :id (:id props)}
   (selected-item-view props)
   (dropdown-menu props)])

(declare on-select-resource-type)
(declare on-open)
(declare on-close)

;; Usage example

(defn get-resource-types [& [search-string]]
  (cond->>
    [{:title "Patient" :value "patient"}
     {:title "Organization" :value "organization"}
     {:title "Encounter" :value "encounter"}
     {:title "Observation" :value "observation"}]
    (some? search-string)
    (filter #(str/includes? (str/lower-case (:title %))
                            (str/lower-case search-string)))))

(defn get-selected-resource-type-view [value]
  (->> (get-resource-types)
       (filter #(= (:value %) value))
       (first)))

(defn on-search-resource-type [context request params]
  (list-items
   {:id (:id params)}
   (for [item (get-resource-types (:search_string params))]
     (list-item {:id (:id params)
                 :value {:value (:selected-value params)}
                 :on-select #'on-select-resource-type} item))))

(defn on-select-resource-type [context request params]
  (let [selected-value-view (get-selected-resource-type-view (:selected-value params))]
    (dropdown {:id       (:id params)
               :on-open   #'on-open
               :value     selected-value-view})))

(defn on-open [context request params]
  (dropdown {:id        (:id params)
             :on-close  #'on-close
             :on-search #'on-search-resource-type
             :on-select #'on-select-resource-type
             :value     (get-selected-resource-type-view (:selected-value params))
             :items     (get-resource-types)}))

(defn on-close [context request params]
  (dropdown {:id        (:id params)
             :on-open   #'on-open
             :value     (get-selected-resource-type-view (:selected-value params))}))

(defn index [context request]
  (h/layout
   context request
   {:content [:div {:class "max-w-[500px]"}
              (dropdown {:id        "dropdown-demo-example"
                         :on-open   #'on-open
                         :value     {:title "Patient" :value "patient"}})]}))
