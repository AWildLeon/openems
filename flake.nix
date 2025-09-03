{
  description = "OpenEMS Development Tooling and Build System";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/release-25.05";
  };

  outputs = { self, nixpkgs, ... }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };
      src = "/home/leon/openems";

      # Fetch the oh-my-posh theme (from user's provided base)
      theme_omz = builtins.fetchurl {
        url = "https://zsh.onlh.de/theme.omp.json";
        sha256 = "1jd355hilldj4ncf0h28n70qwx43zddzn5xdxamc2y6dmlmxh79c";
      };
    in {
      packages.${system} = {
        openems-helper = pkgs.writeShellScriptBin "openems-helper" ''
          #!/usr/bin/env bash
          # Run the project's local Gradle wrapper using JDK21 from nixpkgs
          export JAVA_HOME=${pkgs.jdk21_headless}
          export PATH="$JAVA_HOME/bin:$PATH"
          exec ./gradlew "$@"
        '';

        # A convenience package to run the buildEdge Gradle task using JDK21
        buildEdge = pkgs.writeShellScriptBin "buildEdge" ''
          #!/usr/bin/env bash
          # Use the Nix-provided JDK21 and run the local Gradle wrapper with the task
          export JAVA_HOME=${pkgs.jdk21_headless}
          export PATH="$JAVA_HOME/bin:$PATH"
          exec ./gradlew buildEdge "$@"
        '';

        # A convenience package to run the buildBackend Gradle task using JDK21
        buildBackend = pkgs.writeShellScriptBin "buildBackend" ''
          #!/usr/bin/env bash
          # Use the Nix-provided JDK21 and run the local Gradle wrapper with the task
          export JAVA_HOME=${pkgs.jdk21_headless}
          export PATH="$JAVA_HOME/bin:$PATH"
          exec ./gradlew buildBackend "$@"
        '';

        # A convenience package to build the Angular UI using Nix NodeJS
        buildUI = pkgs.writeShellScriptBin "buildUI" ''
          #!/usr/bin/env bash
          set -euo pipefail
          # Use Nix-provided NodeJS
          export PATH=${pkgs.nodejs}/bin:$PATH

          # Use default theme 'openems'
          THEME=openems
          cd ui

          # Install dependencies and build
          npm ci
          node_modules/.bin/ng lint || true
          node_modules/.bin/ng build -c "openems,openems-edge-prod,prod"
          echo "Built ui/target"
        '';

        # Copy a locally-built UI (ui/target) into the nix store
        buildUIFromLocal = pkgs.runCommand "openems-ui-from-local" {
          srcDir = ./ui/target;
        }''
          mkdir -p "$out"
          cp -r "$srcDir"/* "$out/"
        '';

        # Copy an existing local backend build artifact into the nix store.
        buildBackendFromLocal = pkgs.runCommand "openems-backend-from-local" {
          srcJar = ./build/openems-backend.jar;
        }''
          mkdir -p "$out"
          cp "$srcJar" "$out/"
        '';

        # compileAll: build backend, edge and UI using local tools (nix run .#compileAll)
        startAll = pkgs.writeShellScriptBin "start-all" ''
          #!/usr/bin/env bash
          set -euo pipefail

          export JAVA_HOME=${pkgs.jdk21_headless}
          export PATH="$JAVA_HOME/bin:$PATH"

          mkdir -p build

          cleanup() {
            echo "Stopping backend (pid=$BACKEND_PID) and edge (pid=$EDGE_PID)"
            if [ -n "$BACKEND_PID" ]; then
              kill "$BACKEND_PID" 2>/dev/null || true
            fi
            if [ -n "$EDGE_PID" ]; then
              kill "$EDGE_PID" 2>/dev/null || true
            fi
            if [ -n "$EDGE_2_PID" ]; then
              kill "$EDGE_2_PID" 2>/dev/null || true
            fi
          }
          trap cleanup INT TERM

          # Start backend (background)
          echo "Starting backend..."
          if [ -f "build/openems-backend.jar" ]; then
            mkdir -p ${src}/build/backend-openems/config ${src}/build/backend-openems/data
            java \
              -Dosgi.clean=true \
              -Dorg.apache.felix.eventadmin.Timeout=0 \
              -Dorg.apache.felix.http.host=0.0.0.0 \
              -Dorg.apache.felix.http.port=8079 \
              -Dfelix.cm.dir=${src}/build/backend-openems/config \
              -Dopenems.data.dir=${src}/build/backend-openems/data \
              -XX:+ExitOnOutOfMemoryError \
              -XX:+UseZGC -XX:+ZGenerational \
              -jar build/openems-backend.jar > build/backend.log 2>&1 &
          else
            ./gradlew :io.openems.backend.application:run > build/backend.log 2>&1 &
          fi
          BACKEND_PID=$!

          sleep 10 # Give backend time to start

          # Start edge (background)
          echo "Starting edge..."
          if [ -f "build/openems-edge.jar" ]; then
            mkdir -p ${src}/build/edge-openems/config ${src}/build/edge-openems/data
            java \
              -Dosgi.clean=true \
              -Dorg.apache.felix.eventadmin.Timeout=0 \
              -Dorg.apache.felix.http.host=0.0.0.0 \
              -Dorg.apache.felix.http.port=8080 \
              -Dfelix.cm.dir=${src}/build/edge-openems/config \
              -Dopenems.data.dir=${src}/build/edge-openems/data \
              -XX:+HeapDumpOnOutOfMemoryError \
              -XX:+ExitOnOutOfMemoryError \
              -jar build/openems-edge.jar > build/edge.log 2>&1 &
          else
            ./gradlew :io.openems.edge.application:run > build/edge.log 2>&1 &
          fi
          EDGE_PID=$!

                    # Start edge (background)
          echo "Starting edge2..."
          if [ -f "build/openems-edge.jar" ]; then
            mkdir -p ${src}/build/edge-openems-2/config ${src}/build/edge-openems-2/data
            java \
              -Dosgi.clean=true \
              -Dorg.apache.felix.eventadmin.Timeout=0 \
              -Dorg.apache.felix.http.host=0.0.0.0 \
              -Dorg.apache.felix.http.port=18181 \
              -Dorg.osgi.service.http.port=18181 \
              -Dfelix.cm.dir=${src}/build/edge-openems-2/config \
              -Dopenems.data.dir=${src}/build/edge-openems-2/data \
              -XX:+HeapDumpOnOutOfMemoryError \
              -XX:+ExitOnOutOfMemoryError \
              -jar build/openems-edge.jar > build/edge2.log 2>&1 &
          else
            # run gradle with JVM options for edge2 so it binds to 18181
            GRADLE_OPTS="-Dorg.apache.felix.http.port=18181 -Dorg.osgi.service.http.port=18181 -Dfelix.cm.dir=${src}/build/edge-openems-2/config -Dopenems.data.dir=${src}/build/edge-openems-2/data" \
              ./gradlew :io.openems.edge.application:run > build/edge2.log 2>&1 &
          fi
          EDGE_2_PID=$!

          # Start Angular dev server in foreground so Ctrl-C stops everything
          if [ -d "ui" ]; then
            echo "Starting Angular dev server (openems-backend-dev) in foreground on http://localhost:4200"
            cd ui
            export PATH=${pkgs.nodejs}/bin:$PATH
            # Run ng serve in foreground; when it exits, cleanup will run
            ./node_modules/.bin/ng serve -c openems-backend-dev
            # Foreground process ended — perform cleanup
            cleanup
            cd ..
          else
            echo "UI directory not found; not starting UI"
            # Wait for background processes if UI not started
            wait $BACKEND_PID $EDGE_PID $EDGE_2_PID
          fi
        '';

        
      };

      devShells.${system}.default = pkgs.mkShell {
        buildInputs = with pkgs; [ jdk21_headless oh-my-posh ];

        # Create a disposable ZDOTDIR with a small .zshrc that initializes
        # oh-my-posh (with the fetched theme), fastfetch and zoxide.
        # Also export JAVA_HOME so the Gradle wrapper and javac pick JDK21.
        shellHook = ''
          # Set JAVA_HOME to the provided jdk21 in the shell
          export JAVA_HOME=${pkgs.jdk21_headless}
          export PATH="$JAVA_HOME/bin:$PATH"

          echo "Welcome to the OpenEMS development shell!"
          eval "$(oh-my-posh init bash --config "${theme_omz}")"
        '';
      };
    };
}
