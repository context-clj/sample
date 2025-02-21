(ns mysystem.ui.components.dropdown
  (:require [mysystem.ui.helpers :as h]))

(defn build-value-view-id [dropdown-id]
  (str dropdown-id "-value-view"))

(defn value-view [{:keys [dropdown-id expanded? value on-open on-close]}]
  (let [id (build-value-view-id dropdown-id)]
    [:button (merge
              {:id                 id
               :class              "peer/dropdown border"
               :role               "combobox"
               :aria-expanded      (str (boolean expanded?))
               :data-active-option (:id value)}
              (if expanded?
                {:hx-get  (h/rpc on-close {:component-id dropdown-id
                                          :selected-value-id (:id value)})
                 :hx-swap (str "multi:#" id ":outerHTML")}
                {:hx-get  (h/rpc on-open {:component-id dropdown-id
                                          :selected-value-id (:id value)})
                 :hx-swap (str "multi:#" id ":outerHTML")}))
     [:span {:hx-swap "none"} (:view value)]]))

(defn option-view [props option]
  [:li {:role "option"} (:view option)])

(defn listbox-view [props items]
  [:ul {:role "listbox"} items])

(defn popup-view [props]
  [:div {:class "dropdown-popup-view hidden peer-aria-expanded/dropdown:block border"}
   (listbox-view props
                 (for [option (:options props)]
                   (option-view props option)))])

(defn view [props]
  [:div {:id (:id props) :hx-ext "multi-swap"}
   (value-view
    {:dropdown-id (:id props)
     :value       (:value props)
     :on-open     (:on-open props)})
   (popup-view props)])

;; Events

(defn on-open [handlers {:keys [dropdown-id selected-value]}]
  [:data
   (value-view {:dropdown-id dropdown-id
                :value       selected-value
                :on-close    (:on-close handlers)
                :expanded?   true})])

(defn on-close [handlers {:keys [dropdown-id selected-value]}]
  [:data
   (value-view {:dropdown-id dropdown-id
                :value       selected-value
                :on-open     (:on-open handlers)
                :expanded?   false})])
