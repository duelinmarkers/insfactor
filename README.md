# insfactor

Insfactor indexes your project. Currently it provides a find-usages
feature for vars and keywords. Hopefully there will be more to come.

This is new and in active development, so please be forgiving, but also
open a [github issue][issues] if you run into something that doesn't work,
find a problem with the docs, or have a suggestion for improvement.

## Setup

This is the insfactor Clojure library, which needs to be loaded alongside
your code. There's also [insfactor.el][], which provides emacs integration.

You probably want to configure insfactor in your `~/.lein/profiles.clj`
file rather than in your projects. Here are the entries to add (or merge)
in the `:user` map in that file.

    :dependencies [[duelinmarkers/insfactor "0.2.0"]]
    :repl-options {:nrepl-middleware [duelinmarkers.insfactor.nrepl/index-on-load]}

So the whole file might be just the following if you don't have anything
there yet.

    {:user {:dependencies [[duelinmarkers/insfactor "0.2.0"]]
            :repl-options {:nrepl-middleware [duelinmarkers.insfactor.nrepl/index-on-load]}}}

If you prefer to configure insfactor in your project.clj, I'd recommend
putting it in your `:dev` profile.

    (defproject ...
      ...
      :profiles {
        :dev {
          :dependencies [[duelinmarkers/insfactor "0.2.0"]]
          :repl-options {:nrepl-middleware [duelinmarkers.insfactor.nrepl/index-on-load]}}}
      ...)

Now go over to [insfactor.el][] and set that up.

## Usage

* In Emacs, `nrepl-jack-in` to your project (or whatever you do).
* Run `M-x insfactor-index-project`
  * Maybe take a look at the `*Messages*` buffer to see that your source
  directories were indexed.
* Open a source file in your project.
* Put point (the cursor) on some non-macro var reference (for example a
call to some clojure.core fn).
* Run `M-x insfactor-find-usages`.
* You should get a `Usages` buffer listing the files and lines with
references to the var and showing the content of each line.

## Limitations / Known Issues

* Requires Clojure 1.5.1.
* Can't see macro usages, let alone index them.
* Line numbers will often be a bit off.


[issues]: http://github.com/duelinmarkers/insfactor/issues
[insfactor.el]: http://github.com/duelinmarkers/insfactor.el

## License

Copyright © 2013 John D. Hume

Distributed under the Eclipse Public License, the same as Clojure.
