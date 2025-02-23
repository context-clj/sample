(ns mysystem.ui.sdc
  (:require
   [system]
   [pg]
   [pg.repo]
   [http]
   [mysystem.ui.helpers :as h]
   [clojure.java.io]
   [clojure.string :as str]
   [fhirpath.core :as fp]
   [cheshire.core]))


(def form
  {:subjectType ["Patient" "Person"],
   :date "2017-08-04T10:35:45-04:00",
   :meta
   {:versionId "1",
    :lastUpdated "2024-07-29T02:08:36.000-04:00",
    :profile
    ["http://hl7.org/fhir/4.0/StructureDefinition/Questionnaire"],
    :tag [{:code "lformsVersion: 36.10.2"}]},
   :name
   "Vital signs, weight, height, head circumference, oximetry, BMI, & BSA panel",
   :item
   [{:type "decimal",
     :code
     [{:system "http://loinc.org",
       :code "2710-2",
       :display "SaO2 % BldC Oximetry"}],
     :extension
     [{:url "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
       :valueCoding {:code "%", :system "http://unitsofmeasure.org"}}],
     :linkId "/2710-2",
     :text "SaO2 % BldC Oximetry"}
    {:type "quantity",
     :code
     [{:system "http://loinc.org",
       :code "3141-9",
       :display "Weight Measured"}],
     :extension
     [{:url
       "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-observationLinkPeriod",
       :valueDuration
       {:value 1, :unit "year", :system "http://ucum.org", :code "a"}}
      {:url
       "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-observationExtract",
       :valueBoolean true}
      {:url
       "http://hl7.org/fhir/StructureDefinition/questionnaire-unitOption",
       :valueCoding
       {:code "[lb_av]",
        :display "lbs",
        :system "http://unitsofmeasure.org"}}
      {:url
       "http://hl7.org/fhir/StructureDefinition/questionnaire-unitOption",
       :valueCoding
       {:code "kg",
        :display "kgs",
        :system "http://unitsofmeasure.org"}}],
     :linkId "/3141-9",
     :text "Weight Measured",
     :initial
     [{:valueQuantity
       {:unit "lbs",
        :code "[lb_av]",
        :system "http://unitsofmeasure.org"}}]}
    {:type "decimal",
     :code
     [{:system "http://loinc.org",
       :code "8287-5",
       :display "Head Circumf OFC by Tape measure"}],
     :extension
     [{:url "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
       :valueCoding {:code "cm", :system "http://unitsofmeasure.org"}}],
     :linkId "/8287-5",
     :text "Head Circumf OFC by Tape measure"}
    {:type "quantity",
     :code
     [{:system "http://loinc.org",
       :code "8302-2",
       :display "Bdy height"}],
     :extension
     [{:url
       "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-observationLinkPeriod",
       :valueDuration
       {:value 1, :unit "year", :system "http://ucum.org", :code "a"}}
      {:url
       "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-observationExtract",
       :valueBoolean true}
      {:url
       "http://hl7.org/fhir/StructureDefinition/questionnaire-unitOption",
       :valueCoding
       {:code "[in_i]",
        :display "inches",
        :system "http://unitsofmeasure.org"}}
      {:url
       "http://hl7.org/fhir/StructureDefinition/questionnaire-unitOption",
       :valueCoding
       {:code "[ft_i]",
        :display "feet",
        :system "http://unitsofmeasure.org"}}
      {:url
       "http://hl7.org/fhir/StructureDefinition/questionnaire-unitOption",
       :valueCoding
       {:code "cm",
        :display "centimeters",
        :system "http://unitsofmeasure.org"}}
      {:url
       "http://hl7.org/fhir/StructureDefinition/questionnaire-unitOption",
       :valueCoding
       {:code "m",
        :display "meters",
        :system "http://unitsofmeasure.org"}}],
     :linkId "/8302-2",
     :text "Bdy height",
     :initial
     [{:valueQuantity
       {:unit "inches",
        :code "[in_i]",
        :system "http://unitsofmeasure.org"}}]}
    {:type "decimal",
     :code
     [{:system "http://loinc.org",
       :code "8310-5",
       :display "Body temperature"}],
     :extension
     [{:url "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
       :valueCoding {:code "Cel", :system "http://unitsofmeasure.org"}}],
     :linkId "/8310-5",
     :text "Body temperature"}
    {:type "decimal",
     :code
     [{:system "http://loinc.org", :code "8462-4", :display "BP dias"}],
     :extension
     [{:url "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
       :valueCoding
       {:code "mm[Hg]", :system "http://unitsofmeasure.org"}}],
     :linkId "/8462-4",
     :text "BP dias"}
    {:type "decimal",
     :code
     [{:system "http://loinc.org", :code "8480-6", :display "BP sys"}],
     :extension
     [{:url "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
       :valueCoding
       {:code "mm[Hg]", :system "http://unitsofmeasure.org"}}],
     :linkId "/8480-6",
     :text "BP sys"}
    {:type "decimal",
     :code
     [{:system "http://loinc.org",
       :code "8867-4",
       :display "Heart rate"}],
     :extension
     [{:url "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
       :valueCoding
       {:code "{beats}/min", :system "http://unitsofmeasure.org"}}],
     :linkId "/8867-4",
     :text "Heart rate"}
    {:type "decimal",
     :code
     [{:system "http://loinc.org",
       :code "9279-1",
       :display "Resp rate"}],
     :extension
     [{:url "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
       :valueCoding
       {:code "{breaths}/min", :system "http://unitsofmeasure.org"}}],
     :linkId "/9279-1",
     :text "Resp rate"}
    {:type "decimal",
     :code
     [{:system "http://loinc.org", :code "3140-1", :display "BSA Derived"}],
     :extension
     [{:url "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
       :valueCoding {:code "m2", :system "http://unitsofmeasure.org"}}],
     :linkId "/3140-1",
     :text "BSA Derived"}
    {:type "decimal",
     :code
     [{:system "http://loinc.org", :code "39156-5", :display "BMI"}],
     :extension
     [{:url "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-calculatedExpression",
       :valueExpression
       {:description "BMI calculation",
        :language "text/fhirpath",
        :expression "((%weight/%height/%height*10 +0.5) div 1)/10"
        }}
      {:url "http://hl7.org/fhir/StructureDefinition/questionnaire-unit",
       :valueCoding
       {:code "kg/m2", :system "http://unitsofmeasure.org"}}],
     :linkId "/39156-5",
     :text "BMI"}],
   :resourceType "Questionnaire",
   :title
   "Vital signs, weight, height, head circumference, oximetry, BMI, & BSA panel",
   :extension
   [{:url "http://hl7.org/fhir/StructureDefinition/variable",
     :valueExpression
     {:name "weightQuantity",
      :language "text/fhirpath",
      :expression "%resource.item.where(linkId='/3141-9').answer.value"
      :ccsQuery "input[data-link-id='/3141-9']"
      }}
    {:url "http://hl7.org/fhir/StructureDefinition/variable",
     :valueExpression
     {:name "weight",
      :language "text/fhirpath",
      :expression "%weightQuantity.toQuantity('kg').value"
      :ccsQuery "#form input[data-link-id='/3141-9']"
      }}
    {:url "http://hl7.org/fhir/StructureDefinition/variable",
     :valueExpression
     {:name "heightQuantity",
      :language "text/fhirpath",
      :expression "%resource.item.where(linkId='/8302-2').answer.value"
      :ccsQuery "#form input[data-link-id='/8302-2']"}}
    {:url "http://hl7.org/fhir/StructureDefinition/variable",
     :valueExpression
     {:name "height",
      :language "text/fhirpath",
      :expression "%heightQuantity.toQuantity('m').value"}}],
   :status "draft",
   :id "74728-7-modified-x",
   :url
   "http:/lforms-fhir.nlm.nih.gov/baseR4/Questionnaire/74728-7-modified-x",
   :code
   [{:system "http://loinc.org",
     :code "74728-7_modified",
     :display
     "Vital signs, weight, height, head circumference, oximetry, BMI, & BSA panel"}],
   :identifier [{:system "http://loinc.org", :value "74728-7_modified"}]}

  )

