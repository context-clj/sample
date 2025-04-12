(ns mysystem.semantic
  (:require [http]
            [uui]
            [uui.heroicons :as ico]))


(defn document [context request body]
  [:html
   [:head
    [:script {:src "/static/htmx.min.js"}]
    [:script {:src "/static/app.js"}]
    [:link {:rel "stylesheet", :href "/static/app.build.css"}]
    [:meta {:name "htmx-config", :content "{\"scrollIntoViewOnBoost\":false}"}]]
   body])

(defn boost-response [context request body]
  (uui/response (if (get-in request [:headers "hx-boosted"]) body (document context request body))))

(def menu
  [{:title "Products"
    :submenu [[["/ui/semantic/aidbox"  {:header "Aidbox"  :text "FHIR Server and Database"}]
               ["/ui/semantic/forms"   {:header "Forms"   :text "FHIR SDC builder and runner"}]
               ["/ui/semantic/termbox" {:header "Termbox" :text "Terminology as a Service"}]]
              [["/ui/semantic/aidbox"  {:header "CDA 2 FHIR"  :text "FHIR Server and Database"}]
               ["/ui/semantic/forms"   {:header "MPIBox"   :text "FHIR SDC builder and runner"}]
               ["/ui/semantic/termbox" {:header "Auditbox" :text "Terminology as a Service"}]]
              [["/ui/semantic/aidbox"  {:header "Smartbox"  :text "Smartbox"}]
               ["/ui/semantic/forms"   {:header "Planbox"   :text "Health Plan Box"}]
               ["/ui/semantic/termbox" {:header "FHIRBase" :text "Terminology as a Service"}]]]}
   {:title "Services" :href "/ui/services"}
   {:title "Docs" :href "/ui/docs"}
   {:title "Blog" :href "/ui/blog"}
   {:title "FHIR Meetups" :href "/ui/meetups"}
   {:title "Company"
    :submenu [[["/ui/semantic/aidbox"  {:header "History"  :text "FHIR Server and Database"}]
               ["/ui/semantic/forms"   {:header "Contact Us"   :text "FHIR SDC builder and runner"}]
               ["/ui/semantic/termbox" {:header "Career" :text "Terminology as a Service"}]]
              [["/ui/semantic/aidbox"  {:header "github"  :text "FHIR Server and Database"}]
               ["/ui/semantic/forms"   {:header "linkedin"   :text "FHIR SDC builder and runner"}]
               ["/ui/semantic/termbox" {:header "youtube" :text "Terminology as a Service"}]]]}])


