(ns mysystem.global
  (:require
   [system]
   [uui.heroicons :as ico]
   [uui]
   [mysystem.ui.helpers :as h]
   [http]))


(def fhir-resource-types
  ["Account" "ActivityDefinition" "AdverseEvent" "AllergyIntolerance" "Appointment" "AppointmentResponse" "AuditEvent" "Basic" "Binary" "BiologicallyDerivedProduct" "BodyStructure" "Bundle" "CapabilityStatement" "CarePlan" "CareTeam" "CatalogEntry" "ChargeItem" "ChargeItemDefinition" "Claim" "ClaimResponse" "ClinicalImpression" "CodeSystem" "Communication" "CommunicationRequest" "CompartmentDefinition" "Composition" "ConceptMap" "Condition" "Consent" "Contract" "Coverage" "CoverageEligibilityRequest" "CoverageEligibilityResponse" "DetectedIssue" "Device" "DeviceDefinition" "DeviceMetric" "DeviceRequest" "DeviceUseStatement" "DiagnosticReport" "DocumentManifest" "DocumentReference" "DomainResource" "EffectEvidenceSynthesis" "Encounter" "Endpoint" "EnrollmentRequest" "EnrollmentResponse" "EpisodeOfCare"
   "EventDefinition" "Evidence" "EvidenceVariable" "ExampleScenario" "ExplanationOfBenefit" "FamilyMemberHistory" "Flag" "Goal" "GraphDefinition" "Group" "GuidanceResponse" "HealthcareService" "ImagingStudy" "Immunization" "ImmunizationEvaluation" "ImmunizationRecommendation" "ImplementationGuide" "InsurancePlan" "Invoice"
   "Library" "Linkage" "List" "Location" "Measure" "MeasureReport" "Media" "Medication" "MedicationAdministration" "MedicationDispense" "MedicationKnowledge" "MedicationRequest" "MedicationStatement" "MedicinalProduct" "MedicinalProductAuthorization" "MedicinalProductContraindication" "MedicinalProductIndication" "MedicinalProductIngredient" "MedicinalProductInteraction"
   "MedicinalProductManufactured" "MedicinalProductPackaged" "MedicinalProductPharmaceutical" "MedicinalProductUndesirableEffect" "MessageDefinition" "MessageHeader" "MolecularSequence" "NamingSystem" "NutritionOrder" "Observation" "ObservationDefinition" "OperationDefinition" "OperationOutcome" "Organization" "OrganizationAffiliation" "Parameters"
   "Patient" "PaymentNotice" "PaymentReconciliation" "Person" "PlanDefinition" "Practitioner" "PractitionerRole" "Procedure" "Provenance" "Questionnaire" "QuestionnaireResponse" "RelatedPerson" "RequestGroup" "ResearchDefinition" "ResearchElementDefinition"
   "ResearchStudy" "ResearchSubject" "Resource" "RiskAssessment" "RiskEvidenceSynthesis" "Schedule" "SearchParameter" "ServiceRequest" "Slot" "Specimen" "SpecimenDefinition" "StructureDefinition" "StructureMap" "Subscription" "Substance" "SubstanceNucleicAcid"
   "SubstancePolymer" "SubstanceProtein" "SubstanceReferenceInformation" "SubstanceSourceMaterial" "SubstanceSpecification" "SupplyDelivery" "SupplyRequest" "Task" "TerminologyCapabilities" "TestReport" "TestScript" "ValueSet" "VerificationResult" "VisionPrescription"])

(def blocks
  [[{:title "Resource Types"
     :pinned ["Patient" "Practitioner" "Organization" "Encounter" "Appointment"]
     :others fhir-resource-types}]
   [
    {:title "API"
     :pinned ["REST Console"
              "Operations"]
     :others ["GraphQL"
              "Import"
              "$everything"]}
    {:title "Auth"
     :pinned ["User"
              "AccessPolicy"
              "App"]
     :others ["AuditEvent"
              "Session"
              "TokenIntrospector"]}
    {:title "Database"
     :pinned ["Console"
              "Running Queries"]
     :others ["Tables"
              "ViewDefinition"
              "Indexes"]}]
   [{:title "FHIR"
     :pinned ["Package"
              "StructureDefinition"
              "Profile"
              "Extension"]
     :others ["SearchParameter"
              "CompartmentDefinition"]}
    {:title "Terminology"
     :pinned ["CodeSystem"
              "ValueSet"
              "ValueSet/$expand"
              "CodeSystem/$lookup"]
     :others ["ConceptMap"
              "NameSystem"
              "Concept"]}]
   [{:title "Notebooks"
     :pinned ["Import Data" "FHIR API" "CRUD"]
     :others ["Search" "SQL" "SQL on FHIR"]}
    {:title "Docs"
     :pinned ["Getting Started" "Architecture" "Tutorials"]
     :others ["CRUD" "Transaction" "Search"]}
    ]
   [{:title "Recent"
     :pinned []
     :others ["DB Console"
              "Patient: John Doe"
              "Encounter"]}]
   ])

