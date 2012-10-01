(ns mrfreeze.core
  (:gen-class :main true)
  (:import com.amazonaws.services.glacier.AmazonGlacierClient
           (com.amazonaws.auth AWSCredentials PropertiesCredentials)
           (com.amazonaws.services.glacier.transfer ArchiveTransferManager UploadResult)
           (com.amazonaws.services.glacier.model DescribeVaultOutput DescribeVaultRequest DescribeVaultResult
                                                 ListVaultsRequest ListVaultsResult JobParameters InitiateJobRequest
                                                 DescribeJobRequest GetJobOutputRequest)))

(use '[clojure.tools.cli :only[cli]])
(use 'environ.core)

(defn aws-credentials [access-key secret-key]
  "Return AWS credentials"
  (com.amazonaws.auth.BasicAWSCredentials. access-key secret-key))

(defn glacier-client [credentials]
  "Create a glacier client"
  (let [client (AmazonGlacierClient. credentials)]
    ;; Set it's endpoint to https://glacier.us-east-1.amazonaws.com/
    ;; should change this to be configurable
    (.setEndpoint client "https://glacier.us-east-1.amazonaws.com/")
    client))
  
(defn upload [vault filename credentials]
  "Upload a file to a glacier vault"
    ;; Create an ArchiveTransferManager
  (let [client (glacier-client credentials)
        atm (ArchiveTransferManager. client credentials)
        ;; upload the file
        result (.upload atm vault filename (clojure.java.io/file filename))]
      ;; print the archive id
      (println "Archive ID: " (.getArchiveId result))))

(defn download [vault archive-id filename credentials]
  "Download an archive to a filename"
  (let [client (glacier-client credentials)
        atm (ArchiveTransferManager. client credentials)
        result (.download atm vault archive-id (clojure.java.io/file filename))]
    (println "Downloaded archive...")))


(defn describe-job [vault job-id credentials]
  "Download the result of a job."
  (let [client (glacier-client credentials)
        result (.describeJob client (DescribeJobRequest. vault job-id))]
    result))

(defn download-job-result [vault job-id filename credentials]
  (let [ajob (describe-job vault job-id credentials)]
    (cond
     (false? (.getCompleted ajob)) (println "Job not finished yet.")
     (true? (.getCompleted ajob)) (do
                                    (println "Downloading job...")
                                    (let [client (glacier-client credentials)
                                          job-output-request (GetJobOutputRequest. )
                                          file-handle (clojure.java.io/file filename)]
                                      (.withJobId job-output-request job-id)
                                      (.withVaultName job-output-request vault)
                                      
                                      (let [job-output (.getJobOutput client job-output-request)]
                                        (clojure.java.io/copy (.getBody job-output) file-handle)))))))

(defn print-job-description [vault job-id credentials]
  "Print the description of a job"
  (let [ajob (describe-job vault job-id credentials)]
    (println "Job ID:\t\t" (.getJobId ajob))
    (println "Action:\t\t" (.getAction ajob))
    (println "Creation Date:\t" (.getCreationDate ajob))
    (println "Completed:\t" (.getCompleted ajob))
    (println "StatusCode:\t" (.getStatusCode ajob))))

(defn describe-vault [vault credentials]
  "Print information about what is in a vault"
  (let [client (glacier-client credentials)
        result (.describeVault client (DescribeVaultRequest. vault))]
    (println "Vault:" vault)
    (println "Vault ARN:" (.getVaultARN result))
    (println "Creation Date:" (.getCreationDate result))
    (println "Last Inventory Date:" (.getLastInventoryDate result))
    (println "Number of Archives:" (.getNumberOfArchives result))
    (println "Size in Bytes:" (.getSizeInBytes result))))
    
(defn inventory-vault [vault credentials]
  "Requests an inventory of a vault"
  (let [client (glacier-client credentials)
        parameters (JobParameters.)
        request (InitiateJobRequest.)]
    
    ;; Set the parameters for the job
    (.withType parameters "inventory-retrieval")
    ;; Create the job request
    (.withVaultName request vault)
    (.withJobParameters request parameters)

    (let [result (.initiateJob client request)]
      (println "Inventory job ID:" (.getJobId result)))))



                   
(defn -main [& args]
  (let [[options args banner] (cli args                   
                                   ["-v" "--vault" "Glacier vault to operate on" :default nil]
                                   ["-A" "--action" "One of: upload download inventory describe-vault describe-job download-job-result help" :default nil]
                                   ["-f" "--file" "File" :default nil]
                                   ["-r" "--archive" "Archive ID" :default nil]
                                   ["-h" "--help" "Show help" :default false :flag true]
                                   ["-j" "--job-id" "Job ID" :default nil]
                                   )
        credentials (aws-credentials (env :aws-access-key) (env :aws-secret-key))]
    (when (:help options)
      (println banner)
      (System/exit 0))

    (cond
     (or (nil? (:vault options))
         (nil? (:action options))) (do (println "You must provide AWS credentials, an action, and a vault name!")
                                       (println banner)
                                       (System/exit 1))
         
         (= "help" (:action options)) (do (println banner)
                                          (System/exit 0))

         (= "inventory" (:action options)) (do (inventory-vault
                                                (:vault options)
                                                credentials)
                                               (System/exit 0))
         
         (= "describe-vault" (:action options)) (do (describe-vault
                                                     (:vault options)
                                                     credentials)
                                                    (System/exit 0))
         
         (= "describe-job" (:action options)) (do (print-job-description
                                                   (:vault options)
                                                   (:job-id options)
                                                   credentials)
                                                  (System/exit 0))
         
         (= "download-job-result" (:action options)) (do (download-job-result
                                                          (:vault options)
                                                          (:job-id options)
                                                          (:file options)
                                                          credentials)
                                                         (System/exit 0))
         
         (and (= "download" (:action options))
              (not (nil? (:vault options)))
              (not (nil? (:file options)))
              (not (nil? (:archive options)))) (do (download
                                                     (:vault options)
                                                     (:archive options)
                                                     (:file options)
                                                     credentials)
                                                    (System/exit 0))
              
         (and (= "upload" (:action options))
              (not (nil? (:vault options)))
              (not (nil? (:file options)))) (do (upload
                                                 (:vault options)
                                                 (:file options)
                                                 credentials)
                                                (System/exit 0)))))

