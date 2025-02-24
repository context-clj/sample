(ns mysystem.ui.calendar
  (:require
   [system]
   [mysystem.ui.helpers :as h]
   [http]
   [clojure.string :as str]
            [rewrite-clj.node :as n]
   [cljfmt.core :as cljfmt]))

(comment
  (datetime context {} {})

  )



(defn calendar-month [year month]
  (let [date (java.time.LocalDate/of year month 1)
        days-in-month (.lengthOfMonth date)
        first-dow (.getValue (.getDayOfWeek date))
        offset (dec first-dow)
        weeks (partition 7 7 nil
                (concat (repeat offset nil)
                       (range 1 (inc days-in-month))))]
    [:div.calendar
     [:h3 (.format date
            (java.time.format.DateTimeFormatter/ofPattern "MMMM yyyy"))]

     [:table.calendar-table
      [:thead
       [:tr
        (for [day ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]]
          [:th day])]]

      [:tbody
       (for [week weeks]
         [:tr
          (for [day week
                :let [current (when day
                              (java.time.LocalDate/of year month day))
                      today (java.time.LocalDate/now)]]
            [:td.day
             {:class ["border px-2 py-1 hover:bg-blue-100 cursor-pointer text-center" (when (= current today) "bg-blue-200")]}
             (or day "")])])]]]))

(defn preview [context request opts]
  [:pre {:class "mt-4 border p-4" }(pr-str (http/form-decode (slurp (:body request))))])

(def monthes ["January" "February" "March" "April" "May" "June" "July" "August" "September" "October" "November" "December"])

(defn get-holydays [date]
  (cheshire.core/parse-string
   (slurp (str "https://openholidaysapi.org/PublicHolidays?countryIsoCode=CH&languageIsoCode=EN&validFrom=" (.getYear date) "-" (.getMonthValue date) "-01&validTo=" (.getYear date) "-" (.getMonthValue date) "-" (.lengthOfMonth date)))
   keyword))

(get-holydays (java.time.LocalDate/now))

(def styles "
input[type=radio] {width: 0; height: 0;}
label:has(input[type=radio]:checked)  {
 background-color: oklch(.901 .058 230.902);
 color: black;
}

.tooltip {
  position: relative;
}

.tooltiptext {
  visibility: hidden;
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
}

.tooltip:hover .tooltiptext {
  visibility: visible;
}
")

(defn calendar-month [context request {year :year month :month selected-date :date}]
  (let [year (if (string? year) (Integer/parseInt year) year)
        month (if (string? month) (Integer/parseInt month) month)
        [year month] (cond (= month 0) [(dec year) 12]
                           (= month 13) [(inc year) 1]
                           :else [year month])
        date (java.time.LocalDate/of year month 1)
        days-in-month (.lengthOfMonth date)
        first-dow (.getValue (.getDayOfWeek date))
        offset (dec first-dow)
        weeks (partition 7 7 nil
                (concat (repeat offset nil)
                       (range 1 (inc days-in-month))))
        today (java.time.LocalDate/now)
        holidays (group-by :startDate (get-holydays date))]
    [:div#calendar
     [:style styles]
     [:form {:hx-post (h/rpc #'preview)
             :hx-trigger "change"
             :hx-target "#result"}
      [:table.calendar-table {:class "border rounded shadow-md"}
       [:thead
        [:tr {:class "border-b"}
         [:td [:a {:class "block px-2 py-2 text-sky-600 cursor-pointer"
                   :hx-target "#calendar"
                   :hx-get (h/rpc #'calendar-month {:year year :month (dec month)})} "prev"]]
         [:td {:colspan 5}
          [:div {:class "text-center flex space-x-4"}
           [:select {:name "month"
                     :hx-get (h/rpc #'calendar-month {:year year})
                     :hx-target "#calendar"}
            (->> monthes
                 (map-indexed
                  (fn [i m]
                    [:option {:value (inc i) :selected (when (= (inc i) (.getMonthValue date)) true)} m])))]
           [:select {:name "year"
                     :hx-get (h/rpc #'calendar-month {:month month})
                     :hx-target "#calendar"}
            (for [y (range (- (.getYear date) 10) (+ (.getYear date) 10))]
              [:option {:value y :selected (when (= y (.getYear date)) true)} y])]]]
         [:td [:a {:class "block px-2  py-2 text-sky-600 cursor-pointer"
                   :hx-target "#calendar"
                   :hx-get (h/rpc #'calendar-month {:year year :month (inc month)})} "next"]]]
        [:tr
         (for [day ["Mon" "Tue" "Wed" "Thu" "Fri" "Sat" "Sun"]]
           [:th {:class "px-2 py-1 font-bold text-gray-500 border border-gray-150"}day])]]
       [:tbody
        (for [week weeks]
          [:tr
           (for [day week
                 :let [current (when day (java.time.LocalDate/of year month day))]]
             [:td.day
              {:class ["border border-gray-150" ]}
              (when day
                (let [d (.toString current)
                      hss (get holidays d)]
                  [:label.tooltip {:class ["block px-2 py-1 rounded-md hover:bg-gray-100 cursor-pointer text-center text-gray-500"
                                           (when (= current today) "border border-sky-300 z-100")]}
                   (when hss [:div {:class "bg-green-200 w-2 h-2 absolute top-0 right-0 rounded-full"}])
                   [:input.radio-date {:type "radio" :name "selected-date" :value d}]
                   day
                   (when hss
                     [:div.tooltiptext {:class "border shadow-md p-4 bg-white whitespace-nowrap"}
                      (for [hs hss]
                        [:div (get-in hs [:name 0 :text])])])])
                )])])]]]
     [:div#result ]]))



(defn index-html [context request]
  (let [today (java.time.LocalDate/now)]
    (h/layout
     context request
     {:content
      [:div
       (calendar-month context request {:year (.getYear today) :month (.getMonthValue today)})]})))






(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path  "/ui/calendar"       :fn #'index-html})

  )



(comment
  (def context mysystem/context)

  (mount-routes context)


 )
