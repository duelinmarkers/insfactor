(ns duelinmarkers.insfactor.subjects.macro-def)

(defmacro barfoo [s]
  `(quote ~s))
