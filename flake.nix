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
        buildEdge = pkgs.runCommandLocal "openems-edge" {
          buildInputs = [ pkgs.jdk21_headless pkgs.gradle pkgs.unzip pkgs.bash ];
          src = ./.;
        }''
          set -euo pipefail
          export JAVA_HOME=${pkgs.jdk21_headless}
          export PATH="$JAVA_HOME/bin:$PATH"

          cd "$src"
          # Run Nix-provided Gradle to avoid downloading the Gradle distribution
          export GRADLE_USER_HOME="$PWD/.gradle-home"
          gradle buildEdge --no-build-cache --no-daemon

          mkdir -p "$out"
          # The build task copies the final jar to build/openems-edge.jar
          if [ -f "$src/build/openems-edge.jar" ]; then
            cp "$src/build/openems-edge.jar" "$out/"
          else
            echo "Expected build/openems-edge.jar not found" >&2
            exit 1
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
