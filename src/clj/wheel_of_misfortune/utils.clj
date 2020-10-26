(ns wheel-of-misfortune.utils
  (:import java.security.MessageDigest))

(def ^:private base62-alphabet
  "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")

(defn current-time [] (quot (System/currentTimeMillis) 1000))

(defn md5 [^String s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        raw       (.digest algorithm (.getBytes s))]
    (BigInteger. 1 raw)))

(defn base62-encode [n]
  (let [base 62]
    (loop [remainder n
           result    ""]
      (if (pos? remainder)
        (recur
         (quot remainder base)
         (str (get base62-alphabet (mod remainder base)) result))
        result))))

(defn shortid [digest]
  (subs (base62-encode (md5 digest)) 0 7))
