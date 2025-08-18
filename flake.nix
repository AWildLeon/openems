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