;; TODO move cells logic into separate files


(defn get-units [item]
  (->> (:extension item)
       (filter (fn [x] (=  (:url x) "http://hl7.org/fhir/StructureDefinition/questionnaire-unit")))
       first
       :valueCoding
       :code))

(defn get-unit-options [item]
  (->> (:extension item)
       (filter (fn [x] (=  (:url x) "http://hl7.org/fhir/StructureDefinition/questionnaire-unitOption")))
       (mapv (fn [x] (:valueCoding x)))))

(defn get-variables [item]
  (->> (:extension item)
       (filter (fn [x] (= (:url x) "http://hl7.org/fhir/StructureDefinition/variable")))))


(defn get-calc-expr [item]
  (->> (:extension item)
       (filter (fn [x] (= (:url x) "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-calculatedExpression")))
       (first)
       :valueExpression))




(defn path-assoc [obj [p & ps] v]
  (cond
    (nil? p) v
    (string? p) (assoc obj p (path-assoc (get obj p) ps v))
    (number? p) (assoc obj p (path-assoc (get obj p) ps v))
    (map p) (path-assoc (merge p obj) ps v)))

(defn build-qr [params]
  (->> params
       (reduce (fn [acc [k v]]
                 (if (str/blank? v)
                   acc
                   (path-assoc acc (cheshire.core/parse-string k) v)))
               {})))