(defn layout [context request main]
  (boost-response
   context request
   [:body.uui- {:hx-boost "true"}
    [:div {:class "shadow py-2"}
     [:header {:class "text-sm font-semibold"}
      [:nav
       [:ul {:class "w-full space-x-4 justify-center"}
        [:li
         [:a {:class "" :href "/ui/semantic"}
          [:img {:class "h-8" :src "https://cdn.prod.website-files.com/57441aa5da71fdf07a0a2e19/5a2ff50e669ec50001a59b5d_health-samurai.webp"}]]]
        [:li [:a {:id "product-section"
                  :class "px-4 !text-gray-600 flex items-center"
                  :href "#products"
                  :data-expand "sub-menu"
                  :data-toggle "products-menu"
                  :data-togglegroup "sub-menu"}
              [:span "Products"]
              (ico/chevron-right "expander inline-block ml-4 size-4")]]
        [:li [:a {:class "px-4 !text-gray-600" :href "/ui/semantic/forms"} "Services"]]
        [:li [:a {:class "px-4 !text-gray-600" :href "/ui/semantic/docs"} "Docs"]]
        [:li [:a {:class "px-4 !text-gray-600" :href "/ui/semantic/blog"} "Blog"]]
        [:li [:a {:class "px-4 !text-gray-600" :href "/ui/semantic/blog"} "FHIR Meetups"]]
        [:li [:a {:id "product-section"
                  :class "px-4 !text-gray-600 flex items-center"
                  :href "#products"
                  :data-expand "sub-menu"
                  :data-toggle "company-menu"
                  :data-togglegroup "sub-menu"
                  }
              [:span "Company"]
              (ico/chevron-right "expander inline-block ml-4 size-4")]]
        [:li {:class "flex-1"}]
        [:li [:a.button {} "Sign In"]]]]]]
    [:div {:id "sub-menu" :class "text-gray-500 shadow-md" :stylo "background-color: #f4f8fb;" :data-expanded "off"}
     [:header#products-menu {:data-hidden "yes"}
      [:nav {:id "products-nav" :class "flex py-12 space-x-8"}
       (for [section [[["/ui/semantic/aidbox"  {:header "Aidbox"  :text "FHIR Server and Database"}]
                       ["/ui/semantic/forms"   {:header "Forms"   :text "FHIR SDC builder and runner"}]
                       ["/ui/semantic/termbox" {:header "Termbox" :text "Terminology as a Service"}]]
                      [["/ui/semantic/aidbox"  {:header "CDA 2 FHIR"  :text "FHIR Server and Database"}]
                       ["/ui/semantic/forms"   {:header "MPIBox"   :text "FHIR SDC builder and runner"}]
                       ["/ui/semantic/termbox" {:header "Auditbox" :text "Terminology as a Service"}]]
                      [["/ui/semantic/aidbox"  {:header "Smartbox"  :text "Smartbox"}]
                       ["/ui/semantic/forms"   {:header "Planbox"   :text "Health Plan Box"}]
                       ["/ui/semantic/termbox" {:header "FHIRBase" :text "Terminology as a Service"}]]]]
         [:section {:class "flex-1 flex flex-col space-y-4"}
          (for [[url {h :header t :text}] section]
            [:a {:class "!text-gray-500 border border-gray-200 block hover:border-sky-400 hover:bg-white hover:shadow-md rounded-md p-5"
                 :href url}
             [:b {:class ""} h]
             [:p t]])])]]
     [:header#company-menu {:data-hidden "yes"}
      [:nav {:id "products-nav" :class "flex py-12 space-x-8 items-top"}
       (for [section [[["/ui/semantic/aidbox"  {:header "History"  :text "FHIR Server and Database"}]
                       ["/ui/semantic/forms"   {:header "Contact Us"   :text "FHIR SDC builder and runner"}]
                       ["/ui/semantic/termbox" {:header "Career" :text "Terminology as a Service"}]]
                      [["/ui/semantic/aidbox"  {:header "github"  :text "FHIR Server and Database"}]
                       ["/ui/semantic/forms"   {:header "linkedin"   :text "FHIR SDC builder and runner"}]
                       ["/ui/semantic/termbox" {:header "youtube" :text "Terminology as a Service"}]]]]
         [:section {:class "flex-1 flex flex-col space-y-4"}
          (for [[url {h :header t :text}] section]
            [:a {:class "!text-gray-500 border border-gray-200 block hover:border-sky-400 hover:bg-white hover:shadow-md rounded-md p-5"
                 :href url}
             [:b {:class ""} h]
             [:p t]])])
       [:section {:class "flex-1"}
        [:p
         "Contact Us..."
         [:ul
          [:b "Address:"]
          [:b "Phone"]]]]]]]
    [:main main]
    [:footer
     [:p (uui/raw "2023 Health Samurai. All rights reserved.")]]]))

(defn colapse-section [title content]
  (list
   [:div
    [:button {:class "px-6 py-2 w-full block text-lg flex items-center space-x-4 text-gray-500 text-left hover:text-gray-700 cursor-pointer"
              :data-expand title}
     (ico/check-circle "size-5" ico/outline)
     [:span {:class "flex-1"} title]
     (ico/chevron-right "expander inline-block ml-4 size-6")]]
   [:section {:id title :class "border border-gray-200 " :data-expanded "off"}
    [:div {:class "p-6"}
     content]]))

