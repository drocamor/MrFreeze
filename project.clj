(defproject mrfreeze "0.0.1"
  :description "CLI tool to puts files on ice...in Amazon Glacier"
  :url "https://github.com/drocamor/MrFreeze"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-localrepo "0.4.0"]]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.cli "0.2.2"]
                 [environ "0.3.0"]
                 [com.amazonaws/aws-java-sdk "1.3.17"]]
  :main mrfreeze.core)
