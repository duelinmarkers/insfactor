# insfactor

Insfactor indexes your project. Currently it provides a find-usages
feature. Hopefully there will be more to come.

## Usage

In your project.clj, add `[duelinmarkers/insfactor "0.1.0"]` to your
dev profile's dependencies and the `index-on-load` nrepl middleware.

    :profiles {
      :dev {
        :dependencies [[duelinmarkers/insfactor "0.1.0"]]
        :repl-options {:nrepl-middleware [duelinmarkers.insfactor.nrepl/index-on-load]}}}

TODO: Is it advisible to do this in ~/.lein/profiles.clj rather than
mucking up all your projects?

## License

Copyright © 2013 John D. Hume

Distributed under the Eclipse Public License, the same as Clojure.
