{
  description = "OpenEMS Development Tooling and Build System";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/release-25.05";
  };

  outputs = { self, nixpkgs, ... }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs { inherit system; };
      

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
        compileAll = pkgs.writeShellScriptBin "compile-all" ''
          #!/usr/bin/env bash
          set -euo pipefail

          export JAVA_HOME=${pkgs.jdk21_headless}
          export PATH="$JAVA_HOME/bin:$PATH"

          echo "==> Building backend"
          ./gradlew buildBackend --no-build-cache

          echo "==> Building edge"
          ./gradlew buildEdge --no-build-cache

          echo "==> Building UI"
          cd ui
          npm ci
          node_modules/.bin/ng lint || true
          node_modules/.bin/ng build -c "openems,openems-edge-prod,prod"
          cd ..

          echo "All builds finished"
        '';

        # tarUiTarget: tar.gz the built ui/target directory (nix run .#tarUiTarget)
        tarUiTarget = pkgs.writeShellScriptBin "tar-ui-target" ''
          #!/usr/bin/env bash
          set -euo pipefail

          UI_DIR="ui/target"
          OUT_DIR="build"
          TIMESTAMP=$(date +%Y%m%d%H%M%S)
          OUT_FILE="$OUT_DIR/openems-ui-$TIMESTAMP.tar.gz"

          if [ ! -d "$UI_DIR" ]; then
            echo "UI target directory not found: $UI_DIR"
            echo "Build the UI first (e.g. nix run .#compileAll or nix run .#buildUI)"
            exit 1
          fi

          mkdir -p "$OUT_DIR"
          tar -czf "$OUT_FILE" -C "$UI_DIR" .
          echo "Created $OUT_FILE"
        '';

        # startAll: start backend, edge and serve ui/target for local testing (nix run .#startAll)
        startAll = pkgs.writeShellScriptBin "start-all" ''
          #!/usr/bin/env bash
          set -euo pipefail

          export JAVA_HOME=${pkgs.jdk21_headless}
          export PATH="$JAVA_HOME/bin:$PATH"

          # Start backend
          echo "Starting backend..."
          if [ -f "build/openems-backend.jar" ]; then
            nohup java -jar build/openems-backend.jar > build/backend.log 2>&1 &
          else
            nohup ./gradlew :io.openems.backend.application:run > build/backend.log 2>&1 &
          fi
          BACKEND_PID=$!

          # Start edge
          echo "Starting edge..."
          if [ -f "build/openems-edge.jar" ]; then
            nohup java -jar build/openems-edge.jar > build/edge.log 2>&1 &
          else
            nohup ./gradlew :io.openems.edge.application:run > build/edge.log 2>&1 &
          fi
          EDGE_PID=$!

          # Serve UI
          if [ -d "ui/target" ]; then
            echo "Serving UI on http://localhost:4200"
            (cd ui/target && nohup python3 -m http.server 4200 > ../../build/ui.log 2>&1 &) 
            UI_PID=$!
          else
            echo "UI not built; run 'nix run .#compileAll' or 'nix run .#buildUI' first"
            UI_PID=""
          fi

          if [ -z "$UI_PID" ]; then
            UI_STR="not-started"
          else
            UI_STR="$UI_PID"
          fi
          echo "Started: backend=$BACKEND_PID edge=$EDGE_PID ui=$UI_STR"
          echo "Logs: build/backend.log build/edge.log build/ui.log"
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