(defn menu-dialog [context request opts]
  [:dialog#main-menu {:class ["bg-white rounded-lg shadow-xl h-[94vh] w-[94vw] p-6 text-gray-600"]
                      :style "margin-left: 4rem; margin-top: 1rem; outline: none;"}
   [:div {:class "grid grid-cols-1"}
    [:input
     {:class "border border-gray-300 col-start-1 row-start-1 block w-full rounded-xl py-1.5 pr-3 pl-10 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:pl-9 sm:text-sm/6"
      :type "search"
      :name "search",}]
    (ico/magnifying-glass "size-5 pointer-events-none col-start-1 row-start-1 ml-3 size-5 self-center text-gray-400 sm:size-4 ")]
   [:div {:class "my-2 flex items-center space-x-4 justify-center text-xs font-semibold text-gray-500"}
    [:div {:class "border rounded-md px-4 py-1 bg-gray-50 flex space-x-2"}
     (ico/home "size-4" :outline)
     [:span "Getting Started"]]
    [:div {:class "border rounded-md px-4 py-1 bg-gray-50 flex space-x-2"}
     (ico/arrow-up-on-square "size-4" :outline)
     [:span "REST Console"]]
    [:div {:class "border rounded-md px-4 py-1 bg-gray-50 flex space-x-2"}
     (ico/circle-stack "size-4" :outline)
     [:span "DB Console"]]
    [:div {:class "border rounded-md px-4 py-1 bg-gray-50 flex space-x-2"}
     (ico/cog "size-4" :outline)
     [:span "Settings"]]
    [:div {:class "border rounded-md px-4 py-1 bg-gray-50 flex space-x-2"}
     (ico/document-text "size-4" :outline)
     [:span "Notebooks"]]
    [:div {:class "border rounded-md px-4 py-1 bg-gray-50 flex space-x-2"}
     (ico/home "size-4" :outline)
     [:span "Forms"]]
    ]
   [:div {:class "px-4 flex items-top space-x-8 text-gray-500" }
    (for [block-group blocks]
      [:div {:class "flex-1"} 
       (for [block block-group]
         [:div {:class "mt-4"}
          [:h3 {:class "font-bold text-gray-400 text-xs border-b mb-2"} (:title block)]
          (for [p (:pinned block)]
            [:a {:class "cursor-pointer px-2 py-1 block hover:bg-gray-100 flex items-center space-x-2"} (ico/bookmark "size-4 text-gray-400") [:span p]])
          (for [p (:others block)]
            [:a {:class "cursor-pointer px-2 py-1 block hover:bg-gray-100 flex items-center space-x-2"} (ico/bookmark "size-4 text-gray-400" :outline) [:span p]])])])
    ]
   ])

(defn menu-button [context request]
  [:div
   [:a {:class "block hover:text-sky-600 cursor-pointer"
        :onclick "document.getElementById('main-menu').showModal()"}
    (ico/bars-3 "size-6")]
   [:script (uui/raw "setTimeout(() => { document.getElementById('main-menu').showModal();}, 100);")]
   (menu-dialog context request {})])

(defn index-html [context request]
  (h/hiccup-response
   context
   [:div {:class ""}
    [:div {:style "position: absolute; top: 0; left:0; bottom: 0;" :class "bg-gray-200 px-4 py-4 flex flex-col space-y-4 text-gray-500"}
     (menu-button context request)
     (ico/users "size-6")
     (ico/truck "size-6")
     (ico/calendar "size-6")
     (ico/building-office "size-6")
     [:div {:class "border-b-2 border-gray-300 shadow-md"}]
     (ico/circle-stack "size-6")
     ]
    [:div {:class "bg-white"}
     "Here"]

    ]))


(defn mount-routes [context]
  (http/register-endpoint context {:method :get :path  "/ui/global"       :fn #'index-html})
  )



(comment
  (def context mysystem/context)

  (mount-routes context)

  )
