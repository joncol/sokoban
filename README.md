# sokoban

This is the sokoban project.

## Development Mode

To start the Figwheel compiler, navigate to the project folder and run the
following command in the terminal:
```
lein figwheel
```
Figwheel will automatically push cljs changes to the browser. The server will be
available at [http://localhost:3000](http://localhost:3000) once Figwheel starts
up.

Figwheel also starts `nREPL` using the value of the `:nrepl-port` in the
`:figwheel` config found in `project.clj`. By default the port is set to `7002`.

### Style Compilation

To compile [sass](https://github.com/tuhlmann/lein-sass) sources and then watch
for changes and recompile until interrupted, run:
```
lein sass auto
```

### Optional Development Tools

Start the browser REPL:

```
$ lein repl
```
The system (server and all) can be started by running:

```clojure
(reset)
```
and stopped by running:
```clojure
(stop)
```

## Building for Release

```
lein do clean, uberjar
```
