(ns mysystem.ui.tailwindui
  (:require
   [system]
   [mysystem.ui.helpers :as h]
   [http]))


;; TODO: generate all this combinations


                    "rounded-sm bg-white px-2 py-1 text-xs font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50"
           "rounded-full bg-indigo-600 px-2.5 py-1 text-xs font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
(def btn-xs!-c "rounded-sm bg-indigo-600 px-2 py-1 text-xs font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600")
(def btn-sm!-c "rounded-sm bg-indigo-600 px-2 py-1 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600")
(def btn-md!-c "rounded-md bg-indigo-600 px-2.5 py-1.5 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600")
(def btn-lg!-c "rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600")
(def btn-xl!-c "rounded-md bg-indigo-600 px-3.5 py-2.5 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600")

(defn btn-xs! [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-xs!-c (:class opts)]})] content)
    (into [:button {:class [btn-xs!-c (:class opts)]} opts] content)))

(defn btn-sm! [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-sm!-c (:class opts)]})] content)
    (into [:button {:class [btn-sm!-c (:class opts)]} opts] content)))

(defn btn! [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-md!-c (:class opts)]})] content)
    (into [:button {:class [btn-md!-c (:class opts)]} opts] content)))

(defn btn-lg! [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-lg!-c (:class opts)]})] content)
    (into [:button {:class [btn-lg!-c (:class opts)]} opts] content)))

(defn btn-xl! [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-lg!-c (:class opts)]})] content)
    (into [:button {:class [btn-lg!-c (:class opts)]} opts] content)))

(defn demo-row [& content]
  [:div.bg-white.px-4.py-8
   {:class "w-full overflow-hidden rounded-lg ring-1 ring-slate-900/10"}
   (into [:div.mx-auto.flex.max-w-3xl.flex-col.items-center.justify-start.space-y-4.sm:flex-row.sm:items-end.sm:justify-around.sm:space-y-0]
         content)])

(def h2-c "truncate text-base/7 font-medium text-slate-900")
(defn h2 [opts & content]
  (if (map? opts)
    (into [:h2 (merge opts {:class [h2-c (:class opts)]})] content)
    (into [:h2 {:class [h2-c (:class opts)]} opts] content)))



(def btn-xs-c  "rounded-sm bg-white px-2 py-1 text-xs font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50 flex items-center space-x-1")
(def btn-sm-c  "rounded-sm bg-white px-2 py-1 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50")
(def btn-c     "rounded-md bg-white px-2.5 py-1.5 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50")
(def btn-lg-c  "rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50")
(def btn-xl-c  "rounded-md bg-white px-3.5 py-2.5 text-sm font-semibold text-gray-900 ring-1 shadow-xs ring-gray-300 ring-inset hover:bg-gray-50")

(defn btn-xs [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-xs-c (:class opts)]})] content)
    (into [:button {:class [btn-xs-c (:class opts)]} opts] content)))

(defn btn-sm [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-sm-c (:class opts)]})] content)
    (into [:button {:class [btn-sm-c (:class opts)]} opts] content)))

(defn btn [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-c (:class opts)]})] content)
    (into [:button {:class [btn-c (:class opts)]} opts] content)))

(defn btn-lg [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-lg-c (:class opts)]})] content)
    (into [:button {:class [btn-lg-c (:class opts)]} opts] content)))

(defn btn-xl [opts & content]
  (if (map? opts)
    (into [:button (merge opts {:class [btn-lg-c (:class opts)]})] content)
    (into [:button {:class [btn-lg-c (:class opts)]} opts] content)))

(def plus-icon
  [:svg.w-4.h-4.inline {:xmlns "http://www.w3.org/2000/svg" :viewBox "0 0 16 16" :fill "currentColor" :aria-hidden "true" :data-slot "icon"} [:path {:d "M8.75 3.75a.75.75 0 0 0-1.5 0v3.5h-3.5a.75.75 0 0 0 0 1.5h3.5v3.5a.75.75 0 0 0 1.5 0v-3.5h3.5a.75.75 0 0 0 0-1.5h-3.5v-3.5Z"}]])


(defn update-btn [context request params]
  [:span "Done!"])

(defn index-html [context request]
  (h/hiccup-response
   request
   [:div {:class "max-w-container relative mx-auto mt-20 w-full px-8 sm:px-12 lg:px-8"}
    (h2 "Primary buttons")
    (demo-row
     (btn-xs! "Button text")
     (btn-sm! "Button text")
     (btn! "Button text")
     (btn-lg! "Button text")
     (btn-xl! "Button text"))

    (h2 "Secondary buttons")
    (demo-row
     (btn-xs "Button text")
     (btn-sm "Button text")
     (btn "Button text")
     (btn-lg "Button text")
     (btn-xl "Button text"))

    (h2 "With icons")
    (demo-row
     (btn-xs {:id "btn" :hx-get (h/rpc #'update-btn)} plus-icon [:span "Button text"])
     (btn-sm "Button text")
     (btn "Button text")
     (btn-lg "Button text")
     (btn-xl "Button text"))

    [:div.mt-4
     [:div {:class btn-c}
      [:svg.size-6 {:xmlns "http://www.w3.org/2000/svg" :fill "none" :viewBox "0 0 24 24" :stroke-width "1.5" :stroke "currentColor"} [:path {:stroke-linecap "round" :stroke-linejoin "round" :d "m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z"}]]]]

    [:div.bg-white.p-6.mt-4
     (h2 {:class "py-2 mt-4 flex space-x-4 border-b border-zink-300"}
         [:span.flex-1 "My Header"]
         [:input#name {:type "text" :name "name" :class "block rounded-full bg-white px-4 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6" :placeholder "Jane Smith"}]        (btn  "Make it")
         (btn! "Do it")
         )]


    ]
   ))




(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path  "/ui/tailwindui"       :fn #'index-html})

  )



(comment
  (def context mysystem/context)

  (mount-routes context)


 )