(defn arrayify [r]
  (cond
    (and (map? r) (number? (first (keys r))))
    (->> r (sort-by first) (mapv second) (mapv arrayify))
    (map? r)
    (->> r (reduce (fn [acc [k v]] (assoc acc k (arrayify v))) {}))
    :else
    r))

(defn eval-vars [form qr]
  (->> (get-variables form)
       (reduce (fn [vars {{e :expression nm :name} :valueExpression}]
                 (let [res (fp/fp e qr vars)]
                   (assoc vars (keyword nm) res)))
               {:resource qr})))

(comment
  (def fd
    {"[\"item\",4,{\"linkId\":\"/8310-5\"},\"answer\",\"valueDecimal\",\"value\"]" "",
     "[\"item\",8,{\"linkId\":\"/9279-1\"},\"answer\",\"valueDecimal\",\"value\"]" "",
     "[\"item\",7,{\"linkId\":\"/8867-4\"},\"answer\",\"valueDecimal\",\"value\"]" "",
     "[\"item\",3,{\"linkId\":\"/8302-2\"},\"answer\",\"valueQuantity\",\"unit\"]" "inches",
     "[\"item\",3,{\"linkId\":\"/8302-2\"},\"answer\",\"valueQuantity\",\"value\"]" "100",
     "[\"item\",1,{\"linkId\":\"/3141-9\"},\"answer\",\"valueQuantity\",\"value\"]" "100",
     "[\"item\",9,{\"linkId\":\"/3140-1\"},\"answer\",\"valueDecimal\",\"value\"]" "",
     "[\"item\",10,{\"linkId\":\"/39156-5\"},\"answer\",\"valueDecimal\",\"value\"]" "",
     "[\"item\",6,{\"linkId\":\"/8480-6\"},\"answer\",\"valueDecimal\",\"value\"]" "",
     "[\"item\",2,{\"linkId\":\"/8287-5\"},\"answer\",\"valueDecimal\",\"value\"]" "",
     "[\"item\",1,{\"linkId\":\"/3141-9\"},\"answer\",\"valueQuantity\",\"unit\"]" "lbs",
     "[\"item\",0,{\"linkId\":\"/2710-2\"},\"answer\",\"valueDecimal\",\"value\"]" "",
     "[\"item\",5,{\"linkId\":\"/8462-4\"},\"answer\",\"valueDecimal\",\"value\"]" ""})

  (def qr 
    (-> (build-qr fd)
        (arrayify)))

  (fp/fp "%resource.item.where(linkId='/3141-9').answer.value" qr {:resource qr})

  (fp/fp "%resource.item.where(linkId='/3141-9').answer" qr {:resource qr})
  (fp/fp "%resource.item.where(linkId='/3141-9')" qr {:resource qr})
  (fp/fp "%resource.item" qr {:resource qr})


  (fp/fp "%resource.item.where(linkId='/3141-9').answer.value.toQuantity('kg')" qr {:resource qr})

  (fp/parse "%resource.item.where(linkId='/3141-9').answer.value")

  (fp/fp "((%weight/%height/%height*10 +0.5) div 1)/10" qr (eval-vars form qr))

  )

