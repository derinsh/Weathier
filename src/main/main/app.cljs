(ns main.app
  (:require
   [cljs.core.async :as async :refer [go]]
   [cljs.core]
   [cljs.reader]
   ["https" :as https]
   ["expo" :as ex]
   ["react-native" :as rn]
   ["react" :as react]
   [reagent.core :as r]
   [reagent.ratom :as ratom]
   [shadow.expo :as expo]
   [goog.net.XhrIo]()))

(def state (r/atom {:location nil :search "Enter location..."}))
(def backend (r/atom {:lat 0.0 :long 0.0 :response ""}))

(def styles
  ^js (-> {:container
           {:flex 1
            :backgroundColor "#fff"
            :alignItems "center"
            :justifyContent "center"}
           :title
           {:fontWeight "bold"
            :fontSize 24
            :color "white"}
           :subtitle
           {
            :fontSize 20
            :color "grey"
            }
           :norm
           {
            :fontsize 16
            :color "grey"
            }}
          (clj->js)
          (rn/StyleSheet.create)))

(defn lookup []
  (let [xhr (js/goog.net.XhrIo.)
        url (str "https://nominatim.openstreetmap.org/search?q="
                 (js/encodeURIComponent (:location @state))
                 "&format=json&limit=1")]
    (go
      (. xhr (send) url)
      (when (= 200 (.. xhr (getStatus)))
        (let [response (first (cljs.reader/read-string (.. xhr (getResponseText))))
              coordinates (list (get-in response [:lat]) (get-in response [:lon]))]
          (swap! backend assoc :lat (first coordinates) :long (second coordinates)))))))

(defn root []
  [:> rn/View {:style (.-container styles)}
   [:> rn/Text {:style (.-title styles)} "Weather"]
   [:> rn/Text {:style (.-subtitle styles)} "A weather application"]
   [:> rn/Image {:source splash-img :style {:width 200 :height 200}}]
   [:> rn/TextInput {:defaultValue ;;@(r/track :search state)
                     (:search @state)
                     :onChangeText (fn [input]
                                    ;;(go
                                     ;;  (async/put! text input)
                                     ;;  (let [prev @(r/track (:search state))]
                                     ;;    (loop []
                                     ;;      (when-let []))))
                                     (swap! state assoc :search input))
                     }]
   [:> rn/Button {:title "Search"
                  :onPress #(
                              (swap! state assoc :location (:search @state))
                              (go (lookup))
                              )}]
   [:> rn/Text {:style (.-subtitle styles)} (str (:lat @backend) " " (:long @backend))]])

(defn weather [])

(defn start
  {:dev/after-load true}
  []
  (expo/render-root (r/as-element [root])))

(defn init []
  (start))
