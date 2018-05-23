(ns sokoban.config)

(def debug? ^boolean goog.DEBUG)
(def touch-enabled? (exists? js/TouchList))