(defn preview-html [_context request]
  (let [fd (http/form-decode (slurp (:body request)))
        res (arrayify (build-qr fd))
        vars (eval-vars form res)
        exprs (get res "exprs")
        calcs (->> exprs
                  (reduce (fn [acc [k e]]
                            (assoc acc k (try (fp/fp e res vars) (catch Exception e (str "ERROR:" (.getMessage e)))))
                            ) {}))]
    (h/fragment
     [:div
      (for [[k v] calcs]
        (when v
          [:div
           [:script (h/raw (str "document.getElementById(" (pr-str k) ").value = " v))]
           [:pre (str "document.getElementById(" (pr-str k) ")?.value = " v)]]))
      [:pre (cheshire.core/generate-string vars {:pretty true})]])))


(def inp-c "border px-2 py-1  rounded bg-gray-100")

(defn capitalize [s]
  (when s
    (str (str/upper-case (first s))
         (subs s 1))))

(defn index-html [context request]
  (h/layout
   context request
   {:content
    [:div#form {:class "flex items-top"}
     [:script {:src "/static/fhirpath.min.js"}]
     [:form {:class "p-6 border rounded" :hx-post "/ui/sdc/preview" :hx-target "#result"}
      (h/h1 (:name form))
      [:div {:class "border-b"}]
      (->> (:item form)
           (map-indexed
            (fn [idx item]
        [:div {:class "mt-4"}
         [:label {:for (:linkId item) :class "flex space-x-4 items-center"}
          [:span {:class "block mt-1 text-gray-500"} (:text item) [:span {:class "text-xs"}  " (" (:linkId item) ")"]]
          (cond
            (or (= "decimal" (:type item))
                (= "quantity" (:type item)))
            (let [path ["item" idx  {:linkId (:linkId item)} "answer" (str "value" (capitalize (:type item)))]]
              [:div
               [:div {:class "flex space-x-2 items-baseline"}
                [:input {:id (:linkId item)
                         :name (cheshire.core/generate-string (conj path :value))
                         :data-linkid (:linkId item)
                         :data-path (cheshire.core/generate-string (conj path :value))
                         :class [inp-c]
                         :type "number"}]
                (when-let [options (seq (get-unit-options item))]
                  [:select {:class inp-c
                            :data-linkid (:linkId item)
                            :name (cheshire.core/generate-string (conj path :unit))
                            :data-path (cheshire.core/generate-string (conj path :unit))}
                   (for [opt options]
                     [:option {:value (:unit opt)} (or (:display opt) (:unit opt))])])
                (when-let [units (get-units item)]
                  [:span {:class "text-gray-500"} units])]
               (when-let [expr (get-calc-expr item)]
                 [:div
                  [:input {:type "hidden" :name (cheshire.core/generate-string [:exprs (:linkId item)]) :value (:expression expr)}]
                  [:pre (:expression expr)]])])
            :else
            [:pre (pr-str (dissoc item :text))])
          ]])))
      [:div {:class "mt-4 border-t border-gray-300 py-3 flex"}
       [:div {:class "flex-1"}]
       (h/btn {} "Save")]]
     [:div {:class "px-4"}
      [:details
       [:summary "Vars"]
       [:div {:class "bg-gray-100 border rounded p-4 text-xs"}
        (for [{e :valueExpression}  (get-variables form)]
          (let [id (str "var-" (:name e))]
            [:div
             [:div {:class "py-1 text-xs flex items-center"
                    :data-fhirpathname (:name e)
                    :data-fhirpathexpr (:expression e)
                    :data-target id}
              [:b (:name e) " ="]
              [:pre {:class "bg-gray-100 px-2"} (:expression e)]]
             [:pre {:id id :class "text-blue-800"}]]))]]
      [:details
       [:summary "js-resource"]
       [:pre#debug  {:class "mt-4 border rounded-md p-4 text-xs bg-gray-100 flex-1"}]]
      [:details
       [:summary "sever-submit"]
       [:pre#result {:class "mt-4 border rounded-md p-4 text-xs bg-gray-100 flex-1"}]]
      [:details
       [:summary "server"]
       [:pre#server-result {:class "mt-4 border rounded-md p-4 text-xs bg-gray-100 flex-1"}]]]
     [:script {:src "/static/forms.js"}]]}))

(defn mount-routes [context]

  (http/register-endpoint context {:method :get :path  "/ui/sdc"             :fn #'index-html})
  (http/register-endpoint context {:method :post :path  "/ui/sdc/preview"     :fn #'preview-html})

  )

(comment
  (def context mysystem/context)

  (mount-routes context)


 )