(defn ^{:http {:path "/ui/semantic/aidbox"}}
  aidbox [context request]
  (layout
   context request
   [:div.landing
    [:div {:class "py-24 border-b border-gray-300" :stylo "background-color: #f4f8fb;"}
     [:div.absolute.inset-x-0.-top-40.-z-10.transform-gpu.overflow-hidden.blur-3xl.sm:-top-80
      {:aria-hidden "true"}
      [:div {:class "relative left-[calc(50%-11rem)] aspect-1155/678 w-[36.125rem] -translate-x-1/2 rotate-[30deg] bg-linear-to-tr from-[#ff80b5] to-[#9089fc] opacity-30 sm:left-[calc(50%-30rem)] sm:w-[72.1875rem]" :style "clip-path: polygon(74.1% 44.1%, 100% 61.6%, 97.5% 26.9%, 85.5% 0.1%, 80.7% 2%, 72.5% 32.5%, 60.2% 62.4%, 52.4% 68.1%, 47.5% 58.3%, 45.2% 34.5%, 27.5% 76.7%, 0.1% 64.9%, 17.9% 100%, 27.6% 76.8%, 76.1% 97.7%, 74.1% 44.1%);"}]]
     [:article
      [:section {:class "flex space-x-8"}
       [:div {:class "flex-3"}
        [:h1 {:class "mt-4"} "Aidbox -" [:br] "FHIR Server" [:br] " & Database"]
        [:p {:class "mt-8 w-100 text-lg"} "Build interoperable, secure, and fast healthcare applications with FHIR-native PostgreSQL database, Comprehensive API, Granular Access Control, and SDK"]
        [:p {:class "mt-8 flex space-x-8"}
         [:a.button.lg {:class "bg-white shadow-md !text-red-600 hover:!text-sky-600" :href "#"}
          (ico/cloud "size-4 inline-block mr-2" ico/outline) " Try in Sandbox"]
         [:a.button.lg {:class "bg-white shadow-md" :href "#"}
          (ico/book-open "size-4 inline-block mr-2" ico/outline) "Dive into Documentation"]
         ]]
       [:div {:class "relative flex-3"}
        [:img#screen-1 {:class "absolute w-140 border border-gray-300 rounded-lg shadow-lg z-10 hover:z-100"
               :style "top: 55px; right: 40px;"
               :src "/static/db-console.jpg"}]
        [:img#screen-2 {:class "absolute w-140 border border-gray-300 rounded-lg shadow-lg z-20"
               :src "/static/aidbox.jpg"}]
        ]]]]
    [:div {:class "items-center text-gray-100 text-lg font-semibold py-2" :style "background-color: #011537; opacity: 0.8"}
     [:article
      [:p {:class "text-sm text-gray-400 font-mono font-semibold"} "# Run locally in couple of minutes!"]
      [:div {:class "flex items-center"}
       [:span {:class "text-gray-400"} "$>"]
       [:code#run {:class "flex-1 px-4 py-2  font-mono"}
        "mkdir mybox && cd mybox && curl -JO https://aidbox.app/runme && docker compose up "
        [:button {:class "px-4 hover:text-orange-500 cursor-pointer"
                  :data-copyfrom "run"}
         (ico/clipboard-document "copy-status size-6 inline-block")]]]]]

    [:article {:class "mt-24"}
     [:h2 "Key Features"]
     [:br]
     
     (colapse-section
      "FHIR CRUD, Transaction & Search API"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "FHIR Versions & IGs"
      [:div {:class "p-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "Terminology"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "FHIR Subscriptions"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "FHIR Database & SQL on FHIR"
      [:div {:class "p-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "Bulk Import/Export API"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "GraphQL API"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "Access Policy & Security Labels"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "SDK & Examples"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "BALP FHIR Audit & OpenTelemetry"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "SMART on FHIR & Patient API"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "Interactive Notebooks"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "Multi-tenancy & Sandboxes"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       ])
     (colapse-section
      "Self-hosting & Managed Aidbox in public clouds"
      [:div {:class "py-6"}
       [:p "SQL on FHIR is ....."]
       ])
     ]


    [:article {:class "mt-24"}
     [:h2 "Case Studies"]
     [:br]
     ]

    [:article {:class "mt-24"}
     [:h2 "Getting Started"]
     [:br]
     ]

    [:article {:class "mt-24"}
     [:h2 "Hosting & Deployment"]
     [:br]
     ]

    [:article {:class "mt-24"}
     [:h2 "Pricing & Support"]
     [:br]
     ]

    [:article {:class "mt-24"}
     [:h2 "Schedule Meeting"]
     [:br]
     ]

    ]
   ))

(defn ^{:http {:path "/ui/semantic/forms"}}
  forms [context request]
  (layout
   context request
   [:article
    [:h1 "forms"]])
  )

(defn ^{:http {:path "/ui/semantic/blog"}}
  blog [context request]
  (layout
   context request
   [:article
    [:h1 "blog"]]
   )
  )

(defn ^{:http {:path "/ui/semantic/docs"}}
  docs [context request]
  (layout
   context request
   [:div.landing
    [:div {:class "bg-gray-100 py-10"}
     [:article
      [:h1 "docs"]]]
    ]
   )
  )

(defn ^{:http {:path "/ui/semantic"}}
  index [context request]
  (layout
   context request
   (list
    [:article.landing
     [:h1 "Welcome to Health Samurai Company site"]
     [:p "We are FHIR company crafting developer tools for Health IT"]

     [:section
      [:h3 "Section Heading"]
      [:p "This is a subsection of the main content."]]

     [:section
      [:h3 "Another Section"]
      [:p "Another subsection with more information."]]]
    )))



(comment
  (def context mysystem/context)

  (http/register-ns-endpoints context *ns*)


  )
