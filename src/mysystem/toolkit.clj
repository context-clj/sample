(ns mysystem.toolkit
  (:require [mysystem.ui.helpers :as h]))

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

(defn selected-item-view
  [props]
  [:div {:class selected-item-class}
   (get-in props [:value :title])
   ])

(defn search-input
  [props]
  [:fieldset {:class search-input-root-class}
   [:input {:class search-input-class}]])

(defn list-items [& items]
  [:div {:class "mt-[8px] space-y-[2px] overflow-y-auto mb-[8px] pr-[1px] max-h-[338px]"}
   items])

(defn list-item [props item]
  [:div {:class list-item-class :data-selected (str (= (:value item) (get-in props [:value :value])))}
   (:title item)])

(defn dropdown
  [props]
  [:div {:class "relative"}
   (selected-item-view props)
   [:div {:class menu-class}
    (search-input props)
    (list-items
     (for [item (:items props)]
       (list-item props item)))]])

(defn index [context request]
  (h/layout
   context request
   {:content (dropdown {:value {:title "Patient" :value "patient"}
                        :items [{:title "Patient" :value "patient"}
                                {:title "Organization" :value "organization"}
                                {:title "Encounter" :value "encounter"}
                                {:title "Observation" :value "observation"}]})}))
